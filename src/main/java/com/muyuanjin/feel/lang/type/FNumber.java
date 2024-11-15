package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.lang.FType;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @author muyuanjin
 */
public enum FNumber implements FType {
    INTEGER {
        @Override
        public boolean isInstance(Object o) {
            return o instanceof Integer;
        }

        @Override
        public Type getJavaType() {
            return int.class;
        }
    },
    LONG {
        @Override
        public boolean isInstance(Object o) {
            return o instanceof Long;
        }

        @Override
        public Type getJavaType() {
            return long.class;
        }
    },
    DOUBLE {
        @Override
        public boolean isInstance(Object o) {
            return o instanceof Double;
        }

        @Override
        public Type getJavaType() {
            return double.class;
        }
    },
    BIG_DECIMAL {
        @Override
        public boolean isInstance(Object o) {
            return o instanceof BigDecimal;
        }

        @Override
        public Type getJavaType() {
            return BigDecimal.class;
        }
    },
    NUMBER {
        @Override
        public boolean isInstance(Object o) {
            return o instanceof Number;
        }

        @Override
        public Type getJavaType() {
            return Number.class;
        }
    };

    @Override
    public String getName() {
        return "number";
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FNumber number) {
            if (type == NUMBER) {
                return CT;
            }
            if (type == BIG_DECIMAL) {
                return CC;
            }
            return this.ordinal() < number.ordinal() ? CT : LC;
        }
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        if (type == this || type instanceof FAny) {
            return this;
        }
        if (type instanceof FNumber number) {
            if (this == NUMBER) {
                return number;
            }
            if (number == NUMBER) {
                return this;
            }
        }
        return FNull.NULL;
    }

    @Override
    public FType minSuper(FType type) {
        if (type == this || type instanceof FNull) {
            return this;
        }
        if (type instanceof FNumber number) {
            return NUMBER;
        }
        return FAny.ANY;
    }

    @Override
    public String toString() {
        return getName();
    }
}