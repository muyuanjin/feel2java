package com.muyuanjin.common.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author muyuanjin
 */
@UtilityClass
public class DateUtil {
    /**
     * 每天小时。
     */
    public static final int HOURS_PER_DAY = 24;
    /**
     * 每小时分钟。
     */
    public static final int MINUTES_PER_HOUR = 60;
    /**
     * 每天分钟。
     */
    public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    /**
     * 每分钟秒数。
     */
    public static final int SECONDS_PER_MINUTE = 60;
    /**
     * 每小时秒数。
     */
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * 每天秒数。
     */
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    /**
     * 每天毫秒。
     */
    public static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L;
    /**
     * 每小时毫秒。
     */
    public static final long MILLIS_PER_HOURS = SECONDS_PER_HOUR * 1000L;
    /**
     * 每分钟毫秒。
     */
    public static final long MILLIS_PER_MINUTES = SECONDS_PER_MINUTE * 1000L;

    /**
     * 每秒纳秒
     */
    public static final long NANO_PER_SECOND = 1_000_000_000L;

    public static final String DEFAULT_LOCALE = "zh_CN";
    public static final Locale DEFAULT_LOCALE_INSTANCE = Locale.SIMPLIFIED_CHINESE;
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT+8");
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.of("+8");
    public static final String DEFAULT_LOCAL_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_LOCAL_TIME_FORMAT_PATTERN = "HH:mm:ss";
    public static final String DEFAULT_LOCAL_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Map<String, DateTimeFormatter> DATE_FORMAT_CACHE = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = getDateTimeFormat(DEFAULT_DATE_FORMAT_PATTERN);


    public static String format(TemporalAccessor time) {
        return time == null ? null : DEFAULT_DATE_FORMAT.format(time);
    }

    public static String format(TemporalAccessor time, String dateFormatPattern) {
        return time == null ? null : getDateTimeFormat(dateFormatPattern).format(time);
    }


    public static String format(Instant instant) {
        return instant == null ? null : format(LocalDateTime.ofInstant(instant, DEFAULT_ZONE_OFFSET));
    }

    public static String format(Instant instant, String dateFormatPattern) {
        return instant == null ? null : format(LocalDateTime.ofInstant(instant, DEFAULT_ZONE_OFFSET), dateFormatPattern);
    }


    public static String format(Long time) {
        return time == null ? null : format(Instant.ofEpochMilli(time));
    }

    public static String format(Long time, String dateFormatPattern) {
        return time == null ? null : format(Instant.ofEpochMilli(time), dateFormatPattern);
    }

    public static String format(Date time) {
        return time == null ? null : format(time.toInstant());
    }

    public static String format(Date time, String dateFormatPattern) {
        return time == null ? null : format(time.toInstant(), dateFormatPattern);
    }

    public static LocalDate parseToLocalDate(String time, String dateFormatPattern) {
        return time == null ? null : LocalDate.from(getDateTimeFormat(dateFormatPattern).parse(time));
    }

    public static LocalTime parseToLocalTime(String time, String dateFormatPattern) {
        return time == null ? null : LocalTime.from(getDateTimeFormat(dateFormatPattern).parse(time));
    }

    /**
     * 字符串转LocalDateTime
     */
    public static LocalDateTime parseToLocalDateTime(String time) {
        return time == null ? null : LocalDateTime.from(DEFAULT_DATE_FORMAT.parse(time));
    }

    public static LocalDateTime parseToLocalDateTime(String time, String dateFormatPattern) {
        return time == null ? null : LocalDateTime.from(getDateTimeFormat(dateFormatPattern).parse(time));
    }

    public static Instant parseToInstant(String time) {
        return time == null ? null : parseToLocalDateTime(time).toInstant(DEFAULT_ZONE_OFFSET);
    }

    public static Instant parseToInstant(String time, String dateFormatPattern) {
        return time == null ? null : parseToLocalDateTime(time, dateFormatPattern).toInstant(DEFAULT_ZONE_OFFSET);
    }

    public static Long parseToLong(String time) {
        return time == null ? null : parseToInstant(time).toEpochMilli();
    }

    public static Long parseToLong(String time, String dateFormatPattern) {
        return time == null ? null : parseToInstant(time, dateFormatPattern).toEpochMilli();
    }

    public static Date parseToDate(String time) {
        return time == null ? null : Date.from(parseToInstant(time));
    }

