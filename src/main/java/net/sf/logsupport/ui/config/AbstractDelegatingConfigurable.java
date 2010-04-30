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
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Is a delegating base class to create cleaner configurable project components.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public abstract class AbstractDelegatingConfigurable implements Configurable {

	/**
	 * Returns the delegate to use.
	 * @return the delegate to use.
	 */
	protected abstract Configurable getConfigurableDelegate();

	/**
	 * {@inheritDoc}
	 */
	@Nls
	public String getDisplayName() {
		return getConfigurableDelegate().getDisplayName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	public Icon getIcon() {
		return getConfigurableDelegate().getIcon();
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNls
	@Nullable
	public String getHelpTopic() {
		return getConfigurableDelegate().getHelpTopic();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	public JComponent createComponent() {
		return getConfigurableDelegate().createComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isModified() {
		return getConfigurableDelegate().isModified();
	}

	/**
	 * {@inheritDoc}
	 */
	public void apply() throws ConfigurationException {
		getConfigurableDelegate().apply();
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		getConfigurableDelegate().reset();
	}

	/**
	 * {@inheritDoc}
	 */
	public void disposeUIResources() {
		getConfigurableDelegate().disposeUIResources();
	}
}
