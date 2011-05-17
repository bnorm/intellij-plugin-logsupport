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

package net.sf.logsupport.livetemplates;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateOptionalProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.L10N;
import net.sf.logsupport.intentions.AddLogIfConditionIntention;
import net.sf.logsupport.util.LogPsiUtil;
import org.jetbrains.annotations.Nls;

/**
 * Post-Processes the logsupport templates, when needed to satisfy the correct timing.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class TemplatePostProcessor implements TemplateOptionalProcessor {

	private static final Logger log = Logger.getInstance("#net.sf.logsupport.livetemplates.TemplatePostProcessor");

	public static final Key<Runnable> PENDING_RUNNABLE = Key.create("LOG_SUPPORT_PENDING_RUNNABLE");

	public static void schedule(PsiFile file, final Runnable runnable, boolean replace) {
		final Runnable existing = file.getUserData(PENDING_RUNNABLE);
		if (!replace && existing != null) {
			file.putUserData(PENDING_RUNNABLE, new Runnable() {
				public void run() {
					existing.run();
					runnable.run();
				}
			});
		} else
			file.putUserData(PENDING_RUNNABLE, runnable);
	}

	AddLogIfConditionIntention ifConditionIntention = new AddLogIfConditionIntention();

	/**
	 * {@inheritDoc}
	 */
	public void processText(Project project, Template template,
							Document document, RangeMarker rangeMarker, Editor editor) {
		PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
		if (file != null) {
			// Check whether we need to add a if condition or add a log id.
			PsiMethodCallExpression expression = LogPsiUtil.findSupportedMethodCallExpression(editor, file);
			if (expression != null)
				ifConditionIntention.invoke(expression);

			// Process pending runnables
			Runnable runnable = file.getUserData(PENDING_RUNNABLE);
			if (runnable != null) {
				try {
					runnable.run();
				} catch (Throwable t) {
					log.error(t);
				} finally {
					file.putUserData(PENDING_RUNNABLE, null);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Nls
	public String getOptionName() {
		return L10N.message("LoggerFactoryProcessor.optionName");
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled(Template template) {
		String id;
		try {
			id = String.valueOf(template.getId());
		} catch (IncompatibleClassChangeError e) {
			try {
				Class<?> cls = template.getClass();
				id = String.valueOf(cls.getMethod("getId").invoke(template));
			} catch (Exception ee) {				
				log.error("Failed to retrieve the template id of '" + template +
						"', disabling template post-processor here.", e);
				return false;
			}
		}

		return id.startsWith("logsupport-");
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(Template template, boolean b) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isVisible(Template template) {
		return false;
	}
}
