package com.muyuanjin.feel.lang.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FeelRange;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.muyuanjin.feel.lang.type.FBoolean.BOOLEAN;

/**
 * @author muyuanjin
 */
public final class FRange implements FType {
    @NotNull
    @Getter
    private final FType elementType;
    @NotNull
    @Getter
    private final Map<String, FType> members;
    @Nullable
    @Getter
    private final Boolean start;
    @Nullable
    @Getter
    private final Boolean end;


    @JsonIgnore
    private transient int hashcode = Integer.MIN_VALUE;
    @JsonIgnore
    private transient String toString;
    @JsonIgnore
    private transient Type javaType;

    public static final FRange ANY = new FRange(FAny.ANY);

    private FRange(FType elementType) {
        this(elementType, null, null);
    }

    /**
     * <pre>{@code
     *  int, true , true     -> []   左闭右闭    [1..2]
     *  int, false, false    -> ()   左开右开    (1..2)
     *  int, true , false    -> [)   左闭右开    [1..2)
     *  int, false, true     -> (]   左开右闭    (1..2]
     *  int, null , true     -> ]    无界右闭    >=1
     *  int, true , null     -> [    无界左闭    <=1
     *  int, null , false    -> )    无界右开    >1
     *  int, false, null     -> (    无界左开    <1
     * }</pre>
     */
    private FRange(@NotNull FType elementType, @Nullable Boolean startInclusiveOrUnBound, @Nullable Boolean endInclusiveOrUnBound) {
        this.elementType = Objects.requireNonNull(elementType);
        this.start = startInclusiveOrUnBound;
        this.end = endInclusiveOrUnBound;
        Map<String, FType> map = MapUtil.newLinkedHashMap(4);
        map.put("start included", BOOLEAN);
        map.put("end included", BOOLEAN);
        if (start == null && end == null) {
            map.put("start", elementType);
            map.put("end", elementType);
        }
        if (start != null) {
            map.put("start", elementType);
        }
        if (end != null) {
            map.put("end", elementType);
        }
        this.members = Collections.unmodifiableMap(map);
    }

    @Override
    public int canBe(FType type) {
        if (equals(type)) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FRange range) {
            if (range.start == null && range.end == null || Objects.equals(start, range.start) && Objects.equals(end, range.end)) {
                return elementType.canBe(range.elementType);
            }
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
        if (type instanceof FRange range) {
            if (range.start == null && range.end == null || Objects.equals(start, range.start) && Objects.equals(end, range.end)) {
                return of(elementType.maxSub(range.elementType), start, end);
            }
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
        if (type instanceof FRange range) {
            if (range.start == null && range.end == null || Objects.equals(start, range.start) && Objects.equals(end, range.end)) {
                return of(elementType.minSuper(range.elementType), start, end);
            }
        }
        return FAny.ANY;
    }


    @Override
    public boolean isInstance(Object o) {
        if (!(o instanceof FeelRange<?> range)) {
            return false;
        }
        FRange type = range.type();
        return type != null && type.conformsTo(this);
    }

    @Override
    public Type getJavaType() {
        if (javaType == null) synchronized (this) {
            if (javaType != null) {
                return javaType;
            }
            javaType = TypeUtil.makeParameterizedType(null, FeelRange.class, elementType.getWrappedJavaType());
        }
        return javaType;
    }

    @Override
    public String getName() {
        return "range";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FRange fRange = (FRange) o;

        if (!elementType.equals(fRange.elementType)) return false;
        if (!Objects.equals(start, fRange.start)) return false;
        return Objects.equals(end, fRange.end);
    }

    @Override
    public int hashCode() {
        if (hashcode == Integer.MIN_VALUE) synchronized (this) {
            if (hashcode != Integer.MIN_VALUE) {
                return hashcode;
            }
            int result = elementType.hashCode();
            result = 31 * result + (start != null ? start.hashCode() : 0);
            result = 31 * result + (end != null ? end.hashCode() : 0);
            hashcode = result;
        }
        return hashcode;
    }

    @Override
    public String toString() {
        if (toString == null) synchronized (this) {
            if (toString != null) {
                return toString;
            }
            if (start == null && end == null) {
                toString = "range<" + elementType + ">";
            } else if (start == null) {
                toString = "range" + (end ? "]<" : ")<") + elementType + ">";
            } else if (end == null) {
                toString = "range" + (start ? "[<" : "(<") + elementType + ">";
            } else {
                toString = "range" + (start ? "[" : "(") + (end ? "]<" : ")<") + elementType + ">";
            }
        }
        return toString;
    }

    public static FRange of(FType type) {
        return new FRange(type);
    }

    public static FRange of(Type type) {
        return new FRange(FType.of(type));
    }

    public static FRange of(FType left, FType right) {
        return of(left.minSuper(right));
    }

    public static FRange of(FType type, Boolean start, Boolean end) {
        return new FRange(type, start, end);
    }

    public static FRange of(FType left, FType right, Boolean start, Boolean end) {
        return of(left.minSuper(right), start, end);
    }
}