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

package net.sf.logsupport.config;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import net.sf.logsupport.LogSupportProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the main project configuration data.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public class ProjectConfiguration {

	public static ProjectConfiguration getInstance(Project project) {
		return project.getComponent(LogSupportProjectComponent.class).getState();
	}

	List<LogId> logIds;
	ProjectLogConfigurations logConfigurations;

	protected Project project;

	public void init(@NotNull Project project) {
		this.project = project;
		// Distribute the call..
		setLogConfigurations(getLogConfigurations());
	}

	@Nullable
	public LogId getLogId(String name) {
		for (LogId logId : getLogIds()) {
			if (name != null && name.equals(logId.getName()))
				return logId;
		}
		return null;
	}

	@NotNull
	public List<LogId> getLogIds() {
		if (logIds == null) {
			logIds = new ArrayList<LogId>();
			logIds.add(new LogId("Default"));
		}
		return logIds;
	}

	public void setLogIds(List<LogId> logIds) {
		this.logIds = logIds;
	}

	@NotNull
	public LogConfiguration getLogConfiguration(@NotNull PsiFile file) {
		for (TargetedLogConfiguration configuration : getLogConfigurations().getTargetedLogConfigurations()) {
			if (configuration.isTargetForFile(file))
				return configuration;
		}
		return getLogConfigurations().getDefaultLogConfiguration();
	}

	@NotNull
	public ProjectLogConfigurations getLogConfigurations() {
		ProjectLogConfigurations configurations = logConfigurations;
		if (configurations == null)
			setLogConfigurations(configurations = new ProjectLogConfigurations());
		return configurations;
	}

	public void setLogConfigurations(ProjectLogConfigurations logConfigurations) {
		this.logConfigurations = logConfigurations;
		if (logConfigurations != null)
			logConfigurations.init(project);
	}
}
