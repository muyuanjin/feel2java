package com.muyuanjin.feel;


import com.muyuanjin.feel.parser.ParserUtil;
import io.kotest.property.Arb;
import io.kotest.property.RandomSource;
import io.kotest.property.Sample;
import io.kotest.property.arbitrary.Codepoint;
import io.kotest.property.arbitrary.CodepointsKt;
import io.kotest.property.arbitrary.StringsKt;
import kotlin.sequences.Sequence;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Iterator;

/**
 * @author muyuanjin
 */
@State(Scope.Benchmark)
public class EscapeJavaBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(EscapeJavaBenchmark.class.getSimpleName())
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Param({"10", "100", "200", "500", "1000"})
    private int length;

    private String[] strings;

    @Setup
    public void setup() {
        strings = new String[500];
        Sequence<Sample<String>> samples = StringsKt.string(Arb.Companion, 0, length, CodepointsKt.printableAscii(Codepoint.Companion)).samples(RandomSource.Companion.seeded(0));
        Iterator<Sample<String>> iterator = samples.iterator();
        for (int i = 0; i < 500 && iterator.hasNext(); i++) {
            Sample<String> next = iterator.next();
            strings[i] = next.getValue();
        }
    }

    @Benchmark
    public void twoFor() {
        for (String string : strings) {
            ParserUtil.escapeJava(string, true);
        }
    }

    @Benchmark
    public void builder() {
        for (String string : strings) {
            escapeJava(string, true);
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    private static String escapeJava(String value, boolean quoted) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(quoted ? value.length() + 2 : value.length());
        if (quoted) {
            sb.append('\"');
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                default -> {
                    if (Character.isISOControl(c)) {
                        // 将ASCII中的不可见字符转义为八进制转义
                        sb.append("\\");
                        if (c < 64) {
                            if (c < 8) {
                                sb.append((char) ('0' + c));
                            } else {
                                sb.append((char) ('0' + c / 8)).append((char) ('0' + c % 8));
                            }
                        } else {
                            sb.append((char) ('0' + c / 64)).append((char) ('0' + c / 8 % 8)).append((char) ('0' + c % 8));
                        }
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        if (quoted) {
            sb.append('\"');
        }
        return sb.toString();
    }
}