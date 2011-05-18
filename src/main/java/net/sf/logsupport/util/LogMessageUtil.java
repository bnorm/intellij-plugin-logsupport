/*
 * Copyright 2010, Juergen Kellerer and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.logsupport.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.config.LogLevel;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Defines
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public class LogMessageUtil {

	public static final String DELIMITED_ARTIFACT = "/";
	public static final String VARIABLE_ARTIFACT = "?";

	static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * Converts the given list of log expression to an message map.
	 *
	 * @param expressionList		   The list of expression to convert.
	 * @param mergeConstantExpressions Try to merge constant expression and create only one artifact where possible.
	 * @return A map of log messages, where the unique log message is the key and multiple
	 *         occurrences of the same message is the value.
	 */
	public static Map<LogMessage, List<LogMessage>> toMessages(
			List<PsiMethodCallExpression> expressionList, boolean mergeConstantExpressions) {
		Map<LogMessage, List<LogMessage>> entries = new LinkedHashMap<LogMessage, List<LogMessage>>();

		for (PsiMethodCallExpression expression : expressionList) {
			LogMessage message = new LogMessage(expression, mergeConstantExpressions);
			if (entries.containsKey(message))
				entries.get(message).add(message);
			else
				entries.put(message, new ArrayList<LogMessage>(Arrays.asList(message)));
		}

		return entries;
	}

	/**
	 * Converts the given method call expression to a log message instance.
	 *
	 * @param expression			   the expression to convert.
	 * @param mergeConstantExpressions Try to merge constant expression and create only one artifact where possible.
	 * @return A new instance of LogMessage that is based on the given expression.
	 */
	public static LogMessage toMessage(PsiMethodCallExpression expression, boolean mergeConstantExpressions) {
		return new LogMessage(expression, mergeConstantExpressions);
	}

	/**
	 * Creates a new log message instance with the given values.
	 *
	 * @param id			   The string ID of the message.
	 * @param logLevel		 The log level.
	 * @param logId			The log ID.
	 * @param messageArtifacts The message artifacts as string array.
	 * @return A new instance of log message.
	 */
	public static LogMessage newMessage(String id, String logLevel, String logId, String... messageArtifacts) {
		return new LogMessage(id, logLevel, logId, Arrays.asList(messageArtifacts));
	}

	/**
	 * Describes a single log message.
	 */
	public final static class LogMessage {

		public String source = "";
		public String id = "", logLevel = "", logId = "";
		public List<MessageArtifact> logMessage = new ArrayList<MessageArtifact>();

		private LogMessage(String id, String logLevel, String logId, List<String> messageArtifacts) {
			this.id = id;
			this.logLevel = logLevel;
			this.logId = logId;
			for (String s : messageArtifacts)
				logMessage.add(new MessageArtifact(s));
		}

		private LogMessage(PsiMethodCallExpression callExpression, boolean mergeConstantExpressions) {
			VirtualFile sourceFile = callExpression.getContainingFile().getVirtualFile();
			if (sourceFile != null) {
				source = sourceFile.getPresentableUrl();
				VirtualFile baseDir = callExpression.getProject().getBaseDir();
				if (baseDir != null)
					source = source.substring(baseDir.getPresentableUrl().length());

				Document document = callExpression.getContainingFile().getViewProvider().getDocument();
				if (document != null)
					source += ":" + document.getLineNumber(callExpression.getTextOffset());
			}

			// Extract LogLevel
			LogLevel ll = LogPsiUtil.findLogLevel(callExpression);
			if (ll != null)
				logLevel = ll.name();

			PsiLiteralExpression literalExpression =
					LogPsiUtil.findSupportedLiteralExpression(callExpression.getArgumentList());

			if (literalExpression != null) {
				// Extract ID
				LogConfiguration config = LogConfiguration.getInstance(callExpression.getContainingFile());
				if (config != null) {
					NumericLogIdGenerator gen = config.getLogIdGenerator();
					if (gen != null) {
						String t = literalExpression.getText();
						logId = t.length() > 2 ? gen.extractId(t.substring(1, t.length() - 1)) : "";
					}
				}

				// Extract message artifacts
				PsiExpression pe = literalExpression;
				while (pe.getParent() instanceof PsiBinaryExpression)
					pe = (PsiExpression) pe.getParent();
				MessageArtifact.build(pe, mergeConstantExpressions, logMessage);
			}

			try {
				StringBuilder b = new StringBuilder();
				b.append(logLevel).append(logId).append('-');
				for (MessageArtifact artifact : logMessage)
					b.append(artifact);

				MessageDigest md = MessageDigest.getInstance("md5");
				id = new String(Hex.encodeHex(md.digest(b.toString().getBytes(UTF8))));
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for (MessageArtifact artifact : logMessage)
				b.append(artifact.toString());
			return b.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof LogMessage)) return false;
			LogMessage message = (LogMessage) o;
			return id.equals(message.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	/**
	 * Wraps a single log message artifact.
	 */
	public final static class MessageArtifact {

		static void build(PsiExpression expression, boolean mergeConstantExpressions, List<MessageArtifact> out) {
			if (expression == null)
				return;
			if (expression instanceof PsiLiteralExpression)
				out.add(new MessageArtifact((PsiLiteralExpression) expression));
			else if (expression instanceof PsiBinaryExpression) {
				PsiBinaryExpression binaryExpression = (PsiBinaryExpression) expression;
				Object result = LogPsiUtil.computeConstantExpression(expression);
				boolean mergeable = result != null;

				if (mergeable && mergeConstantExpressions)
					out.add(new MessageArtifact(result.toString()));
				else {
					build(binaryExpression.getLOperand(), mergeConstantExpressions, out);
					if (mergeable)
						out.add(new MessageArtifact(DELIMITED_ARTIFACT));
					build(binaryExpression.getROperand(), mergeConstantExpressions, out);
				}
			} else
				out.add(new MessageArtifact(VARIABLE_ARTIFACT));
		}

		private String constantValue = "";
		private PsiLiteralExpression value;

		private MessageArtifact(String constantValue) {
			this.constantValue = constantValue;
		}

		private MessageArtifact(PsiLiteralExpression expression) {
			String text = expression.getText();
			PsiType type = expression.getType();
			if (type != null && (type.equalsToText("java.lang.String") || type.equalsToText("char"))) {
				value = expression;
				int len = text.length();
				if (len > 2)
					constantValue = text.substring(1, len - 1);
			} else
				constantValue = text;
		}

		public PsiLiteralExpression getValue() {
			return value;
		}

		public boolean isEditable() {
			return value != null;
		}

		@Override
		public String toString() {
			return constantValue;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof MessageArtifact)) return false;
			MessageArtifact that = (MessageArtifact) o;
			return !(constantValue != null ? !constantValue.equals(that.constantValue) : that.constantValue != null);
		}

		@Override
		public int hashCode() {
			return constantValue != null ? constantValue.hashCode() : 0;
		}
	}

	private LogMessageUtil() {
	}
}
