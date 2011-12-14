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
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.ui.util.AbstractEventListener;
import net.sf.logsupport.ui.util.Dialogs;
import net.sf.logsupport.util.Codec;
import net.sf.logsupport.util.LogPsiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Implements the dialog
 *
 * @author Juergen_Kellerer, 2010-04-18
 * @version 1.0
 */
public class GenerateLogReviewDialog extends AbstractLogLevelAwareDialog {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.ui.GenerateLogReviewDialog");

	static final Key<Comparator[]> SORT_ORDER = Key.create("LOG_SUPPORT_GENERATE_REVIEW_SORT_ORDER");

	static final Comparator[] SORT_COMPARATORS = {
			new AbstractMethodComparator("Package Name") {
				public int compare(PsiMethodCallExpression o1, PsiMethodCallExpression o2) {
					PsiClassOwner co1 = (PsiClassOwner) o1.getContainingFile();
					PsiClassOwner co2 = (PsiClassOwner) o2.getContainingFile();
					return co1.getPackageName().compareTo(co2.getPackageName());
				}
			},
			new AbstractMethodComparator("File") {
				public int compare(PsiMethodCallExpression o1, PsiMethodCallExpression o2) {
					VirtualFile v1 = o1.getContainingFile().getVirtualFile();
					VirtualFile v2 = o2.getContainingFile().getVirtualFile();

					int value = (v1 == null ? "" : v1.getUrl()).compareTo((v2 == null ? "" : v2.getUrl()));

					// If the calls are in the same file, we sort by the order inside the file.
					if (value == 0)
						value = Math.max(-1, Math.min(1, o1.getTextOffset() - o2.getTextOffset()));

					return value;
				}
			},
			new AbstractMethodComparator("Log Level") {
				public int compare(PsiMethodCallExpression o1, PsiMethodCallExpression o2) {
					LogLevel l1 = LogPsiUtil.findLogLevel(o1), l2 = LogPsiUtil.findLogLevel(o2);
					if (l1 == null)
						l1 = LogLevel.debug;
					if (l2 == null)
						l2 = LogLevel.debug;
					return l1.compareTo(l2);
				}
			},
			new AbstractMethodComparator("Log Message") {
				public int compare(PsiMethodCallExpression o1, PsiMethodCallExpression o2) {
					return o1.getText().compareTo(o2.getText());
				}
			},
	};

	private final ReviewSelectionTextField outputFile;
	private JComboBox outputFileFormatSelection;

	private JPanel outputOptions;
	private JPanel outputFileContainer;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JList outputSortOrder;

	List<PsiMethodCallExpression> reviewableCalls = new LinkedList<PsiMethodCallExpression>();

	AbstractEventListener okEnabledListener = new AbstractEventListener() {
		@Override
		public void eventOccurred(EventObject e) {
			setOKActionEnabled(!selectionPanel.getSelectedLevels().isEmpty());
		}
	};

