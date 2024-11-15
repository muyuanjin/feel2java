package com.muyuanjin.common.util;


import com.muyuanjin.common.function.SupplierEx;

import java.util.function.Supplier;


/**
 * @author muyuanjin
 */
public interface LazyRef<T> {
    T get();

    default T orElse(T other) {
        T t = get();
        return t != null ? t : other;
    }

    boolean isInitialized();


    /**
     * 创建懒加载容器
     *
     * @param value 对象
     * @param <T>   对象类
     * @return 对象的懒加载容器
     */
    static <T> LazyRef<T> of(T value) {
        return new Impl<>(value);
    }

    /**
     * 创建懒加载容器
     *
     * @param lazyLoader 对象创建函数
     * @param <T>        对象类
     * @return 对象的懒加载容器
     */
    static <T> LazyRef<T> of(SupplierEx<? extends T> lazyLoader) {
        return new Impl<>(lazyLoader);
    }
    /**
     * 创建懒加载容器
     *
     * @param lazyLoader 对象创建函数
     * @param <T>        对象类
     * @return 对象的懒加载容器
     */
    static <T> LazyRef<T> ofSup(Supplier<? extends T> lazyLoader) {
        return new Impl<>(lazyLoader);
    }

    /**
     * 懒加载引用容器
     */
    class Impl<T> implements LazyRef<T> {
        private volatile Supplier<? extends T> supplier;
        private T value;

        public Impl(Supplier<? extends T> supplier) {
            Assert.notNull(supplier, "supplier can not be null");
            this.supplier = supplier;
        }

        public Impl(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            if (this.supplier == null) {
                return value;
            }
            synchronized (this) {
                if (this.supplier != null) {
                    this.value = this.supplier.get();
                    this.supplier = null;
                }
            }
            return this.value;
        }

        /**
         * 是否已经调用过 supplier 执行过初始化
         */
        @Override
        public boolean isInitialized() {
            return supplier == null;
        }
    }
}