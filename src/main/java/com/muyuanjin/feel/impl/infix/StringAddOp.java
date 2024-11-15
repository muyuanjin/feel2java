package com.muyuanjin.feel.impl.infix;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.muyuanjin.feel.impl.DefaultFeelInfixOps;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.FString;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.CodeGens;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.annotation.Nullable;

/**
 * @author muyuanjin
 */
public class StringAddOp implements DefaultFeelInfixOps.InfixOp {
    public static final StringAddOp INSTANCE = new StringAddOp();

    @Override
    public @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op,
                                      FType leftType, FType rightType, boolean constant, FType resultType,
                                      ClassManager manager, Context context) {
        // 字符串拼接
        if (op == InfixOpNode.Op.ADD && (leftType instanceof FString || rightType instanceof FString)) {
            Expression leftValue = left.expr();
            Expression rightValue = right.expr();
            return JavaExpr.of(n, CodeGens.ifOneNullAllNull(left, right, new BinaryExpr(leftValue, rightValue, BinaryExpr.Operator.PLUS), false), String.class, constant)
                    .feelType(FString.STRING).nullable(left.nullable(), right.nullable());
        }
        return null;
    }
}