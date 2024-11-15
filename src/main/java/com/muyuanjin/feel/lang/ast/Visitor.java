package com.muyuanjin.feel.lang.ast;

/**
 * @author muyuanjin
 */
public interface Visitor<T> {
    //@formatter:off
    T visit(ASTNode n);
    T visit(BetweenNode n);
    T visit(BooleanNode n);
    T visit(ContextNode n);
    T visit(DateTimeNode n);
    T visit(FilterNode n);
    T visit(ForNode n);
    T visit(FunDefinitionNode n);
    T visit(FunInvocationNode n);
    T visit(IfNode n);
    T visit(InfixOpNode n);
    T visit(InNode n);
    T visit(InstanceOfNode n);
    T visit(ListNode n);
    T visit(NameExprNode n);
    T visit(NegationNode n);
    T visit(NullNode n);
    T visit(NumberNode n);
    T visit(PathNode n);
    T visit(QuantifiedNode n);
    T visit(RangeNode n);
    T visit(StringNode n);
    T visit(UnaryTestsNode n);
    //@formatter:on

    interface Default<T> extends Visitor<T> {
        //@formatter:off
       @Override default T visit(ASTNode n) {return  n.accept(this);}
       @Override default T visit(BetweenNode n) {return visit((ASTNode) n);}
       @Override default T visit(BooleanNode n) {return visit((ASTNode) n);}
       @Override default T visit(ContextNode n) {return visit((ASTNode) n);}
       @Override default T visit(DateTimeNode n) {return visit((ASTNode) n);}
       @Override default T visit(FilterNode n) {return visit((ASTNode) n);}
       @Override default T visit(ForNode n) {return visit((ASTNode) n);}
       @Override default T visit(FunDefinitionNode n) {return visit((ASTNode) n);}
       @Override default T visit(FunInvocationNode n) {return visit((ASTNode) n);}
       @Override default T visit(IfNode n) {return visit((ASTNode) n);}
       @Override default T visit(InfixOpNode n) {return visit((ASTNode) n);}
       @Override default T visit(InNode n) {return visit((ASTNode) n);}
       @Override default T visit(InstanceOfNode n) {return visit((ASTNode) n);}
       @Override default T visit(ListNode n) {return visit((ASTNode) n);}
       @Override default T visit(NameExprNode n) {return visit((ASTNode) n);}
       @Override default T visit(NegationNode n) {return visit((ASTNode) n);}
       @Override default T visit(NullNode n) {return visit((ASTNode) n);}
       @Override default T visit(NumberNode n) {return visit((ASTNode) n);}
       @Override default T visit(PathNode n) {return visit((ASTNode) n);}
       @Override default T visit(QuantifiedNode n) {return visit((ASTNode) n);}
       @Override default T visit(RangeNode n) {return visit((ASTNode) n);}
       @Override default T visit(StringNode n) {return visit((ASTNode) n);}
       @Override default T visit(UnaryTestsNode n) {return visit((ASTNode) n);}
        //@formatter:on
    }
}
