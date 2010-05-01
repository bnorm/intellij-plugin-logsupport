package net.sf.logsupport.log4j;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Juergen_Kellerer 14.04.2010
 */
public class LoggingClass {
    private static final Logger logger = Logger.getLogger(LoggingClass.class.getName());

    {
        String bla = "";

        logger.log(Level.WARNING, "#SuperLOG-005aa:sfsdf");

        logger.log(Level.WARNING, "asdadasd");

        String test = "";
        if (logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, "#SuperLOG-00276:asdasdasd {0} {1} sdas", new Object[]{"some message", test});

        if (logger.isLoggable(Level.FINER))
            logger.log(Level.FINER, "#SuperLOG-00280:asdasdad");

        if (logger.isLoggable(Level.FINE))
            logger.log(Level.FINE, "#SuperLOG-0028a:asdasd");

        try {
            if (logger.isLoggable(Level.INFO))
                logger.log(Level.INFO, "#SuperLOG-00294:dfdsfsdfsdf");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "#SuperLOG-0029e:sadasdasd", e);
        }
    }

    {
        File myFile = null;

        logger.log(Level.INFO, "#MyID-005dc:passed");
    }
}
