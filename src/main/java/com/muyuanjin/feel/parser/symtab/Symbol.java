package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.lang.FType;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * 一种通用的编程语言符号。一个符号必须有一个名称和一个作用域。了解符号添加到作用域的顺序也很有帮助，因为这通常会转化为寄存器或参数的编号。
 */
sealed interface Symbol extends Serializable permits TypeSymbol, FunctionSymbol, VariableSymbol {
    String getName();

    FType getType();

    /**
     * Name 属性的被词法分析器识别的令牌列表，用于后续字典树匹配
     *
     * @return 令牌列表
     */
    List<String> getTokens();

    @SuppressWarnings("ClassEscapesDefinedScope")
    interface Node {
        @Nullable
        TypeSymbol getTypeSymbol();

        @Nullable
        FunctionSymbol getFunSymbol();

        @Nullable
        VariableSymbol getVarSymbol();

        @Nullable
        default String getName() {
            if (getTypeSymbol() != null) {
                return getTypeSymbol().getName();
            } else if (getFunSymbol() != null) {
                return getFunSymbol().getName();
            } else if (getVarSymbol() != null) {
                return getVarSymbol().getName();
            }
            return null;
        }

        @Nullable
        default List<String> getTokens() {
            if (getTypeSymbol() != null) {
                return getTypeSymbol().getTokens();
            } else if (getFunSymbol() != null) {
                return getFunSymbol().getTokens();
            } else if (getVarSymbol() != null) {
                return getVarSymbol().getTokens();
            }
            return null;
        }
    }
}