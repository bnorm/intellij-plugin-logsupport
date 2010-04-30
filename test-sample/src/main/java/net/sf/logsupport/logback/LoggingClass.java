package net.sf.logsupport.logback;

import org.slf4j.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Juergen_Kellerer 02.04.2010
 */
public class LoggingClass {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LoggingClass.class);

    public void logIt() {
    }

    private static class Inner {
        void logIt() {
            String a = "bla", b = "blub";

            log.info("Probieren wirs mal mit " + a +
                    " und " + b + ".");
            log.info("Probieren wirs mal mit " + a +
                    " und " + b + " und " + a + b + " {}! ", "a");

            log.info("#SuperLOG-004ba:Probieren wirs mal mit " + a +
                    " und " + b + ".");
            log.info("#SuperLOG-004c4:Probieren wirs mal mit " + a +
                    " und " + b + " und " + a + b + " {}! ", "a");

            log.info("#SuperLOG-00398:Noch ein neuer");

            final int name = 1;
            log.info("#SuperLOG-00398:Mein name is '{}'", name);

            log.warn("#SuperLOG-00492:asdasdasd");

            log.info("#SuperLOG-003a2:dfsdfsdf");
            log.info("#SuperLOG-003a2:dfsdfsdf");

            log.info("#SuperLOG-003c0:asdasd");
            log.info("#SuperLOG-00352:sadasd");

            if (log.isDebugEnabled())
                log.debug("#SuperLOG-0035c:asdasdasd");
        }
    }
}
