package com.itsm.knowledge.integration;

import com.itsm.common.approval.application.ApprovalInstanceService;
import com.itsm.common.approval.domain.DecisionType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.knowledge.application.KnowledgeService;
import com.itsm.knowledge.application.dto.CreateArticleRequest;
import com.itsm.knowledge.application.dto.FeedbackRequest;
import com.itsm.knowledge.application.dto.LinkArticleRequest;
import com.itsm.knowledge.application.dto.StatusTransitionRequest;
import com.itsm.knowledge.domain.ArticleStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
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
 * 작성(DRAFT)→검토요청(IN_REVIEW/PUBLISHED, 공용 승인 게이트)→열람(조회수 증가)→유용성평가→
 * 검색(검색로그 기록)→카테고리→KCS 티켓 연계→지표를 실 트랜잭션·실 FK로 재현한다.
 * 승인 프로세스 커스텀 기능(2026-07-11)으로 게이트키퍼 전용 검토승인/반려 API는 제거되었고,
 * 공용 승인 엔진(common.approval)의 결정 확정 콜백이 기사를 자동 전환하는 흐름을 별도 검증한다.
 * 실제 DDL(01/03/04/06/08/10/12/.../26)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class KnowledgeIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
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

    @Autowired KnowledgeService knowledgeService;
    @Autowired ApprovalInstanceService approvalInstanceService;
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

    private Long roleIdOf(String roleCode) {
        jdbc.update("insert into role(role_code, role_name, created_by) values (?,?,?) on conflict (role_code) do nothing",
                roleCode, roleCode, "test");
        return jdbc.queryForObject("select id from role where role_code = ?", Long.class, roleCode);
    }

    /**
     * 도메인+승인요청자 역할(tier=25) 규칙 1건 + 1차 OR 승인(주어진 역할)을 KNOWLEDGE에 시딩한다.
     * KNOWLEDGE는 요청유형 스코프가 없어(request_subtype_key 항상 null) 도메인만으로는(tier=11) 테스트 간
     * 격리가 안 되므로, 이 테스트만의 전용 요청자 스코프 역할(requesterRoleCode)로 tier=25 매칭시켜 격리한다
     * (같은 컨테이너를 공유하는 다른 테스트의 기사 작성자는 이 역할을 보유하지 않아 매칭되지 않음).
     */
    private Long seedRequesterScopedProcess(String requesterRoleCode, Long requesterId, String decisionRoleCode) {
        Long requesterRoleId = roleIdOf(requesterRoleCode);
        jdbc.update("insert into user_role(user_id, role_id, created_by) values (?,?,?)",
                requesterId, requesterRoleId, "test");
        jdbc.update("insert into approval_process(domain, priority_tier, name, created_by) values ('KNOWLEDGE',25,?,?)",
                "게이트키퍼 규칙-" + requesterRoleCode, "test");
        Long processId = jdbc.queryForObject(
                "select id from approval_process where domain = 'KNOWLEDGE' and name = ?",
                Long.class, "게이트키퍼 규칙-" + requesterRoleCode);
        jdbc.update("insert into approval_process_requester_role(approval_process_id, role_id, created_by) values (?,?,?)",
                processId, requesterRoleId, "test");
        jdbc.update("insert into approval_process_step(approval_process_id, step_no, decision_mode, created_by) values (?,1,'OR',?)",
                processId, "test");
        Long stepId = jdbc.queryForObject(
                "select id from approval_process_step where approval_process_id = ? and step_no = 1", Long.class, processId);
        jdbc.update("insert into approval_process_step_role(step_id, role_id, created_by) values (?,?,?)",
                stepId, roleIdOf(decisionRoleCode), "test");
        return processId;
    }

    @Test
    void fullArticleLifecycleWithoutMatchingRule() {
        long ts = System.nanoTime();
        Long contributorId = insertUser("kc" + ts + "@itsm.local");
        Long categoryId = insertCategory("네트워크-" + ts);

        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        var created = knowledgeService.create(new CreateArticleRequest("VPN 접속 오류 해결", "설정을 확인하세요.",
                categoryId, List.of("네트워크", "VPN")));
        Long id = created.id();
        assertThat(created.status()).isEqualTo("DRAFT");

        // 검토 요청 — 매칭되는 승인 프로세스가 없으므로 즉시 게시
        var toReview = knowledgeService.transition(id, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));
        assertThat(toReview.status()).isEqualTo("PUBLISHED");
        assertThat(toReview.approvalRequestId()).isNull();

        // 최종 사용자 열람 — 조회수 증가
        Long endUserId = insertUser("eu" + ts + "@itsm.local");
        as(endUserId, "END_USER");
        var detail = knowledgeService.detail(id);
        assertThat(detail.status()).isEqualTo("PUBLISHED");
        assertThat(detail.labels()).contains("네트워크", "VPN");
        assertThat(detail.approval().approvalRequestId()).isNull();

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
        as(insertUser("kg" + ts + "@itsm.local"), "KNOWLEDGE_GATEKEEPER");
        var metrics = knowledgeService.metrics(null, null);
        assertThat(metrics.usageCount()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.helpfulRate()).isEqualTo(100.0);
    }

    @Test
    void reviewGateBlocksThenDecisionAutoTransitionsArticle() {
        long ts = System.nanoTime();
        Long contributorId = insertUser("kc2" + ts + "@itsm.local");
        Long gatekeeperId = insertUser("kg2" + ts + "@itsm.local");
        seedRequesterScopedProcess("KM_REVIEW_SCOPE_" + ts, contributorId, "KNOWLEDGE_GATEKEEPER");

        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        var created = knowledgeService.create(new CreateArticleRequest("결제 오류 대응", "본문", null, null));
        Long id = created.id();

        // 검토 요청 — 매칭 규칙 있음 → IN_REVIEW + 인스턴스 생성(항상 200)
        var toReview = knowledgeService.transition(id, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));
        assertThat(toReview.status()).isEqualTo("IN_REVIEW");
        assertThat(toReview.approvalRequestId()).isNotNull();
        Long firstRequestId = toReview.approvalRequestId();

        // 게이트키퍼 반려 — 사유 누락 400
        as(gatekeeperId, "KNOWLEDGE_GATEKEEPER");
        assertThatThrownBy(() -> approvalInstanceService.decide(firstRequestId, DecisionType.REJECT, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.REJECT_REASON_REQUIRED));

        // 반려 → 콜백으로 기사가 자동으로 DRAFT 복귀
        approvalInstanceService.decide(firstRequestId, DecisionType.REJECT, "내용 보강 필요");
        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        assertThat(knowledgeService.detail(id).status()).isEqualTo("DRAFT");

        // 재검토 요청 → 신규 인스턴스 생성(이전 REJECTED 인스턴스 재사용 안 함)
        var secondReview = knowledgeService.transition(id, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));
        assertThat(secondReview.status()).isEqualTo("IN_REVIEW");
        Long secondRequestId = secondReview.approvalRequestId();
        assertThat(secondRequestId).isNotEqualTo(firstRequestId);

        // 승인 → 콜백으로 기사가 자동으로 PUBLISHED 전환
        as(gatekeeperId, "KNOWLEDGE_GATEKEEPER");
        var approveResult = approvalInstanceService.decide(secondRequestId, DecisionType.APPROVE, "확인 완료");
        assertThat(approveResult.requestStatus()).isEqualTo("APPROVED");

        as(contributorId, "KNOWLEDGE_CONTRIBUTOR");
        var detail = knowledgeService.detail(id);
        assertThat(detail.status()).isEqualTo("PUBLISHED");
        assertThat(detail.approval().approvalRequestId()).isEqualTo(secondRequestId);
        assertThat(detail.approval().status()).isEqualTo("APPROVED");
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
