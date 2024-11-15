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
public class PathNode extends BaseASTNode {
    public ASTNode left;
    public String name;

    public PathNode(FEELParser.PathExpressionContext ctx, TokenStream input, ASTNode left, String name) {
        super(ctx, input);
        this.left = left;
        this.name = name;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(left);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        sb.append("left=").append(left).append(", ");
        sb.append("right=").append(name).append(", ");
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
