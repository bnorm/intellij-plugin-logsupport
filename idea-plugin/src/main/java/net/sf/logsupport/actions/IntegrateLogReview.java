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

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import net.sf.logsupport.ui.IntegrateLogReviewDialog;
import net.sf.logsupport.ui.util.Dialogs;
import net.sf.logsupport.util.LogPsiElementFactory;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.ReflectionUtil;
import net.sf.logsupport.util.VirtualFileUtil;

import java.util.*;

import static net.sf.logsupport.L10N.message;
import static net.sf.logsupport.util.LogMessageUtil.*;

/**
 * Implements the integration of log reviews.
 *
 * @author Juergen_Kellerer, 2010-04-15
 * @version 1.0
 */
public class IntegrateLogReview extends AbstractAction {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.actions.IntegrateLogReview");

	@Override
	protected void projectActionPerformed(AnActionEvent e, final Project project) {
		IntegrateLogReviewDialog dialog = new IntegrateLogReviewDialog(project);

		dialog.show();

		if (dialog.isOK()) {
			final List<LogMessage> reviewedMessages = dialog.getReviewedMessages();

			PsiDocumentManager.getInstance(project).commitAllDocuments();

			final PsiManager manager = PsiManager.getInstance(project);
			final Map<PsiFile, Map<LogMessage, List<LogMessage>>> mappedMessages = getMappedMessages(manager);

			LOG.info("Identifying files that contain messages that were changed in the log review process.");
			final int pendingChangeCount = removeAllUnchangedMessages(mappedMessages, reviewedMessages);

			if (mappedMessages.isEmpty()) {
				LOG.info("Not applying any reviewed message, operation was either canceled or " +
						"review did not contain any changes.");
				return;
			}

			if (!Dialogs.confirmOverwrite(message("IntegrateLogReview.integrateConfirmation",
					pendingChangeCount, mappedMessages.size()))) {

				LOG.info(String.format("Aborted the integration of %d messages in %d files.",
						pendingChangeCount, mappedMessages.size()));
				return;
			}

			final ProgressManager pm = ProgressManager.getInstance();
			pm.runProcessWithProgressSynchronously(new Runnable() {
						public void run() {
							final ProgressIndicator indicator = pm.getProgressIndicator();
							indicator.setIndeterminate(true);

							manager.startBatchFilesProcessingMode();
							try {
								final String title = message("IntegrateLogReview.integrateApplyJobTitle", pendingChangeCount);

								Set<PsiFile> processableFiles = mappedMessages.keySet();
								new WriteCommandAction(project, title,
										processableFiles.toArray(new PsiFile[processableFiles.size()])) {
									protected void run(Result result) throws Throwable {
										indicator.setText(title);
										getWriteOperation(mappedMessages, reviewedMessages, indicator).run();
									}
								}.execute();
							} finally {
								manager.finishBatchFilesProcessingMode();
							}
						}
					}, dialog.getTitle(), true, project);
		}
	}

