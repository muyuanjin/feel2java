package com.muyuanjin.feel.impl;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.FeelFunctionFactory;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FFunction;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * @author muyuanjin
 */
@RequiredArgsConstructor
public class CompositeFeelFunctionFactory implements FeelFunctionFactory {
    private final Collection<FeelFunctionFactory> factories;

    @Override
    public Map<String, Set<FFunction>> getFunctions() {
        List<Map<String, Set<FFunction>>> functions = new ArrayList<>(factories.size());
        for (FeelFunctionFactory factory : factories) {
            functions.add(factory.getFunctions());
        }
        return MapUtil.merge(functions);
    }

    @Override
    public JavaExpr getFunction(ASTNode node, String fun, FFunction function, Context context) {
        for (FeelFunctionFactory factory : factories) {
            var expr = factory.getFunction(node, fun, function, context);
            if (expr != null) {
                return expr;
            }
        }
        return null;
    }
}
