package net.sf.logsupport.livetemplates;

/**
 * Is an intermediate abstract class that acts as a "facade" that is compatible with IntelliJ IDEA 11.
 *
 * @author Juergen_Kellerer, 2011-12-14
 */
public abstract class AbstractMacroBase extends com.intellij.codeInsight.template.Macro {
	public String getPresentableName() {
		return getName();
	}
}
