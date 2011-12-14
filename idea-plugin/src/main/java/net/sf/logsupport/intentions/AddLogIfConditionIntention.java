package net.sf.logsupport.intentions;

import com.intellij.psi.PsiMethodCallExpression;
import net.sf.logsupport.L10N;
import org.jetbrains.annotations.NotNull;

/**
 * Add a IfStatement to support conditional logging for improved performance.
 *
 * @author Juergen_Kellerer, 2010-04-27
 * @version 1.0
 */
public class AddLogIfConditionIntention extends AbstractLogConditionIntention {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvailable(PsiMethodCallExpression expression) {
		return findSurroundingCondition(expression) == null && createPlainIfCondition(expression, false, false) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doInvoke(PsiMethodCallExpression expression) {
		wrapInIfConditionIfRequired(expression);
	}

	/**
	 * {@inheritDoc}
	 */
	@NotNull
	public String getText() {
		return L10N.message("Intentions.AddLogIfConditionIntention.name");
	}
}
