package de.epiceric.shopchest.nms;

import java.lang.reflect.Field;

/**
 * Reflection utility class
 */
public class ReflectionUtils {

    /**
     * Unsafe cast, used to suppress warning for known type of generic class
     *
     * @param o   The object to cast
     * @param <T> The type you want
     * @return The same object but with the good type
     */
    @SuppressWarnings("unchecked")
    public static <T> T forceCast(Object o) {
        return (T) o;
    }

    /**
     * Get a private static field value
     *
     * @param clazz     The {@link Class} to lookup
     * @param fieldName The {@link Field} name
     * @return The value the private static field
     * @throws ReflectiveOperationException If the field can't be found
     */
    public static Object getPrivateStaticFieldValue(Class<?> clazz, String fieldName) throws ReflectiveOperationException {
        final Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

}
