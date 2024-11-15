package com.muyuanjin.feel.exception;

import com.muyuanjin.feel.lang.ast.ASTDoc;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author muyuanjin
 */
@Getter
public class FeelLangException extends FeelException {
    public FeelLangException(ASTDoc doc, String message) {
        super(doc, message);
    }

    public FeelLangException(ASTDoc doc, String message, Throwable cause) {
        super(doc, message, cause);
    }

    public FeelLangException(int line, int charPositionInLine, String message) {
        super(line, charPositionInLine, message);
    }

    public FeelLangException(int line, int charPositionInLine, String message, Throwable cause) {
        super(line, charPositionInLine, message, cause);
    }

    public FeelLangException(ParserRuleContext context, String message) {
        super(context, message);
    }

    public FeelLangException(ParserRuleContext context, String message, Throwable cause) {
        super(context, message, cause);
    }
}
