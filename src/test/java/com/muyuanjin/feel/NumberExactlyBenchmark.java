package com.muyuanjin.feel;


import com.muyuanjin.feel.parser.ParserUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;

/**
 * @author muyuanjin
 */
@SuppressWarnings("UnusedReturnValue")
@State(Scope.Benchmark)
public class NumberExactlyBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NumberExactlyBenchmark.class.getSimpleName())
                .mode(Mode.Throughput)
                .forks(1)
                .build();
        new Runner(opt).run();
    }


    private BigDecimal[] numbers;

    @Setup
    public void setup() {
        numbers = new BigDecimal[10000];
        for (int i = 0; i < 10000; i++) {
            numbers[i] = new BigDecimal("0." + i);
        }
    }

    @Benchmark
    public void five() {
        for (BigDecimal number : numbers) {
            ParserUtil.canExactlyBeDouble(number);
        }
    }

    @Benchmark
    public void old() {
        for (BigDecimal number : numbers) {
            canExactly(number);
        }
    }

    public static boolean canExactly(BigDecimal decimal) {
        double result = decimal.doubleValue();
        return !Double.isInfinite(result) && new BigDecimal(result).compareTo(decimal) == 0;
    }
}
