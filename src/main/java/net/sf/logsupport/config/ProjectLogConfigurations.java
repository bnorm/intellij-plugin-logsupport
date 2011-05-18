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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all log configuration instances.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public class ProjectLogConfigurations implements Cloneable {

	DefaultLogConfiguration defaultLogConfiguration;
	List<TargetedLogConfiguration> targetedLogConfigurations;

	protected Project project;

	public void init(Project project) {
		this.project = project;
		// Setting the project inside the other areas..
		setDefaultLogConfiguration(getDefaultLogConfiguration());
	}

	public void importFrom(ProjectLogConfigurations other) {
		if (other == this)
			return;
		try {
			other = other.clone();
			setDefaultLogConfiguration(other.getDefaultLogConfiguration());
			setTargetedLogConfigurations(other.getTargetedLogConfigurations());
			init(other.project);
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public DefaultLogConfiguration getDefaultLogConfiguration() {
		if (defaultLogConfiguration == null)
			setDefaultLogConfiguration(new DefaultLogConfiguration());
		return defaultLogConfiguration;
	}

	public void setDefaultLogConfiguration(DefaultLogConfiguration defaultLogConfiguration) {
		this.defaultLogConfiguration = defaultLogConfiguration;
		if (defaultLogConfiguration != null) {
			defaultLogConfiguration.init(project);
			setTargetedLogConfigurations(getTargetedLogConfigurations());
		}
	}

	public TargetedLogConfiguration createTargetedLogConfiguration(String targetPackage) {
		TargetedLogConfiguration configuration = new TargetedLogConfiguration();
		configuration.init(project, getDefaultLogConfiguration());
		configuration.setTargetScopeName(targetPackage);
		return configuration;
	}

	public List<TargetedLogConfiguration> getTargetedLogConfigurations() {
		if (targetedLogConfigurations == null)
			targetedLogConfigurations = new ArrayList<TargetedLogConfiguration>();
		return targetedLogConfigurations;
	}

	public void setTargetedLogConfigurations(List<TargetedLogConfiguration> targetedLogConfigurations) {
		this.targetedLogConfigurations = targetedLogConfigurations;
		if (targetedLogConfigurations != null) {
			for (TargetedLogConfiguration configuration : targetedLogConfigurations)
				configuration.init(project, getDefaultLogConfiguration());
		}
	}

	@Override
	public ProjectLogConfigurations clone() throws CloneNotSupportedException {
		ProjectLogConfigurations clone = (ProjectLogConfigurations) super.clone();

		clone.setDefaultLogConfiguration(clone.getDefaultLogConfiguration().clone());

		List<TargetedLogConfiguration> configurations = new ArrayList<TargetedLogConfiguration>();
		for (TargetedLogConfiguration c : clone.getTargetedLogConfigurations())
			configurations.add(c.clone());
		clone.setTargetedLogConfigurations(configurations);

		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ProjectLogConfigurations)) return false;

		ProjectLogConfigurations that = (ProjectLogConfigurations) o;

		if (!getDefaultLogConfiguration().equals(that.getDefaultLogConfiguration())) return false;
		if (!getTargetedLogConfigurations().equals(that.getTargetedLogConfigurations())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = getDefaultLogConfiguration().hashCode();
		result = 31 * result + getTargetedLogConfigurations().hashCode();
		return result;
	}
}
