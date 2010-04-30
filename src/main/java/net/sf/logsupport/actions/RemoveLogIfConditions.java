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

package net.sf.logsupport.actions;

import com.intellij.openapi.project.Project;
import net.sf.logsupport.ui.AbstractProcessingDialog;
import net.sf.logsupport.ui.AddLogIfConditionsDialog;
import net.sf.logsupport.ui.RemoveLogIfConditionsDialog;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-28
 * @version 1.0
 */
public class RemoveLogIfConditions extends AbstractProcessingAction {
	@Override
	protected AbstractProcessingDialog createDialog(Project project) {
		return new RemoveLogIfConditionsDialog(project, getSelection(project));
	}
}