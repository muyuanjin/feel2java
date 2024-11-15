package com.muyuanjin.feel;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ForStmt;
import com.muyuanjin.feel.impl.CompositeFeelTypeFactory;
import com.muyuanjin.feel.impl.DefaultFeelTypeFactory;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author muyuanjin
 */
public interface FeelTypeFactory {
    @NotNull
    Map<String, FType> getTypes();

    @Nullable
    JavaExpr getMember(ASTNode node, JavaExpr source, String member, Context context);

    @Nullable
    JavaExpr infixOp(ASTNode node, JavaExpr left, JavaExpr right, InfixOpNode.Op op, Context context);

    Expression indexOf(JavaExpr source, JavaExpr index, Context context);

    @Nullable
    ForStmt foreach(Expression start, Expression end, boolean startInclusive, boolean endInclusive,
                    FType type, SimpleName itemName, Context context);

    @Nullable
    JavaExpr convert(JavaExpr original, FType target, Context context);

    FeelTypeFactory FACTORY = new Object() {
        FeelTypeFactory factory() {
            List<FeelTypeFactory> list = ServiceLoader.load(FeelTypeFactory.class)
                    .stream().map(ServiceLoader.Provider::get).toList();
            if (list.isEmpty()) {
                return new DefaultFeelTypeFactory();
            }
            if (list.size() == 1) {
                return list.get(0);
            }
            return new CompositeFeelTypeFactory(list);
        }
    }.factory();


    static FeelTypeFactory instance(Context context) {
        FeelTypeFactory instance = context.get(FeelTypeFactory.class);
        if (instance == null) {
            instance = FACTORY;
            context.put(FeelTypeFactory.class, instance);
        }
        return instance;
    }
}