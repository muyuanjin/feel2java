package com.muyuanjin.feel.translate;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.compiler.JavaCompiler;
import com.muyuanjin.compiler.util.JMethods;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.util.FeelUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
public class ASTCompilerVisitorTest {
    @Test
    @SneakyThrows
    void getBody() {
        run("1 between 3.33000 and 3");
    }

    @Test
    @SneakyThrows
    void test_2024_11_11_20_03_16() {
        run("[@\"2024-04-11\",@\"2024-04-01\"]");
    }

    @Test
    @SneakyThrows
    void test_2024_11_11_20_03_18() {
        String[] strings = {
                "0.1,0.2,0.1*0.2,0.1+0.2,0.1**0.2,0.1/0.2,0.1-0.2",
                "0.1=0.2,0.1=0.1,0.1>0.2,0.1<=0.2",
                "0.1<0.2,0.1>=0.2,0.1!=0.2,0.1!=0.1",
                "\"12345678910111213141516\"",
                "@\"2024-04-11\",@\"2024-04-01\"",
        };
        run(Arrays.toString(strings));

    }

    @Test
    @SneakyThrows
    void test_2024_11_12_09_21_08() {
        String[] strings = {
                "1 instance of any",
                "1 instance of null",
                "1 instance of date",
                "1 instance of time",
                "1 instance of number",
                "1 instance of string",
                "1 instance of boolean",
                "1 instance of list<string>",
                "1 instance of date and time",
                "1 instance of day and time duration",
                "1 instance of list<list<string>>",
                "1 instance of year and month duration",
                "1 instance of context<age:number,name:string>",
                "1 instance of list<list<context<age:number,name:string>>>",
                "1 instance of context<age:number,name:string,other:context<a:any,b:null>>",
                "1 instance of range[]<any>",
                "1 instance of range[)<string>",
                "1 instance of range(]<string>",
                "1 instance of range()<string>",
                "1 instance of range(<string>",
                "1 instance of range)<string>",
                "1 instance of range]<string>",
                "1 instance of range[<string>",
                "1 instance of function<number,number>->number",
                "1 instance of function<number>->number",
                "1 instance of function<number>->context<age:number,name:string>",
                "1 instance of function<number>->context<age:number,name:string,other:function<number>->context<age:number,name:string>>",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    void test_2024_11_12_13_10_17() {
        String[] strings = {
                "1 instance of any",
                "1 instance of null",
                "1 instance of date",
                "1 instance of time",
                "1 instance of number",
                "1 instance of string",
                "1 instance of boolean",
                "1 instance of list<string>",
                "1 instance of range[]<any>",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_23_08_47() {
        String[] strings = {
                "[1,2,3].contains(1)",
                "date(1,2,3)",
                "date(@\"2024-04-11\")",
                "date(@\"2016-07-29T05:48:23\")",
                "date",
                "date.returnType",
                "date.parameterTypes",
                "date.parameterNames",
                "[1,2,3].contains",
                "[1,2,3].contains.returnType",
                "[1,2,3].contains.parameterTypes",
                "[1,2,3].contains.parameterNames",
                "[1,2,3].x",
                "sum(1,2,3)",
                "sum([1,2,3])",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_16_15_26() {
        String[] strings = {
                "{a:1,b:2,c:3}",
                "{a:1,b:2,c:a+b}",
                "{a:1,b:2,c:a+b}.c",
                "{a:1+2,b:2+3,c:a+b}.c",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_09_33_52() {
        String[] strings = {
                "Math.abs.returnType",
                "Math.abs(arg0:-99)",
                "Math.sqrt(99.9)",
                "Math.sqrt(@\"2024-04-11\".value)",
                "Math.sqrt(@\"2016-07-29T05:48:23\".value)",
                "StringUtil.containsAny(\"99.9\",\"98\",\".\")",
                "\"  99.9  \".trim",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    void test_2024_11_14_19_58_25() {
        String[] strings = {
                "{a:1,b:2,c:a+b}.c+Math.sqrt(99.9)",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes"})
    void feelFunctionLambda() {
        String[] strings = {
                "{fun:function(name:string,age:number) name+age,b:fun(\"muyuanjin\",99),c:fun}",
                "{fun:function(a:number,b:number) a+b,b:fun(1,99),c:fun.returnType}",
                "{fun:function(a:number) a+22,b:fun(1),c:fun.returnType}",
                "{fun:function(a:string) a+22,b:fun(\"mu\"),c:fun.returnType}",
                "{fun:function(a:context<name:string,age:number>) a.name+a.age,b:fun({name:\"mu\",age:66}),c:fun.returnType}",
        };
        Map<String, Object> run = (Map) ((List) run(Arrays.toString(strings))).get(0);
        Assertions.assertEquals(FeelFunction.Default.class, run.get("fun").getClass());
        Assertions.assertEquals(FFunction.of(FString.STRING, "name", FString.STRING, "age", FNumber.NUMBER), ((FeelFunction<String>) run.get("fun")).type());
        Assertions.assertEquals("muyuanjin99", run.get("b"));
    }

    @Test
    @SneakyThrows
    void test_2024_11_07_20_19_21() {
        String[] strings = {
                "{a:1,b:2,c:a+b,d:a+b+c,e:function(a:number,b:number) {a:3,b:4,c:a+b,d:a+b+c,e:function(a:number,b:number) a+b+c+d+e}.e(a+c,b+d)}.e(36.3,12.1)",
        };
        Object run = run(Arrays.toString(strings), "e", 2);
        Assertions.assertEquals(Collections.singletonList(new BigDecimal("80.4")), run);
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void test_2024_11_07_10_03_41() {
        String[] strings = {
                "{a:1,b:2,c:a+b,d:a+b+c,e:function(a:number,b:number) {a:3,b:4,c:a+b,d:a+b+c,e:function(a:number,b:number) a+b+c+d+e}.e(a+c,b+d)}.e",
                "{a:1,b:2,c:a+b,d:a+b+c,e:function(a:number,b:number) {a:3,b:4,c:a+b,d:a+b+c,e:function(a:number,b:number) a+b+c+d+e}.e(a+c,b+d)}.e(36.3,12.1)",
        };
        List<Object> run = (List<Object>) run(Arrays.toString(strings), "e", 2);
        FFunction function = FFunction.of(FTypes.NUMBER, List.of("a", "b"), List.of(FTypes.NUMBER, FTypes.NUMBER));
        Assertions.assertEquals(function, ((FeelFunction<BigDecimal>) run.get(0)).type());
        Assertions.assertEquals(new BigDecimal("80.4"), run.get(1));
    }

    @Test
    @SneakyThrows
    void test_2024_11_08_09_48_30() {
        String[] strings = {
                "[1..2]",
                "[1..2)",
                "(1..2)",
                "(1..2]",
                "]1..2[",
                "]1..2)",
                "]1..2]",
                "(1..2[",
                "[1..2[",
                ">=1",
                ">1",
                "<1",
                "<=1",
        };
        run(Arrays.toString(strings));
    }

    @Test
    @SneakyThrows
    void filterNode() {
        String[] strings = {
                "[1,2,3,4,5,6][0]",
                "[1,2,3,4,5,6][1]",
                "[1..3][-1]",
                "[1..3)[-1]",
                "[1,2,3,4,5,6][-1]",
                "[1,2,3,4,5,6][-1+2]",
                "[1,2,3,4,5,6][-1+2+3]",
                "2[1]",
                "2[-1]",
                "2[2]",
                "{a:1,b:2}[b>1.5]",
                "{a:1,b:2}[b>2]",
                "[{a:1,b:2},{a:2.1,b:4.4,c:3,d:0.5}][b>2]",
        };
        Object run = run(Arrays.toString(strings));
        Assertions.assertEquals(Arrays.asList(
                null,
                1,
                3,
                2,
                6,
                1,
                4,
                2,
                2,
                null,
                List.of(MapUtil.of("a", 1, "b", 2)),
                List.of(),
                List.of(MapUtil.of("a", new BigDecimal("2.1"), "b", new BigDecimal("4.4"), "c", 3, "d", 0.5))
        ), run);
    }

    @Test
    @SneakyThrows
    void quantifiedNode() {
        String[] strings = {
                "some _ in [1,2,3,4,5,6] satisfies _ > 3",
                "every _ in [1,2,3,4,5,6] satisfies _ > 3",
                "every _ in [3,4,5,6] satisfies _ >= 3",
                "some _ in 3..6 satisfies _ = 3",
                "every _ in [3,4,5,6],b in [1.2,2.2,3.4] satisfies _ >= 3 and b > 2",
                "some _ in [3,4,5,6],b in [1.2,2.2,3.4] satisfies _ >= 3 and b > 2",
        };
        Object run = run(Arrays.toString(strings));
        Assertions.assertEquals(Arrays.asList(true, false, true, true, false, true), run);
    }

    @Test
    @SneakyThrows
    void ifNode() {
        String[] strings = {
                "if 2 then 1 else 2",
                "if true then false else true",
                "if false then true else false",
                "if 1+2>2 then 2 else 3",
                "if {a:1,b:2,c:3}.c=3 then 3 else 333",
                "if [1..3][item>=2] = [2,3] then 2 else {a:false}",
                "{a:1,b:2.2,c:a+b,d:if c-b=a then true else 2}.d",
        };
        Object run = run(Arrays.toString(strings));
        Assertions.assertEquals(Arrays.asList(1, false, false, 2, 3, 2, true), run);
    }

    @Test
    @SneakyThrows
    void inNode() {
        String[] strings = {
                "1 in [1,2,3]",
                "1 in (1,2)",
                "[1,2] in ([1],[2],[1,2])",
                "[1,2] in ([1],[2])",
        };
        Object run = run(Arrays.toString(strings));
        Assertions.assertEquals(Arrays.asList(true, true, true, false), run);
    }

    @Test
    @SneakyThrows
    void forNode() {
        String[] strings = {
                "for a in [1,2,3],b in [2,3,4] return a+b",
                "for a in [\"a\",\"b\",\"c\"],b in [\"1\",\"2\",\"3\"] return a+b",
        };
        Object run = run(Arrays.toString(strings));
        Assertions.assertEquals(Arrays.asList(
                Arrays.asList(3, 4, 5, 4, 5, 6, 5, 6, 7),
                Arrays.asList("a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3")
        ), run);
    }

    @Test
    @SneakyThrows
    void rootInput() {
        String[] strings = {
                "c+d",
        };

        class POJO2 extends POJO {
            public POJO2(int c, int d) {
                super(c, d);
            }

            @Override
            public int getC() {
                return 99;
            }
        }

        Object run = run(Arrays.toString(strings), new POJO2(1, 2));
        Assertions.assertEquals(List.of(101), run);
    }

    @Test
    @SneakyThrows
    void ut() {
        Assertions.assertEquals(true, run(FAny.ANY, "-"));
        Assertions.assertEquals(false, run(FAny.ANY, "not ( 1, 2, 3 )", 2));
        Assertions.assertEquals(false, run(FAny.ANY, "not ( 2 )", 2));
        Assertions.assertEquals(true, run(FAny.ANY, "not ( 1, 2, 3 )", 4));
        Assertions.assertEquals(false, run(FList.of(FNumber.INTEGER), "not ( [1,2], [3,4], [1,2] )", Arrays.asList(1, 2)));
        Assertions.assertEquals(true, run(FList.of(FNumber.INTEGER), "not ( [1,2], [3,4], [1,2] )", Arrays.asList(1, 4)));
        Assertions.assertEquals(true, run(FAny.ANY, " 1, 2, 3", 3));
        Assertions.assertEquals(false, run(FAny.ANY, "2", 3));
        Assertions.assertEquals(true, run(FAny.ANY, "[1, 2, 3]", 3));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class POJO {
        private int c;
        private int d;
    }

    private static Object run(String expr, Object... inputs) {
        return run(null, expr, inputs);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Object run(FType testInputType, String expr, Object... inputs) {
        Object context;
        if (inputs.length == 0 || (testInputType != null && inputs.length == 1)) {
            context = Map.of();
        } else if (testInputType == null && inputs.length == 1) {
            context = inputs[0];
        } else {
            context = MapUtil.newLinkedHashMap(inputs.length);
            for (int i = testInputType != null ? 1 : 0; i < inputs.length; i += 2) {
                ((Map<String, Object>) context).put((String) inputs[i], inputs[i + 1]);
            }
        }

        String generate = testInputType != null ? FeelUtil.compileUT("com.muyuanjin.feel", "MyClass", "expr", expr, testInputType, context) :
                FeelUtil.compileExpr("com.muyuanjin.feel", "MyClass", "expr", expr, context);
//        System.out.println(generate);
        Path path = Path.of("E:\\JavaWorkSpace\\feel2java\\src\\test\\java\\com\\muyuanjin\\feel\\MyClass.java");
        Files.createDirectories(path.getParent());
        Files.writeString(path, generate);

        Class<?> compiledClass = JavaCompiler.NATIVE.compile("com.muyuanjin.feel.MyClass", generate).loadSingle();
        Object invoke = testInputType != null ? JMethods.invokeStatic(compiledClass, "expr", (inputs.length > 0 ? inputs[0] : null), context) :
                JMethods.invokeStatic(compiledClass, "expr", context);
        System.out.println(invoke);
        return invoke;
    }
}