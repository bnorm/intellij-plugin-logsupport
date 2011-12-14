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

package net.sf.logsupport;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import net.sf.logsupport.config.ApplicationConfiguration;
import net.sf.logsupport.ui.config.AbstractDelegatingConfigurable;
import net.sf.logsupport.ui.config.LogFrameworksPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Implements the main application bound component.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
@State(
		name = "LogSupport",
		storages = {@Storage(id = "LogSupport.Globals", file = "$APP_CONFIG$/logsupport/settings.xml")}
)
public class LogSupportComponent extends AbstractDelegatingConfigurable
		implements ApplicationComponent, Configurable, PersistentStateComponent<ApplicationConfiguration> {

	final static Icon ID_ICON = new ImageIcon(LogSupportComponent.class.getResource("/icons/logsupport-16.png"));

	private Configurable configurable;
	private ApplicationConfiguration configuration;

	public void initComponent() {
	}

	public void disposeComponent() {
		configurable = null;
	}

	@NotNull
	public String getComponentName() {
		return "LogSupport.LogSupportComponent";
	}

	public ApplicationConfiguration getState() {
		if (configuration == null)
			configuration = new ApplicationConfiguration();
		return configuration;
	}

	public void loadState(ApplicationConfiguration applicationConfiguration) {
		configuration = applicationConfiguration;
	}

	@Override
	protected Configurable getConfigurableDelegate() {
		if (configurable == null)
			configurable = new LogFrameworksPanel(this);
		return configurable;
	}

	public Icon getIcon() {
		return ID_ICON;
	}
}
