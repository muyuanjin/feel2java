package com.muyuanjin.feel.impl.infix;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.exception.FeelLangException;
import com.muyuanjin.feel.impl.DefaultFeelInfixOps;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.FNumber;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.annotation.Nullable;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.BiFunction;

/**
 * @author muyuanjin
 */
public class NumberCalc implements DefaultFeelInfixOps.InfixOp {
    public static final NumberCalc INSTANCE = new NumberCalc();

    @Override
    public @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op,
                                      FType leftType, FType rightType, boolean constant, FType resultType,
                                      ClassManager manager, Context context) {
        if (!op.isCalc()) {
            return null;
        }
        Type resultJavaType = resultType.getJavaType();

        BiFunction<JavaExpr, FType, Expression> numberValue =
                (expr, type) -> {
                    JavaExpr convert = FeelTypeFactory.instance(context).convert(expr, type, context);
                    if (convert == null) {
                        return null;
                    }
                    if (convert == expr || convert.isSimple()) {
                        return convert.expr();
                    }
                    if (convert.constant()) {
                        // can be static
                        Type javaType = type.getJavaType();
                        return manager.addStaticField(new VariableDeclarator(manager.getType(javaType), type.getName())
                                .setInitializer(convert.expr()), javaType, convert.expr().toString());
                    }
                    return convert.expr();
                };

        if (resultType instanceof FNumber) {
            if (resultJavaType == BigDecimal.class || resultJavaType == Number.class || op == InfixOpNode.Op.DIV) {
                Expression leftValue = numberValue.apply(left, FNumber.BIG_DECIMAL);
                if (leftValue == null) {
                    return JavaExpr.ofNull(n);
                }
                if (op == InfixOpNode.Op.POW) {
                    Expression rightValue = numberValue.apply(right, FNumber.INTEGER);
                    if (rightValue == null) {
                        return JavaExpr.ofNull(n);
                    }
                    return JavaExpr.of(n, new MethodCallExpr(leftValue, "pow").addArgument(rightValue), BigDecimal.class, constant);
                }
                Expression rightValue = numberValue.apply(right, FNumber.BIG_DECIMAL);
                if (rightValue == null) {
                    return JavaExpr.ofNull(n);
                }
                switch (op) {
                    case ADD -> {
                        //TODO constant 优化 如果左右都是 NumberNode 或者 NegationNode-NumberNode 直接化简
                        return JavaExpr.of(n, new MethodCallExpr(leftValue, "add").addArgument(rightValue), BigDecimal.class, constant);
                    }
                    case SUB -> {
                        return JavaExpr.of(n, new MethodCallExpr(leftValue, "subtract").addArgument(rightValue), BigDecimal.class, constant);
                    }
                    case DIV -> {
                        return JavaExpr.of(n, new MethodCallExpr(leftValue, "divide").addArgument(rightValue)
                                        .addArgument(manager.getStaticField(MathContext.class, "DECIMAL128"))
                                , BigDecimal.class, constant);
                    }
                    case MUL -> {
                        return JavaExpr.of(n, new MethodCallExpr(leftValue, "multiply").addArgument(rightValue), BigDecimal.class, constant);
                    }
                    default -> throw new FeelLangException(n, "Cannot compile infix op node for operator: " + op);
                }
            } else {
                Expression leftValue = numberValue.apply(left, resultType);
                Expression rightValue = numberValue.apply(right, resultType);
                switch (op) {
                    case ADD -> {
                        return JavaExpr.of(n, new BinaryExpr(leftValue, rightValue, BinaryExpr.Operator.PLUS), resultType.getJavaType(), constant)
                                .feelType(resultType);
                    }
                    case SUB -> {
                        return JavaExpr.of(n, new BinaryExpr(leftValue, rightValue, BinaryExpr.Operator.MINUS), resultType.getJavaType(), constant)
                                .feelType(resultType);
                    }
                    case MUL -> {
                        return JavaExpr.of(n, new BinaryExpr(leftValue, rightValue, BinaryExpr.Operator.MULTIPLY), resultType.getJavaType(), constant)
                                .feelType(resultType);
                    }
                    case POW -> {
                        return JavaExpr.of(n, manager.getStaticMethod(Math.class, "pow")
                                        .addArgument(leftValue).addArgument(rightValue), double.class, constant)
                                .feelType(resultType);
                    }
                    default -> throw new FeelLangException(n, "Cannot compile infix op node for operator: " + op);
                }
                //TODO
            }
        }
        //TODO
        return null;
    }
}
