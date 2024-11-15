package com.muyuanjin.feel.parser.antlr4;

import com.muyuanjin.feel.util.JSONUtil;
import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.type.FFunction;
import com.muyuanjin.feel.lang.type.FString;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.invoke.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author muyuanjin
 */
public class JavaTest {
    @Test
    void test_2024_11_10_10_02_36() {
        String[] vegetables = new String[]{"garlic", "tomato"};
        List<Object> result = new ArrayList<>(2 * 2);
        for (String fruit : new String[]{"apple", "bananas"}) {
            for (String vegetable : vegetables) {
                result.add(Map.of("ingredients", List.of(fruit, vegetable)));
            }
        }
        System.out.println(JSONUtil.toPrettyJSONString(result));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_14_55_54() {
        FFunction fFunction = FFunction.of(FString.STRING, FString.STRING, FString.STRING);
        FeelFunction<String> function1 = new FeelFunction.Default<>(fFunction, args -> feelFun$getN$0((String) args[0], (String) args[1]));


        FeelFunction<String> function2 = new FeelFunction.Default<>(fFunction, new Function<Object[], String>() {
            @Override
            public String apply(Object[] args) {
                return feelFun$getN$0((String) args[0], (String) args[1]);
            }
        });
    }

    private static String feelFun$getN$0(String a, String b) {
        return a + b;
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_11_17_09() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // 定义Lambda体的类型 (Object) -> Object
        MethodType invokedType = MethodType.methodType(BiFunction.class);
        // 定义Lambda实现方法的签名 (Object[]) -> String
        MethodType implementationMethodType = MethodType.methodType(String.class, String.class, String.class);

        // 获取指向具体实现方法的MethodHandle
        MethodHandle implMethodHandle = lookup.findStatic(
                JavaTest.class,
                "feelFun$getN$0",
                implementationMethodType
        );

        // Lambda的方法类型签名 (Object) -> Object
        MethodType instantiatedMethodType = MethodType.methodType(Object.class, Object.class, Object.class);

        // 调用LambdaMetafactory.metafactory方法
        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                invokedType,
                instantiatedMethodType,
                implMethodHandle,
                implementationMethodType
        );
        // 获取生成的方法句柄
        MethodHandle factory = callSite.getTarget();

        // 转换方法句柄为(Function<Object, String>)类型并调用
        BiFunction<String, String, String> lambdaFunction = (BiFunction<String, String, String>) factory.invoke();

        // 使用转换后的函数
        String result = lambdaFunction.apply("1", "2");
        System.out.println("Lambda result: " + result);
    }

}
