package com.muyuanjin.feel.parser;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.exception.FeelLangException;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.*;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.antlr4.FEELBaseVisitor;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author muyuanjin
 */
@RequiredArgsConstructor
public class FeelASTBuilderVisitor extends FEELBaseVisitor<ASTNode> {
    private final TokenStream tokenStream;

    @Override
    public UnaryTestsNode visitUnaryTests(FEELParser.UnaryTestsContext ctx) {
        if (ctx.SUB() != null) {
            return new UnaryTestsNode(ctx, tokenStream);
        }
        return new UnaryTestsNode(ctx, tokenStream, visitPositiveUnaryTests(ctx.positiveUnaryTests()));
    }

    @Override
    public ListNode visitPositiveUnaryTests(FEELParser.PositiveUnaryTestsContext ctx) {
        var contexts = ctx.positiveUnaryTest();
        List<ASTNode> positiveUnaryTests = new ArrayList<>(contexts.size());
        for (FEELParser.PositiveUnaryTestContext context : contexts) {
            positiveUnaryTests.add(visit(context.expression));
        }
        return new ListNode(ctx, tokenStream, positiveUnaryTests);
    }

    @Override
    public ASTNode visitPositiveUnaryTest(FEELParser.PositiveUnaryTestContext ctx) {
        return visit(ctx.expression);
    }

    @Override
    public ASTNode visitExpressionUnit(FEELParser.ExpressionUnitContext ctx) {
        return visit(ctx.expression);
    }

    @Override
    public ASTNode visitParens(FEELParser.ParensContext ctx) {
        return visit(ctx.expression);
    }

    @Override
    public NameExprNode visitNameExpression(FEELParser.NameExpressionContext ctx) {
        return new NameExprNode(ctx, tokenStream);
    }

    @Override
    public RangeNode visitUnboundedInterval(FEELParser.UnboundedIntervalContext ctx) {
        return new RangeNode(ctx, tokenStream, visit(ctx.expression));
    }

    @Override
    public RangeNode visitBoundedInterval(FEELParser.BoundedIntervalContext ctx) {
        return new RangeNode(ctx, tokenStream, visit(ctx.left), visit(ctx.right));
    }

    @Override
    public ASTNode visitIntegerLiteral(FEELParser.IntegerLiteralContext ctx) {
        return new NumberNode(ctx, tokenStream);
    }

    @Override
    public NumberNode visitFloatLiteral(FEELParser.FloatLiteralContext ctx) {
        return new NumberNode(ctx, tokenStream);
    }

    @Override
    public BooleanNode visitBooleanLiteral(FEELParser.BooleanLiteralContext ctx) {
        return new BooleanNode(ctx);
    }

    @Override
    public DateTimeNode visitDateTimeLiteral(FEELParser.DateTimeLiteralContext ctx) {
        return new DateTimeNode(ctx, tokenStream);
    }

    @Override
    public StringNode visitStringLiteral(FEELParser.StringLiteralContext ctx) {
        return new StringNode(ctx);
    }

    @Override
    public NullNode visitNullLiteral(FEELParser.NullLiteralContext ctx) {
        return new NullNode(ctx);
    }

