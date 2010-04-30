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

/**
 * Generator & extractor for Log IDs.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public interface LogIdGenerator {
	/**
	 * Extracts the id from the given log message.
	 *
	 * @param logMessage the log message to extract the id from.
	 * @return The extracted id or 'null' if the message contained no log id.
	 */
	String extractId(String logMessage);

	/**
	 * Returns the next id for the given target package or class.
	 *
	 * @return the next id for the given target package or class.
	 */
	String nextId();

	/**
	 * Returns the current id for the given target package or class.
	 *
	 * @return the current id for the given target package or class.
	 */
	String currentId();
}
