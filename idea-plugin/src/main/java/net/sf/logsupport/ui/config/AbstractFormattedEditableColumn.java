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

import com.intellij.util.ui.ColumnInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.Format;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-13
 * @version 1.0
 */
public abstract class AbstractFormattedEditableColumn<Item, Aspect> extends ColumnInfo<Item, Aspect> {

	Format format;

	final TableCellEditor editor;
	final TableCellRenderer renderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table,
													   Object value, boolean isSelected,
													   boolean hasFocus, int row, int column) {
			value = format.format(value);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	};

	protected AbstractFormattedEditableColumn(String name, Format format) {
		super(name);
		this.format = format;

		final JFormattedTextField formattedTextField = new JFormattedTextField(format);
		editor = new DefaultCellEditor(formattedTextField) {
			{
				delegate = new EditorDelegate() {
					public void setValue(Object value) {
						formattedTextField.setValue(value);
					}

					public Object getCellEditorValue() {
						return formattedTextField.getValue();
					}
				};
			}
		};
	}

	@Override
	public boolean isCellEditable(Item e) {
		return true;
	}

	@Override
	public TableCellEditor getEditor(Item o) {
		return editor;
	}

	@Override
	public TableCellRenderer getRenderer(Item e) {
		return renderer;
	}
}