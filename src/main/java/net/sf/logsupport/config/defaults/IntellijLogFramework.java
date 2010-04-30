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

import net.sf.logsupport.config.LogLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines the defaults for the intellij internal logging (can be used with plugin dev).
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class IntellijLogFramework extends LogFrameworkDefaults {
	IntellijLogFramework() {
		super("Intellij-IDEA");

		setDefaultLoggerFieldName("LOG");
		setLoggerClass("com.intellij.openapi.diagnostic.Logger");
		setLoggerFactoryMethod("com.intellij.openapi.diagnostic.Logger.getInstance(\"#%s\")");

		getLogMethod().remove(LogLevel.trace);

		List<LogLevel> noGetterLevels = new ArrayList<LogLevel>(Arrays.asList(LogLevel.values()));
		noGetterLevels.remove(LogLevel.debug);
		getEnabledGetterMethod().keySet().removeAll(noGetterLevels);
	}
}