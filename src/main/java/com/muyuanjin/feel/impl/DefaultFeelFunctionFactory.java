package com.muyuanjin.feel.impl;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.FeelFunctionFactory;
import com.muyuanjin.feel.lang.FeelFunction;
import com.muyuanjin.feel.lang.FeelFunctions;
import com.muyuanjin.feel.lang.ast.ASTNode;
import com.muyuanjin.feel.lang.type.FDate;
import com.muyuanjin.feel.lang.type.FFunction;
import com.muyuanjin.feel.parser.ParserUtil;
import com.muyuanjin.feel.translate.ClassManager;
import com.muyuanjin.feel.translate.Context;
import com.muyuanjin.feel.translate.JavaExpr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author muyuanjin
 */
@SuppressWarnings("unused")
public class DefaultFeelFunctionFactory implements FeelFunctionFactory {
    public static final DefaultFeelFunctionFactory INSTANCE = new DefaultFeelFunctionFactory();

    @Override
    public Map<String, Set<FFunction>> getFunctions() {
        return functions;
    }

    @Override
    public JavaExpr getFunction(ASTNode node, String fun, FFunction function, Context context) {
        FeelFunctions functions = FeelFunctions.from(fun);
        if (functions == null) {
            return null;
        }
        ClassManager manager = ClassManager.instance(context);
        return switch (functions) {
            case date -> {
                int index = functions.getFunctions().indexOf(function);
                yield switch (index) {
                    case 0 ->
                            JavaExpr.of(node, manager.getStaticField(DefaultFeelFunctionFactory.class, "date0"), function.getJavaType(), true)
                                    .inLineFun((classManager, args) -> classManager.getStaticMethod(ParserUtil.class, "parseDate").addArgument(args[0].expr()));
                    case 1 ->
                            JavaExpr.of(node, manager.getStaticField(DefaultFeelFunctionFactory.class, "date1"), function.getJavaType(), true)
                                    .inLineFun((classManager, args) -> {
                                        // date can auto cast to date and time
                                        JavaExpr arg = args[0];
                                        if (arg.original().feelType() instanceof FDate) {
                                            return arg.original().expr();
                                        }
                                        return new MethodCallExpr(arg.expr(), "toLocalDate");
                                    });
                    case 2 ->
                            JavaExpr.of(node, manager.getStaticField(DefaultFeelFunctionFactory.class, "date2"), function.getJavaType(), true)
                                    .inLineFun((classManager, args) -> classManager.getStaticMethod(LocalDate.class, "of").addArgument(args[0].expr()).addArgument(args[1].expr()).addArgument(args[2].expr()));
                    default -> throw new IllegalStateException("Unexpected value: " + index);
                };
                //TODO all functions
            }
            case sum -> {
                int index = functions.getFunctions().indexOf(function);
                yield switch (index) {
                    case 0 ->
                            JavaExpr.of(node, manager.getStaticField(DefaultFeelFunctionFactory.class, "sum0"), function.getJavaType(), true)
                                    .inLineFun((classManager, args) -> classManager.getStaticMethod(DefaultFeelFunctionFactory.class, "sum").addArgument(args[0].expr()));
                    case 1 ->
                            JavaExpr.of(node, manager.getStaticField(DefaultFeelFunctionFactory.class, "sum1"), function.getJavaType(), true)
                                    .inLineFun((classManager, args) -> {
                                        MethodCallExpr sum = classManager.getStaticMethod(DefaultFeelFunctionFactory.class, "sum");
                                        for (JavaExpr arg : args) {
                                            sum.addArgument(arg.expr());
                                        }
                                        return sum;
                                    });
                    default -> throw new IllegalStateException("Unexpected value: " + index);
                };
            }
            default -> null;
        };
    }

    public static final Map<String, Set<FFunction>> functions;

    static {
        FeelFunctions[] values = FeelFunctions.values();
        Map<String, Set<FFunction>> map = MapUtil.newLinkedHashMap(values.length);
        for (FeelFunctions value : values) {
            map.put(value.getName(), Collections.unmodifiableSet(new LinkedHashSet<>(value.getFunctions())));
        }
        functions = Collections.unmodifiableMap(map);
    }

    public static final FeelFunction<LocalDate> date0 = new FeelFunction.Default<>(FeelFunctions.date.getFunction(0),
            args -> ParserUtil.parseDate((String) args[0]));

    public static final FeelFunction<LocalDate> date1 = new FeelFunction.Default<>(FeelFunctions.date.getFunction(1),
            args -> args[0] instanceof LocalDate date ? date : args[0] instanceof LocalDateTime dateTime ? dateTime.toLocalDate() : null);

    public static final FeelFunction<LocalDate> date2 = new FeelFunction.Default<>(FeelFunctions.date.getFunction(2),
            args -> LocalDate.of((Integer) args[0], (Integer) args[1], (Integer) args[2]));

    @SuppressWarnings("unchecked")
    public static final FeelFunction<Number> sum0 = new FeelFunction.Default<>(FeelFunctions.sum.getFunction(0),
            args -> sum((Collection<? extends Number>) args[0]));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final FeelFunction<Number> sum1 = new FeelFunction.Default<>(FeelFunctions.sum.getFunction(0),
            args -> sum((List) Arrays.asList(args)));

    public static Number sum(Number... numbers) {
        return sum(Arrays.asList(numbers));
    }

    public static Number sum(Collection<? extends Number> numbers) {
        BigDecimal bigDecimalSum = BigDecimal.ZERO;
        double doubleSum = 0.0;
        long longSum = 0;
        boolean hasLong = false;

        for (Number number : numbers) {
            if (number instanceof BigDecimal bigDecimal) {
                bigDecimalSum = bigDecimalSum.add(bigDecimal);
            } else if (number instanceof Double || number instanceof Float) {
                doubleSum += number.doubleValue();
            } else if (number instanceof Integer || number instanceof Long || number instanceof Short || number instanceof Byte) {
                hasLong |= number instanceof Long;
                longSum += number.longValue();
            } else {
                doubleSum += number.doubleValue();
            }
        }

        if (bigDecimalSum.compareTo(BigDecimal.ZERO) != 0) {
            if (doubleSum != 0.0) {
                bigDecimalSum = bigDecimalSum.add(BigDecimal.valueOf(doubleSum));
            }
            if (longSum != 0) {
                bigDecimalSum = bigDecimalSum.add(BigDecimal.valueOf(longSum));
            }
            return bigDecimalSum;
        }
        if (doubleSum != 0.0) {
            return doubleSum + longSum;
        }
        return (hasLong || longSum > Integer.MAX_VALUE) ? longSum : (int) longSum;
    }
}