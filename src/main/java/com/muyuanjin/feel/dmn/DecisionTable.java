package com.muyuanjin.feel.dmn;

/**
 * @author muyuanjin
 */
public interface DecisionTable<T> {
    String getSource();

    EvalResult evaluate(T input);

    DecisionTableDefinition getDefinition();
}