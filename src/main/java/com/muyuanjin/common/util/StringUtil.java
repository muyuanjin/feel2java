package com.muyuanjin.common.util;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;


import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author muyuanjin
 */
@SuppressWarnings("unused")
@UtilityClass
public class StringUtil {
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{(\\d*)}");

    public static boolean hasText(@Nullable String str) {
        return (str != null && !str.isBlank());
    }

    public static boolean hasText(@Nullable CharSequence str) {
        if (str == null) {
            return false;
        }

        int strLen = str.length();
        if (strLen == 0) {
            return false;
        }

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor
     * of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.
     * <p><pre class="code">
     * StringUtil.hasLength(null) = false
     * StringUtil.hasLength("") = false
     * StringUtil.hasLength(" ") = true
     * StringUtil.hasLength("Hello") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see #hasLength(String)
     * @see #hasText(CharSequence)
     */
    public static boolean hasLength(@Nullable CharSequence str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * Check that the given {@code String} is neither {@code null} nor of length 0.
     * <p>Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasLength(CharSequence)
     * @see #hasText(String)
     */
    public static boolean hasLength(@Nullable String str) {
        return (str != null && !str.isEmpty());
    }

    public static boolean startsWithIgnoreCase(@Nullable String str, @Nullable String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() &&
                str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    public static boolean endsWithIgnoreCase(@Nullable String str, @Nullable String suffix) {
        return (str != null && suffix != null && str.length() >= suffix.length() &&
                str.regionMatches(true, str.length() - suffix.length(), suffix, 0, suffix.length()));
    }

    /**
     * <pre>{@code
     *      StringUtil.format("找不到ID为{0}的数据,请{1}!并且{2}", "a", "b", "c");
     *      StringUtil.format("找不到ID为{}的数据,请{}!并且{}", "a", "b", "c")
     * }</pre>
     *
     * @param format 格式化的字符串
     * @param args   参数，按照
     * @return 格式化后的字符串
     */
    public static String format(String format, Object... args) {
        Matcher matcher;
        if (args == null || args.length == 0 || !(matcher = PARAM_PATTERN.matcher(format)).find()) {
            return format;
        }
        StringBuilder buffer = new StringBuilder();
        int defaultIndex = 0;
        do {
            String group = matcher.group(1);
            int index;
            if (group.isEmpty()) {
                index = defaultIndex;
                defaultIndex++;
            } else {
                index = Integer.parseInt(group);
            }
            if (index < args.length) {
                matcher.appendReplacement(buffer, String.valueOf(args[index]));
            }
        } while (matcher.find());
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 将特殊符号间隔和驼峰格式的文本转换为空格间隔的文本<br>
     * <pre>{@code
     *      "jakarta.validation.constraints.NotNull.message"
     *                  ——>	"javax validation constraints not null message"
     *      "ServerPreparedStatement.29"
     *                  ——>	"server prepared statement 29"
     * }</pre>
     *
     * @param text 特殊符号间隔和驼峰格式的文本
     * @return 被分隔符间隔的文本
     */
    public static String formatText(String text) {
        return formatText(text, " ");
    }

    /**
     * 将特殊符号间隔和驼峰格式的文本转换为指定分隔符间隔的文本
     * <pre>{@code
     *      ("jakarta.validation.constraints.NotNull.message","-")
     *                  ——>	"javax-validation-constraints-not-null-message"
     *      ("ServerPreparedStatement.29","_")
     *                  ——>	"server_prepared_statement_29"
     * }</pre>
     *
     * @param text      特殊符号间隔和驼峰格式的文本
     * @param delimiter 分隔符
     * @return 被分隔符间隔的文本
     */
    public static String formatText(String text, CharSequence delimiter) {
        StringBuilder rs = new StringBuilder();
        char[] chars = text.strip().toCharArray();
        boolean lastWordNotSpace = true;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            char c1 = i > 0 ? chars[i - 1] : ' ';
            if (Character.isUpperCase(c) && Character.isLowerCase(c1) && i > 0 && lastWordNotSpace) {
                rs.append(delimiter);
                lastWordNotSpace = false;
            }
            if (CharUtil.isSpecialChar(c)) {
                if (i > 0 && lastWordNotSpace) {
                    rs.append(delimiter);
                    lastWordNotSpace = false;
                }
                continue;
            }
            char c2 = i < chars.length - 1 ? chars[i + 1] : ' ';
            if (Character.isDigit(c1) && Character.isUpperCase(c)) {
                rs.append(delimiter);
            }
            if (!CharUtil.isSpecialChar(c2) && !Character.isUpperCase(c1) && !Character.isUpperCase(c2)) {
                rs.append(Character.toLowerCase(c));
            } else {
                rs.append(c);
            }
            lastWordNotSpace = true;
        }
        return rs.toString();
    }

    public static String snakeToCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder output = new StringBuilder();
        boolean toUpperCase = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '_') {
                toUpperCase = true;
            } else {
                output.append(toUpperCase ? Character.toUpperCase(currentChar) : Character.toLowerCase(currentChar));
                toUpperCase = false;
            }
        }

        return output.toString();
    }

