package com.muyuanjin.feel.translate;

import com.muyuanjin.feel.dmn.*;
import com.muyuanjin.feel.dmn.impl.DefaultDMNCompiler;
import com.muyuanjin.feel.util.BenchmarkUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author muyuanjin
 */
public class DMNCompilerTest {
    @Test
    @SneakyThrows
    void test_2024_11_14_14_30_22() {
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
        DefaultDMNCompiler dmnCompiler = new DefaultDMNCompiler("com.muyuanjin.feel", "MyClass");
        DecisionTable<POJO> table = dmnCompiler.compile(definition, POJO.class);
        Path path = Path.of("E:\\JavaWorkSpace\\feel2java\\src\\test\\java\\com\\muyuanjin\\feel\\" + table.getClass().getSimpleName() + ".java");
        Files.createDirectories(path.getParent());
//        Files.writeString(path, table.getSource());
        Assertions.assertEquals(definition, table.getDefinition());
        Assertions.assertEquals("hello word", table.evaluate(new POJO(1, 2)).asString());
        Assertions.assertEquals("hello word 2", table.evaluate(new POJO(1, 1)).asString());

        BenchmarkUtil.benchmark1000(() -> table.evaluate(new POJO(1, 2)));
        BenchmarkUtil.benchmark1000(() -> table.evaluate(new POJO(1, 1)));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class POJO {
        private int a;
        private int b;
    }
}
