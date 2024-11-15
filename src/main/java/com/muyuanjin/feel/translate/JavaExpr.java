package com.muyuanjin.feel.translate;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.muyuanjin.common.util.LazyRef;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FAny;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author muyuanjin
 */
@Data
@NoArgsConstructor
@Accessors(fluent = true)
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public final class JavaExpr {
    private ASTNode node;
    private LazyRef<Expression> expr;
    private FType feelType;
    private Type javaType;
    private boolean constant;
    private boolean nullable;

    /**
     * 原始表达式，如果是类型转换，原始表达式是转换前的表达式
     */
    private LazyRef<JavaExpr> original;
    /**
     * 方法调用的内联优化，即，如果后续是方法调用，则采取的内联优化，比如 string length("") 优化为 "".length()
     */
    private LazyRef<BiFunction<ClassManager, JavaExpr[], Expression>> inLineFun;
    /**
     * 字段访问的内联优化，即，如果后续是字段访问，则采取的内联优化，比如 {a:1,b:2,c:a+b}.c 优化调用中间的map生成
     */
    private LazyRef<BiFunction<ClassManager, String, JavaExpr>> inLinePath;

    private JavaExpr(ASTNode node, LazyRef<Expression> expr, FType feelType, Type javaType, boolean constant, boolean nullable, LazyRef<JavaExpr> original, LazyRef<BiFunction<ClassManager, JavaExpr[], Expression>> inLineFun, LazyRef<BiFunction<ClassManager, String, JavaExpr>> inLinePath) {
        this.node = node;
        this.expr = expr;
        this.feelType = feelType;
        this.javaType = javaType;
        this.constant = constant;
        this.nullable = nullable;
        this.original = original;
        this.inLineFun = inLineFun;
        this.inLinePath = inLinePath;
        if (javaType instanceof Class<?> clazz) {
            if (Number.class.isAssignableFrom(clazz)) {
                this.feelType = FType.of(clazz);
            }
            if (clazz.isPrimitive()) {
                this.nullable = false;
            }
        }
    }

    @NotNull
    public FType feelType() {
        if (feelType != null) {
            return feelType;
        }
        if (node != null) {
            return node.getType();
        }
        if (javaType != null) {
            return FType.of(javaType);
        }
        return FAny.ANY;
    }

    public boolean nullable() {
        if (primitive()) {
            return false;
        }
        return nullable;
    }

    public boolean primitive() {
        return javaType() instanceof Class<?> clazz && clazz.isPrimitive();
    }

    public JavaExpr nullable(boolean nullable1, boolean... nullables) {
        if (nullable1) {
            return nullable(true);
        }
        for (boolean nullable : nullables) {
            if (nullable) return nullable(true);
        }
        return nullable(false);
    }

    public JavaExpr nullable(boolean nullable) {
        this.nullable = nullable;
        if (nullable && primitive()) {
            javaType(TypeUtil.wrapperToPrimitive(javaType()));
        }
        return this;
    }

    public Expression expr() {
        return expr.get();
    }

    public boolean isSimple() {
        return CodeGens.isSimple(expr());
    }

    @NotNull
    public JavaExpr original() {
        return original == null ? this : original.orElse(this);
    }

    @Nullable
    public BiFunction<ClassManager, JavaExpr[], Expression> inLineFun() {
        return inLineFun == null ? null : inLineFun.get();
    }

    @Nullable
    public BiFunction<ClassManager, String, JavaExpr> inLinePath() {
        return inLinePath == null ? null : inLinePath.get();
    }

    public JavaExpr expr(Expression expression) {
        this.expr = LazyRef.of(expression);
        return this;
    }

    public JavaExpr expr(Supplier<? extends Expression> lazyLoader) {
        this.expr = LazyRef.ofSup(lazyLoader);
        return this;
    }

    public JavaExpr original(JavaExpr expression) {
        this.original = LazyRef.of(expression);
        return this;
    }

    public JavaExpr original(Supplier<? extends JavaExpr> lazyLoader) {
        this.original = LazyRef.ofSup(lazyLoader);
        return this;
    }

    public JavaExpr convert(Function<Expression, Expression> exprMapping, FType feelType, Type javaType) {
        JavaExpr copy = this.copy();
        copy.original = LazyRef.of(this);
        copy.expr = LazyRef.of(exprMapping.apply(expr()));
        copy.feelType = feelType;
        copy.javaType = javaType;
        if (copy.inLineFun != null) {
            copy.inLineFun = LazyRef.of((manager, args) -> exprMapping.apply(copy.inLineFun.get().apply(manager, args)));
        }
        if (copy.inLinePath != null) {
            copy.inLinePath = LazyRef.of((manager, path) -> copy.inLinePath.get().apply(manager, path).convert(exprMapping, feelType, javaType));
        }
        return copy;
    }

    public JavaExpr inLineFun(BiFunction<ClassManager, JavaExpr[], Expression> inLineFun) {
        this.inLineFun = LazyRef.of(inLineFun);
        return this;
    }

    public JavaExpr inLineFun(Supplier<? extends BiFunction<ClassManager, JavaExpr[], Expression>> lazyLoader) {
        this.inLineFun = LazyRef.ofSup(lazyLoader);
        return this;
    }

    public JavaExpr inLinePath(BiFunction<ClassManager, String, JavaExpr> inLinePath) {
        this.inLinePath = LazyRef.of(inLinePath);
        return this;
    }

    public JavaExpr inLinePath(Supplier<? extends BiFunction<ClassManager, String, JavaExpr>> lazyLoader) {
        this.inLinePath = LazyRef.ofSup(lazyLoader);
        return this;
    }

    public JavaExpr copy() {
        return new JavaExpr(node, expr, feelType, javaType, constant, nullable, original, inLineFun, inLinePath);
    }

    public static JavaExpr of(ASTNode node, Type type) {
        return new JavaExpr(node, null, null, type, false, true, null, null, null);
    }

    public static JavaExpr of(ASTNode node, Expression expr, Type type) {
        JavaExpr javaExpr = new JavaExpr(node, LazyRef.of(expr), null, type, false, true, null, null, null);
        if (expr instanceof LiteralExpr && !(expr instanceof NullLiteralExpr)) {
            javaExpr.nullable(false);
        }
        return javaExpr;
    }

    public static JavaExpr ofCon(ASTNode node, Expression expr, Type type) {
        JavaExpr javaExpr = new JavaExpr(node, LazyRef.of(expr), null, type, true, true, null, null, null);
        if (expr instanceof LiteralExpr && !(expr instanceof NullLiteralExpr)) {
            javaExpr.nullable(false);
        }
        return javaExpr;
    }

    public static JavaExpr ofNull(ASTNode node) {
        return new JavaExpr(node, LazyRef.of(new NullLiteralExpr()), null, void.class, true, true, null, null, null);
    }

    public static JavaExpr of(ASTNode node, Expression expr, Type type, boolean constant) {
        JavaExpr javaExpr = new JavaExpr(node, LazyRef.of(expr), null, type, constant, true, null, null, null);
        if (expr instanceof LiteralExpr && !(expr instanceof NullLiteralExpr)) {
            javaExpr.nullable(false);
        }
        return javaExpr;
    }

    public static JavaExpr of(ASTNode node, Supplier<? extends Expression> expr, Type type, boolean constant) {
        return new JavaExpr(node, LazyRef.ofSup(expr), null, type, constant, true, null, null, null);
    }

    public static JavaExpr of(ASTNode node, Expression expr, Type javaType, boolean constant, BiFunction<ClassManager, JavaExpr[], Expression> inLine) {
        JavaExpr javaExpr = new JavaExpr(node, LazyRef.of(expr), null, javaType, constant, true, null, LazyRef.of(inLine), null);
        if (expr instanceof LiteralExpr && !(expr instanceof NullLiteralExpr)) {
            javaExpr.nullable(false);
        }
        return javaExpr;
    }
}