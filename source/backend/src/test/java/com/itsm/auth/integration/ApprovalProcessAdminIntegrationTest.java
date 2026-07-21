package com.itsm.auth.integration;

import com.itsm.auth.application.ApprovalProcessAdminService;
import com.itsm.auth.application.RoleService;
import com.itsm.auth.application.dto.ApprovalProcessDetailResponse;
import com.itsm.auth.application.dto.ApprovalProcessStepInput;
import com.itsm.auth.application.dto.CreateApprovalProcessRequest;
import com.itsm.auth.application.dto.CreateRoleRequest;
import com.itsm.auth.application.dto.RoleCreatedResponse;
import com.itsm.auth.application.dto.UpdateApprovalProcessRequest;
import com.itsm.common.approval.domain.DecisionMode;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 승인 프로세스 관리자 CRUD(API-AUTH-027/028)를 검증한다.
 * 단위 mock 테스트로는 잡을 수 없는 회귀(TC-ADM-006: steps/requesterRoleIds 교체 시 같은 트랜잭션 내
 * delete 후 재삽입이 Hibernate 기본 flush 순서(insert가 delete보다 먼저 나감) 때문에
 * UNIQUE(approval_process_id, step_no) 위반으로 500이 나던 버그)를 실 트랜잭션으로 재현·방지한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class ApprovalProcessAdminIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/40_esm_form_schema_jsonb.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/41_approval_process_target_state.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/41_approval_process_target_state.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired ApprovalProcessAdminService approvalProcessAdminService;
    @Autowired RoleService roleService;
    @Autowired JdbcTemplate jdbc;

    @Test
    void replacingStepsInSameTransactionDoesNotViolateUniqueConstraint() {
        RoleCreatedResponse role = roleService.create(new CreateRoleRequest("APPROVER_" + System.nanoTime(), "승인자", null));

        ApprovalProcessDetailResponse created = approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                "INCIDENT", null, null, "flush test", null, List.of(),
                List.of(new ApprovalProcessStepInput(DecisionMode.OR, List.of(role.id())))));
        assertThat(created.steps()).hasSize(1);
        assertThat(created.steps().get(0).decisionMode()).isEqualTo("OR");

        ApprovalProcessDetailResponse updated = approvalProcessAdminService.update(created.id(),
                new UpdateApprovalProcessRequest(null, null, null,
                        List.of(new ApprovalProcessStepInput(DecisionMode.AND, List.of(role.id())))));

        assertThat(updated.steps()).hasSize(1);
        assertThat(updated.steps().get(0).decisionMode()).isEqualTo("AND");
        Integer stepCount = jdbc.queryForObject(
                "select count(*) from approval_process_step where approval_process_id = ?", Integer.class, created.id());
        assertThat(stepCount).isEqualTo(1);
    }

    @Test
    void replacingRequesterRoleIdsTwiceInARowSucceeds() {
        RoleCreatedResponse role = roleService.create(new CreateRoleRequest("REQ_ROLE_" + System.nanoTime(), "요청역할", null));

        ApprovalProcessDetailResponse created = approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                "PROBLEM", null, null, "requester role test", null, List.of(), List.of()));

        approvalProcessAdminService.update(created.id(),
                new UpdateApprovalProcessRequest(null, null, List.of(role.id()), null));
        ApprovalProcessDetailResponse second = approvalProcessAdminService.update(created.id(),
                new UpdateApprovalProcessRequest(null, null, List.of(role.id()), null));

        assertThat(second.requesterRoleIds()).containsExactly(role.id());
        Integer rows = jdbc.queryForObject(
                "select count(*) from approval_process_requester_role where approval_process_id = ?",
                Integer.class, created.id());
        assertThat(rows).isEqualTo(1);
    }

    // 2026-07-15 승인 프로세스 범위 우선순위 재설계(3축 독립 스코프) 회귀 테스트.

    @Test
    void createWithNullDomainYieldsTierZeroAndSecondCatchAllConflicts() {
        ApprovalProcessDetailResponse created = approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                null, null, null, "전체 도메인 캐치올 " + System.nanoTime(), null, List.of(), List.of()));

        assertThat(created.domain()).isNull();
        Short tier = jdbc.queryForObject("select priority_tier from approval_process where id = ?", Short.class, created.id());
        assertThat(tier).isEqualTo((short) 0);

        // tier=0(전체 미지정) 캐치올은 시스템 전체 1개만 허용된다.
        assertThatThrownBy(() -> approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                null, null, null, "캐치올 2 " + System.nanoTime(), null, List.of(), List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.APPROVAL_PROCESS_PRIORITY_CONFLICT));
    }

    @Test
    void createNullDomainWithRequestSubtypeKeyThrowsValidationError() {
        assertThatThrownBy(() -> approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                null, null, "SOME_SUBTYPE", "잘못된 조합 " + System.nanoTime(), null, List.of(), List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createSecondDomainOnlyRuleConflictsAtTierEleven() {
        approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                "ASSET", null, null, "도메인만 1 " + System.nanoTime(), null, List.of(), List.of()));

        assertThatThrownBy(() -> approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                "ASSET", null, null, "도메인만 2 " + System.nanoTime(), null, List.of(), List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.APPROVAL_PROCESS_PRIORITY_CONFLICT));
    }

    @Test
    void createSecondDomainSubtypeRuleConflictsAtTierTwentyThree() {
        approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                "CHANGE", null, "STANDARD", "도메인+요청유형 1 " + System.nanoTime(), null, List.of(), List.of()));

        assertThatThrownBy(() -> approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                "CHANGE", null, "STANDARD", "도메인+요청유형 2 " + System.nanoTime(), null, List.of(), List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.APPROVAL_PROCESS_PRIORITY_CONFLICT));
    }

    @Test
    void createOverlappingRoleOnlyRulesConflictAtTierFourteenButDisjointRolesSucceed() {
        RoleCreatedResponse roleA = roleService.create(new CreateRoleRequest("ROLE_A_" + System.nanoTime(), "역할A", null));
        RoleCreatedResponse roleB = roleService.create(new CreateRoleRequest("ROLE_B_" + System.nanoTime(), "역할B", null));
        RoleCreatedResponse roleC = roleService.create(new CreateRoleRequest("ROLE_C_" + System.nanoTime(), "역할C", null));

        approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                null, null, null, "역할만 1 " + System.nanoTime(), null, List.of(roleA.id(), roleB.id()), List.of()));

        // 역할 조합이 겹치면(roleB 공유) 409
        assertThatThrownBy(() -> approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                null, null, null, "역할만 2 " + System.nanoTime(), null, List.of(roleB.id(), roleC.id()), List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.APPROVAL_PROCESS_PRIORITY_CONFLICT));

        // 역할 조합이 완전히 겹치지 않으면 정상 생성
        ApprovalProcessDetailResponse disjoint = approvalProcessAdminService.create(new CreateApprovalProcessRequest(
                null, null, null, "역할만 3 " + System.nanoTime(), null, List.of(roleC.id()), List.of()));
        assertThat(disjoint.requesterRoleIds()).containsExactly(roleC.id());
    }
}
