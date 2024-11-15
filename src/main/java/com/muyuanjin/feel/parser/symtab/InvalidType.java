package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.lang.FType;

import java.lang.reflect.Type;

/**
 * 出现这个类型，说明解析出现了错误
 *
 * @author muyuanjin
 */
class InvalidType implements FType {
    @Override
    public String getName() {
        return "<INVALID>";
    }

    @Override
    public boolean conformsTo(FType t) {
        return false;
    }

    @Override
    public int canBe(FType type) {
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        return this;
    }

    @Override
    public FType minSuper(FType type) {
        return this;
    }

    @Override
    public boolean isInstance(Object o) {
        return false;
    }

    @Override
    public Type getJavaType() {
        return void.class;
    }
}