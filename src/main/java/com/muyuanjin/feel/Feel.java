package com.muyuanjin.feel;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.muyuanjin.common.util.DigestUtil;
import com.muyuanjin.common.util.LazyLog;
import com.muyuanjin.compiler.CompilationResult;
import com.muyuanjin.compiler.JavaCompiler;
import com.muyuanjin.compiler.util.JUnsafe;
import com.muyuanjin.compiler.util.Throws;
import com.muyuanjin.feel.dmn.DecisionTable;
import com.muyuanjin.feel.dmn.DecisionTableDefinition;
import com.muyuanjin.feel.dmn.EvalResult;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.translate.*;
import com.muyuanjin.feel.util.FeelUtil;
import de.fxlae.typeid.TypeId;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author muyuanjin
 */
@UtilityClass
public class Feel {
    public interface Expression<I> {
        EvalResult eval(I input);
    }

    public interface UnaryTest<T, I> {
        boolean test(T testInput, I input);
    }

    public static Expression<Map<String, Object>> compile(String expression) {
        return cachedCompile(expression, Map.of());
    }

    public static <T> Expression<T> compile(String expression, T input) {
        return cachedCompile(expression, input);
    }

    public static <T> Expression<T> compile(String expression, FType inputFeelType) {
        return cachedCompile(expression, inputFeelType);
    }

    public static <T> Expression<T> compile(String expression, Class<T> inputType) {
        return cachedCompile(expression, inputType);
    }

    public static <T> Expression<T> compile(String expression, Type inputType) {
        return cachedCompile(expression, inputType);
    }

    public static <T> UnaryTest<T, Map<String, Object>> compileUT(String expression, FType inputFeelType) {
        return cachedCompileUT(expression, inputFeelType, Map.of());
    }

    public static <T> UnaryTest<T, Map<String, Object>> compileUT(String expression, T testInput) {
        return cachedCompileUT(expression, testInput, Map.of());
    }

    public static <T> UnaryTest<T, Map<String, Object>> compileUT(String expression, Class<T> testInputType) {
        return cachedCompileUT(expression, testInputType, Map.of());
    }

    public static <T> UnaryTest<T, Map<String, Object>> compileUT(String expression, Type testInputType) {
        return cachedCompileUT(expression, testInputType, Map.of());
    }

    public static <T, I> UnaryTest<T, I> compileUT(String expression, T testInput, I input) {
        return cachedCompileUT(expression, testInput, input);
    }

    public static <T, I> UnaryTest<T, I> compileUT(String expression, FType testInputFeelType, FType inputFeelType) {
        return cachedCompileUT(expression, testInputFeelType, inputFeelType);
    }

    public static <T, I> UnaryTest<T, I> compileUT(String expression, Class<T> testInputType, Class<I> inputType) {
        return cachedCompileUT(expression, testInputType, inputType);
    }

    public static <T, I> UnaryTest<T, I> compileUT(String expression, Type testInputType, Type inputType) {
        return cachedCompileUT(expression, testInputType, inputType);
    }

    public static DecisionTable<Map<String, Object>> compileDMN(DecisionTableDefinition definition) {
        return cachedCompileDMN(definition, Map.of());
    }

    public static <T> DecisionTable<T> compileDMN(DecisionTableDefinition definition, FType inputFeelType) {
        return cachedCompileDMN(definition, inputFeelType);
    }

    public static <T> DecisionTable<T> compileDMN(DecisionTableDefinition definition, T input) {
        return cachedCompileDMN(definition, input);
    }

    public static <T> DecisionTable<T> compileDMN(DecisionTableDefinition definition, Class<T> inputType) {
        return cachedCompileDMN(definition, inputType);
    }

