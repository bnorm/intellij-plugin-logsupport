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

package net.sf.logsupport.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * Is a small facade to simplify working with gridbaglayout (somewhat similar to JSF).
 *
 * @author Juergen_Kellerer, 2010-04-05
 * @version 1.0
 */
public class GridLayoutFacade {

	private int column, columnCount;
	private int horizontalSpacing = 2, verticalSpacing = 2;

	private final JComponent targetComponent;
	private final GridBagConstraints constraints = new GridBagConstraints();

	public GridLayoutFacade(JComponent targetComponent, int columns) {
		this.columnCount = columns;
		this.targetComponent = targetComponent;

		constraints.gridx = constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(0, 0, 0, 0);
	}

	public GridBagConstraints getConstraints() {
		return constraints;
	}

	public GridLayoutFacade(JComponent targetComponent, int columns,
							int startRow, int horizontalSpacing, int verticalSpacing) {
		this(targetComponent, columns);
		constraints.gridy = startRow;
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
	}

	public void add(JComponent component) {
		add(component, 0, 1);
	}

	public void add(JComponent component, double weight, int colspan) {
		constraints.insets.bottom = verticalSpacing;
		if (column == columnCount - 1)
			constraints.insets.right = 0;
		else
			constraints.insets.right = horizontalSpacing;

		constraints.gridx = column;
		constraints.weightx = weight;

		targetComponent.add(component, constraints);

		column += colspan;
		if (column >= columnCount) {
			column = constraints.gridx = 0;
			constraints.gridy++;
		}
	}
}
