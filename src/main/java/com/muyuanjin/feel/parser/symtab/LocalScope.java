package com.muyuanjin.feel.parser.symtab;

import lombok.Getter;
import jakarta.annotation.Nullable;

/** 通常与 {...} 代码块相关联的作用域对象 */
@Getter
class LocalScope extends BaseScope {
    private final String name;

    public LocalScope(@Nullable String name, @Nullable Scope enclosingScope, SymbolTrie symbolTrie) {
        super(enclosingScope, symbolTrie);
        this.name = name == null ? "<local>" : name;
    }
}