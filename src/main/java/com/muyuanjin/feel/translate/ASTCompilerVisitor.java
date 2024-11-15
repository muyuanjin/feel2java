package com.muyuanjin.feel.translate;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.UnknownType;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.common.util.TypeUtil;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.exception.FeelException;
import com.muyuanjin.feel.exception.FeelLangException;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.FeelRange;
import com.muyuanjin.feel.lang.ast.*;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.ParserUtil;

import org.jetbrains.annotations.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * @author muyuanjin
 */
public class ASTCompilerVisitor implements Visitor.Default<JavaExpr> {
    private final FeelTypeFactory typeFactory;
    private final ClassManager manager;
    private final ScopeHelper scope;
    private final CompilerTask task;
    private final Context context;

    private final Map<Object, Integer> countMap = new HashMap<>();
    private int feelFunCount = 0;
    private boolean started = false;

    public ASTCompilerVisitor(Context context) {
        context.put(compilerVisitorKey, this);
        this.context = context;
        this.scope = new ScopeHelper(context);
        this.task = CompilerTask.instance(context);
        this.typeFactory = FeelTypeFactory.instance(context);
        this.manager = ClassManager.instance(context);
    }

    public static final Context.Key<ASTCompilerVisitor> compilerVisitorKey = new Context.Key<>();

    public static ASTCompilerVisitor instance(Context context) {
        ASTCompilerVisitor instance = context.get(compilerVisitorKey);
        if (instance == null) {
            instance = new ASTCompilerVisitor(context);
        }
        return instance;
    }

    public boolean isCastGenerics() {
        return task.castGenerics();
    }

    public boolean isNoLambda() {
        return task.noLambda();
    }

    public boolean isAllFinal() {
        return task.allFinal();
    }

    @Override
    public JavaExpr visit(ASTNode n) {
        boolean oldStarted = started;
        if (!oldStarted) {
            this.started = true;
            this.manager.nextMethod(task.methodName(), Map.of(task.rootInputParam(), task.rootInputJavaType()));
        }
        ASTNode old = manager.node();
        manager.node(n);
        try {
            return manager.update(n.accept(this));
        } catch (Exception e) {
            if (e instanceof FeelException) {
                throw e;
            }
            throw new FeelLangException(n, "Error when compile AST node[" + n.getText() + "]", e);
        } finally {
            manager.node(old);
            started = oldStarted;
        }
    }

    private static final BigDecimal INT_MIN = BigDecimal.valueOf(Integer.MIN_VALUE);
    private static final BigDecimal INT_MAX = BigDecimal.valueOf(Integer.MAX_VALUE);
    private static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    @Override
    public JavaExpr visit(NumberNode n) {
        BigDecimal value = n.getValue();
        if (n.type == FNumber.INTEGER) {
            if (value.compareTo(INT_MIN) >= 0 && value.compareTo(INT_MAX) <= 0) {
                return JavaExpr.ofCon(n, new IntegerLiteralExpr(n.text), int.class).feelType(FNumber.INTEGER);
            }
            if (value.compareTo(LONG_MIN) >= 0 && value.compareTo(LONG_MAX) <= 0) {
                return JavaExpr.ofCon(n, new LongLiteralExpr(n.text + "L"), long.class).feelType(FNumber.LONG);
            }
        } else if (ParserUtil.canExactlyBeDouble(value)) {
            return JavaExpr.ofCon(n, new DoubleLiteralExpr(n.text), double.class).feelType(FNumber.DOUBLE);
        }
        return JavaExpr.ofCon(n, manager.addStaticBigDecimal(value), BigDecimal.class).feelType(FNumber.BIG_DECIMAL);
    }

    @Override
    public JavaExpr visit(BooleanNode n) {
        return JavaExpr.ofCon(n, new BooleanLiteralExpr(n.value), boolean.class).feelType(FBoolean.BOOLEAN);
    }

    @Override
    public JavaExpr visit(StringNode n) {
        if (n.value.length() > 20) {
            //use static field
            var declarator = new VariableDeclarator();
            declarator.setName(nextVarName("str_"));
            declarator.setType(manager.getClassType(String.class));
            declarator.setInitializer(CodeGens.stringLiteral(n.value));
            return JavaExpr.ofCon(n, this.manager.addStaticField(declarator, StringNode.class, n.value), String.class);
        }
        return JavaExpr.ofCon(n, CodeGens.stringLiteral(n.value), String.class);
    }

    @Override
    public JavaExpr visit(DateTimeNode n) {
        if (n.type == FTypes.DATE) {
            return JavaExpr.ofCon(n, this.manager.addStaticDate(ParserUtil.parseDate(n.literal)), LocalDate.class);
        } else if (n.type == FTypes.TIME) {
            return JavaExpr.ofCon(n, this.manager.addStaticTime(ParserUtil.parseTime(n.literal)), LocalTime.class);
        } else if (n.type == FTypes.DATE_TIME) {
            return JavaExpr.ofCon(n, this.manager.addStaticDateTime(ParserUtil.parseDateTime(n.literal)), LocalDateTime.class);
        } else if (n.type == FTypes.DAY_TIME_DURATION) {
            return JavaExpr.ofCon(n, this.manager.addStaticDuration(ParserUtil.parseDuration(n.literal)), Duration.class);
        } else if (n.type == FTypes.YEAR_MONTH_DURATION) {
            return JavaExpr.ofCon(n, this.manager.addStaticPeriod(ParserUtil.parsePeriod(n.literal)), Period.class);
        }
        throw new FeelLangException(n, "Cannot compile date time node for type: " + n.type);
    }

    @Override
    public JavaExpr visit(NullNode n) {
        return JavaExpr.ofCon(n, new NullLiteralExpr(), void.class);
    }

