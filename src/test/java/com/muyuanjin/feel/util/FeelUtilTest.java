package com.muyuanjin.feel.util;

import com.muyuanjin.compiler.JavaCompiler;
import com.muyuanjin.compiler.util.JMethods;
import com.muyuanjin.feel.lang.type.FContext;
import com.muyuanjin.feel.lang.type.FList;
import com.muyuanjin.feel.lang.type.FNumber;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.muyuanjin.feel.lang.FTypes.STRING;

/**
 * @author muyuanjin
 */
class FeelUtilTest {
    @Test
    @SneakyThrows
    void test_2024_11_14_09_34_58() {
        String compiled = FeelUtil.compileExpr("runtime.feel", "TestExpr", "eval", "1+1");
        @Language("Java") String source = """
                package runtime.feel;
                
                import java.util.Map;
                
                public class TestExpr {
                
                    public static int eval(Map<String, Object> input) {
                        return 1 + 1;
                    }
                }
                """;
        Assertions.assertEquals(source.replaceAll("\n", System.lineSeparator()), compiled);
        Class<Object> loaded = JavaCompiler.NATIVE.compile("TestExpr.java", compiled).loadSingle();
        Object o = JMethods.invokeStatic(loaded, "eval", Map.of());
        Assertions.assertEquals(1 + 1, o);
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_10_07_35() {
        String compiled = FeelUtil.compileUT("runtime.feel", "TestUT", "eval", "? > 1", FNumber.NUMBER);
        @Language("Java") String source = """
                package runtime.feel;
                
                import java.util.Map;
                
                public class TestUT {
                
                    public static boolean eval(Number testInput, Map<String, Object> input) {
                        return testInput.doubleValue() > 1;
                    }
                }
                """;
        Assertions.assertEquals(source.replaceAll("\n", System.lineSeparator()), compiled);
        Class<Object> loaded = JavaCompiler.NATIVE.compile("TestUT.java", compiled).loadSingle();
        Object o = JMethods.invokeStatic(loaded, "eval", 2, Map.of());
        Assertions.assertEquals(true, o);
    }

    @Test
    @SneakyThrows
    void test_2024_11_31_16_42_56() {
        System.out.println(JSONUtil.toJSONString(FContext.of("_1", FList.of(STRING))));
    }
}