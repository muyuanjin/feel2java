package com.muyuanjin.common.util;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

@UtilityClass
public class Assert {
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasLength(@Nullable String text, String message) {
        if (!StringUtil.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(@Nullable String text, String message) {
        if (!StringUtil.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(@Nullable Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(@Nullable Collection<?> collection, String message) {
        if (CollectionUtil.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, String message) {
        if (CollectionUtil.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }
}
