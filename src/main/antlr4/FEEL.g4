grammar FEEL;
@parser::header {
import com.muyuanjin.feel.lang.*;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.parser.*;
import com.muyuanjin.feel.parser.symtab.*;
}
@lexer::header {
import com.muyuanjin.feel.parser.*;
}
@parser::members {
private ParserHelper helper;

public FEELParser(TokenStream input, ParserHelper helper){
    this(input);
    this.helper = helper;
}
{
	if (this.helper == null) this.helper = new ParserHelper();
}

public void setHelper(ParserHelper helper) {
    this.helper = helper;
}

public ParserHelper getHelper() {
    return helper;
}
public void define(String name, FType type, List<String> tokens){
    helper.defineVariable(name, type, tokens);
}
}

/** 角色表的每一个单元格即 unaryTests */
unaryTests returns[FType type=FBoolean.BOOLEAN]
@init{helper.start();}
    : 'not' '(' positiveUnaryTests ')' EOF
    | positiveUnaryTests EOF
    | '-' EOF
    ;
finally {helper.close();}

positiveUnaryTests returns[FType type]
    : positiveUnaryTest (',' positiveUnaryTest)* {$type=helper.listType($ctx);}
    ;

positiveUnaryTest returns[FType type]
    : expression {$type=$expression.type;}
    ;

expressionUnit returns[FType type]
@init{helper.start();}
    : expression EOF {$type=$expression.type;}
    ;
finally {helper.close();}

// 解析规则
expression returns [FType type]
    :'(' expression ')'{$type=$expression.type;}                                                                                              # parens //textualExpression
    | nameRef{$type=$nameRef.type;}                                                                                                           # nameExpression //textualExpression
    | bound=('<' | '<=' | '>' | '>=') expression {$type=helper.rangeType($ctx);}                                                              # unboundedInterval //textualExpression simplePositiveUnaryTest
    | leftBound=('(' | ']' | '[') left=expression '..' right=expression rightBound=(')' | '[' |  ']'){$type=helper.rangeType($ctx);}          # boundedInterval //textualExpression simplePositiveUnaryTest
    | simpleLiteral{$type=$simpleLiteral.type;}                                                                                               # literal //textualExpression

    | expression {$type=helper.pushFun($ctx);}'(' (namedParameters | positionalParameters) ')'{$type=helper.popFun($ctx);}                    # functionInvocation //textualExpression

    | target=expression {helper.pushScope($ctx);}'[' filter=expression ']' {helper.popScope();$type=helper.filterType($ctx);}                 # filterExpression //textualExpression

    | left=expression {helper.startRef($left.ctx);} '.' right=nameRef{helper.endRef();$type=$right.type;}                                     # pathExpression //textualExpression

    | expression 'instance' 'of' feelType{$type=FTypes.BOOLEAN;}                                                                              # instanceOf //textualExpression

    | '-' value=expression {$type=$expression.type;}                                                                                          # arithmeticNegation //textualExpression arithmeticExpression
    | <assoc=right> left=expression '**' right=expression {$type=helper.arithmeticType($ctx);}                                                # exponentiation //textualExpression arithmeticExpression
    | left=expression op=('*'| '/') right=expression {$type=helper.arithmeticType($ctx);}                                                     # multiOrDiv //textualExpression arithmeticExpression
    | left=expression op=('+'|'-') right=expression {$type=helper.arithmeticType($ctx);}                                                      # addOrSub //textualExpression arithmeticExpression

    | value=expression 'in' '(' target=positiveUnaryTests ')'{$type=FTypes.BOOLEAN;}                                                          # inList //textualExpression comparison
    | value=expression 'in' target=positiveUnaryTest {$type=FTypes.BOOLEAN;}                                                                  # inSingle //textualExpression comparison
    | value=expression 'between' left=expression 'and' right=expression  {$type=FTypes.BOOLEAN;}                                              # between //textualExpression comparison
    | left=expression op=('=' | '!=' | '<' | '<=' | '>' | '>=') right=expression   {$type=FTypes.BOOLEAN;}                                    # compare //textualExpression comparison

    | left=expression op=('and' | 'or') right=expression{$type=helper.junctionType($ctx);}                                                    # junction //textualExpression conjunction | disjunction

    |{helper.pushScope($ctx);}
    'for' it=iterationContexts 'return' result=expression {$type=FList.of($result.type);}
    {helper.popScope();}                                                                                                                      # forExpression //textualExpression

    |{helper.pushScope($ctx);}
    mo=('some' | 'every') it=iterationContexts 'satisfies' judge=expression {$type=FTypes.BOOLEAN;}
    {helper.popScope();}                                                                                                                      # quantifiedExpression //textualExpression

    | 'if' condition=expression 'then' then=expression 'else' otherwise=expression {$type=helper.ifType($ctx);}                               # ifExpression //textualExpression

    |{helper.pushScope($ctx);}
    '{' (key ':' expression {define($key.name,$expression.type,$key.tokens);} (',' key ':' expression {define($key.name,$expression.type,$key.tokens);})*)? '}'
    {$type=helper.contextType($ctx);}
    {helper.popScope();}                                                                                                                      # context //boxedExpression

    |{helper.pushScope($ctx);int param=0;}
    'function' '(' (nameDef {$nameDef.type=helper.guessType(param++);} (':' feelType {$nameDef.type=$feelType.type;})? {define($nameDef.name,$nameDef.type,$nameDef.tokens);}
    (',' nameDef {$nameDef.type=helper.guessType(param++);}(':' feelType {$nameDef.type=$feelType.type;})? {define($nameDef.name,$nameDef.type,$nameDef.tokens);})*)? ')'
    ('external')? body=expression
    {$type=helper.funType($ctx);}
    {helper.popScope();}                                                                                                                      # functionDefinition //boxedExpression

    | '[' (expression (',' expression)*)? ']' {$type=helper.listType($ctx);}                                                                  # list //boxedExpression
    ;

