package com.muyuanjin.feel.lang;

import com.muyuanjin.feel.lang.type.FFunction;

import java.util.function.Function;

/**
 * @author muyuanjin
 */
public interface FeelFunction<R> {
    FFunction type();

    R invoke(Object... args);

    record Default<R>(FFunction type, Function<Object[], R> fun) implements FeelFunction<R> {
        @Override
        public R invoke(Object... args) {
            return fun.apply(args);
        }

        @Override
        public String toString() {
            return "FeelFunction.Default{" +
                   "type=" + type +
                   '}';
        }
    }
}