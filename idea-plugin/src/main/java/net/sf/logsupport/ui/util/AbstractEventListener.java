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

import javax.swing.event.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * Is a small helper class to reduce code sizes and simplify the implementation of listeners.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public abstract class AbstractEventListener implements ActionListener, ItemListener,
		ChangeListener, KeyListener, MouseListener, DocumentListener,
		ListDataListener, ListSelectionListener {

	public void itemStateChanged(ItemEvent e) {
		eventOccurred(e);
	}

	public void actionPerformed(ActionEvent e) {
		eventOccurred(e);
	}

	public void stateChanged(ChangeEvent e) {
		eventOccurred(e);
	}

	public void keyTyped(KeyEvent e) {
		eventOccurred(e);
	}

	public void mouseClicked(MouseEvent e) {
		eventOccurred(e);
	}

	public void keyPressed(KeyEvent e) {
		eventOccurred(e);
	}

	public void mousePressed(MouseEvent e) {
		eventOccurred(e);
	}

	public void keyReleased(KeyEvent e) {
		eventOccurred(e);
	}

	public void mouseReleased(MouseEvent e) {
		eventOccurred(e);
	}

	public void mouseEntered(MouseEvent e) {
		eventOccurred(e);
	}

	public void mouseExited(MouseEvent e) {
		eventOccurred(e);
	}

	public void insertUpdate(DocumentEvent e) {
		eventOccurred(new EventObject(e));
	}

	public void removeUpdate(DocumentEvent e) {
		eventOccurred(new EventObject(e));
	}

	public void changedUpdate(DocumentEvent e) {
		eventOccurred(new EventObject(e));
	}

	public void intervalAdded(ListDataEvent e) {
		eventOccurred(e);
	}

	public void intervalRemoved(ListDataEvent e) {
		eventOccurred(e);
	}

	public void contentsChanged(ListDataEvent e) {
		eventOccurred(e);
	}

	public void valueChanged(ListSelectionEvent e) {
		eventOccurred(e);
	}

	public void eventOccurred(EventObject e) {
	}
}
