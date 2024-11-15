package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.lang.FeelFunctions;
import com.muyuanjin.feel.lang.type.FFunction;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

/**
 * @author muyuanjin
 */
@Getter
@EqualsAndHashCode(callSuper = true)
final class FunctionSymbol extends BaseSymbol implements Symbol {
    private final List<FFunction> functions;

    public FunctionSymbol(FeelFunctions function) {
        super(function.getName(), function.getFunctions().size() == 1 ? function.getFunctions().get(0) : null);
        this.functions = function.getFunctions();
    }

    public FunctionSymbol(String name, FFunction function) {
        super(name, function);
        this.functions = List.of(function);
    }

    public FunctionSymbol(String name, FFunction... function) {
        super(name, function.length == 1 ? function[0] : null);
        this.functions = List.of(function);
    }

    public FunctionSymbol(String name, Collection<FFunction> functions) {
        super(name, functions.size() == 1 ? functions.iterator().next() : null);
        this.functions = List.copyOf(functions);
    }
}