iterationContexts
    : iterationContext ( ',' iterationContext )*
    ;

iterationContext
    : nameDef 'in' left=expression ('..' right=expression)? {define($nameDef.name,helper.elementType($left.ctx,$right.ctx),$nameDef.tokens);}
    ;


key returns[String name,List<String> tokens]
    : nameDef {$name = $nameDef.name;$tokens = $nameDef.tokens;} #keyName
    | st=StringLiteral {$name = $st.text;} #keyString
    ;

namedParameters
    :  nameDef ':' expression {helper.addParam($nameDef.name,$expression.type);}
        (',' nameDef ':' expression{helper.addParam($nameDef.name,$expression.type);})*
    ;

positionalParameters
    : (expression {helper.addParam($expression.type);}(',' expression{helper.addParam($expression.type);})*)?
    ;

// #33 - #39
// TODO 也许可以合并一些单token分支,以优化性能 https://tomassetti.me/improving-the-performance-of-an-antlr-parser/
simpleLiteral returns [FType type,Object value]
@after{$type = helper.literalType($ctx);}
    :	IntegerLiteral          #integerLiteral
    |	FloatingPointLiteral    #floatLiteral
    |	BooleanLiteral          #booleanLiteral
    |   AT StringLiteral        #dateTimeLiteral
    |	StringLiteral           #stringLiteral
    |	NULL                    #nullLiteral
    ;

feelType returns [FType type]
@init{helper.typeMode();}
@after{$type = helper.feelType($ctx);}
    :  nameRef ('.' nameRef)*                                                           # qnType
    | 'list' '<' feelType '>'                                                           # listType
    | 'range' ('['']'| '('')'| '['')'| '('']' | '['| '('| ')'| ']')? '<' feelType '>'   # rangeType
    | 'context' '<' nameDef ':' feelType (',' nameDef ':' feelType)* '>'                # contextType
    | 'function' '<' (feelType (',' feelType)*)? '>' '->' feelType                      # functionType
    ;
finally {helper.defaultMode();}

/**
* 与许多传统表达式语言不同，友好够用表达式语言（FEEL）支持将空格和一些特殊字符作为变量名和函数名的一部分
*/
nameRef returns[FType type, String name, List<String> tokens]
@init{helper.startVar(_input);}
@after{helper.endVar(_localctx,_input);}
: nameRefStarToken (nameRefPartToken)*;

nameRefStarToken
    : NAME
    | {helper.canAppendVar(_localctx,_input)}? keywords
    ;

