package com.muyuanjin.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.muyuanjin.compiler.util.JFields;
import com.muyuanjin.compiler.util.JModules;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
@UtilityClass
public class TypeUtil {
    static {JModules.makeSureExported();}

    private static final LazyLog log = LazyLog.of(TypeUtil.class);
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[]{};
    private static final LazyRef<Cache<String, Type>> TYPE_CACHE = LazyRef.of(() -> Caffeine.newBuilder().softValues().build());
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP;
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP;

    static {
        var map = MapUtil.<Class<?>, Class<?>>newHashMap(9);
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(short.class, Short.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(double.class, Double.class);
        map.put(float.class, Float.class);
        map.put(void.class, Void.class);
        PRIMITIVE_WRAPPER_MAP = Map.copyOf(map);
        map.clear();
        for (var entry : PRIMITIVE_WRAPPER_MAP.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        WRAPPER_PRIMITIVE_MAP = Map.copyOf(map);
    }

    public static boolean isPrimitiveOrWrapper(Type type) {
        if (!(type instanceof Class<?> clazz)) {
            return false;
        }
        return clazz.isPrimitive() || isPrimitiveWrapper(type);
    }

    public static boolean isPrimitiveWrapper(Type type) {
        if (!(type instanceof Class<?> clazz)) {
            return false;
        }
        return WRAPPER_PRIMITIVE_MAP.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Type> T primitiveToWrapper(T type) {
        if (!(type instanceof Class<?> clazz)) {
            return type;
        }
        return (T) PRIMITIVE_WRAPPER_MAP.getOrDefault(clazz, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Type> T wrapperToPrimitive(T type) {
        if (!(type instanceof Class<?> clazz)) {
            return type;
        }
        return (T) WRAPPER_PRIMITIVE_MAP.getOrDefault(clazz, clazz);
    }

    public static <T> TypeReference<T> typeReference(Type type) {
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    public static Type ofList(Type elementType) {
        return makeParameterizedType(null, List.class, elementType);
    }

    public static Type ofMap(Type keyType, Type valueType) {
        return makeParameterizedType(null, Map.class, keyType, valueType);
    }

    private static final VarHandle TYPE_VARIABLE_BOUNDS = JFields.getVarHandle(TypeVariableImpl.class, "bounds");
    private static final VarHandle WILDCARD_TYPE_UPPER_BOUNDS = JFields.getVarHandle(WildcardTypeImpl.class, "upperBounds");
    private static final VarHandle WILDCARD_TYPE_LOWER_BOUNDS = JFields.getVarHandle(WildcardTypeImpl.class, "lowerBounds");

    /**
     * Factory method.
     *
     * @param declaration - the reflective object that declared the type variable
     *                    that this method should create
     * @param name        - the name of the type variable to be returned
     * @param bounds      - an array of ASTs representing the bounds for the type
     *                    variable to be created
     * @return A type variable with name, bounds, declaration and factory specified
     */
    public static <T extends GenericDeclaration> TypeVariable<T> makeTypeVariable(T declaration, String name,
                                                                                  Type[] bounds) {
        var make = TypeVariableImpl.make(declaration, name, null, null);
        TYPE_VARIABLE_BOUNDS.setVolatile(make, (Object[]) bounds);
        return make;
    }

    /**
     * Static factory. Given a (generic) class, actual type arguments
     * and an owner type, creates a parameterized type.
     * This class can be instantiated with a raw type that does not
     * represent a generic type, provided the list of actual type
     * arguments is empty.
     * If the ownerType argument is null, the declaring class of the
     * raw type is used as the owner type.
     * <p> This method throws a MalformedParameterizedTypeException
     * under the following circumstances:
     * If the number of actual type arguments (i.e., the size of the
     * array {@code typeArgs}) does not correspond to the number of
     * formal type arguments.
     * If any of the actual type arguments is not an instance of the
     * bounds on the corresponding formal.
     *
     * @param rawType             the Class representing the generic type declaration being
     *                            instantiated
     * @param actualTypeArguments a (possibly empty) array of types
     *                            representing the actual type arguments to the parameterized type
     * @param ownerType           the enclosing type, if known.
     * @return An instance of {@code ParameterizedType}
     * @throws MalformedParameterizedTypeException if the instantiation
     *                                             is invalid
     */
    public static ParameterizedType makeParameterizedType(
            @Nullable Type ownerType, Class<?> rawType, Type... actualTypeArguments) {
        return ParameterizedTypeImpl.make(rawType, actualTypeArguments, ownerType);
    }

    /**
     * Returns a new wildcard type variable. If
     * {@code ubs} is empty, a bound of {@code java.lang.Object} is used.
     *
     * @param ubs An array of abstract syntax trees representing
     *            the upper bound(s) on the type variable being declared
     * @param lbs An array of abstract syntax trees representing
     *            the lower bound(s) on the type variable being declared
     * @return a new wildcard type variable
     * @throws NullPointerException if any of the actual parameters
     *                              or any of the elements of {@code ubs} or {@code lbs} are
     *                              {@code null}
     */
    public static WildcardType makeWildcard(Type[] ubs,
                                            Type[] lbs) {
        var make = WildcardTypeImpl.make(null, null, null);
        WILDCARD_TYPE_UPPER_BOUNDS.setVolatile(make, ubs);
        WILDCARD_TYPE_LOWER_BOUNDS.setVolatile(make, lbs);
        return make;
    }

    /**
     * Returns a (possibly generic) array type.
     * If the component type is a parameterized type, it must
     * only have unbounded wildcard arguments, otherwise
     * a MalformedParameterizedTypeException is thrown.
     *
     * @param componentType - the component type of the array
     * @return a (possibly generic) array type.
     * @throws MalformedParameterizedTypeException if {@code componentType}
     *                                             is a parameterized type with non-wildcard type arguments
     * @throws NullPointerException                if any of the actual parameters
     *                                             are {@code null}
     */
    public static GenericArrayType makeArrayType(Type componentType) {
        return GenericArrayTypeImpl.make(componentType);
    }
}
