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

package net.sf.logsupport.ui;

import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.ui.util.GridLayoutFacade;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * Creates a panel of selectable log levels.
 *
 * @author Juergen_Kellerer, 2010-04-14
 * @version 1.0
 */
public class LogLevelSelectionPanel extends JPanel implements ItemListener {

	private final DefaultButtonModel model = new DefaultButtonModel();
	private final Map<LogLevel, JCheckBox> levelBoxes = new HashMap<LogLevel, JCheckBox>();

	public LogLevelSelectionPanel(String title, int columns) {
		this(columns);
		setBorder(BorderFactory.createTitledBorder(title));
	}

	public LogLevelSelectionPanel(int columns) {
		super(new GridBagLayout());

		GridLayoutFacade gridLayout = new GridLayoutFacade(this, columns);
		for (LogLevel level : LogLevel.values()) {
			JCheckBox c = new JCheckBox(level.name().toUpperCase());
			gridLayout.add(c);
			levelBoxes.put(level, c);
			c.addItemListener(this);
		}
	}

	public Set<LogLevel> getSelectedLevels() {
		Set<LogLevel> logLevels = new HashSet<LogLevel>();

		for (Map.Entry<LogLevel, JCheckBox> entry : levelBoxes.entrySet()) {
			if (entry.getValue().isSelected() && entry.getValue().isEnabled())
				logLevels.add(entry.getKey());
		}

		return logLevels;
	}

	public void setSelectedLevels(Set<LogLevel> selectedLevels) {
		if (selectedLevels == null)
			selectedLevels = Collections.emptySet();
		for (Map.Entry<LogLevel, JCheckBox> entry : levelBoxes.entrySet())
			entry.getValue().setSelected(selectedLevels.contains(entry.getKey()));
	}

	public void setEnabledLevels(Set<LogLevel> enabledLevels) {
		if (enabledLevels == null)
			enabledLevels = Collections.emptySet();
		for (Map.Entry<LogLevel, JCheckBox> entry : levelBoxes.entrySet()) {
			boolean enabled = enabledLevels.contains(entry.getKey());
			entry.getValue().setEnabled(enabled);
			if (!enabled)
				entry.getValue().setSelected(false);
		}
	}

	public boolean isAllSelected() {
		for (JCheckBox checkBox : levelBoxes.values()) {
			if (!checkBox.isSelected() || !checkBox.isEnabled())
				return false;
		}
		return true;
	}

	public boolean isLevelSelected(LogLevel level) {
		for (Map.Entry<LogLevel, JCheckBox> entry : levelBoxes.entrySet()) {
			if (entry.getKey() == level)
				return entry.getValue().isSelected() && entry.getValue().isEnabled();
		}
		return false;
	}

	/**
	 * Adds a <code>ChangeListener</code> to the model.
	 *
	 * @param l the okEnabledListener to add
	 */
	public void addChangeListener(ChangeListener l) {
		model.addChangeListener(l);
	}

	/**
	 * Removes a <code>ChangeListener</code> from the model.
	 *
	 * @param l the okEnabledListener to remove
	 */
	public void removeChangeListener(ChangeListener l) {
		model.removeChangeListener(l);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void itemStateChanged(ItemEvent e) {
		ChangeEvent event = new ChangeEvent(e.getSource());
		for (ChangeListener listener : model.getChangeListeners())
			listener.stateChanged(event);
	}
}
