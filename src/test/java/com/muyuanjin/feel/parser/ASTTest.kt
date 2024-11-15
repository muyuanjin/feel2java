package com.muyuanjin.feel.parser

import com.diogonunes.jcolor.Ansi
import com.diogonunes.jcolor.Attribute
import com.muyuanjin.feel.lang.FTypes
import com.muyuanjin.feel.lang.ast.ASTNode
import com.muyuanjin.feel.lang.type.FContext
import com.muyuanjin.feel.lang.type.FList
import io.kotest.core.spec.style.StringSpec
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * @author muyuanjin
 */
internal class ASTTest : StringSpec({
    "一般测试" {
        p("\"a\\u4e2d\\u6587b\"") { print(); node.print() }
        p("@\"-PT5H\"") { print(); node.print() }
        p("@\"P2Y2M\"") { print(); node.print() }
        p("x instance of null") { print(); node.print() }
        p("{get:122,result:321}") { print(); node.print() }
        p("{get:122,result:321}.result") { print(); node.print() }
        p("{get:122,result:\"321\"}.result") { print(); node.print() }
        p("{result:date(1,2,3)}.result") { print(); node.print() }
        p("{result:date and time(\"123\")}.result") { print(); node.print() }
        p("function (a:number,b:function<number,number>->boolean) b(a,a+1)") { print(); node.print() }
        p("{get:function (a:number,b:function<number,number>->boolean) b(a,a+1),result:get(12,function (x,y) x+1=y )}.result") { print(); node.print() }
        p("1") { print(); node.print() }
        p("(1)") { print(); node.print() }
        p("2.1") { print(); node.print() }
        p("(2.1)") { print(); node.print() }
        p("x", "x" to FContext.of("1" to FTypes.NUMBER, "4" to FTypes.NUMBER)) { print(); node.print() }
        p("x._1", "x" to FContext.of("_1" to FList.of(FTypes.STRING), "4" to FTypes.NUMBER)) { print(); node.print() }
        p(
            "x._1[1]",
            "x" to FContext.of("_1" to FList.of(FTypes.STRING), "4" to FTypes.NUMBER)
        ) { print(); node.print() }
        p("<2") { print(); node.print() }
        p("<=2") { print(); node.print() }
        p(">@\"-PT5H\"") { print(); node.print() }
        p(">=2") { print(); node.print() }
        p("[1..2]") { print(); node.print() }
        p("[1..2.3)") { print(); node.print() }
        p("(1..2]") { print(); node.print() }
        p("({result:date and time(\"123\")}.result..date(1,2,3))") { print(); node.print() }
        "true" p { print(); node.print() }
        "false" p { print(); node.print() }
        "null" p { print(); node.print() }
        "@\"P2Y2M\"" p { print(); node.print() }
        "\"1\"" p { print(); node.print() }
        "1l" p { print(); node.print() }
        "1L" p { print(); node.print() }
        "0x1" p { print(); node.print() }
        "0X1" p { print(); node.print() }
        "1.2f" p { print(); node.print() }
        "1.2F" p { print(); node.print() }
        ".2d" p { print(); node.print() }
        ".2D" p { print(); node.print() }
    }
})

private fun ParseTree.print() {
    val walker = ParseTreeWalker()
    val listener = FeelSyntaxHighlighter(this.parser!!.tokenStream!!)
    walker.walk(listener, this)
    println(listener.translatedText)
}

private fun Any?.print() {
    println(
        Ansi.generateCode(
            Attribute.YELLOW_BACK(),
            Attribute.BLACK_TEXT(),
            Attribute.BOLD()
        ) + " " + Ansi.RESET + this
    )
}


// 扩展属性
val ParseTree.node: ASTNode
    get() = FeelASTBuilderVisitor(this.parser!!.tokenStream!!).visit(this)
