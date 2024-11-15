package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.parser.ParserUtil;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class NumberNode extends BaseASTNode implements AstNodeLiteral {
    private BigDecimal value;

    public NumberNode(FEELParser.IntegerLiteralContext context, TokenStream input) {
        super(context, context.IntegerLiteral(), FTypes.INTEGER);
    }

    public NumberNode(FEELParser.FloatLiteralContext context, TokenStream input) {
        super(context, context.FloatingPointLiteral(), FTypes.DOUBLE, FTypes.BIG_DECIMAL);
        if (context.value instanceof BigDecimal v) {
            this.value = v;
        }
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    public BigDecimal getValue() {
        if (value == null) {
            value = ParserUtil.parserJavaNumber(text);
        }
        return value;
    }
}
