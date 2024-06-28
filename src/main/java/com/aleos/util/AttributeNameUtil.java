package com.aleos.util;


public final class AttributeNameUtil {

    public static final String PAYLOAD_MODEL_ATTR = AttributeNameUtil.class.getName() + "_payload_model";

    public static final String RESPONSE_MODEL_ATTR = AttributeNameUtil.class.getName() + "_response_model";

    private AttributeNameUtil() {
    }

    public static String getName(Class<?> clazz) {

        String className = clazz.getSimpleName();
        var letter = className.substring(0, 1);

        return className.replaceFirst(letter, letter.toLowerCase());
    }
}