    public static <T> DecisionTable<T> compileDMN(DecisionTableDefinition definition, Type inputType) {
        return cachedCompileDMN(definition, inputType);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static <T> Expression<T> cachedCompile(String expression, Object inputObjOrInputJavaType) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        CacheKey cacheKey = new CacheKey(expression.length() > 64 ? DigestUtil.sha256Hex(expression) : expression, inputObjOrInputJavaType);
        return (Expression<T>) JUnsafe.UNSAFE.allocateInstance(CACHE.get(cacheKey, key -> doCompile(expression, key.inputFType)));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static <T, I> UnaryTest<T, I> cachedCompileUT(String expression, Object testInputObjOrInputJavaType, Object rootInputObjOrInputJavaType) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        CacheKey cacheKey = new CacheKey(expression.length() > 64 ? DigestUtil.sha256Hex(expression) : expression, testInputObjOrInputJavaType, rootInputObjOrInputJavaType);
        return (UnaryTest<T, I>) JUnsafe.UNSAFE.allocateInstance(CACHE.get(cacheKey, key -> doCompileUT(expression, key.testInputFType, key.inputFType)));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static <T> DecisionTable<T> cachedCompileDMN(DecisionTableDefinition definition, Object rootInputObjOrInputJavaType) {
        if (definition == null) {
            return null;
        }
        String string = definition.toString();
        CacheKey cacheKey = new CacheKey(string.length() > 64 ? DigestUtil.sha256Hex(string) : string, rootInputObjOrInputJavaType);
        return (DecisionTable<T>) JUnsafe.UNSAFE.allocateInstance(CACHE.get(cacheKey, key -> doCompilerDMN(definition, key.inputFType)));
    }

    private static Class<?> doCompile(String expression, Object inputObjOrInputJavaType) {
        CompilerTask task = new CompilerTask()
                .packageName(PACKAGE_NAME)
                .className("Feel" + "$" + TypeId.generate())
                .methodName("doEval")
                .expression(expression)
                .rootInput(inputObjOrInputJavaType);
        ASTNode astNode = FeelUtil.parseExpr2AST(expression, task.inputTypes());
        Context context = task.context();
        ASTCompilerVisitor.instance(context).visit(astNode);
        ClassManager manager = ClassManager.instance(context);
        var classDeclaration = manager.getClassDeclaration();
        classDeclaration.addImplementedType(manager.getClassType(Expression.class)
                .setTypeArguments(manager.getClassType(task.rootInputJavaType())));
        var eval = new MethodDeclaration(NodeList.nodeList(Modifier.publicModifier()), manager.getClassType(EvalResult.class), "eval");
        NameExpr param = new NameExpr(task.rootInputParam());
        eval.addParameter(new Parameter(manager.getClassType(task.rootInputJavaType()), param.getName()));
        eval.addAnnotation(new MarkerAnnotationExpr("Override"));
        BlockStmt evalBody = new BlockStmt();
        eval.setBody(evalBody);
        evalBody.addStatement(new ReturnStmt(manager.getStaticMethod(EvalResult.class, "of")
                .addArgument(new MethodCallExpr(null, task.methodName()).addArgument(param))));
        return getClass(task, manager, classDeclaration, eval);
    }

    private static Class<?> doCompileUT(String expression, Object testInputObjOrInputJavaType, Object rootInputObjOrInputJavaType) {
        CompilerTask task = new CompilerTask()
                .packageName(PACKAGE_NAME)
                .className("FeelUT" + "$" + TypeId.generate())
                .methodName("doTest")
                .expression(expression)
                .unaryTestInput(testInputObjOrInputJavaType)
                .rootInput(rootInputObjOrInputJavaType);
        ASTNode astNode = FeelUtil.parseUT2AST(expression, task.unaryTestInputType(), task.inputTypes());
        Context context = task.context();
        ASTCompilerVisitor.instance(context).visit(astNode);
        ClassManager manager = ClassManager.instance(context);
        var classDeclaration = manager.getClassDeclaration();
        classDeclaration.addImplementedType(manager.getClassType(UnaryTest.class)
                .setTypeArguments(manager.getClassType(task.unaryTestInputJavaType()), manager.getClassType(task.rootInputJavaType())));
        var eval = new MethodDeclaration(NodeList.nodeList(Modifier.publicModifier()), PrimitiveType.booleanType(), "test");
        NameExpr testParam = new NameExpr(task.unaryTestInputParam());
        NameExpr inputParam = new NameExpr(task.rootInputParam());
        eval.addParameter(new Parameter(manager.getClassType(task.unaryTestInputJavaType()), testParam.getName()));
        eval.addParameter(new Parameter(manager.getClassType(task.rootInputJavaType()), inputParam.getName()));
        eval.addAnnotation(new MarkerAnnotationExpr("Override"));
        BlockStmt evalBody = new BlockStmt();
        eval.setBody(evalBody);
        evalBody.addStatement(new ReturnStmt(new MethodCallExpr(null, task.methodName())
                .addArgument(testParam).addArgument(inputParam)));
        return getClass(task, manager, classDeclaration, eval);
    }

    private static Class<?> doCompilerDMN(DecisionTableDefinition definition, Object rootInputObjOrInputJavaType) {
        CompilerTask compilerTask = new CompilerTask().packageName(PACKAGE_NAME)
                .className("FeelDMN" + "$" + TypeId.generate())
                .rootInput(rootInputObjOrInputJavaType);

        String generate = DMNGenerator.instance(compilerTask.context()).generate(definition);
        try {
            CompilationResult compile = JavaCompiler.NATIVE.compile(compilerTask.className(), generate);
            return compile.classes().size() == 1 ? compile.loadSingle() :
                    compile.load(compilerTask.packageName() + "." + compilerTask.className());
        } catch (Exception e) {
            log.error("compile error: {}", generate);
            throw e;
        }
    }

    private static Class<?> getClass(CompilerTask task, ClassManager manager, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration eval) {
        classDeclaration.getMembers().add(0, eval);
        String generate = manager.generate();
        if (log.isDebugEnabled()) {
            log.debug("generate task:{} \n{}", task, generate);
        }
        try {
            CompilationResult compile = JavaCompiler.NATIVE.compile(task.className(), generate);
            return compile.classes().size() == 1 ? compile.loadSingle() :
                    compile.load(task.packageName() + "." + task.className());
        } catch (Exception e) {
            log.error("compile error: {}", generate);
            throw Throws.sneakyThrows(e);
        }
    }

    private static final Cache<CacheKey, Class<?>> CACHE = Caffeine.newBuilder().weakValues().build();
    private static final LazyLog log = LazyLog.of(Feel.class);
    private static final String PACKAGE_NAME = "runtime.feel";

    private record CacheKey(String string, FType testInputFType, FType inputFType) {
        private CacheKey(String string, Object inputObjOrInputJavaType) {
            this(string, null, toType(inputObjOrInputJavaType));
        }

        private CacheKey(String string, Object testInputObjOrInputJavaType, Object rootInputObjOrInputJavaType) {
            this(string, toType(testInputObjOrInputJavaType), toType(rootInputObjOrInputJavaType));
        }

        private static FType toType(Object obj) {
            return obj instanceof Type type ? FType.of(type) : FType.of(obj);
        }
    }
}