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
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import net.sf.logsupport.L10N;
import net.sf.logsupport.config.ApplicationConfiguration;
import net.sf.logsupport.config.LogFramework;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.config.defaults.LogFrameworkDefaultsList;
import net.sf.logsupport.ui.util.AbstractEventListener;
import net.sf.logsupport.ui.util.BindFailedException;
import net.sf.logsupport.ui.util.GridLayoutFacade;
import net.sf.logsupport.ui.util.JComponentBinder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EventObject;

import static net.sf.logsupport.config.LogFramework.MessageFormatType;

/**
 * Implements an editor to view or configure the log framework support.
 *
 * @author Juergen_Kellerer, 2010-04-05
 * @version 1.0
 */
public class LogFrameworkEditor implements Editor {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.ui.config.LogFrameworkEditor");

	private final LogFramework source;

	private JPanel editorPanel; //NOSONAR - field is binding
	private JTabbedPane tabbedPane; //NOSONAR - field is binding

	private JPanel loggerFieldPanel; //NOSONAR - field is binding
	private JTextField defaultLoggerFieldName; //NOSONAR - field is binding
	private JComboBox fieldCreatePosition; //NOSONAR - field is binding
	private JComboBox loggerFieldAccessModifier; //NOSONAR - field is binding
	private JCheckBox useStaticLogger; //NOSONAR - field is binding
	private JCheckBox useFinalLogger; //NOSONAR - field is binding
	private JCheckBox logMethodsAreStatic; //NOSONAR - field is binding

	private JComboBox loggerClass; //NOSONAR - field is binding
	private JButton resetToDefaultsButton; //NOSONAR - field is binding

	private JTextField loggerFactoryMethod; //NOSONAR - field is binding

	private JPanel methodMappingPanel; //NOSONAR - field is binding
	private ArrayList<LevelMethodMapping> methodMappings = new ArrayList<LevelMethodMapping>(LogLevel.values().length);

	private JCheckBox logMessagesCanUsePlaceholders; //NOSONAR - field is binding
	private JPanel placeholdersDetailsPanel; //NOSONAR - field is binding
	private JComboBox placeholderFormatSelection; //NOSONAR - field is binding
	private JTextField placeholderCustomFormat; //NOSONAR - field is binding
	private JCheckBox placeholdersCanBeUsedWithThrowables; //NOSONAR - field is binding
	private JTextPane textPane1; //NOSONAR - field is binding

	private JComponentBinder<LogFramework> componentBinder;

