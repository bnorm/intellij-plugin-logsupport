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
 * @version 1.0
 */
public enum ConditionFormat {
	simple,
	simpleWithNewLine,
	simpleBlock,
	simpleBlockWithNewLine,;

	public static ConditionFormat toBlockFormat(ConditionFormat format) {
		switch (format) {
			case simple:
				return simpleBlock;
			case simpleWithNewLine:
				return simpleBlockWithNewLine;
			default:
				return format;
		}
	}

	public static ConditionFormat toNonBlockFormat(ConditionFormat format) {
		switch (format) {
			case simpleBlock:
				return simple;
			case simpleBlockWithNewLine:
				return simpleWithNewLine;
			default:
				return format;
		}
	}
}