    @Override
    public JavaExpr visit(FilterNode n) {
        scope.pushScope();
        boolean isContext = false;
        JavaExpr target = visit(n.target);
        boolean constant = target.constant();
        //隐式类型转换
        FType type = target.feelType();
        FType elementType = FType.getElementType(type);
        String item = n.itemName;
        int marted = markVarName(item);
        SimpleName name = new SimpleName(nextVarName(item));
        scope.put(item, JavaExpr.of(n, new NameExpr(name), elementType.getJavaType()).feelType(elementType));
        Map<String, SimpleName> identifierMap = new HashMap<>();
        Map<String, Integer> varNameMarks = new HashMap<>();
        if (elementType instanceof FContext ctx) {
            Map<String, FType> members = ctx.getMembers();
            for (var entry : members.entrySet()) {
                String memberName = entry.getKey();
                FType memberType = entry.getValue();
                String id = ParserUtil.escapeIdentifier("", memberName);
                varNameMarks.putIfAbsent(id, markVarName(id));
                String memberId = nextVarName(id);
                SimpleName simpleName = new SimpleName(memberId);
                identifierMap.put(memberName, simpleName);
                scope.put(memberName, JavaExpr.of(n, new NameExpr(simpleName), memberType.getJavaType(), constant).feelType(memberType));
            }
            isContext = true;
        }

        Expression expr = target.expr();
        if (n.filter.getType() instanceof FBoolean) {
            String varName = nextVarName("filter_");
            var varType = manager.getType(n.type.getJavaType());
            var declarator = new VariableDeclarator(varType, varName);
            declarator.setInitializer(new ObjectCreationExpr().setType(manager.getClassType(ArrayList.class).setTypeArguments()));
            NameExpr filterResult = declarator.getNameAsExpression();
            // For 循环
            Statement forStmt = createForStmt(target, null, elementType, name);
            BlockStmt forBody = getOrSetForBody(forStmt);

            BlockStmt oldBlock = manager.getCurrentBlock();
            manager.setCurrentBlock(forBody);
            NameExpr itemExpr = new NameExpr(name);
            if (isContext) {
                scope.addVarListener((memberName, javaExpr) -> {
                    SimpleName simpleName = identifierMap.get(memberName);
                    if (simpleName == null) {
                        return;
                    }
                    var itemVar = new VariableDeclarator();
                    itemVar.setName(simpleName);
                    itemVar.setType(manager.getType(javaExpr.feelType().getWrappedJavaType()));
                    JavaExpr member = typeFactory.getMember(n, JavaExpr.of(n, itemExpr, elementType.getJavaType()).feelType(elementType), memberName, context);
                    itemVar.setInitializer(member == null ? new NullLiteralExpr() : member.expr());
                    manager.addLocalField(itemVar);
                });
            }
            boolean[] constantBox = new boolean[]{constant};
            scope.addVarListener((memberName, typedExpr) -> constantBox[0] &= typedExpr.constant() || typedExpr.node() == n);
            JavaExpr filter = visit(n.filter);
            constant = constantBox[0];
            scope.popScope();
            // 如果 filter expr 的结果是 true 则添加到结果列表
            IfStmt ifStmt = new IfStmt();
            ifStmt.setCondition(filter.expr());
            ifStmt.setThenStmt(new BlockStmt().addStatement(new ExpressionStmt(new MethodCallExpr(filterResult, "add").addArgument(itemExpr))));
            forBody.addStatement(ifStmt);
            manager.setCurrentBlock(oldBlock);
            varNameMarks.forEach(this::resetVarName);
            resetVarName(item, marted);
            if (constant) {
                NameExpr staticField = manager.addStaticField(new VariableDeclarator(varType, varName));
                BlockStmt initBlock = new BlockStmt();
                initBlock.addStatement(new VariableDeclarationExpr(declarator));
                initBlock.addStatement(forStmt);
                // 添加静态字段赋值语句
                initBlock.addStatement(new ExpressionStmt(new AssignExpr(staticField,
                        manager.getStaticMethod(Collections.class, "unmodifiableList").addArgument(declarator.getNameAsExpression()),
                        AssignExpr.Operator.ASSIGN)));
                manager.addStaticFieldInit(staticField, initBlock);
                return JavaExpr.ofCon(n, staticField, n.type.getJavaType());
            }
            manager.addLocalField(declarator);
            manager.addStatement(forStmt);
            return JavaExpr.of(n, filterResult, n.type.getJavaType(), constant);
        } else if (n.filter.getType() instanceof FNumber) {
            JavaExpr filter = visit(n.filter);
            constant &= filter.constant();
            Expression indexExpr = filter.expr();
            scope.popScope();
            BigDecimal value = null;
            if (filter.constant()) {
                if (filter.node() instanceof NumberNode node) {
                    value = node.getValue();
                } else if (filter.node() instanceof NegationNode negationNode && negationNode.value instanceof NumberNode node) {
                    value = node.getValue().negate();
                }
                //TODO 还有 InfixOp 也可以计算出常量
            }
            // 单列表
            if (elementType.equals(type)) {
                if (value != null) {
                    if (value.abs().compareTo(BigDecimal.ONE) == 0) {
                        return JavaExpr.of(n, expr, elementType.getJavaType(), constant);
                    }
                    return JavaExpr.ofNull(n);
                }
                // 如果 filter expr 的结果不是 1 或 -1 则返回null
                MethodCallExpr abs = manager.getStaticMethod(Math.class, "abs").addArgument(indexExpr);
                // 使用三元表达式
                ConditionalExpr conditionalExpr = new ConditionalExpr(new BinaryExpr(abs, new IntegerLiteralExpr("1"), BinaryExpr.Operator.EQUALS),
                        expr, new NullLiteralExpr());
                return JavaExpr.of(n, conditionalExpr, elementType.getJavaType(), constant);
            }
            if (type instanceof FList) {
                //从列表获取 feel列表序号 从 1 开始
                MethodCallExpr getMethodCall = new MethodCallExpr(expr, "get");
                if (value != null) {
                    switch (value.compareTo(BigDecimal.ZERO)) {
                        case 0 -> {return JavaExpr.ofNull(n);}
                        case 1 ->
                                getMethodCall.addArgument(new IntegerLiteralExpr(Integer.toString(value.subtract(BigDecimal.ONE).intValue())));
                        case -1 -> {
                            if (target.constant() && target.node() instanceof ListNode listNode) {
                                getMethodCall.addArgument(new IntegerLiteralExpr(Integer.toString(listNode.elements.size() + value.intValue())));
                            } else {
                                getMethodCall.addArgument(new BinaryExpr(new MethodCallExpr(expr, "size"), indexExpr, BinaryExpr.Operator.PLUS));
                            }
                        }
                    }
                    return JavaExpr.of(n, getMethodCall, elementType.getJavaType(), constant);
                }
                // 判断索引正负
                Expression adjustedIndex = new ConditionalExpr(
                        new BinaryExpr(indexExpr, new IntegerLiteralExpr("0"), BinaryExpr.Operator.GREATER),
                        new BinaryExpr(indexExpr, new IntegerLiteralExpr("1"), BinaryExpr.Operator.MINUS), // 正数索引, 从1开始所以减1
                        new BinaryExpr(new MethodCallExpr(expr, "size"), indexExpr, BinaryExpr.Operator.PLUS) // 负数索引，从-1开始计算
                );
                if (filter.feelType().canBe(FNumber.INTEGER) > FType.CT) {
                    adjustedIndex = new CastExpr(PrimitiveType.intType(), new EnclosedExpr(adjustedIndex));
                }
                getMethodCall.addArgument(adjustedIndex);
                return JavaExpr.of(n, getMethodCall, elementType.getJavaType(), constant);
            } else if (type instanceof FRange range) {
                Expression zeroStartIndex = CodeGens.toZeroStartIndex(filter, context);
                if (zeroStartIndex == null) {
                    return JavaExpr.ofNull(n);
                }
                JavaExpr index = filter.copy().expr(zeroStartIndex);
                Boolean rangeStart = range.getStart();
                Boolean rangeEnd = range.getEnd();
                if (rangeStart == null && rangeEnd == null) {
                    throw new FeelLangException(n, "range must have start or end");
                }
                Expression nullAllNull = CodeGens.ifOneNullAllNull(target, null,
                        new MethodCallExpr(expr, "start"), true);
                if (nullAllNull == null) {
                    return JavaExpr.ofNull(n);
                }
                JavaExpr start = rangeStart == null ? null : JavaExpr.of(n, nullAllNull, elementType.getJavaType(), target.constant())
                        .nullable(target.nullable());
                nullAllNull = CodeGens.ifOneNullAllNull(target, null,
                        new MethodCallExpr(expr, "end"), true);
                if (nullAllNull == null) {
                    return JavaExpr.ofNull(n);
                }
                JavaExpr end = rangeEnd == null ? null : JavaExpr.of(n, nullAllNull, elementType.getJavaType(), target.constant())
                        .nullable(target.nullable());
                JavaExpr gtThanZero = typeFactory.infixOp(n, filter, JavaExpr.of(n, new IntegerLiteralExpr("0"), int.class, true), InfixOpNode.Op.GT, context);
                if (gtThanZero == null) {
                    return JavaExpr.ofNull(n);
                }
                if (rangeStart == null) {
                    ConditionalExpr result = new ConditionalExpr();
                    result.setCondition(gtThanZero.expr());
                    result.setThenExpr(new NullLiteralExpr());

                    result.setElseExpr(typeFactory.indexOf(end, index, context));
                    nullAllNull = CodeGens.ifOneNullAllNull(end, index, result, true);
                    if (nullAllNull == null) {
                        return JavaExpr.ofNull(n);
                    }
                    return JavaExpr.of(n, nullAllNull, elementType.getJavaType(), constant)
                            .nullable(target.nullable(), end.nullable(), index.nullable());
                }
                if (rangeEnd == null) {
                    ConditionalExpr result = new ConditionalExpr();
                    result.setCondition(gtThanZero.expr());
                    result.setThenExpr(typeFactory.indexOf(start, index, context));
                    result.setElseExpr(new NullLiteralExpr());
                    nullAllNull = CodeGens.ifOneNullAllNull(start, index, result, true);
                    if (nullAllNull == null) {
                        return JavaExpr.ofNull(n);
                    }
                    return JavaExpr.of(n, nullAllNull, elementType.getJavaType(), constant)
                            .nullable(target.nullable(), start.nullable(), index.nullable());
                }
                // 两个都不为空
                ConditionalExpr expression = new ConditionalExpr();
                expression.setCondition(gtThanZero.expr());
                Expression thenExpr;
                Expression elseExpr;

                JavaExpr add1 = typeFactory.infixOp(n, index, JavaExpr.of(n, new IntegerLiteralExpr("1"), int.class, true), InfixOpNode.Op.ADD, context);
                if (add1 == null) {
                    return JavaExpr.ofNull(n);
                }
                add1.expr(new EnclosedExpr(add1.expr()));
                JavaExpr sub1 = typeFactory.infixOp(n, index, JavaExpr.of(n, new IntegerLiteralExpr("1"), int.class, true), InfixOpNode.Op.SUB, context);
                if (sub1 == null) {
                    return JavaExpr.ofNull(n);
                }
                sub1.expr(new EnclosedExpr(sub1.expr()));
                if ((thenExpr = typeFactory.indexOf(start, rangeStart ? index : add1, context)) != null &&
                    (elseExpr = typeFactory.indexOf(end, rangeEnd ? index : sub1, context)) != null) {
                    expression.setThenExpr(thenExpr);
                    expression.setElseExpr(elseExpr);

                    JavaExpr source = JavaExpr.of(n, expression, elementType.getJavaType(), target.constant());
                    if (!source.isSimple()) {
                        var sourceVar = new VariableDeclarator(manager.getType(elementType.getJavaType()), nextVarName("source"), expression);
                        source.expr(sourceVar.getNameAsExpression());
                        if (source.constant()) {
                            source.expr(manager.addStaticField(sourceVar));
                        } else {
                            manager.addLocalField(sourceVar);
                        }
                    }

                    JavaExpr con1 = typeFactory.infixOp(n, source, start, rangeStart ? InfixOpNode.Op.GE : InfixOpNode.Op.GT, context);
                    JavaExpr con2 = typeFactory.infixOp(n, source, end, rangeEnd ? InfixOpNode.Op.LE : InfixOpNode.Op.LT, context);
                    if (con1 != null && con2 != null) {
                        ConditionalExpr result = new ConditionalExpr();
                        result.setCondition(new BinaryExpr(con1.expr(), con2.expr(), BinaryExpr.Operator.AND));
                        result.setThenExpr(source.expr());
                        result.setElseExpr(new NullLiteralExpr());
                        return JavaExpr.of(n, result, elementType.getJavaType(), constant);
                    }
                }
                return JavaExpr.ofNull(n);
            }
            return JavaExpr.ofNull(n);
        } else {
            throw new FeelLangException(n, "Cannot compile filter node for type: " + n.filter.getType());
        }
    }

