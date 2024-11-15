package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.type.FRange;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class RangeNode extends BaseASTNode {
    public @Nullable ASTNode start;
    public @Nullable ASTNode end;

    public RangeNode(FEELParser.UnboundedIntervalContext ctx, TokenStream input, @NotNull ASTNode startOrEnd) {
        super(ctx, input, FRange.class);
        Objects.requireNonNull(startOrEnd, "Unbounded interval must have a start or end");
        switch (ctx.bound.getType()) {
            case FEELParser.LT, FEELParser.LE -> {
                this.start = null;
                this.end = startOrEnd;
            }
            case FEELParser.GT, FEELParser.GE -> {
                this.start = startOrEnd;
                this.end = null;
            }
            default -> throw new IllegalArgumentException("Invalid unbounded interval");
        }
    }

    public RangeNode(FEELParser.BoundedIntervalContext ctx, TokenStream input, @Nullable ASTNode start, @Nullable ASTNode end) {
        super(ctx, input, FRange.class);
        this.start = start;
        this.end = end;
        if (start == null && end == null) {
            throw new IllegalArgumentException("Bounded interval must have a start or end");
        }
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        if (start == null) {
            return List.of(Objects.requireNonNull(end));
        } else if (end == null) {
            return List.of(start);
        } else {
            return List.of(start, end);
        }
    }
}