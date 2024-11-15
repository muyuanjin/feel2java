package com.muyuanjin.feel.util;

import com.muyuanjin.common.entity.Pair;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FAny;
import com.muyuanjin.feel.parser.FeelASTBuilderVisitor;
import com.muyuanjin.feel.parser.FeelThrowErrorListener;
import com.muyuanjin.feel.parser.antlr4.FEELLexer;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import com.muyuanjin.feel.parser.symtab.ParserHelper;
import com.muyuanjin.feel.translate.ASTCompilerVisitor;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.CompilerTask;
import com.muyuanjin.feel.translate.Context;
import lombok.experimental.UtilityClass;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

/**
 * @author muyuanjin
 */
@UtilityClass
public class FeelUtil {
    private static final ThreadLocal<ParserHelper> PARSER_HELPER = ThreadLocal.withInitial(ParserHelper::new);

    public static ParseTree parseExpr(String expression) {
        return parse(expression, null, Collections.emptyList());
    }

    public static ParseTree parseExpr(String expression, Map<String, FType> inputTypes) {
        return parse(expression, null, inputTypes.entrySet());
    }

    @SafeVarargs
    public static ParseTree parseExpr(String expression, Pair<String, FType>... inputTypes) {
        return parse(expression, null, Arrays.asList(inputTypes));
    }

    public static ParseTree parseUT(String expression, FType testInputType) {
        return parse(expression, Objects.requireNonNullElse(testInputType, FAny.ANY), Collections.emptyList());
    }

    public static ParseTree parseUT(String expression, FType testInputType, Map<String, FType> inputTypes) {
        return parse(expression, Objects.requireNonNullElse(testInputType, FAny.ANY), inputTypes.entrySet());
    }

    @SafeVarargs
    public static ParseTree parseUT(String expression, FType testInputType, Pair<String, FType>... inputTypes) {
        return parse(expression, Objects.requireNonNullElse(testInputType, FAny.ANY), Arrays.asList(inputTypes));
    }

    /*
     *
     *
     */
    public static ASTNode parseExpr2AST(String expression) {
        return parse2AST(expression, null, Collections.emptyList());
    }

    public static ASTNode parseExpr2AST(String expression, Map<String, FType> inputTypes) {
        return parse2AST(expression, null, inputTypes.entrySet());
    }

    @SafeVarargs
    public static ASTNode parseExpr2AST(String expression, Pair<String, FType>... inputTypes) {
        return parse2AST(expression, null, Arrays.asList(inputTypes));
    }

    public static ASTNode parseUT2AST(String expression, FType testInputType) {
        return parse2AST(expression, Objects.requireNonNullElse(testInputType, FAny.ANY), Collections.emptyList());
    }

    public static ASTNode parseUT2AST(String expression, FType testInputType, Map<String, FType> inputTypes) {
        return parse2AST(expression, Objects.requireNonNullElse(testInputType, FAny.ANY), inputTypes.entrySet());
    }

    @SafeVarargs
    public static ASTNode parseUT2AST(String expression, FType testInputType, Pair<String, FType>... inputTypes) {
        return parse2AST(expression, Objects.requireNonNullElse(testInputType, FAny.ANY), Arrays.asList(inputTypes));
    }

    /*
     *
     *
     */
    public static String compileExpr(String packageName, String className, String methodName, String expression) {
        return compileExpr(packageName, className, methodName, expression, Collections.emptyMap());
    }

    @SafeVarargs
    public static String compileExpr(String packageName, String className, String methodName, String expression, Pair<String, FType>... inputTypes) {
        return compileExpr(packageName, className, methodName, expression, Map.ofEntries(inputTypes));
    }

    public static String compileExpr(String packageName, String className, String methodName, String expression, Map<String, FType> inputTypes) {
        return doCompile(new CompilerTask()
                .packageName(packageName)
                .className(className)
                .methodName(methodName)
                .expression(expression)
                .inputTypes(inputTypes));
    }

    public static String compileExpr(String packageName, String className, String methodName,
                                     String expression, Object inputObjOrInputJavaType) {
        return doCompile(new CompilerTask()
                .packageName(packageName)
                .className(className)
                .methodName(methodName)
                .expression(expression)
                .rootInput(inputObjOrInputJavaType));
    }

