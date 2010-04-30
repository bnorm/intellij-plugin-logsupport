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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import net.sf.logsupport.LogSupportComponent;
import net.sf.logsupport.config.defaults.LogFrameworkDefaultsList;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the application specific configuration.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public class ApplicationConfiguration {

	public static ApplicationConfiguration getInstance() {
		return ApplicationManager.getApplication().getComponent(LogSupportComponent.class).getState();
	}

	private static final LogFrameworkDefaultsList logFrameworkDefaults = new LogFrameworkDefaultsList();

	public static LogFrameworkDefaultsList getLogFrameworkDefaults() {
		return logFrameworkDefaults;
	}

	private List<LogFramework> frameworks;

	public LogFramework getFramework(String name) {
		name = String.valueOf(name);
		for (LogFramework framework : getFrameworks()) {
			if (name.equals(framework.getName()))
				return framework;
		}
		return null;
	}

	public List<LogFramework> getFrameworks() {
		if (frameworks == null)
			frameworks = new ArrayList<LogFramework>();
		if (frameworks.isEmpty()) {
			for (LogFramework framework : logFrameworkDefaults)
				frameworks.add(framework.copy());
		}
		return frameworks;
	}

	public void setFrameworks(List<LogFramework> frameworks) {
		this.frameworks = frameworks;
	}
}
