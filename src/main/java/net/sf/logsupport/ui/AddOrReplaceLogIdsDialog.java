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
import net.sf.logsupport.intentions.AddLogIdIntention;
import net.sf.logsupport.intentions.ReplaceLogIdIntention;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static net.sf.logsupport.util.LogPsiUtil.findSupportedLiteralExpression;

/**
 * Dialog that add or replaces log Ids on selected files.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public class AddOrReplaceLogIdsDialog extends AbstractLogLevelAwareDialog {

	private final JCheckBox replaceAll = new JCheckBox(
			"Replace all existing IDs, instead of adding missing and adjusting the format.");

	public AddOrReplaceLogIdsDialog(Project project, List<VirtualFile> sources) {
		super(project, sources);
		setTitle("Add or Replace Log IDs");
		optionsPanel.add(BorderLayout.SOUTH, replaceAll);
	}

	@Override
	protected String getOptionTitle() {
		return "Options";
	}

	@NotNull
	@Override
	public Runnable getWriteOperation(@NotNull List<PsiFile> files) {
		return new AbstractLogLevelAwareRunnable(files) {

			private final AddLogIdIntention addLogIdIntention = new AddLogIdIntention();
			private final ReplaceLogIdIntention replaceLogIdIntention = new ReplaceLogIdIntention();

			{
				boolean replaceAll = AddOrReplaceLogIdsDialog.this.replaceAll.isSelected();
				replaceLogIdIntention.setForceReplace(replaceAll);
			}

			@Override
			protected void processExpression(PsiMethodCallExpression expression) {
				PsiLiteralExpression le = findSupportedLiteralExpression(expression.getArgumentList());

				if (addLogIdIntention.isAvailable(le))
					addLogIdIntention.invoke(le);
				else
					replaceLogIdIntention.invoke(le);

				markChanged();
			}
		};
	}
}