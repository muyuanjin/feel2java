package com.muyuanjin.feel.lang.ast;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.FAny;
import com.muyuanjin.feel.lang.type.FBoolean;
import com.muyuanjin.feel.lang.type.FNull;
import com.muyuanjin.feel.lang.type.FString;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import com.muyuanjin.feel.parser.antlr4.FEELParser.AddOrSubContext;
import com.muyuanjin.feel.parser.antlr4.FEELParser.CompareContext;
import com.muyuanjin.feel.parser.antlr4.FEELParser.ExponentiationContext;
import com.muyuanjin.feel.parser.antlr4.FEELParser.MultiOrDivContext;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.TokenStream;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
@NoArgsConstructor
public class InfixOpNode extends BaseASTNode {
    public ASTNode left;
    public Op op;
    public ASTNode right;

    public InfixOpNode(ExponentiationContext ctx, TokenStream input, ASTNode left, ASTNode right) {
        super(ctx, input);
        this.left = left;
        this.right = right;
        this.op = Op.POW;
    }

    public InfixOpNode(MultiOrDivContext ctx, TokenStream input, ASTNode left, ASTNode right) {
        super(ctx, input);
        this.left = left;
        this.right = right;
        this.op = ctx.op.getType() == FEELParser.MUL ? Op.MUL : Op.DIV;
    }

    public InfixOpNode(AddOrSubContext ctx, TokenStream input, ASTNode left, ASTNode right) {
        super(ctx, input);
        this.left = left;
        this.right = right;
        this.op = ctx.op.getType() == FEELParser.ADD ? Op.ADD : Op.SUB;
    }

    public InfixOpNode(CompareContext ctx, TokenStream input, ASTNode left, ASTNode right) {
        super(ctx, input);
        this.left = left;
        this.right = right;
        this.op = switch (ctx.op.getType()) {
            case FEELParser.LT -> Op.LT;
            case FEELParser.LE -> Op.LE;
            case FEELParser.GT -> Op.GT;
            case FEELParser.GE -> Op.GE;
            case FEELParser.EQUAL -> Op.EQ;
            case FEELParser.NOTEQUAL -> Op.NE;
            default -> throw new IllegalStateException("Unexpected value: " + ctx.op.getType());
        };
    }

    public InfixOpNode(FEELParser.JunctionContext ctx, TokenStream input, ASTNode left, ASTNode right) {
        super(ctx, input);
        this.left = left;
        this.right = right;
        this.op = ctx.op.getType() == FEELParser.AND ? Op.AND : Op.OR;
    }

    @Override
    public <T> T accept(@NotNull Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public @NotNull List<ASTNode> getChildren() {
        return List.of(left, right);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "left=" + left + ", " +
               "op=" + op + ", " +
               "right=" + right +
               "}";
    }

    public enum Op {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        POW("**"),

        LE("<="),
        LT("<"),
        GT(">"),
        GE(">="),

        EQ("="),
        NE("!="),
        AND("and"),
        OR("or");

        public final String symbol;

        public static final Map<String, Op> SYMBOLS;

        static {
            Map<String, Op> map = MapUtil.newHashMap(Op.values().length);
            for (Op op : Op.values()) {
                map.put(op.symbol, op);
            }
            SYMBOLS = Collections.unmodifiableMap(map);
        }

        Op(String symbol) {
            this.symbol = symbol;
        }

        public BinaryExpr.Operator toBinary() {
            return switch (this) {
                case ADD -> BinaryExpr.Operator.PLUS;
                case SUB -> BinaryExpr.Operator.MINUS;
                case MUL -> BinaryExpr.Operator.MULTIPLY;
                case DIV -> BinaryExpr.Operator.DIVIDE;
                case LE -> BinaryExpr.Operator.LESS_EQUALS;
                case LT -> BinaryExpr.Operator.LESS;
                case GT -> BinaryExpr.Operator.GREATER;
                case GE -> BinaryExpr.Operator.GREATER_EQUALS;
                case EQ -> BinaryExpr.Operator.EQUALS;
                case NE -> BinaryExpr.Operator.NOT_EQUALS;
                case AND -> BinaryExpr.Operator.AND;
                case OR -> BinaryExpr.Operator.OR;
                case POW -> null;
            };
        }

        public static Op from(String symbol) {
            Op op = SYMBOLS.get(symbol);
            if (op != null) {
                return op;
            }
            throw new IllegalArgumentException("No operator found for symbol '" + symbol + "'");
        }

        public static Op from(int tokenType) {
            return switch (tokenType) {
                case FEELParser.ADD -> ADD;
                case FEELParser.SUB -> SUB;
                case FEELParser.MUL -> MUL;
                case FEELParser.DIV -> DIV;
                case FEELParser.POW -> POW;
                case FEELParser.LE -> LE;
                case FEELParser.LT -> LT;
                case FEELParser.GT -> GT;
                case FEELParser.GE -> GE;
                case FEELParser.EQUAL -> EQ;
                case FEELParser.NOTEQUAL -> NE;
                case FEELParser.AND -> AND;
                case FEELParser.OR -> OR;
                default -> throw new IllegalArgumentException("No operator found for token type '" + tokenType + "'");
            };
        }

        public boolean isCalc() {
            return this == ADD || this == SUB || this == MUL || this == DIV || this == POW;
        }

        public boolean isBoolean() {
            return this == LE || this == LT || this == GT || this == GE || this == EQ || this == NE || this == AND || this == OR;
        }

        public boolean isCompare() {
            return this == LE || this == LT || this == GT || this == GE || this == EQ || this == NE;
        }

        public boolean isAndOr() {
            return this == AND || this == OR;
        }

        public boolean isEqNe() {
            return this == EQ || this == NE;
        }

        public boolean supportDiffType() {
            return isAndOr() || isEqNe();
        }

        public FType calculate(FType left, FType right) {
            if (left instanceof FAny && right instanceof FAny) {
                return FNull.NULL;
            }
            if (this == ADD && (left instanceof FString || right instanceof FString)) {
                return FString.STRING;
            }
            if (!FType.isSameType(left, right) && !supportDiffType()) {
                return FNull.NULL;
            }
            if (isBoolean()) {
                return FBoolean.BOOLEAN;
            }
            int lCanBe = left.canBe(right);
            if (lCanBe == FType.EQ) {
                return left;
            }
            int rCanBe = right.canBe(left);
            return lCanBe > rCanBe ? left : right;
        }
    }
}
