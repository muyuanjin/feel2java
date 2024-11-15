package com.muyuanjin.feel.parser.symtab

import com.muyuanjin.feel.util.JSONUtil
import com.muyuanjin.feel.lang.FType
import com.muyuanjin.feel.lang.type.*
import com.muyuanjin.feel.lang.type.FNumber.*
import com.muyuanjin.feel.parser.symtab.PrintMode.JSON
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

/**
 * @author muyuanjin
 */
internal class TypeTest : StringSpec({
    "anyType" {
        val any = FAny.ANY
        FAny.ANY.canConvertTo(any) shouldBe true
        FBoolean.BOOLEAN.canConvertTo(any) shouldBe true
        FContext.EMPTY.canConvertTo(any) shouldBe true
        FContext.of().canConvertTo(any) shouldBe true
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(any) shouldBe true
        FDate.DATE.canConvertTo(any) shouldBe true
        FDateTime.DATE_TIME.canConvertTo(any) shouldBe true
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(any) shouldBe true
        FFunction.of(INTEGER, FContext.of()).canConvertTo(any) shouldBe true
        FList.ANY.canConvertTo(any) shouldBe true
        FList.of(arrayOf("g", "gg")).canConvertTo(FList.ANY) shouldBe true
        FNull.NULL.canConvertTo(any) shouldBe true
        INTEGER.canConvertTo(any) shouldBe true
        DOUBLE.canConvertTo(any) shouldBe true
        NUMBER.canConvertTo(any) shouldBe true
        FRange.of(INTEGER).canConvertTo(any) shouldBe true
        FRange.ANY.canConvertTo(any) shouldBe true
        FString.STRING.canConvertTo(any) shouldBe true
        FTime.TIME.canConvertTo(any) shouldBe true
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(any) shouldBe true


        any.canConvertTo(FBoolean.BOOLEAN) shouldBe false
        any.canConvertTo(FContext.EMPTY) shouldBe false
        any.canConvertTo(FContext.of()) shouldBe false
        any.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false
        any.canConvertTo(FDate.DATE) shouldBe false
        any.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        any.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        any.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        any.canConvertTo(FList.ANY) shouldBe false
        any.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        any.canConvertTo(FNull.NULL) shouldBe false
        any.canConvertTo(INTEGER) shouldBe false
        any.canConvertTo(DOUBLE) shouldBe false
        any.canConvertTo(NUMBER) shouldBe false
        any.canConvertTo(FRange.of(INTEGER)) shouldBe false
        any.canConvertTo(FRange.ANY) shouldBe false
        any.canConvertTo(FString.STRING) shouldBe false
        any.canConvertTo(FTime.TIME) shouldBe false
        any.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false

    }

    "nullType" {
        val nul = FNull.NULL
        nul.canConvertTo(nul) shouldBe true
        nul.canConvertTo(FAny.ANY) shouldBe true
        nul.canConvertTo(NUMBER) shouldBe true
        nul.canConvertTo(INTEGER) shouldBe true
        nul.canConvertTo(DOUBLE) shouldBe true
        nul.canConvertTo(FString.STRING) shouldBe true
        nul.canConvertTo(FBoolean.BOOLEAN) shouldBe true
        nul.canConvertTo(FDate.DATE) shouldBe true
        nul.canConvertTo(FDateTime.DATE_TIME) shouldBe true
        nul.canConvertTo(FTime.TIME) shouldBe true
        nul.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe true
        nul.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe true
        nul.canConvertTo(FList.ANY) shouldBe true
        nul.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe true
        nul.canConvertTo(FRange.of(INTEGER)) shouldBe true
        nul.canConvertTo(FRange.ANY) shouldBe true
        nul.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe true
        nul.canConvertTo(FContext.EMPTY) shouldBe true
        nul.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe true

        FAny.ANY.canConvertTo(nul) shouldBe false
        NUMBER.canConvertTo(nul) shouldBe false
        INTEGER.canConvertTo(nul) shouldBe false
        DOUBLE.canConvertTo(nul) shouldBe false
        FString.STRING.canConvertTo(nul) shouldBe false
        FBoolean.BOOLEAN.canConvertTo(nul) shouldBe false
        FDate.DATE.canConvertTo(nul) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(nul) shouldBe false
        FTime.TIME.canConvertTo(nul) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(nul) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(nul) shouldBe false
        FList.ANY.canConvertTo(nul) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(nul) shouldBe false
        FRange.of(INTEGER).canConvertTo(nul) shouldBe false
        FRange.ANY.canConvertTo(nul) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(nul) shouldBe false
        FContext.EMPTY.canConvertTo(nul) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(nul) shouldBe false
    }

    "booleanType" {
        val bool = FBoolean.BOOLEAN
        bool.canConvertTo(bool) shouldBe true
        bool.canConvertTo(FAny.ANY) shouldBe true
        bool.canConvertTo(NUMBER) shouldBe false
        bool.canConvertTo(INTEGER) shouldBe false
        bool.canConvertTo(DOUBLE) shouldBe false
        bool.canConvertTo(FString.STRING) shouldBe false
        bool.canConvertTo(FDate.DATE) shouldBe false
        bool.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        bool.canConvertTo(FTime.TIME) shouldBe false
        bool.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        bool.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        bool.canConvertTo(FList.ANY) shouldBe false
        bool.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        bool.canConvertTo(FRange.of(INTEGER)) shouldBe false
        bool.canConvertTo(FRange.ANY) shouldBe false
        bool.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        bool.canConvertTo(FContext.EMPTY) shouldBe false
        bool.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(bool) shouldBe false
        NUMBER.canConvertTo(bool) shouldBe false
        INTEGER.canConvertTo(bool) shouldBe false
        DOUBLE.canConvertTo(bool) shouldBe false
        FString.STRING.canConvertTo(bool) shouldBe false
        FDate.DATE.canConvertTo(bool) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(bool) shouldBe false
        FTime.TIME.canConvertTo(bool) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(bool) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(bool) shouldBe false
        FList.ANY.canConvertTo(bool) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(bool) shouldBe false
        FRange.of(INTEGER).canConvertTo(bool) shouldBe false
        FRange.ANY.canConvertTo(bool) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(bool) shouldBe false
        FContext.EMPTY.canConvertTo(bool) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(bool) shouldBe false
    }

    "contextType" {
        val bodyType = context("age" to INTEGER, "name" to FString.STRING)
        val personType = context("person" to bodyType)
        val body = mapOf("age" to 1, "name" to "bob")
        val person = mapOf("person" to body)

        val anonymousBody = mapOf("name" to null, "age" to 99)
        val anonymousPerson = mapOf("person" to anonymousBody)

        val notBody = mapOf("no" to 1)
        val notPerson = mapOf("no" to notBody)

        val empty = mapOf<String, Any>()

        bodyType.canConvertTo(bodyType) shouldBe true
        bodyType.canConvertTo(FAny.ANY) shouldBe true
        personType.canConvertTo(personType) shouldBe true
        personType.canConvertTo(FAny.ANY) shouldBe true
        bodyType.members["age"] shouldBe INTEGER
        bodyType.members["name"] shouldBe FString.STRING
        personType.members["person"] shouldBe bodyType

        personType.canConvertTo(context("person" to bodyType, "other" to FAny.ANY)) shouldBe false
        context("person" to bodyType, "other" to FAny.ANY).canConvertTo(personType) shouldBe true

        personType.canConvertTo(FType.of(person)) shouldBe true
        FType.of(person).canConvertTo(personType) shouldBe true
        FType.of(person).equals(personType) shouldBe true
        FType.of(body).canConvertTo(bodyType) shouldBe true
        FType.of(body).equals(bodyType) shouldBe true

        personType.canConvertTo(FType.of(anonymousPerson)) shouldBe false
        FType.of(anonymousPerson).canConvertTo(personType) shouldBe true
        FType.of(anonymousPerson).equals(personType) shouldBe false
        FType.of(anonymousBody).canConvertTo(bodyType) shouldBe true
        FType.of(anonymousBody).equals(bodyType) shouldBe false

        personType.canConvertTo(FType.of(notPerson)) shouldBe false
        FType.of(notPerson).canConvertTo(personType) shouldBe false
        FType.of(notPerson).equals(personType) shouldBe false
        FType.of(notBody).canConvertTo(bodyType) shouldBe false
        FType.of(notBody).equals(bodyType) shouldBe false

        personType.canConvertTo(FType.of(empty)) shouldBe true
        FType.of(empty).canConvertTo(personType) shouldBe false
        FType.of(empty).equals(personType) shouldBe false
        FType.of(empty).canConvertTo(bodyType) shouldBe false
        FType.of(empty).equals(bodyType) shouldBe false
        FType.of(empty).canConvertTo(FContext.EMPTY) shouldBe true
        FType.of(empty).equals(FContext.EMPTY) shouldBe true

        context().canConvertTo(FContext.EMPTY) shouldBe true
        (context() == FContext.EMPTY) shouldBe true
        bodyType.canConvertTo(FContext.EMPTY) shouldBe true


        personType.canConvertTo(personType) shouldBe true
        personType.canConvertTo(FAny.ANY) shouldBe true
        bodyType.canConvertTo(bodyType) shouldBe true
        bodyType.canConvertTo(FAny.ANY) shouldBe true

        personType.canConvertTo(context("person" to bodyType, "other" to FAny.ANY)) shouldBe false
        context("person" to bodyType, "other" to FAny.ANY).canConvertTo(personType) shouldBe true

        personType.canConvertTo(FType.of(person)) shouldBe true
        FType.of(person).canConvertTo(personType) shouldBe true
        FType.of(person).equals(personType) shouldBe true
        FType.of(body).canConvertTo(bodyType) shouldBe true
        FType.of(body).equals(bodyType) shouldBe true

        context().canConvertTo(FContext.EMPTY) shouldBe true
        bodyType.canConvertTo(FContext.EMPTY) shouldBe true

        FContext.EMPTY.canConvertTo(FContext.EMPTY) shouldBe true
        FContext.EMPTY.canConvertTo(FAny.ANY) shouldBe true
        FContext.EMPTY.canConvertTo(NUMBER) shouldBe false
        FContext.EMPTY.canConvertTo(INTEGER) shouldBe false
        FContext.EMPTY.canConvertTo(DOUBLE) shouldBe false
        FContext.EMPTY.canConvertTo(FString.STRING) shouldBe false
        FContext.EMPTY.canConvertTo(FDate.DATE) shouldBe false
        FContext.EMPTY.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        FContext.EMPTY.canConvertTo(FTime.TIME) shouldBe false
        FContext.EMPTY.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        FContext.EMPTY.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        FContext.EMPTY.canConvertTo(FList.ANY) shouldBe false
        FContext.EMPTY.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        FContext.EMPTY.canConvertTo(FRange.of(INTEGER)) shouldBe false
        FContext.EMPTY.canConvertTo(FRange.ANY) shouldBe false
        FContext.EMPTY.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        FContext.EMPTY.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(FContext.EMPTY) shouldBe false
        NUMBER.canConvertTo(FContext.EMPTY) shouldBe false
        INTEGER.canConvertTo(FContext.EMPTY) shouldBe false
        DOUBLE.canConvertTo(FContext.EMPTY) shouldBe false
        FString.STRING.canConvertTo(FContext.EMPTY) shouldBe false
        FDate.DATE.canConvertTo(FContext.EMPTY) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(FContext.EMPTY) shouldBe false
        FTime.TIME.canConvertTo(FContext.EMPTY) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(FContext.EMPTY) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(FContext.EMPTY) shouldBe false
        FList.ANY.canConvertTo(FContext.EMPTY) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(FContext.EMPTY) shouldBe false
        FRange.of(INTEGER).canConvertTo(FContext.EMPTY) shouldBe false
        FRange.ANY.canConvertTo(FContext.EMPTY) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(FContext.EMPTY) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(FContext.EMPTY) shouldBe true
    }

    "dateTest" {
        FDate.DATE.members["year"] shouldBe INTEGER
        FDate.DATE.members["month"] shouldBe INTEGER
        FDate.DATE.members["day"] shouldBe INTEGER

        FDate.DATE.canConvertTo(FDate.DATE) shouldBe true
        FDate.DATE.canConvertTo(FDateTime.DATE_TIME) shouldBe true // 日期可视为当天的 00:00:00 时间
        FDate.DATE.canConvertTo(FAny.ANY) shouldBe true

        FType.of(LocalDate.now()).canConvertTo(FDate.DATE) shouldBe true
        FType.of(LocalDate.now()).equals(FDate.DATE) shouldBe true
        FType.of(LocalDate.now()).canConvertTo(FDateTime.DATE_TIME) shouldBe true

        val date = FDate.DATE
        date.canConvertTo(date) shouldBe true
        date.canConvertTo(FAny.ANY) shouldBe true
        date.canConvertTo(NUMBER) shouldBe false
        date.canConvertTo(INTEGER) shouldBe false
        date.canConvertTo(DOUBLE) shouldBe false
        date.canConvertTo(FString.STRING) shouldBe false
        date.canConvertTo(FDate.DATE) shouldBe true
        date.canConvertTo(FDateTime.DATE_TIME) shouldBe true
        date.canConvertTo(FTime.TIME) shouldBe false
        date.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        date.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        date.canConvertTo(FList.ANY) shouldBe false
        date.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        date.canConvertTo(FRange.of(INTEGER)) shouldBe false
        date.canConvertTo(FRange.ANY) shouldBe false
        date.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        date.canConvertTo(FContext.EMPTY) shouldBe false
        date.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(date) shouldBe false
        NUMBER.canConvertTo(date) shouldBe false
        INTEGER.canConvertTo(date) shouldBe false
        DOUBLE.canConvertTo(date) shouldBe false
        FString.STRING.canConvertTo(date) shouldBe false
        FDate.DATE.canConvertTo(date) shouldBe true
        FDateTime.DATE_TIME.canConvertTo(date) shouldBe true
        FTime.TIME.canConvertTo(date) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(date) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(date) shouldBe false
        FList.ANY.canConvertTo(date) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(date) shouldBe false
        FRange.of(INTEGER).canConvertTo(date) shouldBe false
        FRange.ANY.canConvertTo(date) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(date) shouldBe false
        FContext.EMPTY.canConvertTo(date) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(date) shouldBe false
    }

    "dateTimeTest" {
        FDateTime.DATE_TIME.members["date"] shouldBe FDate.DATE
        FDateTime.DATE_TIME.members["time"] shouldBe FTime.TIME
        FDateTime.DATE_TIME.members["year"] shouldBe INTEGER
        FDateTime.DATE_TIME.members["month"] shouldBe INTEGER
        FDateTime.DATE_TIME.members["day"] shouldBe INTEGER
        FDateTime.DATE_TIME.members["hour"] shouldBe INTEGER
        FDateTime.DATE_TIME.members["minute"] shouldBe INTEGER
        FDateTime.DATE_TIME.members["second"] shouldBe INTEGER

        FDateTime.DATE_TIME.canConvertTo(FDateTime.DATE_TIME) shouldBe true
        FDateTime.DATE_TIME.canConvertTo(FDate.DATE) shouldBe true
        FDateTime.DATE_TIME.canConvertTo(FTime.TIME) shouldBe true
        FDateTime.DATE_TIME.canConvertTo(FAny.ANY) shouldBe true

        FType.of(LocalDateTime.now()).canConvertTo(FDateTime.DATE_TIME) shouldBe true
        FType.of(LocalDateTime.now()).equals(FDateTime.DATE_TIME) shouldBe true
        FType.of(LocalDateTime.now()).canConvertTo(FDate.DATE) shouldBe true
        FType.of(LocalDateTime.now()).canConvertTo(FTime.TIME) shouldBe true
        FType.of(LocalDateTime.now()).canConvertTo(FAny.ANY) shouldBe true


        val dateTime = FDateTime.DATE_TIME
        dateTime.canConvertTo(dateTime) shouldBe true
        dateTime.canConvertTo(FAny.ANY) shouldBe true
        dateTime.canConvertTo(NUMBER) shouldBe false
        dateTime.canConvertTo(INTEGER) shouldBe false
        dateTime.canConvertTo(DOUBLE) shouldBe false
        dateTime.canConvertTo(FString.STRING) shouldBe false
        dateTime.canConvertTo(FDate.DATE) shouldBe true
        dateTime.canConvertTo(FDateTime.DATE_TIME) shouldBe true
        dateTime.canConvertTo(FTime.TIME) shouldBe true
        dateTime.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        dateTime.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        dateTime.canConvertTo(FList.ANY) shouldBe false
        dateTime.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        dateTime.canConvertTo(FRange.of(INTEGER)) shouldBe false
        dateTime.canConvertTo(FRange.ANY) shouldBe false
        dateTime.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        dateTime.canConvertTo(FContext.EMPTY) shouldBe false
        dateTime.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(dateTime) shouldBe false
        NUMBER.canConvertTo(dateTime) shouldBe false
        INTEGER.canConvertTo(dateTime) shouldBe false
        DOUBLE.canConvertTo(dateTime) shouldBe false
        FString.STRING.canConvertTo(dateTime) shouldBe false
        FDate.DATE.canConvertTo(dateTime) shouldBe true
        FDateTime.DATE_TIME.canConvertTo(dateTime) shouldBe true
        FTime.TIME.canConvertTo(dateTime) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(dateTime) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(dateTime) shouldBe false
        FList.ANY.canConvertTo(dateTime) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(dateTime) shouldBe false
        FRange.of(INTEGER).canConvertTo(dateTime) shouldBe false
        FRange.ANY.canConvertTo(dateTime) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(dateTime) shouldBe false
        FContext.EMPTY.canConvertTo(dateTime) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(dateTime) shouldBe false
    }

    "datTimeDurationTest" {
        FDayTimeDuration.DAY_TIME_DURATION.members["days"] shouldBe INTEGER
        FDayTimeDuration.DAY_TIME_DURATION.members["hours"] shouldBe INTEGER
        FDayTimeDuration.DAY_TIME_DURATION.members["minutes"] shouldBe INTEGER
        FDayTimeDuration.DAY_TIME_DURATION.members["seconds"] shouldBe INTEGER
        FDayTimeDuration.DAY_TIME_DURATION.members["value"] shouldBe LONG

        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe true
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(FAny.ANY) shouldBe true

        FType.of(Duration.ofHours(2)).canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe true
        FType.of(Duration.ofHours(2)).equals(FDayTimeDuration.DAY_TIME_DURATION) shouldBe true
        FType.of(Duration.ofHours(2)).canConvertTo(FAny.ANY) shouldBe true


        val dayTimeDuration = FDayTimeDuration.DAY_TIME_DURATION
        dayTimeDuration.canConvertTo(dayTimeDuration) shouldBe true
        dayTimeDuration.canConvertTo(FAny.ANY) shouldBe true
        dayTimeDuration.canConvertTo(NUMBER) shouldBe false
        dayTimeDuration.canConvertTo(INTEGER) shouldBe false
        dayTimeDuration.canConvertTo(DOUBLE) shouldBe false
        dayTimeDuration.canConvertTo(FString.STRING) shouldBe false
        dayTimeDuration.canConvertTo(FDate.DATE) shouldBe false
        dayTimeDuration.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        dayTimeDuration.canConvertTo(FTime.TIME) shouldBe false
        dayTimeDuration.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        dayTimeDuration.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe true
        dayTimeDuration.canConvertTo(FList.ANY) shouldBe false
        dayTimeDuration.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        dayTimeDuration.canConvertTo(FRange.of(INTEGER)) shouldBe false
        dayTimeDuration.canConvertTo(FRange.ANY) shouldBe false
        dayTimeDuration.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false

        FAny.ANY.canConvertTo(dayTimeDuration) shouldBe false
        NUMBER.canConvertTo(dayTimeDuration) shouldBe false
        INTEGER.canConvertTo(dayTimeDuration) shouldBe false
        DOUBLE.canConvertTo(dayTimeDuration) shouldBe false
        FString.STRING.canConvertTo(dayTimeDuration) shouldBe false
        FDate.DATE.canConvertTo(dayTimeDuration) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(dayTimeDuration) shouldBe false
        FTime.TIME.canConvertTo(dayTimeDuration) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(dayTimeDuration) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(dayTimeDuration) shouldBe true
        FList.ANY.canConvertTo(dayTimeDuration) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(dayTimeDuration) shouldBe false
        FRange.of(INTEGER).canConvertTo(dayTimeDuration) shouldBe false
        FRange.ANY.canConvertTo(dayTimeDuration) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(dayTimeDuration) shouldBe false
        FContext.EMPTY.canConvertTo(dayTimeDuration) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(dayTimeDuration) shouldBe false
    }

    "functionTest" {
        val context = FContext.of("age" to INTEGER, "name" to FString.STRING)
        val function = FFunction.of(INTEGER, context)
        function.canConvertTo(function) shouldBe true
        function.canConvertTo(FAny.ANY) shouldBe true
        function.canConvertTo(NUMBER) shouldBe false
        function.canConvertTo(INTEGER) shouldBe false
        function.canConvertTo(DOUBLE) shouldBe false
        function.canConvertTo(FString.STRING) shouldBe false
        function.canConvertTo(FDate.DATE) shouldBe false
        function.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        function.canConvertTo(FTime.TIME) shouldBe false
        function.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        function.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        function.canConvertTo(FList.ANY) shouldBe false
        function.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        function.canConvertTo(FRange.of(INTEGER)) shouldBe false
        function.canConvertTo(FRange.ANY) shouldBe false
        function.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        function.canConvertTo(FContext.EMPTY) shouldBe false
        function.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(function) shouldBe false
        NUMBER.canConvertTo(function) shouldBe false
        INTEGER.canConvertTo(function) shouldBe false
        DOUBLE.canConvertTo(function) shouldBe false
        FString.STRING.canConvertTo(function) shouldBe false
        FDate.DATE.canConvertTo(function) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(function) shouldBe false
        FTime.TIME.canConvertTo(function) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(function) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(function) shouldBe false
        FList.ANY.canConvertTo(function) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(function) shouldBe false
        FRange.of(INTEGER).canConvertTo(function) shouldBe false
        FRange.ANY.canConvertTo(function) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(function) shouldBe false
        FContext.EMPTY.canConvertTo(function) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(function) shouldBe false
    }

    "listTest" {
        val list = FList.of(arrayOf("g", "gg"))
        list.canConvertTo(list) shouldBe true
        list.canConvertTo(FAny.ANY) shouldBe true
        list.canConvertTo(FList.ANY) shouldBe true
        list.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe true
        list.canConvertTo(FList.of(arrayOf("g", "gg", "ggg"))) shouldBe true
        list.canConvertTo(FList.of(arrayOf("g", 2))) shouldBe true

        FList.of(listOf("g", 2)).equals(FList.of(FAny.ANY)) shouldBe true
        FList.of(arrayOf("g", 2)).equals(FList.of(FAny.ANY)) shouldBe true
        FList.of(listOf("g", null)).equals(list) shouldBe true



        list.canConvertTo(list) shouldBe true
        list.canConvertTo(FAny.ANY) shouldBe true
        list.canConvertTo(NUMBER) shouldBe false
        list.canConvertTo(INTEGER) shouldBe false
        list.canConvertTo(DOUBLE) shouldBe false
        list.canConvertTo(FString.STRING) shouldBe false
        list.canConvertTo(FDate.DATE) shouldBe false
        list.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        list.canConvertTo(FTime.TIME) shouldBe false
        list.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        list.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        list.canConvertTo(FList.ANY) shouldBe true
        list.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe true
        list.canConvertTo(FList.of(arrayOf(1, 2))) shouldBe false
        list.canConvertTo(FRange.of(INTEGER)) shouldBe false
        list.canConvertTo(FRange.ANY) shouldBe false
        list.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        list.canConvertTo(FContext.EMPTY) shouldBe false
        list.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(list) shouldBe false
        NUMBER.canConvertTo(list) shouldBe false
        INTEGER.canConvertTo(list) shouldBe false
        DOUBLE.canConvertTo(list) shouldBe false
        FString.STRING.canConvertTo(list) shouldBe false
        FDate.DATE.canConvertTo(list) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(list) shouldBe false
        FTime.TIME.canConvertTo(list) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(list) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(list) shouldBe false
        FList.ANY.canConvertTo(list) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(list) shouldBe true
        FRange.of(INTEGER).canConvertTo(list) shouldBe false
        FRange.ANY.canConvertTo(list) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(list) shouldBe false
        FContext.EMPTY.canConvertTo(list) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(list) shouldBe false
    }

    "numberTest" {
        NUMBER.canConvertTo(NUMBER) shouldBe true
        NUMBER.canConvertTo(INTEGER) shouldBe true
        NUMBER.canConvertTo(DOUBLE) shouldBe true

        INTEGER.canConvertTo(NUMBER) shouldBe true
        INTEGER.canConvertTo(INTEGER) shouldBe true
        INTEGER.canConvertTo(DOUBLE) shouldBe true

        DOUBLE.canConvertTo(NUMBER) shouldBe true
        DOUBLE.canConvertTo(INTEGER) shouldBe true
        DOUBLE.canConvertTo(DOUBLE) shouldBe true

        for (num in arrayOf(NUMBER, INTEGER, DOUBLE)) {
            num.canConvertTo(FAny.ANY) shouldBe true
            num.canConvertTo(NUMBER) shouldBe true
            num.canConvertTo(DOUBLE) shouldBe true
            num.canConvertTo(FString.STRING) shouldBe false
            num.canConvertTo(FBoolean.BOOLEAN) shouldBe false
            num.canConvertTo(FDate.DATE) shouldBe false
            num.canConvertTo(FDateTime.DATE_TIME) shouldBe false
            num.canConvertTo(FTime.TIME) shouldBe false
            num.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
            num.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
            num.canConvertTo(FList.ANY) shouldBe false
            num.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
            num.canConvertTo(FRange.of(INTEGER)) shouldBe false
            num.canConvertTo(FRange.ANY) shouldBe false
            num.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
            num.canConvertTo(FContext.EMPTY) shouldBe false
            num.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

            FAny.ANY.canConvertTo(num) shouldBe false
            INTEGER.canConvertTo(num) shouldBe true
            FString.STRING.canConvertTo(num) shouldBe false
            FDate.DATE.canConvertTo(num) shouldBe false
            FDateTime.DATE_TIME.canConvertTo(num) shouldBe false
            FTime.TIME.canConvertTo(num) shouldBe false
            FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(num) shouldBe false
            FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(num) shouldBe false
            FList.ANY.canConvertTo(num) shouldBe false
            FList.of(arrayOf("g", "gg")).canConvertTo(num) shouldBe false
            FRange.of(INTEGER).canConvertTo(num) shouldBe false
            FRange.ANY.canConvertTo(num) shouldBe false
            FFunction.of(INTEGER, FContext.of()).canConvertTo(num) shouldBe false
            FContext.EMPTY.canConvertTo(num) shouldBe false
            FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(num) shouldBe false
        }
    }

    "rangeTest" {
        val range = FRange.of(INTEGER)

        for (start in arrayListOf(true, false, null)) {
            for (end in arrayListOf(true, false, null)) {
                for (tStart in arrayListOf(true, false, null)) {
                    for (tEnd in arrayListOf(true, false, null)) {
                        val left = FRange.of(INTEGER, start, end)
                        val right = FRange.of(NUMBER, tStart, tEnd)
                        var result = false
                        if (tStart == null && tEnd == null) {
                            result = true
                        } else if (start == tStart && end == tEnd) {
                            result = true
                        }
                        left.canConvertTo(right) shouldBe result
                    }
                }
            }
        }

        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER)) shouldBe true
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, true, null)) shouldBe true
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, true, true)) shouldBe false
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, true, false)) shouldBe false
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, false, true)) shouldBe false
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, false, false)) shouldBe false
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, false, null)) shouldBe false
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, null, false)) shouldBe false
        FRange.of(INTEGER, true, null).canConvertTo(FRange.of(NUMBER, null, true)) shouldBe false

        FRange.of(INTEGER, NUMBER) shouldBe FRange.of(NUMBER)
        FRange.of(INTEGER, DOUBLE) shouldBe FRange.of(NUMBER)
        FRange.of(INTEGER, FAny.ANY) shouldBe FRange.of(FAny.ANY)

        range.canConvertTo(range) shouldBe true
        range.canConvertTo(FAny.ANY) shouldBe true
        range.canConvertTo(NUMBER) shouldBe false
        range.canConvertTo(INTEGER) shouldBe false
        range.canConvertTo(DOUBLE) shouldBe false
        range.canConvertTo(FString.STRING) shouldBe false
        range.canConvertTo(FDate.DATE) shouldBe false
        range.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        range.canConvertTo(FTime.TIME) shouldBe false
        range.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        range.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        range.canConvertTo(FList.ANY) shouldBe false
        range.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        range.canConvertTo(FRange.of(INTEGER)) shouldBe true
        range.canConvertTo(FRange.ANY) shouldBe true
        range.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        range.canConvertTo(FContext.EMPTY) shouldBe false
        range.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FRange.of(INTEGER).canConvertTo(range) shouldBe true
        FRange.of(INTEGER).equals(range) shouldBe true
        FRange.ANY.canConvertTo(range) shouldBe false
        range.canConvertTo(FRange.ANY) shouldBe true

        FAny.ANY.canConvertTo(range) shouldBe false
        NUMBER.canConvertTo(range) shouldBe false
        INTEGER.canConvertTo(range) shouldBe false
        DOUBLE.canConvertTo(range) shouldBe false
        FString.STRING.canConvertTo(range) shouldBe false
        FDate.DATE.canConvertTo(range) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(range) shouldBe false
        FTime.TIME.canConvertTo(range) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(range) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(range) shouldBe false
        FList.ANY.canConvertTo(range) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(range) shouldBe false
        FRange.of(INTEGER).canConvertTo(range) shouldBe true
        FRange.ANY.canConvertTo(range) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(range) shouldBe false
        FContext.EMPTY.canConvertTo(range) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(range) shouldBe false
    }

    "stringTest" {
        FString.STRING.canConvertTo(FString.STRING) shouldBe true
        FString.STRING.canConvertTo(FAny.ANY) shouldBe true
        FString.STRING.canConvertTo(NUMBER) shouldBe false
        FString.STRING.canConvertTo(INTEGER) shouldBe false
        FString.STRING.canConvertTo(DOUBLE) shouldBe false
        FString.STRING.canConvertTo(FDate.DATE) shouldBe false
        FString.STRING.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        FString.STRING.canConvertTo(FTime.TIME) shouldBe false
        FString.STRING.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        FString.STRING.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        FString.STRING.canConvertTo(FList.ANY) shouldBe false
        FString.STRING.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        FString.STRING.canConvertTo(FRange.of(INTEGER)) shouldBe false
        FString.STRING.canConvertTo(FRange.ANY) shouldBe false
        FString.STRING.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        FString.STRING.canConvertTo(FContext.EMPTY) shouldBe false
        FString.STRING.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FType.of("S").canConvertTo(FString.STRING) shouldBe true
        FType.of("S").equals(FString.STRING) shouldBe true


        FAny.ANY.canConvertTo(FString.STRING) shouldBe false
        NUMBER.canConvertTo(FString.STRING) shouldBe false
        INTEGER.canConvertTo(FString.STRING) shouldBe false
        DOUBLE.canConvertTo(FString.STRING) shouldBe false
        FDate.DATE.canConvertTo(FString.STRING) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(FString.STRING) shouldBe false
        FTime.TIME.canConvertTo(FString.STRING) shouldBe false
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(FString.STRING) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(FString.STRING) shouldBe false
        FList.ANY.canConvertTo(FString.STRING) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(FString.STRING) shouldBe false
        FRange.of(INTEGER).canConvertTo(FString.STRING) shouldBe false
        FRange.ANY.canConvertTo(FString.STRING) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(FString.STRING) shouldBe false
        FContext.EMPTY.canConvertTo(FString.STRING) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(FString.STRING) shouldBe false
    }

    "timeTest" {
        FTime.TIME.members["hour"] shouldBe INTEGER
        FTime.TIME.members["minute"] shouldBe INTEGER
        FTime.TIME.members["second"] shouldBe INTEGER

        FTime.TIME.canConvertTo(FTime.TIME) shouldBe true
        FTime.TIME.canConvertTo(FAny.ANY) shouldBe true
        FTime.TIME.canConvertTo(NUMBER) shouldBe false
        FTime.TIME.canConvertTo(INTEGER) shouldBe false
        FTime.TIME.canConvertTo(DOUBLE) shouldBe false
        FTime.TIME.canConvertTo(FString.STRING) shouldBe false
        FTime.TIME.canConvertTo(FDate.DATE) shouldBe false
        FTime.TIME.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        FTime.TIME.canConvertTo(FYearMonthDuration.YEAR_MONTH_DURATION) shouldBe false
        FTime.TIME.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        FTime.TIME.canConvertTo(FList.ANY) shouldBe false
        FTime.TIME.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        FTime.TIME.canConvertTo(FRange.of(INTEGER)) shouldBe false
        FTime.TIME.canConvertTo(FRange.ANY) shouldBe false
        FTime.TIME.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        FTime.TIME.canConvertTo(FContext.EMPTY) shouldBe false
        FTime.TIME.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FAny.ANY.canConvertTo(FTime.TIME) shouldBe false
        NUMBER.canConvertTo(FTime.TIME) shouldBe false
        INTEGER.canConvertTo(FTime.TIME) shouldBe false
        DOUBLE.canConvertTo(FTime.TIME) shouldBe false
        FString.STRING.canConvertTo(FTime.TIME) shouldBe false
        FDate.DATE.canConvertTo(FTime.TIME) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(FTime.TIME) shouldBe true
        FYearMonthDuration.YEAR_MONTH_DURATION.canConvertTo(FTime.TIME) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(FTime.TIME) shouldBe false
        FList.ANY.canConvertTo(FTime.TIME) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(FTime.TIME) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(FTime.TIME) shouldBe false
        FRange.of(INTEGER).canConvertTo(FTime.TIME) shouldBe false
        FRange.ANY.canConvertTo(FTime.TIME) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(FTime.TIME) shouldBe false
        FContext.EMPTY.canConvertTo(FTime.TIME) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(FTime.TIME) shouldBe false
    }

    "yearMonthDurationTest" {
        val yearMonthDuration = FYearMonthDuration.YEAR_MONTH_DURATION
        yearMonthDuration.members["years"] shouldBe INTEGER
        yearMonthDuration.members["months"] shouldBe INTEGER

        yearMonthDuration.canConvertTo(yearMonthDuration) shouldBe true
        yearMonthDuration.canConvertTo(FAny.ANY) shouldBe true
        yearMonthDuration.canConvertTo(NUMBER) shouldBe false
        yearMonthDuration.canConvertTo(INTEGER) shouldBe false
        yearMonthDuration.canConvertTo(DOUBLE) shouldBe false
        yearMonthDuration.canConvertTo(FString.STRING) shouldBe false
        yearMonthDuration.canConvertTo(FDate.DATE) shouldBe false
        yearMonthDuration.canConvertTo(FDateTime.DATE_TIME) shouldBe false
        yearMonthDuration.canConvertTo(FTime.TIME) shouldBe false
        yearMonthDuration.canConvertTo(FDayTimeDuration.DAY_TIME_DURATION) shouldBe false
        yearMonthDuration.canConvertTo(FList.ANY) shouldBe false
        yearMonthDuration.canConvertTo(FList.of(arrayOf("g", "gg"))) shouldBe false
        yearMonthDuration.canConvertTo(FRange.of(INTEGER)) shouldBe false
        yearMonthDuration.canConvertTo(FRange.ANY) shouldBe false
        yearMonthDuration.canConvertTo(FFunction.of(INTEGER, FContext.of())) shouldBe false
        yearMonthDuration.canConvertTo(FContext.EMPTY) shouldBe false
        yearMonthDuration.canConvertTo(FContext.of("age", INTEGER, "name", FString.STRING)) shouldBe false

        FType.of(Period.ofYears(2)).canConvertTo(yearMonthDuration) shouldBe true
        FType.of(Period.ofYears(2)).equals(yearMonthDuration) shouldBe true

        FAny.ANY.canConvertTo(yearMonthDuration) shouldBe false
        NUMBER.canConvertTo(yearMonthDuration) shouldBe false
        INTEGER.canConvertTo(yearMonthDuration) shouldBe false
        DOUBLE.canConvertTo(yearMonthDuration) shouldBe false
        FString.STRING.canConvertTo(yearMonthDuration) shouldBe false
        FDate.DATE.canConvertTo(yearMonthDuration) shouldBe false
        FDateTime.DATE_TIME.canConvertTo(yearMonthDuration) shouldBe false
        FTime.TIME.canConvertTo(yearMonthDuration) shouldBe false
        FDayTimeDuration.DAY_TIME_DURATION.canConvertTo(yearMonthDuration) shouldBe false
        FList.ANY.canConvertTo(yearMonthDuration) shouldBe false
        FList.of(arrayOf("g", "gg")).canConvertTo(yearMonthDuration) shouldBe false
        FRange.of(INTEGER).canConvertTo(yearMonthDuration) shouldBe false
        FRange.ANY.canConvertTo(yearMonthDuration) shouldBe false
        FFunction.of(INTEGER, FContext.of()).canConvertTo(yearMonthDuration) shouldBe false
        FContext.EMPTY.canConvertTo(yearMonthDuration) shouldBe false
        FContext.of("age", INTEGER, "name", FString.STRING).canConvertTo(yearMonthDuration) shouldBe false
    }

    "test_2024_11_08_14_06_02" {
        FList.of(arrayOf("g", 2)) print JSON
        JSON print context("age" to INTEGER, "name" to FString.STRING)
    }
})

private fun context(vararg pairs: Pair<String, FType>): FContext {
    val map = pairs.toMap()
    // 假设FContext可以从一个Map初始化
    return FContext.of(map)
}

private infix fun <T> T.print(mode: PrintMode): T {
    when (mode) {
        JSON -> println(JSONUtil.toJSONString(this))
        else -> println(this)
    }
    return this
}

private infix fun <T> PrintMode.print(obj: T): T {
    when (this) {
        JSON -> println(JSONUtil.toJSONString(obj))
        else -> println(obj)
    }
    return obj
}

enum class PrintMode {
    JSON,
    NOW;
}