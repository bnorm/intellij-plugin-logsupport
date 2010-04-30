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
import com.intellij.util.ui.ComboBoxCellEditor;

import javax.swing.table.TableCellEditor;
import java.util.List;

/**
* TODO: Create Description.
*
* @author Juergen_Kellerer, 2010-04-12
* @version 1.0
*/
public abstract class AbstractComboBoxEditableColumn<Item> extends ColumnInfo<Item, String> {

	protected TableCellEditor cellEditor = new ComboBoxCellEditor() {
		@Override
		protected List<String> getComboBoxItems() {
			return AbstractComboBoxEditableColumn.this.getComboBoxItems();
		}
	};

	protected AbstractComboBoxEditableColumn(String name) {
		super(name);
	}

	protected abstract List<String> getComboBoxItems();

	@Override
	public boolean isCellEditable(Item item) {
		return true;
	}

	@Override
	public TableCellEditor getEditor(Item o) {
		return cellEditor;
	}
}
