package com.itsm.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * LocalDate 필드에 "yyyy-MM-dd" 외에 FE의 Date 직렬화 관행(전체 ISO-8601 datetime, 예: "2026-01-01T00:00:00.000Z")도
 * 허용한다. 날짜만 오면 그대로, datetime이 오면 날짜 부분만 취한다.
 */
public class LenientLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText().trim();
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(text).toLocalDate();
        }
    }
}
