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
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import net.sf.logsupport.L10N;
import net.sf.logsupport.util.LogPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Is the base class for all log related inspections.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public abstract class AbstractInspection extends BaseJavaLocalInspectionTool {

	/**
	 * {@inheritDoc}
	 */
	@Nls
	@NotNull
	@Override
	public String getGroupDisplayName() {
		String name = GroupNames.LOGGING_GROUP_NAME;
		if (name == null)
			name = L10N.message("name");
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public HighlightDisplayLevel getDefaultLevel() {
		return HighlightDisplayLevel.WARNING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	/**
	 *
	 * Implement this to report problems at log method call level.
	 *
	 * @param expression The log method call to check.
	 * @param manager	InspectionManager to ask for ProblemDescriptor's from.
	 * @param isOnTheFly true if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
	 * @return <code>null</code> if no problems found or the problem message.
	 */
	@Nullable
	public abstract String checkLogMethodCall(
			@NotNull PsiMethodCallExpression expression, @NotNull InspectionManager manager, boolean isOnTheFly);

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitReferenceExpression(PsiReferenceExpression expression) {
			}

			@Override
			public void visitMethodCallExpression(PsiMethodCallExpression expression) {
				if (LogPsiUtil.isSupportedLoggerCall(expression)) {
					String problemMessage = checkLogMethodCall(expression, holder.getManager(), isOnTheFly);
					if (problemMessage != null)
						holder.registerProblem(expression, problemMessage);
				}
			}
		};
	}
}
