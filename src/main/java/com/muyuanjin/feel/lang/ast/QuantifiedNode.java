package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class QuantifiedNode extends BaseASTNode {
    public boolean every; //every or some
    public List<Iteration> iterations;
    public ASTNode judge;

    public QuantifiedNode(FEELParser.QuantifiedExpressionContext ctx, TokenStream input, List<Iteration> iterations, ASTNode judge) {
        super(ctx, input);
        this.every = ctx.mo.getType() == FEELParser.EVERY;
        this.iterations = iterations;
        this.judge = judge;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(judge);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        if (every) {
            sb.append("every").append(", ");
        } else {
            sb.append("some").append(", ");
        }
        sb.append("iterations=").append(iterations).append(", ");
        sb.append("judge=").append(judge).append(", ");
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
