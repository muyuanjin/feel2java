package com.muyuanjin.feel.parser;

import com.muyuanjin.common.util.MapUtil;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.FeelRange;
import com.muyuanjin.feel.lang.type.FAny;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import jakarta.validation.constraints.NotNull;

import javax.lang.model.SourceVersion;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.chrono.ChronoPeriod;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.*;

import static com.muyuanjin.feel.lang.type.FDate.DATE;
import static com.muyuanjin.feel.lang.type.FDateTime.DATE_TIME;
import static com.muyuanjin.feel.lang.type.FDayTimeDuration.DAY_TIME_DURATION;
import static com.muyuanjin.feel.lang.type.FTime.TIME;
import static com.muyuanjin.feel.lang.type.FYearMonthDuration.YEAR_MONTH_DURATION;
import static java.time.temporal.ChronoField.*;

/**
 * @author muyuanjin
 */
@UtilityClass
public class ParserUtil {
    public static final ClassValue<Map<String, Method>> PROPERTY_READ = new ClassValue<>() {
        @Override
        @SneakyThrows
        protected Map<String, Method> computeValue(Class<?> type) {
            var descriptors = (type.isInterface() || type.isPrimitive()) ?
                    Introspector.getBeanInfo(type).getPropertyDescriptors() :
                    Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors();
            var components = type.getRecordComponents();
            if (descriptors.length == 0 && components == null) {
                return Map.of();
            }

            LinkedHashMap<String, Method> map = MapUtil.newLinkedHashMap(components != null ? descriptors.length + components.length : descriptors.length);
            if (components != null) {
                for (var component : components) {
                    map.put(component.getName(), component.getAccessor());
                }
            }
            for (var descriptor : descriptors) {
                Method readMethod = descriptor.getReadMethod();
                if (readMethod != null) {
                    map.put(descriptor.getName(), readMethod);
                }
            }
            return Collections.unmodifiableMap(map);
        }
    };


    private static final String NAN = Double.toString(Double.NaN);
    private static final String INF = Double.toString(Double.POSITIVE_INFINITY);
    private static final String NEG_INF = Double.toString(Double.NEGATIVE_INFINITY);

    /**
     * 返回java规范下字面量字面意思的准确值，避免java字面量受限于IEEE754导致的不准确和精度问题<p>
     * 避免 0.1 + 0.2 = 0.30000000000000004 问题<p>
     * 避免 11111111111111111d = 11111111111111112 而不是 11111111111111111 问题
     */
    public static BigDecimal parserJavaNumber(String numeric) {
        if (numeric == null || numeric.isEmpty()) {
            return null;
        }
        if (NAN.equals(numeric) || INF.equals(numeric) || NEG_INF.equals(numeric)) {
            return null;
        }
        if ("0".equals(numeric)) {
            return BigDecimal.ZERO;
        }

        int radix = 10;
        int floatIndex = -1;
        int exponentIndex = -1;

        char[] chars = numeric.toCharArray();
        int length = chars.length;
        int from = 0;
        int to = 0;
        boolean startWithZero = false;
        while (from < length) {
            char c = chars[from++];
            switch (c) {
                case 'x', 'X' -> radix = 16;
                case 'b', 'B' -> radix = 2;
                case 'e', 'E', 'p', 'P' -> exponentIndex = to;
                case '.' -> {
                    floatIndex = to;
                    if (radix == 16) {//16进制一定更早出现，而2或8进制只存在于没有小数点的整数规范
                        continue;//16进制后续手动计算小数
                    }
                }
                case 'l', 'L', 'd', 'D', 'f', 'F', '_' -> {
                    continue;
                }
                case '0' -> startWithZero = to == 0;
            }
            chars[to++] = c;
        }
        //只在整数规范下有8进制
        if (startWithZero && radix == 10 && floatIndex == -1 && exponentIndex == -1) {
            radix = 8;
        }

        if (radix == 10) {
            // BigDecimal 可以处理十进制整数、小数、科学计数
            return new BigDecimal(new String(chars, 0, to));
        }
        int offset = radix == 8 ? 1 : 2;

        String numberText = new String(chars, offset, to - offset - (exponentIndex == -1 ? 0 : to - exponentIndex));
        BigDecimal bigDecimal = new BigDecimal(new BigInteger(numberText, radix));
        if (floatIndex != -1) {
            int scale = exponentIndex == -1 ? to - floatIndex - 1 : exponentIndex - floatIndex;
            // 2 8 16 进制的小数 是以2为底的分母 均可以被十进制完整表示，不需要舍入
            //noinspection BigDecimalMethodWithoutRoundingCalled
            bigDecimal = bigDecimal.divide(BigDecimal.valueOf(radix).pow(scale));
        }

        // 16进制不使用 Double.parseDouble 防止准确度和精度损失
        if (exponentIndex != -1) {
            int exponent = Integer.parseInt(new String(chars, exponentIndex + 1, to - exponentIndex - 1));
            //根据语法规范，此时只会有十六进制，其指数为2的幂次
            bigDecimal = bigDecimal.multiply(BigDecimal.valueOf(2).pow(exponent));
        }
        return bigDecimal;
    }

