package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.common.entity.Pair;
import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.ast.InfixOpNode;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.ParserUtil;
import com.muyuanjin.feel.parser.antlr4.FEELLexer;
import com.muyuanjin.feel.parser.antlr4.FEELParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

import static com.muyuanjin.feel.parser.antlr4.FEELParser.*;

/**
 * 用于辅助 antlr4 解析上下文相关的语法
 *
 * @author muyuanjin
 */
@Slf4j
@Getter
@Setter
public final class ParserHelper {
    /**
     * 符号表，上下文相关的语法必须通过符号表解析
     */
    private final SymbolTable symbols;
    /**
     * 用于存储函数调用（可能嵌套所以是栈）的参数<P>
     * 1.用于重载函数解析<P>
     * 2.用于函数调用的参数类型推断，比如 sort([1..2],function (x,y) x>y) x,y 需要推断为 Number
     */
    private final Deque<Pair<FType, Integer>> functionCalls;
    /**
     * 命名符号的解析模式，由于g4中无法区分变量模式和函数调用模式，所以只用来区分 类型声明 和 非类型声明 模式
     */
    private Class<?> currentMode;

    /**
     * 对于 xx.yy 解析子属性时，用于记录父属性，g4解析部分负责推栈
     */
    private FType pathParentType;


    /**
     * 是否检查过， 用于区分 <检查过没匹配> 和 <没检查过> 的变量标志
     */
    private boolean hasMatched;
    /**
     * 已经字典树匹配过的token索引，小于该索引的 token 视为匹配成功的
     */
    private int matchStartTokenIndex;
    /**
     * 匹配序列上的最后一个Token索引，小于该索引的 token 视为匹配成功的
     */
    private int matchTargetTokenIndex;
    /**
     * 在多token字典树匹配符号时，记录当前匹配到的符号类型
     */
    private FType matchTargetType;
    /**
     * 用于保存nameRef匹配过程中匹配到的 FunctionSymbol 或者 VariableSymbol, 延迟到后续的FunInvoke单独处理
     */
    private Symbol.Node matchTargetNode;


    /**
     * 用于记录已经缓存过text的Token索引（{@link CommonToken#getText()}总是 new String，对于后续解析不利），Token索引总是递增的
     */
    private int cachedTokenIndex;

    public ParserHelper() {
        this.symbols = new SymbolTable();
        this.functionCalls = new ArrayDeque<>();
    }

    public void start() {
        symbols.start();
        currentMode = VariableSymbol.class;
        pathParentType = null;
        hasMatched = false;
        matchStartTokenIndex = -1;
        matchTargetTokenIndex = -1;
        matchTargetType = null;
        matchTargetNode = null;
        cachedTokenIndex = -1;
    }

    public void close() {
        symbols.close();
        functionCalls.clear();
    }

    public void defineGlobalVar(String name, FType type) {
        symbols.defineGlobalSymbol(new VariableSymbol(name, type));
    }

    public void defineGlobalVar(String name, Object value) {
        symbols.defineGlobalSymbol(new VariableSymbol(name, FType.of(value)));
    }

    public void defineGlobalVar(Object root) {
        symbols.defineGlobalSymbol(new VariableSymbol("?", FType.of(root)));
    }

    /**
     * @see FEELParser
     */
    public void typeMode() {
        currentMode = TypeSymbol.class;
    }

    /**
     * @see FEELParser
     */
    public void defaultMode() {
        currentMode = VariableSymbol.class;
    }

