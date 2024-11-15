package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class DateTimeNode extends BaseASTNode implements AstNodeLiteral {
    public String literal;

    public DateTimeNode(FEELParser.DateTimeLiteralContext ctx, TokenStream input) {
        super(ctx, input, FTypes.DAY_TIME_DURATION, FTypes.YEAR_MONTH_DURATION, FTypes.DATE_TIME, FTypes.TIME, FTypes.DATE);
        this.literal = ctx.StringLiteral().getSymbol().getText();
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }
}
