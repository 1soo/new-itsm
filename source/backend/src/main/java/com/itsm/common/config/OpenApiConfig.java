package com.itsm.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger-UI(OpenAPI) 문서 설정. JWT Bearer 스킴을 등록하여 Authorize로 토큰 주입 가능.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "ITSM API", version = "0.1", description = "ITSM 플랫폼 API 문서"),
        servers = @Server(url = "/", description = "current")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
