package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.common.entity.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.util.Iterator;

/** 抽象基类，包含作用域的常用功能。 */
@Getter
@Setter
abstract class BaseScope implements Scope {
    @Nullable
    protected Scope parentScope;
    @NotNull
    protected SymbolTrie symbolTrie;

    public BaseScope() {
        this(null);
    }

    public BaseScope(@Nullable Scope parentScope) {
        this(parentScope, null);
    }

    public BaseScope(@Nullable Scope parentScope, SymbolTrie symbolTrie) {
        this.parentScope = parentScope;
        this.symbolTrie = symbolTrie == null ? new SymbolTrie() : symbolTrie;
    }

    @Override
    public void define(@NotNull Symbol sym) {
        symbolTrie.insert(sym);
    }

    @Override
    public Symbol.Node resolve(@Nullable String singletonToken) {
        return symbolTrie.get(singletonToken);
    }

    @Override
    public Pair<Symbol.Node, Integer> resolvePrefix(Iterator<String> tokens) {
        return symbolTrie.prefix(tokens);
    }

    public String toString() {return symbolTrie.toString();}

    @Data
    protected static class SymbolNode implements Symbol.Node {
        private TypeSymbol typeSymbol;
        private FunctionSymbol funSymbol;
        private VariableSymbol varSymbol;
    }
}
