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
import com.intellij.util.ui.UIUtil;
import net.sf.logsupport.config.LogId;
import net.sf.logsupport.config.ProjectConfiguration;
import net.sf.logsupport.ui.util.AbstractEventListener;
import net.sf.logsupport.ui.util.BindFailed;
import net.sf.logsupport.ui.util.BindFailedException;
import net.sf.logsupport.ui.util.JComponentBinder;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

/**
 * Implements the editor GUI for log ids.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public class LogIdEditor implements Editor {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.ui.config.LogIdEditor");

	private JPanel editorPanel; //NOSONAR - field is binding

	private JTextField prefix; //NOSONAR - field is binding
	private JTextField trailer; //NOSONAR - field is binding
	private JLabel prefixErrorLabel; //NOSONAR - field is binding
	private JComboBox formatType; //NOSONAR - field is binding

	private JPanel customFormatPanel; //NOSONAR - field is binding
	private JTextField formatPattern; //NOSONAR - field is binding
	private JTextField matcherPattern; //NOSONAR - field is binding
	private JLabel matcherPatternErrorLabel; //NOSONAR - field is binding

	private JSpinner minValue; //NOSONAR - field is binding
	private JSpinner maxValue; //NOSONAR - field is binding

	private JLabel previewOutput;

	private final JComponentBinder<LogId> binder;
	private final LogId editableElement;
	private final ProjectConfiguration projectConfiguration; //NOSONAR - reserved

	private final PreviewUpdater previewUpdater = new PreviewUpdater();

	public LogIdEditor(ProjectConfiguration projectConfiguration, LogId editableElement) {
		this.projectConfiguration = projectConfiguration;
		this.editableElement = editableElement;

		// Configure format types
		for (LogId.Format format : LogId.Format.values())
			formatType.addItem(format.name());
		
		formatType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {
					binder.apply(LogIdEditor.this.editableElement);
					binder.reset(LogIdEditor.this.editableElement);
				} catch (BindFailedException e1) {
					// ignore
				}
			}
		});

		// Configure sequence related elements
		minValue.setModel(new SpinnerNumberModel(editableElement.getMinValue(), 0, Integer.MAX_VALUE, 100) {
			@Override
			public Comparable getMaximum() {
				return ((Number) maxValue.getValue()).intValue() - 1;
			}
		});
		maxValue.setModel(new SpinnerNumberModel(editableElement.getMaxValue(), 0, Integer.MAX_VALUE, 100) {
			@Override
			public Comparable getMinimum() {
				return (Comparable) minValue.getValue();
			}
		});

		AbstractEventListener detailsEnabler = new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				boolean enabled = formatType.getSelectedIndex() == LogId.Format.custom.ordinal();
				UIUtil.setEnabled(customFormatPanel, enabled, true);
				minValue.setEnabled(enabled);
				maxValue.setEnabled(enabled);
			}
		};
		formatType.addItemListener(detailsEnabler);
		detailsEnabler.eventOccurred(null);

		// Register listerns to render preview.
		prefix.addKeyListener(previewUpdater);
		trailer.addKeyListener(previewUpdater);
		formatType.addItemListener(previewUpdater);
		formatPattern.addKeyListener(previewUpdater);
		minValue.addChangeListener(previewUpdater);
		maxValue.addChangeListener(previewUpdater);

		InputValidator validator = new InputValidator();
		prefix.addKeyListener(validator);
		trailer.addKeyListener(validator);
		matcherPattern.addKeyListener(validator);

		binder = new JComponentBinder<LogId>(this, editableElement);
		reset();
	}

	public void apply() throws ConfigurationException {
		editableElement.setFormat(LogId.Format.values()[formatType.getSelectedIndex()]);
		try {
			binder.apply(editableElement);
		} catch (BindFailedException e) {
			throw new ConfigurationException(e.getMessage());
		}
	}

	public void reset() {
		formatType.setSelectedIndex(editableElement.getFormat().ordinal());
		try {
			binder.reset(editableElement);
		} catch (BindFailedException e) {
			LOG.error(e);
		}

		previewUpdater.eventOccurred(null);
	}

	public JComponent getEditorPanel() {
		return editorPanel;
	}

	class PreviewUpdater extends AbstractEventListener {
		@Override
		public void eventOccurred(EventObject e) {
			try {
				apply();
				String id = editableElement.getFormattedId(((Number) maxValue.getValue()).intValue());
				previewOutput.setText(id + "Log message, with upper bound id.");
			} catch (Throwable e1) {
				previewOutput.setText("Invalid settings!");
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	class InputValidator extends AbstractEventListener {
		InputValidator() {
			reset();
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			try {
				binder.apply(editableElement);
				reset();
			} catch (BindFailedException e1) {
				prefixErrorLabel.setVisible(containsComponents(e1, prefix, trailer));
				matcherPatternErrorLabel.setVisible(containsComponents(e1, matcherPattern));
			}
		}

		private void reset() {
			prefixErrorLabel.setVisible(false);
			matcherPatternErrorLabel.setVisible(false);
		}

		private boolean containsComponents(BindFailedException e, JComponent...components) {
			List<JComponent> c = Arrays.asList(components);
			for (BindFailed bindFailed : e) {
				if (c.contains(bindFailed.getComponent()))
					return true;
			}
			return false;
		}
	}
}
