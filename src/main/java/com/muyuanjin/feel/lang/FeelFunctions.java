package com.muyuanjin.feel.lang;

import com.muyuanjin.common.util.LazyRef;
import com.muyuanjin.feel.lang.type.FContext;
import com.muyuanjin.feel.lang.type.FFunction;
import com.muyuanjin.feel.lang.type.FList;
import com.muyuanjin.feel.lang.type.FRange;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.muyuanjin.feel.lang.type.FAny.A;
import static com.muyuanjin.feel.lang.type.FAny.ANY;
import static com.muyuanjin.feel.lang.type.FBoolean.BOOLEAN;
import static com.muyuanjin.feel.lang.type.FDate.DATE;
import static com.muyuanjin.feel.lang.type.FDateTime.DATE_TIME;
import static com.muyuanjin.feel.lang.type.FDayTimeDuration.DAY_TIME_DURATION;
import static com.muyuanjin.feel.lang.type.FFunction.of;
import static com.muyuanjin.feel.lang.type.FNumber.INTEGER;
import static com.muyuanjin.feel.lang.type.FNumber.NUMBER;
import static com.muyuanjin.feel.lang.type.FString.STRING;
import static com.muyuanjin.feel.lang.type.FTime.TIME;
import static com.muyuanjin.feel.lang.type.FYearMonthDuration.YEAR_MONTH_DURATION;

/**
 * @author muyuanjin
 */
@Getter
public enum FeelFunctions {
    //Table 72: Semantics of conversion functions
    //表 72：转换函数
    date("date", of(DATE, STRING), of(DATE, DATE_TIME), of(DATE, INTEGER, INTEGER, INTEGER)),
    date_and_time("date and time", of(DATE_TIME, STRING), of(DATE_TIME, DATE, TIME)),
    time("time", of(TIME, STRING), of(TIME, DATE_TIME), of(TIME, INTEGER, INTEGER, INTEGER), of(TIME, INTEGER, INTEGER, DAY_TIME_DURATION)),
    number("number", of(NUMBER, STRING, STRING, STRING)),// from , grouping separator ,decimal separator，比如欧洲用.分割，用逗号当小数点
    string("string", of(STRING, ANY)),
    duration("duration", of(DAY_TIME_DURATION, STRING)),
    years_and_months_duration("years and months duration", of(YEAR_MONTH_DURATION, STRING), of(YEAR_MONTH_DURATION, DATE, DATE)),

    //Table 73: Semantics of Boolean functions
    //表 73：布尔函数
    not("not", of(BOOLEAN, BOOLEAN)),

    //Table 74: Semantics of string functions
    //表 74：字符串函数
    substring("substring", of(STRING, STRING, INTEGER), of(STRING, STRING, INTEGER, INTEGER)),
    string_length("string length", of(INTEGER, STRING)),
    upper_case("upper case", of(STRING, STRING)),
    lower_case("lower case", of(STRING, STRING)),
    substring_before("substring before", of(STRING, STRING, STRING)),
    substring_after("substring after", of(STRING, STRING, STRING)),
    replace("replace", of(STRING, STRING, STRING), of(STRING, STRING, STRING, STRING)),
    contains("contains", of(BOOLEAN, STRING, STRING)),
    starts_with("starts with", of(BOOLEAN, STRING, STRING)),
    ends_with("ends with", of(BOOLEAN, STRING, STRING)),
    matches("matches", of(BOOLEAN, STRING, STRING), of(BOOLEAN, STRING, STRING, STRING)),
    split("split", of(FList.of(STRING), STRING, STRING)),

    //Table 75: Semantics of list functions
    //表 75：列表函数
    list_contains("list contains", of(BOOLEAN, FList.of(A), A)),
    count("count", of(INTEGER, FList.of(ANY))),
    min("min", of(A, FList.of(A)), of(A, FList.ofVars(A))),
    max("max", of(A, FList.of(A)), of(A, FList.ofVars(A))),
    sum("sum", of(NUMBER, FList.of(NUMBER)), of(NUMBER, FList.ofVars(NUMBER))),
    mean("mean", of(NUMBER, FList.of(NUMBER)), of(NUMBER, FList.ofVars(NUMBER))),
    all("all", of(BOOLEAN, FList.of(BOOLEAN)), of(BOOLEAN, FList.ofVars(BOOLEAN))),
    any("any", of(BOOLEAN, FList.of(BOOLEAN)), of(BOOLEAN, FList.ofVars(BOOLEAN))),
    sublist("sublist", of(FList.of(A), FList.of(A), INTEGER), of(FList.of(A), FList.of(A), INTEGER, INTEGER)),
    append("append", of(FList.of(A), FList.of(A), FList.ofVars(A))),
    concatenate("concatenate", of(FList.of(A), FList.ofVars(FList.of(A)))),
    insert_before("insert before", of(FList.of(A), FList.of(A), INTEGER, A)),
    remove("remove", of(FList.of(A), FList.of(A), INTEGER)),
    reverse("reverse", of(FList.of(A), FList.of(A))),
    index_of("index of", of(INTEGER, FList.of(A), A)),
    union("union", of(FList.of(A), FList.ofVars(FList.of(A)))),
    distinct_values("distinct values", of(FList.of(A), FList.of(A))),
    flatten("flatten", of(FList.of(ANY), FList.of(ANY))),
    product("product", of(NUMBER, FList.of(NUMBER)), of(NUMBER, FList.ofVars(NUMBER))),
    median("median", of(NUMBER, FList.of(NUMBER)), of(NUMBER, FList.ofVars(NUMBER))),
    @SuppressWarnings("SpellCheckingInspection")
    stddev("stddev", of(NUMBER, FList.of(NUMBER)), of(NUMBER, FList.ofVars(NUMBER))),
    mode("mode", of(FList.of(NUMBER), FList.of(NUMBER)), of(FList.of(NUMBER), FList.ofVars(NUMBER))),