    @Override
    public JavaExpr visit(IfNode n) {
        JavaExpr condition = visit(n.condition);
        Expression conditionExpr = condition.expr();
        boolean constant = condition.constant();

        String varName = nextVarName("ifResult");
        Type javaType = n.type.getJavaType();
        var varType = manager.getType(javaType);
        var resultVar = new VariableDeclarator(varType, varName);
        BlockStmt oldBlock = manager.getCurrentBlock();

        NameExpr result = resultVar.getNameAsExpression();
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(getIfCondition(condition));
        // then
        BlockStmt thenBlock = new BlockStmt();
        ifStmt.setThenStmt(thenBlock);
        manager.setCurrentBlock(thenBlock);
        JavaExpr thenExpr = visit(n.then);
        constant &= thenExpr.constant();
        thenBlock.addStatement(new ExpressionStmt(new AssignExpr(result, thenExpr.expr(), AssignExpr.Operator.ASSIGN)));

        // else
        BlockStmt elseBlock = new BlockStmt();
        ifStmt.setElseStmt(elseBlock);
        manager.setCurrentBlock(elseBlock);
        JavaExpr elseExpr = visit(n.otherwise);
        constant &= elseExpr.constant();
        elseBlock.addStatement(new ExpressionStmt(new AssignExpr(result, elseExpr.expr(), AssignExpr.Operator.ASSIGN)));
        manager.setCurrentBlock(oldBlock);
        if (!constant) {
            manager.addLocalField(resultVar);
            manager.addStatement(ifStmt);
            return JavaExpr.of(n, result, javaType, constant);
        }
        NameExpr staticField = manager.addStaticField(new VariableDeclarator(varType, varName));
        BlockStmt initBlockStmt = new BlockStmt();
        initBlockStmt.addStatement(new VariableDeclarationExpr(resultVar));
        initBlockStmt.addStatement(ifStmt);
        // 添加静态字段赋值语句
        initBlockStmt.addStatement(new ExpressionStmt(new AssignExpr(staticField, result, AssignExpr.Operator.ASSIGN)));
        manager.addStaticFieldInit(staticField, initBlockStmt);
        return JavaExpr.ofCon(n, staticField, javaType);
    }

    @Override
    public JavaExpr visit(InNode n) {
        JavaExpr value = visit(n.value);
        JavaExpr target = visit(n.target);
        boolean constant = value.constant() && target.constant();
        String varName = nextVarName("inResult");
        var resultVar = new VariableDeclarator(PrimitiveType.booleanType(), varName);
        resultVar.setInitializer(new BooleanLiteralExpr(false));
        NameExpr result = resultVar.getNameAsExpression();

        Expression expression = inTarget(n, value, target);
        if (expression == null) {
            return JavaExpr.ofNull(n);
        }
        resultVar.setInitializer(expression);
        if (!constant) {
            manager.addLocalField(resultVar);
            return JavaExpr.of(n, result, boolean.class, false);
        }
        return JavaExpr.ofCon(n, manager.addStaticField(resultVar), boolean.class);
    }

    private Expression inTarget(ASTNode n, JavaExpr value, JavaExpr target) {
        if (target.feelType() instanceof FList) {
            BinaryExpr notNull = new BinaryExpr(target.expr(), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS);
            MethodCallExpr contains = new MethodCallExpr(target.expr(), "contains").addArgument(value.expr());
            return new BinaryExpr(notNull, contains, BinaryExpr.Operator.AND);
        }
        if (target.feelType() instanceof FRange) {
            return inRange(n, value, target);
        }
        JavaExpr eq = typeFactory.infixOp(n, value, target, InfixOpNode.Op.EQ, context);
        if (eq == null) {
            return null;
        }
        return eq.expr();
    }

    private Expression inRange(ASTNode n, JavaExpr value, JavaExpr target) {
        if (!(target.feelType() instanceof FRange range)) {
            return null;
        }
        Boolean rangeStart = range.getStart();
        Boolean rangeEnd = range.getEnd();
        if (rangeStart == null && rangeEnd == null) {
            throw new FeelLangException(n, "range must have start or end");
        }
        JavaExpr start = rangeStart == null ? null : JavaExpr.of(n, new MethodCallExpr(target.expr(), "start"), range.getElementType().getJavaType(), target.constant())
                .nullable(target.nullable());
        BinaryExpr notNull = new BinaryExpr(target.expr(), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS);
        JavaExpr end = rangeEnd == null ? null : JavaExpr.of(n, new MethodCallExpr(target.expr(), "end"), range.getElementType().getJavaType(), target.constant())
                .nullable(target.nullable());
        if (rangeStart == null) {
            JavaExpr le = typeFactory.infixOp(n, value, end, rangeEnd ? InfixOpNode.Op.LE : InfixOpNode.Op.LT, context);
            if (le == null) {
                return null;
            }
            return new BinaryExpr(notNull, le.expr(), BinaryExpr.Operator.AND);
        } else if (rangeEnd == null) {
            JavaExpr ge = typeFactory.infixOp(n, value, start, rangeStart ? InfixOpNode.Op.GE : InfixOpNode.Op.GT, context);
            if (ge == null) {
                return null;
            }
            return new BinaryExpr(notNull, ge.expr(), BinaryExpr.Operator.AND);
        } else {
            JavaExpr con1 = typeFactory.infixOp(n, value, start, rangeStart ? InfixOpNode.Op.GE : InfixOpNode.Op.GT, context);
            JavaExpr con2 = typeFactory.infixOp(n, value, end, rangeEnd ? InfixOpNode.Op.LE : InfixOpNode.Op.LT, context);
            if (con1 == null || con2 == null) {
                return null;
            }
            BinaryExpr and = new BinaryExpr(con1.expr(), con2.expr(), BinaryExpr.Operator.AND);
            return new BinaryExpr(notNull, and, BinaryExpr.Operator.AND);
        }
    }

