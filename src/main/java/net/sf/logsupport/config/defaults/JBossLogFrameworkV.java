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

import java.util.EnumSet;

/**
 * Defines the defaults for the SLF4J framework.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class JBossLogFrameworkV extends LogFrameworkDefaults {
	JBossLogFrameworkV() {
		super("jboss-logging-mf");

		setDefaultLoggerFieldName("LOG");
		setLoggerClass("org.jboss.logging.BasicLogger");
		setLoggerFactoryMethod("org.jboss.logging.Logger.getLogger(%s.class)");

		setLogMessagesCanUsePlaceholders(true);
		setPlaceholdersCanBeUsedWithThrowables(true);
		setLogMessageFormatType(MessageFormatType.messageformat);

		for (LogLevel logLevel : LogLevel.values())
			getLogMethod().put(logLevel, logLevel.name() + "v");

		for (LogLevel logLevel : EnumSet.of(LogLevel.warn, LogLevel.error))
			getEnabledGetterMethod().remove(logLevel);
	}
}
