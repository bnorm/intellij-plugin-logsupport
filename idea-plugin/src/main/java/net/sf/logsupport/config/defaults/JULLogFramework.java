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

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the defaults for the SLF4J framework.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class JULLogFramework extends LogFrameworkDefaults {

	private static final Map<LogLevel, String> julLevels = new HashMap<LogLevel, String>();

	static {
		julLevels.put(LogLevel.trace, "FINER");
		julLevels.put(LogLevel.debug, "FINE");
		julLevels.put(LogLevel.info, "INFO");
		julLevels.put(LogLevel.warn, "WARNING");
		julLevels.put(LogLevel.error, "SEVERE");
	}

	JULLogFramework() {
		super("java-util-logging");

	    setDefaultLoggerFieldName("logger");
		setLoggerClass("java.util.logging.Logger");
		setLoggerFactoryMethod("java.util.logging.Logger.getLogger(%s.class.getName())");

		setLogMessagesCanUsePlaceholders(true);
		setLogMessageFormatType(MessageFormatType.messageformat);
		setPlaceholdersCanBeUsedWithThrowables(true);

		Map<LogLevel, String> m = getLogMethod();
		m.clear();
		for (LogLevel level : LogLevel.values())
			m.put(level, String.format("log(java.util.logging.Level.%s, ", julLevels.get(level)));

		m = getEnabledGetterMethod();
		m.clear();
		for (LogLevel level : LogLevel.values())
			m.put(level, String.format("isLoggable(java.util.logging.Level.%s)", julLevels.get(level)));
	}
}