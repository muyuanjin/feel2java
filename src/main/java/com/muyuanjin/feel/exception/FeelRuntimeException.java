package com.muyuanjin.feel.exception;

import com.muyuanjin.feel.lang.ast.ASTDoc;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author muyuanjin
 */
public class FeelRuntimeException extends FeelException {
    public FeelRuntimeException(ASTDoc position, String message) {
        super(position, message);
    }

    public FeelRuntimeException(ASTDoc position, String message, Throwable cause) {
        super(position, message, cause);
    }

    public FeelRuntimeException(int line, int charPositionInLine, String message) {
        super(line, charPositionInLine, message);
    }

    public FeelRuntimeException(int line, int charPositionInLine, String message, Throwable cause) {
        super(line, charPositionInLine, message, cause);
    }

    public FeelRuntimeException(int startLine, int endLine, int startColumn, int endColumn, String message) {
        super(startLine, endLine, startColumn, endColumn, message);
    }

    public FeelRuntimeException(int startLine, int endLine, int startColumn, int endColumn, String message, Throwable cause) {
        super(startLine, endLine, startColumn, endColumn, message, cause);
    }

    public FeelRuntimeException(ParserRuleContext context, String message) {
        super(context, message);
    }

    public FeelRuntimeException(ParserRuleContext context, String message, Throwable cause) {
        super(context, message, cause);
    }
}
