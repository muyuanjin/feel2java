package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;

import static com.muyuanjin.feel.lang.type.FBoolean.BOOLEAN;
import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;
import static com.muyuanjin.feel.lang.type.FNumber.LONG;

/**
 * @author muyuanjin
 */
public enum FDate implements FType {
    DATE;
    private final Map<String, FType> members = Map.of(
            "year", INTEGER,
            "lengthOfYear", INTEGER,
            "isLeapYear", BOOLEAN,
            "dayOfYear", INTEGER,
            "month", INTEGER,
            "dayOfMonth", INTEGER,
            "day", INTEGER,
            "weekday", INTEGER,
            "epochDay", LONG,
            "value", LONG
    );

    @Override
    public String getName() {
        return "date";
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FDateTime) {
            return LC;
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
        if (type instanceof FDateTime) {
            return type;
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
        if (type instanceof FDateTime) {
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
        return o instanceof LocalDate;
    }

    @Override
    public Type getJavaType() {
        return LocalDate.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
