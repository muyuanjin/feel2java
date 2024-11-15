package com.muyuanjin.feel.impl.infix;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.muyuanjin.feel.impl.DefaultFeelInfixOps;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.FNumber;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * @author muyuanjin
 */
public class EqNe implements DefaultFeelInfixOps.InfixOp {
    public static final EqNe INSTANCE = new EqNe();

    @Override
    public @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op,
                                      FType leftType, FType rightType, boolean constant, FType resultType,
                                      ClassManager manager, Context context) {
        if (!op.isEqNe()) {
            return null;
        }
        Expression leftEx = left.expr(), rightEx = right.expr();
        if (leftEx.equals(rightEx)) {
            return JavaExpr.ofCon(n, new BooleanLiteralExpr(op == InfixOpNode.Op.EQ), boolean.class);
        }
        if (left.primitive() && right.primitive()) {
            return JavaExpr.of(n, new BinaryExpr(leftEx, rightEx, op.toBinary()), boolean.class, constant);
        }
        if (leftType instanceof FNumber) {
            // use Compare
            return null;
        }

        // Objects.equals
        Expression equals = manager.getStaticMethod(Objects.class, "equals")
                .addArgument(left.expr()).addArgument(right.expr());
        if (op == InfixOpNode.Op.NE) {
            equals = new UnaryExpr(equals, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        }
        return JavaExpr.of(n, equals, boolean.class, constant);
    }
}