package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.Map;

import static com.muyuanjin.feel.lang.type.FDayTimeDuration.DAY_TIME_DURATION;
import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;

/**
 * @author muyuanjin
 */
public enum FTime implements FType {
    TIME;
    private final Map<String, FType> members = Map.of(
            "hour", INTEGER,
            "minute", INTEGER,
            "second", INTEGER,
            "time offset", DAY_TIME_DURATION,
            "timezone", INTEGER
    );

    @Override
    public String getName() {
        return "time";
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
        return o instanceof LocalTime;
    }

    @Override
    public Type getJavaType() {
        return LocalTime.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
