package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class NullNode extends BaseASTNode implements AstNodeLiteral {
    public NullNode(FEELParser.NullLiteralContext ctx) {
        super(ctx, ctx.NULL(), FTypes.NULL);
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }
}