package net.sf.logsupport.logback;

import org.slf4j.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Juergen_Kellerer 14.04.2010
 */
public class Bla {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Bla.class);
    private static MyLogger myLogger;

    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();

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

        if (log.isDebugEnabled())
            myLogger.debug("#SuperLOG-005e6:");
    }

    static abstract class MyLogger implements Logger {

    }
}
