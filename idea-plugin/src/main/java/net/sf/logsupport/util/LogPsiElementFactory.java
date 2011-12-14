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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;

/**
 * Is an abstraction API to support multiple languages.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public interface LogPsiElementFactory {

	PsiField createField(String text, PsiType type, PsiElement context);

	PsiType createTypeFromText(String text, PsiElement context);

	PsiElement createExpressionFromText(String text, PsiElement context);

	PsiElement createStatementFromText(String text, PsiElement context);

	PsiElement createWhiteSpaceFromText(String text);
}
