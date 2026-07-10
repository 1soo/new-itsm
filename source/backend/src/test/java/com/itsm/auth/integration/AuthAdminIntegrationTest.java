package com.itsm.auth.integration;

import com.itsm.auth.application.AuthService;
import com.itsm.auth.application.RoleService;
import com.itsm.auth.application.UserAdminService;
import com.itsm.auth.application.dto.CreateRoleRequest;
import com.itsm.auth.application.dto.CreateUserRequest;
import com.itsm.auth.application.dto.LoginRequest;
import com.itsm.auth.application.dto.RoleCreatedResponse;
import com.itsm.auth.application.dto.UserDetailResponse;
import com.itsm.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실제 PostgreSQL(Testcontainers)로 검증하는 통합 테스트.
 * 단위 테스트가 AuditLogService를 mock 처리해 잡지 못한 회귀(계정 생성 시 감사 FK,
 * 로그인 실패 감사의 롤백 보존)를 실 트랜잭션·실 FK로 재현·방지한다.
 * dev-database의 실제 DDL(source/db/sql/01_schema.sql)을 그대로 사용한다(FK 포함).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class AuthAdminIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("itsm")
            .withUsername("itsm")
            .withPassword("itsm")
            // dev-database의 실제 DDL(스키마만; 시드는 테스트가 자체 생성하므로 제외)
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/01_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/01_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/03_common_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/03_common_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/04_srm_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/04_srm_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/06_incident_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/06_incident_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/08_problem_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/08_problem_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/10_change_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/10_change_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/12_knowledge_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/12_knowledge_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/14_asset_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/14_asset_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/16_esm_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/16_esm_schema.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Paths.get("../db/sql/18_vulnerability_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/18_vulnerability_schema.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired UserAdminService userAdminService;
    @Autowired RoleService roleService;
    @Autowired AuthService authService;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void createUserPersistsUserAndAuditWithoutFkViolation() {
        RoleCreatedResponse role = roleService.create(new CreateRoleRequest("END_USER", "최종 사용자", "desc"));

        UserDetailResponse created = userAdminService.create(
                new CreateUserRequest("newuser@itsm.local", "신규 사용자", "Welcome123!", List.of(role.id())));

        assertThat(created.id()).isNotNull();
        assertThat(created.roles()).containsExactly("END_USER");

        Integer users = jdbcTemplate.queryForObject(
                "select count(*) from app_user where email = ?", Integer.class, "newuser@itsm.local");
        Integer auditRows = jdbcTemplate.queryForObject(
                "select count(*) from audit_log where event_type = 'USER_CHANGE' and target = ?",
                Integer.class, "newuser@itsm.local");
        assertThat(users).isEqualTo(1);
        assertThat(auditRows).isEqualTo(1);
    }

    @Test
    void failedLoginPersistsFailureAuditDespiteRollback() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@itsm.local", "whatever")))
                .isInstanceOf(BusinessException.class);

        Integer failureRows = jdbcTemplate.queryForObject(
                "select count(*) from audit_log where event_type = 'LOGIN' and result = 'FAILURE' and actor_email = ?",
                Integer.class, "ghost@itsm.local");
        assertThat(failureRows).isEqualTo(1);
    }
}
