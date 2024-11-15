package com.muyuanjin.common.function;

import lombok.SneakyThrows;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * 用于lambda摆脱javac的异常捕捉限制（并非包装为runtime）
 *
 * @author muyuanjin
 */
@FunctionalInterface
public interface FunctionEx<T, R> extends Function<T, R>, Serializable {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R applyEx(T t) throws Throwable;

    @Override
    @SneakyThrows(Throwable.class)
    default R apply(T t) {
        return applyEx(t);
    }

    @NotNull
    default <V> FunctionEx<V, R> compose(@NotNull FunctionEx<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    @NotNull
    default <V> FunctionEx<T, V> andThen(@NotNull FunctionEx<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    default FunctionEx<T, R> nullSafe() {
        return (t) -> {
            if (t == null) {
                return null;
            }
            return apply(t);
        };
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> FunctionEx<T, T> identity() {
        return t -> t;
    }

    static <T, R> FunctionEx<T, R> of(Function<T, R> function) {
        return function::apply;
    }
}

