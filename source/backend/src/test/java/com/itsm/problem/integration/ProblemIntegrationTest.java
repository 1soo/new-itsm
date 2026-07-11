package com.itsm.problem.integration;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.domain.AssetType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.incident.application.dto.LinkProblemRequest;
import com.itsm.incident.application.IncidentService;
import com.itsm.problem.application.ProblemService;
import com.itsm.problem.application.dto.ActionCreateRequest;
import com.itsm.problem.application.dto.ActionStatusRequest;
import com.itsm.problem.application.dto.CloseRequest;
import com.itsm.problem.application.dto.CreateProblemRequest;
import com.itsm.problem.application.dto.KnownErrorCreateRequest;
import com.itsm.problem.application.dto.LinkRequest;
import com.itsm.problem.application.dto.ProblemCreatedResponse;
import com.itsm.problem.application.dto.RcaRequest;
import com.itsm.problem.application.dto.StatusTransitionRequest;
import com.itsm.problem.application.dto.WorkaroundRequest;
import com.itsm.problem.domain.ActionStatus;
import com.itsm.problem.domain.Level;
import com.itsm.problem.domain.LinkTargetType;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.ProblemStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
 * 실 PostgreSQL(Testcontainers)로 문제(problem) 전체 흐름을 검증한다.
 * 등록(우선순위 매트릭스)→상태 6단계 전이(순서 위반 400)→RCA(5 Whys)→워크어라운드→
 * 알려진오류(KEDB)+검색→인시던트 연계(양방향 ticket_link)→후속조치·종료(warning/force),
 * 그리고 인시던트→문제 연계(API-INC-012) 완성을 실 트랜잭션·실 FK로 재현한다.
 * 실제 DDL(01/03/04/06/08)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ProblemIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/18_vulnerability_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/20_compliance_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/20_compliance_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/22_infra_monitoring_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/22_infra_monitoring_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/24_auth_menu_columns.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/24_auth_menu_columns.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired ProblemService problemService;
    @Autowired IncidentService incidentService;
    @Autowired AssetService assetService;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void as(String... roles) {
        AuthPrincipal p = new AuthPrincipal(1L, "pm@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p, null, List.of()));
    }

    private Long insertIncident(String key) {
        jdbc.update("insert into incident(ticket_key, summary, severity, status, created_by) values (?,?,?,?,?)",
                key, "장애", "SEV1", "NEW", "test");
        return jdbc.queryForObject("select id from incident where ticket_key = ?", Long.class, key);
    }

    @Test
    void fullProblemLifecycle() {
        as("PROBLEM_MANAGER");

        // 등록 — impact=HIGH, urgency=HIGH → P1
        ProblemCreatedResponse created = problemService.create(new CreateProblemRequest(
                "결제 반복 실패", "다건 인시던트로 확인", ProblemOrigin.REACTIVE, "3건 이상 발생",
                Level.HIGH, Level.HIGH, "payment"));
        Long id = created.id();
        assertThat(created.ticketKey()).startsWith("PRB-");
        assertThat(created.status()).isEqualTo("DETECTION");
        assertThat(created.priority()).isEqualTo("P1");

        // 상태 6단계 순차 전이
        problemService.transition(id, new StatusTransitionRequest(ProblemStatus.CLASSIFICATION, "분류"));
        problemService.transition(id, new StatusTransitionRequest(ProblemStatus.INVESTIGATION, "조사"));

        // 순서 위반 전이 → 400
        assertThatThrownBy(() -> problemService.transition(id,
                new StatusTransitionRequest(ProblemStatus.RESOLVED_CLOSED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));

        // RCA(5 Whys) 저장
        problemService.saveRca(id, new RcaRequest("커넥션 풀 고갈", List.of("왜1", "왜2", "왜3"), "CONFIG"));
        var detail = problemService.detail(id);
        assertThat(detail.rca().rootCause()).isEqualTo("커넥션 풀 고갈");
        assertThat(detail.rca().fiveWhys()).hasSize(3);

        // RCA 재저장 — 5 Whys 교체
        problemService.saveRca(id, new RcaRequest("풀 크기 부족", List.of("단일 원인"), "CONFIG"));
        assertThat(problemService.detail(id).rca().fiveWhys()).containsExactly("단일 원인");

        // 이후 단계 전이
        problemService.transition(id, new StatusTransitionRequest(ProblemStatus.KNOWN_ERROR, null));

        // 워크어라운드 — 빈 값 400
        assertThatThrownBy(() -> problemService.addWorkaround(id, new WorkaroundRequest("  ", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.WORKAROUND_CONTENT_REQUIRED));
        problemService.addWorkaround(id, new WorkaroundRequest("커넥션 풀 상향 후 재기동", null));
        assertThat(problemService.detail(id).workaround()).contains("재기동");
        problemService.transition(id, new StatusTransitionRequest(ProblemStatus.WORKAROUND, null));

        // 알려진 오류(KEDB) 등록 + 검색
        problemService.createKnownError(id, new KnownErrorCreateRequest(
                "결제 커넥션 풀 고갈", "풀 크기 부족", "풀 상향"));
        var kedb = problemService.searchKnownErrors("커넥션", PageRequest.of(0, 20));
        assertThat(kedb.content()).extracting("title").contains("결제 커넥션 풀 고갈");
        assertThat(kedb.content().get(0).problemKey()).startsWith("PRB-");
        // 매칭 없으면 빈 목록
        assertThat(problemService.searchKnownErrors("존재하지않는키워드zzz", PageRequest.of(0, 20)).content()).isEmpty();

        // 인시던트 연계(양방향 ticket_link)
        Long incId = insertIncident("INC-2026-9001");
        var link = problemService.link(id, new LinkRequest(LinkTargetType.INCIDENT, incId, false));
        assertThat(link.targetType()).isEqualTo("INCIDENT");
        Long linkCount = jdbc.queryForObject(
                "select count(*) from ticket_link where (source_type='PROBLEM' and source_id=?) "
                        + "or (source_type='INCIDENT' and source_id=?)", Long.class, id, incId);
        assertThat(linkCount).isEqualTo(2);
        assertThat(problemService.detail(id).linkedIncidents()).extracting("ticketKey").contains("INC-2026-9001");

        // 존재하지 않는 대상 → 400
        assertThatThrownBy(() -> problemService.link(id, new LinkRequest(LinkTargetType.INCIDENT, 999999L, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));

        // CHANGE 연계 — 신규 생성(양방향 ticket_link)
        var changeLink = problemService.link(id, new LinkRequest(LinkTargetType.CHANGE, null, true));
        assertThat(changeLink.targetType()).isEqualTo("CHANGE");
        Long changeLinkCount = jdbc.queryForObject(
                "select count(*) from ticket_link where (source_type='PROBLEM' and source_id=? and target_type='CHANGE') "
                        + "or (source_type='CHANGE' and target_type='PROBLEM' and target_id=?)", Long.class, id, id);
        assertThat(changeLinkCount).isEqualTo(2);
        assertThat(problemService.detail(id).linkedChanges()).extracting("ticketKey")
                .anyMatch(key -> key != null && key.toString().startsWith("CHG-"));

        // 존재하지 않는 변경 연계 → 400
        assertThatThrownBy(() -> problemService.link(id, new LinkRequest(LinkTargetType.CHANGE, 999999L, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));

        // 후속 조치 등록
        var action = problemService.addAction(id, new ActionCreateRequest("풀 모니터링 알람 추가", "sre", null));
        assertThat(action.status()).isEqualTo("IN_PROGRESS");

        // 미해결 후속조치 + force=false → 경고, 종료 안 됨
        var closeWarn = problemService.close(id, new CloseRequest(false));
        assertThat(closeWarn.status()).isNotEqualTo("RESOLVED_CLOSED");
        assertThat(closeWarn.warning()).contains("1건");

        // 조치 완료 후 종료 → RESOLVED_CLOSED, warning=null
        problemService.changeActionStatus(id, action.id(), new ActionStatusRequest(ActionStatus.DONE));
        var closed = problemService.close(id, new CloseRequest(false));
        assertThat(closed.status()).isEqualTo("RESOLVED_CLOSED");
        assertThat(closed.warning()).isNull();
    }

    @Test
    void detailLinksExposeAssetKeyViaAssetSideLink() {
        // REQ-ITAM-006(TC-REG-002): API-ITAM-007(자산→문제 연계)로 생성된 양방향 ticket_link가
        // 문제 상세(linkedAssets)에도 노출되는지 검증.
        as("PROBLEM_MANAGER");
        var created = problemService.create(new CreateProblemRequest(
                "자산 연계 확인용 문제", null, ProblemOrigin.REACTIVE, null, Level.LOW, Level.LOW, null));
        Long id = created.id();

        as("ASSET_MANAGER");
        var asset = assetService.create(new CreateAssetRequest("문제연계확인자산" + System.nanoTime(), AssetType.SOFTWARE,
                null, null, null, null, null, null, null, null));
        assetService.linkAsset(asset.id(), new LinkAssetRequest(TicketType.PROBLEM, id));

        as("PROBLEM_MANAGER");
        assertThat(problemService.detail(id).linkedAssets()).extracting("ticketKey").containsExactly(asset.assetKey());
    }

    @Test
    void incidentToProblemLinkCompletesStub() {
        // API-INC-012: 인시던트에서 신규 문제 생성 + 양방향 연계
        as("INCIDENT_MANAGER");
        Long incId = insertIncident("INC-2026-9100");

        var link = incidentService.linkProblem(incId, new LinkProblemRequest(null, true));
        assertThat(link.incidentId()).isEqualTo(incId);
        assertThat(link.problemId()).isNotNull();

        // 신규 PRB 티켓 생성 확인
        String prbKey = jdbc.queryForObject("select ticket_key from problem where id = ?", String.class, link.problemId());
        assertThat(prbKey).startsWith("PRB-");

        // 양방향 링크 2건
        Long linkCount = jdbc.queryForObject(
                "select count(*) from ticket_link where (source_type='INCIDENT' and source_id=?) "
                        + "or (source_type='PROBLEM' and source_id=?)", Long.class, incId, link.problemId());
        assertThat(linkCount).isEqualTo(2);
    }

    @Test
    void nonManagerCannotCreateProblem() {
        as("SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> problemService.create(new CreateProblemRequest(
                "요약", null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
