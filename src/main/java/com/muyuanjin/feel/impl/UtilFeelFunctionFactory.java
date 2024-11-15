package com.muyuanjin.feel.impl;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.muyuanjin.common.function.FunctionEx;
import com.muyuanjin.common.util.StringUtil;
import com.muyuanjin.feel.FeelFunctionFactory;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FContext;
import com.muyuanjin.feel.lang.type.FFunction;
import com.muyuanjin.feel.lang.type.FList;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.CodeGens;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author muyuanjin
 */
public class UtilFeelFunctionFactory implements FeelFunctionFactory {
    private static final Map<String, List<MethodFun>> functions;
    private static final Map<String, Set<FFunction>> functionTypes;

    public static FeelFunction<?> fun(String fun, int index) {
        List<MethodFun> methods = functions.get(fun);
        if (methods == null) {
            throw new IllegalArgumentException("No such function: " + fun);
        }
        return Objects.requireNonNull(methods.get(index), "No such function: " + fun + " at index " + index).fun;
    }

    static {
        Map<String, List<MethodFun>> fun = new HashMap<>(getFunctions(Math.class));
        fun.putAll(getFunctions(String.class));
        fun.putAll(getFunctions(Character.class));
        fun.putAll(getFunctions(Collections.class));
        fun.putAll(getFunctions(StringUtil.class));
        functions = Collections.unmodifiableMap(fun);
        functionTypes = Collections.unmodifiableMap(fun.entrySet().stream().collect(HashMap::new, (map, entry) -> {
            Set<FFunction> types = new HashSet<>();
            for (MethodFun methodFun : entry.getValue()) {
                types.add(methodFun.fun.type());
            }
            map.put(entry.getKey(), types);
        }, HashMap::putAll));
    }

    @Override
    public Map<String, Set<FFunction>> getFunctions() {
        return functionTypes;
    }

    @Override
    public JavaExpr getFunction(ASTNode node, String fun, FFunction function, Context context) {
        List<MethodFun> methods = functions.get(fun);
        if (methods == null) {
            return null;
        }
        ClassManager manager = ClassManager.instance(context);
        for (int i = 0; i < methods.size(); i++) {
            MethodFun methodFun = methods.get(i);
            String index = Integer.toString(i);
            if (methodFun.fun.type().equals(function)) {
                return JavaExpr.of(node, () -> manager.getStaticMethod(UtilFeelFunctionFactory.class, "fun")
                                .addArgument(CodeGens.stringLiteral(fun))
                                .addArgument(new IntegerLiteralExpr(index)), function.getJavaType(), true)
                        .inLineFun(methodFun.inLineFun);
            }
        }
        return null;
    }

    static Map<String, List<MethodFun>> getFunctions(Class<?> clazz) {
        Method[] clazzMethods = clazz.getMethods();
        Map<String, LinkedHashSet<MethodFun>> methods = new HashMap<>();
        out:
        for (Method method : clazzMethods) {
            if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            FType returnType = FType.of(method.getGenericReturnType());
            if (returnType instanceof FContext) {
                continue;
            }
            FFunction type;
            if (method.getParameterCount() == 0) {
                type = FFunction.of(returnType);
            } else {
                Parameter[] parameters = method.getParameters();
                List<String> names = new ArrayList<>(parameters.length);
                List<FType> types = new ArrayList<>(parameters.length);
                for (Parameter parameter : parameters) {
                    FType fType;
                    if (parameter.isVarArgs() && parameter.getParameterizedType() instanceof Class<?> arrayClass && arrayClass.isArray()) {
                        fType = FList.ofVars(FType.of(arrayClass.getComponentType()));
                    } else {
                        fType = FType.of(parameter.getParameterizedType());
                    }
                    if (fType instanceof FContext) {
                        continue out;
                    }
                    names.add(parameter.getName());
                    types.add(fType);
                }
                type = FFunction.of(returnType, names, types);
            }
            FeelFunction<?> fun = new FeelFunction.Default<>(type, (FunctionEx<Object[], Object>) args -> method.invoke(null, args));
            BiFunction<ClassManager, JavaExpr[], Expression> inLineFun = (manager, args) -> {
                MethodCallExpr staticMethod = manager.getStaticMethod(clazz, method.getName());
                for (JavaExpr arg : args) {
                    staticMethod.addArgument(arg.expr());
                }
                return staticMethod;
            };
            methods.computeIfAbsent(clazz.getSimpleName() + "." + method.getName(), k -> new LinkedHashSet<>()).add(new MethodFun(fun, inLineFun));
        }
        return methods.entrySet().stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), new ArrayList<>(entry.getValue())), HashMap::putAll);
    }

    record MethodFun(FeelFunction<?> fun,
                     BiFunction<ClassManager, JavaExpr[], Expression> inLineFun) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodFun methodFun = (MethodFun) o;

            return fun.type().equals(methodFun.fun.type());
        }

        @Override
        public int hashCode() {
            return fun.type().hashCode();
        }
    }
}
