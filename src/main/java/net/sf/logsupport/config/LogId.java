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

import net.sf.logsupport.util.BasicLogIdGenerator;
import net.sf.logsupport.util.LogIdGenerator;
import net.sf.logsupport.util.NumericLogIdGenerator;

import java.util.regex.Pattern;

/**
 * Defines the settings to create a log id.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public class LogId implements Cloneable {

	public static enum Format {
		decimal,
		hexadecimal,
		custom
	}

	public static String formatId(String formatPattern, String prefix, String trailer, int idValue) {
		return prefix + String.format(formatPattern, idValue) + trailer;
	}

	public static NumericLogIdGenerator createGenerator(String matcherPattern, String formatPattern,
												 String prefix, String trailer,
												 int startSequence, int increment) {
		String idPattern = prefix + formatPattern + trailer;
		return new BasicLogIdGenerator(
				idPattern, Pattern.compile(matcherPattern, Pattern.DOTALL), startSequence, increment);
	}

	public static boolean validateValues(String matcherPattern, String formatPattern, String prefix, String trailer) {
		LogIdGenerator generator = createGenerator(matcherPattern, formatPattern, prefix, trailer, 10, 10);
		String id = generator.nextId();
		return id.equals(generator.extractId(id + "Sample Message")) &&
				id.equals(generator.extractId(id + " Sample Message \r\n\twith white-spaces."));
	}

	private String name;
	private String prefix = "LOG", trailer = ":";
	private Format format;
	private String formatPattern;
	private String matcherPattern;

	private int minValue, maxValue;

	{
		setFormat(Format.decimal);
	}

	public LogId() {
	}

	public LogId(String name) {
		this.name = name;
	}

	public void importFrom(LogId other) {
		setName(other.getName());
		setPrefix(other.getPrefix());
		setTrailer(other.getTrailer());
		setFormat(other.getFormat());
		formatPattern = other.getFormatPattern();
		matcherPattern = other.getMatcherPattern();
		setMinValue(other.getMinValue());
		setMaxValue(other.getMaxValue());
	}

	public NumericLogIdGenerator createGenerator(int startSequence, int increment) {
		NumericLogIdGenerator generator = createGenerator(
				matcherPattern, formatPattern, prefix, trailer, startSequence, increment);
		generator.setMinSequenceValue(minValue);
		generator.setMaxSequenceValue(maxValue);

		return generator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		if (prefix == null)
			prefix = "";

		if (!validateValues(matcherPattern, formatPattern, prefix, trailer))
			throw new IllegalArgumentException("The prefix '" + prefix +
					"' isn't matchable with '" + matcherPattern + "'.");

		this.prefix = prefix;
	}

	public String getTrailer() {
		return trailer;
	}

	public void setTrailer(String trailer) {
		if (trailer == null)
			trailer = "";

		if (!validateValues(matcherPattern, formatPattern, prefix, trailer))
			throw new IllegalArgumentException("The trailer '" + trailer +
					"' isn't matchable with '" + matcherPattern + "'.");

		this.trailer = trailer;
	}

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		if (format == null)
			throw new NullPointerException();

		this.format = format;

		if (format != Format.custom) {
			minValue = 0;
			switch (format) {
				case decimal:
					maxValue = 99999;
					formatPattern = "%05d";
					matcherPattern = "^(#*[A-Za-z_\\-+~\\.]+[0-9]+[:;#]).*$";
					break;
				case hexadecimal:
					maxValue = Integer.decode("#0fffff");
					formatPattern = "%05x";
					matcherPattern = "^(#*[A-Za-z_\\-+~\\.]+[A-Fa-f0-9]+[:;#]).*$";
					break;
			}
		}
	}

	public String getFormattedId(int idValue) {
		return formatId(formatPattern, prefix, trailer, idValue);
	}

	public String getFormatPattern() {
		return formatPattern;
	}

	public void setFormatPattern(String formatPattern) {
		if (format != Format.custom)
			return;

		try {
			formatId(formatPattern, prefix, trailer, 10);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Format pattern '" + formatPattern + "' is invalid.", e);
		}

		if (!validateValues(matcherPattern, formatPattern, prefix, trailer))
			throw new IllegalArgumentException("Format pattern '" + formatPattern +
					"' isn't matchable with '" + matcherPattern + "'.");

		this.formatPattern = formatPattern;
	}

	public String getMatcherPattern() {
		return matcherPattern;
	}

	public void setMatcherPattern(String matcherPattern) {
		if (!validateValues(matcherPattern, formatPattern, prefix, trailer))
			throw new IllegalArgumentException("The specified pattern '" + matcherPattern + "' is not valid.");
		this.matcherPattern = matcherPattern;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		if (format != Format.custom)
			return;
		if (minValue > maxValue)
			minValue = maxValue;
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		if (format != Format.custom)
			return;
		if (maxValue < minValue)
			maxValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public final LogId clone() {
		try {
			return (LogId) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LogId)) return false;

		LogId logId = (LogId) o;

		if (name != null ? !name.equals(logId.name) : logId.name != null) return false;
		if (!prefix.equals(logId.prefix)) return false;
		if (!trailer.equals(logId.trailer)) return false;
		if (format != logId.format) return false;
		if (!formatPattern.equals(logId.formatPattern)) return false;
		if (maxValue != logId.maxValue) return false;
		if (minValue != logId.minValue) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + prefix.hashCode();
		result = 31 * result + trailer.hashCode();
		result = 31 * result + format.hashCode();
		result = 31 * result + formatPattern.hashCode();
		result = 31 * result + minValue;
		result = 31 * result + maxValue;
		return result;
	}
}
