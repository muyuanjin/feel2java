package com.muyuanjin.feel.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * @author muyuanjin
 */
class UtilFeelFunctionFactoryTest {
    @Test
    @SneakyThrows
    void test_2024_11_14_09_05_43() {
        var functions1 = UtilFeelFunctionFactory.getFunctions(Math.class);
        System.out.println(functions1);
    }
}