    @Override
    public FunInvocationNode visitFunctionInvocation(FEELParser.FunctionInvocationContext ctx) {
        FType type = ctx.expression().type;
        if (!(type instanceof FFunction function)) {
            throw new FeelLangException(ctx, "FunctionInvocation must invoke a function, but got :" + type);
        }
        List<ASTNode> positionalParameters;
        if (ctx.namedParameters() != null) {
            if (function.getParameterNames() == null) {
                throw new FeelLangException(ctx, "Function " + ctx.expression().getText() + "[" + function + "] does not have named parameters");
            }
            if (function.hasVarargs()) {
                throw new FeelLangException(ctx, "Function " + ctx.expression().getText() + "[" + function + "] is varargs function, can not invoke by named parameters");
            }
            var context = ctx.namedParameters();
            var nameDef = context.nameDef();
            var expressions = context.expression();
            if (nameDef.size() != function.getParameterNames().size()) {
                throw new FeelLangException(ctx, "Function " + ctx.expression().getText() + "[" + function + "] named parameters count must be " + function.getParameterNames().size());
            }
            positionalParameters = new ArrayList<>(expressions.size());
            for (int i = 0; i < expressions.size(); i++) {
                int index = function.getParameterNames().indexOf(nameDef.get(i).name);
                if (index == -1) {
                    throw new FeelLangException(ctx, "Function " + ctx.expression().getText() + "[" + function + "] does not have named parameter " + nameDef.get(i).name);
                }
                positionalParameters.add(index, visit(expressions.get(i)));
            }
        } else {
            var expressions = ctx.positionalParameters().expression();
            positionalParameters = new ArrayList<>(expressions.size());
            for (FEELParser.ExpressionContext expression : expressions) {
                positionalParameters.add(visit(expression));
            }
        }
        for (int i = 0; i < positionalParameters.size(); i++) {
            FType inputType = positionalParameters.get(i).getType();
            FType needType = function.getParamType(i);
            if (!inputType.equals(needType) && !inputType.canConvertTo(needType)) {
                throw new FeelLangException(ctx, "Function " + ctx.expression().getText() + "[" + function + "] parameter[" + i + "] must be " + needType + " but got " + inputType);
            }
        }
        if (function.getParamCount() != positionalParameters.size()) {
            if (!function.hasVarargs() || positionalParameters.size() <= function.getParamCount()) {
                throw new FeelLangException(ctx, "Function " + ctx.expression().getText() + "[" + function + "] positional parameters must be " + function.getParameterTypes());
            }
        }
        return new FunInvocationNode(ctx, tokenStream, visit(ctx.expression()), positionalParameters);
    }

    @Override
    public FilterNode visitFilterExpression(FEELParser.FilterExpressionContext ctx) {
        return new FilterNode(ctx, tokenStream, visit(ctx.target), visit(ctx.filter));
    }

    @Override
    public PathNode visitPathExpression(FEELParser.PathExpressionContext ctx) {
        return new PathNode(ctx, tokenStream, visit(ctx.left), ctx.right.name);
    }

    @Override
    public InstanceOfNode visitInstanceOf(FEELParser.InstanceOfContext ctx) {
        return new InstanceOfNode(ctx, tokenStream, visit(ctx.expression()));
    }

    @Override
    public NegationNode visitArithmeticNegation(FEELParser.ArithmeticNegationContext ctx) {
        return new NegationNode(ctx, tokenStream, visit(ctx.expression()));
    }

    @Override
    public InfixOpNode visitExponentiation(FEELParser.ExponentiationContext ctx) {
        return new InfixOpNode(ctx, tokenStream, visit(ctx.left), visit(ctx.right));
    }

    @Override
    public InfixOpNode visitMultiOrDiv(FEELParser.MultiOrDivContext ctx) {
        return new InfixOpNode(ctx, tokenStream, visit(ctx.left), visit(ctx.right));
    }

    @Override
    public InfixOpNode visitAddOrSub(FEELParser.AddOrSubContext ctx) {
        return new InfixOpNode(ctx, tokenStream, visit(ctx.left), visit(ctx.right));
    }

    @Override
    public InNode visitInList(FEELParser.InListContext ctx) {
        return new InNode(ctx, tokenStream, visit(ctx.value), visit(ctx.target));
    }

    @Override
    public InNode visitInSingle(FEELParser.InSingleContext ctx) {
        return new InNode(ctx, tokenStream, visit(ctx.value), visit(ctx.target));
    }

    @Override
    public BetweenNode visitBetween(FEELParser.BetweenContext ctx) {
        var clazz = ctx.value.type.getClass();
        if (!FType.isSameType(ctx.value.type, ctx.left.type) || !FType.isSameType(ctx.value.type, ctx.right.type)) {
            throw new FeelLangException(ctx, "Between value type must be the same as left and right," +
                                             " but got :" + ctx.value.type + " " + ctx.left.type + " " + ctx.right.type);
        }
        if (clazz == FNull.class || clazz == FContext.class || clazz == FFunction.class ||
            clazz == FList.class || clazz == FRange.class) {
            throw new FeelLangException(ctx, "Between value type must be number, boolean, string, date, time, date and time ,date and time duration, year and month duration, but got :" + ctx.value.type);
        }
        return new BetweenNode(ctx, tokenStream, visit(ctx.value), visit(ctx.left), visit(ctx.right));
    }

