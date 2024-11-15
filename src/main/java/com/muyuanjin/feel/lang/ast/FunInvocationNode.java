package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class FunInvocationNode extends BaseASTNode {
    public ASTNode function;
    public List<ASTNode> params;

    public FunInvocationNode(FEELParser.FunctionInvocationContext ctx, TokenStream input, ASTNode function, List<ASTNode> params) {
        super(ctx, input);
        this.function = function;
        this.params = params;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        if (params.isEmpty()) {
            return List.of(function);
        }
        List<ASTNode> children = new ArrayList<>(params.size() + 1);
        children.add(function);
        children.addAll(params);
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        sb.append(function).append("(");
        for (var param : params) {
            sb.append(param).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(")}");
        return sb.toString();
    }
}
