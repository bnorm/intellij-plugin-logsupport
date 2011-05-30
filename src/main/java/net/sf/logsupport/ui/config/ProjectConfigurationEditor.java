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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import net.sf.logsupport.L10N;
import net.sf.logsupport.config.*;
import net.sf.logsupport.ui.LogLevelSelectionPanel;
import net.sf.logsupport.ui.util.AbstractEventListener;
import net.sf.logsupport.ui.util.BindFailedException;
import net.sf.logsupport.ui.util.JComponentBinder;

import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

/**
 * Implements the main editor for project values.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public class ProjectConfigurationEditor implements Editor {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.ui.config.ProjectConfigurationEditor");

	private JPanel editorPanel; //NOSONAR - field is binding
	private JComboBox defaultFrameworkName; //NOSONAR - field is binding
	private JCheckBox forceUsingDefaultLogFramework; //NOSONAR - field is binding

	private JComboBox logIdName; //NOSONAR - field is binding

	private JPanel defaultSequencePanel; //NOSONAR - field is binding
	private JCheckBox customizedSequence; //NOSONAR - field is binding
	private JSpinner logIdMinValue; //NOSONAR - field is binding
	private JSpinner logIdMaxValue; //NOSONAR - field is binding
	private JSpinner logIdIncrement; //NOSONAR - field is binding

	private JPanel conditionalLogContainer; //NOSONAR - field is binding
	private JComboBox conditionFormatSelector; //NOSONAR - field is binding
	private final LogLevelSelectionPanel conditionalLevelsPanel = new LogLevelSelectionPanel(3);

	private JPanel logIdLevelsContainer; //NOSONAR - field is binding
	private final LogLevelSelectionPanel logIdLevelsPanel = new LogLevelSelectionPanel(3);

	private JPanel targetedLogConfigurationContainer; //NOSONAR - field is binding
	private final TargetedLogConfigurationPanel targetedLogConfigurationPanel;

	private final JComponentBinder<DefaultLogConfiguration> binder;
	private final ProjectLogConfigurations editableElement;

	public ProjectConfigurationEditor(Project project, ProjectLogConfigurations editableElement) {
		if (editableElement == null)
			throw new NullPointerException("Editable element may not be null.");

		this.editableElement = editableElement;

		final String disabled = "<html><i>" + L10N.message("ProjectConfigurationEditor.disabled") + "</i></html>";

		defaultFrameworkName.addItem(disabled);
		defaultFrameworkName.setSelectedItem(disabled);
		for (LogFramework framework : ApplicationConfiguration.getInstance().getFrameworks())
			defaultFrameworkName.addItem(framework.getName());

		logIdName.addItem(disabled);
		logIdName.setSelectedItem(disabled);
		for (LogId logId : ProjectConfiguration.getInstance(project).getLogIds())
			logIdName.addItem(logId.getName());

		for (ConditionFormat format : ConditionFormat.values()) {
			conditionFormatSelector.addItem(L10N.message(
					"ProjectConfigurationEditor.ConditionFormat." + format.name()));
		}

		AbstractEventListener sequenceCustomizationEnabler = new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				boolean idsEnabled = logIdName.getSelectedIndex() != 0;
				UIUtil.setEnabled(defaultSequencePanel, idsEnabled && customizedSequence.isSelected(), true);
				customizedSequence.setEnabled(idsEnabled);
			}
		};
		customizedSequence.addItemListener(sequenceCustomizationEnabler);
		logIdName.addItemListener(sequenceCustomizationEnabler);

		// Create log level mappings
		conditionalLogContainer.add(BorderLayout.LINE_START, conditionalLevelsPanel);
		logIdLevelsContainer.add(BorderLayout.LINE_START, logIdLevelsPanel);

		// Create targeted table
		targetedLogConfigurationPanel = new TargetedLogConfigurationPanel(project, editableElement);
		targetedLogConfigurationContainer.add(BorderLayout.CENTER, targetedLogConfigurationPanel);

		// Create binder and call reset to initialize values.
		binder = new JComponentBinder<DefaultLogConfiguration>(this, editableElement.getDefaultLogConfiguration());
		reset();
	}

	/**
	 * {@inheritDoc}
	 */
	public void apply() throws ConfigurationException {
		targetedLogConfigurationPanel.apply();

		DefaultLogConfiguration config = editableElement.getDefaultLogConfiguration();

		try {
			binder.apply(config);
		} catch (BindFailedException e) {
			throw new ConfigurationException("");
		}

		config.setConditionFormat(ConditionFormat.values()[conditionFormatSelector.getSelectedIndex()]);
		config.setConditionalLogLevels(conditionalLevelsPanel.getSelectedLevels());
		config.setLogIdLevels(logIdLevelsPanel.getSelectedLevels());
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		targetedLogConfigurationPanel.reset();

		DefaultLogConfiguration config = editableElement.getDefaultLogConfiguration();

		try {
			binder.reset(config);
		} catch (BindFailedException e) {
			LOG.error(e);
		}

		conditionFormatSelector.setSelectedIndex(config.getConditionFormat().ordinal());
		conditionalLevelsPanel.setSelectedLevels(config.getConditionalLogLevels());
		logIdLevelsPanel.setSelectedLevels(config.getLogIdLevels());
	}

	/**
	 * {@inheritDoc}
	 */
	public JPanel getEditorPanel() {
		return editorPanel;
	}
}
