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

package net.sf.logsupport.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.L10N;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.util.LogMessageUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.MissingFormatArgumentException;

/**
 * Verifies formatted log messages.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public class VerifyFormattedMessage extends AbstractFormattedMessageInspection {
	/**
	 * {@inheritDoc}
	 */
	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return L10N.message("Inspections.VerifyFormattedMessage.name");
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public String getShortName() {
		return "VerifyFormattedMessage";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String checkLogMethodCall(@NotNull LogFramework framework,
									 @NotNull PsiMethodCallExpression expression,
									 @NotNull InspectionManager manager, boolean isOnTheFly) {
		String logMessage = LogMessageUtil.toMessage(expression, true).toString();
		List<Object> callArgumentDefaults = getLogCallArgumentDefaults(expression, false);

		switch (framework.getLogMessageFormatType()) {
			case printf:
				return evaluatePrintf(logMessage, callArgumentDefaults.toArray());
			case messageformat:
				return evaluateMessageFormat(logMessage, callArgumentDefaults.toArray());
			case custom:
				String pattern = framework.getPlaceholderCustomFormat();
				return evaluateCustom(logMessage, pattern, callArgumentDefaults.toArray());
		}

		return null;
	}

	String evaluatePrintf(String logMessage, Object... args) {
		try {
			new Formatter().format(logMessage, args);
		} catch (IllegalFormatException e) {
			return createInvalidFormatMessage(e.getLocalizedMessage());
		}
		return null;
	}

	String evaluateMessageFormat(String logMessage, Object... args) {
		MessageFormat mf = null;
		try {
			mf = new MessageFormat(logMessage);
		} catch (Exception e) {
			return createInvalidFormatMessage(e.getMessage());
		}

		Format[] formats = mf.getFormatsByArgumentIndex();

		if (formats.length != args.length)
			return createInvalidArgumentCountMessage(formats.length, args.length);

		for (int i = 0; i < formats.length; i++) {
			Format format = formats[i];
			if (format == null)
				continue;
			try {
				format.format(args[i]);
			} catch (IllegalArgumentException e) {
				// TODO Log
				return createInvalidArgumentTypeMessage(i + 1);
			}
		}
		return null;
	}

	String evaluateCustom(String logMessage, String placeholderPattern, Object... args) {
		logMessage = ' ' + logMessage + ' ';
		String[] elements = logMessage.split(placeholderPattern);
		int placeHolderCount = elements == null ? 0 : elements.length - 1;
		if (placeHolderCount != args.length)
			return createInvalidArgumentCountMessage(placeHolderCount, args.length);
		return null;
	}

	String createInvalidArgumentCountMessage(int expects, int have) {
		return L10N.message("Inspections.VerifyFormattedMessage.invalidArgumentCount", expects, have);
	}

	String createInvalidArgumentTypeMessage(int argumentIndex) {
		return L10N.message("Inspections.VerifyFormattedMessage.invalidArgumentType", argumentIndex);
	}

	String createInvalidFormatMessage(String message) {
		return L10N.message("Inspections.VerifyFormattedMessage.invalidFormat", message);
	}
}
