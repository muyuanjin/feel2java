package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.common.entity.Pair;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.util.Iterator;

/**
 * 作用域是由输入语言中的某些词法结构组合而成的符号字典。
 */
interface Scope {
    /**
     * 通常作用域的名称就像函数或类的名称。对于代码块等未命名的作用域，可以直接返回 "local "或其他名称
     */
    @NotNull
    String getName();

    /** 定义此作用域的作用域 */
    @Nullable
    Scope getParentScope();

    /**
     * 定义一个新的符号，不同类型的符号不会覆盖，比如 {@link FunctionSymbol}  {@link VariableSymbol}  {@link TypeSymbol}
     */
    void define(@NotNull Symbol sym);

    /** 在本作用域中查找名称，如果不在这里，则在父作用域中递归查找 */
    @Nullable
    Symbol.Node resolve(@Nullable String name);

    /**
     * 倒置字典树用法的 最长前缀匹配，扫描给定序列，找到能作为该序列前缀的最长已定义符号
     */
    Pair<Symbol.Node, Integer> resolvePrefix(Iterator<String> tokens);
}
