package com.muyuanjin.feel.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.muyuanjin.feel.exception.FeelException;
import com.muyuanjin.feel.exception.FeelLangException;
import com.muyuanjin.feel.lang.FType;
import com.muyuanjin.feel.lang.type.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author muyuanjin
 */
public class TypeParser {
    private String input; // the input signature
    private int index;    // index into the input
    private int mark;     // index of mark
    // 用于标记输入结束
    private static final char EOI = '\0';

    public static TypeParser make() {
        return new TypeParser();
    }

    private TypeParser() {}

    // prepares parser for new parsing session
    private void init(String s) {
        input = s;
        mark = index = 0;
    }

    //返回输入的当前元素
    private char current() {
        assert (index <= input.length());
        return index < input.length() ? input.charAt(index) : EOI;
    }

    private char consume() {
        char c = current();
        advance();
        return c;
    }

    //提前输入
    private void advance() {
        assert (index <= input.length());
        if (index < input.length()) index++;
    }

    // 就位
    private void mark() {
        mark = index;
    }

    // 用于调试，将当前字符打印到输入的末尾。
    private String remainder() {
        return input.substring(index);
    }

    // 返回从标记（包含）到当前位置（不包含）的输入子串
    // 到当前位置（不包括）
    private String markToCurrent() {
        return input.substring(mark, index);
    }


    private FeelException error(String errorMsg) {
        return new FeelLangException(0, index, "Type Parse error: " + errorMsg +
                                               "\n\tRemaining input: " + remainder());
    }

    /**
     * 验证解析是否取得进展；如果没有进展，则抛出异常
     * 如果没有进展，则抛出异常。
     */
    private void progress(int startingPosition) {
        if (index <= startingPosition)
            throw error("Failure to make progress!");
    }

    public FType parseType(String type) {
        init(type);
        FType result = parseType();
        skipWhitespace();
        if (current() != EOI)
            throw error("Expected end of input but found " + current());
        return result;
    }

    private FType parseType() {
        return switch (current()) {
            case 'a' -> parseAny();
            case 'b' -> parseBoolean();
            case 'c' -> parseContext();
            case 'd' -> {
                advance();
                expect('a');
                if (current() == 't') {
                    advance();
                    //date or date and time
                    expect("e");
                    if (!Character.isWhitespace(current())) {
                        yield FDate.DATE;
                    }
                    skipWhitespace();
                    expect("and");
                    skipWhitespace();
                    expect("time");
                    yield FDateTime.DATE_TIME;
                }
                // day and time duration
                expect('y');
                skipWhitespace();
                expect("and");
                skipWhitespace();
                expect("time");
                skipWhitespace();
                expect("duration");
                yield FDayTimeDuration.DAY_TIME_DURATION;
            }
            case 'f' -> parseFunction();
            case 'l' -> parseList();
            case 'n' -> {
                advance();
                expect('u');
                if (current() == 'l') {
                    expect("ll");
                    yield FNull.NULL;
                }
                expect('m', 'b', 'e', 'r');
                yield FNumber.NUMBER;
            }
            case 'r' -> parseRange();
            case 's' -> parseString();
            case 't' -> parseTime();
            case 'y' -> parseYearMonthDuration();
            default -> throw error("Expected type but found " + current());
        };
    }

    private FAny parseAny() {
        expect("any");
        return FAny.ANY;
    }

    private FBoolean parseBoolean() {
        expect("boolean");
        return FBoolean.BOOLEAN;
    }

    private FString parseString() {
        expect("string");
        return FString.STRING;
    }

    private FYearMonthDuration parseYearMonthDuration() {
        expect("year");
        skipWhitespace();
        expect("and");
        skipWhitespace();
        expect("month");
        skipWhitespace();
        expect("duration");
        return FYearMonthDuration.YEAR_MONTH_DURATION;
    }

    private FTime parseTime() {
        expect("time");
        return FTime.TIME;
    }

    private FRange parseRange() {
        expect("range");
        Boolean start = null, end = null;
        skipWhitespace();
        if (current() != '<') {
            switch (consume()) {
                case '[' -> start = true;
                case '(' -> start = false;
                case ']' -> end = true;
                case ')' -> end = false;
                default -> throw error("Expected '[' or '(' or ']' or ')' but found " + current());
            }
        }
        skipWhitespace();
        if (current() != '<') {
            switch (consume()) {
                case ']' -> end = true;
                case ')' -> end = false;
                default -> throw error("Expected '[' or '(' or ']' or ')' but found " + current());
            }
        }
        skipWhitespace();
        expect("<");
        skipWhitespace();
        var elementType = parseType();
        skipWhitespace();
        expect(">");
        return FRange.of(elementType, start, end);
    }

    private FList parseList() {
        expect("list");
        skipWhitespace();
        expect("<");
        skipWhitespace();
        var type = parseType();
        skipWhitespace();
        expect(">");
        return FList.of(type);
    }

    private FContext parseContext() {
        expect("context");
        skipWhitespace();
        expect("<");
        skipWhitespace();
        var member = new LinkedHashMap<String, FType>();
        while (current() != '>') {
            String name = parseName();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            member.put(name, parseType());
            skipWhitespace();
            if (current() == ',') {
                advance();
                skipWhitespace();
            }
        }
        expect(">");
        return FContext.of(member);
    }

    private FFunction parseFunction() {
        expect("function");
        skipWhitespace();
        expect('<');
        skipWhitespace();
        var parameters = new ArrayList<FType>();
        while (current() != '>') {
            parameters.add(parseType());
            skipWhitespace();
            if (current() == ',') {
                advance();
                skipWhitespace();
            }
        }
        expect(">");
        skipWhitespace();
        expect("->");
        skipWhitespace();
        return FFunction.of(parseType(), parameters);
    }

    private String parseName() {
        mark();
        while (current() != ':') {
            advance();
        }
        return markToCurrent();
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(current())) {
            advance();
        }
    }

    private void expect(char... cs) {
        for (char c : cs) {
            expect(c);
        }
    }

    private void expect(char c) {
        if (current() != c)
            throw error("Expected " + c + " but found " + current());
        advance();
    }

    private void expect(String expected) {
        int startingPosition = index;
        for (int i = 0; i < expected.length(); i++) {
            char charAt = expected.charAt(i);
            if (current() != charAt)
                throw error("Expected " + charAt + " but found " + current());
            advance();
        }
        progress(startingPosition);
    }

    public static class JsonDes extends JsonDeserializer<FType> {
        @Override
        public FType deserialize(JsonParser p, DeserializationContext context) throws IOException {
            return TypeParser.make().parseType(p.getText());
        }
    }
}
