package net.sf.logsupport.livetemplates;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.impl.JavaTemplateUtil;
import com.intellij.codeInsight.template.macro.MacroUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import net.sf.logsupport.util.LogPsiElementFactory;
import net.sf.logsupport.util.LogPsiUtil;
import net.sf.logsupport.util.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static net.sf.logsupport.util.ReflectionUtil.invoke;

/**
 * TODO
 *
 * @author juergen kellerer, 2011-05-28
 */
public abstract class AbstractResolveMacro extends AbstractMacro {

	public AbstractResolveMacro(String name) {
		super(name);
	}

	/**
	 * Converts the given elements to lookup elements used in the code insight.
	 *
	 * @param variables the variables to convert.
	 * @return the lookup elements to show in the drop down box.
	 */
	@Nullable
	protected LookupElement[] convertToLookupItems(@Nullable PsiElement[] variables) {
		if (variables == null)
			return null;

		// No generics, manual array creation, and reflection to support builds in IDEA 8 and 9 and 10.x

		Set set = new LinkedHashSet();
		for (PsiElement variable : variables)
			invoke(JavaTemplateUtil.class, "addElementLookupItem", set, variable);

		int i = 0;
		LookupElement[] elements = new LookupElement[set.size()];
		for (Object o : set) elements[i++] = (LookupElement) o;
		return elements;
	}

	/**
	 * Helper method that resolves all variables inside the given file that are reachable from
	 * the given expression context.
	 *
	 * @param stringTypes the types to look after (fq java names).
	 * @param file		the file to look in.
	 * @param context	 the expression context describing the scope.
	 * @return an array of resolved variables or an empty array of nothing was resolved. 'null' on an error.
	 */
	@Nullable
	protected PsiElement[] resolveVariables(Set<String> stringTypes, PsiFile file, ExpressionContext context) {
		try {
			PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
			PsiElement place = getPlace(file, context);

			// Resolving the types
			Set<PsiType> types = new LinkedHashSet<PsiType>(stringTypes.size());
			LogPsiElementFactory factory = LogPsiUtil.getFactory(file);
			for (String type : stringTypes)
				types.add(factory.createTypeFromText(type, place.getContext()));


			ArrayList<PsiElement> elementsInScope = new ArrayList<PsiElement>();
			PsiVariable[] variables = MacroUtil.getVariablesVisibleAt(place, "");

			for (PsiVariable var : variables) {
				if (var instanceof PsiLocalVariable) {
					TextRange range = var.getNameIdentifier().getTextRange();
					if (range != null && range.contains(context.getStartOffset())) {
						continue;
					}
				}

				for (PsiType type : types)
					if (type == null || type.isAssignableFrom(var.getType())) {
						elementsInScope.add(var);
						break;
					}
			}

			return elementsInScope.toArray(new PsiElement[elementsInScope.size()]);
		} catch (NullPointerException e) {
			return null;
		}
	}
}
