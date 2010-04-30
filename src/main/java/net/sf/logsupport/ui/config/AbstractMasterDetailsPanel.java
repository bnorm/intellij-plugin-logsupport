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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.MasterDetailsStateService;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Conditions;
import com.intellij.util.Icons;
import net.sf.logsupport.L10N;
import net.sf.logsupport.LogSupportComponent;
import net.sf.logsupport.LogSupportProjectComponent;
import net.sf.logsupport.ui.util.Dialogs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * Is an abstract implementation of a common MasterDetailsComponent to
 * simplify it's usage for this project.
 *
 * @author Juergen_Kellerer, 2010-04-11
 * @version 1.0
 */
public abstract class AbstractMasterDetailsPanel<E> extends MasterDetailsComponent {

	protected final LogSupportComponent logSupport;
	protected final LogSupportProjectComponent projectComponent;

	private E lastNewElement;

	protected AbstractMasterDetailsPanel(@NotNull LogSupportComponent logSupport,
										 LogSupportProjectComponent projectComponent) {
		this.logSupport = logSupport;
		this.projectComponent = projectComponent;

		if (projectComponent != null) {
			try {
				MasterDetailsStateService service = projectComponent.getProject().
						getComponent(MasterDetailsStateService.class);
				service.register(getId(), this);
			} catch (Throwable t) {
				// ignore
			}
		}

		initTree();
	}

	protected abstract String getId();

	protected abstract NamedConfigurable<E> createConfigurable(E source, boolean isNewNode);

	protected abstract E createNewElement(String name);

	protected abstract E cloneElement(E source);

	protected abstract String getElementName(E element);

	protected abstract void setElementName(E element, String name);

	protected abstract List<E> getElementsFromBackingStore();

	protected abstract String getElementDescriptiveName();

	protected String getAskForNewNameMessage(String initialName) {
		return L10N.message("AbstractMasterDetailsPanel.askFroNewNameTemplate", getElementDescriptiveName());
	}

	protected String getEmptySelectionMessage() {
		return L10N.message("AbstractMasterDetailsPanel.emptySelectionMessageTemplate", getElementDescriptiveName());
	}

	protected String getAddActionDescription() {
		return L10N.message("AbstractMasterDetailsPanel.AddAction.descriptionTemplate", getElementDescriptiveName());
	}

	protected String getCopyActionDescription() {
		return L10N.message("AbstractMasterDetailsPanel.CopyAction.descriptionTemplate", getElementDescriptiveName());
	}

	@NotNull
	public LogSupportComponent getApplication() {
		return logSupport;
	}

	public LogSupportProjectComponent getProjectComponent() {
		return projectComponent;
	}

	@Override
	protected String getEmptySelectionString() {
		return getEmptySelectionMessage();
	}

	public Icon getIcon() {
		return null;
	}

	@Override
	protected ArrayList<AnAction> createActions(boolean fromPopup) {
		ArrayList<AnAction> actions = new ArrayList<AnAction>();
		actions.add(new AddAction());
		actions.add(new DeleteAction());
		actions.add(new CopyAction());
		return actions;
	}

	@Override
	protected void processRemovedItems() {
		Set<String> remainingNames = getNamesInPanel();
		List<E> elements = getElementsFromBackingStore();

		for (Iterator<E> i = elements.iterator(); i.hasNext();) {
			E element = i.next();
			if (!remainingNames.contains(getElementName(element)))
				i.remove();
		}
	}

	@Override
	protected boolean wasObjectStored(Object o) {
		return getElementsFromBackingStore().contains(o);
	}

	@Nullable
	protected String askForNewName(String initialName) {
		String title = getAskForNewNameMessage(initialName);
		return Dialogs.askForInput(title, initialName, getNamesInPanel());
	}

	protected synchronized Set<String> getNamesInPanel() {
		Set<String> names = new HashSet<String>();
		for (int i = 0, len = myRoot.getChildCount(); i < len; i++)
			names.add(((MyNode) myRoot.getChildAt(i)).getDisplayName());
		return names;
	}

	protected synchronized void addNode(E element, boolean isNewNode) {
		MyNode node = new MyNode(createConfigurable(element, isNewNode));
		addNode(node, myRoot);
		if (isNewNode) {
			selectNodeInTree(node);
			lastNewElement = element;
		}
	}

	protected synchronized void reloadTree() {
		myRoot.removeAllChildren();
		for (E element : getElementsFromBackingStore())
			addNode(element, false);
	}

	@Override
	public void reset() {
		super.reset();
		reloadTree();
	}

	protected class AddAction extends AnAction {
		private AddAction() {
			super(L10N.message("AbstractMasterDetailsPanel.AddAction.name"), getAddActionDescription(), Icons.ADD_ICON);
		}

		@Override
		public void actionPerformed(AnActionEvent anActionEvent) {
			String name = askForNewName("");
			if (name != null)
				addNode(createNewElement(name), true);
		}
	}

	protected class DeleteAction extends MyDeleteAction {

		public DeleteAction() {
			super(Conditions.TRUE);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(AnActionEvent e) {
			E selected = (E) getSelectedObject();
			if (selected != null && (selected == lastNewElement ||
					Dialogs.confirmDelete(getElementDescriptiveName(), getElementName(selected)))) {
				super.actionPerformed(e);
			}
		}
	}

	protected class CopyAction extends AnAction {
		private CopyAction() {
			super(L10N.message("AbstractMasterDetailsPanel.CopyAction.name"), getCopyActionDescription(), COPY_ICON);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(AnActionEvent anActionEvent) {
			E selected = (E) getSelectedObject();
			if (selected == null)
				return;
			String name = askForNewName(getElementName(selected));
			if (name != null) {
				E copy = cloneElement(selected);
				setElementName(copy, name);
				addNode(copy, true);
			}
		}

		public void update(AnActionEvent event) {
			super.update(event);
			event.getPresentation().setEnabled(getSelectedObject() != null);
		}
	}
}

