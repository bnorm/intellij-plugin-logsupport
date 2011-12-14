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
import net.sf.logsupport.config.LogFramework;
import org.jetbrains.annotations.Nls;

import java.util.List;

/**
 * Creates the graphical editor panel to setup log frameworks.
 *
 * @author Juergen_Kellerer, 2010-04-05
 * @version 1.0
 */
public class LogFrameworksPanel extends AbstractMasterDetailsPanel<LogFramework> {

	public LogFrameworksPanel(LogSupportComponent logSupport) {
		super(logSupport, null);
	}

	@Override
	protected String getId() {
		return "LogFrameworkSupport.UI";
	}

	@Nls
	public String getDisplayName() {
		return "Log Framework Support";
	}

	@Override
	protected String getElementDescriptiveName() {
		return "log framework";
	}

	@Override
	protected NamedConfigurable<LogFramework> createConfigurable(LogFramework source, boolean isNewNode) {
		return new LogFrameworkConfigurable(this, source, isNewNode);
	}

	@Override
	protected LogFramework createNewElement(String name) {
		return new LogFramework(name);
	}

	@Override
	protected LogFramework cloneElement(LogFramework source) {
		return source.copy();
	}

	@Override
	protected String getElementName(LogFramework element) {
		return element.getName();
	}

	@Override
	protected void setElementName(LogFramework element, String name) {
		element.setName(name);
	}

	@Override
	protected List<LogFramework> getElementsFromBackingStore() {
		return logSupport.getState().getFrameworks();
	}
}
