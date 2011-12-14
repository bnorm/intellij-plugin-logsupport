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

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.L10N;
import net.sf.logsupport.config.LogFramework;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public class ThrowableInFormattedMessage extends AbstractFormattedMessageInspection {
	/**
	 * {@inheritDoc}
	 */
	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return L10N.message("Inspections.ThrowableInFormattedMessage.name");
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public String getShortName() {
		return "ThrowableInFormattedMessage";
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public HighlightDisplayLevel getDefaultLevel() {
		return HighlightDisplayLevel.INFO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String checkLogMethodCall(@NotNull LogFramework framework,
									 @NotNull PsiMethodCallExpression expression,
									 @NotNull InspectionManager manager, boolean isOnTheFly) {
		if (!framework.isPlaceholdersCanBeUsedWithThrowables()) {
			List<Object> callArgumentDefaults = getLogCallArgumentDefaults(expression, true);
			if (callArgumentDefaults.size() > 1) {
				for (Object argumentDefault : callArgumentDefaults) {
					if (argumentDefault instanceof Throwable)
						return L10N.message("Inspections.ThrowableInFormattedMessage.problemMessage");
				}
			}
		}

		return null;
	}
}
