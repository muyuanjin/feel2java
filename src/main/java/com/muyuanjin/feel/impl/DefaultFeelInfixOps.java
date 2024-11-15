package com.muyuanjin.feel.impl;

import com.muyuanjin.feel.impl.infix.*;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.FNull;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author muyuanjin
 */
public class DefaultFeelInfixOps {
    private static final List<InfixOp> OPS = List.of(
            EqNe.INSTANCE,
            AndOr.INSTANCE,
            Compare.INSTANCE,
            NumberCalc.INSTANCE,
            StringAddOp.INSTANCE
    );

    public static @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op, Context context) {
        FType leftType = left.feelType();
        FType rightType = right.feelType();
        boolean constant = left.constant() && right.constant();
        FType resultType = op.calculate(leftType, rightType);
        if (resultType instanceof FNull) {
            return JavaExpr.ofNull(n);
        }
        ClassManager manager = ClassManager.instance(context);
        for (InfixOp infixOp : OPS) {
            JavaExpr typedExpr = infixOp.infixOp(n, left, right, op,
                    leftType, rightType, constant, resultType, manager, context);
            if (typedExpr != null) {
                return typedExpr;
            }
        }
        return null;
    }

    public interface InfixOp {
        @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op,
                                   FType leftType, FType rightType, boolean constant, FType resultType,
                                   ClassManager manager, Context context);
    }
}