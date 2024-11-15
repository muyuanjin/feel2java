package com.muyuanjin.feel.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.muyuanjin.common.util.DateUtil;
import lombok.SneakyThrows;

/**
 * @author muyuanjin
 */
public class JSONUtil {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            //对象的所有字段全部列入
            .serializationInclusion(JsonInclude.Include.ALWAYS)
            //取消默认转换timestamps形式
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            //需要覆盖module的配置，否则会忽略重复注册module
            .disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS)
            //忽略空Bean转json的错误
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            //忽略在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .defaultTimeZone(DateUtil.DEFAULT_TIME_ZONE)
            .defaultLocale(DateUtil.DEFAULT_LOCALE_INSTANCE)
            .build();

    @SneakyThrows
    public static String toJSONString(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    public static String toPrettyJSONString(Object obj) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    @SneakyThrows
    public static <T> T parseObject(String text, Class<T> clazz) {
        return objectMapper.readValue(text, clazz);
    }
}
