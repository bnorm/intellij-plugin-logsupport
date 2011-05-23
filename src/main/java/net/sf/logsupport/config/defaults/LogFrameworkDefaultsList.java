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

package net.sf.logsupport.config.defaults;

import net.sf.logsupport.config.LogFramework;

import java.util.*;

/**
 * Lists the default settings for common log frameworks.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class LogFrameworkDefaultsList extends AbstractList<LogFramework> {

	private List<LogFramework> defaultLogFrameworks = Arrays.asList(
			(LogFramework) new SLF4JLogFramework(),
			(LogFramework) new JBossLogFrameworkF(),
			(LogFramework) new JBossLogFrameworkV(),
			(LogFramework) new LOG4J12LogFramework(),
			(LogFramework) new JULLogFramework(),
			(LogFramework) new CommonsLogFramework(),
			(LogFramework) new IntellijLogFramework()
	);

	/**
	 * Returns true if the given framework instance can be reset to built-in defaults.
	 *
	 * @param framework The framework to query.
	 * @return true if the given framework instance can be reset to built-in defaults.
	 */
	public boolean canResetToDefaults(LogFramework framework) {
		for (LogFramework f : defaultLogFrameworks)
			if (f.getLoggerClass().equals(framework.getLoggerClass()))
				return true;
		return false;
	}

	/**
	 * Resets the given framework to the built-in defaults if it's logger class is recognized
	 * by one of the built-in settings.
	 *
	 * @param framework The framework instance to reset.
	 * @return true if the given framework instance was reset to built-in defaults.
	 */
	public boolean resetToDefaults(LogFramework framework) {
		for (LogFramework f : defaultLogFrameworks)
			if (f.getLoggerClass().equals(framework.getLoggerClass())) {
				String protectedName = framework.getName();
				try {
					framework.importSettings(f);
				} finally {
					framework.setName(protectedName);
				}
				return true;
			}
		return false;
	}

	/**
	 * Returns all newly added defaults that do not yet exist inside the configuration.
	 *
	 * @param existing a list of existing log frameworks.
	 * @return all newly added defaults that do not yet exist inside the configuration.
	 */
	public List<LogFramework> getMissingDefaults(Collection<LogFramework> existing) {
		final List<LogFramework> missing = new ArrayList<LogFramework>();

		search:
		for (LogFramework defaultLogFramework : defaultLogFrameworks) {
			for (LogFramework framework : existing) {
				if (defaultLogFramework.getLoggerClass().equals(framework.getLoggerClass())
						&& defaultLogFramework.getLogMethod().equals(framework.getLogMethod())) {
					continue search;
				}

				if (defaultLogFramework.getName().equalsIgnoreCase(framework.getName()))
					continue search;
			}

			missing.add(defaultLogFramework);
		}

		return missing;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogFramework get(int index) {
		return defaultLogFrameworks.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return defaultLogFrameworks.size();
	}
}
