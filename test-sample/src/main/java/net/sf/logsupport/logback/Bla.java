package net.sf.logsupport.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Juergen_Kellerer 14.04.2010
 */
public class Bla extends Super {
    //private static MyLogger myLogger;

    private static final Logger log = LoggerFactory.getLogger(Bla.class);
    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();

    static {
        if (DEBUG_ENABLED)
            log.debug("sad");
    }

    {
        boolean b = DEBUG_ENABLED;
        if (b)
            log.debug("#SuperLOG-004b0:kkk");

        log.info("#SuperLOG-00370:asdasd");
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

        if (log.isDebugEnabled())
            myLogger.debug("#SuperLOG-005e6:");
    }

    static abstract class MyLogger implements Logger {

    }
}
