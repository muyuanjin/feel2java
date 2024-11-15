package com.muyuanjin.feel.parser

import com.github.javaparser.utils.StringEscapeUtils
import com.muyuanjin.feel.lang.FType
import com.muyuanjin.feel.lang.FTypes
import com.muyuanjin.feel.lang.type.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll
import me.tongfei.progressbar.ManuallyProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import java.math.BigDecimal

class EvalUtilTest : StringSpec({
    "parserJavaNumber 能正确转义数字字面量" {
        ParserUtil.parserJavaNumber("0").toDouble() shouldBe 0
        ParserUtil.parserJavaNumber("00").toDouble() shouldBe 0
        ParserUtil.parserJavaNumber("01").toDouble() shouldBe 1

        ParserUtil.parserJavaNumber("77").toDouble() shouldBe 77
        ParserUtil.parserJavaNumber("7_7").toDouble() shouldBe 7_7
        ParserUtil.parserJavaNumber("0x77").toDouble() shouldBe 0x77.toDouble()
        ParserUtil.parserJavaNumber("0x7_7").toDouble() shouldBe 0x7_7.toDouble()
        ParserUtil.parserJavaNumber("077").toDouble() shouldBe 63.toDouble()
        ParserUtil.parserJavaNumber("07_7").toDouble() shouldBe 63.toDouble()
        ParserUtil.parserJavaNumber("0b111").toDouble() shouldBe 0b111.toDouble()
        ParserUtil.parserJavaNumber("0b1_1_1").toDouble() shouldBe 0b1_1_1.toDouble()

        ParserUtil.parserJavaNumber("77.5").toDouble() shouldBe 77.5
        ParserUtil.parserJavaNumber("7_7.5").toDouble() shouldBe 7_7.5
        ParserUtil.parserJavaNumber("0.1").toDouble() shouldBe 0.1
        ParserUtil.parserJavaNumber("0.2").toDouble() shouldBe 0.2
        ParserUtil.parserJavaNumber("0.3").toDouble() shouldBe 0.3
        ParserUtil.parserJavaNumber("0.33").toDouble() shouldBe 0.33
        ParserUtil.parserJavaNumber("0.333").toDouble() shouldBe 0.333
        ParserUtil.parserJavaNumber("0.3_3").toDouble() shouldBe 0.3_3
        ParserUtil.parserJavaNumber("0.3e1").toDouble() shouldBe 0.3e1
        ParserUtil.parserJavaNumber("0.3e2").toDouble() shouldBe 0.3e2
        ParserUtil.parserJavaNumber("0.3e-2").toDouble() shouldBe 0.3e-2
        ParserUtil.parserJavaNumber("3e1").toDouble() shouldBe 3e1
        ParserUtil.parserJavaNumber("3e2").toDouble() shouldBe 3e2
        ParserUtil.parserJavaNumber("3e-1").toDouble() shouldBe 3e-1
        ParserUtil.parserJavaNumber("3e-2").toDouble() shouldBe 3e-2
        ParserUtil.parserJavaNumber("1.3e1").toDouble() shouldBe 1.3e1
        ParserUtil.parserJavaNumber("1.3e2").toDouble() shouldBe 1.3e2
        ParserUtil.parserJavaNumber("1.3e-1").toDouble() shouldBe 1.3e-1
        ParserUtil.parserJavaNumber("1.3e-2").toDouble() shouldBe 1.3e-2
        ParserUtil.parserJavaNumber("0xeP2").toDouble() shouldBe java.lang.Double.parseDouble("0xeP2")
        ParserUtil.parserJavaNumber("0xe.eP2").toDouble() shouldBe java.lang.Double.parseDouble("0xe.eP2")
        ParserUtil.parserJavaNumber("0xe.1P2").toDouble() shouldBe java.lang.Double.parseDouble("0xe.1P2")
        ParserUtil.parserJavaNumber("0x0.1P2").toDouble() shouldBe java.lang.Double.parseDouble("0x0.1P2")
        ParserUtil.parserJavaNumber("0x.1P2").toDouble() shouldBe java.lang.Double.parseDouble("0x.1P2")
        ParserUtil.parserJavaNumber("0x.1P999").toDouble() shouldBe java.lang.Double.parseDouble("0x.1P999")
    }
    "canExactly 能正确区分可以完整二进制浮点表示的小数" {
        for (i in (0..1000000)) {
            val bigDecimal = ParserUtil.parserJavaNumber("0.$i")
            if (ParserUtil.canExactlyBeDouble(bigDecimal)) {
                bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldBe 0
            } else if (i < 100000) {
                //不能准确表示的数值运算很慢，限制一下数量
                bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldNotBe 0
            }
        }
        val n = "0x.1p"
        for (i in (0..1000)) {
            val bigDecimal = ParserUtil.parserJavaNumber(n + i)
            if (ParserUtil.canExactlyBeDouble(bigDecimal)) {
                bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldBe 0
            } else {
                bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldNotBe 0
            }
        }

        val times = 10_0000
        val pbb = ManuallyProgressBarBuilder()
            .setInitialMax(times.toLong())
            .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
            .setTaskName(this.testCase.descriptor.id.value)
            .setUpdateIntervalMillis(200)
            .setUnit("次", 1)
        pbb.build().use {
            checkAll<Double>(times) { double ->
                val bigDecimal = ParserUtil.parserJavaNumber(double.toString())
                if (bigDecimal == null) {
                    it.step()
                    return@checkAll
                }
                if (ParserUtil.canExactlyBeDouble(bigDecimal)) {
                    bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldBe 0
                } else {
                    bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldNotBe 0
                }
                it.step()
            }
        }
        pbb.build().use {
            checkAll<BigDecimal>(times) { bigDecimal ->
                if (ParserUtil.canExactlyBeDouble(bigDecimal)) {
                    bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldBe 0
                } else {
                    bigDecimal.compareTo(BigDecimal(bigDecimal.toDouble())) shouldNotBe 0
                }
                it.step()
            }
        }
    }

    "translateEscapes 应能正确翻译转义字符" {
        // 假设的测试用例
        ParserUtil.translateEscapes("a\\u4e2d\\u6587b") shouldBe "a中文b"
        ParserUtil.translateEscapes("\"\"\"") shouldBe "\""
        ParserUtil.translateEscapes("\"") shouldBe "\""
        checkAll<String>(100000) { text ->
            ParserUtil.translateEscapes(text) shouldBe unescapeString(text)
        }
    }
    "translateEscapes 不会抛出异常" {
        checkAll<String>(100000) { text ->
            ParserUtil.translateEscapes(text)
        }
    }
    "fromAtValue 应能正确识别类型" {
        ParserUtil.determineAtType("-PT5H") shouldBe FTypes.DAY_TIME_DURATION
        ParserUtil.determineAtType("P1Y") shouldBe FTypes.YEAR_MONTH_DURATION
        checkAll<String>(100000) { text ->
            ParserUtil.determineAtType(text) shouldBe fromAtValue(text)
        }
    }
    "escapeJava 能正确反转义字符" {
        ParserUtil.escapeJava("a中文b", true) shouldBe "\"a中文b\""
        ParserUtil.escapeJava("\"", false) shouldBe "\\\""
        val times = 100000
        val pbb = ManuallyProgressBarBuilder()
            .setInitialMax(times.toLong())
            .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
            .setTaskName(this.testCase.descriptor.id.value)
            .setUpdateIntervalMillis(200)
            .setUnit("次", 1)
        pbb.build().use {
            checkAll<String>(times) { text ->
                ParserUtil.escapeJava(text, false) shouldBe StringEscapeUtils.escapeJava(text)
                it.step()
            }
        }
    }
    "escapeJava 能正确处理不可见ASCII" {
        for (i in (0..31) + (127..159)) {
            when (val c = i.toChar()) {
                '\b' -> ParserUtil.escapeJava(c.toString(), false) shouldBe "\\b"
                '\t' -> ParserUtil.escapeJava(c.toString(), false) shouldBe "\\t"
                '\n' -> ParserUtil.escapeJava(c.toString(), false) shouldBe "\\n"
                '\u000c' -> ParserUtil.escapeJava(c.toString(), false) shouldBe "\\f"
                '\r' -> ParserUtil.escapeJava(c.toString(), false) shouldBe "\\r"
                else -> ParserUtil.escapeJava(c.toString(), false) shouldBe "\\" + i.toString(8)
            }
        }
    }
}
)

