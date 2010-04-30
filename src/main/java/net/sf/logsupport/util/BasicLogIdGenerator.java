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

package net.sf.logsupport.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic implementation of a log id generator.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public class BasicLogIdGenerator implements NumericLogIdGenerator {

	static Pattern extractSequenceSplitPattern = Pattern.compile("[^A-Fa-f0-9]+");

	final String idFormat;
	final Pattern idPattern;

	int increment, minValue, maxValue;

	final AtomicInteger sequence;

	public BasicLogIdGenerator() {
		this("LOG%05d:", Pattern.compile("^([A-Za-z0-9]+:).*$", Pattern.DOTALL), 0, 1);
	}

	public BasicLogIdGenerator(String idFormat, Pattern idPattern, int sequence, int increment) {
		this.idFormat = idFormat;
		this.idPattern = idPattern;

		this.sequence = new AtomicInteger(sequence);
		setSequenceIncrement(increment);
	}

	/**
	 * {@inheritDoc}
	 */
	public int parseSequenceValue(String id) {
		if (id != null) {
			String[] parts = extractSequenceSplitPattern.split(id);
			if (parts.length > 0) {
				if (createId(Integer.decode("#ffff")).contains("ffff"))
					return Integer.parseInt(parts[parts.length - 1], 16);
				return Integer.parseInt(parts[parts.length - 1]);
			}
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getSequenceValue() {
		return sequence.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSequenceValue(int value) {
		if (value < minValue)
			value = minValue;
		sequence.set(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getSequenceIncrement() {
		return increment;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSequenceIncrement(int increment) {
		this.increment = Math.max(1, increment);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMinSequenceValue() {
		return minValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinSequenceValue(int minValue) {
		this.minValue = minValue;
		if (sequence.get() < minValue)
			sequence.set(minValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxSequenceValue() {
		return maxValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxSequenceValue(int maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public String extractId(String logMessage) {
		Matcher m = idPattern.matcher(logMessage);
		if (m.matches())
			return m.group(1);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String nextId() {
		return createId(sequence.getAndAdd(increment));
	}

	/**
	 * {@inheritDoc}
	 */
	public String currentId() {
		return createId(sequence.get());
	}

	/**
	 * {@inheritDoc}
	 */
	public String createId(int sequenceValue) {
		if (maxValue > 0)
			sequenceValue = Math.min(maxValue, sequenceValue);
		return String.format(idFormat, sequenceValue);
	}
}
