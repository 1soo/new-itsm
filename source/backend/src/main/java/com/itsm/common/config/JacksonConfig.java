package com.itsm.common.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;

/**
 * 전역 Jackson 커스터마이징. LocalDate 필드가 FE의 전체 ISO-8601 datetime 직렬화도 허용하도록 완화한다.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer lenientLocalDateCustomizer() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LenientLocalDateDeserializer());
        return builder -> builder.addModule(module);
    }
}
