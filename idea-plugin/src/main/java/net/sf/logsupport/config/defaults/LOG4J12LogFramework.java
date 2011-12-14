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

import java.util.Arrays;

/**
 * Defines the defaults for the SLF4J framework.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class LOG4J12LogFramework extends LogFrameworkDefaults {
	LOG4J12LogFramework() {
		super("log4j-1.2");

		setLoggerClass("org.apache.log4j.Logger");
		setLoggerFactoryMethod("org.apache.log4j.Logger.getLogger(%s.class)");

		getEnabledGetterMethod().keySet().removeAll(Arrays.asList(LogLevel.error, LogLevel.warn));
	}
}