package com.muyuanjin.feel.impl;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ForStmt;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
@RequiredArgsConstructor
public class CompositeFeelTypeFactory implements FeelTypeFactory {
    private final Collection<FeelTypeFactory> factories;

    @Override
    public @NotNull Map<String, FType> getTypes() {
        List<Map<String, FType>> types = new ArrayList<>(factories.size());
        for (FeelTypeFactory factory : factories) {
            types.add(factory.getTypes());
        }
        return MapUtil.merge(types);
    }

    @Override
    public JavaExpr getMember(ASTNode node, JavaExpr source, String member, Context context) {
        for (FeelTypeFactory factory : factories) {
            var access = factory.getMember(node, source, member, context);
            if (access != null) {
                return access;
            }
        }
        return null;
    }

    @Override
    public JavaExpr infixOp(ASTNode node, JavaExpr left, JavaExpr right, InfixOpNode.Op op, Context context) {
        for (FeelTypeFactory factory : factories) {
            var infixOp = factory.infixOp(node, left, right, op, context);
            if (infixOp != null) {
                return infixOp;
            }
        }
        return null;
    }

    @Override
    public Expression indexOf(JavaExpr source, JavaExpr index, Context context) {
        for (FeelTypeFactory factory : factories) {
            var indexOf = factory.indexOf(source, index, context);
            if (indexOf != null) {
                return indexOf;
            }
        }
        return null;
    }

    @Override
    public @Nullable ForStmt foreach(Expression start, Expression end,
                                     boolean startInclusive, boolean endInclusive,
                                     FType type, SimpleName itemName, Context context) {
        for (FeelTypeFactory factory : factories) {
            var foreach = factory.foreach(start, end, startInclusive, endInclusive, type, itemName, context);
            if (foreach != null) {
                return foreach;
            }
        }
        return null;
    }

    @Override
    public JavaExpr convert(JavaExpr original, FType target, Context context) {
        for (FeelTypeFactory factory : factories) {
            JavaExpr convert = factory.convert(original, target, context);
            if (convert != null) {
                return convert;
            }
        }
        return null;
    }
}
