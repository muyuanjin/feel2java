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
public class ForNode extends BaseASTNode {
    public List<Iteration> iterations;
    public ASTNode result;

    public ForNode(FEELParser.ForExpressionContext ctx, TokenStream input, List<Iteration> iterations, ASTNode result) {
        super(ctx, input);
        this.iterations = iterations;
        this.result = result;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(result);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "iterations=" + iterations + ", " +
               "result=" + result +
               "}";
    }
}
