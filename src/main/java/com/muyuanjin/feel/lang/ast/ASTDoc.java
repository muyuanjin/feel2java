package com.muyuanjin.feel.lang.ast;

import java.util.List;

/**
 * @author muyuanjin
 */
public interface ASTDoc {
    int getStartChar();

    int getEndChar();

    int getStartLine();

    int getStartColumn();

    int getEndLine();

    int getEndColumn();

    List<String> getComments();

    List<String> getWarnings();
}