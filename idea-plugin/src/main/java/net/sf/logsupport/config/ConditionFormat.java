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

/**
 * Defines the possible format for the log condition.
 *
 * @author Juergen_Kellerer, 2010-04-27
 */
public enum ConditionFormat {
	/**
	 * Simple {@code if (x) log(m); } format.
	 */
	simple,
	/**
	 * Simple {@code if (x) \n\t log(m); } format with line break.
	 */
	simpleWithNewLine,

	/**
	 * Single line {@code if (x) { log(m); }} format.
	 */
	block,
	/**
	 * Single line {@code if (x) \n { log(m); }} format.
	 */
	blockWithNewLine,;

	/**
	 * Translates this condition format to one using blocks.
	 *
	 * @return a block format.
	 */
	public ConditionFormat toBlockFormat() {
		switch (this) {
			case simple:
				return block;
			case simpleWithNewLine:
				return blockWithNewLine;
			default:
				return this;
		}
	}

	/**
	 * Translates this condition format to one that is not using blocks.
	 *
	 * @return a format without any braces.
	 */
	public ConditionFormat toNonBlockFormat() {
		switch (this) {
			case block:
				return simple;
			case blockWithNewLine:
				return simpleWithNewLine;
			default:
				return this;
		}
	}
}