    @Override
    public JavaExpr visit(ForNode n) {
        if (n.iterations == null || n.iterations.isEmpty()) {
            return JavaExpr.ofNull(n);
        }
        scope.pushScope();

        Statement first = null;
        boolean constant = true;
        BlockStmt lastBlock = null;
        int marted = markVarName("it");
        for (Iteration iteration : n.iterations) {
            SimpleName name = new SimpleName(nextVarName("it"));
            JavaExpr start = visit(iteration.start);
            constant &= start.constant();
            JavaExpr end = iteration.end == null ? null : visit(iteration.end);
            constant &= end == null || end.constant();
            FType type = end == null ? start.feelType() : start.feelType().minSuper(end.feelType());
            var elementType = FType.getElementType(start.feelType(), end == null ? null : end.feelType());
            Statement forStmt = createForStmt(start, end, elementType, name);
            scope.put(iteration.name, JavaExpr.of(n, new NameExpr(name), elementType.getJavaType()).feelType(elementType));
            if (lastBlock == null) {
                first = forStmt;
            } else {
                lastBlock.addStatement(forStmt);
            }
            lastBlock = getOrSetForBody(forStmt);
        }
        resetVarName("it", marted);
        if (first == null) {
            throw new FeelLangException(n, "No iteration found");// should not happen, just for idea warning
        }
        BlockStmt oldBlock = manager.getCurrentBlock();
        manager.setCurrentBlock(lastBlock);

        boolean[] constantBox = new boolean[]{constant};
        // “it” 不能是常量，但也不能让它影响filter语句的常量性（其实应该是对外部的引用性，需要重构）
        scope.addVarListener((name, typedExpr) -> constantBox[0] &= typedExpr.constant() || typedExpr.node() == n);
        JavaExpr forReturn = visit(n.result);
        constant = constantBox[0];
        manager.setCurrentBlock(oldBlock);
        scope.popScope();

        String varName = nextVarName("forResult");
        var varType = manager.getType(n.type.getJavaType());
        var resultVar = new VariableDeclarator(varType, varName);
        resultVar.setInitializer(new ObjectCreationExpr().setType(manager.getClassType(ArrayList.class).setTypeArguments()));
        NameExpr result = resultVar.getNameAsExpression();
        lastBlock.addStatement(new MethodCallExpr(result, "add").addArgument(forReturn.expr()));

        if (!constant) {
            manager.addLocalField(resultVar);
            manager.addStatement(first);
            return JavaExpr.of(n, result, n.type.getJavaType(), false);
        }
        NameExpr staticField = manager.addStaticField(new VariableDeclarator(varType, varName));
        BlockStmt initBlockStmt = new BlockStmt();
        initBlockStmt.addStatement(new VariableDeclarationExpr(resultVar));
        initBlockStmt.addStatement(first);
        // 添加静态字段赋值语句
        initBlockStmt.addStatement(new ExpressionStmt(new AssignExpr(staticField, result,
                AssignExpr.Operator.ASSIGN)));
        manager.addStaticFieldInit(staticField, initBlockStmt);
        return JavaExpr.ofCon(n, staticField, n.type.getJavaType());
    }

    @Override
    public JavaExpr visit(QuantifiedNode n) {
        if (n.iterations == null || n.iterations.isEmpty()) {
            return JavaExpr.of(n, new BooleanLiteralExpr(false), boolean.class, true);
        }
        scope.pushScope();

        Statement first = null;
        boolean constant = true;
        BlockStmt lastBlock = null;
        int marted = markVarName("it");
        for (Iteration iteration : n.iterations) {
            SimpleName name = new SimpleName(nextVarName("it"));
            JavaExpr start = visit(iteration.start);
            constant &= start.constant();
            JavaExpr end = iteration.end == null ? null : visit(iteration.end);
            constant &= end == null || end.constant();
            FType type = end == null ? start.feelType() : start.feelType().minSuper(end.feelType());
            var elementType = FType.getElementType(start.feelType(), end == null ? null : end.feelType());
            Statement forStmt = createForStmt(start, end, elementType, name);
            scope.put(iteration.name, JavaExpr.of(n, new NameExpr(name), elementType.getJavaType()).feelType(elementType));
            if (lastBlock == null) {
                first = n.iterations.size() > 1 ? new LabeledStmt("outer", forStmt) : forStmt;
            } else {
                lastBlock.addStatement(forStmt);
            }
            lastBlock = getOrSetForBody(forStmt);
        }
        resetVarName("it", marted);
        if (first == null) {
            throw new FeelLangException(n, "No iteration found");// should not happen, just for idea warning
        }
        BlockStmt oldBlock = manager.getCurrentBlock();
        manager.setCurrentBlock(lastBlock);

        boolean[] constantBox = new boolean[]{constant};
        // “it” 不能是常量，但也不能让它影响filter语句的常量性（其实应该是对外部的引用性，需要重构）
        scope.addVarListener((name, typedExpr) -> constantBox[0] &= typedExpr.constant() || typedExpr.node() == n);
        JavaExpr judge = visit(n.judge);
        constant = constantBox[0];
        manager.setCurrentBlock(oldBlock);
        scope.popScope();

        String varName = nextVarName("quantified");
        var resultVar = new VariableDeclarator(PrimitiveType.booleanType(), varName);
        resultVar.setInitializer(new BooleanLiteralExpr(false));
        NameExpr result = resultVar.getNameAsExpression();
        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(getIfCondition(judge));

        BlockStmt thenBlock = new BlockStmt();
        thenBlock.addStatement(new ExpressionStmt(new AssignExpr(result, new BooleanLiteralExpr(true),
                AssignExpr.Operator.ASSIGN)));
        ifStmt.setThenStmt(thenBlock);
        if (n.every) {
            ifStmt.setElseStmt(new BlockStmt().addStatement(new ExpressionStmt(new AssignExpr(result, new BooleanLiteralExpr(false),
                    AssignExpr.Operator.ASSIGN))).addStatement(new BreakStmt(n.iterations.size() > 1 ? new SimpleName("outer") : null)));
        } else {
            thenBlock.addStatement(new BreakStmt(n.iterations.size() > 1 ? new SimpleName("outer") : null));
        }
        lastBlock.addStatement(ifStmt);
        if (!constant) {
            manager.addLocalField(resultVar);
            manager.addStatement(first);
            return JavaExpr.of(n, result, boolean.class, false);
        }
        NameExpr staticField = manager.addStaticField(new VariableDeclarator(PrimitiveType.booleanType(), varName));
        BlockStmt initBlockStmt = new BlockStmt();
        initBlockStmt.addStatement(new VariableDeclarationExpr(resultVar));
        initBlockStmt.addStatement(first);
        // 添加静态字段赋值语句
        initBlockStmt.addStatement(new ExpressionStmt(new AssignExpr(staticField, result,
                AssignExpr.Operator.ASSIGN)));
        manager.addStaticFieldInit(staticField, initBlockStmt);
        return JavaExpr.ofCon(n, staticField, boolean.class);
    }

    private Statement createForStmt(JavaExpr start, @Nullable JavaExpr end, FType elementType, SimpleName name) {
        FType superType = end == null ? start.feelType() : start.feelType().minSuper(end.feelType());
        if (end == null) {
            // ForEach
            Expression expr = start.expr();
            if (!(superType instanceof FList list)) {
                if (superType instanceof FRange range) {
                    if (range.getStart() == null || range.getEnd() == null) {
                        throw new FeelLangException(manager.node(), "only range with start and end can be used in for each");
                    }
                    return typeFactory.foreach(new MethodCallExpr(start.expr(), "start"),
                            new MethodCallExpr(start.expr(), "end"),
                            range.getStart(), range.getEnd(), range.getElementType(), name, context);
                }
                expr = manager.getStaticMethod(Collections.class, "singletonList").addArgument(expr);
            }
            ForEachStmt forEachStmt = new ForEachStmt();
            forEachStmt.setVariable(new VariableDeclarationExpr(new VariableDeclarator(manager.getType(elementType.getJavaType()), name)));
            forEachStmt.setIterable(expr);
            return forEachStmt;
        }
        return typeFactory.foreach(start.expr(), end.expr(),
                true, true, superType, name, context);
    }

    private BlockStmt getOrSetForBody(Statement forStmt) {
        if (forStmt instanceof ForEachStmt eachStmt) {
            BlockStmt stmtBody;
            if (eachStmt.getBody() instanceof ReturnStmt) {
                eachStmt.setBody(stmtBody = new BlockStmt());
            } else {
                stmtBody = (BlockStmt) eachStmt.getBody();
            }
            return stmtBody;
        } else if (forStmt instanceof ForStmt eachStmt) {
            BlockStmt stmtBody;
            if (eachStmt.getBody() instanceof ReturnStmt) {
                eachStmt.setBody(stmtBody = new BlockStmt());
            } else {
                stmtBody = (BlockStmt) eachStmt.getBody();
            }
            return stmtBody;
        }
        throw new FeelLangException(manager.node(), "forStmt is not ForEachStmt or ForStmt");
    }


