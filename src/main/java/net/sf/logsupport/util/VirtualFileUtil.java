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

package net.sf.logsupport.util;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.EditFileProvider;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Utility class to simplify working with the IDE's file tree.
 *
 * @author Juergen_Kellerer, 2010-04-16
 * @version 1.0
 */
public class VirtualFileUtil {

	final static List<FileType> SUPPORTED_TYPES = Arrays.asList((FileType) StdFileTypes.JAVA);

	/**
	 * Returns true of the given file is of a processable file type.
	 *
	 * @param file the file to check.
	 * @return true if the file can be processed by the plugin.
	 */
	public static boolean isSupportedFile(VirtualFile file) {
		return SUPPORTED_TYPES.contains(file.getFileType());
	}

	/**
	 * Converts the given list of files to files that the plugin can understand.
	 *
	 * @param files		   the files to convert.
	 * @param passDirectories specifies whether directories are passed to the resulting list.
	 * @return A reduced list containing only supported files and directories (optional).
	 */
	@NotNull
	public static List<VirtualFile> toSupportedFiles(@NotNull List<VirtualFile> files, boolean passDirectories) {
		List<VirtualFile> supportedFiles = new ArrayList<VirtualFile>(files.size());
		for (VirtualFile file : files) {
			if ((passDirectories && file.isDirectory()) || isSupportedFile(file))
				supportedFiles.add(file);
		}
		return supportedFiles;
	}

	/**
	 * Changes the given input into a list of files.
	 *
	 * @param project   The project to operate on.
	 * @param input	 The input list that may be a mix of directories and files.
	 * @param recursive specifies whether directories should be processed recursively.
	 * @return A list containing only files (any directory is resolved against it's children).
	 */
	@NotNull
	public static List<VirtualFile> toFiles(Project project, @NotNull List<VirtualFile> input, boolean recursive) {
		ProjectRootManager prm = ProjectRootManager.getInstance(project);
		ProjectFileIndex pfi = prm.getFileIndex();
		List<VirtualFile> files = new ArrayList<VirtualFile>(input.size());

		for (VirtualFile file : input) {
			if (pfi.isIgnored(file))
				continue;
			if (file.isDirectory()) {
				VirtualFile[] children = file.getChildren();
				if (children != null) {
					files.addAll(recursive ?
							toFiles(project, Arrays.asList(children), recursive) :
							Arrays.asList(children));
				}
			} else
				files.add(file);
		}

		return files;
	}

	/**
	 * Strips all files from the input list.
	 *
	 * @param input	  the input list to convert to directories.
	 * @param useParents specifies whether parent directories are read out
	 *                   of file entries and appended to the result list.
	 * @return A resulting list containing only directories.
	 */
	public static List<VirtualFile> toDirectories(@NotNull List<VirtualFile> input, boolean useParents) {
		List<VirtualFile> files = new ArrayList<VirtualFile>();
		for (VirtualFile file : input) {
			if (file.isDirectory()) {
				if (!files.contains(file))
					files.add(file);
			} else if (useParents) {
				VirtualFile dir = file.getParent();
				if (dir != null && !files.contains(dir))
					files.add(dir);
			}
		}
		return files;
	}

	/**
	 * Returns a list of currently selected files or directories.
	 *
	 * @param project the project to look for selections.
	 * @return A list of currently selected files or directories.
	 */
	@NotNull
	public static List<VirtualFile> getSelectedFiles(Project project) {
		final List<VirtualFile> selectedFiles = new ArrayList<VirtualFile>();
		final DataContext context = DataManager.getInstance().getDataContext();

		final Editor editor = PlatformDataKeys.EDITOR.getData(context);
		if (editor != null) {
			PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			if (file != null)
				selectedFiles.add(file.getVirtualFile());
		} else {
			VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(context);
			if (files != null)
				selectedFiles.addAll(Arrays.asList(files));
		}

		return selectedFiles;
	}

	/**
	 * Checks whether the given directory has sub-directories.
	 *
	 * @param project   the project to operate on.
	 * @param directory the directory to check.
	 * @return true if the given directory has sub-directories.
	 */
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

	/**
	 * Returns a list of all source directories in the given project.
	 *
	 * @param project	  the project to check.
	 * @param includeTests specifies whether test-source directories should be included.
	 * @return a list of all source directories in the given project.
	 */
	@NotNull
	public static List<VirtualFile> getSourceDirectories(Project project, boolean includeTests) {
		List<VirtualFile> sources = new ArrayList<VirtualFile>();
		ModuleManager moduleManager = ModuleManager.getInstance(project);
		for (Module module : moduleManager.getModules())
			sources.addAll(getSourceDirectories(module, includeTests));

		return sources;
	}

	/**
	 * Returns a list of all source directories in the given module.
	 *
	 * @param module	   the module to check.
	 * @param includeTests specifies whether test-source directories should be included.
	 * @return a list of all source directories in the given module.
	 */
	@NotNull
	public static List<VirtualFile> getSourceDirectories(Module module, boolean includeTests) {
		ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

		List<VirtualFile> sourceFiles;
		VirtualFile[] sources = rootManager.getSourceRoots();

		if (includeTests)
			sourceFiles = Arrays.asList(sources);
		else {
			sourceFiles = new ArrayList<VirtualFile>();
			ModuleFileIndex fileIndex = rootManager.getFileIndex();
			for (VirtualFile source : sources) {
				if (fileIndex.isInTestSourceContent(source))
					continue;
				sourceFiles.add(source);
			}
		}

		return sourceFiles;
	}

	private VirtualFileUtil() {
	}
}
