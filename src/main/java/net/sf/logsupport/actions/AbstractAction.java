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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.Nullable;

/**
 * Is the common base class for all actions.
 *
 * @author Juergen_Kellerer, 2010-04-29
 * @version 1.0
 */
public abstract class AbstractAction extends AnAction {

	protected boolean isVisible(AnActionEvent e, Project project) {
		return true;
	}

	protected boolean isEnabled(AnActionEvent e, Project project) {
		return true;
	}

	protected abstract void projectActionPerformed(AnActionEvent e, final Project project);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(AnActionEvent e) {
		super.update(e);

		final Project project = getProject(e);

		Presentation presentation = e.getPresentation();
		presentation.setVisible(project != null && isVisible(e, project));
		presentation.setEnabled(project != null && isEnabled(e, project));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = getProject(e);
		if (project == null)
			return;

		PsiDocumentManager.getInstance(project).commitAllDocuments();

		if (isEnabled(e, project))
			projectActionPerformed(e, project);
	}

	/**
	 * Returns the project for the given event, if the event was fired under a project context.
	 *
	 * @param event The event to extract the project instance from.
	 * @return the project for the given event, if the event was fired under a project context.
	 */
	@Nullable
	protected Project getProject(AnActionEvent event) {
		final DataContext context = event.getDataContext();
		return PlatformDataKeys.PROJECT.getData(context);
	}	
}
