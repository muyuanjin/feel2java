package com.muyuanjin.feel.util;

import lombok.SneakyThrows;

/**
 * @author muyuanjin
 */
@FunctionalInterface
public interface IRunnable extends Runnable {
    void runEx() throws Throwable;

    @Override
    @SneakyThrows
    default void run() {
        runEx();
    }
}
