package com.muyuanjin.feel.impl;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.muyuanjin.common.util.DateUtil;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FTypes;
import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.ParserUtil;
import com.muyuanjin.feel.translate.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;

/**
 * @author muyuanjin
 */
@Slf4j
@SuppressWarnings("unused")
public class DefaultFeelTypeFactory implements FeelTypeFactory {
    public static final DefaultFeelFunctionFactory INSTANCE = new DefaultFeelFunctionFactory();

    @Override
    public @NotNull Map<String, FType> getTypes() {
        return FTypes.ENUMS_MAP;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public JavaExpr getMember(ASTNode node, JavaExpr source, String member, Context context) {
        Object o = doGetMember(node, source, member, context);
        if (o instanceof Expression expression) {
            return JavaExpr.of(node, expression, node.getType().getJavaType(), source.constant());
        }
        if (o instanceof JavaExpr expr) {
            return expr;
        }
        if (o == null) {
            return null;
        }
        // can not be here
        throw new IllegalStateException("Unexpected value: " + o);
    }

    @Override
    public @Nullable JavaExpr infixOp(ASTNode n, JavaExpr left, JavaExpr right, InfixOpNode.Op op, Context context) {
        return DefaultFeelInfixOps.infixOp(n, left, right, op, context);
    }

    @Override
    public Expression indexOf(JavaExpr source, JavaExpr index, Context context) {
        FType type = source.feelType();
        ClassManager manager = ClassManager.instance(context);
        if (type instanceof FNumber number) {
            return switch (number) {
                case INTEGER, LONG -> new BinaryExpr(source.expr(), index.expr(), BinaryExpr.Operator.PLUS);
                default -> manager.getStaticMethod(DefaultFeelTypeFactory.class, "nextNumber")
                        .addArgument(source.expr()).addArgument(index.expr());
            };
        }
        if (type instanceof FBoolean) {
            // 如果 index expr 的结果不是 0,1 或 0,-1 则返回 null
            // 0 返回 source.expr ，1，-1 返回 !source.expr
            // 构建 SwitchExpr
            SwitchExpr switchExpr = new SwitchExpr();
            switchExpr.setSelector(new MethodCallExpr(manager.getStaticMethod(Objects.class, "requireNonNull")
                    .addArgument(source.expr()), "intValue"));
            // 添加 SwitchEntry
            switchExpr.setEntries(NodeList.nodeList(
                    new SwitchEntry(NodeList.nodeList(new IntegerLiteralExpr("0")), SwitchEntry.Type.EXPRESSION, NodeList.nodeList(new ExpressionStmt(source.expr()))),
                    new SwitchEntry(NodeList.nodeList(new IntegerLiteralExpr("1"), new IntegerLiteralExpr("-1")), SwitchEntry.Type.EXPRESSION,
                            NodeList.nodeList(new ExpressionStmt(
                                    CodeGens.ifOneNullAllNull(null, source,
                                            new UnaryExpr(source.expr(), UnaryExpr.Operator.LOGICAL_COMPLEMENT), true)))),
                    new SwitchEntry(NodeList.nodeList(), SwitchEntry.Type.EXPRESSION, NodeList.nodeList(new ExpressionStmt(new NullLiteralExpr())))
            ));
            return CodeGens.ifOneNullAllNull(source, index, switchExpr, false);
        }
        if (type instanceof FDate) {
            //plusDays
            return new MethodCallExpr(source.expr(), "plusDays").addArgument(index.expr());
        }
        if (type instanceof FDateTime || type instanceof FTime || type instanceof FDayTimeDuration) {
            //plusSeconds
            return new MethodCallExpr(source.expr(), "plusSeconds").addArgument(index.expr());
        }
        if (type instanceof FYearMonthDuration) {
            //plusMonths
            return new MethodCallExpr(source.expr(), "plusMonths").addArgument(index.expr());
        }
        return null;
    }

    @Override
    public @Nullable ForStmt foreach(Expression start, Expression end,
                                     boolean startInclusive, boolean endInclusive,
                                     FType type, SimpleName name, Context context) {
        ForStmt forStmt = new ForStmt();
        forStmt.setInitialization(new NodeList<>());
        forStmt.setUpdate(new NodeList<>());
        NameExpr nameExpr = new NameExpr(name);
        ClassManager manager = ClassManager.instance(context);

        Type elementType = manager.getType(type.getJavaType());
        if (type instanceof FNumber number) {
            return switch (number) {
                case INTEGER, LONG -> {
                    if (!startInclusive) {
                        start = new BinaryExpr(start, new IntegerLiteralExpr("1"), BinaryExpr.Operator.PLUS);
                    }
                    forStmt.getInitialization().add(new VariableDeclarationExpr(new VariableDeclarator(elementType, name, start)));
                    forStmt.getUpdate().add(new UnaryExpr(nameExpr, UnaryExpr.Operator.POSTFIX_INCREMENT));
                    yield forStmt.setCompare(new BinaryExpr(nameExpr, end, endInclusive ? BinaryExpr.Operator.LESS_EQUALS : BinaryExpr.Operator.LESS));
                }
                default -> {
                    if (!startInclusive) {
                        start = manager.getStaticMethod(DefaultFeelTypeFactory.class, "nextNumber")
                                .addArgument(nameExpr);
                    }
                    forStmt.getInitialization().add(new VariableDeclarationExpr(new VariableDeclarator(elementType, name, start)));
                    forStmt.getUpdate().add(new AssignExpr(nameExpr, manager.getStaticMethod(DefaultFeelTypeFactory.class, "nextNumber")
                            .addArgument(nameExpr), AssignExpr.Operator.ASSIGN));
                    if (number == FNumber.DOUBLE) {
                        yield forStmt.setCompare(new BinaryExpr(nameExpr, end, endInclusive ? BinaryExpr.Operator.LESS_EQUALS : BinaryExpr.Operator.LESS));
                    }
                    yield forStmt.setCompare(manager.getStaticMethod(DefaultFeelTypeFactory.class, "hasNext")
                            .addArgument(end).addArgument(nameExpr).addArgument(new BooleanLiteralExpr(endInclusive)));
                }
            };
        }
        if (type instanceof FBoolean) {
            NameExpr i = new NameExpr(manager.nextName("i"));
            forStmt.getInitialization().add(new VariableDeclarationExpr(new VariableDeclarator(PrimitiveType.intType(), i.getName(),
                    new IntegerLiteralExpr(startInclusive ? "0" : "1"))));
            forStmt.setCompare(new BinaryExpr(nameExpr, new IntegerLiteralExpr(endInclusive ? "2" : "1"), BinaryExpr.Operator.LESS));
            forStmt.getUpdate().add(new UnaryExpr(i, UnaryExpr.Operator.POSTFIX_INCREMENT));
            BlockStmt blockStmt = new BlockStmt();
            forStmt.setBody(blockStmt);
            // 使用三元表达式
            ConditionalExpr conditionalExpr = new ConditionalExpr(start,
                    new UnaryExpr(new BinaryExpr(i, new IntegerLiteralExpr("0"), BinaryExpr.Operator.EQUALS), UnaryExpr.Operator
                            .LOGICAL_COMPLEMENT), new BinaryExpr(i, new IntegerLiteralExpr("0"), BinaryExpr.Operator.EQUALS));
            blockStmt.addStatement(new VariableDeclarationExpr(new VariableDeclarator(elementType, name, conditionalExpr)));
            return forStmt;
        }
        if (type instanceof FDate) {
            if (!startInclusive) {
                start = new MethodCallExpr(start, "plusDays").addArgument(new IntegerLiteralExpr("1"));
            }
            forStmt.getInitialization().add(new VariableDeclarationExpr(new VariableDeclarator(elementType, name, start)));
            forStmt.getUpdate().add(new AssignExpr(nameExpr, new MethodCallExpr(nameExpr, "plusDays")
                    .addArgument(new IntegerLiteralExpr("1")), AssignExpr.Operator.ASSIGN));
            forStmt.setCompare(new BinaryExpr(new MethodCallExpr(nameExpr, "compareTo").addArgument(end),
                    new IntegerLiteralExpr("0"), endInclusive ? BinaryExpr.Operator.LESS_EQUALS : BinaryExpr.Operator.LESS));
            return forStmt;
        }
        if (type instanceof FDateTime || type instanceof FTime || type instanceof FDayTimeDuration) {
            if (!startInclusive) {
                start = new MethodCallExpr(start, "plusSeconds").addArgument(new IntegerLiteralExpr("1"));
            }
            forStmt.getInitialization().add(new VariableDeclarationExpr(new VariableDeclarator(elementType, name, start)));
            forStmt.getUpdate().add(new AssignExpr(nameExpr, new MethodCallExpr(nameExpr, "plusSeconds")
                    .addArgument(new IntegerLiteralExpr("1")), AssignExpr.Operator.ASSIGN));
            forStmt.setCompare(new BinaryExpr(new MethodCallExpr(nameExpr, "compareTo").addArgument(end),
                    new IntegerLiteralExpr("0"), endInclusive ? BinaryExpr.Operator.LESS_EQUALS : BinaryExpr.Operator.LESS));
            return forStmt;
        }
        if (type instanceof FYearMonthDuration) {
            if (!startInclusive) {
                start = new MethodCallExpr(start, "plusMonths").addArgument(new IntegerLiteralExpr("1"));
            }
            forStmt.getInitialization().add(new VariableDeclarationExpr(new VariableDeclarator(elementType, name, start)));
            forStmt.getUpdate().add(new AssignExpr(nameExpr, new MethodCallExpr(nameExpr, "plusMonths")
                    .addArgument(new IntegerLiteralExpr("1")), AssignExpr.Operator.ASSIGN));
            // Period has no compareTo ,use toTotalMonths
            forStmt.setCompare(new BinaryExpr(new MethodCallExpr(nameExpr, "toTotalMonths"),
                    new MethodCallExpr(end, "toTotalMonths"), endInclusive ? BinaryExpr.Operator.LESS_EQUALS : BinaryExpr.Operator.LESS));
            return forStmt;
        }
        return null;
    }

    private Object doGetMember(ASTNode node, JavaExpr source, String member, Context context) {
        Expression expr = source.expr();
        FType sourceType = source.feelType();
        FType nodeType = node.getType();
        ClassManager manager = ClassManager.instance(context);
        CompilerTask task = CompilerTask.instance(context);
        boolean castGenerics = task.castGenerics();
        if (sourceType instanceof FContext ctx) {
            if (!(ctx.getJavaType() instanceof Class<?> clazz)) {
                FType type = ctx.getMembers().getOrDefault(member, nodeType);
                //Map base
                return manager.castTo(type.getJavaType(), new MethodCallExpr(expr, "get").addArgument(CodeGens.stringLiteral(member)));
            }
            //POJO
            Method method = ParserUtil.PROPERTY_READ.get(clazz).get(member);
            if (method == null) {
                return null;
            }
            return new MethodCallExpr(expr, method.getName());
        }
        if (sourceType instanceof FString) {
            return switch (member) {
                case "trim" -> new MethodCallExpr(expr, "trim");
                case "strip" -> new MethodCallExpr(expr, "strip");
                case "length" -> new MethodCallExpr(expr, "length");
                case "isBlank" -> new MethodCallExpr(expr, "isBlank");
                case "isEmpty" -> new MethodCallExpr(expr, "isEmpty");
                case "lowerCase" -> new MethodCallExpr(expr, "toLowerCase");
                case "upperCase" -> new MethodCallExpr(expr, "toUpperCase");
                default -> null;
            };
        }
        if (sourceType instanceof FDate) {
            return switch (member) {
                case "year" -> new MethodCallExpr(expr, "getYear");
                case "lengthOfYear" -> new MethodCallExpr(expr, "lengthOfYear");
                case "isLeapYear" -> new MethodCallExpr(expr, "isLeapYear");
                case "dayOfYear" -> new MethodCallExpr(expr, "getDayOfYear");
                case "month" -> new MethodCallExpr(expr, "getMonthValue");
                case "dayOfMonth", "day" -> new MethodCallExpr(expr, "getDayOfMonth");
                case "weekday" -> new MethodCallExpr(new MethodCallExpr(expr, "getDayOfWeek"), "getValue");
                case "epochDay", "value" -> new MethodCallExpr(expr, "toEpochDay");
                default -> null;
            };
        }
        if (sourceType instanceof FTime) {
            return switch (member) {
                case "hour" -> new MethodCallExpr(expr, "getHour");
                case "minute" -> new MethodCallExpr(expr, "getMinute");
                case "second" -> new MethodCallExpr(expr, "getSecond");
                //TODO time offset timezone 完整实现?
                case "time offset" ->
                        manager.getStaticMethod(Duration.class, "ofSeconds").addArgument(manager.getStaticField(DateUtil.class, "DEFAULT_ZONE_OFFSET"));
                case "timezone" ->
                        new MethodCallExpr(manager.getStaticMethod(TimeZone.class, "getTimeZone").addArgument(manager.getStaticField(DateUtil.class, "DEFAULT_TIME_ZONE")), "getID");
                default -> null;
            };
        }
        if (sourceType instanceof FDateTime) {
            return switch (member) {
                case "date" -> new MethodCallExpr(expr, "toLocalDate");
                case "time" -> new MethodCallExpr(expr, "toLocalTime");
                case "year" -> new MethodCallExpr(expr, "getYear");
                case "month" -> new MethodCallExpr(expr, "getMonthValue");
                case "day" -> new MethodCallExpr(expr, "getDayOfMonth");
                case "weekday" -> new MethodCallExpr(new MethodCallExpr(expr, "getDayOfWeek"), "getValue");
                case "hour" -> new MethodCallExpr(expr, "getHour");
                case "minute" -> new MethodCallExpr(expr, "getMinute");
                case "second" -> new MethodCallExpr(expr, "getSecond");
                //TODO time offset timezone 完整实现?
                case "value", "epochSecond" -> new MethodCallExpr(expr, "toEpochSecond")
                        .addArgument(manager.getStaticField(DateUtil.class, "DEFAULT_ZONE_OFFSET"));
                case "time offset" ->
                        manager.getStaticMethod(Duration.class, "ofSeconds").addArgument(manager.getStaticField(DateUtil.class, "DEFAULT_ZONE_OFFSET"));
                case "timezone" ->
                        new MethodCallExpr(manager.getStaticMethod(TimeZone.class, "getTimeZone").addArgument(manager.getStaticField(DateUtil.class, "DEFAULT_TIME_ZONE")), "getID");
                default -> null;
            };
        }
        if (sourceType instanceof FDayTimeDuration) {
            return switch (member) {
                case "days" -> new MethodCallExpr(expr, "toDaysPart");//since java 9
                case "hours" -> new MethodCallExpr(expr, "toHoursPart");//since java 9
                case "minutes" -> new MethodCallExpr(expr, "toMinutesPart");//since java 9
                case "seconds" -> new MethodCallExpr(expr, "toSecondsPart");//since java 9
                case "value" -> new MethodCallExpr(expr, "toSeconds");
                default -> null;
            };
        }
        if (sourceType instanceof FYearMonthDuration) {
            return switch (member) {
                case "years" -> new MethodCallExpr(expr, "getYears");
                case "months" -> new MethodCallExpr(expr, "getMonths");
                case "value" -> new MethodCallExpr(expr, "toTotalMonths");
                default -> null;
            };
        }
        if (sourceType instanceof FRange range) {
            return switch (member) {
                case "start" -> new MethodCallExpr(expr, "start");
                case "end" -> new MethodCallExpr(expr, "end");
                case "start included" -> new MethodCallExpr(expr, "startInclusive");
                case "end included" -> new MethodCallExpr(expr, "endInclusive");
                default -> null;
            };
        }
        if (sourceType instanceof FFunction function) {
            MethodCallExpr type = new MethodCallExpr(expr, "type");
            return switch (member) {
                case "returnType" -> new MethodCallExpr(type, "getReturnType");
                case "parameterTypes" -> new MethodCallExpr(type, "getParameterTypes");
                case "parameterNames" -> new MethodCallExpr(type, "getParameterNames");
                default -> null;
            };
        }
        if (sourceType instanceof FList list) {
            return switch (member) {
                case "size" -> new MethodCallExpr(expr, "size");
                case "isEmpty" -> new MethodCallExpr(expr, "isEmpty");
                case "isNotEmpty" ->
                        new UnaryExpr(new MethodCallExpr(expr, "isEmpty"), UnaryExpr.Operator.LOGICAL_COMPLEMENT);
                case "contains" -> {
                    MethodCallExpr invoke = new MethodCallExpr(manager.getStaticField(DefaultFeelTypeFactory.class, "list0"), "apply")
                            .addArgument(expr);
                    FType type = list.getMembers().getOrDefault("contains", nodeType);
                    Expression expression = castGenerics ? manager.castTo(type.getJavaType(), invoke) : invoke;
                    yield JavaExpr.of(node, expression, type.getJavaType(), source.constant())
                            .inLineFun((classManager, args) -> new MethodCallExpr(expr, "contains").addArgument(args[0].expr()));
                }
                default -> new NullLiteralExpr();
            };
        }
        if (sourceType instanceof FNumber || sourceType instanceof FBoolean) {
            return null;
        }
        //null or any
        return manager.getStaticMethod(ParserUtil.class, "accessMember")
                .addArgument(expr).addArgument(CodeGens.stringLiteral(member));
    }

    @Override
    public JavaExpr convert(JavaExpr original, FType target, Context context) {
        FType fType = original.feelType();
        int canBe = fType.canBe(target);
        if (canBe == FType.EQ || canBe == FType.CT) {
            return original;
        }
        if (canBe == FType.NO) {
            return null;
        }
        ClassManager manager = ClassManager.instance(context);
        //TODO use canBe 装拆箱 的转换
        if (fType instanceof FNumber number && target instanceof FNumber targetNumber) {
            return switch (number) {
                case INTEGER, LONG, DOUBLE -> {
                    if (targetNumber == FNumber.BIG_DECIMAL) {
                        yield original.convert(expr -> manager.getStaticMethod(BigDecimal.class, "valueOf")
                                .addArgument(expr), FNumber.BIG_DECIMAL, BigDecimal.class);
                    } else yield original;
                }
                case BIG_DECIMAL, NUMBER -> switch (targetNumber) {
                    case INTEGER ->
                            original.convert(expr -> new MethodCallExpr(expr, "intValue"), FNumber.INTEGER, int.class);
                    case LONG ->
                            original.convert(expr -> new MethodCallExpr(expr, "longValue"), FNumber.LONG, long.class);
                    case DOUBLE ->
                            original.convert(expr -> new MethodCallExpr(expr, "doubleValue"), FNumber.DOUBLE, double.class);
                    case BIG_DECIMAL, NUMBER -> {
                        if (number == FNumber.NUMBER) {
                            // number to big decimal
                            if (original.javaType() == BigDecimal.class) {
                                if (log.isDebugEnabled()) {
                                    log.debug("不精确的类型描述, BigDecimal 对象的 FType 被设置为了 FNumber.NUMBER 而不是 FNumber.BIG_DECIMAL", new Throwable());
                                }
                                yield original;
                            }
                            yield original.convert(expr -> manager.getStaticMethod(DefaultFeelTypeFactory.class, "toBigDecimal")
                                    .addArgument(expr), FNumber.BIG_DECIMAL, BigDecimal.class);
                        }
                        yield original;
                    }
                };
            };
        }
        if (fType instanceof FList list && target instanceof FList targetList) {
            // 使用泛型擦除强转
            return original.convert(expr -> new CastExpr(manager.getListType(), expr), targetList, targetList.getJavaType());
        }

        if (fType instanceof FDate date) {
            if (target instanceof FDateTime dateTime) {
                return original.convert(expr -> new MethodCallExpr(expr, "atStartOfDay"), FDateTime.DATE_TIME, LocalDateTime.class);
            }
        }
        if (fType instanceof FDateTime dateTime) {
            if (target instanceof FDate date) {
                return original.convert(expr -> new MethodCallExpr(expr, "toLocalDate"), FDate.DATE, LocalDate.class);
            }
            if (target instanceof FTime time) {
                return original.convert(expr -> new MethodCallExpr(expr, "toLocalTime"), FTime.TIME, LocalTime.class);
            }
        }
        //TODO 其他类型转换 比如 OffsetDateTime
        return null;
    }

    public static BigDecimal toBigDecimal(Number number) {
        return number instanceof BigDecimal b ? b : new BigDecimal(number.toString(), MathContext.DECIMAL128);
    }

    public static <T extends Number> T nextNumber(final T current) {
        return nextNumber(current, 1);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T nextNumber(final T current, long distance) {
        if (!(current instanceof BigDecimal) && !(current instanceof Double)) {
            throw new IllegalArgumentException("Unsupported number type: " + (current == null ? null : current.getClass()));
        }
        BigDecimal value;
        BigDecimal result;
        if (current instanceof BigDecimal decimal) {
            value = decimal;
        } else {
            value = new BigDecimal(current.toString(), MathContext.DECIMAL128);
        }
        //整数加1，小数加自身的最小缩放单位
        int scale = value.scale();//小数位数
        if (scale <= 0 || (value = value.stripTrailingZeros()).scale() <= 0) {
            result = value.add(BigDecimal.valueOf(distance));
        } else {
            result = value.add(BigDecimal.valueOf(distance, scale));
        }
        if (current instanceof Double d) {
            return (T) (Double) result.doubleValue();
        }
        return (T) result;
    }

    public static boolean hasNext(Number end, Number current, boolean endInclusive) {
        if (end instanceof BigDecimal || current instanceof BigDecimal) {
            if (endInclusive) {
                return toBigDecimal(end).compareTo(toBigDecimal(current)) >= 0;
            }
            return toBigDecimal(end).compareTo(toBigDecimal(current)) > 0;
        }
        if (endInclusive) {
            return end.doubleValue() >= current.doubleValue();
        }
        return end.doubleValue() > current.doubleValue();
    }

    public static final Function<List<?>, FeelFunction<Boolean>> list0 = list -> new FeelFunction.Default<>(
            FFunction.of(FBoolean.BOOLEAN, FAny.ANY),
            args -> list.contains(args[0])
    );
}
