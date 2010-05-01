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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.Spacer;
import net.sf.logsupport.L10N;
import net.sf.logsupport.ui.util.AbstractEventListener;
import net.sf.logsupport.util.LogMessageUtil;
import net.sf.logsupport.util.LogReviewCodec;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * Dialog to select the review file for the integration.
 *
 * @author Juergen_Kellerer, 2010-04-29
 * @version 1.0
 */
public class IntegrateLogReviewDialog extends DialogWrapper {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.ui.IntegrateLogReviewDialog");

	private static final Icon WARN = new ImageIcon(
			IntegrateLogReviewDialog.class.getResource("/icons/configuration-warning.png"));
	private static final Icon INFO = new ImageIcon(
			IntegrateLogReviewDialog.class.getResource("/icons/info.png"));

	private JPanel centerPanel = new JPanel(new BorderLayout());

	private ReviewSelectionTextField reviewFileLabel;

	private JLabel invalidFileLabel = new JLabel(
			L10N.message("IntegrateLogReview.invalidReview"), WARN, SwingConstants.LEADING);
	private JLabel contentInfoLabel = new JLabel("", INFO, SwingConstants.LEADING);

	private File parsedFile;
	private List<LogMessageUtil.LogMessage> reviewedMessages = Collections.emptyList();

	/**
	 * Creates a new one time dialog for the specified project.
	 *
	 * @param project the project to create the dialog for.
	 */
	public IntegrateLogReviewDialog(Project project) {
		super(project, false);

		setTitle(L10N.message("IntegrateLogReview.title"));

		reviewFileLabel = new ReviewSelectionTextField(project, false);
		AbstractEventListener listener = new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						parseReviewDocument();
						setOKActionEnabled(!reviewedMessages.isEmpty());
					}
				});
			}
		};
		reviewFileLabel.getTextField().getDocument().addDocumentListener(listener);
		listener.eventOccurred(null);

		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(280, 4));
		centerPanel.add(BorderLayout.CENTER, spacer);
		centerPanel.setBorder(BorderFactory.createTitledBorder(L10N.message("IntegrateLogReview.selectTitle")));

		centerPanel.add(BorderLayout.NORTH, reviewFileLabel);
		
		setOKButtonText(L10N.message("IntegrateLogReview.command"));
		init();
	}

	@Override
	protected JComponent createCenterPanel() {
		return centerPanel;
	}

	/**
	 * Returns the reviewed messages contained inside the selected report file.
	 *
	 * @return the reviewed messages contained inside the selected report file.
	 */
	@NotNull
	public List<LogMessageUtil.LogMessage> getReviewedMessages() {
		return reviewedMessages;
	}

	private void parseReviewDocument() {
		File file = reviewFileLabel.getReviewFile();
		if (file != null && file.isFile() && file.canRead()) {

			// Avoid that we double-parse the same file.
			if (parsedFile != null && parsedFile.equals(file))
				return;

			// Remove info labels.
			centerPanel.remove(invalidFileLabel);
			invalidFileLabel.setIcon(WARN);
			centerPanel.remove(contentInfoLabel);

			try {
				reviewedMessages = new LogReviewCodec(file).decode();
				parsedFile = file;

				contentInfoLabel.setText(L10N.message("IntegrateLogReview.contentInfo", reviewedMessages.size()));
				centerPanel.add(BorderLayout.SOUTH, contentInfoLabel);
			} catch (IOException e1) {
				LOG.warn("Failed to read log review, on the attempt to prepare a log integration.", e1);
				centerPanel.add(BorderLayout.SOUTH, invalidFileLabel);
			}

			centerPanel.invalidate();
			pack();
		}
	}
}
