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

package net.sf.logsupport.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.*;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sf.logsupport.util.LogPsiUtil.resolveVariableInitializer;

/**
 * Is the common base class for formatted message related inspections.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public abstract class AbstractFormattedMessageInspection extends AbstractInspection {

	private static final Logger LOG = Logger.getInstance(
			"#net.sf.logsupport.inspections.AbstractFormattedMessageInspection");

	private static final PsiType DOUBLE = ReflectionUtil.getField(PsiType.class, "DOUBLE");
	private static final PsiType FLOAT = ReflectionUtil.getField(PsiType.class, "FLOAT");
	private static final PsiType LONG = ReflectionUtil.getField(PsiType.class, "LONG");
	private static final PsiType INT = ReflectionUtil.getField(PsiType.class, "INT");
	private static final PsiType SHORT = ReflectionUtil.getField(PsiType.class, "SHORT");
	private static final PsiType CHAR = ReflectionUtil.getField(PsiType.class, "CHAR");
	private static final PsiType BYTE = ReflectionUtil.getField(PsiType.class, "BYTE");
	private static final PsiType BOOLEAN = ReflectionUtil.getField(PsiType.class, "BOOLEAN");

	@NotNull
	List<Object> getLogCallArgumentDefaults(PsiMethodCallExpression expression, boolean includeThrowables) {
		List<Object> results = new ArrayList<Object>();
		PsiExpressionList list = expression.getArgumentList();

		PsiLiteralExpression literalExpression = LogPsiUtil.findSupportedLiteralExpression(list);
		if (literalExpression != null) {
			// find index in list
			PsiElement hookExpression = literalExpression;
			while (hookExpression.getParent() instanceof PsiBinaryExpression)
				hookExpression = hookExpression.getParent();

			boolean collect = false;

			PsiExpression[] arguments = list.getExpressions();
			for (PsiExpression argument : arguments) {
				if (!collect) {
					collect = argument == hookExpression;
				} else {
					if (argument.getType() instanceof PsiArrayType) {
						PsiArrayInitializerExpression initializer = null;
						if (argument instanceof PsiNewExpression) {
							PsiNewExpression ne = (PsiNewExpression) argument;
							initializer = ne.getArrayInitializer();
						} else if (argument instanceof PsiReferenceExpression) {
							PsiExpression pe = resolveVariableInitializer((PsiReferenceExpression) argument);
							if (pe instanceof PsiArrayInitializerExpression)
								initializer = (PsiArrayInitializerExpression) pe;
						}

						if (initializer != null) {
							for (PsiExpression e : initializer.getInitializers())
								results.add(createDefaultValueFor(e));
						}
					} else {
						results.add(createDefaultValueFor(argument));
					}
				}
			}
		}

		if (!includeThrowables) {
			for (int i = results.size() - 1; i >= 0; i--) {
				if (results.get(i) instanceof Throwable)
					results.remove(i);
				else
					break;
			}
		}

		return results;
	}

	Object createDefaultValueFor(PsiExpression expression) {
		PsiType type = expression.getType();
		if (type != null) {
			try {
				if (isTypeAssignableTo(type, "java.util.Date"))
					return new Date();
				if (isTypeAssignableTo(type, "java.lang.Throwable"))
					return new RuntimeException();
				if (type.isAssignableFrom(DOUBLE))
					return 2.2D;
				if (type.isAssignableFrom(FLOAT))
					return 2.2f;
				if (type.isAssignableFrom(LONG))
					return 2L;
				if (type.isAssignableFrom(INT))
					return 2;
				if (type.isAssignableFrom(SHORT))
					return (short) 2;
				if (type.isAssignableFrom(CHAR))
					return 'c';
				if (type.isAssignableFrom(BYTE))
					return (byte) 2;
				if (type.isAssignableFrom(BOOLEAN))
					return true;
			} catch (ProcessCanceledException e) {
				throw e;
			} catch (Throwable e) {
				LOG.error("Failed to determine a value type for " + expression +
						", will return a string as default value.", e);
			}
		}

		return "s";
	}

	static boolean isTypeAssignableTo(PsiType type, String className) {
		String name = type.getCanonicalText();
		if (name != null && name.equals(className))
			return true;

		for (PsiType psiType : type.getSuperTypes()) {
			if (isTypeAssignableTo(psiType, className))
				return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String checkLogMethodCall(@NotNull PsiMethodCallExpression expression,
										   @NotNull InspectionManager manager, boolean isOnTheFly) {
		LogFramework framework = LogPsiUtil.getLogFramework(expression);
		if (framework != null && framework.isLogMessagesCanUsePlaceholders())
			return checkLogMethodCall(framework, expression, manager, isOnTheFly);

		return null;
	}

	public abstract String checkLogMethodCall(@NotNull LogFramework framework,
											  @NotNull PsiMethodCallExpression expression,
											  @NotNull InspectionManager manager, boolean isOnTheFly);
}
