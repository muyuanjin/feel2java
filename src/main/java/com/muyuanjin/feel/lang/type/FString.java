package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Type;
import java.util.Map;

import static com.muyuanjin.feel.lang.type.FBoolean.BOOLEAN;
import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;

/**
 * @author muyuanjin
 */

public enum FString implements FType {
    STRING;

    private static final Map<String, FType> members = Map.of(
            "trim", STRING,
            "strip", STRING,
            "length", INTEGER,
            "isEmpty", BOOLEAN,
            "isBlank", BOOLEAN,
            "upperCase", STRING,
            "lowerCase", STRING
    );

    @Override
    public String getName() {
        return "string";
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
    public @NotNull Map<String, FType> getMembers() {
        return members;
    }

    @Override
    public boolean isInstance(Object o) {
        return o instanceof String;
    }

    @Override
    public Type getJavaType() {
        return String.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
