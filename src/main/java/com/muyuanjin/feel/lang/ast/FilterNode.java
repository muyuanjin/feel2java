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
public class FilterNode extends BaseASTNode {
    public String itemName = "item";
    public ASTNode target;
    public ASTNode filter;

    public FilterNode(FEELParser.FilterExpressionContext ctx, TokenStream input, ASTNode target, ASTNode filter) {
        super(ctx, input);
        this.target = target;
        this.filter = filter;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(target, filter);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "target=" + target + ", " +
               "filter=" + filter +
               "}";
    }
}