nameRefPartToken
    :{helper.canAppendVar(_localctx,_input)}? (NAME | IntegerLiteral | FloatingPointLiteral | additional_name_symbols | keywords)
    ;

nameDef returns[FType type, String name, List<String> tokens]
@init{helper.startVar(_input);}
@after{helper.endVar(_localctx,_input);}
    :(NAME | keywords) (NAME |IntegerLiteral | FloatingPointLiteral | additional_name_symbols | keywords)*
;

keywords: FOR | RETURN | IF | THEN | ELSE | SOME | EVERY | SATISFIES | INSTANCE | OF | FUNCTION | EXTERNAL | OR | AND | BETWEEN | NOT | NULL | TRUE | FALSE | IN | CONTEXT | LIST;
additional_name_symbols: '.' | '/' | '-' | '\'' | '+' | '*';

/********************************
 *      LEXER RULES
 *
 * Include:
 *      - number literals
 *      - boolean literals
 *      - string literals
 *      - null literal
 ********************************/
BooleanLiteral:   TRUE | FALSE;
// Number Literals
// #37 整数
IntegerLiteral
	:	DecimalIntegerLiteral //十进制整数
	|	HexIntegerLiteral //十六进制整数
	;
    fragment DecimalIntegerLiteral:     DecimalNumeral IntegerTypeSuffix?;
    fragment HexIntegerLiteral:     HexNumeral IntegerTypeSuffix?;
    fragment IntegerTypeSuffix:	    [lL];
    fragment DecimalNumeral:	Digit (Digits? | Underscores Digits);
    fragment Digits:	Digit (DigitsAndUnderscores? Digit)?;
    fragment Digit:	[0-9];
    fragment DigitsAndUnderscores:	DigitOrUnderscore+;
    fragment DigitOrUnderscore:	Digit|	'_';
    fragment Underscores:	'_'+;
    fragment HexNumeral:	'0' [xX] HexDigits;
    fragment HexDigits:	HexDigit (HexDigitsAndUnderscores? HexDigit)?;
    fragment HexDigit:	[0-9a-fA-F];
    fragment HexDigitsAndUnderscores:	HexDigitOrUnderscore+;
    fragment HexDigitOrUnderscore:	HexDigit|	'_';

// #37 浮点数
FloatingPointLiteral
	:	DecimalFloatingPointLiteral //十进制浮点数
	|	HexadecimalFloatingPointLiteral //十六进制浮点数
	;
    fragment DecimalFloatingPointLiteral
    	:	Digits '.' Digits ExponentPart? FloatTypeSuffix?
    	|	'.' Digits ExponentPart? FloatTypeSuffix?
    	|	Digits ExponentPart FloatTypeSuffix?
    	|	Digits FloatTypeSuffix
    	;
    fragment ExponentPart:	ExponentIndicator SignedInteger;
    fragment ExponentIndicator:	[eE];
    fragment SignedInteger:	Sign? Digits;
    fragment Sign:	[+-];
    fragment FloatTypeSuffix:	[fFdD];
    fragment HexadecimalFloatingPointLiteral:	HexSignificand BinaryExponent FloatTypeSuffix?;
    fragment HexSignificand
    	:	HexNumeral '.'?
    	|	'0' [xX] HexDigits? '.' HexDigits
    	;
    fragment BinaryExponent :   BinaryExponentIndicator SignedInteger ;
    fragment BinaryExponentIndicator :	[pP];

