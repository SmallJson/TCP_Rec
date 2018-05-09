package com.bupt.Util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T readValue(String content, Class<T> valueType) {
        T res = null;
        try {
                res = objectMapper.readValue(content, valueType);
        } catch (Exception e) {
        }
        return res;
    }
    public static String writeValueAsString(Object value) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(value);
        } catch (Exception e) {
        }
        return json;
    }
}
