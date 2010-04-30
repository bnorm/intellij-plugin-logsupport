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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Trivial dialog to select the review file for the integration.
 *
 * @author Juergen_Kellerer, 2010-04-29
 * @version 1.0
 */
public class IntegrateLogReviewDialog extends DialogWrapper {

	JPanel centerPanel = new JPanel(new BorderLayout());

	JLabel reviewLabel = new JLabel("Select review to integrate:");
	ReviewSelectionTextField reviewTextField;

	public IntegrateLogReviewDialog(Project project) {
		super(project, false);

		setTitle("Integrate Log Review");

		reviewTextField = new ReviewSelectionTextField(project, false);
		reviewTextField.setPreferredSize(new Dimension(280, reviewTextField.getPreferredSize().height));

		centerPanel.add(BorderLayout.LINE_START, reviewLabel);
		centerPanel.add(BorderLayout.LINE_END, reviewTextField);

		setOKButtonText("Integrate");
		init();
	}

	@Override
	protected JComponent createCenterPanel() {
		return centerPanel;
	}

	public File getReviewFile() {
		return reviewTextField.getReviewFile();
	}
}
