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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.intentions.RemoveLogIdIntention;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.sf.logsupport.util.LogPsiUtil.findSupportedLiteralExpression;

/**
 * Dialog that removes log Ids on selected files.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public class RemoveLogIdsDialog extends AbstractLogLevelAwareDialog {

	public RemoveLogIdsDialog(Project project, List<VirtualFile> sources) {
		super(project, sources);
		setTitle("Remove All Log IDs");
	}

	@Override
	protected String getOptionTitle() {
		return "Remove all IDs from the following log levels:";
	}

	@NotNull
	@Override
	public Runnable getWriteOperation(@NotNull List<PsiFile> files) {
		return new LogLevelAwareRunnable(files) {

			private final RemoveLogIdIntention removeLogIdIntention = new RemoveLogIdIntention();

			@Override
			protected void processExpression(PsiMethodCallExpression expression) {
				PsiLiteralExpression le = findSupportedLiteralExpression(expression.getArgumentList());
				removeLogIdIntention.invoke(le);
			}
		};
	}
}