package net.sf.logsupport.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Small utility that simplifies the usage of reflection.
 * <p/>
 * Reflection is often needed to support more than one IntelliJ version with a build.
 *
 * @author Juergen_Kellerer, 2011-05-17
 */
public class ReflectionUtil {

	/**
	 * Returns the field value using reflection.
	 *
	 * @param instance  the instance to get the field from.
	 * @param fieldName the name of the field.
	 * @param <T>       the type of the value.
	 * @return the value of the field.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getField(Object instance, String fieldName) {
		return (T) getField(instance.getClass(), instance, fieldName);
	}

	/**
	 * Returns the field value using reflection.
	 *
	 * @param cls	   the class to get the field from.
	 * @param instance  the instance to get the field from (use 'null' to access static fields).
	 * @param fieldName the name of the field.
	 * @param <T>       the type of the value.
	 * @return the value of the field.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getField(Class cls, @Nullable Object instance, String fieldName) {
		try {
			Field field = cls.getField(fieldName);
			return (T) field.get(instance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes the given method with optional params and returns the result.
	 *
	 * @param instance   the instance to invoke the method on.
	 * @param methodName the name of the method to invoke.
	 * @param params	 the params to pass to the method.
	 * @param <T>        the type of the return value.
	 * @return the return value of the method or 'null' if the method signature is void.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Object instance, String methodName, Object... params) {
		return (T) invoke(instance, methodName, null, params);
	}

	/**
	 * Invokes the given method with optional params and returns the result.
	 *
	 * @param instance   the instance to invoke the method on.
	 * @param methodName the name of the method to invoke.
	 * @param paramTypes the types of the params (only needed if one of the params is 'null').
	 * @param params	 the params to pass to the method.
	 * @param <T>        the type of the return value.
	 * @return the return value of the method or 'null' if the method signature is void.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Object instance, String methodName,
							   @Nullable Class<?>[] paramTypes, Object... params) {
		if (paramTypes == null) {
			paramTypes = new Class[params.length];
			for (int i = 0; i < paramTypes.length; i++) {
				if (params[i] == null)
					throw new IllegalArgumentException("Cannot use 'null' arguments with this method.");
				paramTypes[i] = params[i].getClass();
			}
		}

		try {
			Method method = instance.getClass().getMethod(methodName, (Class<Object>[]) paramTypes);
			return (T) method.invoke(instance, (Object[]) params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ReflectionUtil() {
	}
}
