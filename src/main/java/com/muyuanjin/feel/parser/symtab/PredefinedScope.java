package com.muyuanjin.feel.parser.symtab;

import jakarta.validation.constraints.NotNull;

/**
 * 用于保存语言预定义符号的作用域。这可以是类型名称（如 int）或方法（如 print）的列表。
 */
class PredefinedScope extends BaseScope {
    @Override
    public @NotNull String getName() {
        return "<predefined>";
    }
}
