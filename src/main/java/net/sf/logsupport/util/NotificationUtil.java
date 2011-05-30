package net.sf.logsupport.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

/**
 * Backwards compatible notification utility.
 *
 * @author Juergen_Kellerer, 2011-05-30
 * @version 1.0
 */
public class NotificationUtil {

	private static final Logger LOG = Logger.getInstance("#net.sf.logsupport.util.NotificationUtil");

	private static final String TYPE_ENUM = "com.intellij.notification.NotificationType";
	private static final String BUS_CLASS = "com.intellij.notification.Notifications$Bus";
	private static final String NOTIFICATION_CLASS = "com.intellij.notification.Notification";
	private static final String LOG_METHOD = "notify";

	private static Constructor<?> notificationConstructor;

	/**
	 * Sends a info notification.
	 *
	 * @param message the message to send.
	 * @param project the project the message belongs to.
	 */
	public static void notifyInformation(String message, @Nullable Project project) {
		notify(message, "INFORMATION", project);
	}

	/**
	 * Sends a warning notification.
	 *
	 * @param message the message to send.
	 * @param project the project the message belongs to.
	 */
	public static void notifyWarning(String message, @Nullable Project project) {
		notify(message, "WARNING", project);
	}

	/**
	 * Sends an error notification.
	 *
	 * @param message the message to send.
	 * @param project the project the message belongs to.
	 */
	public static void notifyError(String message, @Nullable Project project) {
		notify(message, "ERROR", project);
	}

	private static void notify(String message, String type, @Nullable Project project) {
		try {
			//notify(new Notification(LOG_ONLY_GROUP_ID, "", text, type), project);

			final Object typeEnum = Class.forName(TYPE_ENUM).getField(type).get(null);
			if (notificationConstructor == null) {
				notificationConstructor = Class.forName(NOTIFICATION_CLASS).getConstructor(
						String.class, String.class, String.class, typeEnum.getClass());
			}

			Object notification = notificationConstructor.newInstance("Log Only", "", message, typeEnum);
			ReflectionUtil.invoke(Class.forName(BUS_CLASS), LOG_METHOD, notification, project);
		} catch (Throwable e) {
			LOG.info("Notification disabled. Logging notification instead.");
			LOG.info(project == null ? "global" : project.getName() + ":" + type + ":" + message);
		}
	}

	private NotificationUtil() {
	}
}