    public static String compileUT(String packageName, String className, String methodName, String expression, FType testInputType) {
        return compileUT(packageName, className, methodName, expression, testInputType, Collections.emptyMap());
    }

    @SafeVarargs
    public static String compileUT(String packageName, String className, String methodName,
                                   String expression, FType testInputType, Pair<String, FType>... inputTypes) {
        return compileUT(packageName, className, methodName, expression, Objects.requireNonNullElse(testInputType, FAny.ANY), Arrays.asList(inputTypes));
    }

    public static String compileUT(String packageName, String className, String methodName,
                                   String expression, FType testInputType, Map<String, FType> inputTypes) {
        testInputType = Objects.requireNonNullElse(testInputType, FAny.ANY);
        return doCompile(new CompilerTask()
                .asFeelExpr(false)
                .packageName(packageName)
                .className(className)
                .methodName(methodName)
                .expression(expression)
                .unaryTestInputType(testInputType)
                .unaryTestInputJavaType(testInputType.getJavaType())
                .inputTypes(inputTypes));
    }

    public static String compileUT(String packageName, String className, String methodName,
                                   String expression, FType testInputType, Object inputObjOrInputJavaType) {
        testInputType = Objects.requireNonNullElse(testInputType, FAny.ANY);
        return doCompile(new CompilerTask()
                .asFeelExpr(false)
                .packageName(packageName)
                .className(className)
                .methodName(methodName)
                .expression(expression)
                .unaryTestInputType(testInputType)
                .unaryTestInputJavaType(testInputType.getJavaType())
                .rootInput(inputObjOrInputJavaType));
    }

    private static String doCompile(CompilerTask task) {
        ASTNode astNode = parse2AST(task.expression(), task.asFeelExpr() ? null : Objects.requireNonNullElse(task.unaryTestInputType(), FAny.ANY), task.inputTypes().entrySet());
        Context context = task.context();
        ASTCompilerVisitor.instance(context).visit(astNode);
        return ClassManager.instance(context).generate();
    }

    /*
     *
     */
    private static ParseTree parse(String expression, FType testInputType, Collection<? extends Map.Entry<String, FType>> inputTypes) {
        FEELLexer lexer = getFEELLexer(expression);
        return doParse(lexer, new CommonTokenStream(lexer), testInputType, inputTypes);
    }

    private static ASTNode parse2AST(String expression, FType testInputType, Collection<? extends Map.Entry<String, FType>> inputTypes) {
        FEELLexer lexer = getFEELLexer(expression);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ParseTree parse = doParse(lexer, tokenStream, testInputType, inputTypes);
        return parse.accept(new FeelASTBuilderVisitor(tokenStream));
    }

    private static FEELLexer getFEELLexer(String expression) {
        var lexer = new FEELLexer(CharStreams.fromString(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(FeelThrowErrorListener.INSTANCE);
        return lexer;
    }

    private static ParseTree doParse(FEELLexer lexer, TokenStream tokenStream, FType testInputType, Collection<? extends Map.Entry<String, FType>> inputTypes) {
        // FEELParser 不能复用，会导致解析错误
        var parser = new FEELParser(tokenStream, PARSER_HELPER.get());
        for (Map.Entry<String, FType> entry : inputTypes) {
            parser.getHelper().defineGlobalVar(entry.getKey(), entry.getValue());
        }
        if (testInputType != null) {
            parser.getHelper().defineGlobalVar("?", testInputType);
        }
        boolean isExpr = testInputType == null;
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        ParseTree tree;
        try {
            tree = isExpr ? parser.expressionUnit() : parser.unaryTests();
        } catch (ParseCancellationException ex) {
            lexer.reset();//回滚输入流
            parser.reset();
            //重新使用标准的错误监听器和错误处理器
            parser.addErrorListener(FeelThrowErrorListener.INSTANCE);
            parser.setErrorHandler(new DefaultErrorStrategy());
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);//尝试全功能的LL( *)
            tree = isExpr ? parser.expressionUnit() : parser.unaryTests();
        }
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new IllegalArgumentException("Syntax error in expression");
        }
        return tree;
    }
}