package com.muyuanjin.common.function;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 用于lambda摆脱javac的异常捕捉限制（并非包装为runtime）
 *
 * @author muyuanjin
 */
@FunctionalInterface
public interface SupplierEx<T> extends Supplier<T>, Serializable {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T getEx() throws Throwable;

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    @SneakyThrows(Throwable.class)
    default T get() {
        return getEx();
    }
}

