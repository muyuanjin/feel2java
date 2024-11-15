package com.muyuanjin.feel;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.muyuanjin.feel.util.JSONUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;

/**
 * @author muyuanjin
 */
public class JavaAstTest {
    @Test
    void test_2024_11_14_14_18_08() {
        // 创建一个新的编译单元
        CompilationUnit compilationUnit = new CompilationUnit();
        // 在编译单元中添加一个类
        ClassOrInterfaceDeclaration myClass = compilationUnit
                .addClass("MyClass")
                .setPublic(true); // 设置类为public

        // 在类中添加一个主方法
        MethodDeclaration mainMethod = myClass.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        mainMethod.addParameter(String[].class, "args"); // 添加参数String[] args
        mainMethod.setType(void.class); // 设置返回类型为void

        // 创建一个代码块作为方法体
        BlockStmt block = new BlockStmt();

        // 向方法体中添加一个打印语句
        MethodCallExpr printCall = new MethodCallExpr(new MethodCallExpr(null, "System.out"), "println");
        printCall.addArgument("\"Hello, World!\"");
        block.addStatement(printCall);

        // 将代码块设置为方法的体
        mainMethod.setBody(block);

        // 使用JavaParser提供的toString方法打印出Java代码
        System.out.println(compilationUnit);
        System.out.println(mainMethod);
        System.out.println(block);
    }

    @Test
    void test_2024_11_14_14_23_32() {
        System.out.println(ServiceLoader.load(FeelFunctionFactory.class).stream().toList());
        System.out.println(FeelFunctionFactory.FACTORY.getFunctions());
    }

    @Test
    void test_2024_11_08_10_33_49() {
        // 创建一个新的编译单元
        CompilationUnit cu = new CompilationUnit();
        // 添加一个名为"Test"的类
        ClassOrInterfaceDeclaration testClass = cu.addClass("Test");

        // 添加两个包含相同类名但包路径不同的import语句
        cu.addImport(new ImportDeclaration("com.package1.MyClass", false, false));
        cu.addImport(new ImportDeclaration("com.package2.MyClass", false, false));

        // 在"Test"类中添加一个方法，该方法使用其中一个"MyClass"类
        MethodDeclaration method = testClass.addMethod("useMyClass");
        BlockStmt blockStmt = new BlockStmt();
        // 使用其中一个"MyClass"类的实例调用一个方法，注意这里直接使用类名
        MethodCallExpr methodCall = new MethodCallExpr(new NameExpr("com.package2.MyClass"), "doSomething");
        blockStmt.addStatement(methodCall);
        method.setBody(blockStmt); // 设置为抽象方法，不实现具体逻辑
        // 方法体中声明局部变量

        System.out.println(cu);
    }

    @Test
    void test_2024_11_09_13_26_12() {
        System.out.println(JSONUtil.toJSONString(Math.abs(NEGATIVE_INFINITY)));
        System.out.println(JSONUtil.toJSONString(Math.abs(NaN)));
        System.out.println(JSONUtil.toJSONString(Math.abs(Integer.MIN_VALUE)));
    }

    public static void main(String[] args) {
        // 初始化JavaParser
        JavaParser parser = new JavaParser();

        // 创建一个空的CompilationUnit（代表一个Java文件）
        CompilationUnit cu = new CompilationUnit();

        // 在CompilationUnit中添加一个主方法
        MethodDeclaration main = cu.addClass("Main").addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        main.addParameter(String[].class, "args");

        // 添加您的代码逻辑
        // 创建变量访问表达式
        VariableDeclarationExpr valueExpr = new VariableDeclarationExpr(PrimitiveType.intType(), "value");
        VariableDeclarationExpr startExpr = new VariableDeclarationExpr(PrimitiveType.intType(), "start");
        VariableDeclarationExpr endExpr = new VariableDeclarationExpr(PrimitiveType.intType(), "end");


        // 创建条件表达式
        BinaryExpr condition = new BinaryExpr(
                new BinaryExpr(new NameExpr("value"), new NameExpr("start"), BinaryExpr.Operator.LESS),
                new BinaryExpr(new NameExpr("value"), new NameExpr("end"), BinaryExpr.Operator.GREATER),
                BinaryExpr.Operator.OR
        );

        // 创建if语句
        IfStmt ifStmt = new IfStmt();

        ifStmt.setCondition(condition);

        // 创建变量声明，给变量赋值false
        VariableDeclarator varDeclExpr = new VariableDeclarator(PrimitiveType.booleanType(), "expr", new BooleanLiteralExpr(false));
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new AssignExpr(varDeclExpr.getNameAsExpression(), new BooleanLiteralExpr(false), AssignExpr.Operator.ASSIGN));
        ifStmt.setThenStmt(blockStmt);

        // 将创建的表达式和语句添加到主方法中
        main.getBody().get().addStatement(valueExpr);
        main.getBody().get().addStatement(startExpr);
        main.getBody().get().addStatement(endExpr);
        main.getBody().get().addStatement(new VariableDeclarationExpr(varDeclExpr));
        main.getBody().get().addStatement(ifStmt);


        // 输出生成的代码
        System.out.println(cu);
    }

    @Test
    @SneakyThrows
    void test_2024_11_13_11_06_52() {
        for (int i = 0; i < 10; i++) {
            if (i == 4) {
                throw sneakyThrows(new Throwable("hhh"));
            }
        }
    }


    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneakyThrows(Throwable throwable) throws T {
        throw (T) throwable;
    }
}
