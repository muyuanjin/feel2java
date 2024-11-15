package com.muyuanjin.common.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.muyuanjin.compiler.util.JMethods;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

/**
 * @author muyuanjin
 */
@UtilityClass
public final class ReflectionUtil {
    private static final LazyLog log = LazyLog.of(ReflectionUtil.class);
    private static final LazyRef<Cache<Object, Object>> CACHE = LazyRef.of(() -> Caffeine.newBuilder().weakKeys().maximumSize(500).build());


    public static boolean isPublic(Class<?> clazz) {
        if (!Modifier.isPublic(clazz.getModifiers())) {
            return false;
        }
        Class<?> enclosingClass = clazz.getEnclosingClass();
        return enclosingClass == null || isPublic(enclosingClass);
    }

    public static boolean canPublicAccess(Class<?> clazz) {
        return !clazz.isAnonymousClass() && !clazz.isLocalClass() && isPublic(clazz);
    }

    private static final ClassValue<Class<?>> PUBLIC_ACCESS_TYPE = new ClassValue<>() {
        @Override
        protected Class<?> computeValue(Class<?> type) {
            return Objects.requireNonNullElse(getPublicAccessType0(type), NULL.class);
        }
    };

    private static class NULL {
    }

    /**
     * 获取可访问的最大兼容性的类，一般用于自动根据参数确定合适的参数类型（比如传递的是hashMap，参数类型应该是map比较好）
     */
    @Nullable
    public static Class<?> getPublicAccessType(Class<?> clazz) {
        Class<?> target = PUBLIC_ACCESS_TYPE.get(clazz);
        if (target == NULL.class) {
            return null;
        }
        return target;
    }

    /**
     * 获取可访问的最大兼容性的类，一般用于自动根据参数确定合适的参数类型（比如传递的是hashMap，参数类型应该是map比较好）
     */
    @Nullable
    private static Class<?> getPublicAccessType0(Class<?> clazz) {
        List<Method> publicMethods = JMethods.getPublicMethods(clazz, false);
        Class<?> result = null;
        Class<?> target = clazz;
        do {
            if (canPublicAccess(target)) {
                result = target;
                if (target.isInterface()) {
                    return target;
                }
            }
            for (Class<?> anInterface : target.getInterfaces()) {
                if (canPublicAccess(anInterface) && JMethods.publicMethodsContainsAll(anInterface, publicMethods)) {
                    return anInterface;
                }
            }
            target = target.getSuperclass();
        } while (JMethods.publicMethodsContainsAll(target, publicMethods));
        return result;
    }
}