    @Override
    public JavaExpr visit(NegationNode n) {
        JavaExpr visit = visit(n.value);
        return JavaExpr.of(n, CodeGens.negate(visit, context), visit.javaType(), visit.constant());
    }

    /**
     * 如果满足以下条件之一，单值测试表达式将返回 true
     * a) 当输入值应用于表达式时，表达式的值为 true
     * b) 表达式求值为一个列表，输入值至少等于列表中的一个值
     * c) 表达式求值为一个值，输入值等于该值。
     * d) 表达式等于 - （破折号）。
     */
    @Override
    public JavaExpr visit(UnaryTestsNode n) {
        NameExpr testInput = new NameExpr(task.unaryTestInputParam());
        manager.getCurrentMethod().getParameters().add(0, new Parameter(manager.getType(task.unaryTestInputJavaType()), testInput.getNameAsString()));
        if (n.blank) {
            return JavaExpr.ofCon(n, new BooleanLiteralExpr(true), boolean.class);
        }
        // 如果只有一个表达式，且表达式是一个一元测试，则直接返回该表达式的结果
        if (n.positiveUnaryTests.elements.size() == 1 && n.positiveUnaryTests.type instanceof FList list && list.getElementType() instanceof FBoolean) {
            return visit(n.positiveUnaryTests.elements.get(0));
        }

        // 无论作为 UnaryTests 解析的 一个单独可以做 expressionUT 的 expression 还是 多个 expression 组成的positiveUnaryTests
        // 都会作为 positiveUnaryTests 处理,而 positiveUnaryTests 的结果就是列表
        // 作为特殊变量"?"的输入条款的 inputExpression 会作为参数提供
        JavaExpr list = visit(n.positiveUnaryTests);

        Expression expr = list.expr();
        manager.getCurrentMethod().addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"), new StringLiteralExpr("SuspiciousMethodCalls")));
        Expression contains = new MethodCallExpr(expr, "contains").addArgument(testInput);
        if (n.not) {
            contains = new UnaryExpr(contains, UnaryExpr.Operator.LOGICAL_COMPLEMENT);
            return JavaExpr.of(n, contains, boolean.class);
        }

        FType elementType = ((FList) list.feelType()).getElementType();
        //TODO 除了单个区间，也可能存在多个区间 ? 如 >2,<5
        boolean isScopeTest = n.positiveUnaryTests.elements.size() == 1 && (elementType instanceof FList || elementType instanceof FRange);
        if (!isScopeTest) {
            return JavaExpr.of(n, new BinaryExpr(manager.getStaticMethod(Objects.class, "equals").addArgument(testInput).addArgument(expr),
                    contains, BinaryExpr.Operator.OR), boolean.class);
        }

        String varName = nextVarName("unaryTests");
        var resultVar = new VariableDeclarator(PrimitiveType.booleanType(), varName);
        manager.addLocalField(resultVar);
        NameExpr result = resultVar.getNameAsExpression();

        AssignExpr assignExpr = new AssignExpr(result, new BooleanLiteralExpr(true), AssignExpr.Operator.ASSIGN);
        resultVar.setInitializer(manager.getStaticMethod(Objects.class, "equals").addArgument(testInput).addArgument(expr));

        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(new BinaryExpr(new UnaryExpr(result, UnaryExpr.Operator.LOGICAL_COMPLEMENT), contains, BinaryExpr.Operator.AND));
        ifStmt.setThenStmt(new BlockStmt().addStatement(assignExpr));

        //有可能是单个范围表达式，如 >2，或者单个列表表达式，如 [1,2,3]
        JavaExpr input = JavaExpr.of(n, testInput, task.unaryTestInputJavaType()).feelType(task.unaryTestInputType());
        Expression expression = inTarget(n, input, JavaExpr.of(n, new MethodCallExpr(expr, "get")
                .addArgument(new IntegerLiteralExpr("0")), elementType.getJavaType()).feelType(elementType));
        if (expression == null) {
            return JavaExpr.ofCon(n, new BooleanLiteralExpr(false), boolean.class);
        }
        IfStmt ifStmt2 = new IfStmt();
        ifStmt2.setCondition(new BinaryExpr(new UnaryExpr(result, UnaryExpr.Operator.LOGICAL_COMPLEMENT), expression, BinaryExpr.Operator.AND));
        ifStmt2.setThenStmt(new BlockStmt().addStatement(assignExpr));
        manager.addStatement(ifStmt);
        manager.addStatement(ifStmt2);
        return JavaExpr.of(n, result, boolean.class);
    }

    @Override
    public JavaExpr visit(PathNode n) {
        JavaExpr typedExpr = visit(n.left);
        var inLinePath = typedExpr.inLinePath();
        if (inLinePath != null) {
            JavaExpr apply = inLinePath.apply(manager, n.name);
            return apply.node(n).javaType(n.type.getJavaType()).constant(typedExpr.constant());
        }

        JavaExpr expr = this.typeFactory.getMember(n, typedExpr, n.name, context);
        if (expr != null) {
            return expr;
        }
        return JavaExpr.ofCon(n, new NullLiteralExpr(), void.class);
    }

    @Override
    public JavaExpr visit(ListNode n) {
        if (n.elements.isEmpty()) {
            // empty list
            return JavaExpr.ofCon(n, manager.getStaticMethod(List.class, "of"), List.class);
        }
        var javaType = n.type.getJavaType();
        var elementJavaType = ((FList) n.type).getElementType().getJavaType();


        //如果所有元素都是静态值，则可以创建静态列表（因为FEEL中列表不可变所以可以直接静态，否则就要逃逸检测）
        boolean allConstant = true;
        boolean nonNull = true;
        JavaExpr[] elements = new JavaExpr[n.elements.size()];
        for (int i = 0; i < n.elements.size(); i++) {
            JavaExpr typedExpr = visit(n.elements.get(i));
            typedExpr.expr();//让懒加载的 Expr 进行加载
            allConstant &= typedExpr.constant();
            nonNull &= (typedExpr.node() instanceof AstNodeLiteral node && !(node instanceof NullNode));
            elements[i] = typedExpr;
        }

        String varName = nextVarName("list_");
        var varType = manager.getType(javaType);
        var expr = new VariableDeclarator(varType, varName);
        if (allConstant) {
            //静态列表创建不可变集合
            CodeGens.setComment(expr, n.text);
            if (elements.length == 1) {
                //单个元素使用Collections.singletonList
                MethodCallExpr singletonList = manager.getStaticMethod(Collections.class, "singletonList");
                singletonList.addArgument(elements[0].expr());
                expr.setInitializer(singletonList);
                return JavaExpr.ofCon(n, manager.addStaticField(expr), javaType);
            } else if (nonNull && elements.length < 10) {
                //少量不含null元素使用List.of
                MethodCallExpr asList = manager.getStaticMethod(List.class, "of");
                for (JavaExpr element : elements) {
                    asList.addArgument((Objects.requireNonNullElse(typeFactory.convert(element, FType.getElementType(n.type), context), element)).expr());
                }
                expr.setInitializer(asList);
                return JavaExpr.ofCon(n, manager.addStaticField(expr), javaType);
            } else {
                //更多元素 在静态代码块中初始化
                //创建静态字段
                NameExpr staticField = manager.addStaticField(expr);
                BlockStmt blockStmt = new BlockStmt();
                //创建临时列表
                var creationExpr = new ObjectCreationExpr()
                        .addArgument(new IntegerLiteralExpr(Integer.toString(n.elements.size())));
                creationExpr.setType(manager.getClassType(ArrayList.class).setTypeArguments());
                //创建临时列表本地变量
                var temp = new VariableDeclarator(varType, varName, creationExpr);
                NameExpr tempList = temp.getNameAsExpression();
                //将初始化语句添加至静态代码快
                blockStmt.addStatement(new ExpressionStmt(new VariableDeclarationExpr(temp)));
                for (JavaExpr element : elements) {
                    Expression cast = (Objects.requireNonNullElse(typeFactory.convert(element, FType.getElementType(n.type), context), element)).expr();
                    MethodCallExpr addArgument = new MethodCallExpr(tempList, "add").addArgument(cast);
                    if (cast != element.expr()) {
                        //noinspection SpellCheckingInspection
                        CodeGens.setComment(addArgument, "noinspection unchecked,rawtypes");
                    }
                    blockStmt.addStatement(CodeGens.setComment(new ExpressionStmt(addArgument), element.node().getText()));
                }
                // 添加静态字段赋值语句
                blockStmt.addStatement(new ExpressionStmt(new AssignExpr(staticField,
                        manager.getStaticMethod(Collections.class, "unmodifiableList").addArgument(tempList),
                        AssignExpr.Operator.ASSIGN)));
                manager.addStaticFieldInit(staticField, blockStmt);
                return JavaExpr.ofCon(n, staticField, javaType);
            }
        }


        manager.addComment(n.text);

        NameExpr listExpr = expr.getNameAsExpression();
        manager.addStatement(new VariableDeclarationExpr(expr));

        if (elements.length == 1) {
            //单个元素使用Collections.singletonList
            MethodCallExpr singletonList = manager.getStaticMethod(Collections.class, "singletonList");
            singletonList.addArgument(elements[0].expr());
            expr.setInitializer(singletonList);
        } else {
            var creationExpr = new ObjectCreationExpr();
            creationExpr.setType(manager.getClassType(ArrayList.class).setTypeArguments());
            expr.setInitializer(creationExpr);
            if (elements.length <= 5) {
                // use Arrays.asList 初始化
                MethodCallExpr asList = manager.getStaticMethod(Arrays.class, "asList");
                for (JavaExpr element : elements) {
                    asList.addArgument((Objects.requireNonNullElse(typeFactory.convert(element, FType.getElementType(n.type), context), element)).expr());
                }
                creationExpr.addArgument(asList);
            } else {
                creationExpr.addArgument(new IntegerLiteralExpr(Integer.toString(n.elements.size())));
                for (JavaExpr element : elements) {
                    Expression cast = (Objects.requireNonNullElse(typeFactory.convert(element, FType.getElementType(n.type), context), element)).expr();
                    MethodCallExpr addArgument = new MethodCallExpr(listExpr, "add").addArgument(cast);
                    if (cast != element.expr()) {
                        //noinspection SpellCheckingInspection
                        CodeGens.setComment(addArgument, "noinspection unchecked,rawtypes");
                    }
                    manager.addStatement(addArgument);
                }
            }
        }
        return JavaExpr.of(n, listExpr, javaType);
    }