    /**
     * @see FEELParser
     */
    public void pushScope(ParserRuleContext context) {
        String name = getScopeName(context);
        symbols.pushScope(name);

        //更符合设计模式的话其实这里应该给Filter 创建一个独立的Scope实现类，然后在类初始化中定义变量，但是因为别的scope都不需要这个步骤，所以省略了
        if (context instanceof FilterExpressionContext ctx) {

            //隐式转换, 将单个元素转换为列表
            FType type = FType.getElementType(ctx.target.type);
            symbols.getCurrentScope().define(new VariableSymbol("item", type, Collections.singletonList("item")));
            if (type instanceof FContext contextType) {
                Map<String, FType> members = contextType.getMembers();
                if (!members.isEmpty()) {
                    for (var entry : members.entrySet()) {
                        symbols.getCurrentScope().define(new VariableSymbol(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
    }

    /**
     * @see FEELParser
     */
    public void popScope() {
        symbols.popScope();
    }

    public void defineVariable(String name, FType type, List<String> tokens) {
        symbols.getCurrentScope().define(new VariableSymbol(name, type, tokens));
    }

    /**
     * @see FEELParser
     */
    public void startRef(ExpressionContext context) {
        pathParentType = context.type;
    }

    /**
     * @see FEELParser
     */

    public void endRef() {
        pathParentType = null;
    }

    /**
     * @see FEELParser
     */
    public void startVar(TokenStream input) {
        // 只在start 之后第一次 followUpVar 查找计算，后续都使用缓存直接判断（后续的token都是不完整的，如果第一次计算失败，后续都失败）
        resetVar();
        hasMatched = false;
        matchStartTokenIndex = cacheText(input.LT(1)).getTokenIndex();
        matchTargetNode = null;
    }

    /**
     * @see FEELParser
     */
    public void endVar(ParserRuleContext context, TokenStream input) {
        Token stop = input.LT(-1);
        Token start = input.get(matchStartTokenIndex);
        var varStartIndex = start.getStartIndex();
        var varStopIndex = stop.getStopIndex();
        String text = input.getTokenSource().getInputStream().getText(Interval.of(varStartIndex, varStopIndex));
        List<String> tokens = new ArrayList<>(stop.getTokenIndex() - stop.getTokenIndex() + 1);
        for (int i = matchStartTokenIndex; i <= stop.getTokenIndex(); i++) {
            tokens.add(input.get(i).getText());
        }
        if (context instanceof NameRefContext ctx) {
            ctx.name = text;
            if (!hasMatched) {
                appendSingleton(ctx.name);
            }
            ctx.type = matchTargetType;
            ctx.type = ctx.type == null
                    ? FAny.ANY
                    : ctx.type;

            ctx.tokens = tokens;
        } else if (context instanceof NameDefContext ctx) {
            ctx.name = text;
            ctx.tokens = tokens;
        } else {
            throw new UnsupportedOperationException("Unsupported context: " + context.getClass());
        }
        resetVar();
        hasMatched = false;
        matchStartTokenIndex = -1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isInMember() {
        return pathParentType != null;
    }

    private void resetVar() {
        matchTargetTokenIndex = -1;
    }

    /**
     * @see FEELParser
     */
    public boolean canAppendVar(ParserRuleContext localCtx, TokenStream input) {
        boolean isPredict = localCtx == null;
        Token token = input.LT(1);
        if (isPredict && matchStartTokenIndex == -1) {
            //预测时可能不会运行 starVar 和 endVar
            startVar(input);
        } else {
            token = cacheText(input.LT(1));
        }
        if (!hasMatched) {
            hasMatched = true;
            // 首次开始匹配
            if (!isInMember()) {
                // 在当先scope寻找匹配的符号，即后续的token可以组合匹配到当前域的符号
                Symbol symbol = longestMatchScope(input);
                matchTargetType = getTypeFromSymbol(symbol);
            } else {
                matchTargetType = longestMatchMember(input);
                matchTargetNode = null;// member 不存在二义性，不需要记录歧义
            }
        }
        return token.getTokenIndex() <= matchTargetTokenIndex;
    }

    /**
     * 如果没有经过 canAppendVar 就 endVar 说明是单个 NAME token,是最简单的变量名引用,但是 当前input index 是 -1 而不是 LT(1)
     *
     * @param nameRefName name
     */
    private void appendSingleton(String nameRefName) {
        if (nameRefName == null) {
            matchTargetType = null;
            resetVar();
            return;
        }
        if (!isInMember()) {
            if (symbols.getCurrentScope() == null) {
                matchTargetType = null;
                resetVar();
                return;
            }
            matchTargetNode = symbols.getCurrentScope().resolve(nameRefName);
            if (matchTargetNode == null) {
                matchTargetType = null;
                resetVar();
                return;
            }
            Symbol symbol = null;
            if (currentMode == TypeSymbol.class) {
                if (matchTargetNode.getTypeSymbol() != null) {
                    // 只有type 模式下返回type
                    symbol = matchTargetNode.getTypeSymbol();
                } else {
                    matchTargetType = null;
                    resetVar();
                    return;
                }
            } else {
                if (matchTargetNode.getVarSymbol() != null) {
                    // 非type 模式下，可能是寻找变量，也可能是寻找函数
                    // 由于g4直接左递归限制，无法在函数调用前切换到函数模式，所以只能让变量和函数模式共存
                    // 优先返回变量，函数调用通过在后续的 FunInvoke 单独处理 ，通过 targetNode 传递可能的 Fun
                    symbol = matchTargetNode.getVarSymbol();
                } else if (matchTargetNode.getFunSymbol() != null) {
                    symbol = matchTargetNode.getFunSymbol();
                }
            }
            matchTargetType = getTypeFromSymbol(symbol);
        } else {
            Map<String, FType> members = pathParentType.getMembers();
            if (members.isEmpty() || (matchTargetType = members.get(nameRefName)) == null) {
                resetVar();
                matchTargetType = null;
                return;
            }
            matchTargetNode = null;
        }
    }

    private FType getTypeFromSymbol(Symbol symbol) {
        if (symbol instanceof FunctionSymbol functionSymbol) {
            if (functionSymbol.getFunctions().size() == 1) {
                return functionSymbol.getFunctions().get(0);
            } else {
                return new CandidateFun(functionSymbol.getFunctions());
            }
        } else if (symbol != null && symbol.getType() != null) {
            return symbol.getType();
        }
        return null;
    }

    private static final Comparator<Symbol> SYMBOL_COMPARATOR = Comparator.<Symbol>comparingInt(e -> e.getName().length()).reversed();

    private Symbol longestMatchScope(TokenStream input) {
        if (symbols.getCurrentScope() == null || input == null) {
            resetVar();
            return null;
        }
        Iterator<String> tokens = new Iterator<>() {
            private Token token;
            private int index = matchStartTokenIndex;

            @Override
            public boolean hasNext() {
                if (index >= input.size()) {
                    // BufferedTokenStream input 还没有填充到 index ，触发填充
                    input.LT(index - input.LT(1).getTokenIndex() + 1);
                }
                token = input.get(index++);
                return token.getType() != FEELLexer.EOF;
            }

            @Override
            public String next() {
                return cacheText(token).getText();
            }
        };

        // 不忽略任何通道, 这是不合FEEL规范的,但是变量名就应该不乱穿插空白
        Pair<Symbol.Node, Integer> pair = symbols.getCurrentScope().resolvePrefix(tokens);
        Symbol.Node prefix = pair == null ? null : pair.getKey();
        String name;
        if (prefix != null) {
            matchTargetTokenIndex = pair.getValue() + matchStartTokenIndex;
            if (currentMode == TypeSymbol.class) {
                if (prefix.getTypeSymbol() != null) {
                    // 只有type 模式下返回type
                    return prefix.getTypeSymbol();
                } else {
                    resetVar();
                    return null;
                }
            }
            matchTargetNode = prefix;
            if (prefix.getVarSymbol() != null) {
                // 非type 模式下，可能是寻找变量，也可能是寻找函数
                // 由于g4直接左递归限制，无法在函数调用前切换到函数模式，所以只能让变量和函数模式共存
                // 优先返回变量，函数调用通过在后续的 FunInvoke 单独处理 ，通过 targetNode 传递可能的 Fun
                return prefix.getVarSymbol();
            } else if (prefix.getFunSymbol() != null) {
                return prefix.getFunSymbol();
            }
        }
        resetVar();
        return null;
    }

    private FType longestMatchMember(TokenStream input) {
        if (pathParentType == null || input == null) {
            resetVar();
            return null;
        }
        Map<String, FType> members = pathParentType.getMembers();
        if (members.isEmpty()) {
            resetVar();
            return null;
        }
        members = MapUtil.newMapSortedBy(members, Comparator.<Map.Entry<String, FType>>comparingInt(e -> e.getKey().length()).reversed());
        int maxLen = 0;
        for (String s : members.keySet()) {
            maxLen = Math.max(maxLen, s.length());
        }
        CharStream stream = input.getTokenSource().getInputStream();
        Token startToken = input.get(matchStartTokenIndex);
        int startCharIndex = startToken.getStartIndex();
        int maxNeedIndex = Math.min(stream.size() - 1, startCharIndex + maxLen - 1);
        if (input.get(input.size() - 1).getStopIndex() < maxNeedIndex) {
            throw new UnsupportedOperationException("Please use BufferedTokenStream and call fill()");
        }

        Map<Integer, Integer> indexMap = new HashMap<>();
        Token token = startToken;
        int index = token.getTokenIndex();
        while (token.getStopIndex() <= maxNeedIndex && token.getType() != FEELLexer.EOF) {
            indexMap.put(token.getStopIndex(), token.getTokenIndex());
            token = input.get(++index);
        }

        // 不忽略任何通道, 这是不合FEEL规范的,但是变量名就应该不乱穿插空白
        String text = stream.getText(Interval.of(startCharIndex, maxNeedIndex));
        Integer tokenIndex;
        for (var entry : members.entrySet()) {
            String name = entry.getKey();
            if (text.startsWith(name) &&
                //并且匹配到的变量名没有被TOKEN分隔,即变量名不能跨TOKEN匹配, a+bbb 不能匹配为 a+bb 和 b
                (tokenIndex = indexMap.get(startCharIndex + name.length() - 1)) != null) {
                matchTargetTokenIndex = tokenIndex;
                return entry.getValue();
            }
        }
        resetVar();
        return null;
    }

    /**
     * @see FEELParser
     */
    public FType pushFun(ExpressionContext context) {
        if (context instanceof FunctionInvocationContext ctx) {
            FType type = ctx.expression().type;
            if (matchTargetNode != null && matchTargetNode.getFunSymbol() != null && !(type instanceof FFunction)) {
                type = getTypeFromSymbol(matchTargetNode.getFunSymbol());
            }
            functionCalls.push(new Pair<>(type, 0));
            if (type instanceof FFunction function) {
                return function.getReturnType();
            }
            return type;
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType popFun(ExpressionContext context) {
        if (context instanceof FunctionInvocationContext fnCall) {
            ExpressionContext expression = fnCall.expression();
            if (expression.type instanceof CandidateFun candidateFun) {
                expression.type = candidateFun.getMostSpecific();
            }
            var pair = functionCalls.pollFirst();
            FType type;
            if (pair != null && (type = pair.getKey()) != null) {
                if (type instanceof CandidateFun function) {
                    if (!function.isOnlyOneWinner()) {
                        log.trace("Ambiguous function invocation: {}", function);
                        return invalidType();
                    }
                    return function.getReturnType();
                }
                if (type instanceof FFunction function) {
                    return function.getReturnType();
                }
                return type;
            }
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public void addParam(String name, FType type) {
        var pair = functionCalls.peek();
        if (pair != null) {
            pair.setValue(pair.getValue() + 1);
            if (pair.getKey() instanceof CandidateFun candidateFun) {
                candidateFun.addParam(name, type);
            }
        }
    }

    /**
     * @see FEELParser
     */
    public void addParam(FType type) {
        var pair = functionCalls.peek();
        if (pair != null) {
            pair.setValue(pair.getValue() + 1);
            if (pair.getKey() instanceof CandidateFun candidateFun) {
                candidateFun.addParam(type);
            }
        }
    }

    /**
     * 猜测函数参数类型
     *
     * @see FEELParser
     */
    public FType guessType(int paramIndex) {
        var pair = functionCalls.peek();
        if (pair != null) {
            FType funType = null;
            if (pair.getKey() instanceof FFunction function) {
                List<FType> parameterTypes = function.getParameterTypes();
                funType = parameterTypes.get(pair.getValue());
            }
            if (funType instanceof FFunction function) {
                List<FType> parameterTypes = function.getParameterTypes();
                if (paramIndex < parameterTypes.size()) {
                    return parameterTypes.get(paramIndex);
                }
            }
        }
        return FAny.ANY;
    }

    /**
     * @see FEELParser
     */
    public FType funType(ExpressionContext context) {
        if (context instanceof FunctionDefinitionContext ctx) {
            var nameDefContexts = ctx.nameDef();
            List<FType> types = new ArrayList<>(nameDefContexts.size());
            List<String> names = new ArrayList<>(nameDefContexts.size());
            for (var name : nameDefContexts) {
                names.add(name.name);
                types.add(name.type);
            }
            return FFunction.of(ctx.body.type, names, types);
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType rangeType(ExpressionContext context) {
        if (context instanceof UnboundedIntervalContext ctx) {
            return switch (ctx.bound.getType()) {
                case FEELLexer.LT -> FRange.of(ctx.expression.type, null, Boolean.FALSE);
                case FEELLexer.LE -> FRange.of(ctx.expression.type, null, Boolean.TRUE);
                case FEELLexer.GT -> FRange.of(ctx.expression.type, Boolean.FALSE, null);
                case FEELLexer.GE -> FRange.of(ctx.expression.type, Boolean.TRUE, null);
                default -> invalidType();
            };
        } else if (context instanceof BoundedIntervalContext ctx) {
            Boolean start = ctx.leftBound.getType() == FEELLexer.LBRACK ? Boolean.TRUE : Boolean.FALSE;
            Boolean end = ctx.rightBound.getType() == FEELLexer.RBRACK ? Boolean.TRUE : Boolean.FALSE;
            return FRange.of(ctx.left.type, ctx.right.type, start, end);
        }
        return invalidType();
    }

    /**
     * 10.3.2.5 Lists and filters
     *
     * @see FEELParser
     */
    public FType filterType(ExpressionContext context) {
        if (context instanceof FilterExpressionContext ctx) {
            FType targetType = ctx.target.type;
            FType filterType = ctx.filter.type;
            if (filterType instanceof FBoolean) {
                return FList.of(FType.getElementType(targetType));
            } else if (filterType instanceof FNumber) {
                return FType.getElementType(targetType);
            } else {
                return FNull.NULL;
            }
        }
        return invalidType();
    }

    /**
     * TODO Node 57: Specific semantics of addition and subtraction
     *  根据语法规定，加减法的类型转换规则如下：
     *
     * @see FEELParser
     */
    public FType arithmeticType(ExpressionContext context) {
        if (context instanceof ExponentiationContext ctx) {
            return InfixOpNode.Op.POW.calculate(ctx.left.type, ctx.right.type);
        } else if (context instanceof MultiOrDivContext ctx) {
            return InfixOpNode.Op.from(ctx.op.getType()).calculate(ctx.left.type, ctx.right.type);
        } else if (context instanceof AddOrSubContext ctx) {
            return InfixOpNode.Op.from(ctx.op.getType()).calculate(ctx.left.type, ctx.right.type);
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType junctionType(ExpressionContext context) {
        if (context instanceof JunctionContext ctx) {
            return ctx.left.type.minSuper(ctx.right.type);
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType ifType(ExpressionContext context) {
        if (context instanceof IfExpressionContext ctx) {
            return ctx.then.type.minSuper(ctx.otherwise.type);
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType literalType(ParserRuleContext context) {
        if (context instanceof IntegerLiteralContext) {
            return FNumber.INTEGER;
        } else if (context instanceof FloatLiteralContext literalContext) {
            String text = literalContext.getText();
            BigDecimal bigDecimal = ParserUtil.parserJavaNumber(text);
            literalContext.value = bigDecimal;
            if (ParserUtil.canExactlyBeDouble(bigDecimal)) {
                return FNumber.DOUBLE;
            }
            return FNumber.BIG_DECIMAL;
        } else if (context instanceof BooleanLiteralContext) {
            return FBoolean.BOOLEAN;
        } else if (context instanceof StringLiteralContext) {
            return FString.STRING;
        } else if (context instanceof DateTimeLiteralContext literalContext) {
            return ParserUtil.determineAtType(literalContext.StringLiteral().getText());
        } else if (context instanceof NullLiteralContext) {
            return FNull.NULL;
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType feelType(ParserRuleContext context) {
        if (context instanceof QnTypeContext qnTypeContext) {
            var nameRefContexts = qnTypeContext.nameRef();
            FType type = null;
            for (int i = 0; i < nameRefContexts.size(); i++) {
                NameRefContext nameRefContext = nameRefContexts.get(i);
                if (i == 0) {
                    Pair<Symbol.Node, Integer> pair = symbols.getCurrentScope().resolvePrefix(nameRefContext.tokens.iterator());
                    Symbol.Node resolve = pair == null ? null : pair.getKey();
                    if (resolve == null || resolve.getTypeSymbol() == null) {
                        return invalidType();
                    }
                    type = resolve.getTypeSymbol().getType();
                } else {
                    if (type == null) {
                        return invalidType();
                    }
                    Map<String, FType> members = type.getMembers();
                    if (members.isEmpty()) {
                        return FNull.NULL;
                    }
                    type = members.getOrDefault(nameRefContext.name, FNull.NULL);
                }
            }
            if (type == null) {
                type = invalidType();
            }
            return type;
        } else if (context instanceof ListTypeContext listTypeContext) {
            return FList.of(listTypeContext.feelType().type);
        } else if (context instanceof RangeTypeContext rangeTypeContext) {
            Boolean start = rangeTypeContext.LBRACK() != null ? Boolean.TRUE :
                    rangeTypeContext.LPAREN() != null ? Boolean.FALSE : null;
            Boolean end = rangeTypeContext.RBRACK() != null ? Boolean.TRUE :
                    rangeTypeContext.RPAREN() != null ? Boolean.FALSE : null;
            return FRange.of(rangeTypeContext.feelType().type, start, end);
        } else if (context instanceof ContextTypeContext contextTypeContext) {
            int size = contextTypeContext.nameDef().size();
            Map<String, FType> members = MapUtil.newLinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                members.put(contextTypeContext.nameDef(i).getText(), contextTypeContext.feelType(i).type);
            }
            return FContext.of(members);
        } else if (context instanceof FunctionTypeContext functionTypeContext) {
            var feelTypes = functionTypeContext.feelType();
            List<FType> types = new ArrayList<>(feelTypes.size());
            for (FeelTypeContext feelType : feelTypes) {
                types.add(feelType.type);
            }
            return FFunction.of(types.get(types.size() - 1), types.subList(0, feelTypes.size() - 1));
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType contextType(ParserRuleContext context) {
        if (context instanceof ContextContext ctx) {
            int size = ctx.key().size();
            Map<String, FType> members = MapUtil.newLinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                members.put(ctx.key(i).name, ctx.expression(i).type);
            }
            return FContext.of(members);
        } else if (context instanceof NamedParametersContext ctx) {
            int size = ctx.nameDef().size();
            Map<String, FType> members = MapUtil.newLinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                members.put(ctx.nameDef(i).name, ctx.expression(i).type);
            }
            return FContext.of(members);
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType listType(ParserRuleContext context) {
        if (context instanceof ListContext ctx) {
            FType[] types = new FType[ctx.expression().size()];
            for (int i = 0; i < ctx.expression().size(); i++) {
                types[i] = ctx.expression(i).type;
            }
            return FList.of(FType.getMinSuperType(types));
        } else if (context instanceof PositionalParametersContext ctx) {
            FType[] types = new FType[ctx.expression().size()];
            for (int i = 0; i < ctx.expression().size(); i++) {
                types[i] = ctx.expression(i).type;
            }
            return FList.of(FType.getMinSuperType(types));
        } else if (context instanceof PositiveUnaryTestsContext ctx) {
            FType[] types = new FType[ctx.positiveUnaryTest().size()];
            for (int i = 0; i < ctx.positiveUnaryTest().size(); i++) {
                types[i] = ctx.positiveUnaryTest(i).type;
            }
            return FList.of(FType.getMinSuperType(types));
        }
        return invalidType();
    }

    /**
     * @see FEELParser
     */
    public FType elementType(ExpressionContext left, @Nullable ExpressionContext right) {
        if (right == null) {
            if (left.type instanceof FList list) {
                return list.getElementType();
            } else if (left.type instanceof FRange range) {
                return range.getElementType();
            }
            return left.type;
        }
        return left.type.minSuper(right.type);
    }

    private FType invalidType() {
        //TODO 添加错误计数，存储发生错误的源代码位置
        throw new UnsupportedOperationException("invalidType");
//        return SymbolTable.INVALID;
    }

    @NotNull
    private static String getScopeName(ParserRuleContext context) {
        String name;
        if (context instanceof QuantifiedExpressionContext) {
            name = "<quantify>";
        } else if (context instanceof ForExpressionContext) {
            name = "<for>";
        } else if (context instanceof ContextContext) {
            name = "<context>";
        } else if (context instanceof FunctionDefinitionContext) {
            name = "<function>";
        } else if (context instanceof FilterExpressionContext) {
            name = "<filter>";
        } else {
            name = "<local>";
        }
        return name;
    }

    private static final String[] _LITERAL_CONSTANTS = IntStream.range(0, FEELLexer.VOCABULARY.getMaxTokenType())
            .mapToObj(FEELLexer.VOCABULARY::getLiteralName)
            .map(e -> e == null ? null : e.substring(1, e.length() - 1))
            .toArray(String[]::new);

    private Token cacheText(Token token) {
        if (token instanceof WritableToken writableToken && token.getTokenIndex() > cachedTokenIndex) {
            cachedTokenIndex = token.getTokenIndex();
            int tokenType = token.getType();
            if (tokenType >= 0 && tokenType < _LITERAL_CONSTANTS.length) {
                String constant = _LITERAL_CONSTANTS[tokenType];
                if (constant != null) {
                    writableToken.setText(constant);
                    return token;
                }
            }
            writableToken.setText(token.getText());
        }
        return token;
    }
}
