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

package net.sf.logsupport;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Static method for accessing localized messages.
 *
 * @author Juergen_Kellerer, 2010-04-02
 * @version 1.0
 */
public class L10N {

	@NonNls
	private static final String BUNDLE = "net.sf.logsupport.L10N";
	
	private static Reference<ResourceBundle> bundle;

	private L10N() {
	}

	public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
		return CommonBundle.message(getBundle(), key, params);
	}

	public static String defaultableMessage(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
		return CommonBundle.messageOrDefault(getBundle(), key, "default", true, params);
	}

	private static ResourceBundle getBundle() {
		ResourceBundle bundle = null;

		if (L10N.bundle != null)
			bundle = L10N.bundle.get();

		if (bundle == null) {
			bundle = ResourceBundle.getBundle(BUNDLE);
			L10N.bundle = new SoftReference<ResourceBundle>(bundle);
		}

		return bundle;
	}
}