    @Override
    public JavaExpr visit(RangeNode n) {
        boolean constant = true;
        Expression start, end;
        boolean nullable = false;
        if (n.start != null) {
            JavaExpr startExpr = visit(n.start);
            start = startExpr.expr();
            constant &= startExpr.constant();
            nullable |= startExpr.nullable();
        } else {
            start = new NullLiteralExpr();
        }
        if (n.end != null) {
            JavaExpr endExpr = visit(n.end);
            end = endExpr.expr();
            constant &= endExpr.constant();
            nullable |= endExpr.nullable();
        } else {
            end = new NullLiteralExpr();
        }
        FRange fType = (FRange) n.type;
        Expression expression = this.manager.addStaticFType(fType);

        var declarator = new VariableDeclarator();
        declarator.setName(nextVarName("range_"));
        declarator.setType(manager.getClassType(FeelRange.class).setTypeArguments(manager.getType(fType.getElementType().getWrappedJavaType())));
        declarator.setInitializer(new ObjectCreationExpr().setType(manager.getClassType(FeelRange.Default.class).setTypeArguments())
                .addArgument(expression).addArgument(start).addArgument(end));
        if (constant) {
            return JavaExpr.ofCon(n, manager.addStaticField(declarator, RangeNode.class, n.text), FeelRange.class)
                    .nullable(nullable);
        }
        this.manager.addLocalField(declarator);
        return JavaExpr.of(n, declarator.getNameAsExpression(), fType.getJavaType(), constant)
                .nullable(nullable);
    }

    @Override
    public JavaExpr visit(ContextNode n) {
        if (n.entries.isEmpty()) {
            // empty map
            return JavaExpr.ofCon(n, manager.getStaticMethod(Map.class, "of"), Map.class);
        }
        Map<String, JavaExpr> exprMap = MapUtil.newLinkedHashMap(n.entries.size());
        scope.pushScope();
        boolean allConstant = true;
        for (var entry : n.entries.entrySet()) {
            ASTNode node = entry.getValue();
            JavaExpr expr = visit(node);
            allConstant &= expr.constant();
            exprMap.put(entry.getKey(), expr);
            scope.put(entry.getKey(), expr);
        }
        scope.popScope();
        boolean finalAllConstant = allConstant;
        return JavaExpr.of(n, n.type.getJavaType())
                .expr(() -> {
                    exprMap.values().forEach(JavaExpr::expr);//让懒加载的 Expr 进行加载
                    var javaType = n.type.getJavaType();
                    var jparserType = manager.getType(javaType);
                    String varName = nextVarName("map_");
                    var expr = new VariableDeclarator(jparserType, varName);
                    IntegerLiteralExpr initArg = new IntegerLiteralExpr(Integer.toString((int) ((float) n.entries.size() / 0.75f + 1)));
                    var creationExpr = new ObjectCreationExpr().addArgument(initArg);
                    creationExpr.setType(manager.getClassType(LinkedHashMap.class).setTypeArguments());
                    if (!finalAllConstant) {
                        manager.addComment(n.text);
                        expr.setInitializer(creationExpr);
                        NameExpr mapExpr = expr.getNameAsExpression();
                        manager.addStatement(new VariableDeclarationExpr(expr));
                        for (var entry : exprMap.entrySet()) {
                            manager.addStatement(new MethodCallExpr(mapExpr, "put")
                                    .addArgument(CodeGens.stringLiteral(entry.getKey()))
                                    .addArgument(entry.getValue().expr()));
                        }
                        return mapExpr;
                    }
                    NameExpr staticField = manager.addStaticField(expr);
                    BlockStmt blockStmt = new BlockStmt();

                    var temp = new VariableDeclarator(jparserType, varName, creationExpr);
                    NameExpr tempMap = temp.getNameAsExpression();
                    //将初始化语句添加至静态代码快
                    blockStmt.addStatement(new ExpressionStmt(new VariableDeclarationExpr(temp)));
                    for (var entry : exprMap.entrySet()) {
                        blockStmt.addStatement(new MethodCallExpr(tempMap, "put")
                                .addArgument(CodeGens.stringLiteral(entry.getKey()))
                                .addArgument(entry.getValue().expr()));
                    }
                    blockStmt.addStatement(new ExpressionStmt(new AssignExpr(staticField,
                            manager.getStaticMethod(Collections.class, "unmodifiableMap").addArgument(tempMap),
                            AssignExpr.Operator.ASSIGN)));

                    manager.addStaticFieldInit(staticField, blockStmt);
                    return staticField;
                })
                .constant(allConstant)
                .inLinePath(() -> (manager, path) -> {
                    JavaExpr typedExpr = exprMap.get(path);
                    return typedExpr == null ? JavaExpr.ofNull(n) : typedExpr;
                });
    }

