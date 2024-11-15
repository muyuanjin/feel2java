package com.muyuanjin.feel.translate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.muyuanjin.feel.FeelTypeFactory;
import com.muyuanjin.feel.impl.DefaultFeelTypeFactory;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.ast.NegationNode;
import com.muyuanjin.feel.lang.ast.NumberNode;
import com.muyuanjin.feel.lang.type.FNumber;
import com.muyuanjin.feel.parser.ParserUtil;
import lombok.experimental.UtilityClass;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author muyuanjin
 */
@UtilityClass
public class CodeGens {
    private static final Cache<java.lang.reflect.Type, Type> TYPE_CACHE = Caffeine.newBuilder().weakKeys()
            .expireAfterAccess(Duration.ofHours(2)).build();

    public static FieldDeclaration staticField(Type type, String name, Expression initializer, String... comments) {
        FieldDeclaration fieldDeclaration = new FieldDeclaration(
                NodeList.nodeList(Modifier.privateModifier(), Modifier.staticModifier(), Modifier.finalModifier()),
                new VariableDeclarator(type, name, initializer));
        setComment(fieldDeclaration, comments);
        return fieldDeclaration;
    }

    public static <N extends Node> N setComment(N node, String... comments) {
        if (comments == null || comments.length == 0) {
            return node;
        }
        if (comments.length == 1) {
            String comment = comments[0];
            if (comment != null && !(comment = comment.strip()).isBlank()) {
                String[] array = comment.split("\\R");
                if (array.length == 1) {
                    node.setLineComment(array[0]);
                } else {
                    node.setBlockComment(comment);
                }
            }
            return node;
        }

        StringBuilder builder = new StringBuilder();
        for (String comment : comments) {
            if (comment == null || (comment = comment.strip()).isBlank()) {
                continue;
            }
            for (String str : comment.split("\\R")) {
                builder.append(str).append("\n");
            }
        }
        if (builder.isEmpty()) {
            return node;
        }
        builder.setLength(builder.length() - 1);
        node.setBlockComment(builder.toString());
        return node;
    }

    public static StringLiteralExpr stringLiteral(String value) {
        return new StringLiteralExpr(null, ParserUtil.escapeJava(value, false));
    }

    public static EnclosedExpr castTo(Type type, Expression expr) {
        return new EnclosedExpr(new CastExpr(type, new EnclosedExpr(expr)));
    }

    public static @NotNull String getBoxedName(Class<?> clazz) {
        String name;
        if (clazz == int.class) {
            name = "Integer";
        } else if (clazz == long.class) {
            name = "Long";
        } else if (clazz == short.class) {
            name = "Short";
        } else if (clazz == byte.class) {
            name = "Byte";
        } else if (clazz == float.class) {
            name = "Float";
        } else if (clazz == double.class) {
            name = "Double";
        } else if (clazz == boolean.class) {
            name = "Boolean";
        } else if (clazz == char.class) {
            name = "Character";
        } else if (clazz == void.class) {
            name = "Void";
        } else {
            name = clazz.getSimpleName();
        }
        return name;
    }