    public static String camelToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder output = new StringBuilder();
        output.append(Character.toUpperCase(input.charAt(0)));

        for (int i = 1; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (Character.isUpperCase(currentChar) || (Character.isDigit(currentChar) && !Character.isDigit(input.charAt(i - 1)))) {
                output.append('_');
            }
            output.append(Character.toUpperCase(currentChar));
        }
        return output.toString();
    }

    /**
     * 查找指定字符串是否包含指定字符列表中的任意一个字符
     *
     * @param str       指定字符串
     * @param testChars 需要检查的字符数组
     * @return 是否包含任意一个字符
     */
    public static boolean containsAny(CharSequence str, char... testChars) {
        if (!isEmpty(str)) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                char charAt = str.charAt(i);
                for (char testChar : testChars) {
                    if (charAt == testChar) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     *
     * @param str         指定字符串
     * @param testStrings 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAny(CharSequence str, CharSequence... testStrings) {
        return null != getContainsStr(str, testStrings);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串，如果包含返回找到的第一个字符串
     *
     * @param str         指定字符串
     * @param testStrings 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsStr(CharSequence str, CharSequence... testStrings) {
        if (isEmpty(str) || testStrings == null) {
            return null;
        }
        for (CharSequence checkStr : testStrings) {
            if (str.toString().contains(checkStr)) {
                return checkStr.toString();
            }
        }
        return null;
    }

    /**
     * <p>字符串是否为空，空的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.isEmpty(null)     // true}</li>
     *     <li>{@code StrUtil.isEmpty("")       // true}</li>
     *     <li>{@code StrUtil.isEmpty(" \t\n")  // false}</li>
     *     <li>{@code StrUtil.isEmpty("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议用于工具类或任何可以预期的方法参数的校验中。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasEmpty(CharSequence...)} 或 {@link #isAllEmpty(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 是否为空
     * @see #isBlank(CharSequence)
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

    /**
     * <p>字符串是否为空白，空白的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.isBlank(null)     // true}</li>
     *     <li>{@code StrUtil.isBlank("")       // true}</li>
     *     <li>{@code StrUtil.isBlank(" \t\n")  // true}</li>
     *     <li>{@code StrUtil.isBlank("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isEmpty(CharSequence)} 略慢。</p>
     * <br>
     *
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议仅对于客户端（或第三方接口）传入的参数使用该方法。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasBlank(CharSequence...)} 或 {@link #isAllBlank(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 若为空白，则返回 true
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(CharSequence str) {
        final int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (!CharUtil.isBlankChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>指定字符串数组中，是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.hasBlank()                  // true}</li>
     *     <li>{@code StrUtil.hasBlank("", null, " ")     // true}</li>
     *     <li>{@code StrUtil.hasBlank("123", " ")        // true}</li>
     *     <li>{@code StrUtil.hasBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllBlank(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasBlank(CharSequence...)            等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>{@link #isAllBlank(CharSequence...)} 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strings 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }

        for (CharSequence str : strings) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.isAllBlank()                  // true}</li>
     *     <li>{@code StrUtil.isAllBlank("", null, " ")     // true}</li>
     *     <li>{@code StrUtil.isAllBlank("123", " ")        // false}</li>
     *     <li>{@code StrUtil.isAllBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasBlank(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasBlank(CharSequence...)}   等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>isAllBlank(CharSequence...)          等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param strings 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllBlank(CharSequence... strings) {
        if (strings == null) {
            return true;
        }

        for (CharSequence str : strings) {
            if (isNotBlank(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     *     <li>不为空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.isNotBlank(null)     // false}</li>
     *     <li>{@code StrUtil.isNotBlank("")       // false}</li>
     *     <li>{@code StrUtil.isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code StrUtil.isNotBlank("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isNotEmpty(CharSequence)} 略慢。</p>
     * <p>建议：仅对于客户端（或第三方接口）传入的参数使用该方法。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.isNotEmpty(null)     // false}</li>
     *     <li>{@code StrUtil.isNotEmpty("")       // false}</li>
     *     <li>{@code StrUtil.isNotEmpty(" \t\n")  // true}</li>
     *     <li>{@code StrUtil.isNotEmpty("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：该方法建议用于工具类或任何可以预期的方法参数的校验中。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isEmpty(CharSequence)
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.isAllEmpty()                  // true}</li>
     *     <li>{@code StrUtil.isAllEmpty("", null)          // true}</li>
     *     <li>{@code StrUtil.isAllEmpty("123", "")         // false}</li>
     *     <li>{@code StrUtil.isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code StrUtil.isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasEmpty(CharSequence...)}   等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>isAllEmpty(CharSequence...)          等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param strings 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(CharSequence... strings) {
        if (strings == null) {
            return true;
        }

        for (CharSequence str : strings) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StrUtil.hasEmpty()                  // true}</li>
     *     <li>{@code StrUtil.hasEmpty("", null)          // true}</li>
     *     <li>{@code StrUtil.hasEmpty("123", "")         // true}</li>
     *     <li>{@code StrUtil.hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code StrUtil.hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasEmpty(CharSequence...)            等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>{@link #isAllEmpty(CharSequence...)} 等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param strings 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }

        for (CharSequence str : strings) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    //ISO-8859-1 to UTF-8
    public static String iso2Utf8(String str) {
        if (str == null) {
            return null;
        }
        return new String(str.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    public static int countOccurrencesOf(String str, String sub) {
        if (!hasLength(str) || !hasLength(sub)) {
            return 0;
        }

        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        return trimAllWhitespace((CharSequence) str).toString();
    }

    public static CharSequence trimAllWhitespace(CharSequence str) {
        if (!hasLength(str)) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb;
    }


    @NotNull
    public static String arrayToCommaDelimitedString(@Nullable Object[] list) {
        return toCommaDelimitedString(Arrays.asList(list));
    }

    @NotNull
    public static String arrayToDelimitedString(@Nullable Object[] list, String delimiter) {
        return toDelimitedString(Arrays.asList(list), delimiter);
    }

    @NotNull
    public static String toCommaDelimitedString(@Nullable Collection<?> list) {
        return toDelimitedString(list, ",");
    }

    @NotNull
    public static String toDelimitedString(@Nullable Collection<?> list, String delimiter) {
        if (list == null || delimiter == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object s : list) {
            if (s != null) {
                builder.append(s).append(delimiter);
            }
        }
        if (builder.isEmpty()) {
            return "";
        }
        builder.setLength(builder.length() - delimiter.length());
        return builder.toString();
    }

    private static final String[] EMPTY = {};

    @NotNull
    public static String[] commaDelimitedListToStringArray(@Nullable String str) {
        if (str == null) {
            return EMPTY;
        }
        return Objects.requireNonNull(commaDelimitedList(str)).toArray(EMPTY);
    }

    @NotNull
    public static String[] delimitedListToStringArray(@Nullable String str, String delimiter) {
        if (str == null) {
            return EMPTY;
        }
        return Objects.requireNonNull(delimitedList(str, delimiter)).toArray(EMPTY);
    }

    @NotNull
    public static Set<String> commaDelimitedListToSet(@Nullable String str) {
        List<String> strings = commaDelimitedList(str, Function.identity());
        return strings == null ? Collections.emptySet() : new LinkedHashSet<>(strings);
    }

    @NotNull
    public static Set<String> delimitedListToSet(@Nullable String str, String delimiter) {
        List<String> strings = delimitedList(str, delimiter, Function.identity());
        return strings == null ? Collections.emptySet() : new LinkedHashSet<>(strings);
    }

    @Nullable
    public static List<String> commaDelimitedList(@Nullable String str) {
        return commaDelimitedList(str, Function.identity());
    }

    @Nullable
    public static <T> List<T> commaDelimitedList(String str, Function<String, T> mapper) {
        return delimitedList(str, ",", mapper);
    }

    @Nullable
    public static List<String> delimitedList(String str, String delimiter) {
        return delimitedList(str, delimiter, Function.identity());
    }

    @Nullable
    public static <T> List<T> delimitedList(String str, String delimiter, Function<String, T> mapper) {
        if (str == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        int pos = 0;
        int delPos;
        int length = delimiter.length();
        while ((delPos = str.indexOf(delimiter, pos)) != -1) {
            result.add(mapper.apply(str.substring(pos, delPos)));
            pos = delPos + length;
        }
        if (!str.isEmpty() && pos <= str.length()) {
            result.add(mapper.apply(str.substring(pos)));
        }
        return result;
    }
}