// String Literals 字符串字面量
StringLiteral:	'"' StringCharacter* '"'{setText(ParserUtil.translateEscapes(getText()));};
    fragment StringCharacter
    	:	~["\\]
    	|	EscapeSequence
    	;
    // 字符和字符串字面量的转义序列 ( g4里 双斜杠表示单斜杠)
    fragment EscapeSequence
    	:	'\\' ~[u]     // 需要支持 FEEL regexps 如 \d{3}
        |   UnicodeEscape // 这并不在规范中，但可以避免对输入进行预处理
    	;
    // 这并不在规范中，但可以避免对输入进行预处理
    fragment UnicodeEscape
        :   '\\' 'U' HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit
        |   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
        ;

// The Null Literal (在关键字NULL定义)

/********************************
 *      KEYWORDS
 ********************************/
// 关键词
FOR:'for'           ; RETURN:'return'     ; IF:'if'               ; THEN:'then'         ; ELSE:'else'       ;
SOME:'some'         ; EVERY:'every'       ; SATISFIES:'satisfies' ; INSTANCE:'instance' ; OF:'of'           ;
FUNCTION:'function' ; EXTERNAL:'external' ; OR:'or'               ; AND:'and'           ; BETWEEN:'between' ;
NULL:'null'         ; TRUE:'true'         ; FALSE: 'false'        ; IN:'in'             ; QUOTE:'\''        ;
CONTEXT:'context'   ; LIST:'list'         ; RANGE:'range'         ;
// 分隔符
LPAREN : '(' ; RPAREN : ')' ; LBRACE : '{' ; RBRACE : '}' ; LBRACK : '[' ; RBRACK : ']' ; COMMA : ',' ; ELIPSIS : '..' ; DOT : '.' ;

// 操作符
EQUAL : '=' ; GT : '>'   ; LT : '<'  ; LE : '<='   ; GE : '>='     ; NOTEQUAL : '!=' ;
POW : '**'  ; ADD : '+'  ; SUB : '-' ; MUL : '*'   ; DIV : '/'     ;
BANG: '!'   ; NOT: 'not' ; AT  : '@' ; COLON : ':' ; RARROW : '->' ;

NAME: NAME_START NAME_PART* {setText(getText());};

fragment NAME_PART: NAME_PART_CHAR+;

fragment NAME_START
    : NAME_START_CHAR (NAME_PART_CHAR)*
    ;

fragment NAME_START_CHAR
    : '?'               // 问号
    | [A-Z]             // 英文大写字母
    | '_'               // 下划线
    | [a-z]             // 英文小写字母
    | [\u00C0-\u00D6]   // 拉丁字母扩展-A（部分）
    | [\u00D8-\u00F6]   // 拉丁字母扩展-A（部分）和拉丁字母扩展-B（部分）
    | [\u00F8-\u02FF]   // 拉丁字母扩展-B的剩余部分和IPA扩展
    | [\u0370-\u037D]   // 希腊字母扩展
    | [\u037F-\u1FFF]   // 更多的希腊字母和其他语言字符，包括但不限于西里尔字母、亚美尼亚语、希伯来语等
    | [\u200C-\u200D]   // 零宽度非连接符和零宽度连接符
    | [\u2070-\u218F]   // 上标和下标、货币符号、组合用符号等
    | [\u2C00-\u2FEF]   // 格拉哥里字母、哥特字母等
    | [\u3001-\uD7FF]   // 中日韩统一表意符号 符号和标点、平假名、片假名、杂项符号、象形文字等
    | [\uF900-\uFDCF]   // 中日韩统一表意符号 兼容象形文字
    | [\uFDF0-\uFFFD]   // 阿拉伯演示形式
    | [\u{10000}-\u{EFFFF}] // 补充的私人使用区域、其他补充字符
    ;

fragment NAME_PART_CHAR
    : NAME_START_CHAR
    | [0-9]             // 数字
    | '\u00B7'          // 中点
    | [\u0300-\u036F]   // 组合附加符号
    | [\u203F-\u2040]   // 下划线、字符连接符
    ;

// 不符合以上词法的输入会优先进入该无效词法(不能channel(HIDDEN)), 用于防止出现 afor 解析为 a for
// 使用此方法避免显式插入 WS+ 要求 for 前面有什么(未必是空格,也可能是数字或字符等不需要空格也能分隔的字符)
IDENTIFIER : [a-zA-Z]+;

//
// 忽略字符, 不是skip, 通过不同通道在词法或语法阶段读取,用于判断变量名是否完全匹配
//
// #61 空白
WS: [\u0009\u0020\u0085\u00A0\u1680\u180E\u2000-\u200B\u2028\u2029\u202F\u205F\u3000\uFEFF]+ -> channel(HIDDEN);
// #62 换行
VS:[\u000A-\u000D]+ -> channel(HIDDEN);
// 注释
COMMENT: '/*' .*? '*/' -> channel(2);
// 单行注释
LINE_COMMENT: '//' ~[\r\n]* -> channel(2);
// 其他字符
ANY_OTHER_CHAR: ~[ \t\r\n\u000c]-> channel(HIDDEN);