    @NotNull
    public static Type parse(java.lang.reflect.Type type) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        Type result = TYPE_CACHE.getIfPresent(type);
        if (result != null) {
            return result;
        }
        result = parse0(type);
        TYPE_CACHE.put(type, result);
        return result.clone();
    }

    private static Type parse0(java.lang.reflect.Type type) {
        if (type instanceof Class<?> clazz) {
            if (clazz.isPrimitive()) {
                return new PrimitiveType(PrimitiveType.Primitive.byTypeName(clazz.getName()).orElseThrow());
            }
            if (clazz.isArray()) {
                return new ArrayType(parse(clazz.getComponentType()));
            }
            ClassOrInterfaceType t = null;
            for (String s : clazz.getCanonicalName().split("\\.")) {
                t = new ClassOrInterfaceType(t, s);
            }
            return Objects.requireNonNull(t);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            var arguments = parameterizedType.getActualTypeArguments();
            List<Type> list = new ArrayList<>(arguments.length);
            for (var _type : arguments) {
                list.add(parse(_type));
            }
            NodeList<Type> typeArguments = NodeList.nodeList(list);
            if (parse(parameterizedType.getRawType()) instanceof ClassOrInterfaceType clazzType) {
                return new ClassOrInterfaceType(clazzType.getScope().orElse(null), clazzType.getName(), typeArguments);
            }
            return new ClassOrInterfaceType(null, new SimpleName(parameterizedType.getRawType().getTypeName()), typeArguments);
        }
        if (type instanceof GenericArrayType genericArrayType) {
            return new ArrayType(parse(genericArrayType.getGenericComponentType()));
        }
        return StaticJavaParser.parseType(type.getTypeName());
    }

    /**
     * 把 1 或 -1开头的改为0开头的索引值, 如1,-1 ->0 ,2->1,-2->-1
     */
    public static Expression toZeroStartIndex(JavaExpr javaExpr, Context context) {
        if (!(javaExpr.feelType() instanceof FNumber number)) {
            return null;
        }
        BigDecimal value = null;
        if (javaExpr.node() instanceof NegationNode negationNode && negationNode.value instanceof NumberNode node) {
            value = node.getValue().negate();
        }
        if (javaExpr.node() instanceof NumberNode node) {
            value = node.getValue();
        }
        if (value != null) {
            if (value.signum() == 0) {
                return null;
            }
            if (number == FNumber.INTEGER) {
                return new IntegerLiteralExpr(Integer.toString(value.signum() > 0 ? value.intValue() - 1 : value.intValue() + 1));
            } else if (number == FNumber.LONG) {
                return new LongLiteralExpr((value.signum() > 0 ? value.longValue() - 1 : value.longValue() + 1) + "L");
            } else if (number == FNumber.DOUBLE) {
                return new DoubleLiteralExpr((value.signum() > 0 ? value.doubleValue() - 1 : value.doubleValue() + 1) + "D");
            }
        }
        Expression expression = javaExpr.expr();
        FeelTypeFactory typeFactory = FeelTypeFactory.instance(context);
        ConditionalExpr conditionalExpr = new ConditionalExpr();
        JavaExpr zero = JavaExpr.of(javaExpr.node(), new IntegerLiteralExpr("0"), int.class, true);
        JavaExpr one = JavaExpr.of(javaExpr.node(), new IntegerLiteralExpr("1"), int.class, true);
        JavaExpr con = typeFactory.infixOp(javaExpr.node(), javaExpr, zero, InfixOpNode.Op.GE, context);
        if (con == null) {
            return null;
        }
        conditionalExpr.setCondition(con.expr());
        JavaExpr sub1 = typeFactory.infixOp(javaExpr.node(), javaExpr, one, InfixOpNode.Op.SUB, context);
        if (sub1 == null) {
            return null;
        }
        conditionalExpr.setThenExpr(sub1.expr());
        JavaExpr add1 = typeFactory.infixOp(javaExpr.node(), javaExpr, one, InfixOpNode.Op.ADD, context);
        if (add1 == null) {
            return null;
        }
        conditionalExpr.setElseExpr(add1.expr());
        return new EnclosedExpr(conditionalExpr);
    }

    public static Expression negate(JavaExpr javaExpr, Context context) {
        if (!(javaExpr.feelType() instanceof FNumber number)) {
            return null;
        }
        Expression expression = javaExpr.expr();
        ClassManager manager = ClassManager.instance(context);
        NumberNode numberNode = null;
        if (javaExpr.node() instanceof NegationNode negationNode && negationNode.value instanceof NumberNode node) {
            numberNode = node;
        }
        if (javaExpr.node() instanceof NumberNode node) {
            numberNode = node;
        }
        if (numberNode != null) {
            BigDecimal value = numberNode.getValue();
            if (number == FNumber.INTEGER) {
                return new IntegerLiteralExpr(Integer.toString(-value.intValue()));
            } else if (number == FNumber.LONG) {
                return new LongLiteralExpr(-value.longValue() + "L");
            } else if (number == FNumber.DOUBLE) {
                return new DoubleLiteralExpr(-value.doubleValue() + "D");
            }
        }
        if (expression instanceof UnaryExpr unaryExpr) {
            if (unaryExpr.getOperator() == UnaryExpr.Operator.MINUS) {
                return unaryExpr.getExpression();
            }
        }
        //TODO npe
        return switch (number) {
            case INTEGER, LONG, DOUBLE -> new UnaryExpr(expression, UnaryExpr.Operator.MINUS);
            case BIG_DECIMAL -> new MethodCallExpr(expression, "negate");
            case NUMBER -> new MethodCallExpr(manager.getStaticMethod(DefaultFeelTypeFactory.class, "toBigDecimal")
                    .addArgument(expression), "negate");
        };
    }

    public static Expression ifOneNullAllNull(@Nullable JavaExpr left, @Nullable JavaExpr right, Expression old, boolean enclose) {
        if ((left == null || !left.nullable()) && (right == null || !right.nullable())) {
            return old;
        }
        // if one null ,result is null
        Expression nullCondition = null;
        Expression leftExpr = null;
        if (left != null && left.nullable()) {
            leftExpr = new BinaryExpr(left.expr(), new NullLiteralExpr(), BinaryExpr.Operator.EQUALS);
            nullCondition = leftExpr;
        }

        if (right != null && right.nullable()) {
            Expression rightExpr = new BinaryExpr(right.expr(), new NullLiteralExpr(), BinaryExpr.Operator.EQUALS);
            if (nullCondition == null) {
                nullCondition = rightExpr;
            } else {
                nullCondition = new BinaryExpr(leftExpr, rightExpr, BinaryExpr.Operator.OR);
            }
        }
        ConditionalExpr conditionalExpr = new ConditionalExpr();
        conditionalExpr.setCondition(Objects.requireNonNull(nullCondition));
        conditionalExpr.setThenExpr(new NullLiteralExpr());
        conditionalExpr.setElseExpr(old);
        return enclose ? new EnclosedExpr(conditionalExpr) : conditionalExpr;
    }

    private static final Set<Class<? extends Expression>> SIMPLE_EXPR_CLASSES;

    static {
        SIMPLE_EXPR_CLASSES = Set.of(
                NameExpr.class,
                ArrayAccessExpr.class,
                ClassExpr.class,
                SuperExpr.class,
                ThisExpr.class,
                TypeExpr.class
        );
    }

    public static boolean isSimple(Expression expression) {
        if (SIMPLE_EXPR_CLASSES.contains(expression.getClass())) {
            return true;
        }
        if (expression instanceof LiteralExpr) {
            return true;
        }
        if (expression instanceof CastExpr castExpr) {
            return isSimple(castExpr.getExpression());
        }
        if (expression instanceof EnclosedExpr enclosedExpr) {
            return isSimple(enclosedExpr.getInner());
        }
        if (expression instanceof ConditionalExpr conditionalExpr) {
            return isSimple(conditionalExpr.getCondition()) &&
                   isSimple(conditionalExpr.getThenExpr()) &&
                   isSimple(conditionalExpr.getElseExpr());
        }
        if (expression instanceof FieldAccessExpr fieldAccessExpr) {
            return isSimple(fieldAccessExpr.getScope());
        }
        if (expression instanceof InstanceOfExpr instanceOfExpr) {
            return isSimple(instanceOfExpr.getExpression());
        }
        if (expression instanceof UnaryExpr unaryExpr) {
            return isSimple(unaryExpr.getExpression());
        }
        if (expression instanceof BinaryExpr binaryExpr) {
            return isSimple(binaryExpr.getLeft()) && isSimple(binaryExpr.getRight());
        }
        if (expression instanceof MethodReferenceExpr methodReferenceExpr) {
            return isSimple(methodReferenceExpr.getScope());
        }
        return false;
    }
}
