package net.sf.logsupport.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Juergen_Kellerer 14.04.2010
 */
public class Bla extends Super {

    private static MyLogger myLogger;

    private final Logger log = LoggerFactory.getLogger(Bla.class);
    private final boolean DEBUG_ENABLED = log.isDebugEnabled();

    {
        boolean b = DEBUG_ENABLED;

        if (myLogger.isDebugEnabled()) { myLogger.debug("asd"); }

        log.info("#SuperLOG-00726:");

        Object[] argArray = {b};
        log.info("#SuperLOG-0debug(sdasd, {}", argArray);
        if (DEBUG_ENABLED)
            log.debug("#SuperLOG-0037a:asdsad");

        log.warn("#SuperLOG-00384:asda");
        log.warn("#SuperLOG-0038e:asdasd");
    }

    {
        if (DEBUG_ENABLED)
            log.debug("");

        if (DEBUG_ENABLED)
            log.debug("#SuperLOG-00618:sadasd");

        try {
            if (myLogger.isDebugEnabled())
                myLogger.debug("asdsd");
            myLogger.error("#SuperLOG-006b8:asdasd");
        } catch (Exception e) {
            myLogger.error("#SuperLOG-006cc:asda", e);
            myLogger.error("#SuperLOG-006c2:asdasdas", e);
        }

        if (myLogger.isDebugEnabled())
            myLogger.debug("#SuperLOG-005e6:");
    }

    private static void test() {
    }

    static abstract class MyLogger implements Logger {

    }
}
