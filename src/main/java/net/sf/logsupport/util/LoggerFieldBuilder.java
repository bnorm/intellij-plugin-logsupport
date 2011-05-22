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

package net.sf.logsupport.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.config.LogFramework;

/**
 * Is a helper object used to build logger fields and assign them to classes.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class LoggerFieldBuilder {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.util.LoggerFieldBuilder");

	private PsiClass classForPlace(PsiElement place) {
		return place instanceof PsiClass ? (PsiClass) place : PsiUtil.getTopLevelClass(place);
	}

	/**
	 * Creates the logger field for the context of the specified place,
	 * using the configured default logger implementation.
	 *
	 * @param place The place to use as reference, either the top level class or a child of it.
	 * @return an instance of PsiField that may be used inside the top-level class of the given place.
	 */
	public PsiField createField(PsiElement place) {
		final PsiField field;
		final PsiClass cls = classForPlace(place);
		if (cls != null) {
			LogFramework framework = LogConfiguration.getInstance(place.getContainingFile()).getDefaultLogFramework();
			final LogPsiElementFactory factory = LogPsiUtil.getFactory(place.getContainingFile());

			// Creating a unique field name.
			int fieldNameSequence = 1;
			String fieldName = framework.getDefaultLoggerFieldName();
			while (!PsiUtil.isVariableNameUnique(fieldName, place))
				fieldName = framework.getDefaultLoggerFieldName() + (fieldNameSequence++);

			// Creating the class field
			field = factory.createField(fieldName,
					factory.createTypeFromText(framework.getLoggerClass(), place.getContext()), place.getContext());

			if (field != null) {
				PsiModifierList modifierList = field.getModifierList();
				if (modifierList != null) {
					for (@Modifier String modifier : framework.getDefaultLoggerFieldModifiers())
						modifierList.setModifierProperty(modifier, true);
				}

				field.setInitializer((PsiExpression) factory.createExpressionFromText(
						framework.getLoggerFactoryMethod(cls.getQualifiedName()),
						cls.getContext()));
			}
		} else
			field = null;

		return field;
	}

	/**
	 * Creates a runnable that modifies the top-level class of the given place by adding a logger field.
	 *
	 * @param place The place to use as reference, either the top level class or a child of it.
	 * @return a runnable that modifies the top-level class of the given place by adding a logger field.
	 */
	public Runnable createFieldInserter(final PsiElement place) {
		return createFieldInserter(place, createField(place));
	}

	/**
	 * Creates a runnable that modifies the top-level class of the given place by adding a logger field.
	 *
	 * @param place The place to use as reference, either the top level class or a child of it.
	 * @param field The field to add.
	 * @return a runnable that modifies the top-level class of the given place by adding a logger field.
	 */
	public Runnable createFieldInserter(final PsiElement place, final PsiField field) {
		PsiFile file = place.getContainingFile();
		final Project project = file.getProject();
		final PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
		final Document document = manager.getDocument(file);

		if (document != null) {
			return new Runnable() {
				public void run() {
					manager.commitDocument(document);

					try {
						PsiClass cls = classForPlace(place);
						PsiElement brace = ReflectionUtil.invoke(cls, "getLBrace"); // IF changed in IDEA 9/10

						LogPsiElementFactory factory = LogPsiUtil.getFactory(place.getContainingFile());
						LogFramework framework = LogConfiguration.getInstance(
								place.getContainingFile()).getDefaultLogFramework();

						PsiElement addedField;
						if (framework.isInsertLoggerAtEndOfClass()) {
							addedField = addFieldBeforeAnchor(factory, cls, field, brace);
						} else {
							PsiField[] allFields = cls.getFields();
							if (allFields.length == 0) {
								addedField = cls.addAfter(field, brace);
								cls.addAfter(factory.createWhiteSpaceFromText("\n\n\t"), brace);
							} else
								addedField = addFieldBeforeAnchor(factory, cls, field, allFields[0]);
						}

						shortenFQNames(addedField);
					} finally {
						manager.doPostponedOperationsAndUnblockDocument(document);
					}
				}

				void shortenFQNames(PsiElement elementToFormat) {
					try {
						JavaCodeStyleManager javaStyle = JavaCodeStyleManager.getInstance(project);
						javaStyle.shortenClassReferences(elementToFormat);
					} catch (IncorrectOperationException e) {
						LOG.error(e);
					}
				}
			};
		}

		return null;
	}

	private static PsiElement addFieldBeforeAnchor(LogPsiElementFactory factory,
												   PsiClass cls, PsiField field, PsiElement anchor) {
		PsiElement addedField;
		cls.addBefore(factory.createWhiteSpaceFromText("\n\t"), anchor);
		addedField = cls.addBefore(field, anchor);
		cls.addBefore(factory.createWhiteSpaceFromText("\n"), anchor);

		return addedField;
	}
}
