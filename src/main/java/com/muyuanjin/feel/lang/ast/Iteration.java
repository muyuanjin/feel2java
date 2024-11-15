package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.annotation.Nullable;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class Iteration {
    public String name;
    public ASTNode start;
    public @Nullable ASTNode end;

    public Iteration(FEELParser.IterationContextContext ctx, TokenStream input, ASTNode start) {
        this.name = ctx.nameDef.name;
        this.start = start;
    }

    public Iteration(FEELParser.IterationContextContext ctx, TokenStream input, ASTNode start, @Nullable ASTNode end) {
        this.name = ctx.nameDef.name;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        sb.append("name=").append(name).append(", ");
        sb.append("start=").append(start).append(", ");
        if (end != null) {
            sb.append("end=").append(end).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
