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

package net.sf.logsupport.ui.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public class BindFailedException extends Exception implements Iterable<BindFailed> {

	BindFailed[] causes;

	public BindFailedException(Collection<BindFailed> causes) {
		this(causes.toArray(new BindFailed[causes.size()]));
	}

	public BindFailedException(BindFailed[] causes) {
		this.causes = causes;
	}

	public BindFailed[] getCauses() {
		return causes;
	}

	public Iterator<BindFailed> iterator() {
		return Arrays.asList(causes).iterator();
	}
}
