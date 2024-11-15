package com.muyuanjin.feel.lang.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.common.util.ReflectionUtil;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.parser.ParserUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author muyuanjin
 */
public final class FContext implements FType {
    private static final Type DEFAULT_JAVA_TYPE = TypeUtil.ofMap(String.class, Object.class);

    @Getter
    private final @NotNull Map<String, FType> members;

    @JsonIgnore
    private transient int hashcode = Integer.MIN_VALUE;
    @JsonIgnore
    private transient String toString;
    @JsonIgnore
    private transient Type javaType;

    public static final FContext EMPTY = new FContext();
    public static final FContext ANY = new FContext();

    private FContext() {
        this.members = Collections.emptyMap();
    }

    private FContext(LinkedHashMap<String, ? extends FType> members) {
        this.members = Collections.unmodifiableMap(members);
    }

    @Override
    public String getName() {
        return "context";
    }

    @Override
    public boolean isInstance(Object o) {
        if (o == null) {
            return false;
        }
        if (javaType instanceof Class<?> clazz) {
            return clazz.isInstance(o);
        }
        if (!(o instanceof Map<?, ?> map)) {
            return false;
        }
        for (Map.Entry<String, FType> entry : members.entrySet()) {
            Object object = map.get(entry.getKey());
            if (object == null) {
                return false;
            }
            if (!entry.getValue().isInstance(object)) {
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
            javaType = DEFAULT_JAVA_TYPE;
        }
        return javaType;
    }

    @Override
    public int canBe(FType type) {
        if (type == this) {
            return EQ;
        }
        if (type instanceof FAny) {
            return CT;
        }
        if (type instanceof FContext context) {
            int result = EQ;
            for (Map.Entry<String, FType> entry : context.members.entrySet()) {
                FType t = members.get(entry.getKey());
                if (t == null) {
                    return NO;
                }
                int canBe = t.canBe(entry.getValue());
                result = Math.max(result, canBe);
                if (result == NO) {
                    return NO;
                }
            }
            return result;
        }
        return NO;
    }

    @Override
    public FType maxSub(FType type) {
        if (equals(type)) {
            return this;
        }
        if (type instanceof FAny) {
            return this;
        }
        if (type instanceof FContext context) {
            var map = new LinkedHashMap<>(members);
            for (Map.Entry<String, FType> entry : context.members.entrySet()) {
                FType t = map.get(entry.getKey());
                if (t == null) {
                    map.put(entry.getKey(), entry.getValue());
                } else {
                    FType sup = t.maxSub(entry.getValue());
                    map.put(entry.getKey(), sup);
                }
            }
            return of(map);
        }
        return FNull.NULL;
    }

    @Override
    public FType minSuper(FType type) {
        if (equals(type)) {
            return this;
        }
        if (type instanceof FAny) {
            return type;
        }
        if (type instanceof FContext context) {
            var set = new LinkedHashSet<>(members.keySet());
            set.retainAll(context.members.keySet());
            if (set.isEmpty()) {
                return EMPTY;
            }
            var map = MapUtil.<String, FType>newLinkedHashMap(set.size());
            for (String key : set) {
                FType t = members.get(key);
                FType sub = t.minSuper(context.members.get(key));
                map.put(key, sub);
            }
            return of(map);
        }
        return FAny.ANY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FContext fContext = (FContext) o;

        return members.equals(fContext.members);
    }

    @Override
    public int hashCode() {
        if (hashcode == Integer.MIN_VALUE) synchronized (this) {
            if (hashcode != Integer.MIN_VALUE) {
                return hashcode;
            }
            hashcode = members.hashCode();
        }
        return hashcode;
    }

    @Override
    public String toString() {
        if (toString == null) synchronized (this) {
            if (toString != null) {
                return toString;
            }
            StringBuilder builder = new StringBuilder("context<");
            for (Map.Entry<String, FType> entry : members.entrySet()) {
                builder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
            if (builder.length() > 8) {
                builder.setLength(builder.length() - 1);
            }
            toString = builder.append(">").toString();
        }
        return toString;
    }


    private static final ClassValue<FContext> PROPERTY = new ClassValue<>() {
        @Override
        @SneakyThrows
        protected FContext computeValue(Class<?> clazz) {
            clazz = ReflectionUtil.getPublicAccessType(clazz);
            if (clazz == null) {
                throw new IllegalArgumentException("class [" + clazz + "] is not a public class that can be introspected");
            }
            var readMethods = ParserUtil.PROPERTY_READ.get(clazz);
            LinkedHashMap<String, FType> map = MapUtil.newLinkedHashMap(readMethods.size());
            for (var entry : readMethods.entrySet()) {
                map.put(entry.getKey(), FType.of(entry.getValue().getGenericReturnType()));
            }
            FContext fContext = new FContext(map);
            fContext.javaType = clazz;
            return fContext;
        }
    };

    public static FContext of(Map<String, ? extends FType> members) {
        return new FContext(new LinkedHashMap<>(members));
    }

    public static FContext of(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                return EMPTY;
            }
            var members = MapUtil.<String, FType>newLinkedHashMap(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    members.put(key, FType.of(entry.getValue()));
                }
            }
            return new FContext(members);
        }
        return PROPERTY.get(obj instanceof Class<?> clazz ? clazz : obj.getClass());
    }

    public static FContext of() {
        return FContext.EMPTY;
    }

    public static FContext ofVars(Object... members) {
        var map = MapUtil.<String, FType>newLinkedHashMap(members.length);
        for (int i = 0; i < members.length; i += 2) {
            map.put((String) members[i], members[i + 1] instanceof FType fType ? fType : FType.of(members[i + 1]));
        }
        return of(map);
    }

    public static FContext of(String key, FType value) {
        return ofVars(key, value);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2) {
        return ofVars(key1, value1, key2, value2);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3) {
        return ofVars(key1, value1, key2, value2, key3, value3);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4, String key5, FType value5) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4, String key5, FType value5, String key6, FType value6) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4, String key5, FType value5, String key6, FType value6, String key7, FType value7) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6, key7, value7);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4, String key5, FType value5, String key6, FType value6, String key7, FType value7, String key8, FType value8) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6, key7, value7, key8, value8);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4, String key5, FType value5, String key6, FType value6, String key7, FType value7, String key8, FType value8, String key9, FType value9) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6, key7, value7, key8, value8, key9, value9);
    }

    public static FContext of(String key1, FType value1, String key2, FType value2, String key3, FType value3, String key4, FType value4, String key5, FType value5, String key6, FType value6, String key7, FType value7, String key8, FType value8, String key9, FType value9, String key10, FType value10) {
        return ofVars(key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6, key7, value7, key8, value8, key9, value9, key10, value10);
    }

    @NotNull
    @SafeVarargs
    public static FContext of(@NotNull kotlin.Pair<String, ? extends FType>... pairs) {
        if (pairs.length == 0) {
            return EMPTY;
        }
        var map = MapUtil.<String, FType>newLinkedHashMap(pairs.length);
        for (var pair : pairs) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return of(map);
    }
}