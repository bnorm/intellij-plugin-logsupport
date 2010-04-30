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

package net.sf.logsupport.ui.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public abstract class AbstractConfigurable<E> implements Configurable {

	private Editor editor;
	private E editableElement, initialElement;

	protected abstract Editor createEditor(E editableElement);

	protected abstract void copySettings(E source, E target);

	protected abstract E cloneElement(E source);

	public abstract E getSourceElement();

	protected void initPanel() {
		editableElement = initialElement = null;

		E sourceElement = getSourceElement();
		if (sourceElement != null) {
			editableElement = cloneElement(sourceElement);
			reset();
		}
	}

	public Icon getIcon() {
		return null;
	}

	public String getHelpTopic() {
		return null;
	}

	public JComponent createComponent() {
		if (editor == null)
			editor = createEditor(editableElement);
		return editor.getEditorPanel();
	}

	public void disposeUIResources() {
		editor = null;
	}

	public boolean isModified() {
		if (editor != null)
			try {
				editor.apply();
			} catch (ConfigurationException e) {
				// ignore
			}

		return !editableElement.equals(initialElement);
	}

	public void apply() throws ConfigurationException {
		if (editor != null)
			editor.apply();

		copySettings(editableElement, getSourceElement());
		initialElement = null;
		reset();
	}

	public void reset() {
		if (initialElement == null)
			initialElement = cloneElement(editableElement);
		else
			copySettings(initialElement, editableElement);

		if (editor != null)
			editor.reset();
	}
}
