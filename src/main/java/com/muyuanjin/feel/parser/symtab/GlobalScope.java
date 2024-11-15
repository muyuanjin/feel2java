package com.muyuanjin.feel.parser.symtab;

import jakarta.validation.constraints.NotNull;

/** 与全局相关的作用域。 */
class GlobalScope extends BaseScope {
    public GlobalScope(Scope scope, SymbolTrie symbolTrie) {super(scope, symbolTrie);}

    public @NotNull String getName() {return "<global>";}
}
