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
import com.intellij.ui.PanelWithButtons;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import net.sf.logsupport.ui.util.Dialogs;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Defines a sortable table editor component.
 *
 * @author Juergen_Kellerer, 2010-04-13
 * @version 1.0
 */
public abstract class AbstractSortableTableEditor<E> extends PanelWithButtons implements Editor {

	static <E> int indexOfIdentity(Collection<E> collection, E item) {
		int i = 0;
		for (E e : collection) {
			if (e == item)
				return i;
			i++;
		}
		return -1;
	}

	protected final ListTableModel<E> tableModel;
	protected final TableView<E> tableView;

	protected JButton[] buttons = new JButton[0];

	protected AbstractSortableTableEditor() {
		tableModel = new ListTableModel<E>(createColumns());
		tableView = new TableView<E>(tableModel);
	}

	protected abstract ColumnInfo[] createColumns();

	protected abstract E createNewRow();

	protected abstract List<E> getItems();

	protected abstract void setItems(List<E> items);

	@Override
	protected void initPanel() {
		tableView.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				for (JButton button : buttons)
					button.setEnabled(button.getAction().isEnabled());
			}
		});

		try {
			reset();
		} finally {
			super.initPanel();
		}
	}

	public void apply() throws ConfigurationException {
		setItems(tableModel.getItems());
	}

	public void reset() {
		tableModel.setItems(getItems());
	}

	public JComponent getEditorPanel() {
		return this;
	}

	@Override
	protected String getLabelText() {
		return null;
	}

	@Override
	protected final JButton[] createButtons() {
		if (buttons.length == 0) {
			buttons = new JButton[]{
					new JButton(new AddAction()),
					new JButton(new MoveAction(true)),
					new JButton(new MoveAction(false)),
					new JButton(new RemoveAction()),
			};
		}
		return buttons;
	}

	@Override
	protected JComponent createMainComponent() {
		tableModel.setSortable(false);

		JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
		tableView.setPreferredScrollableViewportSize(new Dimension(450, 200));
		mainPanel.add(BorderLayout.CENTER, new JScrollPane(tableView));
		return mainPanel;
	}

	public class AddAction extends AbstractAction {
		public AddAction() {
			super("Add");
		}

		public void actionPerformed(ActionEvent e) {
			List<E> rows = new ArrayList<E>(tableModel.getItems());
			rows.add(createNewRow());
			tableModel.setItems(rows);
		}
	}

	public class RemoveAction extends AbstractAction {
		public RemoveAction() {
			super("Remove");
		}

		@Override
		public boolean isEnabled() {
			return !tableView.getSelection().isEmpty();
		}

		public void actionPerformed(ActionEvent e) {
			if (!Dialogs.confirmDelete("selected overrides", null))
				return;

			List<E> items = new ArrayList<E>(tableModel.getItems());
			for (E configuration : tableView.getSelection()) {
				int idx = indexOfIdentity(items, configuration);
				if (idx != -1)
					items.remove(idx);
			}

			tableModel.setItems(items);
		}
	}

	public class MoveAction extends AbstractAction {

		private boolean directionIsUp;

		public MoveAction(boolean directionIsUp) {
			super(directionIsUp ? "Move Up" : "Move Down");
			this.directionIsUp = directionIsUp;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			Object[] items = tableView.getSelection().toArray();
			if (items.length > 0) {
				if (directionIsUp) {
					return indexOfIdentity(tableModel.getItems(), (E) items[0]) > 0;
				} else {
					return indexOfIdentity(tableModel.getItems(),
							(E) items[items.length - 1]) < tableModel.getRowCount() - 1;
				}
			}
			return false;
		}

		public void actionPerformed(ActionEvent e) {
			if (isEnabled()) {
				List<E> selectedItems = new ArrayList<E>(tableView.getSelection());
				List<E> items = new ArrayList<E>(tableModel.getItems());

				for (E selectedItem : selectedItems) {
					int idx = indexOfIdentity(items, selectedItem);
					if (idx != -1) {
						items.remove(idx);
						items.add(idx + (directionIsUp ? -1 : 1), selectedItem);
					}
				}

				// Apply changes
				tableModel.setItems(items);

				// Restore selection
				ListSelectionModel model = tableView.getSelectionModel();
				model.setValueIsAdjusting(true);
				try {
					for (E item : selectedItems) {
						int idx = indexOfIdentity(items, item);
						if (idx != -1)
							model.addSelectionInterval(idx, idx);
					}
				} finally {
					model.setValueIsAdjusting(false);
				}
			}
		}
	}
}

