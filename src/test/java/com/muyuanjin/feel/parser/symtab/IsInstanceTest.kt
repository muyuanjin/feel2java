package com.muyuanjin.feel.parser.symtab

import com.muyuanjin.feel.lang.FeelFunction
import com.muyuanjin.feel.lang.FeelFunctions
import com.muyuanjin.feel.lang.FeelRange
import com.muyuanjin.feel.lang.type.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.math.BigDecimal
import java.time.*

/**
 * @author muyuanjin
 */
internal class IsInstanceTest : StringSpec({
    "anyType" {
        FAny.ANY.isInstance(null) shouldBe true
        checkAll<Any>(100) {
            FAny.ANY.isInstance(it) shouldBe true
        }
    }
    "nullType" {
        FNull.NULL.isInstance(null) shouldBe true
        checkAll<Any>(100) {
            FNull.NULL.isInstance(it) shouldBe false
        }
    }
    "numberType" {
        FNumber.NUMBER.isInstance(null) shouldBe false
        checkAll<Int>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<Long>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<Double>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<Float>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<Short>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<Byte>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<BigDecimal>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe true
        }
        checkAll<Char>(100) {
            FNumber.NUMBER.isInstance(it) shouldBe false
        }
    }
    "string" {
        FString.STRING.isInstance(null) shouldBe false
        checkAll<String>(100) {
            FString.STRING.isInstance(it) shouldBe true
        }
    }
    "boolean" {
        FBoolean.BOOLEAN.isInstance(null) shouldBe false
        checkAll<Boolean>(100) {
            FBoolean.BOOLEAN.isInstance(it) shouldBe true
        }
    }
    "date" {
        FDate.DATE.isInstance(null) shouldBe false
        checkAll<LocalDate>(100) {
            FDate.DATE.isInstance(it) shouldBe true
        }
    }
    "date time" {
        FDateTime.DATE_TIME.isInstance(null) shouldBe false
        checkAll<LocalDateTime>(100) {
            FDateTime.DATE_TIME.isInstance(it) shouldBe true
        }
    }
    "time" {
        FTime.TIME.isInstance(null) shouldBe false
        checkAll<LocalTime>(100) {
            FTime.TIME.isInstance(it) shouldBe true
        }
    }
    "list" {
        FList.ANY.isInstance(null) shouldBe false
        checkAll<List<Int>>(100) {
            FList.ANY.isInstance(it) shouldBe true
            FList.of(FNumber.NUMBER).isInstance(it) shouldBe true
        }
        checkAll<List<String>>(100) {
            FList.ANY.isInstance(it) shouldBe true
            FList.of(FString.STRING).isInstance(it) shouldBe true
        }
        checkAll<List<Boolean>>(100) {
            FList.ANY.isInstance(it) shouldBe true
            FList.of(FBoolean.BOOLEAN).isInstance(it) shouldBe true
        }
    }
    "context" {
        FContext.ANY.isInstance(null) shouldBe false
        checkAll<Map<String, Int>>(100) {
            FContext.ANY.isInstance(it) shouldBe true
        }
        FContext.of("a", FString.STRING).isInstance(mapOf("a" to "", "b" to 1)) shouldBe true
        FContext.of("a", FString.STRING, "b", FNumber.NUMBER).isInstance(mapOf("a" to "", "b" to 1)) shouldBe true
        FContext.of("a", FString.STRING, "b", FNumber.NUMBER).isInstance(mapOf("a" to 1, "b" to 1)) shouldBe false
        FContext.of("a", FString.STRING, "b", FNumber.NUMBER).isInstance(mapOf("a" to "")) shouldBe false
        FContext.of("a", FString.STRING, "c", FNumber.NUMBER).isInstance(mapOf("a" to "", "b" to 1)) shouldBe false
    }
    "day time duration" {
        FDayTimeDuration.DAY_TIME_DURATION.isInstance(null) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.isInstance(Duration.ZERO) shouldBe true
        FDayTimeDuration.DAY_TIME_DURATION.isInstance(Duration.ofHours(3)) shouldBe true
    }
    "year month duration" {
        FYearMonthDuration.YEAR_MONTH_DURATION.isInstance(null) shouldBe false
        checkAll<Period>(100) {
            FYearMonthDuration.YEAR_MONTH_DURATION.isInstance(it) shouldBe true
        }
    }
    "range" {
        FRange.ANY.isInstance(null) shouldBe false

        FRange.ANY.isInstance(RangeImpl<Any>(FRange.ANY)) shouldBe true
        FRange.of(FString.STRING).isInstance(RangeImpl<Any>(FRange.ANY)) shouldBe false
        FRange.of(FString.STRING).isInstance(RangeImpl<Any>(FRange.of(FString.STRING))) shouldBe true
    }
    "function" {
        FFunction.ANY.isInstance(null) shouldBe false
        val type = FFunction.of(FNumber.NUMBER)
        type.isInstance(FunctionImpl<Number>(type)) shouldBe true
        type.isInstance(FunctionImpl<Any>(FFunction.ANY)) shouldBe false
        for (func in FeelFunctions.entries) {
            for (function in func.functions) {
                function.isInstance(FunctionImpl<Any>(function)) shouldBe true
                if (!func.getName().equals("min") && !func.getName().equals("max")) {
                    function.isInstance(FunctionImpl<Any>(FFunction.ANY)) shouldBe false
                }
            }
        }
    }
})

class RangeImpl<T>(private val type: FRange) : FeelRange<T> {
    override fun type(): FRange {
        return type
    }

    override fun start(): T? {
        return null
    }

    override fun end(): T? {
        return null
    }
}

class FunctionImpl<T>(private val type: FFunction) : FeelFunction<T> {
    override fun type(): FFunction {
        return type
    }

    override fun invoke(vararg args: Any?): T? {
        return null
    }
}