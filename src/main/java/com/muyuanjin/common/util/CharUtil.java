package com.muyuanjin.common.util;

import lombok.experimental.UtilityClass;

/**
 * @author muyuanjin
 */
@UtilityClass
public class CharUtil {
    private static final char[] CHINESE_PUNCTUATION = "！￥…（）—｛｝《》【】‘；：”“’。，、？".toCharArray();

    /**
     * 是否是特殊符号<br>
     * 特殊符号包括中英文标点、空白符、ASCII控制符<br>
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isSpecialChar(char c) {
        return isEnglishPunctuation(c) || isAsciiControl(c) || isBlankChar(c) || isChinesePunctuation(c);
    }

    /**
     * 是否为中文标点<br>
     * 中文标点包括 ！￥…（）—｛｝《》【】‘；：”“’。，、？
     */
    public static boolean isChinesePunctuation(char c) {
        for (char c1 : CHINESE_PUNCTUATION) {
            if (c == c1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为英文标点<br>
     */
    public static boolean isEnglishPunctuation(char c) {
        return (c > 32 && c < '0') || (c > '9' && c < 'A') || (c > 'Z' && c < 'a') || (c > 'z' && c <= '~');
    }

    /**
     * 是否空白符<br>
     * 空白符包括空格、制表符、全角空格和不间断空格<br>
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(char c) {
        return isBlankChar((int) c);
    }

    /**
     * 是否空白符<br>
     * 空白符包括空格、制表符、全角空格和不间断空格<br>
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c)
                || Character.isSpaceChar(c)
                || c == '\ufeff'
                || c == '\u202a'
                || c == '\u0000';
    }

    /**
     * 是否为ASCII控制符（不可见字符），控制符位于0~31和127
     *
     * <pre>
     *   CharUtil.isAsciiControl('a')  = false
     *   CharUtil.isAsciiControl('A')  = false
     *   CharUtil.isAsciiControl('3')  = false
     *   CharUtil.isAsciiControl('-')  = false
     *   CharUtil.isAsciiControl('\n') = true
     *   CharUtil.isAsciiControl('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为控制符，控制符位于0~31和127
     */
    public static boolean isAsciiControl(final char ch) {
        return ch < 32 || ch == 127;
    }
}
