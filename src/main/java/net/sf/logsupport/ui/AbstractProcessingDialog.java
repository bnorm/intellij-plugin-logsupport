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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.VirtualFileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public abstract class AbstractProcessingDialog extends DialogWrapper {

	private JPanel centerPane;

	protected JRadioButton wholeProject;
	protected JRadioButton module;
	protected JRadioButton file;
	protected JRadioButton allFiles;
	protected JCheckBox recursion;

	protected JPanel optionsPanel;

	private final Project project;

	private Set<Module> moduleSelection;
	private List<VirtualFile> allFilesSelection;

	private final List<VirtualFile> sourceSelection;

	public AbstractProcessingDialog(Project project, List<VirtualFile> sources) {
		super(project, false);
		if (sources == null)
			sources = Collections.emptyList();

		this.project = project;
		sourceSelection = sources;

		allFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (recursion.isVisible())
					recursion.setEnabled(allFiles.isSelected());
			}
		});

		wholeProject.setVisible(sources.isEmpty());
		module.setVisible(false);
		recursion.setEnabled(false);

		setOKButtonText("Run");

		init();
	}

	/**
	 * Returns a callable that identifies all changeable files and return them as a list.
	 *
	 * @return a callable that identifies all changeable files and return them as a list.
	 */
	@NotNull
	public Computable<List<PsiFile>> getReadOperation() {
		return new DefaultReadOperation();
	}

	/**
	 * Returns a runnable that operates on the given files inside a write context.
	 *
	 * @param files the files to operate on.
	 * @return a runnable that operates on the given files inside a write context.
	 */
	@NotNull
	public abstract Runnable getWriteOperation(@NotNull List<PsiFile> files);

	public boolean isAllFiles() {
		return allFiles.isVisible() && allFiles.isEnabled() && allFiles.isSelected();
	}

	public boolean isRecursive() {
		return recursion.isVisible() && recursion.isEnabled() && recursion.isSelected();
	}

	public Project getProject() {
		return project;
	}

	public Set<Module> getModuleSelection() {
		return moduleSelection;
	}

	public List<VirtualFile> getSelection() {
		return isAllFiles() ? allFilesSelection : sourceSelection;
	}

	@Override
	protected JComponent createCenterPanel() {
		return centerPane;
	}

	@Override
	protected void init() {
		try {
			moduleSelection = new LinkedHashSet<Module>();
			if (sourceSelection.isEmpty()) {
				allFilesSelection = Collections.emptyList();
				allFiles.setVisible(false);
				file.setVisible(false);
			} else {
				allFilesSelection = VirtualFileUtil.toDirectories(sourceSelection, false);
				if (!allFilesSelection.isEmpty()) {
					decorateButton(allFiles, true, allFilesSelection);
					allFiles.setSelected(true);

					decorateButton(file, false, "");
				} else {
					allFilesSelection = VirtualFileUtil.toDirectories(sourceSelection, true);
					if (!allFilesSelection.isEmpty())
						decorateButton(allFiles, true, allFilesSelection);
					else
						allFiles.setVisible(false);

					decorateButton(file, true, sourceSelection);
					file.setSelected(true);
				}
			}

			ModuleManager manager = ModuleManager.getInstance(project);
			Module[] modules = manager.getModules();

			for (VirtualFile dir : allFilesSelection) {
				if (hasSubdirectories(project, dir))
					recursion.setEnabled(true);

				// Resolve what path belongs to what module.
				for (Module module : modules) {
					for (VirtualFile virtualFile : VirtualFileUtil.getSourceDirectories(module, true)) {
						if (dir.getUrl().startsWith(virtualFile.getUrl()))
							moduleSelection.add(module);
					}
				}
			}

			if (!recursion.isEnabled())
				recursion.setVisible(false);
		} finally {
			super.init();
		}
	}

	public static boolean hasSubdirectories(Project project, VirtualFile directory) {
		if (directory != null) {
			ProjectRootManager prm = ProjectRootManager.getInstance(project);
			ProjectFileIndex pfi = prm.getFileIndex();

			VirtualFile[] children = directory.getChildren();
			for (VirtualFile aChildren : children)
				if (aChildren.isDirectory() && !pfi.isIgnored(aChildren))
					return true;
		}

		return false;
	}

	protected void decorateModuleButton() {
		boolean enabled = !moduleSelection.isEmpty();
		if (enabled) {
			StringBuffer b = new StringBuffer();
			for (Module m : moduleSelection) {
				if (b.length() > 0)
					b.append(", ");
				b.append('"').append(m.getName()).append('"');
			}

			decorateButton(module, enabled, b.toString());
		} else
			decorateButton(module, enabled, "");
	}

	protected void decorateButton(AbstractButton button, boolean enabled, List<VirtualFile> files) {
		if (enabled) {
			decorateButton(button, enabled,
					"'" + files.get(0).getPresentableUrl() + "'" +
							(files.size() > 1 ? ", ..." : ""));
		} else
			decorateButton(button, enabled, "");
	}

	protected void decorateButton(AbstractButton button, boolean enabled, String descriptiveText) {
		button.setEnabled(enabled);

		if (enabled)
			button.setText(button.getText() + ' ' + descriptiveText);
	}

	protected class DefaultReadOperation implements Computable<List<PsiFile>> {

		boolean wholeProjectSelected = wholeProject.isSelected();
		boolean moduleSelected = module.isSelected();
		boolean allFiles = isAllFiles();
		boolean recursive = isRecursive();

		public List<PsiFile> compute() {
			List<VirtualFile> files;

			if (wholeProjectSelected) {
				files = VirtualFileUtil.toFiles(project, VirtualFileUtil.getSourceDirectories(project, true), true);
			} else if (moduleSelected) {
				files = new ArrayList<VirtualFile>();
				for (Module module : moduleSelection) {
					files.addAll(VirtualFileUtil.toFiles(project,
							VirtualFileUtil.getSourceDirectories(module, true), true));
				}
			} else if (allFiles) {
				files = VirtualFileUtil.toFiles(project, allFilesSelection, recursive);
			} else {
				files = VirtualFileUtil.toFiles(project, sourceSelection, false);
			}

			PsiManager manager = PsiManager.getInstance(project);
			List<PsiFile> sourceFiles = new ArrayList<PsiFile>(files.size());

			for (VirtualFile file : files) {
				if (!VirtualFileUtil.isSupportedFile(file))
					continue;

				PsiFile pf = manager.findFile(file);
				if (pf == null || !acceptFile(pf, LogPsiUtil.findSupportedLoggerCalls(pf)))
					continue;

				sourceFiles.add(pf);
			}
			return sourceFiles;
		}

		protected boolean acceptFile(PsiFile file, List<PsiMethodCallExpression> loggerCalls) {
			return !loggerCalls.isEmpty();
		}
	}
}
