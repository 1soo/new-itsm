package com.itsm.change.integration;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.domain.AssetType;
import com.itsm.change.application.ChangeService;
import com.itsm.change.application.dto.ClassificationRequest;
import com.itsm.change.application.dto.CreateChangeRequest;
import com.itsm.change.application.dto.LinkRequest;
import com.itsm.change.application.dto.ResultRequest;
import com.itsm.change.application.dto.StatusTransitionRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.LinkTargetType;
import com.itsm.change.domain.Outcome;
import com.itsm.common.approval.application.ApprovalInstanceService;
import com.itsm.common.approval.domain.DecisionType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 변경(change) 전체 흐름을 검증한다.
 * RFC 등록→6단계 전이(순서 위반 400)→분류(유형·위험)→구현결과→인시던트/문제/자산 양방향 연계→일정·템플릿·지표를
 * 실 트랜잭션·실 FK로 재현한다. 승인 경로 자동 라우팅·승인/반려는 승인 프로세스 커스텀 기능(2026-07-11)으로
 * 제거되었다(공용 승인 엔진이 대체). Stage 2에서 IMPLEMENTATION 전이의 실제 게이트 차단·승인 후 재시도 통과를
 * 실 트랜잭션으로 재현한다(SRM과 동일 패턴, TC-ADM-006류 flush 회귀 방지 포함).
 * 실제 DDL(01/03/04/06/08/10/.../26)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ChangeIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/32_approval_process_priority_redesign.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired ChangeService changeService;
    @Autowired AssetService assetService;
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

    private Long insertIncident(String key) {
        jdbc.update("insert into incident(ticket_key, summary, severity, status, created_by) values (?,?,?,?,?)",
                key, "장애", "SEV1", "NEW", "test");
        return jdbc.queryForObject("select id from incident where ticket_key = ?", Long.class, key);
    }

    private Long insertProblem(String key) {
        jdbc.update("insert into problem(ticket_key, summary, status, created_by) values (?,?,?,?)",
                key, "문제", "DETECTION", "test");
        return jdbc.queryForObject("select id from problem where ticket_key = ?", Long.class, key);
    }

    private Long insertTemplate(String name) {
        jdbc.update("insert into change_template(name, description, created_by) values (?,?,?)",
                name, "설명", "test");
        return jdbc.queryForObject("select id from change_template where name = ?", Long.class, name);
    }

    private Long roleIdOf(String roleCode) {
        jdbc.update("insert into role(role_code, role_name, created_by) values (?,?,?) on conflict (role_code) do nothing",
                roleCode, roleCode, "test");
        return jdbc.queryForObject("select id from role where role_code = ?", Long.class, roleCode);
    }

    /** 도메인+요청유형(tier=23) 규칙 1건 + 1차 OR 승인(주어진 역할)을 CHANGE 유형별로 시딩한다(테스트 간 격리). */
    private void seedSubtypeProcess(String requestSubtypeKey, String roleCode) {
        jdbc.update("insert into approval_process(domain, request_subtype_key, priority_tier, name, created_by) values ('CHANGE',?,23,?,?)",
                requestSubtypeKey, "요청유형 규칙", "test");
        Long processId = jdbc.queryForObject(
                "select id from approval_process where domain = 'CHANGE' and request_subtype_key = ?", Long.class, requestSubtypeKey);
        jdbc.update("insert into approval_process_step(approval_process_id, step_no, decision_mode, created_by) values (?,1,'OR',?)",
                processId, "test");
        Long stepId = jdbc.queryForObject(
                "select id from approval_process_step where approval_process_id = ? and step_no = 1", Long.class, processId);
        jdbc.update("insert into approval_process_step_role(step_id, role_id, created_by) values (?,?,?)",
                stepId, roleIdOf(roleCode), "test");
    }

    @Test
    void fullChangeLifecycleWithoutMatchingRule() {
        long ts = System.nanoTime();
        Long cmId = insertUser("cm" + ts + "@itsm.local");
        as(cmId, "CHANGE_MANAGER");

        var created = changeService.create(new CreateChangeRequest("결제 모듈 배포", "설명",
                ChangeType.NORMAL, ChangeRisk.HIGH, "배포 계획", List.of("payment-api"), "롤백 계획", null, null));
        Long id = created.id();
        assertThat(created.ticketKey()).startsWith("CHG-");
        assertThat(created.status()).isEqualTo("REQUESTED");

        // 6단계 순차 전이 — REQUESTED→REVIEW→PLANNING→APPROVAL
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.REVIEW, "검토"));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.PLANNING, "계획"));

        // 순서 위반 전이 → 400
        assertThatThrownBy(() -> changeService.transition(id, new StatusTransitionRequest(ChangeStatus.CLOSED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));

        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.APPROVAL, null));
        // 매칭되는 승인 프로세스가 없으면(NORMAL 유형에 대해 아무 규칙도 시딩하지 않음) 게이트 없이 통과
        var toImpl = changeService.transition(id, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(toImpl.status()).isEqualTo("IMPLEMENTATION");

        var result = changeService.recordResult(id, new ResultRequest(Outcome.SUCCESS, false, "정상 배포"));
        assertThat(result.outcome()).isEqualTo("SUCCESS");
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.CLOSED, null));

        var detail = changeService.detail(id);
        assertThat(detail.status()).isEqualTo("CLOSED");
    }

    @Test
    void implementationGateBlocksThenApprovedRetrySucceeds() {
        long ts = System.nanoTime();
        Long cmId = insertUser("cm" + ts + "@itsm.local");
        Long approverId = insertUser("apr" + ts + "@itsm.local");
        seedSubtypeProcess("EMERGENCY", "APPROVER");

        as(cmId, "CHANGE_MANAGER");
        var created = changeService.create(new CreateChangeRequest("긴급 보안 패치", null,
                ChangeType.EMERGENCY, ChangeRisk.HIGH, null, null, null, null, null));
        Long id = created.id();
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.REVIEW, null));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.PLANNING, null));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.APPROVAL, null));

        // 게이트 차단: 인스턴스 없음 → 스냅샷 생성 + 409(approvalRequestId 포함)
        BusinessException blocked = (BusinessException) org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> changeService.transition(id, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null)));
        assertThat(blocked.getErrorCode()).isEqualTo(ErrorCode.APPROVAL_PENDING);
        Long approvalRequestId = blocked.getApprovalRequestId();
        assertThat(approvalRequestId).isNotNull();

        // 상세 조회에 approval 필드로 노출
        var detailPending = changeService.detail(id);
        assertThat(detailPending.approval().approvalRequestId()).isEqualTo(approvalRequestId);
        assertThat(detailPending.approval().status()).isEqualTo("IN_PROGRESS");

        // 승인자 결정 → APPROVED
        as(approverId, "APPROVER");
        approvalInstanceService.decide(approvalRequestId, DecisionType.APPROVE, "ok");

        // 재시도 시 게이트 통과
        as(cmId, "CHANGE_MANAGER");
        var toImpl = changeService.transition(id, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(toImpl.status()).isEqualTo("IMPLEMENTATION");

        var detailApproved = changeService.detail(id);
        assertThat(detailApproved.approval().status()).isEqualTo("APPROVED");
    }

    @Test
    void classificationUpdatesTypeAndRisk() {
        as(insertUser("cm" + System.nanoTime() + "@itsm.local"), "CHANGE_MANAGER");
        var created = changeService.create(new CreateChangeRequest("긴급 조치", null,
                ChangeType.EMERGENCY, null, null, null, null, null, null));
        var response = changeService.classify(created.id(), new ClassificationRequest(ChangeType.NORMAL, ChangeRisk.MEDIUM));
        assertThat(response.type()).isEqualTo("NORMAL");
        assertThat(response.risk()).isEqualTo("MEDIUM");
    }

    @Test
    void linksIncidentAndProblemBidirectionally() {
        as(insertUser("cm" + System.nanoTime() + "@itsm.local"), "CHANGE_MANAGER");
        var created = changeService.create(new CreateChangeRequest("연계 테스트", null,
                ChangeType.NORMAL, ChangeRisk.MEDIUM, null, null, null, null, null));
        Long id = created.id();

        Long incId = insertIncident("INC-2026-8001");
        var incLink = changeService.link(id, new LinkRequest(LinkTargetType.INCIDENT, incId));
        assertThat(incLink.targetType()).isEqualTo("INCIDENT");

        Long prbId = insertProblem("PRB-2026-8001");
        var prbLink = changeService.link(id, new LinkRequest(LinkTargetType.PROBLEM, prbId));
        assertThat(prbLink.targetType()).isEqualTo("PROBLEM");

        Long linkCount = jdbc.queryForObject(
                "select count(*) from ticket_link where (source_type='CHANGE' and source_id=?) "
                        + "or (target_type='CHANGE' and target_id=?)", Long.class, id, id);
        assertThat(linkCount).isEqualTo(4); // 인시던트·문제 각 양방향 2건씩

        assertThatThrownBy(() -> changeService.link(id, new LinkRequest(LinkTargetType.INCIDENT, 999999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));

        var detail = changeService.detail(id);
        assertThat(detail.links()).hasSize(2);
    }

    @Test
    void detailLinksExposeAssetKeyViaAssetSideLink() {
        // REQ-ITAM-006(TC-REG-002): API-ITAM-007(자산→변경 연계)로 생성된 양방향 ticket_link가
        // 변경 상세(links)에도 ASSET으로 노출되는지 검증.
        long ts = System.nanoTime();
        as(insertUser("cm" + ts + "@itsm.local"), "CHANGE_MANAGER");
        var created = changeService.create(new CreateChangeRequest("자산 연계 확인용 변경", null,
                ChangeType.NORMAL, ChangeRisk.LOW, null, null, null, null, null));
        Long id = created.id();

        as(insertUser("am" + ts + "@itsm.local"), "ASSET_MANAGER");
        var asset = assetService.create(new CreateAssetRequest("변경연계확인자산" + ts, AssetType.CLOUD,
                null, null, null, null, null, null, null, null));
        assetService.linkAsset(asset.id(), new LinkAssetRequest(TicketType.CHANGE, id));

        as(insertUser("cm2" + ts + "@itsm.local"), "CHANGE_MANAGER");
        var detailWithAsset = changeService.detail(id);
        assertThat(detailWithAsset.links()).hasSize(1);
        assertThat(detailWithAsset.links().get(0).type()).isEqualTo("ASSET");
        assertThat(detailWithAsset.links().get(0).targetKey()).isEqualTo(asset.assetKey());
    }

    @Test
    void scheduleTemplatesAndMetrics() {
        as(insertUser("cm" + System.nanoTime() + "@itsm.local"), "CHANGE_MANAGER");
        Long templateId = insertTemplate("지표 확인용 템플릿");
        assertThat(changeService.listTemplates()).extracting("name").contains("지표 확인용 템플릿");

        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(3);
        var created = changeService.create(new CreateChangeRequest("일정 확인", null,
                ChangeType.NORMAL, ChangeRisk.LOW, null, null, null, scheduledAt, null));

        var schedule = changeService.schedule(OffsetDateTime.now(), OffsetDateTime.now().plusDays(7), null);
        assertThat(schedule).extracting("id").contains(created.id());

        var metrics = changeService.metrics(OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));
        assertThat(metrics.total()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void nonManagerCannotCreateChange() {
        as(insertUser("agent" + System.nanoTime() + "@itsm.local"), "SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> changeService.create(new CreateChangeRequest("요약", null,
                ChangeType.NORMAL, null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
