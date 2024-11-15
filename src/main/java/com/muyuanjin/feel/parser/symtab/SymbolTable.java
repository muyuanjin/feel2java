package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.FeelFunctionFactory;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.lang.FType;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.time.Duration;

/**
 * 符号表，用于保存符号字典树和作用域栈。<p>
 * FEEL 表达式语言具备上下文相关语法，因此需要符号表辅助解析过程
 *
 * @author muyuanjin
 */
@Getter
class SymbolTable {
    /**
     * 无效类型，用于标记解析错误
     */
    public static final InvalidType INVALID = new InvalidType();
    /**
     * 共享不变的预定义作用域
     */
    public static final PredefinedScope PREDEFINED = new UnmodifiablePredefinedScope();
    /**
     * 符号字典树对象池，用于复用符号字典树
     */
    private static final GenericObjectPool<SymbolTrie> SYMBOL_TRIE_POOL;

    static {
        GenericObjectPoolConfig<SymbolTrie> config = new GenericObjectPoolConfig<>();
        // 设置最大池对象总数 无限制
        config.setMaxTotal(-1);
        // 设置最大空闲对象数
        config.setMaxIdle(8);
        // 设置最小空闲对象数
        config.setMinIdle(0);
        // 获取当池已用完时（已达到 "活动 "对象的最大数量）调用borrowObject()方法时是否阻塞。
        config.setBlockWhenExhausted(false);
        // 空闲对象驱逐器线程运行之间的时间间隔。如果为负值，则不运行空闲对象驱逐器线程。
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        // 对象在池中的最小空闲时间，达到此值后空闲对象可能会被驱逐
        config.setMinEvictableIdleDuration(Duration.ofSeconds(60));
        // 使用配置创建对象池
        SYMBOL_TRIE_POOL = new GenericObjectPool<>(new SymbolTrieFactory(), config);
    }
    /*========================================================================================================================*/
    /**
     * 预定义作用域
     */
    private PredefinedScope predefined;
    /**
     * 从共享预定义域中获取符号字典树的拷贝，拷贝的速度比重新构建快
     */
    private GlobalScope globals;
    /**
     * 符号字典树，在多个作用域共享
     */
    private SymbolTrie symbolTrie;

    /**
     * 当前作用域，作用域栈的栈顶
     */
    private Scope currentScope;

    private boolean started = false;

    public SymbolTable() {
        start();
    }

    @SneakyThrows
    public void start() {
        if (this.started) {
            return;
        }
        this.started = true;
        this.predefined = PREDEFINED;
        this.globals = new GlobalScope(PREDEFINED, SYMBOL_TRIE_POOL.borrowObject());
        this.symbolTrie = globals.symbolTrie;
        this.currentScope = globals;
        this.symbolTrie.releaseAll();//防止popScope没有和pushScope一一对应，完全弹出
        this.symbolTrie.mark();
    }

    public void close() {
        if (!this.started) {
            return;
        }
        this.started = false;
        this.symbolTrie.releaseAll();//防止popScope没有和pushScope一一对应，完全弹出
        SYMBOL_TRIE_POOL.returnObject(this.symbolTrie);
    }

    /**
     * 创建一个新的作用域并推入栈中作为当前作用域
     *
     * @param name 作用域名称
     */
    public void pushScope(String name) {
        symbolTrie.mark();//由于解析过程中嵌套的作用域共享字典树，所以需要标记字典树状态
        currentScope = new LocalScope(name, currentScope, symbolTrie);//创建新的作用域，并且共享字典树
    }

    /**
     * 弹出当前作用域，并恢复字典树的状态，如果解析存在错误，popScope未必和pushScope一一对应
     */
    public void popScope() {
        currentScope = currentScope.getParentScope();//弹出作用域
        symbolTrie.release();//重置字典树状态
    }

    public void defineGlobalSymbol(Symbol s) {
        start();
        globals.define(s);
    }

    private static final class UnmodifiablePredefinedScope extends PredefinedScope {
        public UnmodifiablePredefinedScope() {
            for (FType type : FeelTypeFactory.FACTORY.getTypes().values()) {
                super.define(new TypeSymbol(type.getName(), type));
            }
            for (var entry : FeelFunctionFactory.FACTORY.getFunctions().entrySet()) {
                super.define(new FunctionSymbol(entry.getKey(), entry.getValue()));
            }
        }

        @Override
        public void setParentScope(@Nullable Scope parentScope) {
            throw new UnsupportedOperationException("Cannot set parent scope in predefined scope");
        }

        @Override
        public void setSymbolTrie(@NotNull SymbolTrie symbolTrie) {
            throw new UnsupportedOperationException("Cannot set symbol trie in predefined scope");
        }

        @Override
        public void define(@NotNull Symbol sym) {
            throw new UnsupportedOperationException("Cannot define symbol in predefined scope");
        }
    }


    static class SymbolTrieFactory extends BasePooledObjectFactory<SymbolTrie> {
        @Override
        public SymbolTrie create() {
            return PREDEFINED.symbolTrie.copy();
        }

        @Override
        public PooledObject<SymbolTrie> wrap(SymbolTrie obj) {
            return new DefaultPooledObject<>(obj);
        }
    }
}