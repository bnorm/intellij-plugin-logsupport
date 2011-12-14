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

import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import net.sf.logsupport.config.ProjectConfiguration;
import net.sf.logsupport.ui.config.AbstractDelegatingConfigurable;
import net.sf.logsupport.ui.config.LogIdsPanel;
import net.sf.logsupport.ui.config.ProjectLogConfigurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Implements the project bound component to support logging.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
@State(
		name = "LogSupport",
		storages = {
				@Storage(id = "LogSupport.Project", file = "$PROJECT_FILE$"),
				@Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/logsupport/settings.xml",
						scheme = StorageScheme.DIRECTORY_BASED)
		}
)
public class LogSupportProjectComponent extends AbstractDelegatingConfigurable
		implements ProjectComponent, Configurable.Composite, PersistentStateComponent<ProjectConfiguration> {

	private Project project;
	private LogSupportComponent logSupport;

	private boolean configurationInitialized;
	private ProjectConfiguration configuration;

	private Configurable configurableDelegate;
	private Configurable[] configurables;

	public LogSupportProjectComponent(Project project, LogSupportComponent logSupport) {
		this.project = project;
		this.logSupport = logSupport;
	}

	public void initComponent() {
	}

	public void disposeComponent() {
		disposeAllConfigurables();
	}

	public Project getProject() {
		return project;
	}

	public ProjectConfiguration getState() {
		if (configuration == null)
			loadState(new ProjectConfiguration());

		if (!configurationInitialized) {
			disposeAllConfigurables();

			// Init calls getState, setting initialized first to avoid recursion.
			configurationInitialized = true;
			try {
				configuration.init(project);
			} catch (RuntimeException t) {
				configurationInitialized = false;
			}
		}

		return configuration;
	}

	public void loadState(ProjectConfiguration projectConfiguration) {
		configuration = projectConfiguration;
		configurationInitialized = false;
	}

	@NotNull
	public String getComponentName() {
		return "LogSupport.LogSupportProjectComponent";
	}

	public void disposeAllConfigurables() {
		configurableDelegate = null;
		configurables = null;
	}

	public void disposeUIResources() {
		super.disposeUIResources();
		disposeAllConfigurables();
	}

	@Override
	public Icon getIcon() {
		return LogSupportComponent.ID_ICON;
	}

	@Override
	protected Configurable getConfigurableDelegate() {
		if (configurableDelegate == null)
			configurableDelegate = new ProjectLogConfigurable(project, getState().getLogConfigurations());
		return configurableDelegate;
	}

	public Configurable[] getConfigurables() {
		if (configurables == null) {
			configurables = new Configurable[]{new LogIdsPanel(logSupport, this)};
		}
		return configurables;
	}

	public void projectOpened() {
		// Calling get state to ensure everything is initialized.
		getState();
	}

	public void projectClosed() {
	}
}
