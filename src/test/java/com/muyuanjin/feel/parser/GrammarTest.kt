package com.muyuanjin.feel.parser

import com.muyuanjin.feel.lang.FType
import com.muyuanjin.feel.lang.FTypes.*
import com.muyuanjin.feel.lang.FeelFunctions
import com.muyuanjin.feel.lang.type.*
import com.muyuanjin.feel.parser.antlr4.FEELLexer
import com.muyuanjin.feel.parser.antlr4.FEELParser
import com.muyuanjin.feel.parser.symtab.ParserHelper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.DecisionState
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * @author muyuanjin
 */
internal class GrammarTest : StringSpec({
    "一般测试" {
        p("\"a\\u4e2d\\u6587b\"") {
            text shouldBe "a中文b<EOF>"
            print(); type shouldBe STRING
        }
        p("@\"-PT5H\"") {
            text shouldBe "@-PT5H<EOF>"
            print(); type shouldBe DAY_TIME_DURATION
        }
        p("@\"P2Y2M\"") {
            Arb.string()
            text shouldBe "@P2Y2M<EOF>"
            print(); type shouldBe YEAR_MONTH_DURATION
        }
        p("x instance of any") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of null") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of date") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of time") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of number") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of string") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of boolean") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of list<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of date and time") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of day and time duration") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of list<list<string>>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of year and month duration") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of context<age:number,name:string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of list<list<context<age:number,name:string>>>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of context<age:number,name:string,other:context<a:any,b:null>>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range[]<any>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range[)<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range(]<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range()<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range(<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range)<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range]<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of range[<string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of function<number,number>->number") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of function<number>->number") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of function<number>->context<age:number,name:string>") {
            print(); type shouldBe BOOLEAN
        }
        p("x instance of function<number>->context<age:number,name:string,other:function<number>->context<age:number,name:string>>") {
            print(); type shouldBe BOOLEAN
        }
        p("0 between 1 and 2") {
            print(); type shouldBe BOOLEAN
        }
        p("{get:122,result:321}") {
            print(); type shouldBe FContext.of("get" to INTEGER, "result" to INTEGER)
        }
        p("{get:122,result:321}.result") {
            print(); type shouldBe INTEGER
        }
        p("{get:122,result:\"321\"}.result") {
            print(); type shouldBe STRING
        }
        p("{result:date(1,2,3)}.result") {
            print(); type shouldBe DATE
        }
        p("{result:date and time(\"123\")}.result") {
            print(); type shouldBe DATE_TIME
        }
        p("function (a:number,b:function<number,number>->boolean) b(a,a+1)") {
            print()
            type shouldBe FFunction.of(BOOLEAN, "a", NUMBER, "b", FFunction.of(BOOLEAN, NUMBER, NUMBER))
        }
        p("{get:function (a:number,b:function<number,number>->boolean) b(a,a+1),result:get(12,function (x,y) x+1=y )}.result") {
            print()
            type shouldBe BOOLEAN
        }
        p("1") {
            print(); type shouldBe INTEGER
        }
        p("(1)") {
            print(); type shouldBe INTEGER
        }
        p("2.1") {
            print(); type shouldBe BIG_DECIMAL
        }
        p("(2.1)") {
            print(); type shouldBe BIG_DECIMAL
        }
        p("x", "x" to FContext.of("1" to NUMBER, "4" to NUMBER)) {
            print(); type shouldBe FContext.of("1" to NUMBER, "4" to NUMBER)
        }
        p("x._1", "x" to FContext.of("_1" to FList.of(STRING), "4" to NUMBER)) {
            print(); type shouldBe FList.of(STRING)
        }
        p("x._1[1]", "x" to FContext.of("_1" to FList.of(STRING), "4" to NUMBER)) {
            print(); type shouldBe STRING
        }
        p("<2") {
            print(); type shouldBe FRange.of(INTEGER, null, false)
        }
        p("<=2") { print();type shouldBe FRange.of(INTEGER, null, true) }
        p(">@\"-PT5H\"") { print();type shouldBe FRange.of(DAY_TIME_DURATION, false, null) }
        p(">=2") { print();type shouldBe FRange.of(INTEGER, true, null) }
        p("[1..2]") {
            print();type shouldBe FRange.of(INTEGER, true, true)
        }
        p("[1..2.3)") {
            print();type shouldBe FRange.of(NUMBER, true, false)
        }
        p("(1..2]") {
            print();type shouldBe FRange.of(INTEGER, false, true)
        }
        p("({result:date and time(\"123\")}.result..date(1,2,3))") {
            print();type shouldBe FRange.of(DATE, false, false)
        }
        "true" p {
            print(); type shouldBe BOOLEAN
        }
        "false" p {
            print(); type shouldBe BOOLEAN
        }
        "null" p {
            print(); type shouldBe NULL
        }
        "@\"P2Y2M\"" p {
            print(); type shouldBe YEAR_MONTH_DURATION
        }
        "\"1\"" p {
            print(); type shouldBe STRING
        }
        "1l" p {
            print(); type shouldBe INTEGER
        }
        "1L" p {
            print(); type shouldBe INTEGER
        }
        "0x1" p {
            print(); type shouldBe INTEGER
        }
        "0X1" p {
            print(); type shouldBe INTEGER
        }
        "1.2f" p {
            print(); type shouldBe BIG_DECIMAL
        }
        "1.2F" p {
            print(); type shouldBe BIG_DECIMAL
        }
        ".2d" p {
            print(); type shouldBe BIG_DECIMAL
        }
        ".2D" p {
            print(); type shouldBe BIG_DECIMAL
        }
    }
    "一元测试" {
        ut("-") {
            print(); type shouldBe BOOLEAN
        }
        ut("not(1)") {
            print()
            type shouldBe BOOLEAN;this.getChild(2).getChild(0).type shouldBe INTEGER
        }
        ut("not(1.2)") {
            print()
            type shouldBe BOOLEAN;this.getChild(2).getChild(0).type shouldBe BIG_DECIMAL
        }
        ut("not(\"1.2\")") {
            print()
            type shouldBe BOOLEAN;this.getChild(2).getChild(0).type shouldBe STRING
        }
    }

    "全局特殊变量名" {
        p("a+++b+b+c", "a++" to INTEGER, "b" to INTEGER, "c" to DOUBLE) {
            print(); type shouldBe DOUBLE
        }
        p("a+++b+b+c", "a" to DOUBLE, "a+++b+b" to INTEGER, "c" to INTEGER) {
            print(); type shouldBe INTEGER
        }
        p("{\"abb+c\":2,abb:3,c:4.2,d:abb+c}.d") {
            print(); type shouldBe INTEGER
        }
        p("{\"abb+c\":2,abb:3,c:4.2,d:abb + c}.d") {
            print(); type shouldBe BIG_DECIMAL
        }
        p(
            "a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+aaa+bbb+c",
            "a" to INTEGER,
            "aaa" to INTEGER,
            "a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a+a" to DOUBLE,
            "bbb" to INTEGER,
            "c" to INTEGER
        ) {
            print(); type shouldBe INTEGER
        }
    }
    "复杂类型推导" {
        p("{复杂:{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}}") {
            print(); type shouldBe FContext.of(
            "复杂" to FContext.of(
                "a" to INTEGER, "b" to STRING,
                "abb" to INTEGER, "c" to BIG_DECIMAL, "d" to BIG_DECIMAL, "abb+c" to STRING
            )
        )
        }
        p("{复杂:{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}}.复杂", "c" to DOUBLE) {
            print(); type shouldBe FContext.of(
            "a" to INTEGER, "b" to STRING,
            "abb" to INTEGER, "c" to BIG_DECIMAL, "d" to BIG_DECIMAL, "abb+c" to STRING
        )
        }
        p(
            "{复杂:{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}}.复杂.abb +c",
            "c" to DOUBLE
        ) {
            print(); type shouldBe DOUBLE
        }
        p("{a:1,b:\"文字\\u4e2d\\u6587b\",abb:3,c:4.2,d:abb+c,abb+c:\"66\"}.abb+c", "c" to DOUBLE) {
            print(); type shouldBe STRING
        }
        p("{abb:3,c:4,d:abb+c,abb+c:文字}.d", "文字" to DATE) {
            print(); type shouldBe INTEGER
        }
        p("{abb+c:文字,abb:3,c:4,d:abb+c}.d", "文字" to DATE) {
            print(); type shouldBe DATE
        }
        p("{abb+c:文字,abb:3,c:4,d:abb +c}.d", "文字" to DATE) {
            print(); type shouldBe INTEGER
        }
        p("{getName:function (person:context<name:string,age:number>) person.name,person:{name:\"小明\",age:12},result:getName(person)}.result") {
            print(); type shouldBe STRING
        }
        p("{get:function (a:number,b:function<number,number>->boolean) b(a,a+1),result:get(12,function (x,y) x+1=y )}.result") {
            print(); type shouldBe BOOLEAN
        }
    }
    "重载函数推导" {
        p("sort(1,2,3)") {
            print(); type shouldBe FList.of(INTEGER)
        }
        p("sort(1,2,3.2)") {
            print(); type shouldBe FList.of(BIG_DECIMAL)
        }
        p("sort(\"1\",\"2\",\"3.2\")") {
            print(); type shouldBe FList.of(STRING)
        }
        p("sort(a,function(x,y) if x instance of null then true else false)", "a" to FList.of(FAny.ANY)) {
            print(); type shouldBe FList.of(FAny.ANY)
        }
        p("sort(a,function(x,y) if count(x)>7 then true else false)", "a" to FList.of(FList.of(STRING))) {
            print(); type shouldBe FList.of(FList.of(STRING))
            this.getChild(0).getChild(2).getChild(2).type shouldBe FFunction.of(
                BOOLEAN,
                "x" to FList.of(STRING),
                "y" to FList.of(STRING)
            )
        }
        p("sort([1,2,3,4],function(x,y) x>y)") {
            print(); type shouldBe FList.of(INTEGER)
            this.getChild(0).getChild(2).getChild(2).type shouldBe FFunction.of(BOOLEAN, "x" to INTEGER, "y" to INTEGER)
        }

        p("abs(1)") {
            print(); type shouldBe NUMBER
            this.getChild(0).getChild(0).type shouldBe FeelFunctions.abs.functions[0]
        }
        p("abs(@\"PT5H\")") {
            print(); type shouldBe DAY_TIME_DURATION
            this.getChild(0).getChild(0).type shouldBe FeelFunctions.abs.functions[1]
        }
        p("abs(@\"P2Y2M\")") {
            print(); type shouldBe YEAR_MONTH_DURATION
            this.getChild(0).getChild(0).type shouldBe FeelFunctions.abs.functions[2]
        }
    }
    "同名变量与同名函数" {
        p("{abs:-2,b:abs(abs)}.b") {
            print(); type shouldBe NUMBER
        }
        p("{count:\"文字\",b:count([1,2,3])}.b") {
            print(); type shouldBe INTEGER
        }
    }
    "注释" {
        p("a//注释\n+b") {
            print(); type shouldBe NULL
        }
        p("a//注释\n+b//注释") {
            print(); type shouldBe NULL
        }
        p("a//注释\n+b//注释\n") {
            print(); type shouldBe NULL
        }
        p("a//注释\n+b//注释\n//注释") {
            print(); type shouldBe NULL
        }
        p("a/*注释*/\n+b") {
            print(); type shouldBe NULL
        }
        p("a/*注释\n注释*/\n+b") {
            print(); type shouldBe NULL
        }
        p("a/*注释\n注释*/\n+b/*注释\n注释*/") {
            print(); type shouldBe NULL
        }
    }
})

