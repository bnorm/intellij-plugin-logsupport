package net.sf.logsupport.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: juergen
 * Date: 22.05.2011
 * Time: 23:06:52
 * To change this template use File | Settings | File Templates.
 */
public interface IfTest {

    Logger log = LoggerFactory.getLogger(IfTest.class);
    Object test = new Object() {

        {
            log.info("#SuperLOG-00622:asdad");
        }

    };

}