    public static Date parseToDate(String time, String dateFormatPattern) {
        return time == null ? null : Date.from(parseToInstant(time, dateFormatPattern));
    }

    public static Date toDate(LocalDate time) {
        return time == null ? null : Date.from(time.atStartOfDay(DEFAULT_ZONE_OFFSET).toInstant());
    }

    public static Date toDate(LocalDateTime time) {
        return time == null ? null : Date.from(time.toInstant(DEFAULT_ZONE_OFFSET));
    }

    public static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(DEFAULT_ZONE_OFFSET).toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        return date == null ? null : date.toInstant().atZone(DEFAULT_ZONE_OFFSET).toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_OFFSET);
    }

    /**
     * 毫秒时间
     * Long类型时间转换成视频时长
     */
    public static String formatToDuration(Long time) {
        if (time == null) {
            return null;
        }
        long hour = time / (MILLIS_PER_HOURS);
        long minute = (time - hour * MILLIS_PER_HOURS) / (MILLIS_PER_MINUTES);
        long second = (time - hour * MILLIS_PER_HOURS - minute * MILLIS_PER_MINUTES) / 1000L;
        StringBuilder builder = new StringBuilder();
        formatToDoubleCharacter(builder, hour).append(":");
        formatToDoubleCharacter(builder, minute).append(":");
        formatToDoubleCharacter(builder, second);
        return builder.toString();
    }

    private StringBuilder formatToDoubleCharacter(StringBuilder builder, long time) {
        if (time < 0) {
            time = Math.abs(time);
        }
        if (time == 0) {
            builder.append("00");
        } else if (time <= 10L) {
            builder.append("0").append(time);
        } else {
            builder.append(time);
        }
        return builder;
    }

    /**
     * 将时长格式化为X天X分X时X秒
     * 例如：
     * <pre>
     * DateUtil.formatToText(Duration.parse("PT20H4M3S"));      20小时4分钟
     * DateUtil.formatToText(Duration.parse("PT20H0M3S"));      20小时
     * DateUtil.formatToText(Duration.parse("PT3M3S"));         3分钟3秒
     * DateUtil.formatToText(Duration.parse("PT3M0S"));         3分钟
     * DateUtil.formatToText(Duration.parse("PT0M3S"));         3秒
     * DateUtil.formatToText(Duration.parse("PT3S"));           3秒
     * DateUtil.formatToText(Duration.parse("P2DT20H4M3S"));    2天20小时
     * DateUtil.formatToText(Duration.parse("P2DT3H4M"));       2天3小时
     * DateUtil.formatToText(Duration.parse("P2D"));            2天
     * </pre>
     * 另外 24小时，36小时，48小时，72小时会特别显示
     */
    public static String formatToReaderFriendlyText(Duration duration) {
        if (duration == null) {
            return null;
        }
        long allSeconds = duration.getSeconds();
        long days = duration.toDays();
        long hour = (allSeconds - days * SECONDS_PER_DAY) / SECONDS_PER_HOUR;
        long minute = (allSeconds - days * SECONDS_PER_DAY - hour * SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long second = allSeconds - days * SECONDS_PER_DAY - hour * SECONDS_PER_HOUR - minute * SECONDS_PER_MINUTE;
        StringBuilder builder = new StringBuilder();
        long allHours = duration.toHours();
        if ((allHours == 72 || allHours == 48 || allHours == 36 || allHours == 24) && minute <= 0) {
            builder.append(allHours).append("小时");
            return builder.toString();
        }
        if (days > 0) {
            builder.append(days).append("天");
        }
        if (hour > 0) {
            builder.append(hour).append("小时");
        }
        if (minute > 0 && days <= 0) {
            builder.append(minute).append("分钟");
        }
        if (second > 0 && hour <= 0 && days <= 0) {
            builder.append(second).append("秒");
        }
        return builder.toString();
    }


    public static DateTimeFormatter getDateTimeFormat(String dateTimeFormatPattern) {
        //JDK8 ConcurrentHashMap.computeIfAbsent()的并发 Bug
        DateTimeFormatter dateTimeFormatter = DATE_FORMAT_CACHE.get(dateTimeFormatPattern);
        if (dateTimeFormatter != null) {
            return dateTimeFormatter;
        }
        return DATE_FORMAT_CACHE.computeIfAbsent(dateTimeFormatPattern, e -> DateTimeFormatter.ofPattern(e, Locale.CHINA).withZone(DEFAULT_ZONE_OFFSET));
    }
}
