package com.itsm.search.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.NoOpResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실 PostgreSQL(Testcontainers) + 실 HTTP(RestTemplate)로 통합 검색(API-SEARCH-001)을
 * 로그인→기사 작성→검색까지 end-to-end 재현한다. Spring Boot 4에서 TestRestTemplate이 제거되어
 * RestTemplate에 랜덤 포트 base URL을 설정해 동등하게 대체한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("itsm").withUsername("itsm").withPassword("itsm")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/01_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/01_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/02_seed.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/02_seed.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/03_common_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/03_common_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/04_srm_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/04_srm_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/06_incident_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/06_incident_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/08_problem_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/08_problem_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/10_change_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/10_change_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/11_change_seed.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/11_change_seed.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/12_knowledge_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/12_knowledge_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/13_knowledge_seed.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/13_knowledge_seed.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/14_asset_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/14_asset_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/16_esm_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/16_esm_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/18_vulnerability_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/18_vulnerability_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/20_compliance_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/20_compliance_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/22_infra_monitoring_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/22_infra_monitoring_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/24_auth_menu_columns.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/24_auth_menu_columns.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/25_common_notification_dismissal.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/25_common_notification_dismissal.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/26_approval_engine_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/26_approval_engine_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/27_approval_engine_seed.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/27_approval_engine_seed.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/28_approval_engine_index_fix.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/28_approval_engine_index_fix.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/29_knowledge_km004_cleanup.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/29_knowledge_km004_cleanup.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/30_auth_screen_i18n.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/30_auth_screen_i18n.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/31_sidebar_menu_label_cleanup.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/31_sidebar_menu_label_cleanup.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/32_approval_process_priority_redesign.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/32_approval_process_priority_redesign.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/33_srm_catalog_assignee_role.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/33_srm_catalog_assignee_role.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/34_srm_catalog_category.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/34_srm_catalog_category.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/35_catalog_form_field_textarea_type.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/35_catalog_form_field_textarea_type.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/36_srm_form_schema_jsonb.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/36_srm_form_schema_jsonb.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/40_esm_form_schema_jsonb.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/40_esm_form_schema_jsonb.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @LocalServerPort int port;

    RestTemplate rest;

    @BeforeEach
    void setUpRestTemplate() {
        rest = new RestTemplate();
        rest.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + port));
        rest.setErrorHandler(new NoOpResponseErrorHandler());
    }

    private String loginAndGetAccessToken(String email) {
        ResponseEntity<Map> response = rest.postForEntity("/api/v1/auth/login",
                Map.of("email", email, "password", "Admin@1234"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("accessToken");
    }

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void knowledgeContributorSearchesOwnArticleByKeyword() {
        String accessToken = loginAndGetAccessToken("kc@itsm.local");
        HttpHeaders headers = authHeaders(accessToken);

        ResponseEntity<Map> createResponse = rest.exchange("/api/v1/knowledge/articles", HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "Change management guide", "body", "How to request a change"), headers),
                Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> searchResponse = rest.exchange(
                "/api/v1/search?keyword=change&page=0&size=20", HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> content = (List<Map<String, Object>>) searchResponse.getBody().get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0)).containsEntry("domain", "KNOWLEDGE").containsEntry("key", "KB-1");
    }

    @Test
    void changeManagerSearchesOwnChangeByKeyword() {
        String accessToken = loginAndGetAccessToken("cm@itsm.local");
        HttpHeaders headers = authHeaders(accessToken);

        ResponseEntity<Map> createResponse = rest.exchange("/api/v1/changes", HttpMethod.POST,
                new HttpEntity<>(Map.of("summary", "change management test", "type", "NORMAL"), headers),
                Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> searchResponse = rest.exchange(
                "/api/v1/search?keyword=change&page=0&size=20", HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> content = (List<Map<String, Object>>) searchResponse.getBody().get("content");
        assertThat(content).hasSize(1);
        assertThat(content.get(0)).containsEntry("domain", "CHANGE");
    }

    @Test
    void unauthenticatedSearchReturns401() {
        ResponseEntity<String> response = rest.getForEntity("/api/v1/search?keyword=change", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
