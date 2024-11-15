package com.muyuanjin.feel.translate;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.muyuanjin.feel.FeelFunctionFactory;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FAny;
import com.muyuanjin.feel.lang.type.FFunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author muyuanjin
 */
final class ScopeHelper {
    private final Context context;
    private final Deque<LocalVars> stack;

    public ScopeHelper(Context context) {
        this.context = context;
        this.stack = new ArrayDeque<>();
    }

    private static class LocalVars {
        private boolean convert2NameExpr = false;
        private BiConsumer<String, JavaExpr> varListener;
        private final Map<String, JavaExpr> currentVars = new HashMap<>();
        private final LinkedHashMap<String, JavaExpr> outerVars = new LinkedHashMap<>();

        public void addOuterVar(@NotNull String name, @NotNull JavaExpr expr) {
            outerVars.put(Objects.requireNonNull(name), Objects.requireNonNull(expr));
        }

        public boolean hasOuterVar() {
            return !outerVars.isEmpty();
        }
    }

    public int size() {
        return stack.size();
    }

    public void pushScope() {
        stack.push(new LocalVars());
    }

    public void addVarListener(BiConsumer<String, JavaExpr> listener) {
        LocalVars first = stack.getFirst();
        if (first.varListener == null) {
            first.varListener = listener;
        } else {
            first.varListener = first.varListener.andThen(listener);
        }
    }

    public void pushFunScope() {
        LocalVars localVars = new LocalVars();
        localVars.convert2NameExpr = true;
        stack.push(localVars);
    }

    public boolean hasOuterVar() {
        return stack.getFirst().hasOuterVar();
    }

    public boolean convert2NameExpr() {
        Iterator<LocalVars> iterator = stack.iterator();
        if (!iterator.hasNext()) {
            return false;
        }
        iterator.next();
        while (iterator.hasNext()) {
            if (iterator.next().convert2NameExpr) {
                return true;
            }
        }
        return false;
    }

    public Map<String, JavaExpr> getOuterVars() {
        return stack.getFirst().outerVars;
    }

    public void popScope() {
        stack.pop();
    }

    public void putAll(Map<String, JavaExpr> inputTs) {
        stack.getFirst().currentVars.putAll(inputTs);
    }

    public void put(String name, JavaExpr expr) {
        stack.getFirst().currentVars.put(name, expr);
    }

    public JavaExpr resolve(ASTNode node, String name, FType type) {
        int index = 0;
        JavaExpr result = null;
        boolean convert2NameExpr = convert2NameExpr();
        for (var localVars : stack) {
            result = localVars.currentVars.getOrDefault(name, localVars.outerVars.get(name));
            if (result != null) {
                if (index > 0 && !result.constant()) {
                    // 添加outerVar到所有小于index的localVars中
                    Iterator<LocalVars> localVarsIterator = stack.iterator();
                    while (index-- > 0) {
                        LocalVars next = localVarsIterator.next();
                        if (next.varListener != null) {
                            next.varListener.accept(name, result);
                        }
                        next.addOuterVar(name, result);
                    }
                    if (convert2NameExpr && !(result.expr() instanceof NameExpr)) {
                        result = JavaExpr.of(node, new NameExpr(name), result.javaType());
                    }
                } else {
                    if (localVars.varListener != null) {
                        localVars.varListener.accept(name, result);
                    }
                }
                return result.copy().node(node);
            }
            index++;
        }

        // 先从input看是否显式声明，如果有则直接返回，否则再从全局函数库查看，最后再用input兜底
        CompilerTask task = CompilerTask.instance(context);
        ClassManager manager = ClassManager.instance(context);
        FeelTypeFactory typeFactory = FeelTypeFactory.instance(context);
        FeelFunctionFactory functionFactory = FeelFunctionFactory.instance(context);

        FType fType = task.inputTypes().get(name);
        if (fType != null) {
            result = JavaExpr.of(node, getInputMember(node, name), fType.getJavaType()).feelType(fType);
        } else if ("?".equals(name)) {
            result = JavaExpr.of(node, new NameExpr(task.unaryTestInputParam()), task.unaryTestInputJavaType()).feelType(task.unaryTestInputType());
        } else if (type instanceof FFunction function) {
            result = functionFactory.getFunction(node, name, function, context);
            // 全局函数都是静态的不需要作为捕获的外部变量
            if (result != null) {
                return result;
            }
        }
        if (result == null) {
            // 没有定义的变量最后还是从input获取 只是类型为ANY 方便后续 instanceof
            result = JavaExpr.of(node, getInputMember(node, name), Object.class).feelType(FAny.ANY);
        }
        for (LocalVars c : stack) {
            c.addOuterVar(name, result);
            if (c.varListener != null) {
                c.varListener.accept(name, result);
            }
        }
        if (!result.constant() && convert2NameExpr && !(result.expr() instanceof NameExpr)) {
            result = JavaExpr.of(node, new NameExpr(name), result.javaType());
        }
        return result;
    }

    Expression getInputMember(ASTNode node, String memberName) {
        CompilerTask task = CompilerTask.instance(context);
        FeelTypeFactory typeFactory = FeelTypeFactory.instance(context);
        JavaExpr input = JavaExpr.of(node, new NameExpr(task.rootInputParam()), task.rootInputJavaType()).feelType(task.rootInputType());
        JavaExpr member = typeFactory.getMember(node, input, memberName, context);
        return member == null ? new NullLiteralExpr() : member.expr();
    }
}