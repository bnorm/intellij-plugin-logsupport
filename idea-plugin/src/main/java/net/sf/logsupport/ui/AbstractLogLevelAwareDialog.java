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

package net.sf.logsupport.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.config.ProjectConfiguration;
import net.sf.logsupport.util.LogPsiUtil;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * Implements a processing dialog that supports log level filtering.
 *
 * @author Juergen_Kellerer, 2010-04-18
 * @version 1.0
 */
public abstract class AbstractLogLevelAwareDialog extends AbstractProcessingDialog {

	static final Key<Set<LogLevel>> INCLUDED_LOG_LEVELS = Key.create("LOG_SUPPORT_INCLUDED_LOG_LEVELS");

	protected final LogLevelSelectionPanel selectionPanel = new LogLevelSelectionPanel(3);

	protected AbstractLogLevelAwareDialog(Project project, List<VirtualFile> sources) {
		super(project, sources);

		((TitledBorder) optionsPanel.getBorder()).setTitle(getOptionTitle());
		optionsPanel.add(BorderLayout.LINE_START, selectionPanel);

		Set<LogLevel> selectedLevels = project.getUserData(getSelectionStorageKey());
		if (selectedLevels == null)
			selectedLevels = getDefaultSelectedLevels(ProjectConfiguration.getInstance(project));

		selectionPanel.setSelectedLevels(selectedLevels);
		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Set<LogLevel> logLevels = selectionPanel.getSelectedLevels();
				getProject().putUserData(INCLUDED_LOG_LEVELS, logLevels);
				setOKActionEnabled(!logLevels.isEmpty());
			}
		};
		selectionPanel.addChangeListener(listener);
		listener.stateChanged(null);
	}

	protected Key<Set<LogLevel>> getSelectionStorageKey() {
		return INCLUDED_LOG_LEVELS;
	}

	protected Set<LogLevel> getDefaultSelectedLevels(ProjectConfiguration configuration) {
		return configuration.getLogConfigurations().getDefaultLogConfiguration().getLogIdLevels();
	}

	protected abstract String getOptionTitle();

	protected boolean isLoggerCallInSelectedLevel(PsiMethodCallExpression expression) {
		if (selectionPanel.isAllSelected())
			return true;

		LogLevel level = LogPsiUtil.findLogLevel(expression);
		return level != null && selectionPanel.isLevelSelected(level);
	}

	protected abstract class AbstractLogLevelAwareRunnable implements Runnable {

		private final List<PsiFile> files;
		private boolean changed;

		protected AbstractLogLevelAwareRunnable(List<PsiFile> files) {
			this.files = files;
		}

		public void run() {
			for (PsiFile psiFile : files) {
				if (!psiFile.isWritable())
					continue;

				processFile(psiFile);
			}
		}

		protected void processFile(PsiFile psiFile) {
			changed = false;
			for (PsiMethodCallExpression expression : LogPsiUtil.findSupportedLoggerCalls(psiFile)) {
				if (!isLoggerCallInSelectedLevel(expression))
					continue;
				processExpression(expression);
			}

			// Committing the changes
			if (isChanged()) {
				PsiDocumentManager manager = PsiDocumentManager.getInstance(psiFile.getProject());
				Document doc = psiFile.getViewProvider().getDocument();
				if (doc == null)
					manager.commitAllDocuments();
				else
					manager.commitDocument(doc);
			}
		}

		protected void markChanged() {
			changed = true;
		}

		protected boolean isChanged() {
			return changed;
		}

		protected abstract void processExpression(PsiMethodCallExpression expression);
	}
}
