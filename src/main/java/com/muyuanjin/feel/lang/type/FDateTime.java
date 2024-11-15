package com.muyuanjin.feel.lang.type;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static com.muyuanjin.feel.lang.type.FDate.DATE;
import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;
import static com.muyuanjin.feel.lang.type.FNumber.LONG;
import static com.muyuanjin.feel.lang.type.FTime.TIME;

/**
 * @author muyuanjin
 */
public enum FDateTime implements FType {
    DATE_TIME;
    private final Map<String, FType> members = Collections.unmodifiableMap(MapUtil.of(
            "date", DATE, "time", TIME,
            "year", INTEGER, "month", INTEGER, "day", INTEGER, "weekday", INTEGER,
            "hour", INTEGER, "minute", INTEGER, "second", INTEGER,
            "time offset", INTEGER, "timezone", INTEGER,
            "epochSecond", LONG, "value", LONG
    ));

    @Override
    public String getName() {
        return "date and time";
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FDate || type instanceof FTime) {
            return CC;
        }
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        if (type == this) {
            return this;
        }
        if (type instanceof FAny || type instanceof FDate || type instanceof FTime) {
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
        if (type instanceof FDate || type instanceof FTime) {
            return type;
        }
        return FAny.ANY;
    }


    @Override
    public @NotNull Map<String, FType> getMembers() {
        return members;
    }

    @Override
    public boolean isInstance(Object o) {
        return o instanceof LocalDateTime;
    }

    @Override
    public Type getJavaType() {
        return LocalDateTime.class;
    }

    @Override
    public String toString() {
        return getName();
    }
}