private fun Any?.print() {
    System.err.println(this)
}

private fun ParseTree.print() {
    val walker = ParseTreeWalker()
    val listener = FeelSyntaxHighlighter(this.parser!!.tokenStream!!)
    walker.walk(listener, this)
    println(listener.translatedText)
}

private fun ParseTree.toTree() {
    this.toStringTree(this.parser)
}

// 使用一个映射来存储ParseTree到Parser的映射
private val parseTreeToParserMap = mutableMapOf<ParseTree, Parser>()

// 扩展属性
var ParseTree.parser: Parser?
    get() = parseTreeToParserMap[this]
    set(value) {
        if (value != null) {
            parseTreeToParserMap[this] = value
        } else {
            parseTreeToParserMap.remove(this)
        }
    }

val ParseTree.type: FType?
    get() {
        //switch in kotlin
        return when (this) {
            is FEELParser.UnaryTestsContext -> this.type
            is FEELParser.PositiveUnaryTestsContext -> this.type
            is FEELParser.PositiveUnaryTestContext -> this.type
            is FEELParser.ExpressionContext -> this.type
            is FEELParser.SimpleLiteralContext -> this.type
            is FEELParser.FeelTypeContext -> this.type
            is FEELParser.NameRefContext -> this.type
            is FEELParser.NameDefContext -> this.type
            is FEELParser.ExpressionUnitContext -> this.type

            else -> throw UnsupportedOperationException()
        }
    }

