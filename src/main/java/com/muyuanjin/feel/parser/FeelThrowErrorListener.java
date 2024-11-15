package com.muyuanjin.feel.parser;

import com.muyuanjin.feel.exception.FeelLangException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * @author muyuanjin
 */
public class FeelThrowErrorListener extends BaseErrorListener {
    public static final FeelThrowErrorListener INSTANCE = new FeelThrowErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new FeelLangException(line, charPositionInLine, msg);
    }
}