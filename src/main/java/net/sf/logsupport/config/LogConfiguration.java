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

import com.intellij.psi.PsiFile;
import net.sf.logsupport.util.NumericLogIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Defines the main module or package specific configuration that is used by intentions, actions, live-templates, etc.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public abstract class LogConfiguration { //NOSONAR

	/**
	 * Returns the log configuration instance on the specified PsiFile.
	 *
	 * @param file The file to resolve the log configuration for.
	 * @return the log configuration instance on the specified PsiFile.
	 */
	@NotNull
	public static LogConfiguration getInstance(PsiFile file) {
		return ProjectConfiguration.getInstance(file.getProject()).getLogConfiguration(file);
	}

	protected LogFramework defaultLogFramework;
	protected NumericLogIdGenerator logIdGenerator;
	protected boolean forceUsingDefaultLogFramework;

	/**
	 * Returns true if the log configuration allows the usage of the named logger class.
	 * <p/>
	 * Note: The class of the logger doesn't necessarily need to be the same as the configured
	 * log default framework, for the case that reusing existing logger instances is enabled.
	 *
	 * @param className The classname to look for.
	 * @return True if the logger class is supported for usage.
	 */
	public boolean isSupportedLoggerClass(String className) {
		return getSupportedLoggerClasses().contains(className);
	}

	/**
	 * Returns a set of supported logger class names.
	 *
	 * @return A set of logger names that are usable for logging.
	 */
	@NotNull
	public Set<String> getSupportedLoggerClasses() {
		Set<String> classes;
		LogFramework defaultLogFramework = getDefaultLogFramework();
		if (isForceUsingDefaultLogFramework()) {
			classes = defaultLogFramework == null ? Collections.<String>emptySet() :
					Collections.singleton(defaultLogFramework.getLoggerClass());
		} else {
			classes = new LinkedHashSet<String>();
			if (defaultLogFramework != null)
				classes.add(defaultLogFramework.getLoggerClass());

			for (LogFramework f : ApplicationConfiguration.getInstance().getFrameworks())
				classes.add(f.getLoggerClass());
		}

		return classes;
	}

	/**
	 * Returns the log framework for the given class if supported.
	 *
	 * @param loggerClassName The classname of the logger.
	 * @param methodName	  the method that was called.
	 * @return the log framework for the given class if supported or 'null'
	 *         if the logger is not backed by a supported framework.
	 */
	public LogFramework getSupportedFrameworkForLoggerClass(String loggerClassName, String methodName) {
		if (isForceUsingDefaultLogFramework()) {
			LogFramework framework = getDefaultLogFramework();
			return framework != null && framework.getLoggerClass().equals(loggerClassName) ? framework : null;
		} else {
			LogFramework match = null;
			for (LogFramework f : ApplicationConfiguration.getInstance().getFrameworks())
				if (f.getLoggerClass().equals(loggerClassName)) {
					match = f;

					for (String methodFragment : f.getLogMethod().values()) {
						if (methodFragment.contains(methodName))
							return f;
					}
				}

			return match;
		}
	}

	/**
	 * Returns true if only the default framework should be used.
	 *
	 * @return true if only the default framework should be used.
	 */
	public boolean isForceUsingDefaultLogFramework() {
		return forceUsingDefaultLogFramework;
	}

	/**
	 * Returns true if log support is enabled.
	 *
	 * @return true if log support is enabled.
	 */
	public boolean isEnabled() {
		return getDefaultLogFramework() != null;
	}

	/**
	 * Returns the default log framework to use.
	 *
	 * @return the default log framework to use.
	 */
	@Nullable
	public LogFramework getDefaultLogFramework() {
		return defaultLogFramework;
	}

	/**
	 * Returns true if log-id's are enabled.
	 *
	 * @return true if log-id's are enabled.
	 */
	public boolean isUseLogIds() {
		return getLogIdGenerator() != null;
	}

	/**
	 * Returns the log-id generator used to generate log-ids.
	 *
	 * @return the log-id generator used to generate log-ids.
	 */
	@Nullable
	public NumericLogIdGenerator getLogIdGenerator() {
		return logIdGenerator;
	}

	/**
	 * Returns the log levels that require an ID.
	 *
	 * @return the log levels that require an ID.
	 */
	@NotNull
	public abstract Set<LogLevel> getLogIdLevels();

	/**
	 * Returns the levels that require wrapping inside a conditional expression
	 * (when supported by the framework).
	 *
	 * @return the levels that require wrapping inside a conditional expression
	 *         (when supported by the framework).
	 */
	@NotNull
	public abstract Set<LogLevel> getConditionalLogLevels();

	/**
	 * Returns the format to use when creating a log condition.
	 *
	 * @return the format to use when creating a log condition.
	 */
	@NotNull
	public abstract ConditionFormat getConditionFormat();
}
