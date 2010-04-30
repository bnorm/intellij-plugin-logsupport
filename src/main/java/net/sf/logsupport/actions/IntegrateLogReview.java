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

package net.sf.logsupport.actions;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.CommandProcessor;
import com.intellij.openapi.wm.impl.commands.FinalizableCommand;
import com.intellij.openapi.wm.impl.commands.InvokeLaterCmd;
import com.intellij.psi.*;
import net.sf.logsupport.ui.IntegrateLogReviewDialog;
import net.sf.logsupport.ui.util.Dialogs;
import net.sf.logsupport.util.LogPsiElementFactory;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.LogReviewCodec;
import net.sf.logsupport.util.VirtualFileUtil;

import java.io.IOException;
import java.util.*;

import static net.sf.logsupport.util.LogMessageUtil.*;

/**
 * Implements the integration of log reviews.
 *
 * @author Juergen_Kellerer, 2010-04-15
 * @version 1.0
 */
public class IntegrateLogReview extends AbstractAction {

	@Override
	protected void projectActionPerformed(AnActionEvent e, final Project project) {
		IntegrateLogReviewDialog dialog = new IntegrateLogReviewDialog(project);
		dialog.show();
		if (dialog.isOK()) {
			PsiManager manager = PsiManager.getInstance(project);
			PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

			List<LogMessage> messages;
			LogReviewCodec codec = new LogReviewCodec(dialog.getReviewFile());
			try {
				messages = codec.decode();
			} catch (IOException e1) {
				Messages.showErrorDialog(project,
						"Cannot read the contents of the given XHTML file.",
						"Integration Failed");
				return;
			}

			documentManager.commitAllDocuments();
			final Map<PsiFile, Map<LogMessage, List<LogMessage>>> mappedMessages = getMappedMessages(manager);

			// Remove all files that do not match or do not contain changes
			int messageCount = 0;

			reduceLoop:
			for (Iterator<Map<LogMessage, List<LogMessage>>> i = mappedMessages.values().iterator(); i.hasNext();) {
				Map<LogMessage, List<LogMessage>> fileMessages = i.next();
				for (LogMessage message : messages) {
					List<LogMessage> msgs = fileMessages.get(message);
					if (msgs != null) {
						Iterator<LogMessage> mi = msgs.iterator();
						while (mi.hasNext())
							if (mi.next().logMessage.equals(message.logMessage))
								mi.remove();

						if (!msgs.isEmpty()) {
							messageCount += msgs.size();
							continue reduceLoop;
						}
					}
				}
				i.remove();
			}

			if (mappedMessages.isEmpty())
				return;

			if (!Dialogs.confirmOverwrite(String.format(
					"%d log messages in %d files", messageCount, mappedMessages.size())))
				return;

			VirtualFileUtil.makeFilesWritable(project, mappedMessages.keySet());

			manager.startBatchFilesProcessingMode();
			try {
				final List<LogMessage> reviewedMessages = messages;
				final String title = "Integrating all changed log messages.";

				ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
					public void run() {
						new WriteCommandAction(project, title, null) {
							protected void run(Result result) throws Throwable {
								getWriteOperation(mappedMessages, reviewedMessages).run();
							}
						}.execute();
      				}
    			}, title, true, project);
			} finally {
				manager.finishBatchFilesProcessingMode();
			}
		}
	}

	protected Map<PsiFile, Map<LogMessage, List<LogMessage>>> getMappedMessages(PsiManager manager) {
		Map<PsiFile, Map<LogMessage, List<LogMessage>>> messageMap =
				new HashMap<PsiFile, Map<LogMessage, List<LogMessage>>>();

		List<VirtualFile> allFiles = VirtualFileUtil.toFiles(manager.getProject(),
				VirtualFileUtil.getSourceDirectories(manager.getProject(), true), true);
		allFiles = VirtualFileUtil.toSupportedFiles(allFiles, false);

		for (VirtualFile virtualFile : allFiles) {
			PsiFile file = manager.findFile(virtualFile);
			if (file == null)
				continue;

			List<PsiMethodCallExpression> calls = LogPsiUtil.findSupportedLoggerCalls(file);
			if (calls.isEmpty())
				continue;

			messageMap.put(file, toMessages(calls, false));
		}

		return messageMap;
	}

	protected Runnable getWriteOperation(
			final Map<PsiFile, Map<LogMessage, List<LogMessage>>> mappedMessages,
			final List<LogMessage> reviewedMessages) {
		
		return new Runnable() {
			public void run() {

				for (Map.Entry<PsiFile, Map<LogMessage, List<LogMessage>>> entry : mappedMessages.entrySet()) {
					final PsiFile file = entry.getKey();
					final LogPsiElementFactory elementFactory = LogPsiUtil.getFactory(file);
					final Map<LogMessage, List<LogMessage>> fileMessages = entry.getValue();

					boolean changed = false;
					for (LogMessage message : reviewedMessages) {
						List<LogMessage> msgs = fileMessages.get(message);
						if (msgs != null) {
							for (LogMessage msg : msgs) {
								if (msg.logMessage.size() != message.logMessage.size()) {
									// TODO: Log warning!
									continue;
								}

								Iterator<MessageArtifact> source = message.logMessage.iterator();
								Iterator<MessageArtifact> target = msg.logMessage.iterator();

								while (source.hasNext() && target.hasNext()) {
									MessageArtifact sma = source.next();
									MessageArtifact tma = target.next();
									if (tma.isEditable()) {
										tma.getValue().replace(toLiteralExpression(elementFactory,
												sma.toString(), tma.getValue().getContext()));
										changed = true;
									}
								}
							}
						}
					}

					if (changed) {
						ReformatCodeProcessor codeProcessor = new ReformatCodeProcessor(
								file.getProject(), file, file.getTextRange());
						codeProcessor.runWithoutProgress();
					}
				}
			}

			PsiLiteralExpression toLiteralExpression(LogPsiElementFactory elementFactory,
													 String text, PsiElement context) {
				String delimiters = "\"\n\r\\";
				StringBuilder string = new StringBuilder(text.length() + 4).append('"');
				StringTokenizer t = new StringTokenizer(text, delimiters, true);
				
				while (t.hasMoreTokens()) {
					String token = t.nextToken();
					if (token.length() == 1 && delimiters.indexOf(token.charAt(0)) != -1)
						string.append('\\').append(token);
					else
						string.append(token);
				}

				return (PsiLiteralExpression)
						elementFactory.createExpressionFromText(string.append('"').toString(), context);
			}
		};
	}
}
