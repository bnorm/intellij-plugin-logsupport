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
import net.sf.logsupport.intentions.RemoveLogIfConditionIntention;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog that adds log Ids on selected files.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public class RemoveLogIfConditionsDialog extends AbstractLogLevelAwareDialog {

	static final Key<Set<LogLevel>> INCLUDED_LOG_LEVELS = Key.create("LOG_SUPPORT_REMOVE_IF_INCLUDED_LOG_LEVELS");

	public RemoveLogIfConditionsDialog(Project project, List<VirtualFile> sources) {
		super(project, sources);
		setTitle("Remove 'if' statement from log calls.");
	}

	@Override
	protected Key<Set<LogLevel>> getSelectionStorageKey() {
		return INCLUDED_LOG_LEVELS;
	}

	@Override
	protected Set<LogLevel> getDefaultSelectedLevels(ProjectConfiguration configuration) {
		Set<LogLevel> allLevels = new HashSet<LogLevel>(Arrays.asList(LogLevel.values()));
		allLevels.removeAll(configuration.getLogConfigurations().
				getDefaultLogConfiguration().getConditionalLogLevels());
		return allLevels;
	}

	@Override
	protected String getOptionTitle() {
		return "Remove 'if' statement from log calls of the following log levels:";
	}

	@NotNull
	@Override
	public Runnable getWriteOperation(@NotNull List<PsiFile> files) {
		return new AbstractLogLevelAwareRunnable(files) {

			private final RemoveLogIfConditionIntention intention = new RemoveLogIfConditionIntention();

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

				// We need to apply code formatting when changes occurred.
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
