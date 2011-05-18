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

package net.sf.logsupport.config;

import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.DefaultScopesProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import net.sf.logsupport.util.NumericLogIdGenerator;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Implements a log configuration that may override some defaults at package level.
 *
 * @author Juergen_Kellerer, 2010-04-12
 * @version 1.0
 */
public class TargetedLogConfiguration extends DefaultLogConfiguration {

	public static final String USE_DEFAULTS_NAME = "##use-defaults##";

	public static List<NamedScope> getAllNamedScopes(Project project) {
		Set<NamedScope> scopes = new LinkedHashSet<NamedScope>();
		for (NamedScopesHolder holder : NamedScopeManager.getAllNamedScopeHolders(project))
			scopes.addAll(Arrays.asList(holder.getScopes()));

		// Removing the "Problems" scope from the list..
		scopes.remove(DefaultScopesProvider.getInstance(project).getProblemsScope());

		return new ArrayList<NamedScope>(scopes);
	}

	public static List<String> getAllScopeNames(Project project) {
		List<NamedScope> scopes = getAllNamedScopes(project);
		List<String> names = new ArrayList<String>(scopes.size());
		for (NamedScope scope : scopes)
			names.add(scope.getName());
		return names;
	}

	private String targetScopeName;

	private DefaultLogConfiguration defaults;

	private transient WeakReference<NamedScope> scopeReference;

	{
		setLogIdName(USE_DEFAULTS_NAME);
		setDefaultFrameworkName(USE_DEFAULTS_NAME);
	}

	public void init(Project project, DefaultLogConfiguration defaults) {
		init(project);
		this.defaults = defaults;
	}

	public boolean isTargetForFile(PsiFile file) {
		NamedScopeManager manager = NamedScopeManager.getInstance(file.getProject());
		NamedScope scope = scopeReference == null ? null : scopeReference.get();

		if (scope == null && targetScopeName != null) {
			scope = NamedScopesHolder.getScope(file.getProject(), targetScopeName);
			scopeReference = new WeakReference<NamedScope>(scope);
		}

		if (scope != null) {
			PackageSet packageSet = scope.getValue();
			if (packageSet != null)
				return packageSet.contains(file, manager);
		}

		return false;
	}

	public String getTargetScopeName() {
		return targetScopeName;
	}

	public void setTargetScopeName(String targetScopeName) {
		this.targetScopeName = targetScopeName;
		scopeReference = null;
	}

	@NotNull
	@Override
	public LogFramework getDefaultLogFramework() {
		if (USE_DEFAULTS_NAME.equals(getDefaultFrameworkName()) && defaults != null)
			return defaults.getDefaultLogFramework();
		else
			return super.getDefaultLogFramework();
	}

	@Override
	public NumericLogIdGenerator getLogIdGenerator() {
		boolean useDefaultGenerator = defaults != null && !isCustomizedSequence() && (
				USE_DEFAULTS_NAME.equals(getLogIdName()) ||
						String.valueOf(defaults.getLogIdName()).equals(String.valueOf(getLogIdName())));

		if (useDefaultGenerator)
			return defaults.getLogIdGenerator();
		else
			return super.getLogIdGenerator();
	}

	@Override
	public boolean isForceUsingDefaultLogFramework() {
		if (defaults != null)
			return defaults.isForceUsingDefaultLogFramework();
		else
			return super.isForceUsingDefaultLogFramework();
	}

	@NotNull
	@Override
	public Set<LogLevel> getLogIdLevels() {
		if (defaults != null)
			return defaults.getLogIdLevels();
		else
			return super.getLogIdLevels();
	}

	@NotNull
	@Override
	public ConditionFormat getConditionFormat() {
		if (defaults != null)
			return defaults.getConditionFormat();
		else
			return super.getConditionFormat();
	}

	@NotNull
	@Override
	public Set<LogLevel> getConditionalLogLevels() {
		if (defaults != null)
			return defaults.getConditionalLogLevels();
		else
			return super.getConditionalLogLevels();
	}

	@Override
	public TargetedLogConfiguration clone() throws CloneNotSupportedException {
		return (TargetedLogConfiguration) super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TargetedLogConfiguration)) return false;
		if (!super.equals(o)) return false;

		TargetedLogConfiguration that = (TargetedLogConfiguration) o;

		if (defaults != null ? !defaults.equals(that.defaults) : that.defaults != null) return false;
		return !(targetScopeName != null ?
				!targetScopeName.equals(that.targetScopeName) :
				that.targetScopeName != null);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (targetScopeName != null ? targetScopeName.hashCode() : 0);
		result = 31 * result + (defaults != null ? defaults.hashCode() : 0);
		return result;
	}
}