// 扩展函数，用于接受lambda表达式
infix fun String.p(configure: ParseTree.() -> Unit): ParseTree {
    // 这里简化了解析逻辑，直接创建一个ParseTree实例
    val parseTree = p(this, true)
    // 应用配置
    parseTree.configure()
    return parseTree
}

// 修改扩展函数以接受上下文参数和lambda表达式
infix fun String.p(inputTypes: Map<String, FType>): ParseTreeConfiguration {
    return ParseTreeConfiguration(p(this, true, *inputTypes.map { it.key to it.value }.toTypedArray()))
}

class ParseTreeConfiguration(private val parseTree: ParseTree) {
    // 这个函数接受lambda，并应用到ParseTree实例上
    infix fun configure(block: ParseTree.() -> Unit): ParseTree {
        parseTree.block()
        return parseTree
    }
}

fun ut(expression: String, vararg inputTypes: Pair<String, FType>): ParseTree {
    return p(expression, false, *inputTypes)
}

fun ut(expression: String, vararg inputTypes: Pair<String, FType>, configure: ParseTree.() -> Unit): ParseTree {
    // 解析逻辑，假设创建并返回了一个ParseTree实例
    val parseTree = p(expression, false, *inputTypes)
    // 在返回的ParseTree上执行配置的lambda表达式
    parseTree.configure()
    return parseTree
}

