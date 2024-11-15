package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 不能被{@code List<ASTNode>} 替代的原因是 ListNode 具备 ListType
 *
 * @author muyuanjin
 */
@NoArgsConstructor
public class ListNode extends BaseASTNode {
    public List<ASTNode> elements;

    public ListNode(FEELParser.PositiveUnaryTestsContext ctx, TokenStream input, List<ASTNode> elements) {
        super(ctx, input);
        this.elements = elements;
    }

    public ListNode(FEELParser.ListContext ctx, TokenStream input, List<ASTNode> elements) {
        super(ctx, input);
        this.elements = elements;
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return elements;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }
}