    @Override
    public InfixOpNode visitCompare(FEELParser.CompareContext ctx) {
        var right = ctx.right.type;
        var left = ctx.left.type;
        if (!FType.isSameType(right, left)) {
            if ((!(right instanceof FDayTimeDuration) && !(right instanceof FYearMonthDuration)) || (!(left instanceof FDayTimeDuration) && !(left instanceof FYearMonthDuration))) {
                throw new FeelLangException(ctx, "Compare left and right type must be the same, but got :" + ctx.left.type + " " + ctx.right.type);
            }
        }
        return new InfixOpNode(ctx, tokenStream, visit(ctx.left), visit(ctx.right));
    }

    @Override
    public InfixOpNode visitJunction(FEELParser.JunctionContext ctx) {
        // Table 50: Semantics of conjunction and disjunction
        return new InfixOpNode(ctx, tokenStream, visit(ctx.left), visit(ctx.right));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ForNode visitForExpression(FEELParser.ForExpressionContext ctx) {
        var it = ctx.it.iterationContext();
        List<Iteration> nodes = new ArrayList<>(it.size());
        for (var context : it) {
            if (context.right != null) {
                nodes.add(new Iteration(context, tokenStream, visit(context.left), visit(context.right)));
            } else {
                nodes.add(new Iteration(context, tokenStream, visit(context.left)));
            }
        }
        return new ForNode(ctx, tokenStream, nodes, visit(ctx.result));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public QuantifiedNode visitQuantifiedExpression(FEELParser.QuantifiedExpressionContext ctx) {
        var it = ctx.it.iterationContext();
        List<Iteration> nodes = new ArrayList<>(it.size());
        for (var context : it) {
            if (context.right != null) {
                nodes.add(new Iteration(context, tokenStream, visit(context.left), visit(context.right)));
            } else {
                nodes.add(new Iteration(context, tokenStream, visit(context.left)));
            }
        }
        return new QuantifiedNode(ctx, tokenStream, nodes, visit(ctx.judge));
    }

    @Override
    public IfNode visitIfExpression(FEELParser.IfExpressionContext ctx) {
        IfNode ifNode = new IfNode(ctx, tokenStream, visit(ctx.condition), visit(ctx.then), visit(ctx.otherwise));
        if (!(ctx.condition.type instanceof FBoolean)) {
            ifNode.addWarning("If condition should be boolean type, but got " + ctx.condition.type);
        }
        return ifNode;
    }

    @Override
    public ContextNode visitContext(FEELParser.ContextContext ctx) {
        List<FEELParser.KeyContext> key = ctx.key();
        List<FEELParser.ExpressionContext> value = ctx.expression();
        LinkedHashMap<String, ASTNode> entries = MapUtil.newLinkedHashMap(key.size());
        for (int i = 0; i < key.size(); i++) {
            entries.put(key.get(i).name, visit(value.get(i)));
        }
        return new ContextNode(ctx, tokenStream, entries);
    }

    @Override
    public FunDefinitionNode visitFunctionDefinition(FEELParser.FunctionDefinitionContext ctx) {
        if (ctx.EXTERNAL() != null) {
            //TODO 允许白名单或者通过设置来允许是否开启？
            throw new FeelLangException(ctx, "External function not supported yet");
        }
        return new FunDefinitionNode(ctx, tokenStream, visit(ctx.body));
    }

    @Override
    public ListNode visitList(FEELParser.ListContext ctx) {
        List<ASTNode> expressions = new ArrayList<>(ctx.expression().size());
        for (FEELParser.ExpressionContext context : ctx.expression()) {
            expressions.add(visit(context));
        }
        return new ListNode(ctx, tokenStream, expressions);
    }
}
