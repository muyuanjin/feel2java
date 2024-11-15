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
public class IfNode extends BaseASTNode {
    public ASTNode condition;
    public ASTNode then;
    public ASTNode otherwise;

    public IfNode(FEELParser.IfExpressionContext ctx, TokenStream input, ASTNode condition, ASTNode then, ASTNode otherwise) {
        super(ctx, input);
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(condition, then, otherwise);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "condition=" + condition + ", " +
               "then=" + then + ", " +
               "otherwise=" + otherwise +
               "}";
    }
}
