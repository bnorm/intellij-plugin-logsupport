package net.sf.logsupport.config.defaults;

import net.sf.logsupport.config.LogFramework;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Smoke test the default configuration list.
 *
 * @author juergen kellerer, 2011-09-01
 */
public class LogFrameworkDefaultsListTest {

	LogFrameworkDefaultsList defaultsList;

	@Before
	public void setUp() throws Exception {
		defaultsList = new LogFrameworkDefaultsList();
	}

	@Test
	public void testDefaultListCanBeCreatedAndIsNotEmpty() throws Exception {
		assertFalse(defaultsList.isEmpty());
	}

	@Test
	public void testNamesAreUnique() throws Exception {
		Set<String> names = new HashSet<String>();
		for (LogFramework framework : defaultsList)
			assertTrue(names.add(framework.getName()));
	}
}
