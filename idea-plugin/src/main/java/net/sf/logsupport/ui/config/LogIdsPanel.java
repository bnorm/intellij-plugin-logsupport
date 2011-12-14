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

package net.sf.logsupport.ui.config;

import com.intellij.openapi.ui.NamedConfigurable;
import net.sf.logsupport.LogSupportComponent;
import net.sf.logsupport.LogSupportProjectComponent;
import net.sf.logsupport.config.LogId;
import org.jetbrains.annotations.Nls;

import java.util.List;

/**
 * Implements the editor panel for log ids.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public class LogIdsPanel extends AbstractMasterDetailsPanel<LogId> {

	public LogIdsPanel(LogSupportComponent logSupport, LogSupportProjectComponent projectComponent) {
		super(logSupport, projectComponent);
	}

	@Override
	protected String getId() {
		return "LogIds.UI";
	}

	@Nls
	public String getDisplayName() {
		return "Log IDs";
	}

	@Override
	protected String getElementDescriptiveName() {
		return "log id";
	}

	@Override
	protected NamedConfigurable<LogId> createConfigurable(LogId source, boolean isNewNode) {
		return new LogIdConfigurable(this, source, isNewNode);
	}

	@Override
	protected LogId createNewElement(String name) {
		return new LogId(name);
	}

	@Override
	protected LogId cloneElement(LogId source) {
		return source.clone();
	}

	@Override
	protected String getElementName(LogId element) {
		return element.getName();
	}

	@Override
	protected void setElementName(LogId element, String name) {
		element.setName(name);
	}

	@Override
	protected List<LogId> getElementsFromBackingStore() {
		return projectComponent.getState().getLogIds();
	}
}
