package com.itsm.change.integration;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.domain.AssetType;
import com.itsm.change.application.ChangeService;
import com.itsm.change.application.dto.ChangeApprovalDecision;
import com.itsm.change.application.dto.ChangeApprovalRequest;
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
import org.testcontainers.containers.PostgreSQLContainer;
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
 * RFC 등록(승인경로 분류: CAB/PEER_REVIEW/AUTO)→6단계 전이(승인 전 구현 차단 409)→
 * 승인/반려(역할 기반 공유 대기함, 반려사유 필수, 재결정 409)→구현결과(미승인 400)→
 * 인시던트/문제 양방향 연계→일정·템플릿·지표를 실 트랜잭션·실 FK로 재현한다.
 * 실제 DDL(01/03/04/06/08/10)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ChangeIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/16_esm_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/18_vulnerability_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/18_vulnerability_schema.sql");

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

    @Test
    void fullChangeLifecycleWithCabApproval() {
        long ts = System.nanoTime();
        Long cmId = insertUser("cm" + ts + "@itsm.local");
        Long approverId = insertUser("apr" + ts + "@itsm.local");
        as(cmId, "CHANGE_MANAGER");

        // 등록 — 고위험 → CAB 경로
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
        // CAB 경로 → 승인 대기 레코드 생성 확인
        Long approvalCount = jdbc.queryForObject(
                "select count(*) from approval where ticket_type='CHANGE' and ticket_id=? and status='PENDING'",
                Long.class, id);
        assertThat(approvalCount).isEqualTo(1);

        // 승인 완료 전 구현 전이 → 409
        assertThatThrownBy(() -> changeService.transition(id, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.APPROVAL_PENDING));

        // 구현 결과 기록 시도(미승인) → 400
        assertThatThrownBy(() -> changeService.recordResult(id, new ResultRequest(Outcome.SUCCESS, false, "완료")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CHANGE_NOT_APPROVED));

        // CAB(APPROVER) 승인
        as(approverId, "APPROVER");
        var approved = changeService.decideApproval(id, new ChangeApprovalRequest(ChangeApprovalDecision.APPROVE, "승인합니다"));
        assertThat(approved.id()).isEqualTo(id);

        // 재결정 → 409
        assertThatThrownBy(() -> changeService.decideApproval(id, new ChangeApprovalRequest(ChangeApprovalDecision.APPROVE, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.APPROVAL_ALREADY_DECIDED));

        // 승인 후 구현 전이 성공
        as(cmId, "CHANGE_MANAGER");
        var toImpl = changeService.transition(id, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(toImpl.status()).isEqualTo("IMPLEMENTATION");

        // 구현 결과 기록
        var result = changeService.recordResult(id, new ResultRequest(Outcome.SUCCESS, false, "정상 배포"));
        assertThat(result.outcome()).isEqualTo("SUCCESS");
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.CLOSED, null));

        // 상세 조회 — 승인 이력 노출
        var detail = changeService.detail(id);
        assertThat(detail.approvals()).extracting("decision").contains("APPROVED");
        assertThat(detail.status()).isEqualTo("CLOSED");
    }

    @Test
    void standardChangeWithTemplateAutoApproves() {
        // TC-CHG-015: risk 미평가(null)로 생성해도 STANDARD+templateId는 AUTO여야 한다.
        as(insertUser("cm" + System.nanoTime() + "@itsm.local"), "CHANGE_MANAGER");
        Long templateId = insertTemplate("표준 배포 템플릿");
        var created = changeService.create(new CreateChangeRequest("표준 패치 배포", null,
                ChangeType.STANDARD, null, null, null, null, null, templateId));
        Long id = created.id();

        assertThat(changeService.detail(id).approvalRoute()).isEqualTo("AUTO");

        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.REVIEW, null));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.PLANNING, null));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.APPROVAL, null));

        // AUTO 경로 → 승인 레코드 생성 안 됨
        Long approvalCount = jdbc.queryForObject(
                "select count(*) from approval where ticket_type='CHANGE' and ticket_id=?", Long.class, id);
        assertThat(approvalCount).isEqualTo(0);

        // 승인 절차 없이 구현 전이 즉시 성공
        var toImpl = changeService.transition(id, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(toImpl.status()).isEqualTo("IMPLEMENTATION");
    }

    @Test
    void classificationChangesApprovalRoute() {
        as(insertUser("cm" + System.nanoTime() + "@itsm.local"), "CHANGE_MANAGER");
        var created = changeService.create(new CreateChangeRequest("긴급 조치", null,
                ChangeType.EMERGENCY, null, null, null, null, null, null));
        var response = changeService.classify(created.id(), new ClassificationRequest(ChangeType.NORMAL, ChangeRisk.MEDIUM));
        assertThat(response.approvalRoute()).isEqualTo("PEER_REVIEW");

        var response2 = changeService.classify(created.id(), new ClassificationRequest(ChangeType.NORMAL, ChangeRisk.HIGH));
        assertThat(response2.approvalRoute()).isEqualTo("CAB");
    }

    @Test
    void rejectRequiresReasonAndRecordsDecision() {
        long ts = System.nanoTime();
        as(insertUser("cm" + ts + "@itsm.local"), "CHANGE_MANAGER");
        var created = changeService.create(new CreateChangeRequest("위험 변경", null,
                ChangeType.NORMAL, ChangeRisk.HIGH, null, null, null, null, null));
        Long id = created.id();
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.REVIEW, null));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.PLANNING, null));
        changeService.transition(id, new StatusTransitionRequest(ChangeStatus.APPROVAL, null));

        as(insertUser("apr" + ts + "@itsm.local"), "APPROVER");
        assertThatThrownBy(() -> changeService.decideApproval(id, new ChangeApprovalRequest(ChangeApprovalDecision.REJECT, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.REJECT_REASON_REQUIRED));

        var rejected = changeService.decideApproval(id, new ChangeApprovalRequest(ChangeApprovalDecision.REJECT, "위험도 과다"));
        assertThat(rejected.id()).isEqualTo(id);
        String status = jdbc.queryForObject(
                "select status from approval where ticket_type='CHANGE' and ticket_id=?", String.class, id);
        assertThat(status).isEqualTo("REJECTED");
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
