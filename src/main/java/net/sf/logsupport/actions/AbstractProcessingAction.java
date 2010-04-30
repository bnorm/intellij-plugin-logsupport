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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import net.sf.logsupport.ui.AbstractProcessingDialog;
import net.sf.logsupport.util.VirtualFileUtil;

import java.util.List;

import static net.sf.logsupport.util.VirtualFileUtil.getSelectedFiles;
import static net.sf.logsupport.util.VirtualFileUtil.toSupportedFiles;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public abstract class AbstractProcessingAction extends AbstractAction {

	public AbstractProcessingAction() {
	}

	protected boolean isVisible(AnActionEvent e, Project project) {
		return !getSelection(project).isEmpty();
	}

	protected List<VirtualFile> getSelection(Project project) {
		return toSupportedFiles(getSelectedFiles(project), true);
	}

	protected abstract AbstractProcessingDialog createDialog(Project project);

	public void projectActionPerformed(AnActionEvent e, final Project project) {
		final AbstractProcessingDialog dialog = createDialog(project);
		dialog.show();
		if (dialog.isOK()) {
			PsiManager manager = PsiManager.getInstance(project);
			PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

			Computable<List<PsiFile>> readAction = dialog.getReadOperation();
			try {
				final List<PsiFile> processableFiles = documentManager.commitAndRunReadAction(readAction);

				VirtualFileUtil.makeFilesWritable(project, processableFiles);
				documentManager.commitAllDocuments();

				manager.startBatchFilesProcessingMode();
				try {
					ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
						public void run() {
							new WriteCommandAction(project, dialog.getTitle(), null) {
								protected void run(Result result) throws Throwable {
									dialog.getWriteOperation(processableFiles).run();
								}
							}.execute();
						}
					}, dialog.getTitle(), true, project);
				} finally {
					manager.finishBatchFilesProcessingMode();
				}
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}
}
