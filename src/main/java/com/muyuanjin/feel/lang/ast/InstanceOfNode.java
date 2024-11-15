package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class InstanceOfNode extends BaseASTNode {
    public ASTNode value;
    public FType type;

    public InstanceOfNode(FEELParser.InstanceOfContext ctx, TokenStream input, ASTNode value) {
        super(ctx, input);
        this.value = value;
        this.type = ctx.feelType.type;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "value=" + value + ", " +
               "type=" + type +
               "}";
    }
}
