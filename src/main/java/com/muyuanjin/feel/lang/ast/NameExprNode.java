package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class NameExprNode extends BaseASTNode {
    public String name;

    public NameExprNode(FEELParser.NameExpressionContext ctx, TokenStream input) {
        super(ctx, input);
        this.name = ctx.nameRef.name;
    }

    public NameExprNode(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }
}
