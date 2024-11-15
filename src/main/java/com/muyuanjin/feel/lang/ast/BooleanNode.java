package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
public class BooleanNode extends BaseASTNode implements AstNodeLiteral {
    public boolean value;

    public BooleanNode(FEELParser.BooleanLiteralContext ctx) {
        super(FTypes.BOOLEAN, ctx.BooleanLiteral().getSymbol());
        value = "true".equalsIgnoreCase(ctx.BooleanLiteral().getText());
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

}
