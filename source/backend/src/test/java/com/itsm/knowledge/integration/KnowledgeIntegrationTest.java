package com.itsm.knowledge.integration;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.knowledge.application.KnowledgeService;
import com.itsm.knowledge.application.dto.CreateArticleRequest;
import com.itsm.knowledge.application.dto.FeedbackRequest;
import com.itsm.knowledge.application.dto.LinkArticleRequest;
import com.itsm.knowledge.application.dto.ReviewRequest;
import com.itsm.knowledge.application.dto.StatusTransitionRequest;
import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.ReviewDecision;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 지식(knowledge) 전체 흐름을 검증한다.
 * 작성(DRAFT)→검토요청(IN_REVIEW)→반려(사유 필수, DRAFT 복귀)→재검토요청→승인(PUBLISHED)→
 * 열람(조회수 증가)→유용성평가→검색(검색로그 기록)→카테고리→KCS 티켓 연계→지표를
 * 실 트랜잭션·실 FK로 재현한다. 실제 DDL(01/03/04/06/08/10/12)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class KnowledgeIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("itsm").withUsername("itsm").withPassword("itsm")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/01_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/01_schema.sql")
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
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/12_knowledge_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/12_knowledge_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/14_asset_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/14_asset_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/16_esm_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/16_esm_schema.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired KnowledgeService knowledgeService;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void as(Long userId, String... roles) {
        AuthPrincipal p = new AuthPrincipal(userId, "u" + userId + "@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p, null, List.of()));
    }

    private Long insertUser(String email) {
        jdbc.update("insert into app_user(email, password_hash, name, status, created_by) values (?,?,?,?,?)",
                email, "hash", email, "ACTIVE", "test");
        return jdbc.queryForObject("select id from app_user where email = ?", Long.class, email);
    }

    private Long insertCategory(String name) {
        jdbc.update("insert into knowledge_category(name, created_by) values (?,?)", name, "test");
        return jdbc.queryForObject("select id from knowledge_category where name = ?", Long.class, name);
    }

    private Long insertIncident(String key) {
        jdbc.update("insert into incident(ticket_key, summary, severity, status, created_by) values (?,?,?,?,?)",
                key, "장애", "SEV1", "NEW", "test");
        return jdbc.queryForObject("select id from incident where ticket_key = ?", Long.class, key);
    }

    @Test
    void fullArticleLifecycle() {
        long ts = System.nanoTime();
        Long contributorId = insertUser("kc" + ts + "@itsm.local");
        Long gatekeeperId = insertUser("kg" + ts + "@itsm.local");
        Long categoryId = insertCategory("네트워크-" + ts);

        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        var created = knowledgeService.create(new CreateArticleRequest("VPN 접속 오류 해결", "설정을 확인하세요.",
                categoryId, List.of("네트워크", "VPN")));
        Long id = created.id();
        assertThat(created.status()).isEqualTo("DRAFT");

        // 검토 요청
        var toReview = knowledgeService.transition(id, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));
        assertThat(toReview.status()).isEqualTo("IN_REVIEW");

        // 게이트키퍼 반려 — 사유 누락 400
        as(gatekeeperId, "KNOWLEDGE_GATEKEEPER");
        assertThatThrownBy(() -> knowledgeService.review(id, new ReviewRequest(ReviewDecision.REJECT, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.REJECT_REASON_REQUIRED));

        var rejected = knowledgeService.review(id, new ReviewRequest(ReviewDecision.REJECT, "내용 보강 필요"));
        assertThat(rejected.status()).isEqualTo("DRAFT");

        // 재검토 요청 → 승인
        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        knowledgeService.transition(id, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));

        as(gatekeeperId, "KNOWLEDGE_GATEKEEPER");
        var approved = knowledgeService.review(id, new ReviewRequest(ReviewDecision.APPROVE, "확인 완료"));
        assertThat(approved.status()).isEqualTo("PUBLISHED");

        // 최종 사용자 열람 — 조회수 증가
        Long endUserId = insertUser("eu" + ts + "@itsm.local");
        as(endUserId, "END_USER");
        var detail = knowledgeService.detail(id);
        assertThat(detail.status()).isEqualTo("PUBLISHED");
        assertThat(detail.labels()).contains("네트워크", "VPN");

        // 유용성 평가
        var feedback = knowledgeService.feedback(id, new FeedbackRequest(true, "도움이 되었습니다"));
        assertThat(feedback.helpful()).isEqualTo(1);

        // 검색 — 검색어 있을 때 search_log 기록, 매칭 결과 확인
        var searchResult = knowledgeService.search("VPN", null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 20));
        assertThat(searchResult.noResult()).isFalse();
        assertThat(searchResult.content()).extracting("title").contains("VPN 접속 오류 해결");

        Long searchLogCount = jdbc.queryForObject(
                "select count(*) from search_log where keyword = 'VPN'", Long.class);
        assertThat(searchLogCount).isGreaterThanOrEqualTo(1);

        // 카테고리 목록
        assertThat(knowledgeService.categories()).extracting("id").contains(categoryId);

        // KCS 티켓 연계
        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        Long incId = insertIncident("INC-2026-7001");
        var link = knowledgeService.linkArticle(new LinkArticleRequest(TicketType.INCIDENT, incId, id, null));
        assertThat(link.articleId()).isEqualTo(id);
        Long linkCount = jdbc.queryForObject(
                "select count(*) from ticket_link where source_type='INCIDENT' and source_id=? "
                        + "and target_type='KNOWLEDGE' and target_id=?", Long.class, incId, id);
        assertThat(linkCount).isEqualTo(1);

        // 존재하지 않는 티켓 연계 → 400
        assertThatThrownBy(() -> knowledgeService.linkArticle(
                new LinkArticleRequest(TicketType.INCIDENT, 999999L, id, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));

        // 지표 — Gatekeeper 전용
        as(gatekeeperId, "KNOWLEDGE_GATEKEEPER");
        var metrics = knowledgeService.metrics(null, null);
        assertThat(metrics.usageCount()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.helpfulRate()).isEqualTo(100.0);
    }

    @Test
    void draftArticleNotVisibleToOtherContributor() {
        long ts = System.nanoTime();
        Long authorId = insertUser("author" + ts + "@itsm.local");
        Long otherId = insertUser("other" + ts + "@itsm.local");

        as(authorId, "KNOWLEDGE_CONTRIBUTOR");
        var created = knowledgeService.create(new CreateArticleRequest("비공개 초안", "본문", null, null));

        as(otherId, "KNOWLEDGE_CONTRIBUTOR");
        assertThatThrownBy(() -> knowledgeService.detail(created.id()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void nonContributorCannotCreateArticle() {
        long ts = System.nanoTime();
        as(insertUser("agent" + ts + "@itsm.local"), "SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> knowledgeService.create(new CreateArticleRequest("제목", "본문", null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
