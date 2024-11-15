package com.muyuanjin.feel.lang;

import com.muyuanjin.feel.lang.type.FRange;

/**
 * @author muyuanjin
 */
public interface FeelRange<T> {
    FRange type();

    T start();

    T end();

    default boolean startInclusive() {
        return Boolean.TRUE.equals(type().getStart());
    }

    default boolean endInclusive() {
        return Boolean.TRUE.equals(type().getEnd());
    }

    record Default<T>(FRange type, T start, T end) implements FeelRange<T> {
    }
}