	public GenerateLogReviewDialog(final Project project, List<VirtualFile> sources) {
		super(project, sources);

		setTitle("Generate Review");
		setOKButtonText("Generate");

		optionsPanel.add(BorderLayout.SOUTH, outputOptions);

		outputFile = new ReviewSelectionTextField(project, true);
		outputFileContainer.add(BorderLayout.CENTER, outputFile);


		// Configure the codecs.
		for (Codec codec : Codec.SELECTOR.codecs())
			outputFileFormatSelection.addItem(codec);

		outputFileFormatSelection.addItemListener(new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				outputFile.setTargetCodec((Codec) outputFileFormatSelection.getSelectedItem());
			}
		});
		outputFileFormatSelection.addItemListener(okEnabledListener);

		File reviewFile = outputFile.getReviewFile();
		if (reviewFile != null) {
			for (Codec codec : Codec.SELECTOR.codecs()) {
				try {
					if (codec.isSupported(reviewFile))
						outputFileFormatSelection.setSelectedItem(codec);
				} catch (IOException e) {
					// ignore.
				}
			}
		} else if (outputFileFormatSelection.getItemCount() > 0)
			outputFileFormatSelection.setSelectedIndex(0);


		// Configure sorting
		Comparator[] sortOrder = project.getUserData(SORT_ORDER);
		if (sortOrder == null)
			sortOrder = SORT_COMPARATORS;

		outputSortOrder.setModel(new DefaultComboBoxModel(sortOrder));
		outputSortOrder.getSelectionModel().addListSelectionListener(new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				moveUpButton.setEnabled(moveUpButton.getAction().isEnabled());
				moveDownButton.setEnabled(moveDownButton.getAction().isEnabled());
				updateCachedSortOrder();
			}

			private void updateCachedSortOrder() {
				ListModel model = outputSortOrder.getModel();
				Comparator[] comparators = new Comparator[model.getSize()];
				for (int i = 0; i < comparators.length; i++)
					comparators[i] = (Comparator) model.getElementAt(i);
				project.putUserData(SORT_ORDER, comparators);
			}
		});

		moveUpButton.setAction(new MoveSortOrderAction(true));
		moveDownButton.setAction(new MoveSortOrderAction(false));

		outputFile.getTextField().getDocument().addDocumentListener(okEnabledListener);
		okEnabledListener.eventOccurred(null);
	}

	@Override
	protected void setOKActionEnabled(boolean isEnabled) {
		if (isEnabled && outputFile != null) {
			File path = new File(outputFile.getText());
			if (!path.exists()) {
				File parent = path.getParentFile();
				isEnabled = parent != null && parent.isDirectory();
			} else
				isEnabled = path.isFile();

			if (isEnabled) {
				Codec selectedCodec = (Codec) outputFileFormatSelection.getSelectedItem();
				try {
					isEnabled = selectedCodec.isSupported(path);
				} catch (IOException e) {
					LOG.warn("Failed to validate whether the selected codec support writing to " + path, e);
					isEnabled = false;
				}
			}
		}

		super.setOKActionEnabled(isEnabled);
	}

	@Override
	protected String getOptionTitle() {
		return "Generation Options";
	}

	@Override
	protected void init() {
		super.init();

		wholeProject.setVisible(true);
		module.setVisible(true);
		decorateModuleButton();

		if (module.isEnabled())
			module.setSelected(true);
		else
			wholeProject.setSelected(true);
	}

	@NotNull
	@Override
	public Computable<List<PsiFile>> getReadOperation() {
		reviewableCalls.clear();

		return new DefaultReadOperation() {

			@Override
			protected boolean acceptFile(PsiFile file, List<PsiMethodCallExpression> loggerCalls) {
				for (PsiMethodCallExpression callExpression : loggerCalls) {
					if (!isLoggerCallInSelectedLevel(callExpression))
						continue;
					reviewableCalls.add(callExpression);
				}

				// We always return false, as we do not want to write any files.
				return false;
			}
		};
	}

	@NotNull
	@Override
	public Runnable getWriteOperation(@NotNull List<PsiFile> files) {
		final File reviewFile = outputFile.getReviewFileForWriting();

		if (reviewFile == null)
			return new EmptyRunnable();

		return new Runnable() {
			public void run() {
				// Check if we have something to do
				if (reviewableCalls.isEmpty()) {
					Dialogs.showInfoDialog(
							"Did not find any log messages inside this project.",
							"No log messages found.");
					return;
				}

				// Sorting the calls first
				Collections.sort(reviewableCalls, new Comparator<PsiMethodCallExpression>() {

					Comparator[] comparators = getProject().getUserData(SORT_ORDER);

					@SuppressWarnings("unchecked")
					public int compare(PsiMethodCallExpression o1, PsiMethodCallExpression o2) {
						int c = 0;
						if (comparators != null)
							for (Comparator comparator : comparators) {
								c = comparator.compare(o1, o2);
								if (c != 0)
									break;
							}
						return c;
					}
				});

				// Create the report.
				try {
					Codec codec = Codec.SELECTOR.select(reviewFile);
					if (codec != null)
						codec.encode(reviewableCalls, reviewFile);

					// Open the file afterwards, using the system default viewer.
					if (Desktop.isDesktopSupported() && reviewFile.exists())
						Desktop.getDesktop().open(reviewFile);
				} catch (IOException e) {
					LOG.error(String.format("Failed writing into the log review file '%s'", reviewFile), e);
					Dialogs.showErrorDialog(String.format(
							"Failed writing into the log review file '%s'", reviewFile),
							"Failed creating log review.");
				}
			}
		};
	}

	private class MoveSortOrderAction extends AbstractAction {

		boolean directionIsUp;

		protected MoveSortOrderAction(boolean directionIsUp) {
			super(directionIsUp ? "Move Up" : "Move Down");
			this.directionIsUp = directionIsUp;
		}

		@Override
		public boolean isEnabled() {
			int idx = outputSortOrder.getSelectedIndex();
			return idx != -1 && (directionIsUp ? idx > 0 : idx < outputSortOrder.getModel().getSize() - 1);
		}

		public void actionPerformed(ActionEvent e) {
			if (isEnabled()) {
				int idx = outputSortOrder.getSelectedIndex();
				MutableComboBoxModel mutableModel = (MutableComboBoxModel) outputSortOrder.getModel();
				Object selectedElement = mutableModel.getElementAt(idx);
				mutableModel.removeElementAt(idx);
				if (directionIsUp)
					mutableModel.insertElementAt(selectedElement, --idx);
				else
					mutableModel.insertElementAt(selectedElement, ++idx);
				outputSortOrder.setSelectedIndex(idx);
			}
		}
	}

	private abstract static class AbstractMethodComparator implements Comparator<PsiMethodCallExpression> {

		String name;

		private AbstractMethodComparator(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
