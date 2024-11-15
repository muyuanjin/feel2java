package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.lang.FType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author muyuanjin
 */
@Getter
@Setter
final class TypeSymbol extends BaseSymbol implements Symbol, Serializable {
    public TypeSymbol(String name, FType type) {
        super(name, type);
    }
}
