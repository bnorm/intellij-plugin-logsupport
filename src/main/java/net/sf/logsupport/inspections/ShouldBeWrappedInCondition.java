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
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.L10N;
import net.sf.logsupport.intentions.AddLogIfConditionIntention;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Reports when log methods are not wrapped in an if condition though
 * this is configured for the specified log level.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public class ShouldBeWrappedInCondition extends AbstractInspection {

	private AddLogIfConditionIntention ifConditionIntention = new AddLogIfConditionIntention();

	@Nls
	@NotNull
	@Override
	public String getDisplayName() {
		return L10N.message("Inspections.ShouldBeWrappedInCondition.name");
	}

	@NotNull
	@Override
	public String getShortName() {
		return "ShouldBeWrappedInCondition";
	}

	@Override
	public String checkLogMethodCall(@NotNull PsiMethodCallExpression expression,
									 @NotNull InspectionManager manager, boolean isOnTheFly) {
		return !ifConditionIntention.isAvailable(expression) ? null :
				L10N.message("Inspections.ShouldBeWrappedInCondition.problemMessage");
	}
}
