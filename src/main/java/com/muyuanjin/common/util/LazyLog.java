package com.muyuanjin.common.util;

import lombok.experimental.Delegate;
import org.slf4j.Logger;

/**
 * @author muyuanjin
 */
public class LazyLog implements Logger {
    private final LazyRef<Logger> logRef;

    private LazyLog(Class<?> clazz) {
        this.logRef = LazyRef.of(() -> org.slf4j.LoggerFactory.getLogger(clazz));
    }

    @Delegate
    public Logger get() {
        return logRef.get();
    }

    public static LazyLog of(Class<?> clazz) {
        return new LazyLog(clazz);
    }
}