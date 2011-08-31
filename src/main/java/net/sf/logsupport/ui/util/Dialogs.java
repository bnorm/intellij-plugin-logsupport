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

import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import net.sf.logsupport.L10N;

import javax.swing.*;
import java.util.Collection;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2010-04-13
 * @version 1.0
 */
public class Dialogs {

	public static String askForInput(String title, String initialInput, final Collection<String> excludes) {
		return Messages.showInputDialog(
				title, title, Messages.getQuestionIcon(), initialInput,
				new InputValidator() {
					public boolean checkInput(String s) {
						return s.length() > 0 && (excludes == null || !excludes.contains(s));
					}

					public boolean canClose(String s) {
						return checkInput(s);
					}
				});
	}

	public static boolean confirmDelete(String what, String name) {
		if (name == null) {
			return Messages.showYesNoDialog(
					L10N.message("Dialogs.confirmDelete.confirmationTemplate.unnamed", what),
					L10N.message("Dialogs.confirmDelete.titleTemplate.unnamed", what),
					Messages.getWarningIcon()) == JOptionPane.YES_OPTION;
		} else {
			return Messages.showYesNoDialog(
					L10N.message("Dialogs.confirmDelete.confirmationTemplate", what, name),
					L10N.message("Dialogs.confirmDelete.titleTemplate", what, name),
					Messages.getWarningIcon()) == JOptionPane.YES_OPTION;
		}
	}

	public static boolean confirmOverwrite(String what) {
		return Messages.showYesNoDialog(
				L10N.message("Dialogs.confirmOverwrite.confirmationTemplate", what),
				L10N.message("Dialogs.confirmOverwrite.titleTemplate", what),
				Messages.getWarningIcon()) == JOptionPane.YES_OPTION;
	}

	/**
	 * Shows an info message without blocking the calling thread.
	 *
	 * @param message the message to show.
	 * @param title   the window title.
	 */
	public static void showInfoDialog(final String message, final String title) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Messages.showInfoMessage(message, title);
			}
		});
	}

	/**
	 * Shows an info message without blocking the calling thread.
	 *
	 * @param message the message to show.
	 * @param title   the window title.
	 */
	public static void showErrorDialog(final String message, final String title) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Messages.showErrorDialog(message, title);
			}
		});
	}

	private Dialogs() {
	}
}
