package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;

import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;
import static com.muyuanjin.feel.lang.type.FNumber.LONG;

/**
 * @author muyuanjin
 */
public enum FDayTimeDuration implements FType {
    DAY_TIME_DURATION;
    private final Map<String, FType> members = Map.of(
            "days", INTEGER,
            "hours", INTEGER,
            "minutes", INTEGER,
            "seconds", INTEGER,
            "value", LONG //总秒数
    );

    @Override
    public String getName() {
        return "day and time duration";
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
        return o instanceof Duration;
    }

    @Override
    public Type getJavaType() {
        return Duration.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
