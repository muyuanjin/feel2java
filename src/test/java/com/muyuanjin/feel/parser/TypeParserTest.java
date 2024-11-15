package com.muyuanjin.feel.parser;

import com.muyuanjin.feel.util.JSONUtil;
import com.muyuanjin.feel.exception.FeelLangException;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.*;
import com.muyuanjin.feel.util.BenchmarkUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author muyuanjin
 */
class TypeParserTest {
    @Test
    @SneakyThrows
    void test_2024_11_04_16_36_50() {
        Assertions.assertEquals(FNumber.NUMBER, TypeParser.make().parseType("number"));
        Assertions.assertEquals(FNumber.NUMBER, TypeParser.make().parseType("number "));
        Assertions.assertThrows(FeelLangException.class, () -> TypeParser.make().parseType("number1"));
        Assertions.assertThrows(FeelLangException.class, () -> TypeParser.make().parseType("number 1"));
        Assertions.assertEquals(FAny.ANY, TypeParser.make().parseType("any"));
        Assertions.assertEquals(FAny.ANY, TypeParser.make().parseType("any "));
        Assertions.assertThrows(FeelLangException.class, () -> TypeParser.make().parseType("any1"));
        Assertions.assertThrows(FeelLangException.class, () -> TypeParser.make().parseType("any 1"));
        Assertions.assertEquals(FBoolean.BOOLEAN, TypeParser.make().parseType("boolean"));
        Assertions.assertEquals(FDate.DATE, TypeParser.make().parseType("date"));
        Assertions.assertEquals(FDateTime.DATE_TIME, TypeParser.make().parseType("date and time"));
        Assertions.assertEquals(FDateTime.DATE_TIME, TypeParser.make().parseType("date   and  time"));
        Assertions.assertEquals(FDayTimeDuration.DAY_TIME_DURATION, TypeParser.make().parseType("day and time duration"));
        Assertions.assertEquals(FDayTimeDuration.DAY_TIME_DURATION, TypeParser.make().parseType("day  and   time duration"));
        Assertions.assertEquals(FNull.NULL, TypeParser.make().parseType("null"));
        Assertions.assertEquals(FNumber.NUMBER, TypeParser.make().parseType("number"));
        Assertions.assertEquals(FString.STRING, TypeParser.make().parseType("string"));
        Assertions.assertEquals(FTime.TIME, TypeParser.make().parseType("time"));
        Assertions.assertEquals(FYearMonthDuration.YEAR_MONTH_DURATION, TypeParser.make().parseType("year and month duration"));
        Assertions.assertEquals(FYearMonthDuration.YEAR_MONTH_DURATION, TypeParser.make().parseType("year   and  month  duration"));
        Assertions.assertThrows(FeelLangException.class, () -> TypeParser.make().parseType("year     month  duration"));

        Assertions.assertEquals(FList.of(FString.STRING), TypeParser.make().parseType("list<string>"));
        Assertions.assertEquals(FList.of(FNumber.NUMBER), TypeParser.make().parseType("list<number>"));
        Assertions.assertEquals(FList.of(FAny.ANY), TypeParser.make().parseType("list<any>"));
        Assertions.assertEquals(FList.of(FYearMonthDuration.YEAR_MONTH_DURATION), TypeParser.make().parseType("list<year and month duration>"));
        Assertions.assertEquals(FList.of(FYearMonthDuration.YEAR_MONTH_DURATION), TypeParser.make().parseType("list<year   and  month  duration>"));


        Assertions.assertEquals(FRange.of(FNumber.NUMBER), TypeParser.make().parseType("range<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, true, null), TypeParser.make().parseType("range[<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, false, null), TypeParser.make().parseType("range(<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, null, true), TypeParser.make().parseType("range]<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, null, false), TypeParser.make().parseType("range)<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, true, true), TypeParser.make().parseType("range[]<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, true, false), TypeParser.make().parseType("range[)<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, false, true), TypeParser.make().parseType("range(]<number>"));
        Assertions.assertEquals(FRange.of(FNumber.NUMBER, false, false), TypeParser.make().parseType("range()<number>"));

        Assertions.assertEquals(FContext.of(), TypeParser.make().parseType("context<>"));
        Assertions.assertEquals(FContext.of("a", FAny.ANY), TypeParser.make().parseType("context<a:any>"));
        Assertions.assertEquals(FContext.of("a", FString.STRING), TypeParser.make().parseType("context<a:string>"));
        Assertions.assertEquals(FContext.of("a", FNumber.NUMBER), TypeParser.make().parseType("context<a:number>"));
        Assertions.assertEquals(FContext.of("a", FList.of(FString.STRING)), TypeParser.make().parseType("context<a:list<string>>"));
        Assertions.assertEquals(FContext.of("a", FContext.of("a", FAny.ANY)), TypeParser.make().parseType("context<a:context<a:any>>"));


        Assertions.assertEquals(FFunction.of(FAny.ANY), TypeParser.make().parseType("function<>->any"));
        Assertions.assertEquals(FFunction.of(FAny.ANY, FAny.ANY), TypeParser.make().parseType("function<any>->any"));
        Assertions.assertEquals(FFunction.of(FContext.of("a", FContext.of("a", FAny.ANY)), FAny.ANY, FBoolean.BOOLEAN), TypeParser.make().parseType("function<any,boolean>->context<a:context<a:any>>"));
        Assertions.assertEquals(FFunction.of(FAny.ANY, FAny.ANY, FString.STRING, FList.of(FString.STRING)), TypeParser.make().parseType("function<any,string,list<string>>->any"));
        Assertions.assertEquals(FFunction.of(FAny.ANY, FAny.ANY, FNumber.NUMBER, FAny.ANY, FAny.ANY), TypeParser.make().parseType("function<any,number,any,any>->any"));
    }

    @Test
    @SneakyThrows
    void test_2024_11_04_17_06_21() {
        BenchmarkUtil.benchmark1000(() -> TypeParser.make().parseType("number"));
        BenchmarkUtil.benchmark1000(() -> TypeParser.make().parseType("range<number>"));
        BenchmarkUtil.benchmark1000(() -> TypeParser.make().parseType("function<>->any"));
        BenchmarkUtil.benchmark1000(() -> TypeParser.make().parseType("list<year and month duration>"));
        BenchmarkUtil.benchmark1000(() -> TypeParser.make().parseType("function<any,boolean>->context<a:context<a:any>>"));
    }

    @Test
    @SneakyThrows
    void test_2024_11_05_08_39_23() {
        Assertions.assertEquals(FList.of(FList.of(FString.STRING)), FList.of(FList.of(FString.STRING)));
        Assertions.assertEquals(FList.of(FList.of(FString.STRING)), JSONUtil.parseObject(JSONUtil.toJSONString(FList.of(FList.of(FString.STRING))), FType.class));
        System.out.println(JSONUtil.parseObject(JSONUtil.toJSONString(FList.of(FList.of(FString.STRING))), FType.class));
    }
}