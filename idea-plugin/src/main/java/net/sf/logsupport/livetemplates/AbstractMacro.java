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
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.macro.MacroUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiVariable;
import net.sf.logsupport.LogSupportComponent;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.config.LogLevel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for log related macros.
 *
 * @author Juergen_Kellerer, 2010-04-05
 * @version 1.0
 */
public abstract class AbstractMacro extends AbstractMacroBase {

	private static final Logger log = Logger.getInstance("#net.sf.logsupport.livetemplates.AbstractMacro");

	protected String name;

	protected AbstractMacro(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return getName() + "()";
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNls
	@NotNull
	public String getDefaultValue() {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public Result calculateQuickResult(@NotNull Expression[] expressions, ExpressionContext context) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public LookupElement[] calculateLookupItems(@NotNull Expression[] expressions, ExpressionContext context) {
		return null;
	}

	/**
	 * Returns the psi file that the context refers to.
	 *
	 * @param context The context to evaluate.
	 * @return the psi file that the context refers to.
	 */
	@Nullable
	protected PsiFile getPsiFile(@NotNull ExpressionContext context) {
		Project project = context.getProject();
		return PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
	}

	/**
	 * Returns the element under the caret of the expression context.
	 *
	 * @param file	the psi file that the context refers to.
	 * @param context the context to evaluate.
	 * @return the element under the caret of the expression context.
	 */
	@Nullable
	protected PsiElement getPlace(PsiFile file, @NotNull ExpressionContext context) {
		int offset = context.getStartOffset();
		PsiDocumentManager.getInstance(file.getProject()).commitAllDocuments();
		return file.findElementAt(offset);
	}

	/**
	 * Resolves the type of the variable, referenced by the given expression.
	 *
	 * @param expression the expression referencing a variable.
	 * @param context	The context to evaluate against.
	 * @return the type of the variable.
	 */
	@Nullable
	protected String resolveVariableType(Expression expression, @NotNull ExpressionContext context) {
		Result result = expression.calculateResult(context);
		String variableName = String.valueOf(result);
		return resolveVariableType(variableName, context);
	}

	/**
	 * Resolves the type of the variable, referenced by the given name.
	 *
	 * @param variableName the name of the variable under the context's scope.
	 * @param context	  The context to evaluate against.
	 * @return the type of the variable.
	 */
	@Nullable
	protected String resolveVariableType(String variableName, @NotNull ExpressionContext context) {
		int offset = context.getStartOffset();
		PsiFile file = getPsiFile(context);
		PsiElement place = file == null ? null : file.findElementAt(offset);

		if (file != null && place != null) {
			// Try to resolve the real variable type.
			for (PsiVariable variable : MacroUtil.getVariablesVisibleAt(place, variableName)) {
				String name = variable.getName();
				if (name != null && name.equals(variableName))
					return variable.getType().getCanonicalText();
			}
		}

		// Look after static logger methods if variableName points to a class instead of a local variable.
		Application application = ApplicationManager.getApplication();
		for (LogFramework framework : application.getComponent(LogSupportComponent.class).getState().getFrameworks()) {
			if (framework.isLogMethodsAreStatic() && framework.getLoggerClass().equals(variableName))
				return variableName;
		}

		return null;
	}

	/**
	 * Resolves the callable expression to log under the given level.
	 *
	 * @param loggerClass the class containing the log method.
	 * @param level	   The level used to log.
	 * @return An expression that can be used inside the log template, describing the method to call.
	 */
	protected String resolveLogMethodExpression(String loggerClass, LogLevel level) {
		if (loggerClass != null) {
			Application application = ApplicationManager.getApplication();
			for (LogFramework framework : application.getComponent(LogSupportComponent.class).getState().getFrameworks()) {
				if (framework.getLoggerClass().equals(loggerClass))
					return framework.getLogMethod().get(level);
			}

			log.warn("Failed resolving a log method using logger class '" + loggerClass + "', using default method name instead.");
		}
		return level.name();
	}

	/**
	 * Parses the log level contained in the given expression.
	 *
	 * @param expression The expression to parse the log level from.
	 * @param context	The context to evaluate against.
	 * @return The log level contained in the expression. Defaults to "info".
	 */
	@NotNull
	protected LogLevel parseLogLevel(@NotNull Expression expression, ExpressionContext context) {
		LogLevel level = LogLevel.info;
		try {
			level = LogLevel.valueOf(String.valueOf(expression.calculateResult(context)));
		} catch (IllegalArgumentException e) {
			log.warn("Failed resolving log level from expression, using the default of '" + level + "'");
		}
		return level;
	}
}
