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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLiteralExpression;
import net.sf.logsupport.L10N;
import net.sf.logsupport.util.NumericLogIdGenerator;
import org.jetbrains.annotations.NotNull;

import static net.sf.logsupport.util.LogPsiUtil.getLogIdGenerator;
import static net.sf.logsupport.util.LogPsiUtil.isLogIdPresent;

/**
 * Implements an intention that is used to add new log IDs to log calls that have no ID yet.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public class AddLogIdIntention extends AbstractLogIdIntention {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(PsiLiteralExpression literalExpression) {
		return !isLogIdPresent(literalExpression);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String adjustId(PsiClass target, String literalText) {
		NumericLogIdGenerator generator = getLogIdGenerator(target.getContainingFile());
		if (generator == null)
			return literalText;
		return '"' + generator.nextId() + literalText.substring(1);
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public String getText() {
		return L10N.message("Intentions.AddLogIdIntention.name");
	}
}
