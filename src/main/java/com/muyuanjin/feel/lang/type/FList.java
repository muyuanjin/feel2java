package com.muyuanjin.feel.lang.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muyuanjin.common.util.LazyRef;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author muyuanjin
 */
public final class FList implements FType {
    @Getter
    private final FType elementType;
    @Getter
    // 是否只接受或来自变长参数
    private final boolean varargs;
    private final LazyRef<Map<String, FType>> members;

    @JsonIgnore
    private transient int hashcode = Integer.MIN_VALUE;
    @JsonIgnore
    private transient String toString;
    @JsonIgnore
    private transient Type javaType;

    public static final FList ANY = of(FAny.ANY);

    private FList(FType elementType) {
        this(elementType, false);
    }

    private FList(FType elementType, boolean varargs) {
        this.elementType = Objects.requireNonNull(elementType);
        this.varargs = varargs;
        // 对于可能创建其他类型的类型，需要使用懒加载，防止StackOverflowError
        this.members = LazyRef.of(() -> {
            Map<String, FType> elementMem = this.elementType.getMembers();
            Map<String, FType> members = MapUtil.newLinkedHashMap(elementMem.size() + 4);
            members.put("size", FNumber.INTEGER);
            members.put("isEmpty", FBoolean.BOOLEAN);
            members.put("isNotEmpty", FBoolean.BOOLEAN);
            members.put("contains", FFunction.of(FBoolean.BOOLEAN, this.elementType));
            // [ {x:1, y:2}, {x:2, y:3} ].y = [2,3]
            for (Map.Entry<String, FType> entry : elementMem.entrySet()) {
                members.put(entry.getKey(), of(entry.getValue()));
            }
            return Collections.unmodifiableMap(members);
        });
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FList list) {
            int canBe = elementType.canBe(list.elementType);
            if (canBe == EQ) return EQ;
            if (canBe == CT) return CC;// List<String> 不能直接作为 List<Object> 使用，泛型边界需要严格对其
            return canBe;
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
        if (type instanceof FList list) {
            if (varargs == list.varargs) {
                return of(elementType.maxSub(list.elementType));
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
        if (type instanceof FList list) {
            return of(elementType.minSuper(list.elementType));
        }
        return FAny.ANY;
    }

    @Override
    public @NotNull Map<String, FType> getMembers() {
        return members.get();
    }

    @Override
    public boolean isInstance(Object o) {
        if (!(o instanceof Collection<?> collection)) {
            return false;
        }
        for (Object obj : collection) {
            if (!elementType.isInstance(obj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Type getJavaType() {
        if (javaType == null) synchronized (this) {
            if (javaType != null) {
                return javaType;
            }
            javaType = TypeUtil.ofList(elementType.getWrappedJavaType());
        }
        return javaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FList fList = (FList) o;

        if (varargs != fList.varargs) return false;
        return elementType.equals(fList.elementType);
    }

    @Override
    public int hashCode() {
        if (hashcode == Integer.MIN_VALUE) synchronized (this) {
            if (hashcode != Integer.MIN_VALUE) {
                return hashcode;
            }
            int result = elementType.hashCode();
            result = 31 * result + (varargs ? 1 : 0);
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
            toString = getName() + "<" + elementType + ">";
        }
        return toString;
    }

    public static FList of(FType type) {
        return new FList(type);
    }

    public static FList ofVars(FType type) {
        return new FList(type, true);
    }

    public static FList of(Type type) {
        return new FList(FType.of(type));
    }

    public static FList of(Object obj) {
        if (obj == null) {
            return ANY;
        }
        if (obj.getClass().isArray()) {
            return of(FType.of(obj.getClass().getComponentType()));
        }
        if (!(obj instanceof Collection<?> collection)) {
            return of(FType.of(obj));
        }
        if (collection.isEmpty()) {
            return ANY;
        }
        FType type = FNull.NULL;
        for (Object o : collection) {
            FType targetType = FType.of(o);
            if (targetType == null) {
                throw new IllegalArgumentException("unsupported type: " + o.getClass());
            }
            type = type.minSuper(targetType);
        }
        return of(type);
    }
}