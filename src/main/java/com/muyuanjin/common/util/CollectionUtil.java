package com.muyuanjin.common.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author muyuanjin
 */
@UtilityClass
public class CollectionUtil {
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static boolean isEmpty(Iterable<?> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }

    public static boolean isNotEmpty(Iterable<?> iterable) {
        return !isEmpty(iterable);
    }

    public static boolean isEmpty(Iterator<?> iterator) {
        return iterator == null || !iterator.hasNext();
    }

    public static boolean isNotEmpty(Iterator<?> iterator) {
        return !isEmpty(iterator);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Collection<?> it) {
            return isEmpty(it);
        }
        if (obj instanceof Iterable<?> it) {
            return isEmpty(it);
        }
        if (obj instanceof Iterator<?> it) {
            return isEmpty(it);
        }
        if (obj instanceof Object[] it) {
            return isEmpty(it);
        }
        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}