package com.muyuanjin.feel.impl.infix;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.muyuanjin.feel.exception.FeelLangException;
import com.muyuanjin.feel.impl.DefaultFeelInfixOps;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.FBoolean;
import com.muyuanjin.feel.lang.type.FNumber;
import com.muyuanjin.feel.lang.type.FYearMonthDuration;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.annotation.Nullable;

import static com.muyuanjin.feel.translate.CodeGens.ifOneNullAllNull;

/**
 * @author muyuanjin
 */
public class Compare implements DefaultFeelInfixOps.InfixOp {
    public static final Compare INSTANCE = new Compare();

    @Override
    public @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op,
                                      FType leftType, FType rightType, boolean constant, FType resultType,
                                      ClassManager manager, Context context) {
        if (!op.isCompare()) {
            return null;
        }
        if (!FType.isSameType(leftType, rightType)) {
            // 不应该发生，在此之前应该返回空值
            throw new FeelLangException(n, "Cannot compile infix op node for operator: " + op + " with different type");
        }
        // <=   <    >  >=
        if (leftType instanceof FNumber) {
            BinaryExpr.Operator operator = op.toBinary();
            if (operator == null) {
                throw new FeelLangException(n, "Cannot compile infix op node for operator: " + op);
            }
            Expression leftValue = left.expr(), rightValue = right.expr();
            if (leftType == FNumber.BIG_DECIMAL && rightType == FNumber.BIG_DECIMAL) {
                return JavaExpr.of(n, new BinaryExpr(new MethodCallExpr(leftValue, "compareTo").addArgument(rightValue),
                                new IntegerLiteralExpr("0"), operator), boolean.class, constant)
                        .nullable(left.nullable(), right.nullable());
            }
            if (leftType == FNumber.BIG_DECIMAL || leftType == FNumber.NUMBER) {
                leftValue = new MethodCallExpr(leftValue, "doubleValue");
            }
            if (rightType == FNumber.BIG_DECIMAL || rightType == FNumber.NUMBER) {
                rightValue = new MethodCallExpr(rightValue, "doubleValue");
            }
            return JavaExpr.of(n, new BinaryExpr(leftValue, rightValue, operator), boolean.class, constant)
                    .nullable(left.nullable(), right.nullable());
        }
        if (leftType instanceof FBoolean) {
            Expression expr = new BinaryExpr(manager.getStaticMethod(Boolean.class, "compare")
                    .addArgument(left.expr()).addArgument(right.expr()), new IntegerLiteralExpr("0"), op.toBinary());
            return JavaExpr.of(n, ifOneNullAllNull(left, right, expr, false), boolean.class, constant)
                    .nullable(left.nullable(), right.nullable());
        }
        if (leftType instanceof FYearMonthDuration) {
            BinaryExpr expr = new BinaryExpr(new MethodCallExpr(left.expr(), "toTotalMonths"),
                    new MethodCallExpr(right.expr(), "toTotalMonths"), op.toBinary());
            return JavaExpr.of(n, ifOneNullAllNull(left, right, expr, false), boolean.class, constant)
                    .nullable(left.nullable(), right.nullable());
        }
        // use compareTo
        Expression leftValue = left.expr(), rightValue = right.expr();
        Expression compare = new MethodCallExpr(leftValue, "compareTo").addArgument(rightValue);
        BinaryExpr.Operator operator = op.toBinary();
        if (operator == null) {
            throw new FeelLangException(n, "Cannot compile infix op node for operator: " + op);
        }
        BinaryExpr binaryExpr = new BinaryExpr(compare, new IntegerLiteralExpr("0"), operator);
        return JavaExpr.of(n, ifOneNullAllNull(left, right, binaryExpr, false), boolean.class, constant)
                .nullable(left.nullable(), right.nullable());
    }
}