	public LogFrameworkEditor(ApplicationConfiguration configuration, LogFramework source) {
		this.source = source;

		// Removing unfinished tabs:
		tabbedPane.remove(1);

		// Configure field create options
		fieldCreatePosition.setModel(new DefaultComboBoxModel(new Object[]{
				"Create on top of the class using:", "Create at bottom of the class using:"}));
		loggerFieldAccessModifier.setModel(new DefaultComboBoxModel(
				LogFramework.LOGGER_ACCESS_MODIFIERS.toArray()));

		final AbstractEventListener fieldCreateOptionsDisabler = new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				boolean enabled = !logMethodsAreStatic.isSelected();
				UIUtil.setEnabled(loggerFieldPanel, enabled, true);
				loggerFactoryMethod.setEnabled(enabled);
			}
		};
		logMethodsAreStatic.addItemListener(fieldCreateOptionsDisabler);
		fieldCreateOptionsDisabler.stateChanged(null);

		// Configure loggerClass related elements
		for (LogFramework framework : configuration.getFrameworks())
			loggerClass.addItem(framework.getLoggerClass());

		AbstractEventListener eventListener = new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				Action action = resetToDefaultsButton.getAction();
				resetToDefaultsButton.setEnabled(action != null && action.isEnabled());
			}
		};
		loggerClass.addItemListener(eventListener);
		loggerClass.addActionListener(eventListener);

		// Configure placeholder related elements
		for (LogFramework.MessageFormatType type : MessageFormatType.values())
			placeholderFormatSelection.addItem(L10N.message("LogFrameworkEditor.MessageFormatType." + type.name()));

		final AbstractEventListener placeholderDetailsEnabler = new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				boolean enabled = logMessagesCanUsePlaceholders.isSelected();
				UIUtil.setEnabled(placeholdersDetailsPanel, enabled, true);
				placeholderCustomFormat.setEnabled(enabled &&
						placeholderFormatSelection.getSelectedIndex() == MessageFormatType.custom.ordinal());
			}
		};
		placeholderFormatSelection.addItemListener(placeholderDetailsEnabler);
		logMessagesCanUsePlaceholders.addChangeListener(placeholderDetailsEnabler);
		placeholderDetailsEnabler.stateChanged(null);

		// Build the level to method mapping.
		GridLayoutFacade layoutFacade = new GridLayoutFacade(methodMappingPanel, 3, 1, 2, 2);
		for (LogLevel level : LogLevel.values()) {
			LevelMethodMapping m = new LevelMethodMapping(level);
			methodMappings.add(m);
			m.addToPanel(layoutFacade);
		}

		// Binding the source content.
		componentBinder = new JComponentBinder<LogFramework>(this, source);
		reset();

		// Adding actions
		resetToDefaultsButton.setAction(new ResetToDefaultsAction());
	}

	public void reset() {
		placeholderFormatSelection.setSelectedIndex(source.getLogMessageFormatType().ordinal());
		fieldCreatePosition.setSelectedIndex(source.isInsertLoggerAtEndOfClass() ? 1 : 0);
		try {
			componentBinder.reset(source);
		} catch (BindFailedException e) {
			LOG.error(e);
		}

		for (LevelMethodMapping mapping : methodMappings)
			mapping.reset();
	}

	public void apply() {
		source.setLogMessageFormatType(MessageFormatType.values()[placeholderFormatSelection.getSelectedIndex()]);
		source.setInsertLoggerAtEndOfClass(fieldCreatePosition.getSelectedIndex() == 1);
		try {
			componentBinder.apply(source);
		} catch (BindFailedException e) {
			LOG.error(e);
		}

		for (LevelMethodMapping mapping : methodMappings)
			mapping.apply();
	}

	public JPanel getEditorPanel() {
		return editorPanel;
	}

	/**
	 * Defines an action that can reset the framework settings to the build-in defaults.
	 */
	class ResetToDefaultsAction extends AbstractAction {

		LogFrameworkDefaultsList defaults = ApplicationConfiguration.getLogFrameworkDefaults();

		ResetToDefaultsAction() {
			super("Defaults");
		}

		@Override
		public boolean isEnabled() {
			apply();
			return super.isEnabled() && defaults.canResetToDefaults(source);
		}

		public void actionPerformed(ActionEvent e) {
			if (Messages.showYesNoDialog(editorPanel,
					"Do you really want to reset all settings to built-in defaults?\n" +
							"Any changes made to this page will be overwritten.",
					"Confirm reset to defaults", Messages.getWarningIcon()) == JOptionPane.YES_OPTION) {
				apply();
				defaults.resetToDefaults(source);
				reset();
			}
		}
	}

	/**
	 * Defines the subset of fields and actions to map log metods and getters.
	 */
	class LevelMethodMapping {
		LogLevel level;
		JTextField method = new JTextField(), enabledGetter = new JTextField();

		LevelMethodMapping(LogLevel level) {
			this.level = level;
			reset();
		}

		void addToPanel(GridLayoutFacade facade) {
			facade.add(new JLabel(level.name().toUpperCase()));
			facade.add(method);
			facade.add(enabledGetter);
		}

		void reset() {
			String methodExpression = source.getLogMethod().get(level),
					conditionalExpression = source.getEnabledGetterMethod().get(level);
			method.setText(methodExpression == null ? "" : methodExpression);
			enabledGetter.setText(conditionalExpression == null ? "" : conditionalExpression);
		}

		void apply() {
			String text = getText(method);
			if (text == null)
				source.getLogMethod().remove(level);
			else
				source.getLogMethod().put(level, text);

			text = getText(enabledGetter);
			if (text == null)
				source.getEnabledGetterMethod().remove(level);
			else
				source.getEnabledGetterMethod().put(level, text);
		}

		private String getText(JTextField field) {
			String text = field.getText();
			if (text != null)
				text = text.trim();
			else
				text = "";
			return text.isEmpty() ? null : text;
		}
	}
}
