package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class ContextNode extends BaseASTNode {
    public LinkedHashMap<String, ASTNode> entries;

    public ContextNode(FEELParser.ContextContext ctx, TokenStream input, LinkedHashMap<String, ASTNode> entries) {
        super(ctx, input);
        this.entries = entries;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.copyOf(entries.values());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        for (Map.Entry<String, ASTNode> entry : entries.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
