package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.lang.FType;

import java.util.List;

final class VariableSymbol extends BaseSymbol implements Symbol {
    public VariableSymbol(String name, FType type) {
        super(name, type);
    }

    public VariableSymbol(String name, FType type, List<String> tokens) {
        super(name, type, tokens);
    }
}
