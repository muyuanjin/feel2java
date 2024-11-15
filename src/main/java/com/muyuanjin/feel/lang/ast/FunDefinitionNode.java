package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class FunDefinitionNode extends BaseASTNode {
    public LinkedHashMap<String, FType> params;
    public boolean external;
    public ASTNode body;

    public FunDefinitionNode(FEELParser.FunctionDefinitionContext ctx, TokenStream input, ASTNode body) {
        super(ctx, input);
        this.body = body;
        this.external = ctx.EXTERNAL() != null;
        this.params = MapUtil.newLinkedHashMap(ctx.nameDef().size());
        for (FEELParser.NameDefContext context : ctx.nameDef()) {
            this.params.put(context.name, context.type);
        }
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(body);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{(params=");
        for (var entry : params.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("), body=").append(body);
        sb.append(")}");
        return sb.toString();
    }
}
