package com.muyuanjin.feel.exception;

import com.muyuanjin.feel.lang.ast.ASTDoc;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author muyuanjin
 */
@Slf4j
@Getter
public class FeelException extends RuntimeException {
    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
    public static boolean fillInStackTrace = false;

    public FeelException(ASTDoc position, String message) {
        super(formatMessage(position, message), null, false, DEBUG_ENABLED);
    }

    public FeelException(ASTDoc position, String message, Throwable cause) {
        super(formatMessage(position, message), cause, false, DEBUG_ENABLED);
    }

    public FeelException(int line, int charPositionInLine, String message) {
        super(formatMessage(line, charPositionInLine, message), null, false, DEBUG_ENABLED);
    }

    public FeelException(int line, int charPositionInLine, String message, Throwable cause) {
        super(formatMessage(line, charPositionInLine, message), cause, false, DEBUG_ENABLED);
    }

    public FeelException(int startLine, int endLine, int startColumn, int endColumn, String message) {
        super(formatMessage(startLine, endLine, startColumn, endColumn, message), null, false, DEBUG_ENABLED);
    }

    public FeelException(int startLine, int endLine, int startColumn, int endColumn, String message, Throwable cause) {
        super(formatMessage(startLine, endLine, startColumn, endColumn, message), cause, false, DEBUG_ENABLED);
    }

    public FeelException(ParserRuleContext context, String message) {
        super(formatMessage(context, message));
    }

    public FeelException(ParserRuleContext context, String message, Throwable cause) {
        super(formatMessage(context, message), cause);
    }

    @Override
    public final synchronized Throwable fillInStackTrace() {
        if (!DEBUG_ENABLED && !fillInStackTrace) {
            return this; // super call is too expensive
        }
        return super.fillInStackTrace();
    }

    private static String formatMessage(ParserRuleContext context, String message) {
        return formatMessage(
                context.getStart().getLine(),
                context.getStop().getLine(),
                context.getStart().getCharPositionInLine(),
                context.getStop().getCharPositionInLine(),
                message
        );
    }

    private static String formatMessage(ASTDoc position, String message) {
        return formatMessage(
                position.getStartLine(),
                position.getEndLine(),
                position.getStartColumn(),
                position.getEndColumn(),
                message
        );
    }

    private static String formatMessage(int startLine, int endLine, int startColumn, int endColumn, String message) {
        boolean oneLine = startLine == endLine;
        boolean oneColumn = startColumn == endColumn;
        if (oneLine && oneColumn) {
            return String.format("Line %d, column %d: %s", startLine, startColumn, message);
        } else if (oneLine) {
            return String.format("Line %d, column %d-%d: %s", startLine, startColumn, endColumn, message);
        } else {
            return String.format("Line %d-%d, column %d-%d: %s", startLine, endLine, startColumn, endColumn, message);
        }
    }

    private static String formatMessage(int line, int charPositionInLine, String message) {
        return String.format("Line %d, column %d: %s", line, charPositionInLine, message);
    }
}
