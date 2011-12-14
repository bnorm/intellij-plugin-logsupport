package net.sf.logsupport.util;

import com.intellij.psi.PsiType;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Simple test for the reflection util.
 *
 * @author juergen kellerer, 2011-05-28
 */
public class ReflectionUtilTest {

	public static class TestClass {
		public String field = "abc";

		public static void callA() {
		}

		public String callB(String cmd) {
			return cmd;
		}

		public Collection callB(Set set) {
			return set;
		}
	}

	@Test
	public void testGetField() throws Exception {
		assertNotNull(ReflectionUtil.getField(PsiType.class, "DOUBLE"));
		assertNotNull(ReflectionUtil.getField(PsiType.class, null, "DOUBLE"));
		assertEquals("abc", ReflectionUtil.getField(new TestClass(), "field"));
	}

	@Test
	public void testInvoke() throws Exception {
		ReflectionUtil.methods.entrySet().clear();
		for (int i = 0; i < 10; i++) {
			ReflectionUtil.invoke(TestClass.class, "callA");
			assertEquals("xyz", ReflectionUtil.invoke(new TestClass(), "callB", "xyz"));

			Set<String> set = new LinkedHashSet<String>(Collections.singleton("xyz"));
			assertEquals(set, ReflectionUtil.invoke(new TestClass(), "callB", set));
		}
		assertEquals(3, ReflectionUtil.methods.size());
	}
}
