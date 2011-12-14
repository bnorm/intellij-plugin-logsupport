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

import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.util.ui.ColumnInfo;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-13
 * @version 1.0
 */
public abstract class AbstractBooleanEditableColumn<E> extends ColumnInfo<E, Boolean> {

	TableCellEditor editor = new DefaultCellEditor(new JCheckBox());
	TableCellRenderer renderer = new BooleanTableCellRenderer();

	protected AbstractBooleanEditableColumn(String name) {
		super(name);
	}

	@Override
	public boolean isCellEditable(E e) {
		return true;
	}

	@Override
	public TableCellEditor getEditor(E o) {
		return editor;
	}

	@Override
	public TableCellRenderer getRenderer(E e) {
		return renderer;
	}
}
