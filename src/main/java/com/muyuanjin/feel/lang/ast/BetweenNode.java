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
public class BetweenNode extends BaseASTNode {
    public ASTNode value;
    public ASTNode start;
    public ASTNode end;

    public BetweenNode(FEELParser.BetweenContext ctx, TokenStream input, ASTNode value, ASTNode start, ASTNode end) {
        super(ctx, input);
        this.value = value;
        this.start = start;
        this.end = end;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(value, start, end);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "value=" + value + ", " +
               "start=" + start + ", " +
               "end=" + end +
               "}";
    }
}
