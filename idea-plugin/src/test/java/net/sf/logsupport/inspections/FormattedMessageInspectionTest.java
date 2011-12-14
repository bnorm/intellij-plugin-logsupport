package net.sf.logsupport.inspections;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PsiTestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO: Create Description.
 *
 * @author Juergen_Kellerer, 2011-02-13
 * @version 1.0
 */
@Ignore("test is broken")
public class FormattedMessageInspectionTest extends PsiTestCase {

	private static final String TEST_CODE = "public class Test { Test() { int i = 1; } }";
	PsiFile file;

	@Before
	public void initFile() throws Exception {
		file = createFile("Test.java", TEST_CODE);
	}

	@Test
	public void testDummy() {
		file.getFileType();
	}
}
