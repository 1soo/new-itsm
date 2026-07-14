package com.itsm.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * `.env` 파일을 읽어 application.yml의 {@code ${VAR}} 참조가 해석되도록 Environment에 추가한다.
 * spring-dotenv(me.paulschwarz)가 Spring Boot 4의 {@code ConfigurableBootstrapContext} 패키지 이동으로
 * {@code SpringApplicationRunListener}를 더 이상 오버라이드하지 못해 무동작하게 되어 자체 구현으로 대체.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        Map<String, Object> properties = new LinkedHashMap<>();
        for (DotenvEntry entry : dotenv.entries()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        environment.getPropertySources()
                .addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new MapPropertySource("env", properties));
    }
}
