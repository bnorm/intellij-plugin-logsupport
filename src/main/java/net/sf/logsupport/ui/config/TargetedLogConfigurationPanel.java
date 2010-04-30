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

import com.intellij.ide.DataManager;
import com.intellij.ide.util.scopeChooser.ScopeChooserConfigurable;
import com.intellij.openapi.options.newEditor.OptionsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.ColumnInfo;
import net.sf.logsupport.L10N;
import net.sf.logsupport.config.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements an editor for targeted (scopes) log configuration.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public class TargetedLogConfigurationPanel extends AbstractSortableTableEditor<TargetedLogConfiguration> {

	private final String useDefaultLabel = "<html><i>" +
			L10N.message("TargetedLogConfigurationPanel.noChange") + "</i></html>";

	private final HyperlinkLabel scopesLink =
			new HyperlinkLabel(L10N.message("TargetedLogConfigurationPanel.scopesLink"));

	private Project project;
	private ProjectLogConfigurations configurations;

	public TargetedLogConfigurationPanel(final Project project, final ProjectLogConfigurations configurations) {
		this.project = project;
		this.configurations = configurations;

		// Configure scopes link
		scopesLink.setVisible(!project.isDefault());
		scopesLink.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					final OptionsEditor optionsEditor = OptionsEditor.KEY.getData(DataManager.getInstance().getDataContext());
					if (optionsEditor != null)
						optionsEditor.select(ScopeChooserConfigurable.getInstance(project));
				}
			}
		});

		initPanel();
	}

	@Override
	protected ColumnInfo[] createColumns() {
		return new ColumnInfo[]{
				new AbstractComboBoxEditableColumn<TargetedLogConfiguration>(
						L10N.message("TargetedLogConfigurationPanel.scope")) {

					@Override
					protected List<String> getComboBoxItems() {
						return TargetedLogConfiguration.getAllScopeNames(project);
					}

					@Override
					public String valueOf(TargetedLogConfiguration configuration) {
						return configuration.getTargetScopeName();
					}

					@Override
					public void setValue(TargetedLogConfiguration targetedLogConfiguration, String value) {
						targetedLogConfiguration.setTargetScopeName(value);
					}
				},

				new AbstractComboBoxEditableColumn<TargetedLogConfiguration>(
						L10N.message("TargetedLogConfigurationPanel.logFramework")) {

					@Override
					protected List<String> getComboBoxItems() {
						List<String> frameworks = new ArrayList<String>();

						frameworks.add(useDefaultLabel);
						for (LogFramework framework : ApplicationConfiguration.getInstance().getFrameworks())
							frameworks.add(framework.getName());

						return frameworks;
					}

					@Override
					public String valueOf(TargetedLogConfiguration configuration) {
						return formatCellValue(configuration.getDefaultFrameworkName());
					}

					@Override
					public void setValue(TargetedLogConfiguration targetedLogConfiguration, String value) {
						targetedLogConfiguration.setDefaultFrameworkName(fromCellValue(value));
					}
				},

				new AbstractComboBoxEditableColumn<TargetedLogConfiguration>(
						L10N.message("TargetedLogConfigurationPanel.logId")) {

					@Override
					protected List<String> getComboBoxItems() {
						List<String> ids = new ArrayList<String>();

						ids.add(useDefaultLabel);
						for (LogId logId : ProjectConfiguration.getInstance(project).getLogIds())
							ids.add(logId.getName());

						return ids;
					}

					@Override
					public String valueOf(TargetedLogConfiguration configuration) {
						return formatCellValue(configuration.getLogIdName());
					}

					@Override
					public void setValue(TargetedLogConfiguration targetedLogConfiguration, String value) {
						targetedLogConfiguration.setLogIdName(fromCellValue(value));
					}
				},

				new AbstractBooleanEditableColumn<TargetedLogConfiguration>(
						L10N.message("TargetedLogConfigurationPanel.customize")) {

					@Override
					public Boolean valueOf(TargetedLogConfiguration o) {
						return o.isCustomizedSequence();
					}

					@Override
					public void setValue(final TargetedLogConfiguration o, Boolean value) {
						o.setCustomizedSequence(value);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								tableModel.fireTableStructureChanged();
								tableView.addSelection(o);
							}
						});
					}
				},

				new SequenceColumn(L10N.message("TargetedLogConfigurationPanel.from")) {

					@Override
					protected int intValueOf(TargetedLogConfiguration o) {
						return o.getLogIdFromValue();
					}

					@Override
					protected void setIntValue(TargetedLogConfiguration o, int value) {
						o.setLogIdFromValue(value);
					}
				},

				new SequenceColumn(L10N.message("TargetedLogConfigurationPanel.to")) {

					@Override
					protected int intValueOf(TargetedLogConfiguration o) {
						return o.getLogIdToValue();
					}

					@Override
					protected void setIntValue(TargetedLogConfiguration o, int value) {
						o.setLogIdToValue(value);
					}
				},

				new SequenceColumn(L10N.message("TargetedLogConfigurationPanel.increment")) {

					@Override
					protected int intValueOf(TargetedLogConfiguration o) {
						return o.getLogIdIncrement();
					}

					@Override
					protected void setIntValue(TargetedLogConfiguration o, int value) {
						o.setLogIdIncrement(value);
					}
				},
		};
	}

	@Override
	protected TargetedLogConfiguration createNewRow() {
		List<String> scopeNames = TargetedLogConfiguration.getAllScopeNames(project);
		return configurations.createTargetedLogConfiguration(scopeNames.isEmpty() ? "" : scopeNames.get(0));
	}

	@Override
	protected List<TargetedLogConfiguration> getItems() {
		return configurations.getTargetedLogConfigurations();
	}

	@Override
	protected void setItems(List<TargetedLogConfiguration> items) {
		configurations.setTargetedLogConfigurations(items);
	}

	private String formatCellValue(String value) {
		return value == null || value.equals(TargetedLogConfiguration.USE_DEFAULTS_NAME) ?
				useDefaultLabel : value;
	}

	private String fromCellValue(String value) {
		return value == null || value.equals(useDefaultLabel) ?
				TargetedLogConfiguration.USE_DEFAULTS_NAME : value;
	}

	@Override
	protected JComponent createMainComponent() {
		JComponent mainPanel = super.createMainComponent();
		mainPanel.add(BorderLayout.SOUTH, scopesLink);
		return mainPanel;
	}

	private static abstract class SequenceColumn extends
			AbstractFormattedEditableColumn<TargetedLogConfiguration, Long> {

		final TableCellRenderer renderer;

		private TargetedLogConfiguration currentRow;

		private SequenceColumn(String name) {
			super(name, NumberFormat.getIntegerInstance());

			final TableCellRenderer parentRenderer = super.getRenderer(null);
			renderer = new TableCellRenderer() {
				public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					Component c = parentRenderer.getTableCellRendererComponent(
							table, value, isSelected, hasFocus, row, column);
					boolean enabled = isCellEditable(currentRow);
					c.setEnabled(enabled);
					return c;
				}
			};
		}

		@Override
		public TableCellRenderer getRenderer(TargetedLogConfiguration e) {
			currentRow = e;
			return renderer;
		}

		@Override
		public boolean isCellEditable(TargetedLogConfiguration e) {
			return e.isCustomizedSequence();
		}

		@Override
		public final Long valueOf(TargetedLogConfiguration o) {
			return (long) intValueOf(o);
		}

		@Override
		public final void setValue(TargetedLogConfiguration o, Long value) {
			setIntValue(o, value.intValue());
		}

		protected abstract int intValueOf(TargetedLogConfiguration o);

		protected abstract void setIntValue(TargetedLogConfiguration o, int value);
	}
}
