# 本项目仍在开发🚧 This project is still under development

## 工作计划 Work Plan
- [x] 实现基本的FEEL解析 Implement basic FEEL parsing
- [x] 实现基本的FEEL编译 Implement basic FEEL compilation
- [ ] 清理整合代码 Clean up and integrate code
- [ ] 重构IR优化 Refactor IR optimization
- [ ] 实现FEEL的SDK Implement FEEL SDK
- [ ] 完善组件使用 Improve component usage

## FEEL to Java
What is FEEL?  
Friendly Enough Expression Language (FEEL) 足够友好的表达式语言

本项目用于将 FEEL 翻译为可执行的 Java 代码

## 使用 Instructions:

## 1.编译 FEEL 表达式 Compile FEEL expression

```java
Feel.Expression<Map<String, Object>> compile = Feel.compile("1+1");
EvalResult eval = compile.eval(null);

assertEquals(2, eval.asNumber());

Feel.Expression<Map<String, Integer>> compile1 = Feel.compile("a+1", Map.of("a", 1));
EvalResult eval1 = compile1.eval(Map.of("a", 1));

assertEquals(2, eval1.asNumber());
```

```java
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
Assertions.assertEquals(source.replaceAll("\n", System.lineSeparator()),compiled);
Class<Object> loaded = JavaCompiler.NATIVE.compile("TestExpr.java", compiled).loadSingle();
Object o = JMethods.invokeStatic(loaded, "eval", Map.of());
Assertions.assertEquals(1+1, o);
```

## 2.编译 FEEL 一元测试 Compile FEEL unary test

```java
Feel.UnaryTest<Integer, Map<String, Object>> unaryTest = Feel.compileUT("?>1", Integer.class);

assertTrue(unaryTest.test(2, null));

Feel.UnaryTest<Integer, Map<String, Integer>> unaryTest1 = Feel.compileUT("a>1", null, Map.of("a", 2));

assertTrue(unaryTest1.test(null, Map.of("a", 2)));

Feel.UnaryTest<Integer, Map<String, Integer>> unaryTest2 = Feel.compileUT("?<a", 1, Map.of("a", 2));

assertTrue(unaryTest2.test(1, Map.of("a", 2)));
```

```java
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
Assertions.assertEquals(source.replaceAll("\n", System.lineSeparator()),compiled);
Class<Object> loaded = JavaCompiler.NATIVE.compile("TestUT.java", compiled).loadSingle();
Object o = JMethods.invokeStatic(loaded, "eval", 2, Map.of());
Assertions.assertEquals(true, o);
```

## 3.编译决策表 Compilation decision table:

```java
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

Assertions.assertEquals(definition, table.getDefinition());
Assertions.assertEquals("hello word",table.evaluate(new POJO(1, 2)).asString());
Assertions.assertEquals("hello word 2",table.evaluate(new POJO(1, 1)).asString());
```

## 构建 Build

1. 运行 `mvn clean compile` 或 `mvn clean antlr4:antlr4` 来生成antlr4类
2. 右键`target/generated-sources/antlr4`标记为`生成的源代码根目录`