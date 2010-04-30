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

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the next log ID and returns it if logIds are enabled.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class ResolveLogMethod extends AbstractMacro {

	public ResolveLogMethod() {
		super("resolveLogMethod");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return getName() + "(LOGGER, LogLevel)";
	}

	/**
	 * {@inheritDoc}
	 */
	public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {

		Result method = null;

		if (expressions.length == 2) {
			String loggerClass = resolveVariableType(expressions[0], context);
			if (loggerClass == null)
				loggerClass = ResolveLoggerInstance.LAST_CREATED_LOGGER_CLASS;

			String methodExpression = resolveLogMethodExpression(
					loggerClass, parseLogLevel(expressions[1], context));

			if (!methodExpression.contains("("))
				methodExpression += "(";

			method = new TextResult(methodExpression);
		}

		return method;
	}
}