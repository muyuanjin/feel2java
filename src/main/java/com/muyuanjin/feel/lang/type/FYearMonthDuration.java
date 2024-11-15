package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Type;
import java.time.Period;
import java.util.Map;

import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;
import static com.muyuanjin.feel.lang.type.FNumber.LONG;

/**
 * @author muyuanjin
 */
public enum FYearMonthDuration implements FType {
    YEAR_MONTH_DURATION;
    private final Map<String, FType> members = Map.of(
            "years", INTEGER,
            "months", INTEGER,
            "value", LONG //总月份
    );

    @Override
    public String getName() {
        return "year and month duration";
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
        return o instanceof Period;
    }

    @Override
    public Type getJavaType() {
        return Period.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
