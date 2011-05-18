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

import com.intellij.openapi.project.Project;
import net.sf.logsupport.util.NumericLogIdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements the default log configuration.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public class DefaultLogConfiguration extends LogConfiguration implements Cloneable {

	public static final int DEFAULT_LOG_ID_INCREMENT = 10;

	private Set<LogLevel> logIdLevels;
	private Set<LogLevel> conditionalLogLevels;
	private ConditionFormat conditionFormat = ConditionFormat.lineBreak;

	String defaultFrameworkName;
	String logIdName;

	boolean customizedSequence;
	int logIdSequenceValue;
	int logIdIncrement = DEFAULT_LOG_ID_INCREMENT;
	int logIdFromValue = -1, logIdToValue = -1;

	transient Project project;

	public void init(Project project) {
		if (project == null)
			return;

		this.project = project;

		if (getLogIdGenerator() == null)
			createGenerator();
	}

	public String getDefaultFrameworkName() {
		return defaultFrameworkName;
	}

	public void setDefaultFrameworkName(String defaultFrameworkName) {
		if (defaultFrameworkName != null)
			defaultLogFramework = ApplicationConfiguration.getInstance().getFramework(defaultFrameworkName);
		this.defaultFrameworkName = defaultFrameworkName;
	}

	public boolean isForceUsingDefaultLogFramework() {
		return forceUsingDefaultLogFramework;
	}

	public void setForceUsingDefaultLogFramework(boolean forceUsingDefaultLogFramework) {
		this.forceUsingDefaultLogFramework = forceUsingDefaultLogFramework;
	}

	public LogId getLogId() {
		return project == null ? null : ProjectConfiguration.getInstance(project).getLogId(logIdName);
	}

	public String getLogIdName() {
		return logIdName;
	}

	public void setLogIdName(String logIdName) {
		this.logIdName = logIdName;

		if (logIdName == null || getLogIdGenerator() == null || !logIdName.equals(this.logIdName))
			createGenerator();
	}

	private void createGenerator() {
		LogId id = getLogId();
		if (id != null) {
			int fromValue = getLogIdFromValue(), toValue = getLogIdToValue();
			if (!isCustomizedSequence() || fromValue == -1 || fromValue < id.getMinValue())
				fromValue = id.getMinValue();
			if (!isCustomizedSequence() || toValue == -1 || toValue > id.getMaxValue())
				toValue = id.getMaxValue();
			if (fromValue > toValue)
				fromValue = toValue;

			int sequenceValue = getLogIdSequenceValue();
			if (sequenceValue < fromValue)
				setLogIdSequenceValue(sequenceValue = fromValue);

			logIdGenerator = id.createGenerator(sequenceValue,
					isCustomizedSequence() ? getLogIdIncrement() : DEFAULT_LOG_ID_INCREMENT);
			logIdGenerator.setMinSequenceValue(fromValue);
			logIdGenerator.setMaxSequenceValue(toValue);
		}
	}

	public boolean isCustomizedSequence() {
		return customizedSequence;
	}

	public void setCustomizedSequence(boolean customizedSequence) {
		if (this.customizedSequence == customizedSequence)
			return;

		this.customizedSequence = customizedSequence;

		// Update cached generator instance.
		if (customizedSequence) {
			if (getLogIdGenerator() == null)
				createGenerator();
			setLogIdFromValue(getLogIdFromValue());
			setLogIdToValue(getLogIdToValue());
			setLogIdIncrement(getLogIdIncrement());
		} else
			createGenerator();
	}

	public int getLogIdSequenceValue() {
		int value = logIdSequenceValue;

		NumericLogIdGenerator generator = getLogIdGenerator();
		if (generator != null)
			value = logIdSequenceValue = generator.getSequenceValue();

		return value;
	}

	public void setLogIdSequenceValue(int logIdSequenceValue) {
		NumericLogIdGenerator generator = getLogIdGenerator();
		if (generator != null)
			generator.setSequenceValue(logIdSequenceValue);

		this.logIdSequenceValue = logIdSequenceValue;
	}

	public int getLogIdIncrement() {
		return logIdIncrement;
	}

	public void setLogIdIncrement(int logIdIncrement) {
		NumericLogIdGenerator generator = getLogIdGenerator();
		if (generator != null && isCustomizedSequence() && logIdIncrement > -1)
			generator.setSequenceIncrement(logIdIncrement);

		this.logIdIncrement = logIdIncrement;
	}

	public int getLogIdFromValue() {
		return logIdFromValue;
	}

	public void setLogIdFromValue(int logIdFromValue) {
		if (logIdFromValue > -1) {
			LogId id = getLogId();
			if (id != null)
				logIdFromValue = Math.max(logIdFromValue, id.getMinValue());

			int toValue = getLogIdToValue();
			if (toValue > -1 && logIdFromValue > toValue)
				logIdFromValue = toValue;
		}

		NumericLogIdGenerator generator = getLogIdGenerator();
		if (generator != null && isCustomizedSequence() && logIdFromValue > -1)
			generator.setMinSequenceValue(logIdFromValue);

		this.logIdFromValue = logIdFromValue;
	}

	public int getLogIdToValue() {
		return logIdToValue;
	}

	public void setLogIdToValue(int logIdToValue) {
		if (logIdToValue > -1) {
			LogId id = getLogId();
			if (id != null)
				logIdToValue = Math.min(logIdToValue, id.getMaxValue());

			int fromValue = getLogIdFromValue();
			if (fromValue > -1 && logIdToValue < fromValue)
				logIdToValue = fromValue;
		}

		NumericLogIdGenerator generator = getLogIdGenerator();
		if (generator != null && isCustomizedSequence() && logIdToValue > -1)
			getLogIdGenerator().setMaxSequenceValue(logIdToValue);

		this.logIdToValue = logIdToValue;
	}

	@NotNull
	@Override
	public Set<LogLevel> getLogIdLevels() {
		if (logIdLevels == null) {
			logIdLevels = new HashSet<LogLevel>(Arrays.asList(LogLevel.values()));
			logIdLevels.removeAll(Arrays.asList(LogLevel.trace, LogLevel.debug));
		}
		return logIdLevels;
	}

	public void setLogIdLevels(Set<LogLevel> logIdLevels) {
		this.logIdLevels = logIdLevels;
	}

	@NotNull
	@Override
	public ConditionFormat getConditionFormat() {
		if (conditionFormat == null)
			conditionFormat = ConditionFormat.lineBreak;
		return conditionFormat;
	}

	public void setConditionFormat(ConditionFormat conditionFormat) {
		this.conditionFormat = conditionFormat;
	}

	@NotNull
	@Override
	public Set<LogLevel> getConditionalLogLevels() {
		if (conditionalLogLevels == null)
			conditionalLogLevels = new HashSet<LogLevel>();
		return conditionalLogLevels;
	}

	public void setConditionalLogLevels(Set<LogLevel> conditionalLogLevels) {
		this.conditionalLogLevels = conditionalLogLevels;
	}

	@Override
	public DefaultLogConfiguration clone() throws CloneNotSupportedException {
		DefaultLogConfiguration clone = (DefaultLogConfiguration) super.clone();
		clone.setConditionalLogLevels(new HashSet<LogLevel>(clone.getConditionalLogLevels()));
		clone.setLogIdLevels(new HashSet<LogLevel>(clone.getLogIdLevels()));
		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultLogConfiguration)) return false;

		DefaultLogConfiguration that = (DefaultLogConfiguration) o;

		if (logIdIncrement != that.logIdIncrement) return false;
		if (logIdToValue != that.logIdToValue) return false;
		if (logIdFromValue != that.logIdFromValue) return false;
		if (customizedSequence != that.customizedSequence) return false;
		if (conditionFormat != that.conditionFormat) return false;
		if (!getConditionalLogLevels().equals(that.getConditionalLogLevels())) return false;
		if (!getLogIdLevels().equals(that.getLogIdLevels())) return false;
		if (defaultFrameworkName != null ?
				!defaultFrameworkName.equals(that.defaultFrameworkName) :
				that.defaultFrameworkName != null)
			return false;
		if (logIdName != null ? !logIdName.equals(that.logIdName) : that.logIdName != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = getConditionalLogLevels().hashCode();
		result = 31 * result + (logIdName != null ? logIdName.hashCode() : 0);
		result = 31 * result + getLogIdLevels().hashCode();
		result = 31 * result + getConditionFormat().hashCode();
		result = 31 * result + logIdIncrement;
		result = 31 * result + logIdFromValue;
		result = 31 * result + logIdToValue;
		result = 31 * result + (customizedSequence ? 1 : 0);
		result = 31 * result + (defaultFrameworkName != null ? defaultFrameworkName.hashCode() : 0);
		return result;
	}
}