private fun Any?.print() {
    System.err.println(this)
}

private fun fromAtValue(value: String): FType {
    val indexOfAt = value.indexOf("@")
    val literalBeforeAt = if (indexOfAt >= 0) value.substring(0, indexOfAt) else value
    if (literalBeforeAt.startsWith("P") || literalBeforeAt.startsWith("-P")) {
        return if (literalBeforeAt.contains("T")) {
            FDayTimeDuration.DAY_TIME_DURATION
        } else FYearMonthDuration.YEAR_MONTH_DURATION
    } else if (literalBeforeAt.contains("T")) {
        return FDateTime.DATE_TIME
    } else if (literalBeforeAt.contains(":")) {
        return FTime.TIME
    } else if (literalBeforeAt.contains("-")) {
        return FDate.DATE
    }
    return FAny.ANY
}

private fun unescapeString(content: String?): String? {
    var text = content ?: return null
    if (text.length >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
        // remove the quotes
        text = text.substring(1, text.length - 1)
    }
    if (text.indexOf('\\') >= 0) {
        // might require un-escaping
        val r = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\') {
                if (text.length > i + 1) {
                    i++
                    when (val cn = text[i]) {
                        'b' -> r.append('\b')
                        't' -> r.append('\t')
                        'n' -> r.append('\n')
                        'f' -> r.append('\u000c')
                        'r' -> r.append('\r')
                        '"' -> r.append('"')
                        '\'' -> r.append('\'')
                        '\\' -> r.append('\\')
                        'u' -> if (text.length >= i + 5) {
                            // escape unicode
                            try {
                                val hex = text.substring(i + 1, i + 5)
                                val chars = Character.toChars(hex.toInt(16))
                                r.append(chars)
                                i += 4
                            } catch (e: Exception) {
                                // not really unicode
                                r.append("\\").append(cn)
                            }
                        } else {
                            // not really unicode
                            r.append("\\").append(cn)
                        }

                        'U' -> if (text.length >= i + 7) {
                            try {// escape unicode
                                val hex = text.substring(i + 1, i + 7)
                                val chars = Character.toChars(hex.toInt(16))
                                r.append(chars)
                                i += 6
                            } catch (e: Exception) {
                                // not really unicode
                                r.append("\\").append(cn)
                            }
                        } else {
                            // not really unicode
                            r.append("\\").append(cn)
                        }

                        else -> r.append("\\").append(cn)
                    }
                } else {
                    r.append(c)
                }
            } else {
                r.append(c)
            }
            i++
        }
        text = r.toString()
    }
    return text
}