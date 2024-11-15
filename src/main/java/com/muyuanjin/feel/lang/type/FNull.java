package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;

import java.lang.reflect.Type;

/**
 * @author muyuanjin
 */
public enum FNull implements FType {
    NULL;

    @Override
    public String getName() {
        return "null";
    }

    @Override
    public boolean conformsTo(FType t) {
        return true;
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        return CT;
    }

    @Override
    public FType maxSub(FType type) {
        return this;
    }

    @Override
    public FType minSuper(FType type) {
        return type;
    }

    @Override
    public boolean isInstance(Object o) {
        return o == null;
    }

    @Override
    public Type getJavaType() {
        return void.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}