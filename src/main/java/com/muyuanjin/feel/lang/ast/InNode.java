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
public class InNode extends BaseASTNode {
    public ASTNode value;
    public ASTNode target;

    public InNode(FEELParser.InListContext ctx, TokenStream input, ASTNode value, ASTNode target) {
        super(ctx, input);
        this.value = value;
        this.target = target;
    }

    public InNode(FEELParser.InSingleContext ctx, TokenStream input, ASTNode value, ASTNode target) {
        super(ctx, input);
        this.value = value;
        this.target = target;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(value, target);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        sb.append("value=").append(value).append(", ");
        sb.append("target=").append(target).append(", ");
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
