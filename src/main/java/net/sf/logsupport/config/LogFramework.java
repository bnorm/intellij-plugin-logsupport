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

import com.intellij.psi.PsiModifier;

import java.util.*;

/**
 * Describes all log framework specific settings.
 * <p/>
 * Note: Take care that every modification to the fields in this class requires that
 * importSettings, equals and hashCode are adjusted accordingly!.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class LogFramework {
	/**
	 * Lists all access modifiers that are valid to use with logger fields.
	 */
	public static final Set<String> LOGGER_ACCESS_MODIFIERS = Collections.unmodifiableSet(new LinkedHashSet<String>(
			Arrays.asList(PsiModifier.PRIVATE, PsiModifier.PACKAGE_LOCAL, PsiModifier.PROTECTED, PsiModifier.PUBLIC)));

	public static enum MessageFormatType {
		none, printf, messageformat, custom
	}

	private String name;

	private String loggerClass;
	private String loggerFactoryMethod;
	private String defaultLoggerFieldName = "log";
	private boolean insertLoggerAtEndOfClass;
	private String loggerFieldAccessModifier = PsiModifier.PRIVATE;
	private boolean useStaticLogger = true, useFinalLogger = true;


	private boolean logMessagesCanUsePlaceholders, placeholdersCanBeUsedWithThrowables;
	private MessageFormatType logMessageFormatType = MessageFormatType.none;
	private String placeholderCustomFormat;

	private Map<LogLevel, String> logMethod = new HashMap<LogLevel, String>();
	private Map<LogLevel, String> enabledGetterMethod = new HashMap<LogLevel, String>();

	{
		// Setting defaults for method names and enabledGetters.
		for (LogLevel level : LogLevel.values()) {
			logMethod.put(level, level.name());
			enabledGetterMethod.put(level, String.format("is%sEnabled",
					Character.toUpperCase(level.name().charAt(0)) + level.name().substring(1)));
		}
	}

	public LogFramework() {
	}

	public LogFramework(String name) {
		this.name = name;
	}

	/**
	 * Imports the settings from the other framework.
	 *
	 * @param other The framework to import all settings from.
	 */
	public void importSettings(LogFramework other) {
		if (other == this)
			return;

		name = other.name;

		loggerClass = other.loggerClass;
		loggerFactoryMethod = other.loggerFactoryMethod;
		defaultLoggerFieldName = other.defaultLoggerFieldName;
		loggerFieldAccessModifier = other.loggerFieldAccessModifier;
		insertLoggerAtEndOfClass = other.insertLoggerAtEndOfClass;
		useFinalLogger = other.useFinalLogger;
		useStaticLogger = other.useStaticLogger;

		logMessageFormatType = other.logMessageFormatType;
		logMessagesCanUsePlaceholders = other.logMessagesCanUsePlaceholders;
		placeholderCustomFormat = other.placeholderCustomFormat;
		placeholdersCanBeUsedWithThrowables = other.placeholdersCanBeUsedWithThrowables;
		
		logMethod.clear();
		logMethod.putAll(other.logMethod);

		enabledGetterMethod.clear();
		enabledGetterMethod.putAll(other.enabledGetterMethod);
	}

	/**
	 * Copies this instance and returns the copy.
	 *
	 * @return the copy of this instance.
	 */
	public LogFramework copy() {
		LogFramework copy = new LogFramework();
		copy.importSettings(this);
		return copy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LogFramework)) return false;

		LogFramework framework = (LogFramework) o;

		if (name != null ? !name.equals(framework.name) : framework.name != null) return false;
		if (loggerClass != null ? !loggerClass.equals(framework.loggerClass) : framework.loggerClass != null)
			return false;
		if (loggerFactoryMethod != null ?
				!loggerFactoryMethod.equals(framework.loggerFactoryMethod) :
				framework.loggerFactoryMethod != null)
			return false;
		if (defaultLoggerFieldName != null ?
				!defaultLoggerFieldName.equals(framework.defaultLoggerFieldName) :
				framework.defaultLoggerFieldName != null)
			return false;
		if (!loggerFieldAccessModifier.equals(framework.loggerFieldAccessModifier)) return false;
		if (insertLoggerAtEndOfClass != framework.insertLoggerAtEndOfClass) return false;
		if (useFinalLogger != framework.useFinalLogger) return false;
		if (useStaticLogger != framework.useStaticLogger) return false;
		if (logMessagesCanUsePlaceholders != framework.logMessagesCanUsePlaceholders) return false;
		if (logMessageFormatType != framework.logMessageFormatType) return false;
		if (placeholderCustomFormat != null ?
				!placeholderCustomFormat.equals(framework.placeholderCustomFormat) :
				framework.placeholderCustomFormat != null)
			return false;
		if (placeholdersCanBeUsedWithThrowables != framework.placeholdersCanBeUsedWithThrowables) return false;
		if (!logMethod.equals(framework.logMethod)) return false;
		if (!enabledGetterMethod.equals(framework.enabledGetterMethod)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (loggerClass != null ? loggerClass.hashCode() : 0);
		result = 31 * result + (loggerFactoryMethod != null ? loggerFactoryMethod.hashCode() : 0);
		result = 31 * result + (defaultLoggerFieldName != null ? defaultLoggerFieldName.hashCode() : 0);
		result = 31 * result + (insertLoggerAtEndOfClass ? 1 : 0);
		result = 31 * result + loggerFieldAccessModifier.hashCode();
		result = 31 * result + (useStaticLogger ? 1 : 0);
		result = 31 * result + (useFinalLogger ? 1 : 0);
		result = 31 * result + (logMessagesCanUsePlaceholders ? 1 : 0);
		result = 31 * result + logMessageFormatType.hashCode();
		result = 31 * result + (placeholderCustomFormat != null ? placeholderCustomFormat.hashCode() : 0);
		result = 31 * result + (placeholdersCanBeUsedWithThrowables ? 1 : 0);
		result = 31 * result + logMethod.hashCode();
		result = 31 * result + enabledGetterMethod.hashCode();
		return result;
	}

	public String getName() {
		return name == null ? "" : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoggerClass() {
		return loggerClass == null ? "" : loggerClass;
	}

	public void setLoggerClass(String loggerClass) {
		this.loggerClass = loggerClass;
	}

	public String getLoggerFactoryMethod() {
		return loggerFactoryMethod;
	}

	public String getLoggerFactoryMethod(String forClass) {
		try {
			return String.format(loggerFactoryMethod, forClass);
		} catch (IllegalFormatException e) {
			return loggerFactoryMethod;
		}
	}

	public void setLoggerFactoryMethod(String loggerFactoryMethod) {
		if (loggerFactoryMethod == null || loggerFactoryMethod.isEmpty())
			throw new IllegalArgumentException("Argument may not be empty");
		this.loggerFactoryMethod = loggerFactoryMethod;
	}

	public boolean isUseStaticLogger() {
		return useStaticLogger;
	}

	public void setUseStaticLogger(boolean useStaticLogger) {
		this.useStaticLogger = useStaticLogger;
	}

	public boolean isUseFinalLogger() {
		return useFinalLogger;
	}

	public void setUseFinalLogger(boolean useFinalLogger) {
		this.useFinalLogger = useFinalLogger;
	}

	public MessageFormatType getLogMessageFormatType() {
		return logMessageFormatType;
	}

	public void setLogMessageFormatType(MessageFormatType logMessageFormatType) {
		this.logMessageFormatType = logMessageFormatType == null ? MessageFormatType.none : logMessageFormatType;
	}

	public String getPlaceholderCustomFormat() {
		return placeholderCustomFormat;
	}

	public void setPlaceholderCustomFormat(String placeholderCustomFormat) {
		this.placeholderCustomFormat = placeholderCustomFormat;
	}

	public boolean isLogMessagesCanUsePlaceholders() {
		return logMessagesCanUsePlaceholders;
	}

	public void setLogMessagesCanUsePlaceholders(boolean logMessagesCanUsePlaceholders) {
		this.logMessagesCanUsePlaceholders = logMessagesCanUsePlaceholders;
	}

	public boolean isPlaceholdersCanBeUsedWithThrowables() {
		return placeholdersCanBeUsedWithThrowables;
	}

	public void setPlaceholdersCanBeUsedWithThrowables(boolean placeholdersCanBeUsedWithThrowables) {
		this.placeholdersCanBeUsedWithThrowables = placeholdersCanBeUsedWithThrowables;
	}

	public boolean isInsertLoggerAtEndOfClass() {
		return insertLoggerAtEndOfClass;
	}

	public void setInsertLoggerAtEndOfClass(boolean insertLoggerAtEndOfClass) {
		this.insertLoggerAtEndOfClass = insertLoggerAtEndOfClass;
	}

	public String getDefaultLoggerFieldName() {
		return defaultLoggerFieldName;
	}

	public void setDefaultLoggerFieldName(String defaultLoggerFieldName) {
		this.defaultLoggerFieldName = defaultLoggerFieldName;
	}

	public String getLoggerFieldAccessModifier() {
		return loggerFieldAccessModifier;
	}

	public void setLoggerFieldAccessModifier(String loggerFieldAccessModifier) {
		if (!LOGGER_ACCESS_MODIFIERS.contains(loggerFieldAccessModifier))
			throw new IllegalArgumentException("The access modifier must be one of " + LOGGER_ACCESS_MODIFIERS);
		this.loggerFieldAccessModifier = loggerFieldAccessModifier;
	}

	public Set<String> getDefaultLoggerFieldModifiers() {
		Set<String> modifiers = new LinkedHashSet<String>();
		modifiers.add(loggerFieldAccessModifier);
		if (useStaticLogger)
			modifiers.add(PsiModifier.STATIC);
		if (useFinalLogger)
			modifiers.add(PsiModifier.FINAL);
		return modifiers;
	}

	public Map<LogLevel, String> getLogMethod() {
		return logMethod;
	}

	public void setLogMethod(Map<LogLevel, String> logMethod) {
		if (logMethod == null)
			this.logMethod.clear();
		else
			this.logMethod = logMethod;
	}

	public Map<LogLevel, String> getEnabledGetterMethod() {
		return enabledGetterMethod;
	}

	public void setEnabledGetterMethod(Map<LogLevel, String> enabledGetterMethod) {
		if (enabledGetterMethod == null)
			this.enabledGetterMethod.clear();
		else
			this.enabledGetterMethod = enabledGetterMethod;
	}
}
