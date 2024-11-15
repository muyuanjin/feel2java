package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.FAny;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
abstract class BaseASTNode implements ASTNode {
    public final int startChar;
    public final int endChar;
    public final int startLine;
    public final int startColumn;
    public final int endLine;
    public final int endColumn;
    public final List<String> comments;
    public final List<String> warnings;

    public FType type;
    public String text;

    public BaseASTNode() {
        this.startChar = -1;
        this.endChar = -1;
        this.startLine = -1;
        this.startColumn = -1;
        this.endLine = -1;
        this.endColumn = -1;
        this.comments = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.type = FAny.ANY;
        this.text = "";
    }

    public BaseASTNode(ParserRuleContext ctx, TokenStream input) {
        this(ASTUtil.typeOf(ctx), ASTUtil.tokens(ctx, input));
    }

    @SafeVarargs
    public BaseASTNode(ParserRuleContext ctx, TokenStream input, Class<? extends FType>... expectedTypes) {
        this(ASTUtil.check(ctx, expectedTypes), ASTUtil.tokens(ctx, input));
    }

    @SafeVarargs
    public BaseASTNode(ParserRuleContext type, TerminalNode node, Class<? extends FType>... expectedTypes) {
        this(ASTUtil.check(type, expectedTypes), node.getSymbol());
    }

    @SafeVarargs
    public BaseASTNode(ParserRuleContext type, Token node, Class<? extends FType>... expectedTypes) {
        this(ASTUtil.check(type, expectedTypes), node);
    }

    public BaseASTNode(ParserRuleContext ctx, TokenStream input, FType... expectedTypes) {
        this(ASTUtil.check(ctx, expectedTypes), ASTUtil.tokens(ctx, input));
    }

    public BaseASTNode(ParserRuleContext type, TerminalNode node, FType... expectedTypes) {
        this(ASTUtil.check(type, expectedTypes), node.getSymbol());
    }

    public BaseASTNode(ParserRuleContext type, Token node, FType... expectedTypes) {
        this(ASTUtil.check(type, expectedTypes), node);
    }

    public BaseASTNode(FType type, TerminalNode... tokens) {
        this(type, ASTUtil.tokens(tokens));
    }

    public BaseASTNode(FType type, Token... tokens) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        if (tokens == null || tokens.length == 0) throw new IllegalArgumentException("tokens cannot be null or empty");
        this.type = type;
        var start = tokens[0];
        var end = tokens[tokens.length - 1];
        this.startChar = start.getStartIndex();
        this.startLine = start.getLine();
        this.startColumn = start.getCharPositionInLine();
        this.endChar = end.getStopIndex();
        this.endLine = end.getLine();
        this.endColumn = end.getCharPositionInLine() + end.getText().length();

        this.comments = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getChannel() == 2) {
                this.comments.add(token.getText());
            }
        }
        this.warnings = new ArrayList<>();
        this.text = start.getInputStream().getText(Interval.of(startChar, endChar));
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return Collections.emptyList();
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    @Override
    public String toString() {
        List<ASTNode> children = getChildren();
        if (children.isEmpty()) {
            return getClass().getSimpleName() + "{" + text + "}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        for (ASTNode child : children) {
            sb.append(child).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}