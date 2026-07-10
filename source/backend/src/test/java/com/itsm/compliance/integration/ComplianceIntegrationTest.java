package com.itsm.compliance.integration;

import com.itsm.change.application.ChangeService;
import com.itsm.change.application.dto.CreateChangeRequest;
import com.itsm.change.domain.ChangeType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.compliance.application.ComplianceService;
import com.itsm.compliance.application.dto.CorrectiveActionCreateRequest;
import com.itsm.compliance.application.dto.CorrectiveActionStatusTransitionRequest;
import com.itsm.compliance.application.dto.CreateRequirementRequest;
import com.itsm.compliance.application.dto.LinkRequest;
import com.itsm.compliance.application.dto.OwnerRequest;
import com.itsm.compliance.application.dto.UpdateRequirementRequest;
import com.itsm.compliance.domain.ComplianceStatus;
import com.itsm.compliance.domain.CorrectiveActionStatus;
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
 * 실 PostgreSQL(Testcontainers)로 컴플라이언스 관리 전체 흐름을 검증한다: 등록→준수상태 계산(시정조치 미해결/전부
 * RESOLVED)→시정조치 순차전이(순서 위반 400)→변경 요청 연계(존재하지 않는 변경 400, change 상세에도 노출)→
 * 컴플라이언스 전용 감사로그 조회(이벤트타입 필터)→준수 현황 집계.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ComplianceIntegrationTest {

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
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/21_compliance_seed.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/21_compliance_seed.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired ComplianceService complianceService;
    @Autowired ChangeService changeService;
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
    void requirementLifecycleWithCorrectiveActionAndChangeLink() {
        long ts = System.nanoTime();
        Long coId = insertUser("co" + ts + "@itsm.local");
        as(coId, "COMPLIANCE_OFFICER");

        var created = complianceService.create(new CreateRequirementRequest(
                "개인정보보호법 준수 점검 " + ts, "개인정보보호법 제29조", "고객정보 처리 시스템"));
        assertThat(created.requirementKey()).startsWith("COMP-");

        var initialDetail = complianceService.detail(created.id());
        assertThat(initialDetail.complianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT.name());

        var action = complianceService.addCorrectiveAction(created.id(),
                new CorrectiveActionCreateRequest("취약점 점검 결과 미비 항목 보완 필요"));
        assertThat(action.status()).isEqualTo("DETECTED");

        var nonCompliantDetail = complianceService.detail(created.id());
        assertThat(nonCompliantDetail.complianceStatus()).isEqualTo(ComplianceStatus.NON_COMPLIANT.name());

        assertThatThrownBy(() -> complianceService.transitionCorrectiveAction(action.id(),
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.RESOLVED)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));

        complianceService.transitionCorrectiveAction(action.id(),
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.IN_PROGRESS));
        var resolved = complianceService.transitionCorrectiveAction(action.id(),
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.RESOLVED));
        assertThat(resolved.status()).isEqualTo("RESOLVED");

        var compliantAgainDetail = complianceService.detail(created.id());
        assertThat(compliantAgainDetail.complianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT.name());

        var ownerResponse = complianceService.assignOwner(created.id(), new OwnerRequest(coId));
        assertThat(ownerResponse.owner()).isNotBlank();

        complianceService.update(created.id(), new UpdateRequirementRequest(
                "개인정보보호법 준수 점검(수정) " + ts, "개인정보보호법 제29조 개정", "고객정보 처리 시스템 전체"));
        var updatedDetail = complianceService.detail(created.id());
        assertThat(updatedDetail.name()).contains("수정");

        // ---- 변경 연계 ----
        assertThatThrownBy(() -> complianceService.link(created.id(), new LinkRequest(999999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));

        as(coId, "CHANGE_MANAGER");
        var change = changeService.create(new CreateChangeRequest(
                "컴플라이언스 연계 변경 " + ts, "설명", ChangeType.NORMAL, null, null, null, null, null, null));

        as(coId, "COMPLIANCE_OFFICER");
        complianceService.link(created.id(), new LinkRequest(change.id()));
        var linkedDetail = complianceService.detail(created.id());
        assertThat(linkedDetail.linkedChanges()).extracting(l -> l.id()).contains(change.id());

        as(coId, "CHANGE_MANAGER");
        var changeDetail = changeService.detail(change.id());
        assertThat(changeDetail.links()).anySatisfy(l -> {
            assertThat(l.type()).isEqualTo("COMPLIANCE_REQUIREMENT");
            assertThat(l.targetKey()).isEqualTo(created.requirementKey());
        });

        // ---- 컴플라이언스 전용 감사 로그 조회(이벤트 타입 필터) ----
        as(coId, "COMPLIANCE_OFFICER");
        var logs = complianceService.auditLogs(created.id(), null, null);
        assertThat(logs).isNotEmpty();
        assertThat(logs).allSatisfy(l -> assertThat(l.eventType()).startsWith("COMPLIANCE_"));
        assertThat(logs).extracting(l -> l.eventType())
                .contains("COMPLIANCE_REQ_CREATE", "COMPLIANCE_REQ_UPDATE", "COMPLIANCE_ACTION_STATUS_CHANGE");

        // ---- 목록 필터(계산값 일관성) ----
        var page = complianceService.list(ComplianceStatus.COMPLIANT, null, null, PageRequest.of(0, 50));
        assertThat(page.content()).extracting(r -> r.id()).contains(created.id());

        // ---- 준수 현황 ----
        var metrics = complianceService.metrics(null, null);
        assertThat(metrics.totalRequirements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void addCorrectiveActionOnNonExistentRequirementThrows404() {
        long ts = System.nanoTime();
        Long coId = insertUser("co2" + ts + "@itsm.local");
        as(coId, "COMPLIANCE_OFFICER");

        assertThatThrownBy(() -> complianceService.addCorrectiveAction(999999L,
                new CorrectiveActionCreateRequest("내용")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COMPLIANCE_REQUIREMENT_NOT_FOUND));
    }

    @Test
    void nonOfficerRoleForbidden() {
        long ts = System.nanoTime();
        Long userId = insertUser("nonco" + ts + "@itsm.local");
        as(userId, "END_USER");

        assertThatThrownBy(() -> complianceService.list(null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