    //Table 76: Semantics of numeric functions
    //表 76：数值函数
    decimal("decimal", of(NUMBER, NUMBER, INTEGER)),
    floor("floor", of(INTEGER, NUMBER)),
    ceiling("ceiling", of(INTEGER, NUMBER)),
    abs("abs", of(NUMBER, NUMBER), of(DAY_TIME_DURATION, DAY_TIME_DURATION), of(YEAR_MONTH_DURATION, YEAR_MONTH_DURATION)),
    modulo("modulo", of(NUMBER, NUMBER, NUMBER)),
    sqrt("sqrt", of(NUMBER, NUMBER)),
    log("log", of(NUMBER, NUMBER)),//TODO 任意底数?
    exp("exp", of(NUMBER, NUMBER)),
    odd("odd", of(BOOLEAN, NUMBER)),
    even("even", of(BOOLEAN, NUMBER)),

    //Table 77: Semantics of date and time functions
    //表 77：日期和时间函数
    is("is", of(BOOLEAN, "value1", A, "value2", A)),

    //Table 78: Semantics of range functions
    // 范围函数
    before("before", of(BOOLEAN, A, A), of(BOOLEAN, A, FRange.of(A)), of(BOOLEAN, FRange.of(A), A), of(BOOLEAN, FRange.of(A), FRange.of(A))),
    after("after", of(BOOLEAN, A, A), of(BOOLEAN, A, FRange.of(A)), of(BOOLEAN, FRange.of(A), A), of(BOOLEAN, FRange.of(A), FRange.of(A))),
    meets("meets", of(BOOLEAN, FRange.of(A), FRange.of(A))),
    met_by("met by", of(BOOLEAN, FRange.of(A), FRange.of(A))),
    overlaps("overlaps", of(BOOLEAN, FRange.of(A), FRange.of(A))),
    overlaps_before("overlaps before", of(BOOLEAN, FRange.of(A), FRange.of(A))),
    overlaps_after("overlaps after", of(BOOLEAN, FRange.of(A), FRange.of(A))),
    finishes("finishes", of(BOOLEAN, A, FRange.of(A)), of(BOOLEAN, FRange.of(A), FRange.of(A))),
    finished_by("finished by", of(BOOLEAN, FRange.of(A), A), of(BOOLEAN, FRange.of(A), FRange.of(A))),
    starts("starts", of(BOOLEAN, A, FRange.of(A)), of(BOOLEAN, FRange.of(A), FRange.of(A))),
    started_by("started by", of(BOOLEAN, FRange.of(A), A), of(BOOLEAN, FRange.of(A), FRange.of(A))),
    coincides("coincides", of(BOOLEAN, A, A), of(BOOLEAN, FRange.of(A), FRange.of(A))),

    //Table 79: Temporal built-in functions
    //表 79：时间内置函数
    day_of_year("day of year", of(INTEGER, DATE), of(INTEGER, DATE_TIME)),
    day_of_week("day of week", of(INTEGER, DATE), of(INTEGER, DATE_TIME)),
    month_of_year("month of year", of(STRING, DATE), of(STRING, DATE_TIME)),//TODO 返回月数？
    week_of_year("week of year", of(INTEGER, DATE), of(INTEGER, DATE_TIME)),

    //Table 80: Semantics of sort functions
    //表 80：排序函数
    sort("sort",
            of(FList.of(A), "list", FList.of(A)),// 自然排序
            of(FList.of(A), "list", FList.ofVars(A)),// 自然排序
            of(FList.of(A), "list", FList.of(A), "precedes", of(BOOLEAN, A, A))
    ),

    //Table 81: Semantics of Context functions
    //表 81：上下文函数
    get_value("get value", of(ANY, "context", FContext.ANY, "key", STRING)),
    get_entries("get entries", of(FList.of(FContext.of("key", STRING, "value", ANY)), "context", FContext.ANY)),

    ;
    private final String name;
    private final List<FFunction> functions;

    FeelFunctions(String name, FFunction... functions) {
        this.name = name;
        this.functions = List.of(functions);
    }

    public FFunction getFunction(int index) {
        return functions.get(index);
    }

    private static final LazyRef<Map<String, FeelFunctions>> FUNCTIONS = LazyRef.of(() -> Stream.of(values()).collect(Collectors.toMap(FeelFunctions::getName, f -> f)));

    public static FeelFunctions from(String name) {
        return FUNCTIONS.get().get(name);
    }
}