	/**
	 * Search all log messages and maps them against the PsiFile that contained the message.
	 * Similar messages are grouped under the first message used as key.
	 *
	 * @param manager the PsiManager to use.
	 * @return a map of PsiFile to the log messages it contains.
	 */
	private Map<PsiFile, Map<LogMessage, List<LogMessage>>> getMappedMessages(PsiManager manager) {
		Map<PsiFile, Map<LogMessage, List<LogMessage>>> messageMap =
				new HashMap<PsiFile, Map<LogMessage, List<LogMessage>>>();

		List<VirtualFile> allFiles = VirtualFileUtil.toFiles(manager.getProject(),
				VirtualFileUtil.getSourceDirectories(manager.getProject(), true), true);
		allFiles = VirtualFileUtil.toSupportedFiles(allFiles, false);

		for (VirtualFile virtualFile : allFiles) {
			checkCanceled();
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

	/**
	 * Normalizes the pending changes by removing all unchanged messages.
	 *
	 * @param mappedMessages   The messages contained inside the project mapped against the PsiFile.
	 * @param reviewedMessages A flat list of all reviewed messages that are pending their integration.
	 * @return The count of pending of changes.
	 */
	private int removeAllUnchangedMessages(
			Map<PsiFile, Map<LogMessage, List<LogMessage>>> mappedMessages,
			List<LogMessage> reviewedMessages) {

		int pendingChangeCount = 0;

		reduceLoop:
		for (Iterator<Map<LogMessage, List<LogMessage>>> i = mappedMessages.values().iterator(); i.hasNext(); ) {
			checkCanceled();

			Map<LogMessage, List<LogMessage>> fileMessages = i.next();
			for (LogMessage message : reviewedMessages) {
				List<LogMessage> similarFileMessages = fileMessages.get(message);
				if (similarFileMessages != null) {
					Iterator<LogMessage> mi = similarFileMessages.iterator();
					while (mi.hasNext()) {
						LogMessage m = mi.next();

						if (m.getLogMessage().equals(message.getLogMessage())) {
							if (LOG.isDebugEnabled())
								LOG.debug("Not integrating unchanged log message '" + message + "'");
							mi.remove();
						}

						if (m.getLogMessage().size() != message.getLogMessage().size()) {
							LOG.warn("The log message artifacts differ in length, " +
									"not applying reviewed message '" + message + "' to '" + m + "'");
							mi.remove();
						}
					}

					if (!similarFileMessages.isEmpty()) {
						pendingChangeCount += similarFileMessages.size();
						continue reduceLoop;
					}
				}
			}
			i.remove();
		}

		return pendingChangeCount;
	}

	/**
	 * Returns the actual write operation.
	 *
	 * @param mappedMessages   The messages contained inside the project mapped against the PsiFile.
	 * @param reviewedMessages A flat list of all reviewed messages that are pending their integration.
	 * @param indicator		The indicator to use for specifying the overall progress.
	 * @return A runnable that performs the actual writes.
	 */
	private Runnable getWriteOperation(
			final Map<PsiFile, Map<LogMessage, List<LogMessage>>> mappedMessages,
			final List<LogMessage> reviewedMessages, final ProgressIndicator indicator) {

		return new Runnable() {
			public void run() {
				indicator.setIndeterminate(false);
				double processedFiles = 0, totalFiles = mappedMessages.size();

				for (Map.Entry<PsiFile, Map<LogMessage, List<LogMessage>>> entry : mappedMessages.entrySet()) {
					checkCanceled();

					final PsiFile file = entry.getKey();
					final LogPsiElementFactory elementFactory = LogPsiUtil.getFactory(file);
					final Map<LogMessage, List<LogMessage>> fileMessages = entry.getValue();

					VirtualFile virtualFile = file.getVirtualFile();
					if (virtualFile != null)
						indicator.setText2(virtualFile.getPresentableUrl());

					boolean changed = false;
					for (LogMessage message : reviewedMessages) {
						List<LogMessage> msgs = fileMessages.get(message);
						if (msgs != null) {
							for (LogMessage msg : msgs) {
								Iterator<MessageArtifact> source = message.getLogMessage().iterator();
								Iterator<MessageArtifact> target = msg.getLogMessage().iterator();

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

					processedFiles++;
					indicator.setFraction(processedFiles / totalFiles);
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

				return (PsiLiteralExpression) elementFactory.
						createExpressionFromText(string.append('"').toString(), context);
			}
		};
	}

	static void checkCanceled() {
		try {
			ReflectionUtil.invoke(ProgressManager.getInstance(), "checkCanceled");
		} catch (RuntimeException e) {
			if (e.getCause() instanceof ProcessCanceledException)
				throw (ProcessCanceledException) e.getCause();
			else
				LOG.warn("Failed checking whether the log review integration was cancelled.", e);
		}
	}
}
