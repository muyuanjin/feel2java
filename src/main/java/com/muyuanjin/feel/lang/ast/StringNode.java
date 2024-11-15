package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.Token;
import jakarta.validation.constraints.NotNull;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class StringNode extends BaseASTNode implements AstNodeLiteral {
    public String value;

    public StringNode(FEELParser.StringLiteralContext ctx) {
        super(FTypes.STRING, ctx.StringLiteral().getSymbol());
        this.value = ctx.StringLiteral().getText();
    }

    public StringNode(Token token) {
        super(FTypes.STRING, token);
        this.value = token.getText();
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }
}