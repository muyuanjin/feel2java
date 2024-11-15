package com.muyuanjin.feel.lang;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.TypeParser;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 这个接口是一个标签，表示实现对象是一种类型。每个类型都有自己的名称。在 C 语言等需要树状结构来表示类型的语言中，我们可以返回一个字符串作为名称。
 * 这些类型通常是结构体、类和基元类型，以及用于 C 等语言的类型树。
 */
@JsonSerialize(using = ToStringSerializer.class)
@JsonDeserialize(using = TypeParser.JsonDes.class)
public interface FType extends Serializable {
    String getName();

    Type getJavaType();

    default Type getWrappedJavaType() {
        return TypeUtil.primitiveToWrapper(getJavaType());
    }

    /**
     * 获取这个类型的成员。<P>
     * 例如，一个类的成员可以是它的字段和方法。<P>
     * 对于容器类型，比如list map set等，可以返回容器的元素类型。
     */
    @NotNull
    default Map<String, FType> getMembers() {
        return Collections.emptyMap();
    }

    /**
     * 10.3.2.9.2 类型一致性<p/>
     * 一致性（T <:S）：在每个需要 S 类型实例的地方，都可以用T类型的实例替代，即 T conformsTo S
     */
    default boolean conformsTo(FType t) {
        return canBe(t) <= CT;
    }

    int EQ = 0; // 相等 equals
    int CT = 1; // 一致 conforms to
    int CC = 2; // 可以转换 can conversion
    int LC = 3; // 有损转换 lossy conversion
    int RC = 4; // 运行时转换 runtime conversion

    int NO = Integer.MAX_VALUE; // 不兼容

    default boolean canConvertTo(FType type) {
        return canBe(type) != NO;
    }

    int canBe(FType type);

    FType maxSub(FType type);

    FType minSuper(FType type);

    boolean isInstance(Object o);

    /*
     *
     *      ██╗   ██╗   ████████╗   ██╗   ██╗
     *      ██║   ██║   ╚══██╔══╝   ██║   ██║
     *      ██║   ██║      ██║      ██║   ██║
     *      ██║   ██║      ██║      ██║   ██║
     *      ╚██████╔╝      ██║      ██║   ███████╗
     *       ╚═════╝       ╚═╝      ╚═╝   ╚══════╝
     *
     */
    static FType parse(String typeName) {
        return TypeParser.make().parseType(typeName);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isSameType(FType t1, FType t2) {
        return t1 == t2 || t1.getClass() == t2.getClass() || t1.getClass().getNestHost() == t2.getClass().getNestHost();
    }

    static FType of(Object o) {
        if (o == null) {
            return FNull.NULL;
        }
        //TODO 更多类型 比如 OffsetDateTime ZonedDateTime Instant
        if (o instanceof FType) {
            return (FType) o;
        } else if (o instanceof Integer) {
            return FNumber.INTEGER;
        } else if (o instanceof Long) {
            return FNumber.LONG;
        } else if (o instanceof Double) {
            return FNumber.DOUBLE;
        } else if (o instanceof BigDecimal) {
            return FNumber.BIG_DECIMAL;
        } else if (o instanceof Number) {
            return FNumber.NUMBER;
        } else if (o instanceof String) {
            return FString.STRING;
        } else if (o instanceof CharSequence) {
            return FString.STRING;
        } else if (o instanceof Boolean) {
            return FBoolean.BOOLEAN;
        } else if (o instanceof LocalDate) {
            return FDate.DATE;
        } else if (o instanceof LocalTime) {
            return FTime.TIME;
        } else if (o instanceof LocalDateTime) {
            return FDateTime.DATE_TIME;
        } else if (o instanceof Duration) {
            return FDayTimeDuration.DAY_TIME_DURATION;
        } else if (o instanceof Period) {
            return FYearMonthDuration.YEAR_MONTH_DURATION;
        } else if (o instanceof FeelFunction<?> function) {
            return function.type();
        } else if (o instanceof FeelRange<?> range) {
            return range.type();
        } else if (o instanceof Collection<?> list) {
            return FList.of(list);
        } else if (o instanceof Map<?, ?> map) {
            return FContext.of(map);
        } else if (o instanceof Object[] array) {
            return FList.of(array);
        } else if (Object.class.equals(o.getClass())) {
            return FAny.ANY;
        }
        return FContext.of(o);
    }

    static FType of(Type type) {
        //TODO 更多类型 比如 OffsetDateTime ZonedDateTime Instant
        if (type instanceof Class<?> clazz) {
            if (clazz == int.class || clazz == Integer.class) {
                return FNumber.INTEGER;
            } else if (clazz == long.class || clazz == Long.class) {
                return FNumber.LONG;
            } else if (clazz == double.class || clazz == Double.class) {
                return FNumber.DOUBLE;
            } else if (clazz == BigDecimal.class) {
                return FNumber.BIG_DECIMAL;
            } else if (Number.class.isAssignableFrom(clazz)) {
                return FNumber.NUMBER;
            } else if (clazz == boolean.class || clazz == Boolean.class) {
                return FBoolean.BOOLEAN;
            } else if (clazz == String.class) {
                return FString.STRING;
            } else if (CharSequence.class.isAssignableFrom(clazz)) {
                return FString.STRING;
            } else if (clazz == LocalDate.class) {
                return FDate.DATE;
            } else if (clazz == LocalTime.class) {
                return FTime.TIME;
            } else if (clazz == LocalDateTime.class) {
                return FDateTime.DATE_TIME;
            } else if (clazz == Duration.class) {
                return FDayTimeDuration.DAY_TIME_DURATION;
            } else if (clazz == Period.class) {
                return FYearMonthDuration.YEAR_MONTH_DURATION;
            } else if (List.class.isAssignableFrom(clazz)) {
                return FList.ANY;
            } else if (FeelFunction.class.isAssignableFrom(clazz)) {
                return FFunction.ANY;
            } else if (FeelRange.class.isAssignableFrom(clazz)) {
                return FRange.ANY;
            } else if (clazz == Object.class) {
                return FAny.ANY;
            }
            return FContext.of(clazz);
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() instanceof Class<?> clazz && List.class.isAssignableFrom(clazz)) {
                return FList.of(of(parameterizedType.getActualTypeArguments()[0]));
            }
        }
        return FAny.ANY;
    }

    static FType getElementType(FType... types) {
        FType type = getMinSuperType(types);
        if (type instanceof FList list) {
            return list.getElementType();
        } else if (type instanceof FRange range) {
            return range.getElementType();
        }
        return type;
    }

    static FType getMaxSubType(FType... types) {
        if (types.length == 0) {
            return FNull.NULL;
        }
        FType type = types[0];
        for (int i = 1; i < types.length; i++) {
            FType next = types[i];
            if (next == null) {
                continue;
            }
            type = type.maxSub(next);
            if (type == FNull.NULL) {
                return FNull.NULL;
            }
        }
        return type;
    }

    static FType getMinSuperType(FType... types) {
        if (types.length == 0) {
            return FAny.ANY;
        }
        FType type = types[0];
        for (int i = 1; i < types.length; i++) {
            FType next = types[i];
            if (next == null) {
                continue;
            }
            type = type.minSuper(next);
            if (type == FAny.ANY) {
                return FAny.ANY;
            }
        }
        return type;
    }
}