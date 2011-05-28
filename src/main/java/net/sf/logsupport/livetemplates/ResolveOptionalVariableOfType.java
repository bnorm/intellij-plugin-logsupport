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

package net.sf.logsupport.livetemplates;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * This macro is similar to VariablesOfTypeMacro with the difference that
 * it does not force a stop if no instance of found.
 *
 * @author Juergen_Kellerer, 2010-04-03
 * @version 1.0
 */
public class ResolveOptionalVariableOfType extends AbstractResolveMacro {

	public ResolveOptionalVariableOfType() {
		super("resolveOptionalVariableOfType");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultValue() {
		return "a";
	}

	/**
	 * {@inheritDoc}
	 */
	public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {
		Set<String> types = extractExpectedTypes(expressions, context);
		if (!types.isEmpty()) {
			PsiFile file = getPsiFile(context);
			PsiElement[] variables = resolveVariables(types, file, context);
			if (variables != null && variables.length > 0)
				return new JavaPsiElementResult(variables[0]);
		}

		return new TextResult("");
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public LookupElement[] calculateLookupItems(@NotNull Expression[] expressions, ExpressionContext context) {
		Set<String> types = extractExpectedTypes(expressions, context);
		if (!types.isEmpty()) {
			PsiFile file = getPsiFile(context);
			PsiElement[] variables = resolveVariables(types, file, context);
			if (variables != null && variables.length >= 2)
				return convertToLookupItems(variables);
		}

		return null;
	}

	private Set<String> extractExpectedTypes(Expression[] expressions, ExpressionContext context) {
		Set<String> types = new HashSet<String>(expressions.length);
		for (Expression expression : expressions) {
			Result result = expression.calculateResult(context);
			if (result != null)
				types.add(result.toString());
		}
		return types;
	}
}
