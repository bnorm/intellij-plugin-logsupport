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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.LoggerFieldBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

/**
 * Macro that resolves the instance of the logger to be used with the log call.
 *
 * @author Juergen_Kellerer, 2010-04-03
 * @version 1.0
 */
public class ResolveLoggerInstance extends AbstractResolveMacro {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.livetemplates.ResolveLoggerInstance");
	private static volatile String lastCreatedLoggerInstance;

	public static String getLastCreatedLoggerInstance() {
		return lastCreatedLoggerInstance;
	}

	public static void setLastCreatedLoggerInstance(String lastCreatedLoggerInstance) {
		ResolveLoggerInstance.lastCreatedLoggerInstance = lastCreatedLoggerInstance;
	}

	public ResolveLoggerInstance() {
		super("resolveLoggerInstance");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultValue() {
		return "log";
	}

	/**
	 * {@inheritDoc}
	 */
	public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {
		PsiFile file = getPsiFile(context);
		LogConfiguration config = LogConfiguration.getInstance(file);

		PsiElement[] variables = resolveVariables(config.getSupportedLoggerClasses(), file, context);
		if (variables != null && variables.length > 0)
			return new JavaPsiElementResult(variables[0]);

		return new TextResult(getDefaultValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public LookupElement[] calculateLookupItems(@NotNull Expression[] expressions, ExpressionContext context) {
		PsiFile file = getPsiFile(context);
		LogConfiguration config = LogConfiguration.getInstance(file);

		PsiElement[] variables = resolveVariables(config.getSupportedLoggerClasses(), file, context);
		if (variables != null && variables.length >= 2)
			return convertToLookupItems(variables);

		return null;
	}

	@Override
	protected PsiElement[] resolveVariables(Set<String> stringTypes, PsiFile file, ExpressionContext context) {
		final PsiElement place = getPlace(file, context);
		final LogFramework framework = LogConfiguration.getInstance(place.getContainingFile()).getDefaultLogFramework();

		PsiElement[] variables = super.resolveVariables(stringTypes, file, context);

		if (framework != null && framework.isLogMethodsAreStatic()) {
			if (LOG.isDebugEnabled()) LOG.debug("Default logger uses static method, adding the logger class to the list of resolved variables.");
			variables = variables == null ? new PsiElement[1] : Arrays.copyOf(variables, variables.length + 1);
			variables[variables.length - 1] = LogPsiUtil.getFactory(file).createExpressionFromText(framework.getLoggerClass(), place);
		}

		if (variables != null && variables.length == 0) {
			PsiField field = createField(file, place);
			if (field != null) {
				setLastCreatedLoggerInstance(field.getType().getCanonicalText());
				variables = new PsiElement[]{field};
			}
		}

		return variables;
	}

	PsiField createField(PsiFile file, PsiElement place) {
		if (!file.isWritable())
			return null;

		LoggerFieldBuilder fieldBuilder = new LoggerFieldBuilder();
		PsiField field = fieldBuilder.createField(place);
		if (field != null)
			TemplatePostProcessor.schedule(file, fieldBuilder.createFieldInserter(place, field), true);

		return field;
	}
}
