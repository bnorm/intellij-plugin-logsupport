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

import com.intellij.openapi.project.Project;
import net.sf.logsupport.L10N;
import net.sf.logsupport.config.ProjectLogConfigurations;
import org.jetbrains.annotations.Nls;

/**
 * Implements a project orientated configurable for the log configurations.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public class ProjectLogConfigurable extends AbstractConfigurable<ProjectLogConfigurations> {

	private Project project;
	private ProjectLogConfigurations source;

	public ProjectLogConfigurable(Project project, ProjectLogConfigurations source) {
		this.project = project;
		this.source = source;
		initPanel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProjectLogConfigurations getSourceElement() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nls
	public String getDisplayName() {
		return L10N.message("name");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Editor createEditor(ProjectLogConfigurations editableElement) {
		return new ProjectConfigurationEditor(project, editableElement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void copySettings(ProjectLogConfigurations source, ProjectLogConfigurations target) {
		target.importFrom(source);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ProjectLogConfigurations cloneElement(ProjectLogConfigurations source) {
		try {
			return source.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
