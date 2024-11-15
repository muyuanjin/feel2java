package com.muyuanjin.feel.parser;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import com.muyuanjin.feel.parser.antlr4.FEELBaseListener;
import com.muyuanjin.feel.parser.antlr4.FEELParser.*;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static com.muyuanjin.common.util.ColorUtil.RESET;
import static com.muyuanjin.common.util.ColorUtil.YELLOW_BRIGHT;

/**
 * 语法高亮监听器<P>
 * <div style="top: 35px; left: 25px; height: 80px; width: 400px;background: #d66e1e;border-top: 12px solid #fab514;border-left: 12px solid #fab514;border-right: 12px solid #fab514;border-bottom: 12px solid #fab514;">
 *   <span style="color:#dff0d6;font-weight:bold;font-size:32">
 *       TODO 使用html 和可配置的颜色。 可能需要创建内部树来处理嵌套颜色（决定哪个规则的染色优先级最高） 以及格式化代码
 *   </span>
 * </div>
 *
 * @author muyuanjin
 */
@Getter
@Setter
public class FeelSyntaxHighlighter extends FEELBaseListener {
    private @NonNull Config config = new Config();
    private final TokenStreamRewriter rewriter;

    public FeelSyntaxHighlighter(TokenStream stream) {
        this.rewriter = new TokenStreamRewriter(stream);
    }

    /**
     * @return 高亮后的文本
     */
    public String getTranslatedText() {
        TokenStream stream = this.rewriter.getTokenStream();
        //处理注释
        for (int i = 0; i < stream.size(); i++) {
            Token token = stream.get(i);
            if (token.getChannel() == 2) {
                around(token, config.comment);
            }
        }
        return rewriter.getText();
    }

    @Data
    public static class Config {
        public static final Attribute JAVA_CLASS_COLOR = Attribute.TEXT_COLOR(207, 142, 109);

        private boolean stringEscaping = false;

        private String stringLiteral = Ansi.generateCode(Attribute.GREEN_TEXT());
        private String integerLiteral = Ansi.generateCode(Attribute.TEXT_COLOR(42, 172, 184));
        private String floatLiteral = Ansi.generateCode(Attribute.TEXT_COLOR(42, 172, 184));
        private String booleanLiteral = Ansi.generateCode(JAVA_CLASS_COLOR);
        private String dateTimeLiteral = Ansi.generateCode(Attribute.ITALIC(), Attribute.GREEN_TEXT());
        private String nullLiteral = Ansi.generateCode(JAVA_CLASS_COLOR);

        private String functionCall = Ansi.generateCode(Attribute.ITALIC(), Attribute.UNDERLINE(), Attribute.BRIGHT_WHITE_TEXT());

        private String varDefine = Ansi.generateCode(Attribute.BRIGHT_BLUE_TEXT());
        private String varReference = Ansi.generateCode(Attribute.BOLD(), Attribute.ITALIC(), Attribute.UNDERLINE());

        private String type = Ansi.generateCode(Attribute.BRIGHT_CYAN_TEXT());

        private String symbol;
        private String keyword = Ansi.generateCode(JAVA_CLASS_COLOR);
        private String operator = Ansi.generateCode(JAVA_CLASS_COLOR);

        private String comment = Ansi.generateCode(Attribute.TEXT_COLOR(242));
    }

    @Override
    public void enterUnaryTests(UnaryTestsContext ctx) {
        around(ctx.NOT(), config.keyword);
        around(ctx.LPAREN(), config.symbol);
        around(ctx.RPAREN(), config.symbol);
        around(ctx.SUB(), config.keyword);
    }