    private static final BigInteger FIVE = BigInteger.valueOf(5);
    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);
    private static final BigDecimal MIN_DOUBLE = BigDecimal.valueOf(Double.MIN_NORMAL);

    /**
     * 能否以 IEEE-754 二进制浮点数完整表示该值<p>
     * 如果不能
     * <pre>{@code
     *      BigDecimal bigDecimal = EvalUtil.parserJavaNumber("0.1");
     *      bigDecimal.equals(new BigDecimal(bigDecimal.doubleValue())); //false
     * }</pre>
     */
    public static boolean canExactlyBeDouble(BigDecimal value) {
        int signum = value.signum();
        if (signum == 0) {
            return true;// is 0
        }
        value = value.abs();
        // 检查是否超过最大最小值范围
        if (value.compareTo(MAX_DOUBLE) > 0 || value.compareTo(MIN_DOUBLE) < 0) {
            return false;
        }
        int scale = value.scale();//小数位数
        // 判断是否为整数，如果是整数，不用接着判断小数的部分了
        //https://stackoverflow.com/questions/1078953/check-if-bigdecimal-is-an-integer-in-java
        if (scale <= 0 || (scale = (value = value.stripTrailingZeros()).scale()) <= 0) {
            BigInteger integer = scale == 0 ? value.unscaledValue() : value.unscaledValue().multiply(BigInteger.TEN.pow(-scale));
            // 判断指数位数是否超过IEEE Double 二进制浮点数的指数位数上限
            int lowestSetBit = integer.getLowestSetBit();
            if (lowestSetBit > (signum < 0 ? -Double.MIN_EXPONENT : Double.MAX_EXPONENT)) {
                return false;
            }
            // 超过53位的IEEE Double 二进制尾数位数上限,一定损失精度
            return integer.bitLength() - lowestSetBit <= 53;
        }
        BigInteger molecule = value.unscaledValue();//分子 = unscaledValue， 分母 = 10^scale
        // 检查一个小数能否被IEEE规定的二进制浮点数完整表示，取决于其分数形式约分后的分母是否为2的幂次
        // 又因为十进制下，分母是10的n幂次，所以只需检查分子是否能被5的n次幂整除，n即 value.scale()
        BigInteger[] bigIntegers = molecule.divideAndRemainder(FIVE.pow(scale));
        BigInteger quotient = bigIntegers[0];// 商, value = 商/2^scale
        if (!bigIntegers[1].equals(BigInteger.ZERO)) {
            // 有余数说明不能被5的n次幂整除，一定损失精度
            return false;
        }
        // 判断指数位数是否超过IEEE Double 二进制浮点数的指数位数上限
        // 商本身的2的因数个数 + scale 即为 value 的 IEEE 指数位数
        int lowestSetBit = quotient.getLowestSetBit();
        if ((lowestSetBit + scale) > (signum < 0 ? -Double.MIN_EXPONENT : Double.MAX_EXPONENT)) {
            return false;
        }
        // 判断有效位数是否过多
        return quotient.bitLength() - lowestSetBit <= 53;
    }

    public static String removeLeadingZero(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        // 去除可能的前导零
        int from = 0;
        while (from < str.length() - 1 && str.charAt(from) == '0') {
            from++;
        }
        return str.substring(from);
    }

    public static FType determineAtType(String value) {
        if (value == null || value.isEmpty()) {
            return FAny.ANY;
        }
        boolean startP = false;
        boolean seenT = false;
        boolean seenDash = false;
        boolean seenColon = false;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            // 忽略@后的内容，因为时区信息可能包含其他字符
            if (c == '@') {
                break; // 不再处理时区信息后的字符
            }
            switch (c) {
                case 'P' -> startP |= i == 0 || (i == 1 && value.charAt(0) == '-');
                case 'T' -> {
                    seenT = true;
                    i = length;
                }
                case '-' -> seenDash = true;
                case ':' -> seenColon = true;
            }
        }
        // 根据收集的信息判断类型
        if (startP) {
            return seenT ? DAY_TIME_DURATION : YEAR_MONTH_DURATION;
        } else if (seenT) {
            return DATE_TIME;
        } else if (seenColon) {
            return TIME;
        } else if (seenDash) {
            return DATE;
        }
        return FAny.ANY;
    }

    /**
     * 使用 Java 字符串规则转义 {@code String} 中的字符<p>
     * 正确处理引号和控制字符（制表符、反斜杠、cr、ff 等）<p>
     * 示例:<p>
     * <pre>
     * 输入字符串: He didn't say, "Stop!"
     * 输出字符串: He didn't say, \"Stop!\"
     * </pre>
     *
     * @param value 用于转义的字符串，可以为空
     * @return 带转义值的字符串，{@code null} 如果输入字符串为空
     */
    public static String escapeJava(String value, boolean quoted) {
        if (value == null) {
            return null;
        }
        int newLength = quoted ? value.length() + 2 : value.length();
        boolean needEscape = false;
        // 由于需要转义的字符串通常不会超过100个字符，两次循环比StringBuilder更有优势
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\b', '\f', '\n', '\r', '\t', '\"', '\\' -> {
                    newLength++;
                    needEscape = true;
                }
                default -> {
                    if (Character.isISOControl(c)) {
                        needEscape = true;
                        //将ASCII中的不可见字符转义为 八进制转义
                        if (c < 64) {
                            newLength += (c < 8) ? 1 : 2;
                        } else {
                            newLength += 3;
                        }
                    }
                }
            }
        }
        if (!needEscape) {
            return quoted ? "\"" + value + "\"" : value;
        }
        char[] buffer = new char[newLength];
        int index = 0;
        if (quoted) {
            buffer[index++] = '\"';
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\b' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = 'b';
                }
                case '\f' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = 'f';
                }
                case '\n' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = 'n';
                }
                case '\r' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = 'r';
                }
                case '\t' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = 't';
                }
                case '\"' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = '\"';
                }
                case '\\' -> {
                    buffer[index++] = '\\';
                    buffer[index++] = '\\';
                }
                default -> {
                    if (Character.isISOControl(c)) {
                        //将ASCII中的不可见字符转义为 八进制转义
                        buffer[index++] = '\\';
                        if (c < 64) {
                            if (c < 8) {
                                buffer[index++] = (char) ('0' + c);
                            } else {
                                buffer[index++] = (char) ('0' + c / 8);
                                buffer[index++] = (char) ('0' + c % 8);
                            }
                        } else {
                            buffer[index++] = (char) ('0' + c / 64);
                            buffer[index++] = (char) ('0' + c / 8 % 8);
                            buffer[index++] = (char) ('0' + c % 8);
                        }
                    } else {
                        buffer[index++] = c;
                    }
                }
            }
        }
        if (quoted) {
            buffer[index] = '\"';
        }
        return new String(buffer);
    }


    /**
     * 将FEEL字符串字面量的原始值中的转义字符替换为真实字符串对象，类似于{@link String#translateEscapes}
     *
     * @param text 字符串字面量的原始值，包括双引号
     * @return 转义字符被替换后的字符串
     */
    public static String translateEscapes(String text) {
        if (text == null) {
            return null;
        }
        if (text.isEmpty()) {
            return "";
        }
        char[] chars = text.toCharArray();
        int length = chars.length;
        int from = 0;
        int to = 0;
        // 如果被双引号包围，掐头去尾
        if (length >= 2 && chars[0] == '\"' && chars[length - 1] == '\"') {
            from = 1;
            length--;
        }
        while (from < length) {
            char ch = chars[from++];
            if (ch == '\\') {
                if (from < length) {
                    ch = chars[from++];
                } else {
                    chars[to++] = '\\'; // 将之前跳过的'\'加回来
                    break;
                }
                switch (ch) {
                    case 'b' -> ch = '\b';
                    case 'f' -> ch = '\f';
                    case 'n' -> ch = '\n';
                    case 'r' -> ch = '\r';
                    case 't' -> ch = '\t';
                    case '\'', '\"', '\\' -> {
                        // as is
                    }
                    case 'u' -> {
                        int codePoint = parseUnicodeCodePoint(chars, from, 4);
                        //4位十六进制数一定在0x0000到0xFFFF之间
                        if (codePoint != -1) {
                            ch = (char) codePoint;
                            from += 4; // 移动过已解析的字符
                        } else {
                            // 如果不是有效的十六进制数字，保留原样
                            chars[to++] = '\\'; // 将之前跳过的'\'加回来
                        }
                    }
                    case 'U' -> {
                        int codePoint = parseUnicodeCodePoint(chars, from, 6);
                        //必须 大于 0 小于 0x10FFFF
                        if (Character.isValidCodePoint(codePoint)) {
                            from += 6; // 移动过已解析的字符
                            if (Character.isSupplementaryCodePoint(codePoint)) {
                                char[] unicodePair = Character.toChars(codePoint);
                                chars[to++] = unicodePair[0];
                                ch = unicodePair[1];
                            } else {
                                ch = (char) codePoint;
                            }
                        } else {
                            // 如果不是有效的十六进制 Unicode，保留原样
                            chars[to++] = '\\'; // 将之前跳过的'\'加回来
                        }
                    }
                    case '\n' -> {
                        continue;
                    }
                    case '\r' -> {
                        if (from < length && chars[from] == '\n') {
                            from++;
                        }
                        continue;
                    }
                    default -> chars[to++] = '\\'; // 将之前跳过的'\'加回来
                }
            }
            chars[to++] = ch;
        }
        return new String(chars, 0, to);
    }

    // 解析Unicode码点
    private static int parseUnicodeCodePoint(char[] chars, int from, int length) {
        if (from + length > chars.length) {
            return -1; // 超出范围
        }
        int codePoint = 0;
        for (int i = 0; i < length; i++) {
            // 检查是否是十六进制数字(0-9, A-F, a-f
            int digit = Character.digit(chars[from + i], 16);
            if (digit == -1) {
                return -1; // 不是有效的十六进制数字
            }
            codePoint = (codePoint << 4) | digit;
        }
        return codePoint;
    }

    /**
     * 将ID中的转义都替换为_
     *
     * @param escapedIdentifier {@link #escapeIdentifier} 的结果
     */
    public static String removeEscape(String escapedIdentifier) {
        char[] chars = escapedIdentifier.toCharArray();
        int length = chars.length;
        int from = 0;
        int to = 0;
        int lastUnderline = 0;
        boolean ignore = false;
        while (from < length) {
            char ch = chars[from++];
            if (ch == '$') {
                ignore = !ignore;
                if (!ignore && lastUnderline < to - 1) {
                    lastUnderline = to;
                    chars[to++] = '_';
                }
                continue;
            }
            if (!ignore) {
                chars[to++] = ch;
            }
        }
        if (!Character.isJavaIdentifierStart(chars[0])) {
            if (to < length) {
                System.arraycopy(chars, 0, chars, 1, to);
            }
            chars[0] = '_';
        }
        return new String(chars, 0, to);
    }

    /**
     * 标识符部分（非开头）的转义类似于 drools-model 的 StringUtil
     */
    public static String escapeIdentifier(String prefix, String partOfIdentifier) {
        StringBuilder result = new StringBuilder(prefix.length() + partOfIdentifier.length());
        if (prefix.isEmpty()) {
            if (partOfIdentifier.isEmpty()) {
                throw new IllegalArgumentException("prefix and partOfIdentifier cannot be empty at the same time");
            }
            // 处理第一个字符，确保它是一个合法的Java标识符起始字符
            if (!Character.isJavaIdentifierStart(partOfIdentifier.charAt(0))) {
                result.append('_');
            }
        } else {
            // 处理第一个字符，确保它是一个合法的Java标识符起始字符
            if (!Character.isJavaIdentifierStart(prefix.charAt(0))) {
                result.append('_');
            }
        }

        // 处理prefix剩余部分和partOfIdentifier的全部
        for (int i = 0; i < prefix.length(); i++) {
            appendChar(result, prefix.charAt(i));
        }
        for (int i = 0; i < partOfIdentifier.length(); i++) {
            appendChar(result, partOfIdentifier.charAt(i));
        }

        // 检查构建的字符串是否为Java关键字，如果是，则在前面添加"_"
        String id = result.toString();
        if (SourceVersion.isKeyword(id)) {
            return "_" + id;
        }
        return id;
    }

    private static void appendChar(StringBuilder result, char c) {
        if (c == '$') {
            result.append("$$");
        } else if (Character.isJavaIdentifierPart(c)) {
            result.append(c);
        } else {
            result.append("$").append(Integer.toString(c, 16)).append("$");
        }
    }

    public static final DateTimeFormatter FEEL_DATE;
    public static final DateTimeFormatter FEEL_TIME;
    public static final DateTimeFormatter FEEL_DATE_TIME;
    public static final DateTimeFormatter REGION_DATETIME;

    static {
        FEEL_DATE = new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4, 9, SignStyle.NORMAL)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
        FEEL_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .optionalStart()
                .appendLiteral("@")
                .appendZoneRegionId()
                .optionalEnd()
                .optionalStart()
                .appendOffsetId()
                .optionalEnd()
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
        FEEL_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(FEEL_DATE)
                .appendLiteral('T')
                .append(FEEL_TIME)
                .toFormatter();
        REGION_DATETIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .append(FEEL_DATE)
                .appendLiteral('T')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .appendLiteral("@")
                .appendZoneRegionId()
                .toFormatter();
    }

    @NotNull
    public static LocalDate parseDate(String text) {
        return LocalDate.from(FEEL_DATE.parse(text));
    }

    @NotNull
    public static LocalTime parseTime(String text) {
        TemporalAccessor parsed = FEEL_TIME.parse(text);
        if (parsed.query(TemporalQueries.offset()) != null) {
            return Objects.requireNonNull(OffsetTime.from(parsed)).toLocalTime();
        }
        return Objects.requireNonNull(LocalTime.from(parsed));
    }

    @NotNull
    public static LocalDateTime parseDateTime(String text) {
        if (text.contains("T")) {
            TemporalAccessor temporalAccessor = FEEL_DATE_TIME.parseBest(text, ZonedDateTime::from, OffsetDateTime::from, LocalDateTime::from);
            if (temporalAccessor instanceof ZonedDateTime zonedDateTime) {
                return zonedDateTime.toLocalDateTime();
            } else if (temporalAccessor instanceof OffsetDateTime offsetDateTime) {
                return offsetDateTime.toLocalDateTime();
            } else {
                return (LocalDateTime) temporalAccessor;
            }
        } else {
            LocalDate value = DateTimeFormatter.ISO_DATE.parse(text, LocalDate::from);
            return LocalDateTime.of(value, LocalTime.of(0, 0));
        }
    }

    @NotNull
    public static Duration parseDuration(String text) {
        return Duration.parse(text);
    }

    @NotNull
    public static Period parsePeriod(String text) {
        return Period.parse(text);
    }


    @SneakyThrows
    public static Object accessMember(Object current, String property) {
        if (current == null) {
            return null;
        }
        if (current instanceof Map<?, ?> map) {
            return map.get(property);
        } else if (current instanceof ChronoPeriod) {
            return switch (property) {
                case "years" -> ((ChronoPeriod) current).get(ChronoUnit.YEARS);
                case "months" -> ((ChronoPeriod) current).get(ChronoUnit.MONTHS) % 12;
                default -> null;
            };
        } else if (current instanceof Duration duration) {
            return switch (property) {
                case "days" -> duration.toDays();
                case "hours" -> duration.toHours() % 24;
                case "minutes" -> duration.toMinutes() % 60;
                case "seconds" -> duration.getSeconds() % 60;
                default -> null;
            };
        } else if (current instanceof TemporalAccessor temporalAccessor) {
            return switch (property) {
                case "year" -> temporalAccessor.get(ChronoField.YEAR);
                case "month" -> temporalAccessor.get(ChronoField.MONTH_OF_YEAR);
                case "day" -> temporalAccessor.get(ChronoField.DAY_OF_MONTH);
                case "hour" -> temporalAccessor.get(ChronoField.HOUR_OF_DAY);
                case "minute" -> temporalAccessor.get(ChronoField.MINUTE_OF_HOUR);
                case "second" -> temporalAccessor.get(ChronoField.SECOND_OF_MINUTE);
                case "time offset" -> {
                    if (temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
                        yield Duration.ofSeconds(temporalAccessor.get(ChronoField.OFFSET_SECONDS));
                    } else {
                        yield null;
                    }
                }
                case "timezone" -> {
                    ZoneId zoneId = temporalAccessor.query(TemporalQueries.zoneId());
                    if (zoneId != null) {
                        yield TimeZone.getTimeZone(zoneId).getID();
                    } else {
                        yield null;
                    }
                }
                case "weekday" -> temporalAccessor.get(ChronoField.DAY_OF_WEEK);
                default -> null;
            };
        } else if (current instanceof FeelRange<?> range) {
            return switch (property) {
                case "start included" -> range.startInclusive();
                case "start" -> range.start();
                case "end" -> range.end();
                case "end included" -> range.endInclusive();
                default -> null;
            };
        }
        Method method = PROPERTY_READ.get(current.getClass()).get(property);
        if (method != null) {
            Object invoke = method.invoke(current);
            if (invoke instanceof Character c) {
                invoke = c.toString();
            } else if (invoke instanceof Date date) {
                invoke = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            return invoke;
        }
        return null;
    }
}