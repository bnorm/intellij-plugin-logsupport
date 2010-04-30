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
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.JavaPsiElementResult;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.impl.JavaTemplateUtil;
import com.intellij.codeInsight.template.macro.MacroUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.util.LogPsiElementFactory;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.LoggerFieldBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves
 *
 * @author Juergen_Kellerer, 2010-04-03
 * @version 1.0
 */
public class ResolveLoggerInstance extends AbstractMacro {

	static String LAST_CREATED_LOGGER_CLASS;

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
		LogConfiguration configuration = LogConfiguration.getInstance(file);

		PsiElement[] variables = getVariables(configuration.getSupportedLoggerClasses(), file, context);
		if (variables != null && variables.length > 0)
			return new JavaPsiElementResult(variables[0]);

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public LookupElement[] calculateLookupItems(@NotNull Expression[] expressions, ExpressionContext context) {
		PsiFile file = getPsiFile(context);
		LogConfiguration configuration = LogConfiguration.getInstance(file);

		PsiElement[] variables = getVariables(configuration.getSupportedLoggerClasses(), file, context);
		if (variables == null || variables.length < 2)
			return null;

		// No generics and manual array creation to support builds in IDEA 8 and 9
		Set set = new LinkedHashSet();
		for (PsiElement variable : variables)
			JavaTemplateUtil.addElementLookupItem(set, variable);

		int i = 0;
		LookupElement[] elements = new LookupElement[set.size()];
		for (Object o : set) elements[i++] = (LookupElement) o;
		return elements;
	}

	@Nullable
	PsiElement[] getVariables(Set<String> stringTypes, PsiFile file, ExpressionContext context) {
		try {
			PsiElement place = getPlace(file, context);

			// Resolving the types
			Set<PsiType> types = new LinkedHashSet<PsiType>(stringTypes.size());
			LogPsiElementFactory factory = LogPsiUtil.getFactory(file);
			for (String type : stringTypes)
				types.add(factory.createTypeFromText(type, place.getContext()));


			ArrayList<PsiElement> elementsInScope = new ArrayList<PsiElement>();
			PsiVariable[] variables = MacroUtil.getVariablesVisibleAt(place, "");

			for (PsiVariable var : variables) {
				if (var instanceof PsiLocalVariable) {
					TextRange range = var.getNameIdentifier().getTextRange();
					if (range != null && range.contains(context.getStartOffset())) {
						continue;
					}
				}

				for (PsiType type : types)
					if (type == null || type.isAssignableFrom(var.getType())) {
						elementsInScope.add(var);
						break;
					}
			}

			if (elementsInScope.isEmpty()) {
				PsiField field = createField(file, place);
				if (field != null) {
					LAST_CREATED_LOGGER_CLASS = field.getType().getCanonicalText();
					elementsInScope.add(field);
				}
			}

			return elementsInScope.toArray(new PsiElement[elementsInScope.size()]);
		} catch (NullPointerException e) {
			return null;
		}
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
