package com.muyuanjin.feel.translate;

import com.sun.tools.javac.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author muyuanjin
 */
public final class Context {
    public static class Key<T> {
        // note: we inherit identity equality from Object.
    }

    public interface Factory<T> {
        T make(Context c);
    }

    private final Map<Class<?>, Key<?>> keyMap = new HashMap<>();
    private final Map<Key<?>, Object> values = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Key<T> key(Class<T> clazz) {
        Key<T> k = (Key<T>) keyMap.get(clazz);
        if (k == null) {
            k = new Key<>();
            keyMap.put(clazz, k);
        }
        return k;
    }

    public <T> T get(Class<T> clazz) {
        return get(key(clazz));
    }

    public <T> void put(Class<T> clazz, T data) {
        put(key(clazz), data);
    }

    public <T> void put(Class<T> clazz, Factory<T> fac) {
        put(key(clazz), fac);
    }

    public <T> void put(Key<T> key, Factory<T> fac) {
        Object old = values.put(key, fac);
        if (old != null)
            throw new AssertionError("duplicate context value");
    }

    public <T> void put(Key<T> key, T data) {
        if (data instanceof Factory<?>)
            throw new AssertionError("T extends Context.Factory");
        Object old = values.put(key, data);
        if (old != null && !(old instanceof Factory<?>) && old != data && data != null)
            throw new AssertionError("duplicate context value");
    }

    /** 获取该上下文中键的值. */
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        Object o = values.get(key);
        if (o instanceof Factory<?> fac) {
            o = fac.make(this);
            if (o instanceof Factory<?>)
                throw new AssertionError("T extends Context.Factory");
            Assert.check(values.get(key) == o);
        }
        return (T) o;
    }
}

