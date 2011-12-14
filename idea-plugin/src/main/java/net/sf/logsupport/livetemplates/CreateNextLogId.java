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

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.psi.PsiFile;
import net.sf.logsupport.config.LogConfiguration;
import net.sf.logsupport.config.LogLevel;
import net.sf.logsupport.util.NumericLogIdGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the next log ID and returns it if logIds are enabled.
 *
 * @author Juergen_Kellerer, 2010-04-04
 * @version 1.0
 */
public class CreateNextLogId extends AbstractMacro {

	private static String lastIdResultKey;
	private static Result lastIdResult;

	public CreateNextLogId() {
		super("createNextLogId");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return getName() + "(LogLevel)";
	}

	/**
	 * {@inheritDoc}
	 */
	public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {

		Result result = new TextResult("");

		PsiFile file = getPsiFile(context);
		if (file != null && expressions.length == 1) {
			LogLevel level = parseLogLevel(expressions[0], context);
			LogConfiguration config = LogConfiguration.getInstance(file);

			if (config.isUseLogIds() && config.getLogIdLevels().contains(level)) {
				String key = file.getName() + "-" + context.getEditor().getCaretModel().getLogicalPosition().line;
				if (key.equals(lastIdResultKey))
					result = lastIdResult;
				else {
					NumericLogIdGenerator generator = config.getLogIdGenerator();
					if (generator != null) {
						result = lastIdResult = new TextResult(generator.nextId());
						lastIdResultKey = key;
					}
				}
			}
		}

		return result;
	}
}