    @Override
    public JavaExpr visit(FunDefinitionNode n) {
        scope.pushFunScope();
        BlockStmt original = manager.getCurrentBlock();
        BlockStmt shadow = new BlockStmt();
        // 生成静态方法，利用该静态方法生成FeelFunction对象，并且返回该静态方法的 inLine
        var method = new MethodDeclaration();
        method.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        method.setName("feelFun$" + manager.getCurrentMethod().getName().getIdentifier() + "$" + feelFunCount++);
        for (var entry : n.params.entrySet()) {
            String name = entry.getKey();
            FType fType = entry.getValue();
            Type javaType = fType.getJavaType();
            method.addParameter(manager.getType(javaType), name);
            JavaExpr typedExpr = JavaExpr.of(n, new NameExpr(name), javaType).feelType(fType);
            scope.put(name, typedExpr);
        }
        manager.setCurrentBlock(shadow);
        JavaExpr bodyResult = visit(n.body);
        manager.setCurrentBlock(original);

        method.setType(manager.getType(bodyResult.javaType()));
        shadow.addStatement(new ReturnStmt(bodyResult.expr()));
        method.setBody(shadow);
        manager.getMethods().add(method);

        boolean hasOuterVar = scope.hasOuterVar();
        boolean convert2NameExpr = scope.convert2NameExpr();
        Map<String, JavaExpr> outerVars = scope.getOuterVars();
        if (hasOuterVar) {
            NodeList<Parameter> parameters = method.getParameters();
            int i = 0;
            for (var entry : outerVars.entrySet()) {
                String name = entry.getKey();
                JavaExpr typedExpr = entry.getValue();
                parameters.add(i++, new Parameter(manager.getType(typedExpr.javaType()), name));
            }
        }

        scope.popScope();
        return JavaExpr.of(n, () -> {
                    // 没有外部引用就是静态函数
                    // 通过lambda构建 feelFunction对象
                    var funField = new VariableDeclarator();
                    funField.setName(manager.nextName("fun_"));
                    funField.setType(manager.getType(n.type.getJavaType()));
                    var creationExpr = new ObjectCreationExpr();
                    creationExpr.setType(manager.getClassType(FeelFunction.Default.class).setTypeArguments());
                    creationExpr.addArgument(manager.addStaticFType(n.type));

                    Expression function;
                    if (!isNoLambda()) {
                        LambdaExpr lambdaExpr = new LambdaExpr();
                        lambdaExpr.addParameter(new UnknownType(), "args");
                        MethodCallExpr methodCallExpr = new MethodCallExpr(method.getName().getIdentifier());
                        NameExpr args = new NameExpr("args");
                        if (hasOuterVar) {
                            for (var entry : outerVars.entrySet()) {
                                Expression expr = entry.getValue().expr();
                                if (convert2NameExpr && !(expr instanceof NameExpr)) {
                                    expr = new NameExpr(entry.getKey());
                                }
                                methodCallExpr.addArgument(expr);
                            }
                        }
                        int index = 0;
                        for (FType value : n.params.values()) {
                            Expression accessExpr = new ArrayAccessExpr(args, new IntegerLiteralExpr(Integer.toString(index++)));
                            if (!(value instanceof FAny)) {
                                accessExpr = new CastExpr(manager.getType(value.getJavaType()), accessExpr);
                            }
                            methodCallExpr.addArgument(accessExpr);
                        }
                        lambdaExpr.setBody(new ExpressionStmt(methodCallExpr));
                        function = lambdaExpr;
                    } else {
                        //替代 InvokeDynamic 手动调用 bootstrap method (BSM) 生成 lambda
                        Type[] methodTypes = new Type[n.params.size()];
                        int index = 0;
                        for (FType value : n.params.values()) {
                            methodTypes[index++] = value.getJavaType();
                        }
                        NameExpr funCallSite = manager.getFunCallSite(method.getNameAsString(), bodyResult.javaType(), outerVars, methodTypes);
                        MethodCallExpr dynamicInvoker = new MethodCallExpr(funCallSite, "dynamicInvoker");
                        MethodCallExpr methodCallExpr = hasOuterVar ? new MethodCallExpr(dynamicInvoker, "invokeWithArguments")
                                : new MethodCallExpr(dynamicInvoker, "invoke");
                        for (var entry : outerVars.entrySet()) {
                            Expression expr = entry.getValue().expr();
                            if (convert2NameExpr && !(expr instanceof NameExpr)) {
                                expr = new NameExpr(entry.getKey());
                            }
                            methodCallExpr.addArgument(expr);
                        }
                        ClassOrInterfaceType lambdaType = new ClassOrInterfaceType(null, new SimpleName("Function"),
                                NodeList.nodeList(manager.getType(Object[].class), manager.getType(bodyResult.javaType())));
                        var expr = new VariableDeclarator(lambdaType, manager.nextName("lambda_"));
                        NameExpr nameExpr;
                        if (!hasOuterVar) {
                            // 静态
                            nameExpr = manager.addStaticField(expr);
                            TryStmt tryStmt = manager.tryInit(n, nameExpr, new CastExpr(lambdaType, methodCallExpr), "noinspection unchecked");
                            manager.addStaticFieldInit(nameExpr, new BlockStmt().addStatement(tryStmt));
                        } else {
                            manager.addStatement(new VariableDeclarationExpr(expr));
                            nameExpr = expr.getNameAsExpression();
                            TryStmt tryStmt = manager.tryInit(n, nameExpr, new CastExpr(lambdaType, methodCallExpr), "noinspection unchecked");
                            manager.addStatement(tryStmt);
                        }
                        function = nameExpr;
                    }
                    creationExpr.addArgument(function);
                    funField.setInitializer(creationExpr);
                    if (hasOuterVar) {
                        this.manager.addStatement(new VariableDeclarationExpr(funField));
                    }
                    return hasOuterVar ? funField.getNameAsExpression() :
                            this.manager.addStaticField(funField, FunDefinitionNode.class, n.text);
                }, n.type.getJavaType(), !hasOuterVar)
                .inLineFun(() -> (manager, params) -> {
                    MethodCallExpr callExpr = new MethodCallExpr(method.getName().getIdentifier());
                    for (var entry : outerVars.entrySet()) {
                        Expression expr = entry.getValue().expr();
                        if (convert2NameExpr && !(expr instanceof NameExpr)) {
                            expr = new NameExpr(entry.getKey());
                        }
                        callExpr.addArgument(expr);
                    }
                    for (JavaExpr param : params) {
                        callExpr.addArgument(param.expr());
                    }
                    return callExpr;
                });
    }

    @Override
    public JavaExpr visit(NameExprNode node) {
        JavaExpr resolve = scope.resolve(node, node.text, node.type);
        return resolve == null ? JavaExpr.ofNull(node) : resolve;
    }

    @Override
    public JavaExpr visit(FunInvocationNode n) {
        boolean allConstant = true;
        JavaExpr fun = visit(n.function);
        var inLineFun = fun.inLineFun();
        boolean inLine = inLineFun != null;

        allConstant &= fun.constant();
        FFunction type = (FFunction) fun.feelType();

        Type resultType = type.getReturnType().getJavaType();
        var resultAstType = manager.getType(resultType);

        Expression invoke;
        JavaExpr[] params = new JavaExpr[n.params.size()];

        for (int i = 0; i < n.params.size(); i++) {
            params[i] = convert(visit(n.params.get(i)), type.getParamType(i));
            allConstant &= params[i].constant();
        }
        if (inLine) {
            invoke = inLineFun.apply(manager, params);
        } else {
            MethodCallExpr methodCallExpr = new MethodCallExpr(fun.expr(), "invoke");
            if (params.length == 1 && params[0].javaType() instanceof Class<?> clazz && (clazz.isArray() || clazz == void.class)) {
                //如果只有一个参数，且参数类型是 null 或者数组，则需要强换
                methodCallExpr.addArgument(manager.castTo(Object.class, params[0].expr()));
            } else {
                for (JavaExpr param : params) {
                    methodCallExpr.addArgument(param.expr());
                }
            }
            invoke = methodCallExpr;
        }

        boolean needCast = true;
        if (fun.javaType() instanceof ParameterizedType pType && pType.getRawType() instanceof Class<?> clazz && FeelFunction.class.isAssignableFrom(clazz)) {
            Type[] args = pType.getActualTypeArguments();
            //泛型中的返回值类型和实际返回值类型一致时，不需要强转
            if (args.length == 1 && args[0].equals(TypeUtil.primitiveToWrapper(resultType))) {
                needCast = false;
            }
        }
        //返回值根据是否需要强转 不内联时 FeeLFunction<?> 具备泛型，需要根据 castGenerics 强转
        Expression result = needCast || (!inLine && isCastGenerics()) ? manager.castTo(resultAstType, invoke) : invoke;
        if (!allConstant) {
            return JavaExpr.of(n, result, resultType);
        }
        var declarator = new VariableDeclarator();
        declarator.setName(nextVarName("fCall_"));
        declarator.setType(resultAstType);
        declarator.setInitializer(result);
        return JavaExpr.ofCon(n, this.manager.addStaticField(declarator, FunInvocationNode.class, n.text), resultType);
    }

    private JavaExpr convert(JavaExpr original, FType target) {
        JavaExpr convert = typeFactory.convert(original, target, context);
        if (convert != null) {
            return convert;
        }
        if (original.feelType().conformsTo(target)) {
            return original;
        }
        return JavaExpr.ofNull(original.node()).original(original);
    }

    @Override
    public JavaExpr visit(InstanceOfNode n) {
        JavaExpr valueExpr = visit(n.value);
        var expr = new VariableDeclarator(PrimitiveType.booleanType(), nextVarName("instance_of_"),
                new MethodCallExpr(manager.addStaticFType(n.type), "isInstance")
                        .addArgument(valueExpr.expr())
        );
        if (valueExpr.constant()) {
            return JavaExpr.of(n, manager.addStaticField(expr, InstanceOfNode.class, n.text), boolean.class, true);
        } else {
            manager.addComment(n.text);
            manager.addStatement(new VariableDeclarationExpr(expr));
            return JavaExpr.of(n, expr.getNameAsExpression(), boolean.class, false);
        }
    }

