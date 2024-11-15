package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.parser.antlr4.FEELLexer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 抽象基类，用于容纳常用功能。
 * 您可以在解析树中关联一个节点，该节点负责定义该符号的节点。
 */
@Getter
@Unmodifiable
@EqualsAndHashCode
abstract class BaseSymbol implements Serializable {
    protected final String name;
    @Nullable
    protected final FType type;
    /**
     * name 在分词后的token列表，懒加载
     */
    protected List<String> tokens;

    public BaseSymbol(@NotNull String name, @Nullable FType type) {
        this(name, type, null);
    }

    public BaseSymbol(@NotNull String name, @Nullable FType type, @Nullable List<String> tokens) {
        this.name = Objects.requireNonNull(name);
        this.type = type;
        this.tokens = tokens != null ? List.copyOf(tokens) : (this.name.length() == 1 ? Collections.singletonList(this.name) : null);
    }

    public List<String> getTokens() {
        if (name.isEmpty()) {
            return Collections.emptyList();
        }
        if (tokens != null) {
            return tokens;
        }
        synchronized (this) {
            if (tokens != null) {
                return tokens;
            }
            tokens = new ArrayList<>();
            FEELLexer feelLexer = new FEELLexer(CharStreams.fromString(name));
            Token token = feelLexer.nextToken();
            while (token.getType() != Token.EOF) {
                tokens.add(token.getText());
                token = feelLexer.nextToken();
            }
            tokens = Collections.unmodifiableList(tokens);
        }
        return tokens;
    }


    public String toString() {
        String s = "";
        if (type != null) {
            String ts = type.toString();
            return '<' + s + getName() + ":" + ts + '>';
        }
        return s + getName();
    }
}
