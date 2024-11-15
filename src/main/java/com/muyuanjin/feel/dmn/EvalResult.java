package com.muyuanjin.feel.dmn;


import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.FeelRange;

import java.time.*;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
public record EvalResult(boolean success, String message, Object value) {
    public static final EvalResult NULL = new EvalResult(true, null, null);

    public static EvalResult of(Object value) {
        return new EvalResult(true, null, value);
    }

    public static EvalResult ofError(String message) {
        return new EvalResult(false, message, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
        if (success) {
            return (T) value;
        }
        throw new IllegalStateException("EvalResult is not success");
    }

    public boolean isNull() {
        return get() == null;
    }

    public String asString() {
        return get();
    }

    public LocalDate asDate() {
        return get();
    }

    public LocalTime asTime() {
        return get();
    }

    public Boolean asBoolean() {
        return get();
    }

    public Number asNumber() {
        return get();
    }

    public <T> List<T> asList() {
        return get();
    }

    public <T> FeelRange<T> asRange() {
        return get();
    }

    public LocalDateTime asDateTime() {
        return get();
    }

    public Duration asDayTimeDuration() {
        return get();
    }

    public Period asYearMonthDuration() {
        return get();
    }

    public Map<String, Object> asContext() {
        return get();
    }

    public <T> FeelFunction<T> asFunction() {
        return get();
    }
}