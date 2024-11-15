package com.muyuanjin.feel;

import com.muyuanjin.feel.impl.CompositeFeelFunctionFactory;
import com.muyuanjin.feel.impl.DefaultFeelFunctionFactory;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FFunction;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author muyuanjin
 */
public interface FeelFunctionFactory {
    Map<String, Set<FFunction>> getFunctions();

    JavaExpr getFunction(ASTNode node, String fun, FFunction function, Context context);

    FeelFunctionFactory FACTORY = new Object() {
        FeelFunctionFactory factory() {
            List<FeelFunctionFactory> list = ServiceLoader.load(FeelFunctionFactory.class)
                    .stream().map(ServiceLoader.Provider::get).toList();
            if (list.isEmpty()) {
                return new DefaultFeelFunctionFactory();
            }
            if (list.size() == 1) {
                return list.get(0);
            }
            return new CompositeFeelFunctionFactory(list);
        }
    }.factory();


    static FeelFunctionFactory instance(Context context) {
        FeelFunctionFactory instance = context.get(FeelFunctionFactory.class);
        if (instance == null) {
            instance = FACTORY;
            context.put(FeelFunctionFactory.class, instance);
        }
        return instance;
    }
}