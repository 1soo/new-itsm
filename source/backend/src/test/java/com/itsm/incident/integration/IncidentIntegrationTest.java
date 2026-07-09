package com.itsm.incident.integration;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.incident.application.IncidentService;
import com.itsm.incident.application.dto.AssignRoleRequest;
import com.itsm.incident.application.dto.CreateIncidentRequest;
import com.itsm.incident.application.dto.EscalateRequest;
import com.itsm.incident.application.dto.IncidentCreatedResponse;
import com.itsm.incident.application.dto.PostmortemRequest;
import com.itsm.incident.application.dto.ResolveRequest;
import com.itsm.incident.application.dto.SeverityChangeRequest;
import com.itsm.incident.application.dto.StatusTransitionRequest;
import com.itsm.incident.application.dto.TimelineUpdateRequest;
import com.itsm.incident.domain.EscalationType;
import com.itsm.incident.domain.IncidentStatus;
import com.itsm.incident.domain.Priority;
import com.itsm.incident.domain.ResponseRole;
import com.itsm.incident.domain.Severity;
import com.itsm.common.ticket.Visibility;
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
 * 실 PostgreSQL(Testcontainers)로 인시던트 전체 흐름을 검증한다.
 * 등록→상태전이→심각도 이력(append)→역할 배정→에스컬레이션→타임라인(INTERNAL/EXTERNAL)→
 * 해결(MTTx 계산)→포스트모템(rootCause 필수)→지표 집계, 그리고 postmortemRequired 상태 규칙(TC-INC-037)을
 * 실 트랜잭션·실 FK로 재현한다(단위 mock 사각지대 보완). 실제 DDL(01/03/04/06)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class IncidentIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/08_problem_schema.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired IncidentService incidentService;
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

    @Test
    void fullIncidentLifecycle() {
        long ts = System.nanoTime();
        Long imId = insertUser("im" + ts + "@itsm.local");
        Long responderId = insertUser("resp" + ts + "@itsm.local");

        // 등록(INCIDENT_MANAGER)
        as(imId, "INCIDENT_MANAGER");
        IncidentCreatedResponse created = incidentService.create(
                new CreateIncidentRequest("결제 지연", "결제 API 지연", Severity.SEV1, "payment", "checkout"));
        Long id = created.id();
        assertThat(created.ticketKey()).startsWith("INC-");
        assertThat(created.status()).isEqualTo("NEW");

        // 심각도·우선순위 변경 → severity_history append
        incidentService.changeSeverity(id, new SeverityChangeRequest(Severity.SEV2, Priority.P2));
        Long histCount = jdbc.queryForObject(
                "select count(*) from incident_severity_history where incident_id = ?", Long.class, id);
        assertThat(histCount).isEqualTo(1);

        // 상태 전이 NEW→IN_PROGRESS
        var status = incidentService.transition(id, new StatusTransitionRequest(IncidentStatus.IN_PROGRESS, "대응 착수"));
        assertThat(status.status()).isEqualTo("IN_PROGRESS");

        // 대응 역할 배정(IM 전용)
        var detail = incidentService.assignRole(id, new AssignRoleRequest(responderId, ResponseRole.TECH_LEAD));
        assertThat(detail.responders()).extracting("role").contains("TECH_LEAD");

        // 에스컬레이션
        incidentService.escalate(id, new EscalateRequest(responderId, EscalationType.HIERARCHICAL, "상위 필요"));

        // 타임라인 업데이트(INTERNAL/EXTERNAL)
        incidentService.addUpdate(id, new TimelineUpdateRequest("내부 조사 중", Visibility.INTERNAL));
        incidentService.addUpdate(id, new TimelineUpdateRequest("서비스 영향 공지", Visibility.EXTERNAL));
        Long externalCount = jdbc.queryForObject(
                "select count(*) from timeline_event where ticket_type='INCIDENT' and ticket_id=? and visibility='EXTERNAL'",
                Long.class, id);
        assertThat(externalCount).isEqualTo(1);

        // 상태 전이 IN_PROGRESS→RESOLVED via resolve + MTTx 계산
        OffsetDateTime start = OffsetDateTime.parse("2026-07-09T10:00:00Z");
        var resolve = incidentService.resolve(id, new ResolveRequest(
                start, start.plusMinutes(4), start.plusMinutes(40), "핫픽스 배포"));
        assertThat(resolve.status()).isEqualTo("RESOLVED");
        assertThat(resolve.metrics().mttdMinutes()).isEqualTo(4);
        assertThat(resolve.metrics().mttaMinutes()).isEqualTo(36);
        assertThat(resolve.metrics().mttrMinutes()).isEqualTo(40);

        // SEV2 해결·PM 미작성 → postmortemRequired=true (상세)
        assertThat(incidentService.detail(id).status()).isEqualTo("RESOLVED");

        // 포스트모템 조회(미작성) → 404
        assertThatThrownBy(() -> incidentService.getPostmortem(id))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.POSTMORTEM_NOT_FOUND));

        // 포스트모템 작성(rootCause 필수)
        incidentService.savePostmortem(id, new PostmortemRequest("요약", "타임라인",
                List.of("왜1", "왜2", "왜3"), "설정 롤백 누락",
                List.of(new com.itsm.incident.application.dto.ActionItemDto("배포 게이트 추가", "sre", null, "OPEN"))));
        var pm = incidentService.getPostmortem(id);
        assertThat(pm.rootCause()).isEqualTo("설정 롤백 누락");
        assertThat(pm.fiveWhys()).hasSize(3);
        assertThat(pm.actionItems()).hasSize(1);

        // 포스트모템 수정(재PUT) — fiveWhys 교체
        incidentService.savePostmortem(id, new PostmortemRequest("요약2", "타임라인2",
                List.of("왜A"), "근본원인 갱신", List.of()));
        var pm2 = incidentService.getPostmortem(id);
        assertThat(pm2.rootCause()).isEqualTo("근본원인 갱신");
        assertThat(pm2.fiveWhys()).containsExactly("왜A");

        // 지표 집계
        var metrics = incidentService.metrics(null, null);
        assertThat(metrics.count()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.severityDistribution().SEV2()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.avgMttrMinutes()).isGreaterThan(0.0);
    }

    @Test
    void nonManagerCannotAssignRole() {
        long ts = System.nanoTime();
        Long agentId = insertUser("agent" + ts + "@itsm.local");
        Long targetId = insertUser("t" + ts + "@itsm.local");

        as(agentId, "INCIDENT_MANAGER");
        IncidentCreatedResponse created = incidentService.create(
                new CreateIncidentRequest("장애", null, Severity.SEV3, null, null));

        // SERVICE_DESK_AGENT 역할 배정 시도 → 403
        as(agentId, "SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> incidentService.assignRole(created.id(),
                new AssignRoleRequest(targetId, ResponseRole.COMMS)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
