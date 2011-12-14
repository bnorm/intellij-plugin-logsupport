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
public class AndroidLogFramework extends LogFrameworkDefaults {

	private static final Map<LogLevel, String[]> androidMapping = new HashMap<LogLevel, String[]>();

	static {
		androidMapping.put(LogLevel.trace, new String[] {"VERBOSE", "v"});
		androidMapping.put(LogLevel.debug, new String[] {"DEBUG", "d"});
		androidMapping.put(LogLevel.info, new String[] {"INFO", "i"});
		androidMapping.put(LogLevel.warn, new String[] {"WARN", "w"});
		androidMapping.put(LogLevel.error, new String[] {"ERROR", "e"});
	}

	AndroidLogFramework() {
		super("android-util-log");

		setLogMethodsAreStatic(true);
		setDefaultLoggerFieldName("");
		setLoggerClass("android.util.Log");
		setLoggerFactoryMethod("");

		Map<LogLevel, String> m = getLogMethod();
		m.clear();
		for (LogLevel level : LogLevel.values())
			m.put(level, String.format("%s(\"MyTag\", ", androidMapping.get(level)[1]));

		m = getEnabledGetterMethod();
		m.clear();
		for (LogLevel level : LogLevel.values())
			m.put(level, String.format("isLoggable(\"MyTag\", android.util.Log.%s)", androidMapping.get(level)[0]));
	}
}