    @Override
    public JavaExpr visit(InfixOpNode n) {
        JavaExpr left = visit(n.left);
        FType leftType = left.feelType();
        InfixOpNode.Op op = n.op;
        JavaExpr right = visit(n.right);
        FType rightType = right.feelType();


        Expression leftEx = left.expr();
        if (!left.isSimple()) {
            var leftVar = new VariableDeclarator(manager.getType(leftType.getJavaType()), nextVarName("left"), leftEx);
            left.expr(leftVar.getNameAsExpression());
            if (left.constant()) {
                left.expr(manager.addStaticField(leftVar));
            } else {
                manager.addLocalField(leftVar);
            }
        }
        Expression rightEx = right.expr();
        if (!right.isSimple()) {
            var rightVar = new VariableDeclarator(manager.getType(rightType.getJavaType()), nextVarName("right"), rightEx);
            right.expr(rightVar.getNameAsExpression());
            if (right.constant()) {
                right.expr(manager.addStaticField(rightVar));
            } else {
                manager.addLocalField(rightVar);
            }
        }
        JavaExpr typedExpr = typeFactory.infixOp(n, left, right, op, context);
        if (typedExpr == null) {
            return JavaExpr.ofCon(n, new NullLiteralExpr(), void.class);
        }

        return typedExpr;
    }

    @Override
    public JavaExpr visit(BetweenNode n) {
        JavaExpr value = visit(n.value);
        JavaExpr start = visit(n.start);
        JavaExpr end = visit(n.end);
        FType type = value.feelType();
        Expression left;
        Expression right;

        if (type instanceof FNumber) {
            if (value.javaType() == BigDecimal.class || start.javaType() == BigDecimal.class || end.javaType() == BigDecimal.class) {
                Expression valueExpr = bigDecimalValue(value);
                left = new BinaryExpr(new MethodCallExpr(valueExpr, "compareTo").addArgument(bigDecimalValue(start)), new IntegerLiteralExpr("0"),
                        BinaryExpr.Operator.GREATER_EQUALS);
                right = new BinaryExpr(new MethodCallExpr(valueExpr, "compareTo").addArgument(bigDecimalValue(end)), new IntegerLiteralExpr("0"),
                        BinaryExpr.Operator.LESS_EQUALS);
            } else {
                Expression valueExpr = numberValue(value);
                left = new BinaryExpr(valueExpr, numberValue(start), BinaryExpr.Operator.GREATER_EQUALS);
                right = new BinaryExpr(valueExpr, numberValue(end), BinaryExpr.Operator.LESS_EQUALS);
            }
        } else if (type instanceof FBoolean) {
            left = new BinaryExpr(new MethodCallExpr(new NameExpr("Boolean"), "compare").addArgument(value.expr()).addArgument(start.expr()),
                    new IntegerLiteralExpr("0"), BinaryExpr.Operator.GREATER_EQUALS);
            right = new BinaryExpr(new MethodCallExpr(new NameExpr("Boolean"), "compare").addArgument(value.expr()).addArgument(end.expr()),
                    new IntegerLiteralExpr("0"), BinaryExpr.Operator.LESS_EQUALS);
        } else if (type instanceof FString || type instanceof FTime || type instanceof FDate ||
                   type instanceof FDateTime || type instanceof FDayTimeDuration) {
            left = new BinaryExpr(new MethodCallExpr(value.expr(), "compareTo").addArgument(start.expr()), new IntegerLiteralExpr("0"),
                    BinaryExpr.Operator.GREATER_EQUALS);
            right = new BinaryExpr(new MethodCallExpr(value.expr(), "compareTo").addArgument(end.expr()), new IntegerLiteralExpr("0"),
                    BinaryExpr.Operator.LESS_EQUALS);
        } else if (type instanceof FYearMonthDuration) {
            //Period.toTotalMonths()
            Expression valueExpr = new MethodCallExpr(value.expr(), "toTotalMonths");
            left = new BinaryExpr(valueExpr, new MethodCallExpr(start.expr(), "toTotalMonths"), BinaryExpr.Operator.GREATER_EQUALS);
            right = new BinaryExpr(valueExpr, new MethodCallExpr(end.expr(), "toTotalMonths"), BinaryExpr.Operator.LESS_EQUALS);
        } else {
            throw new FeelLangException(n, "Cannot compile between node for type: " + type);
        }
        // 创建条件表达式
        BinaryExpr condition = new BinaryExpr(left, right, BinaryExpr.Operator.AND);
        // 创建变量声明，给变量赋值 true
        String varName = nextVarName("between_");
        var expr = new VariableDeclarator(PrimitiveType.booleanType(), varName, new BooleanLiteralExpr(false));
        NameExpr result = expr.getNameAsExpression();
        // 创建if语句
        IfStmt ifStmt = new IfStmt();
        // 设置条件
        ifStmt.setCondition(condition);
        // 创建 if 代码块
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new AssignExpr(result, new BooleanLiteralExpr(true), AssignExpr.Operator.ASSIGN));
        ifStmt.setThenStmt(blockStmt);

        if (!value.constant() || !start.constant() || !end.constant()) {
            // 将创建的表达式和语句添加到主方法中
            manager.addLocalField(expr);
            manager.addStatement(ifStmt);
            // 返回变量名
            return JavaExpr.of(n, result, boolean.class);
        } else {
            NameExpr staticField = manager.addStaticField(new VariableDeclarator(PrimitiveType.booleanType(), varName));
            BlockStmt initBlockStmt = new BlockStmt();
            initBlockStmt.addStatement(new VariableDeclarationExpr(expr));
            initBlockStmt.addStatement(ifStmt);
            // 添加静态字段赋值语句
            initBlockStmt.addStatement(new ExpressionStmt(new AssignExpr(staticField, result, AssignExpr.Operator.ASSIGN)));
            manager.addStaticFieldInit(staticField, initBlockStmt);
            return JavaExpr.ofCon(n, staticField, boolean.class);
        }
    }

    private String nextVarName(String prefix) {
        int compute = countMap.compute(prefix, (k, v) -> v == null ? 0 : v + 1);
        return compute == 0 ? prefix : (prefix + compute);
    }

    private int markVarName(String prefix) {
        return countMap.computeIfAbsent(prefix, k -> -1);
    }

    private void resetVarName(String prefix, int compute) {
        countMap.put(prefix, compute);
    }

    private Expression numberValue(JavaExpr expression) {
        return convert(expression, FNumber.DOUBLE).expr();
    }

    private Expression bigDecimalValue(JavaExpr expr) {
        JavaExpr convert = convert(expr, FNumber.BIG_DECIMAL);
        if (expr.constant()) {
            // can be static
            return manager.addStaticField(new VariableDeclarator(manager.getClassType(BigDecimal.class), nextVarName("big_"))
                    .setInitializer(convert.expr()), BigDecimal.class, expr.expr().toString());
        }
        return convert.expr();
    }

    private Expression intValue(JavaExpr expr) {
        if (expr.feelType() instanceof FNumber) {
            if (expr.javaType() instanceof Class<?> clazz && clazz.isPrimitive()) {
                return expr.expr();
            }
            if (expr.node() instanceof NumberNode node) {
                // can be static
                return new MethodCallExpr(manager.addStaticBigDecimal(node.getValue()), "intValue");
            }
            return new MethodCallExpr(expr.expr(), "intValue");
        }
        throw new FeelLangException(expr.node(), "Cannot convert to BigDecimal: " + expr.feelType());
    }

    private Expression getIfCondition(JavaExpr condition) {
        Expression conditionExpr = condition.expr();
        if (condition.feelType() instanceof FBoolean) {
            if (!condition.nullable()) {
                return conditionExpr;
            } else {
                // Boolean.TRUE.equals
                return new MethodCallExpr(manager.getStaticField(Boolean.class, "TRUE"), "equals").addArgument(conditionExpr);
            }
        } else {
            if (condition.nullable()) {
                //  Objects.requireNonNullElse(,false)
                conditionExpr = manager.getStaticMethod(Objects.class, "requireNonNullElse").addArgument(conditionExpr).addArgument(new BooleanLiteralExpr(false));
            }
            // !Boolean.FALSE.equals
            return new UnaryExpr(new MethodCallExpr(manager.getStaticField(Boolean.class, "FALSE"), "equals").addArgument(conditionExpr),
                    UnaryExpr.Operator.LOGICAL_COMPLEMENT);
        }
    }
}