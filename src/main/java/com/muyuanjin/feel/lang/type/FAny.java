package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;

import java.lang.reflect.Type;

/**
 * @author muyuanjin
 */
public enum FAny implements FType {
    ANY,//真任意类型
    /* 某个具体类型 */
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;

    @Override
    public String getName() {
        return "any";
    }

    @Override
    public int canBe(FType type) {
        if (type instanceof FAny) {
            return type == this ? EQ : CT;
        }
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        if (type instanceof FAny) {
            return type == this ? this : ANY;
        }
        return type;
    }

    @Override
    public FType minSuper(FType type) {
        if (type instanceof FAny) {
            return type == this ? this : ANY;
        }
        return this;
    }

    @Override
    public boolean isInstance(Object o) {
        return true;
    }

    @Override
    public Type getJavaType() {
        return Object.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
