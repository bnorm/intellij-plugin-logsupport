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
import com.intellij.psi.impl.source.resolve.ResolveVariableUtil;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import net.sf.logsupport.config.ConditionFormat;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.util.LogPsiElementFactory;
import net.sf.logsupport.util.LogPsiUtil;

import javax.resource.spi.EISSystemException;
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

	public static PsiIfStatement findSurroundingCondition(PsiMethodCallExpression expression) {
		LogFramework framework = LogPsiUtil.getLogFramework(expression);
		LogLevel level = framework == null ? null : LogPsiUtil.findLogLevel(expression);

		if (framework != null && level != null) {
			PsiIfStatement expectedStatement = createPlainIfCondition(expression, true);
			PsiExpression expectedCondition = expectedStatement == null ? null : expectedStatement.getCondition();
			if (expectedStatement == null || !(expectedCondition instanceof PsiMethodCallExpression))
				return null;

			final PsiMethodCallExpression expectedExpression = (PsiMethodCallExpression) expectedCondition;

			int searchLevels = 65;
			PsiElement element = expression;
			while (searchLevels-- > 0 && element != null && !(element instanceof PsiMethod)) {
				element = element.getParent();
				if (element instanceof PsiIfStatement) {
					PsiExpression e1 = ((PsiIfStatement) element).getCondition();
					if (e1 instanceof PsiReferenceExpression)
						e1 = LogPsiUtil.resolveVariableInitializer((PsiReferenceExpression) e1);

					if (e1 instanceof PsiMethodCallExpression &&
							LogPsiUtil.isEquivalentTo((PsiMethodCallExpression) e1, expectedExpression)) {
						return (PsiIfStatement) element;
					}
				}
			}
		}

		return null;
	}

	public static PsiIfStatement createPlainIfCondition(PsiMethodCallExpression expression,
														boolean ignoreDisabledLevels) {
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

				if (!conditionalMethod.contains("("))
					conditionalMethod += "()";

				if (!qualifier.getText().isEmpty())
					conditionalMethod = qualifier.getText() + '.' + conditionalMethod;

				String template = conditionTemplates.get(config.getConditionFormat());
				return (PsiIfStatement) factory.createStatementFromText(
						String.format(template, conditionalMethod), expression.getContext());
			}
		}

		return null;
	}

	public static PsiIfStatement createIfCondition(PsiMethodCallExpression expression) {
		PsiIfStatement statement = createPlainIfCondition(expression, false);

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
