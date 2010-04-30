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

package net.sf.logsupport.intentions;

import com.intellij.codeInsight.intention.AbstractIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.util.IncorrectOperationException;
import net.sf.logsupport.config.LogConfiguration;
import org.jetbrains.annotations.NotNull;

import static net.sf.logsupport.util.LogPsiUtil.findSupportedMethodCallExpression;

/**
 * Defines a common base class for log related intentions.
 *
 * @author Juergen_Kellerer, 2010-04-27
 * @version 1.0
 */
public abstract class AbstractLogIntention extends AbstractIntentionAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startInWriteAction() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		PsiMethodCallExpression expression = findSupportedMethodCallExpression(editor, file);
		return expression != null && isAvailable(expression);
	}

	/**
	 * {@inheritDoc}
	 */
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		invoke(findSupportedMethodCallExpression(editor, file));
	}

	public void invoke(PsiMethodCallExpression expression) {
		if (expression != null && isAvailable(expression))
			doInvoke(expression);
	}

	public abstract void doInvoke(PsiMethodCallExpression expression);

	public abstract boolean isAvailable(PsiMethodCallExpression expression);
}
