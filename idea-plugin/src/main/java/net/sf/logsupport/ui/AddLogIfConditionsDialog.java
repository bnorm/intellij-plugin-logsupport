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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.config.ProjectConfiguration;
import net.sf.logsupport.intentions.AddLogIfConditionIntention;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Dialog that adds log Ids on selected files.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public class AddLogIfConditionsDialog extends AbstractLogLevelAwareDialog {

	static final Key<Set<LogLevel>> INCLUDED_LOG_LEVELS = Key.create("LOG_SUPPORT_ADD_IF_INCLUDED_LOG_LEVELS");

	public AddLogIfConditionsDialog(Project project, List<VirtualFile> sources) {
		super(project, sources);
		setTitle("Add missing 'if' statements.");

		selectionPanel.setEnabledLevels(getDefaultSelectedLevels(ProjectConfiguration.getInstance(project)));
	}

	@Override
	protected Key<Set<LogLevel>> getSelectionStorageKey() {
		return INCLUDED_LOG_LEVELS;
	}

	@Override
	protected Set<LogLevel> getDefaultSelectedLevels(ProjectConfiguration configuration) {
		return configuration.getLogConfigurations().getDefaultLogConfiguration().getConditionalLogLevels();
	}

	@Override
	protected String getOptionTitle() {
		return "Add missing 'if' statements to the following log levels:";
	}

	@NotNull
	@Override
	public Runnable getWriteOperation(@NotNull List<PsiFile> files) {
		return new AbstractLogLevelAwareRunnable(files) {

			private final AddLogIfConditionIntention intention = new AddLogIfConditionIntention();


			@Override
			protected void processExpression(PsiMethodCallExpression expression) {
				if (expression != null && intention.isAvailable(expression)) {
					intention.doInvoke(expression);
					markChanged();
				}
			}

			@Override
			protected void processFile(PsiFile psiFile) {
				super.processFile(psiFile);

				if (isChanged()) {
					// THIS DOESN'T WORK (and it doesn't seem to be necessary either...)
					/*ReformatCodeProcessor processor =
							new ReformatCodeProcessor(psiFile.getProject(), psiFile, psiFile.getTextRange());
					processor.runWithoutProgress();*/
				}
			}
		};
	}
}
