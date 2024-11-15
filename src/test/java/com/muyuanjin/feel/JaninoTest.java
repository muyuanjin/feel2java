package com.muyuanjin.feel;

import lombok.SneakyThrows;
import org.codehaus.commons.compiler.ErrorHandler;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.CachingJavaSourceClassLoader;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.ScriptEvaluator;
import org.codehaus.janino.UnitCompiler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * @author muyuanjin
 */
class JaninoTest {
    @Test
    @SneakyThrows
    void test_2024_11_10_10_09_56() {
        ExpressionEvaluator ee = new ExpressionEvaluator();

        // 该表达式将有两个“int”参数：“a”和“b”。
        ee.setParameters(new String[]{"a", "b"}, new Class[]{int.class, int.class});

        //并且表达式（即“结果”）类型也是“int”。
        ee.setExpressionType(int.class);

        // 现在我们“烹饪”（扫描、解析、编译和加载）这个美妙的表达式。
        ee.cook("a + b");

        //最终我们评估表达式 - 并且速度非常快。
        int result = (Integer) ee.evaluate(new Object[]{19, 23});
    }

    @SneakyThrows
    @Test
    void test_2024_11_10_10_12_15() {
        ScriptEvaluator se = new ScriptEvaluator();
        se.setCompileErrorHandler(new ErrorHandler() {
            private static final Field FIELD = UnitCompiler.class.getDeclaredField("compileErrorCount");

            @Override
            public void handleError(String message, Location location) {
                System.err.println("message = " + message);
                System.err.println("location = " + location);
                FIELD.setAccessible(true);
            }
        });
        se.setSourceVersion(17);
        se.setTargetVersion(17);
        se.setReturnType(String.class);
        se.cook("""
                import java.util.*;
                import com.muyuanjin.feel.lang.*;
                import com.muyuanjin.feel.lang.type.*;
                import com.muyuanjin.common.util.JSONUtil;
                String[] vegetables = new String[]{"garlic", "tomato"};
                List<String> result = new ArrayList<>(2*2);
                for (String fruit : new String[]{"apple", "bananas"}) {
                    for (String vegetable : vegetables) {
                        result.add(Map.of("ingredients", List.of(fruit, vegetable)));
                    }
                }
                int a = 1;
                final FType type = FList.of(FString.STRING);
                FeelFunction ff= new FeelFunction<>(){
                    @Override
                    public FFunction type() {
                        return FFunction.of(type);
                    }
                    @Override
                    public Object invoke(Object... args) {
                        return a+ args.length;
                    }
                };
                System.out.println(JSONUtil.toPrettyJSONString(result));
                return JSONUtil.toPrettyJSONString(ff.invoke("a", "b"));
                """);
        Object res = se.evaluate(null);
        System.out.println(res);
    }

    @Test
    void test_2024_11_10_10_22_11() {
        ClassLoader platformClassLoader = CachingJavaSourceClassLoader.getPlatformClassLoader();

    }
}
