package com.muyuanjin.feel.lang;

import com.muyuanjin.feel.lang.type.*;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author muyuanjin
 */
@UtilityClass
public class FTypes {
    //@formatter:off
    public static final FAny               ANY                  =  FAny.ANY;
    public static final FNull              NULL                 =  FNull.NULL;
    public static final FNumber            INTEGER              =  FNumber.INTEGER;
    public static final FNumber            LONG                 =  FNumber.LONG;
    public static final FNumber            DOUBLE               =  FNumber.DOUBLE;
    public static final FNumber            BIG_DECIMAL          =  FNumber.BIG_DECIMAL;
    public static final FNumber            NUMBER               =  FNumber.NUMBER;
    public static final FString            STRING               =  FString.STRING;
    public static final FBoolean           BOOLEAN              =  FBoolean.BOOLEAN;
    public static final FTime              TIME                 =  FTime.TIME;
    public static final FDate              DATE                 =  FDate.DATE;
    public static final FDateTime          DATE_TIME            =  FDateTime.DATE_TIME;
    public static final FDayTimeDuration   DAY_TIME_DURATION    =  FDayTimeDuration.DAY_TIME_DURATION;
    public static final FYearMonthDuration YEAR_MONTH_DURATION  =  FYearMonthDuration.YEAR_MONTH_DURATION;

    public static final FList              LIST_ANY             =  FList.ANY;
    public static final FContext           CONTEXT_EMPTY        =  FContext.EMPTY;
    public static final FRange             RANGE_ANY            =  FRange.ANY;
    public static final FFunction          FUNCTION_ANY         =  FFunction.ANY;
    //@formatter:on
    // miss List, Context, Range, Function
    public static final List<FType> ENUMS = List.of(
            ANY, NULL, NUMBER, STRING, BOOLEAN,
            TIME, DATE, DATE_TIME, DAY_TIME_DURATION, YEAR_MONTH_DURATION
    );
    public static final Map<String, FType> ENUMS_MAP = ENUMS.stream().collect(Collectors.toUnmodifiableMap(FType::getName, t -> t));
}
