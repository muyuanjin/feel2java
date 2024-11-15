package com.muyuanjin.feel;

import com.muyuanjin.feel.dmn.*;
import com.muyuanjin.feel.util.BenchmarkUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author muyuanjin
 */
public class FeelTest {
    @Test
    @SneakyThrows
    void test_2024_11_14_14_34_03() {
        Feel.Expression<Map<String, Object>> compile = Feel.compile("1+1");
        EvalResult eval = compile.eval(null);
        assertEquals(2, eval.asNumber());
        BenchmarkUtil.benchmark1000(() -> compile.eval(null));

        Feel.Expression<Map<String, Integer>> compile1 = Feel.compile("a+1", Map.of("a", 1));
        EvalResult eval1 = compile1.eval(Map.of("a", 1));
        assertEquals(2, eval1.asNumber());
        BenchmarkUtil.benchmark1000(() -> compile1.eval(Map.of("a", 1)));

        Feel.Expression<Map<String, Double>> compile2 = Feel.compile("a+1", Map.of("a", 1.0));
        EvalResult eval2 = compile2.eval(Map.of("a", 1.0));
        assertEquals(2.0, eval2.asNumber());
        BenchmarkUtil.benchmark1000(() -> compile2.eval(Map.of("a", 1.0)));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_14_41_40() {
        Feel.UnaryTest<Integer, Map<String, Object>> unaryTest = Feel.compileUT("?>1", Integer.class);
        assertTrue(unaryTest.test(2, null));
        BenchmarkUtil.benchmark1000(() -> unaryTest.test(2, null));

        Feel.UnaryTest<Integer, Map<String, Integer>> unaryTest1 = Feel.compileUT("a>1", null, Map.of("a", 2));
        assertTrue(unaryTest1.test(null, Map.of("a", 2)));
        BenchmarkUtil.benchmark1000(() -> unaryTest1.test(null, Map.of("a", 2)));

        Feel.UnaryTest<Integer, Map<String, Integer>> unaryTest2 = Feel.compileUT("?<a", 1, Map.of("a", 2));
        assertTrue(unaryTest2.test(1, Map.of("a", 2)));
        BenchmarkUtil.benchmark1000(() -> unaryTest2.test(1, Map.of("a", 2)));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_16_10_13() {
        var definition = DecisionTableDefinition.builder()
                .name("simple")
                .input(new InputClause("a+b", null))
                .input(new InputClause("a", "1"))
                .output(new OutputClause())
                .rule(DecisionRule.builder()
                        .input("3")
                        .input("-")
                        .output("\"hello word\"")
                        .build())
                .rule(DecisionRule.builder()
                        .input("2")
                        .input("-")
                        .output("\"hello word 2\"")
                        .build())
                .build();

        DecisionTable<POJO> table = Feel.compileDMN(definition, POJO.class);
        Path path = Path.of("E:\\JavaWorkSpace\\feel2java\\src\\test\\java\\com\\muyuanjin\\feel\\" + table.getClass().getSimpleName() + ".java");
        Files.createDirectories(path.getParent());
//        Files.writeString(path, table.getSource());
        Assertions.assertEquals(definition, table.getDefinition());
        Assertions.assertEquals("hello word", table.evaluate(new POJO(1, 2)).asString());
        Assertions.assertEquals("hello word 2", table.evaluate(new POJO(1, 1)).asString());

        BenchmarkUtil.benchmark1000(() -> table.evaluate(new POJO(1, 2)));
        BenchmarkUtil.benchmark1000(() -> table.evaluate(new POJO(1, 1)));
    }

    public record POJO(int a, int b) {}
}