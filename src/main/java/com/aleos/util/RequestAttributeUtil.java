package com.aleos.util;

import com.aleos.exception.servlet.PayloadNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

import java.util.Optional;

public final class RequestAttributeUtil {

    public static final String PAYLOAD_MODEL = "payload_model";
    public static final String RESPONSE_MODEL = "response_model";

    private RequestAttributeUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getName(Class<?> clazz) {
        String className = clazz.getSimpleName();
        var letter = className.substring(0, 1);
        return className.replaceFirst(letter, letter.toLowerCase());
    }

    public static <T> T getPayload(HttpServletRequest req, Class<T> clazz) {
        Object rawPayload = req.getAttribute(PAYLOAD_MODEL);

        if (rawPayload == null) {
            throw new PayloadNotFoundException("Payload is not found for %s".formatted(clazz.getSimpleName()));
        } else if (!clazz.isInstance(rawPayload)) {
            throw new PayloadNotFoundException("Payload type is invalid. Expected: %s, Found: %s".formatted(
                    clazz.getSimpleName(), rawPayload.getClass().getSimpleName()));
        } else {
            return clazz.cast(rawPayload);
        }
    }

    public static void setPayload(HttpServletRequest req, Object payload) {
        req.setAttribute(PAYLOAD_MODEL, payload);
    }

    public static Optional<Object> getResponse(HttpServletRequest req) {
        return Optional.ofNullable(req.getAttribute(RESPONSE_MODEL));
    }

    public static void setResponse(HttpServletRequest req, @NonNull Object model) {
        req.setAttribute(RESPONSE_MODEL, model);
    }
}
