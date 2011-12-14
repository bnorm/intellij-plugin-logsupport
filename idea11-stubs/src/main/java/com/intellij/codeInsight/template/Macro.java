package com.intellij.codeInsight.template;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Is a stub to the equally named class used from IntelliJ IDEA 11.
 */
public abstract class Macro {

	ExtensionPointName<Macro> EP_NAME;

	@NonNls
	public abstract String getName();

	public abstract String getDescription();

	public abstract String getPresentableName();

	@NonNls
	@NotNull
	public abstract String getDefaultValue();

	@Nullable
	public abstract Result calculateResult(@NotNull Expression[] expressions, ExpressionContext expressionContext);

	@Nullable
	public abstract Result calculateQuickResult(@NotNull Expression[] expressions, ExpressionContext expressionContext);

	@Nullable
	public abstract LookupElement[] calculateLookupItems(@NotNull Expression[] expressions, ExpressionContext expressionContext);
}
