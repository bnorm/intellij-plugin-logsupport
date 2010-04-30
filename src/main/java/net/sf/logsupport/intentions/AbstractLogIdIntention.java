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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.util.LogIdGenerator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static net.sf.logsupport.util.LogPsiUtil.*;

/**
 * Defines a common base for intention actions that add, remove or replace log IDs.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public abstract class AbstractLogIdIntention extends AbstractIntentionAction {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startInWriteAction() {
		return true;
	}

	/**
	 * Returns true if the intention is available for the given input.
	 *
	 * @param literalExpression The literal expression used to evaluate.
	 * @return true if the intention is available for the given input.
	 */
	public abstract boolean isAvailable(PsiLiteralExpression literalExpression);

	/**
	 * Adjusts the ID in the given literal text.
	 *
	 * @param target	  The target class to create the ID for.
	 * @param literalText The literal text to modify.
	 * @return A modified literal text.
	 */
	public abstract String adjustId(PsiClass target, String literalText);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		if (getLogIdGenerator(file) == null)
			return false;
		PsiLiteralExpression literalExpression = findSupportedLiteralExpression(editor, file);
		return literalExpression != null && isAvailable(literalExpression);
	}

	/**
	 * {@inheritDoc}
	 */
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		invoke(findSupportedLiteralExpression(editor, file));
	}

	public void invoke(PsiLiteralExpression literalExpression) {
		if (literalExpression != null &&
				getLogIdGenerator(literalExpression.getContainingFile()) != null &&
				isAvailable(literalExpression)) {
			
			String literalText = literalExpression.getText();
			String adjustedLiteralText = adjustId(PsiUtil.getTopLevelClass(literalExpression), literalText);

			PsiElement replacement = getFactory(literalExpression.getContainingFile()).createExpressionFromText(
					adjustedLiteralText, literalExpression.getContext());
			literalExpression.replace(replacement);
		}
	}
}