    @Override
    public void enterPositiveUnaryTests(PositiveUnaryTestsContext ctx) {
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterParens(ParensContext ctx) {
        around(ctx.LPAREN(), config.symbol);
        around(ctx.RPAREN(), config.symbol);
    }

    @Override
    public void enterCompare(CompareContext ctx) {
        around(ctx.EQUAL(), config.keyword);
        around(ctx.NOTEQUAL(), config.keyword);
        around(ctx.LE(), config.operator);
        around(ctx.LT(), config.operator);
        around(ctx.GE(), config.operator);
        around(ctx.GT(), config.operator);
    }

    @Override
    public void enterAddOrSub(AddOrSubContext ctx) {
        around(ctx.ADD(), config.operator);
        around(ctx.SUB(), config.operator);
    }

    @Override
    public void enterFunctionInvocation(FunctionInvocationContext ctx) {
        if (config.functionCall != null) {
            rewriter.insertBefore(ctx.expression().start, RESET + config.functionCall);
            reset(ctx.expression());
        }
        around(ctx.LPAREN(), config.symbol);
        around(ctx.RPAREN(), config.symbol);
    }

    @Override
    public void enterInstanceOf(InstanceOfContext ctx) {
        around(ctx.INSTANCE(), config.keyword);
        around(ctx.OF(), config.keyword);
    }

    @Override
    public void enterJunction(JunctionContext ctx) {
        around(ctx.op, config.operator);
    }

    @Override
    public void enterContext(ContextContext ctx) {
        around(ctx.LBRACE(), config.symbol);
        around(ctx.RBRACE(), config.symbol);
        around(ctx.COLON(), config.symbol);
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterIfExpression(IfExpressionContext ctx) {
        around(ctx.IF(), config.keyword);
        around(ctx.THEN(), config.keyword);
        around(ctx.ELSE(), config.keyword);
    }

    @Override
    public void enterForExpression(ForExpressionContext ctx) {
        around(ctx.FOR(), config.keyword);
        around(ctx.RETURN(), config.keyword);
    }

    @Override
    public void enterQuantifiedExpression(QuantifiedExpressionContext ctx) {
        around(ctx.EVERY(), config.keyword);
        around(ctx.SOME(), config.keyword);
        around(ctx.SATISFIES(), config.keyword);
    }

    @Override
    public void enterIterationContexts(IterationContextsContext ctx) {
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterIterationContext(IterationContextContext ctx) {
        around(ctx.IN(), config.keyword);
        around(ctx.ELIPSIS(), config.keyword);
    }

    @Override
    public void enterBetween(BetweenContext ctx) {
        around(ctx.BETWEEN(), config.keyword);
        around(ctx.AND(), config.keyword);
    }

    @Override
    public void enterFunctionDefinition(FunctionDefinitionContext ctx) {
        around(ctx.FUNCTION(), config.keyword);
        around(ctx.LPAREN(), config.symbol);
        around(ctx.RPAREN(), config.symbol);
        around(ctx.EXTERNAL(), config.keyword);
        around(ctx.COLON(), config.symbol);
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterInSingle(InSingleContext ctx) {
        around(ctx.IN(), config.keyword);
    }

    @Override
    public void enterList(ListContext ctx) {
        around(ctx.LBRACK(), config.symbol);
        around(ctx.RBRACK(), config.symbol);
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterMultiOrDiv(MultiOrDivContext ctx) {
        around(ctx.DIV(), config.operator);
    }

    @Override
    public void enterArithmeticNegation(ArithmeticNegationContext ctx) {
        around(ctx.SUB(), config.operator);
    }

    @Override
    public void enterExponentiation(ExponentiationContext ctx) {
        around(ctx.POW(), config.operator);
    }

    @Override
    public void enterFilterExpression(FilterExpressionContext ctx) {
        around(ctx.LBRACK(), config.symbol);
        around(ctx.RBRACK(), config.symbol);
    }

    @Override
    public void enterBoundedInterval(BoundedIntervalContext ctx) {
        around(ctx.ELIPSIS(), config.keyword);
        around(ctx.LPAREN(), config.symbol);
        around(ctx.RPAREN(), config.symbol);
        around(ctx.LBRACK(), config.symbol);
        around(ctx.RBRACK(), config.symbol);
    }

    @Override
    public void enterUnboundedInterval(UnboundedIntervalContext ctx) {
        around(ctx.LE(), config.operator);
        around(ctx.LT(), config.operator);
        around(ctx.GE(), config.operator);
        around(ctx.GT(), config.operator);
    }

    @Override
    public void enterInList(InListContext ctx) {
        around(ctx.IN(), config.keyword);
        around(ctx.LPAREN(), config.symbol);
        around(ctx.RPAREN(), config.symbol);
    }

    @Override
    public void enterPathExpression(PathExpressionContext ctx) {
        around(ctx.DOT(), config.symbol);
    }

    @Override
    public void enterKeyName(KeyNameContext ctx) {
        if (config.varDefine != null) {
            rewriter.insertBefore(ctx.start, config.varDefine);
            reset(ctx);
        }
    }

    @Override
    public void enterKeyString(KeyStringContext ctx) {
        if (config.varDefine != null) {
            rewriter.insertBefore(ctx.start, config.varDefine);
            reset(ctx);
        }
    }

    @Override
    public void enterNamedParameters(NamedParametersContext ctx) {
        around(ctx.COLON(), config.symbol);
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterPositionalParameters(PositionalParametersContext ctx) {
        around(ctx.COMMA(), config.symbol);
    }

    @Override
    public void enterIntegerLiteral(IntegerLiteralContext ctx) {
        around(ctx.IntegerLiteral(), config.integerLiteral);
    }

    @Override
    public void enterFloatLiteral(FloatLiteralContext ctx) {
        around(ctx.FloatingPointLiteral(), config.floatLiteral);
    }

    @Override
    public void enterBooleanLiteral(BooleanLiteralContext ctx) {
        around(ctx.BooleanLiteral(), config.booleanLiteral);
    }

    @Override
    public void enterDateTimeLiteral(DateTimeLiteralContext ctx) {
        around(ctx.AT(), RESET + config.keyword);
        if (!config.stringEscaping) {
            Token token = ctx.StringLiteral().getSymbol();
            rewriter.delete(token);
            String rawText = token.getTokenSource().getInputStream().getText(Interval.of(token.getStartIndex(), token.getStopIndex()));
            if (config.keyword != null && !config.keyword.equals(config.dateTimeLiteral)) {
                rewriter.insertBefore(token, getHighlightedRawText(rawText, config.keyword, config.dateTimeLiteral));
            } else {
                rewriter.insertBefore(token, rawText);
            }
            if (config.dateTimeLiteral != null) {
                rewriter.insertBefore(token, RESET + config.dateTimeLiteral);
            }
            if (config.dateTimeLiteral != null) {
                rewriter.insertAfter(token, RESET);
            }
        } else if (config.dateTimeLiteral != null) {
            rewriter.insertBefore(ctx.StringLiteral().getSymbol().getStartIndex(), RESET + config.dateTimeLiteral);
            rewriter.insertAfter(ctx.StringLiteral().getSymbol().getStopIndex(), RESET);
        }
    }

    @Override
    public void enterStringLiteral(StringLiteralContext ctx) {
        if (!config.stringEscaping) {
            Token token = ctx.StringLiteral().getSymbol();
            rewriter.delete(token);
            String rawText = token.getTokenSource().getInputStream().getText(Interval.of(token.getStartIndex(), token.getStopIndex()));
            if (config.keyword != null && !config.keyword.equals(config.stringLiteral)) {
                rewriter.insertBefore(token, getHighlightedRawText(rawText, config.keyword, config.stringLiteral));
            } else {
                rewriter.insertBefore(token, rawText);
            }
            if (config.stringLiteral != null) {
                rewriter.insertBefore(token, config.stringLiteral);
            }
            if (config.stringLiteral != null) {
                rewriter.insertAfter(token, RESET);
            }
        } else if (config.stringLiteral != null) {
            rewriter.insertBefore(ctx.start, config.stringLiteral);
            reset(ctx);
        }
    }

    @Override
    public void enterNullLiteral(NullLiteralContext ctx) {
        around(ctx.NULL(), config.nullLiteral);
    }

    @Override
    public void enterQnType(QnTypeContext ctx) {
        if (config.type != null) {
            around(ctx.DOT(), config.symbol);
            around(ctx.nameRef(), RESET + config.type);
        }
    }

    @Override
    public void enterListType(ListTypeContext ctx) {
        if (config.type != null) {
            around(ctx.LIST(), RESET + config.type);
            around(ctx.LT(), config.symbol);
            around(ctx.GT(), config.symbol);
        }
    }

    @Override
    public void enterRangeType(RangeTypeContext ctx) {
        if (config.type != null) {
            around(ctx.RANGE(), RESET + config.type);
            around(ctx.LT(), config.symbol);
            around(ctx.GT(), config.symbol);
        }
    }

    @Override
    public void enterContextType(ContextTypeContext ctx) {
        if (config.type != null) {
            around(ctx.CONTEXT(), RESET + config.type);
            around(ctx.LT(), config.symbol);
            around(ctx.GT(), config.symbol);
        }
    }

    @Override
    public void enterFunctionType(FunctionTypeContext ctx) {
        around(ctx.FUNCTION(), config.type);
        around(ctx.LT(), config.symbol);
        around(ctx.GT(), config.symbol);
        around(ctx.COMMA(), config.symbol);
        around(ctx.RARROW(), config.symbol);
    }

    @Override
    public void enterNameDef(NameDefContext ctx) {
        if (config.varDefine != null) {
            rewriter.insertBefore(ctx.start, config.varDefine);
            reset(ctx);
        }
    }

    @Override
    public void enterNameRef(NameRefContext ctx) {
        if (config.varReference != null) {
            rewriter.insertBefore(ctx.start, config.varReference);
            reset(ctx);
        }
    }

    private void reset(ParserRuleContext context) {
        rewriter.insertAfter(context.stop, RESET);
    }

    private void around(List<TerminalNode> nodes, String star) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (TerminalNode node : nodes) {
            if (node != null && star != null) {
                rewriter.insertBefore(node.getSymbol(), star);
                rewriter.insertAfter(node.getSymbol(), RESET);
            }
        }
    }

    private void around(ParserRuleContext context, String star) {
        if (context == null) {
            return;
        }
        rewriter.insertBefore(context.start, star);
        rewriter.insertAfter(context.stop, RESET);
    }

    private void around(List<? extends ParserRuleContext> contexts, String star, Void... voids) {
        if (contexts == null || contexts.isEmpty()) {
            return;
        }
        for (ParserRuleContext context : contexts) {
            if (context != null && star != null) {
                rewriter.insertBefore(context.start, star);
                rewriter.insertAfter(context.stop, RESET);
            }
        }
    }

    private void around(TerminalNode token) {
        around(token, YELLOW_BRIGHT);
    }

    private void around(TerminalNode token, String star) {
        if (token != null && star != null) {
            rewriter.insertBefore(token.getSymbol(), star);
            rewriter.insertAfter(token.getSymbol(), RESET);
        }
    }


    @SuppressWarnings("SameParameterValue")
    private void around(Token token, String star) {
        if (token != null && star != null) {
            rewriter.insertBefore(token, star);
            rewriter.insertAfter(token, RESET);
        }
    }

    @NotNull
    private static String getHighlightedRawText(String rawText, String escapingColor, String textColor) {
        if (escapingColor == null) {
            escapingColor = "";
        }
        StringBuilder builder = new StringBuilder();
        int resetIndex = 0;
        for (int i = 0; i < rawText.length(); i++) {
            char c = rawText.charAt(i);
            switch (c) {
                case '\\' -> {
                    builder.append(escapingColor);
                    resetIndex = i + 1;
                }
                case 'u' -> {
                    if (i == resetIndex) {
                        resetIndex += 4;
                    }
                }
                case 'U' -> {
                    if (i == resetIndex) {
                        resetIndex += 6;
                    }
                }
            }
            builder.append(c);
            if (i == resetIndex) {
                builder.append(RESET);
                if (textColor != null) {
                    builder.append(textColor);
                }
            }
        }
        return builder.toString();
    }
}
