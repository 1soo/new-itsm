package com.itsm.search.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실 PostgreSQL(Testcontainers) + 실 HTTP(TestRestTemplate)로 통합 검색(API-SEARCH-001)을
 * 로그인→기사 작성→검색까지 end-to-end 재현한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
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
                    "/docker-entrypoint-initdb.d/14_asset_schema.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired TestRestTemplate rest;

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
