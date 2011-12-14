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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Abstract helper implementation to simplify the usage of MasterDetailsComponent.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public abstract class AbstractNamedConfigurable<E> extends NamedConfigurable<E> {

	private boolean isNew;
	private Editor editor;
	private E editableElement, initialElement;

	protected final AbstractMasterDetailsPanel<E> masterDetailsPanel;

	protected AbstractNamedConfigurable(@NotNull AbstractMasterDetailsPanel<E> masterDetailsPanel,
										E source, boolean isNew) {
		this.masterDetailsPanel = masterDetailsPanel;
		editableElement = isNew ? source : masterDetailsPanel.cloneElement(source);
		this.isNew = isNew;
		reset();
	}

	protected abstract Editor createEditor(E editableElement);

	protected abstract void copySettings(E source, E target);

	protected void addNewElementToBackingStore(E element) {
		masterDetailsPanel.getElementsFromBackingStore().add(element);
	}

	public boolean isModified() {
		if (editor != null)
			try {
				editor.apply();
			} catch (ConfigurationException e) {
				// ignore
			}

		return isNew || !editableElement.equals(initialElement);
	}

	public void apply() throws ConfigurationException {
		if (editor != null)
			editor.apply();

		if (isNew)
			addNewElementToBackingStore(editableElement);
		else {
			for (E element : masterDetailsPanel.getElementsFromBackingStore()) {
				if (masterDetailsPanel.getElementName(element).equals(
						masterDetailsPanel.getElementName(editableElement))) {
					copySettings(editableElement, element);
					break;
				}
			}
		}

		isNew = false;
		initialElement = null;
		reset();
	}

	public void reset() {
		if (initialElement == null)
			initialElement = masterDetailsPanel.cloneElement(editableElement);
		else
			copySettings(initialElement, editableElement);

		if (editor != null)
			editor.reset();
	}

	@Override
	public E getEditableObject() {
		return editableElement;
	}

	@Nls
	public String getDisplayName() {
		return masterDetailsPanel.getElementName(editableElement);
	}

	@Override
	public void setDisplayName(String s) {
		masterDetailsPanel.setElementName(editableElement, s);
	}

	@Override
	public String getBannerSlogan() {
		return getDisplayName();
	}

	public Icon getIcon() {
		return null;
	}

	public String getHelpTopic() {
		return null;
	}

	@Override
	public JComponent createOptionsPanel() {
		if (editor == null)
			editor = createEditor(editableElement);
		return editor.getEditorPanel();
	}

	public void disposeUIResources() {
		editor = null;
	}
}

