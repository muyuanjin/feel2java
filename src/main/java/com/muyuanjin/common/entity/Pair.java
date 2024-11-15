package com.muyuanjin.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author muyuanjin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<K, V> implements Map.Entry<K, V>, Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private K key;
    private V value;


    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}