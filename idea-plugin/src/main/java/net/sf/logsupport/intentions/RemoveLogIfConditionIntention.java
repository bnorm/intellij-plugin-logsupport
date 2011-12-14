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

import com.intellij.psi.*;
import net.sf.logsupport.L10N;
import org.jetbrains.annotations.NotNull;

/**
 * Removes an if condition.
 *
 * @author Juergen_Kellerer, 2010-04-27
 * @version 1.0
 */
public class RemoveLogIfConditionIntention extends AbstractLogConditionIntention {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(PsiMethodCallExpression expression) {
		return findSurroundingCondition(expression) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doInvoke(PsiMethodCallExpression expression) {
		PsiIfStatement statement = findSurroundingCondition(expression);
		PsiStatement branch = statement == null ? null : statement.getThenBranch();
		if (branch == null)
			return;

		if (branch instanceof PsiBlockStatement) {
			PsiCodeBlock block = ((PsiBlockStatement) branch).getCodeBlock();
			for (PsiElement c = block.getFirstBodyElement(),
					last = block.getLastBodyElement(); c != null ; c = c.getNextSibling()) {
				if (c == last)
					break;
				statement.getParent().addAfter(c.copy(), statement);
			}
		} else
			statement.getParent().addAfter(branch.copy(), statement);

		statement.delete();
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public String getText() {
		return L10N.message("Intentions.RemoveLogIfConditionIntention.name");
	}
}