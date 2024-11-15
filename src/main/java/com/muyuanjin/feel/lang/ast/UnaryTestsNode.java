package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class UnaryTestsNode extends BaseASTNode {
    public boolean blank;
    public boolean not;
    public ListNode positiveUnaryTests;

    public UnaryTestsNode(FEELParser.UnaryTestsContext ctx, TokenStream input) {
        super(ctx, input);
        this.blank = ctx.SUB() != null;
    }

    public UnaryTestsNode(FEELParser.UnaryTestsContext ctx, TokenStream input, ListNode positiveUnaryTests) {
        super(ctx, input);
        this.not = ctx.NOT() != null;
        this.positiveUnaryTests = positiveUnaryTests;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return Collections.singletonList(positiveUnaryTests);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        if (blank) {
            sb.append("-").append(", ");
        } else {
            if (not) {
                sb.append("not").append(", ");
            }
            sb.append("positiveUnaryTests=").append(positiveUnaryTests).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}