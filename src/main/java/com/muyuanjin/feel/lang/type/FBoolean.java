package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;

import java.lang.reflect.Type;

/**
 * @author muyuanjin
 */
public enum FBoolean implements FType {
    BOOLEAN;

    @Override
    public String getName() {
        return "boolean";
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        if (type == this) {
            return this;
        }
        if (type instanceof FAny) {
            return this;
        }
        return FNull.NULL;
    }

    @Override
    public FType minSuper(FType type) {
        if (type == this) {
            return this;
        }
        if (type instanceof FNull) {
            return this;
        }
        return FAny.ANY;
    }

    @Override
    public boolean isInstance(Object o) {
        return o instanceof Boolean;
    }

    @Override
    public Type getJavaType() {
        return boolean.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}