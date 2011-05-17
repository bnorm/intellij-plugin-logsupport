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

package net.sf.logsupport.livetemplates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.NotNull;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-03
 * @version 1.0
 */
public class TemplatesProvider implements DefaultLiveTemplatesProvider {

	private static final String[] TEMPLATES = {"/liveTemplates/logsupport"};

	/**
	 * {@inheritDoc}
	 */
	public String[] getDefaultLiveTemplateFiles() {
		return TEMPLATES.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getHiddenLiveTemplateFiles() {
		return new String[0];
	}
}