fun p(expression: String, vararg inputTypes: Pair<String, FType>): ParseTree {
    return p(expression, true, *inputTypes)
}

fun p(expression: String, vararg inputTypes: Pair<String, FType>, configure: ParseTree.() -> Unit): ParseTree {
    // 解析逻辑，假设创建并返回了一个ParseTree实例
    val parseTree = p(expression, true, *inputTypes)
    // 在返回的ParseTree上执行配置的lambda表达式
    parseTree.configure()
    return parseTree
}

private val HELPER = ParserHelper()

private fun p(expression: String, isExpr: Boolean = true, vararg inputTypes: Pair<String, FType>): ParseTree {
    val lexer = FEELLexer(CharStreams.fromString(expression))
    val parser = FEELParser(CommonTokenStream(lexer), HELPER)
    parser.setProfile(true)
    lexer.removeErrorListeners()
    lexer.addErrorListener(FeelThrowErrorListener.INSTANCE)
    for (context in inputTypes) {
        parser.helper.defineGlobalVar(context.first, context.second)
    }
    parser.interpreter.predictionMode = PredictionMode.SLL
    parser.removeErrorListeners()
    parser.errorHandler = BailErrorStrategy()
    var tree: ParseTree
    try {
        tree = if (isExpr) parser.expressionUnit() else parser.unaryTests()
    } catch (ex: ParseCancellationException) {
        lexer.reset()
        parser.reset()
        parser.addErrorListener(FeelThrowErrorListener.INSTANCE)
        parser.errorHandler = DefaultErrorStrategy()
        parser.interpreter.predictionMode = PredictionMode.LL
        tree = if (isExpr) parser.expressionUnit() else parser.unaryTests()
    }
    if (parser.numberOfSyntaxErrors > 0) {
        throw IllegalStateException("语法错误")
    }
    tree.parser = parser
    profileParser(parser)
    return tree
}

fun profileParser(parser: FEELParser) {
    print(String.format("%-" + 35 + "s", "rule"))
    print(String.format("%-" + 15 + "s", "time"))
    print(String.format("%-" + 15 + "s", "invocations"))
    print(String.format("%-" + 15 + "s", "lookahead"))
    print(String.format("%-" + 15 + "s", "lookahead(max)"))
    print(String.format("%-" + 15 + "s", "ambiguities"))
    println(String.format("%-" + 15 + "s", "errors"))
    for (decisionInfo in parser.getParseInfo().decisionInfo) {
        val ds: DecisionState = parser.atn.getDecisionState(decisionInfo.decision)
        val rule: String = parser.ruleNames[ds.ruleIndex]
        if (decisionInfo.timeInPrediction > 0) {
            print(String.format("%-" + 35 + "s", rule))
            print(String.format("%-" + 15 + "s", decisionInfo.timeInPrediction))
            print(String.format("%-" + 15 + "s", decisionInfo.invocations))
            print(String.format("%-" + 15 + "s", decisionInfo.SLL_TotalLook))
            print(String.format("%-" + 15 + "s", decisionInfo.SLL_MaxLook))
            print(String.format("%-" + 15 + "s", decisionInfo.ambiguities.size))
            println(String.format("%-" + 15 + "s", decisionInfo.errors.size))
        }
    }
}