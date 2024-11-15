package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.experimental.UtilityClass;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;

/**
 * @author muyuanjin
 */
@UtilityClass
final class ASTUtil {
    public static Token[] tokens(ParserRuleContext context, TokenStream input) {
        int start = context.start.getTokenIndex();
        int end = context.stop.getTokenIndex();
        Token[] result = new Token[end - start + 1];
        for (int i = start; i <= end; i++) {
            result[i - start] = input.get(i);
        }
        return result;
    }

    public static Token[] tokens(TerminalNode... nodes) {
        Token[] result = new Token[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            result[i] = nodes[i].getSymbol();
        }
        return result;
    }

    @SafeVarargs
    public static FType check(ParserRuleContext ctx, Class<? extends FType>... expectedTypes) {
        FType type = typeOf(ctx);
        if (expectedTypes.length == 0) {
            return type;
        }
        for (Class<? extends FType> expectedType : expectedTypes) {
            if (expectedType.isAssignableFrom(type.getClass())) {
                return type;
            }
        }
        int startLine = ctx.start.getLine();
        int charPositionInLine = ctx.start.getCharPositionInLine();
        throw new IllegalArgumentException("type must be one of " + Arrays.toString(expectedTypes) + ", but got " + type + " at " + startLine + ":" + charPositionInLine);
    }

    public static FType check(ParserRuleContext ctx, FType... expectedTypes) {
        FType type = typeOf(ctx);
        if (expectedTypes.length == 0) {
            return type;
        }
        for (FType expectedType : expectedTypes) {
            if (type.equals(expectedType)) {
                return type;
            }
        }
        int startLine = ctx.start.getLine();
        int charPositionInLine = ctx.start.getCharPositionInLine();
        throw new IllegalArgumentException("type must be one of " + Arrays.toString(expectedTypes) + ", but got " + type + " at " + startLine + ":" + charPositionInLine);
    }

    public static FType typeOf(ParserRuleContext context) {
        if (context instanceof FEELParser.UnaryTestsContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.PositiveUnaryTestsContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.PositiveUnaryTestContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.ExpressionUnitContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.ExpressionContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.SimpleLiteralContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.FeelTypeContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.NameRefContext ctx) {
            return ctx.type;
        }
        if (context instanceof FEELParser.NameDefContext ctx) {
            return ctx.type;
        }
        throw new IllegalArgumentException("cannot determine type of " + context.getClass().getSimpleName());
    }
}