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

import com.intellij.codeInsight.template.macro.MacroUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import net.sf.logsupport.config.ConditionFormat;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.util.LogPsiElementFactory;
import net.sf.logsupport.util.LogPsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a base class to support condition handling.
 *
 * @author Juergen_Kellerer, 2010-04-27
 * @version 1.0
 */
public abstract class AbstractLogConditionIntention extends AbstractLogIntention {

	private static final Map<ConditionFormat, String> conditionTemplates = new HashMap<ConditionFormat, String>();

	static {
		conditionTemplates.put(ConditionFormat.lineBreak, "if (%s) \n\tlog()");
		conditionTemplates.put(ConditionFormat.lineBreakAndBraces, "if (%s) { log(); }");
		conditionTemplates.put(ConditionFormat.noLineBreak, "if (%s) log()");
	}

	static PsiElementFilter dummyLogMethodCallFilter = new PsiElementFilter() {
		public boolean isAccepted(PsiElement element) {
			return element instanceof PsiMethodCallExpression && "log()".equals(element.getText());
		}
	};

	/**
	 * Finds the surrounding if condition that covers the conditional logger call.
	 *
	 * @param expression the log method call expression.
	 * @return the surrounding if condition that covers the conditional logger call, or 'null' if not found.
	 */
	@Nullable
	public static PsiIfStatement findSurroundingCondition(PsiMethodCallExpression expression) {
		LogFramework framework = LogPsiUtil.getLogFramework(expression);
		LogLevel level = framework == null ? null : LogPsiUtil.findLogLevel(expression);

		if (framework != null && level != null) {
			final PsiMethodCallExpression expectedCondition = createExpectedConditionFor(expression);
			if (expectedCondition == null)
				return null;

			int searchLevels = 65;
			PsiElement element = expression;
			while (searchLevels-- > 0 && element != null && !(element instanceof PsiMethod)) {
				element = element.getParent();
				if (element instanceof PsiIfStatement) {
					PsiExpression condition = ((PsiIfStatement) element).getCondition();
					if (condition instanceof PsiReferenceExpression)
						condition = LogPsiUtil.resolveVariableInitializer((PsiReferenceExpression) condition);

					if (isValidCondition(expectedCondition, condition)) {
						return (PsiIfStatement) element;
					}
				}
			}
		}

		return null;
	}

	@Nullable
	private static PsiMethodCallExpression createExpectedConditionFor(PsiMethodCallExpression expression) {
		PsiIfStatement expectedStatement = createPlainIfCondition(expression, true, false);
		PsiExpression expectedCondition = expectedStatement == null ? null : expectedStatement.getCondition();
		if (!(expectedCondition instanceof PsiMethodCallExpression))
			return null;
		return (PsiMethodCallExpression) expectedCondition;
	}

	private static boolean isValidCondition(PsiMethodCallExpression expectedCondition, PsiExpression condition) {
		return condition instanceof PsiMethodCallExpression &&
				LogPsiUtil.isEquivalentTo((PsiMethodCallExpression) condition, expectedCondition);
	}

	/**
	 * Creates a plain if statement that may be used to surround a logger call.
	 *
	 * @param expression			 the logger call expression to wrap.
	 * @param ignoreDisabledLevels   whether non-conditional levels are not considered.
	 * @param preferCustomConditions whether custom conditions (e.g. constants) are preferred over
	 *                               direct calls to the log framework.
	 * @return a plain if statement or 'null' if no framework is configured or the level could not be extracted.
	 */
	@Nullable
	public static PsiIfStatement createPlainIfCondition(PsiMethodCallExpression expression,
														boolean ignoreDisabledLevels,
														boolean preferCustomConditions) {
		LogFramework framework = LogPsiUtil.getLogFramework(expression);
		LogLevel level = framework == null ? null : LogPsiUtil.findLogLevel(expression);

		if (framework != null && level != null) {
			LogPsiElementFactory factory = LogPsiUtil.getFactory(expression.getContainingFile());
			PsiExpression qualifier = expression.getMethodExpression().getQualifierExpression();
			LogConfiguration config = LogConfiguration.getInstance(expression.getContainingFile());
			String conditionalMethod = framework.getEnabledGetterMethod().get(level);

			if (config != null &&
					(ignoreDisabledLevels || config.getConditionalLogLevels().contains(level)) &&
					qualifier != null && conditionalMethod != null && !conditionalMethod.isEmpty()) {

				final PsiElement context = expression.getContext();

				boolean useCustomCondition = false;
				if (preferCustomConditions) {
					PsiVariable[] variables = MacroUtil.getVariablesVisibleAt(context, "");
					if (variables.length > 0) {
						final PsiMethodCallExpression expectedCondition = createExpectedConditionFor(expression);
						if (expectedCondition != null) {
							for (PsiVariable variable : variables) {
								PsiExpression condition = LogPsiUtil.resolveVariableInitializer(variable);
								if (isValidCondition(expectedCondition, condition)) {
									PsiIdentifier nameIdentifier = variable.getNameIdentifier();
									if (nameIdentifier != null) {
										useCustomCondition = true;
										conditionalMethod = nameIdentifier.getText();
										break;
									}
								}
							}
						}
					}
				}

				if (!useCustomCondition) {
					if (!conditionalMethod.contains("("))
						conditionalMethod += "()";

					if (!qualifier.getText().isEmpty())
						conditionalMethod = qualifier.getText() + '.' + conditionalMethod;
				}

				String template = conditionTemplates.get(config.getConditionFormat());
				return (PsiIfStatement) factory.createStatementFromText(
						String.format(template, conditionalMethod), context);
			}
		}

		return null;
	}

	public static PsiIfStatement createIfCondition(PsiMethodCallExpression expression) {
		PsiIfStatement statement = createPlainIfCondition(expression, false, true);

		if (statement != null) {
			PsiStatement then = statement.getThenBranch();
			PsiElement[] methods = PsiTreeUtil.collectElements(then, dummyLogMethodCallFilter);

			if (methods.length == 1) {
				methods[0].replace(expression.copy());
				return statement;
			}
		}

		return null;
	}
}
