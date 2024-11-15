package com.muyuanjin.feel;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author muyuanjin
 */
@State(Scope.Benchmark)
public class StringTraversalBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringTraversalBenchmark.class.getSimpleName())
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Param({"10", "100", "1000", "10000"})
    private int length;

    private String string;

    @Setup
    public void setup() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + i % 26));
        }
        string = sb.toString();
    }

    @Benchmark
    public void charAt() {
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
        }
    }

    @Benchmark
    public void toCharArray() {
        for (char c : string.toCharArray()) {
        }
    }

    @Benchmark
    public void charStream() {
        string.chars().forEachOrdered(i -> {
        });
    }
}
