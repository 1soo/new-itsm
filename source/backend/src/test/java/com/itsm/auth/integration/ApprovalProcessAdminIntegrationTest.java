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
                    "/docker-entrypoint-initdb.d/31_sidebar_menu_label_cleanup.sql");

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
                "INCIDENT", null, "flush test", null, List.of(),
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
                "PROBLEM", null, "requester role test", null, List.of(), List.of()));

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
}
