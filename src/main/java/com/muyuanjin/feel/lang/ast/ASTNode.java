package com.muyuanjin.feel.lang.ast;

import com.muyuanjin.feel.lang.FType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author muyuanjin
 */
public interface ASTNode extends ASTDoc {
    @NotNull
    FType getType();

    @NotNull
    String getText();

    @NotNull
    List<ASTNode> getChildren();

    <T> T accept(@NotNull Visitor<T> v);
}