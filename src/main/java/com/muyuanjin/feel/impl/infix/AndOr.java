package com.muyuanjin.feel.impl.infix;

import com.github.javaparser.ast.expr.*;
import com.muyuanjin.feel.impl.DefaultFeelInfixOps;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.FBoolean;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.annotation.Nullable;

import static com.muyuanjin.feel.translate.CodeGens.ifOneNullAllNull;

/**
 * @author muyuanjin
 */
public class AndOr implements DefaultFeelInfixOps.InfixOp {
    public static final AndOr INSTANCE = new AndOr();

    @Override
    public @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op,
                                      FType leftType, FType rightType, boolean constant, FType resultType,
                                      ClassManager manager, Context context) {
        if (!op.isAndOr()) {
            return null;
        }

        /*
         * Table 50: Semantics of conjunction and disjunction
         *
         * | a         | b         | a and b  | a or b  |
         * |-----------|-----------|----------|---------|
         * | true      | true      | true     | true    |
         * | true      | false     | false    | true    |
         * | true      | otherwise | null     | true    |
         * | false     | true      | false    | true    |
         * | false     | false     | false    | false   |
         * | false     | otherwise | false    | null    |
         * | otherwise | true      | null     | true    |
         * | otherwise | false     | false    | null    |
         * | otherwise | otherwise | null     | null    |
         */
        boolean isAnd = op == InfixOpNode.Op.AND;
        Expression leftEx = left.expr(), rightEx = right.expr();
        if (left.constant() && right.constant()) {
            BooleanLiteralExpr leftBoolean = leftEx instanceof BooleanLiteralExpr ? (BooleanLiteralExpr) leftEx : null;
            BooleanLiteralExpr rightBoolean = rightEx instanceof BooleanLiteralExpr ? (BooleanLiteralExpr) rightEx : null;

            if (leftBoolean != null && rightBoolean != null) {
                boolean result = isAnd ? leftBoolean.getValue() && rightBoolean.getValue() : leftBoolean.getValue() || rightBoolean.getValue();
                return JavaExpr.of(n, new BooleanLiteralExpr(result), boolean.class, constant);
            }
            if (leftEx instanceof NullLiteralExpr && rightEx instanceof NullLiteralExpr) {
                return JavaExpr.ofNull(n);
            }
            if ((leftBoolean != null && rightEx instanceof NullLiteralExpr) || (leftEx instanceof NullLiteralExpr && rightBoolean != null)) {
                boolean value = leftBoolean != null ? leftBoolean.getValue() : rightBoolean.getValue();
                if (isAnd) {
                    return value ? JavaExpr.ofNull(n) : JavaExpr.ofCon(n, new BooleanLiteralExpr(false), boolean.class);
                }
                return !value ? JavaExpr.ofNull(n) : JavaExpr.ofCon(n, new BooleanLiteralExpr(true), boolean.class);
            }
        }
        if (leftType instanceof FBoolean && rightType instanceof FBoolean) {
            if (left.primitive() && right.primitive()) {
                return JavaExpr.of(n, new BinaryExpr(leftEx, rightEx, isAnd ? BinaryExpr.Operator.AND : BinaryExpr.Operator.OR), boolean.class, constant);
            }
        }
        ConditionalExpr one = new ConditionalExpr();
        FieldAccessExpr trueOrFalse = manager.getStaticField(Boolean.class, isAnd ? "FALSE" : "TRUE");
        one.setThenExpr(trueOrFalse);
        Expression leftConExpr = leftEx, rightConExpr = rightEx;
        Expression leftElseExpr = leftEx, rightElseExpr = rightEx;
        if (!(leftType instanceof FBoolean) || left.nullable()) {
            leftConExpr = new MethodCallExpr(trueOrFalse, "equals").addArgument(leftConExpr);
            leftElseExpr = new MethodCallExpr(manager.getStaticField(Boolean.class, "TRUE"), "equals").addArgument(leftElseExpr);
        } else {
            leftConExpr = !isAnd ? leftConExpr : new UnaryExpr(leftConExpr, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        }
        if (!(rightType instanceof FBoolean || right.nullable())) {
            rightConExpr = new MethodCallExpr(trueOrFalse, "equals").addArgument(rightConExpr);
            rightElseExpr = new MethodCallExpr(manager.getStaticField(Boolean.class, "TRUE"), "equals").addArgument(rightElseExpr);
        } else {
            rightConExpr = !isAnd ? rightConExpr : new UnaryExpr(rightConExpr, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        }
        one.setCondition(new BinaryExpr(leftConExpr, rightConExpr, BinaryExpr.Operator.OR));
        BinaryExpr endBinary = new BinaryExpr(leftElseExpr, rightElseExpr, isAnd ? BinaryExpr.Operator.AND : BinaryExpr.Operator.OR);
        one.setElseExpr(ifOneNullAllNull(left, right, endBinary, true));
        return JavaExpr.of(n, one, boolean.class, constant)
                .nullable(left.nullable(), right.nullable());
    }
}
