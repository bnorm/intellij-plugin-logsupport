package net.sf.logsupport.logback;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Juergen_Kellerer 14.04.2010
 */
public class LoggingClassTest {
    private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance("#net.sf.logsupport.logback.LoggingClassTest");


    @org.junit.Test
    public void testLogIt() throws Exception {
        LOG.info("#SuperLOG-00514:");

        LOG.warn("#SuperLOG-0058c:asdasda");
    }
}
