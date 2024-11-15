package com.muyuanjin.feel.dmn;

import com.muyuanjin.feel.dmn.impl.DefaultDMNCompiler;

/**
 * @author muyuanjin
 */
public interface DMNCompiler {
    <T> DecisionTable<T> compile(DecisionTableDefinition definition, T input);

    <T> DecisionTable<T> compile(DecisionTableDefinition definition, Class<T> inputType);

    DMNCompiler DEFAULT = new DefaultDMNCompiler();
}