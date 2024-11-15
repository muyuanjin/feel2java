package com.muyuanjin.feel.parser;

import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.FAny;
import com.muyuanjin.feel.lang.type.FContext;
import com.muyuanjin.feel.lang.type.FList;
import com.muyuanjin.feel.parser.antlr4.FEELLexer;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import com.muyuanjin.feel.util.BenchmarkUtil;
import kotlin.Pair;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.muyuanjin.feel.lang.FTypes.*;

/**
 * @author muyuanjin
 */
@SuppressWarnings("UnusedReturnValue")
class BenchmarkTest {

    @Test
    void test_2024_11_14_08_38_54() {
        p("\"a\\u4e2d\\u6587b\"");
        p("@\"-PT5H\"");
        p("@\"P2Y2M\"");
        p("x instance of null");
        p("{get:122,result:321}");
        p("{get:122,result:321}.result");
        p("{get:122,result:\"321\"}.result");
        p("{result:date(1,2,3)}.result");
        p("{result:date and time(\"123\")}.result");
        p("function (a:number,b:function<number,number>->boolean) b(a,a+1)");
        p("{get:function (a:number,b:function<number,number>->boolean) b(a,a+1),result:get(12,function (x,y) x+1=y )}.result");
        p("1");
        p("(1)");
        p("2.1");
        p("(2.1)");
        p("x", of("x", FContext.of(of("1", NUMBER), of("4", NUMBER))));
        p("x._1", of("x", FContext.of(of("_1", FList.of(STRING)), of("4", NUMBER))));
        p("x._1[1]", of("x", FContext.of(of("_1", FList.of(STRING)), of("4", NUMBER))));
        p("<2");
        p("<=2");
        p(">@\"-PT5H\"");
        p(">=2");
        p("[1..2]");
        p("[1..2.3)");
        p("(1..2]");
        p("({result:date and time(\"123\")}.result..date(1,2,3))");
        p("a+++b+b+c", of("a++", INTEGER), of("b", INTEGER), of("c", DOUBLE));
        p("a+++b+b+c", of("a", DOUBLE), of("a+++b+b", INTEGER), of("c", INTEGER));
        p("{\"abb+c\":2,abb:3,c:4.2,d:abb+c}.d");
        p("{\"abb+c\":2,abb:3,c:4.2,d:abb + c}.d");
        p("{复杂:{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}}");
        p("{复杂:{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}}.复杂", of("c", DOUBLE));
        p("{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}.abb+c", of("c", DOUBLE));
        p("{abb:3,c:4,d:abb+c,abb+c:文字}.d", of("文字", DATE));
        p("{abb+c:文字,abb:3,c:4,d:abb+c}.d", of("文字", DATE));
        p("{abb+c:文字,abb:3,c:4,d:abb +c}.d", of("文字", DATE));
        p("{getName:function (person:context<name:string,age:number>) person.name,person:{name:\"小明\",age:12},result:getName(person)}.result");
        p("{get:function (a:number,b:function<number,number>->boolean) b(a,a+1),result:get(12,function (x,y) x+1=y )}.result");
        p("sort(1,2,3)");
        p("sort(1,2,3.2)");
        p("sort(\"1\",\"2\",\"3.2\")");
        p("sort(a,function(x,y) if x instance of null then true else false)", of("a", FList.of(FAny.ANY)));
        p("sort(a,function(x,y) if count(x)>7 then true else false)", of("a", FList.of(FList.of(STRING))));
        p("sort([1,2,3,4],function(x,y) x>y)");
        p("abs(1)");
        p("abs(@\"PT5H\")");
        p("abs(@\"P2Y2M\")");
        p("{abs:-2,b:abs(abs)}.b");
        p("{count:\"文字\",b:count([1,2,3])}.b");
        p("a//注释\n+b");
        p("a//注释\n+b//注释");
        p("a//注释\n+b//注释\n");
        p("a//注释\n+b//注释\n//注释");
        p("a/*注释*/\n+b");
        p("a/*注释\n注释*/\n+b");
        p("a/*注释\n注释*/\n+b/*注释\n注释*/");
        ut("1 between 0 and 3");
    }

    @Test
    void benchmark() {
        BenchmarkUtil.benchmark(100, 1000, this::test_2024_11_14_08_38_54);
    }

    @Test
    void test_2024_11_14_10_11_16() {
        BenchmarkUtil.benchmark1000(() -> p("{get:122,result:321}.result"));
        BenchmarkUtil.benchmark1000(() -> p("sort(a,function(x,y) if count(x)>7 then true else false)", of("a", FList.of(FList.of(STRING)))));
    }

    @Test
    void test_2024_11_14_09_10_34() {
        CharStream input = CharStreams.fromString("3 + 4 * 5 +/* hhh */ + // 222");
        FEELLexer lexer = new FEELLexer(input);
        List<Token> strings = new ArrayList<>();
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
            strings.add(token);
        }
        System.out.println(strings);
    }

    private static Pair<String, FType> of(String s, FType type) {
        return new Pair<>(s, type);
    }

    @SafeVarargs
    private static ParseTree p(String expression, Pair<String, FType>... inputTypes) {
        return p(expression, true, inputTypes);
    }

    @SafeVarargs
    private static ParseTree ut(String expression, Pair<String, FType>... inputTypes) {
        return p(expression, false, inputTypes);
    }

    @SafeVarargs
    private static ParseTree p(String expression, boolean isExpr, Pair<String, FType>... inputTypes) {
        var lexer = new FEELLexer(CharStreams.fromString(expression));
        var parser = new FEELParser(new CommonTokenStream(lexer));

        lexer.setInputStream(CharStreams.fromString(expression));
        parser.setTokenStream(new CommonTokenStream(lexer));
        for (Pair<String, FType> context : inputTypes) {
            parser.getHelper().defineGlobalVar(context.getFirst(), context.getSecond());
        }

        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        ParseTree tree;
        try {
            tree = isExpr ? parser.expressionUnit() : parser.unaryTests();
        } catch (ParseCancellationException ex) {
            lexer.reset();
            parser.reset();
            //重新使用标准的错误监听器和错误处理器
            parser.addErrorListener(ConsoleErrorListener.INSTANCE);
            parser.setErrorHandler(new DefaultErrorStrategy());
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);//尝试全功能的LL( *)
            tree = isExpr ? parser.expressionUnit() : parser.unaryTests();
        }
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new RuntimeException("语法错误");
        }
        return tree;
    }
}