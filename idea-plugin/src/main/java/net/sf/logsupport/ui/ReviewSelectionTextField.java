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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import net.sf.logsupport.ui.util.AbstractEventListener;
import net.sf.logsupport.ui.util.Dialogs;
import net.sf.logsupport.util.Codec;

import javax.swing.*;
import java.io.File;
import java.util.EventObject;

/**
 * Creates a text field that may be used to select report files for reading and writing them.
 *
 * @author Juergen_Kellerer, 2010-04-18
 * @version 1.0
 */
public class ReviewSelectionTextField extends TextFieldWithBrowseButton {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.ui.ReviewSelectionTextField");

	public static final Key<String> REVIEW_FILE = Key.create("LOG_SUPPORT_LOG_REVIEW_FILE");

	private Codec targetCodec;

	public ReviewSelectionTextField(final Project project, boolean isGeneratingReport) {

		String title = isGeneratingReport ?
				"Select file to write the review to" :
				"Select file to integrate";

		addBrowseFolderListener(title, title, project,
				new FileChooserDescriptor(true, isGeneratingReport, false, false, false, false) {
					@Override
					public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
						if (!file.isDirectory() && file.isInLocalFileSystem()) {
							try {
								File f = new File(file.getPath());
								if (targetCodec != null)
									return targetCodec.isSupported(f);

								for (Codec codec : Codec.SELECTOR.codecs()) {
									if (codec.isSupported(f))
										return true;
								}
							} catch (Throwable e) {
								LOG.warn("Failed to evaluate whether the file can be used for a log review.", e);
								return false;
							}
						}

						return super.isFileVisible(file, showHiddenFiles);
					}
				});

		// Propose file name, when a path was selected inside the picker.
		getTextField().getDocument().addDocumentListener(new AbstractEventListener() {
			@Override
			public void eventOccurred(EventObject e) {
				String path = getText();
				project.putUserData(REVIEW_FILE, path);

				if (targetCodec != null && !path.endsWith(File.separator) && new File(path).isDirectory()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							File path = new File(getText());
							if (targetCodec != null && path.isDirectory())
								setText(getText() + File.separatorChar + targetCodec.getDefaultFilename());
						}
					});
				}
			}
		});

		setText(project.getUserData(REVIEW_FILE));
	}

	public Codec getTargetCodec() {
		return targetCodec;
	}

	public void setTargetCodec(Codec targetCodec) {
		this.targetCodec = targetCodec;
	}

	public File getReviewFile() {
		File file = new File(getText());
		return !file.isDirectory() ? file : null;
	}

	public File getReviewFileForWriting() {
		File file = getReviewFile();
		if (file != null && file.exists() && !Dialogs.confirmOverwrite(file.getName()))
			return null;
		return file;
	}
}
