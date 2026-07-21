package com.itsm.esm.integration;

import com.itsm.auth.domain.Department;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.esm.application.EsmCatalogService;
import com.itsm.esm.application.EsmChecklistService;
import com.itsm.esm.application.EsmHrCaseService;
import com.itsm.esm.application.EsmMetricsService;
import com.itsm.esm.application.EsmRequestService;
import com.itsm.esm.application.dto.CatalogItemDetailResponse;
import com.itsm.esm.application.dto.ChecklistTaskStatusRequest;
import com.itsm.esm.application.dto.CreateCatalogItemRequest;
import com.itsm.esm.application.dto.CreateHrCaseRequest;
import com.itsm.esm.application.dto.CreateRequestRequest;
import com.itsm.esm.application.dto.HrCaseStatusTransitionRequest;
import com.itsm.esm.application.dto.StatusTransitionRequest;
import com.itsm.esm.application.dto.UpdateCatalogItemRequest;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.ChecklistTaskStatus;
import com.itsm.esm.domain.EsmRequestStatus;
import com.itsm.esm.domain.HrCaseStatus;
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
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 ESM 전체 흐름을 검증한다: 카탈로그(체크리스트 템플릿)→온보딩/오프보딩
 * 요청 제출(체크리스트·자산회수 자동생성)→부서 상태전이(department 불일치 403)→하위 작업 완료(자동 완료)→
 * HR 케이스(HR_CASE_MANAGER 전용, 순차 전이)→지표 집계.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class EsmIntegrationTest {

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

    @Autowired EsmCatalogService catalogService;
    @Autowired EsmRequestService requestService;
    @Autowired EsmHrCaseService hrCaseService;
    @Autowired EsmChecklistService checklistService;
    @Autowired EsmMetricsService metricsService;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void as(Long userId, String... roles) {
        AuthPrincipal p = new AuthPrincipal(userId, "u" + userId + "@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p, null, List.of()));
    }

    private Long insertUser(String email, Department department) {
        jdbc.update("insert into app_user(email, password_hash, name, status, department, created_by) values (?,?,?,?,?,?)",
                email, "hash", email, "ACTIVE", department == null ? null : department.name(), "test");
        return jdbc.queryForObject("select id from app_user where email = ?", Long.class, email);
    }

    private Long insertCatalogItem(String name, Department department, String checklistTemplateType) {
        jdbc.update("insert into esm_catalog_item(name, department, checklist_template_type, created_by) values (?,?,?,?)",
                name, department.name(), checklistTemplateType, "test");
        return jdbc.queryForObject("select id from esm_catalog_item where name = ?", Long.class, name);
    }

    private void insertTemplateTask(Long catalogItemId, Department department, String description, int order) {
        jdbc.update("insert into esm_checklist_template_task(catalog_item_id, department, task_description, sort_order, created_by) values (?,?,?,?,?)",
                catalogItemId, department.name(), description, order, "test");
    }

    private Long insertAsset(String assetKey, String owner) {
        jdbc.update("insert into asset(asset_key, name, type, status, owner, created_by) values (?,?,?,?,?,?)",
                assetKey, "노트북", "HARDWARE", "OPERATION", owner, "test");
        return jdbc.queryForObject("select id from asset where asset_key = ?", Long.class, assetKey);
    }

    @Test
    void onboardingFlowGeneratesChecklistAndAutoCompletesOnAllTasksDone() {
        long ts = System.nanoTime();
        Long catalogItemId = insertCatalogItem("온보딩-" + ts, Department.HR, "ONBOARDING");
        insertTemplateTask(catalogItemId, Department.HR, "인사 서류 확인", 1);
        insertTemplateTask(catalogItemId, Department.IT, "장비 지급", 2);

        Long requesterId = insertUser("req" + ts + "@itsm.local", null);
        Long hrCoordId = insertUser("hrcoord" + ts + "@itsm.local", Department.HR);
        Long itCoordId = insertUser("itcoord" + ts + "@itsm.local", Department.IT);

        as(requesterId, "END_USER");
        var created = requestService.create(new CreateRequestRequest(catalogItemId, Map.of(), "김철수"));
        assertThat(created.checklistId()).isNotNull();

        var checklistDetail = checklistService.detail(created.checklistId());
        assertThat(checklistDetail.tasks()).hasSize(2);
        Long hrTaskId = checklistDetail.tasks().stream().filter(t -> t.department() == Department.HR).findFirst().orElseThrow().id();
        Long itTaskId = checklistDetail.tasks().stream().filter(t -> t.department() == Department.IT).findFirst().orElseThrow().id();

        as(hrCoordId, "DEPT_COORDINATOR");
        var transitioned = requestService.transition(created.id(), new StatusTransitionRequest(EsmRequestStatus.IN_PROGRESS, "처리중"));
        assertThat(transitioned.status()).isEqualTo("IN_PROGRESS");

        var afterHrTask = checklistService.updateTaskStatus(hrTaskId, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE));
        assertThat(afterHrTask.checklistStatus()).isEqualTo("IN_PROGRESS");

        as(itCoordId, "DEPT_COORDINATOR");
        var afterItTask = checklistService.updateTaskStatus(itTaskId, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE));
        assertThat(afterItTask.checklistStatus()).isEqualTo("COMPLETED");

        as(hrCoordId, "DEPT_COORDINATOR");
        var completed = requestService.transition(created.id(), new StatusTransitionRequest(EsmRequestStatus.COMPLETED, "완료"));
        assertThat(completed.status()).isEqualTo("COMPLETED");

        var metrics = metricsService.compute(null, null, Department.HR);
        assertThat(metrics.requestCount()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.onboardingCompletionRate()).isGreaterThan(0);
    }

    @Test
    void offboardingAutoAddsAssetRecoveryTaskAndBlocksMismatchedDepartment() {
        long ts = System.nanoTime();
        Long catalogItemId = insertCatalogItem("오프보딩-" + ts, Department.HR, "OFFBOARDING");
        insertTemplateTask(catalogItemId, Department.FACILITIES, "출입카드 회수", 1);
        Long assetId = insertAsset("AST-" + (ts % 100000), "이영희");

        Long requesterId = insertUser("req2" + ts + "@itsm.local", null);
        Long legalCoordId = insertUser("legalcoord" + ts + "@itsm.local", Department.LEGAL);

        as(requesterId, "END_USER");
        var created = requestService.create(new CreateRequestRequest(catalogItemId, Map.of(), "이영희"));

        var checklistDetail = checklistService.detail(created.checklistId());
        assertThat(checklistDetail.tasks()).hasSize(2);
        var assetTask = checklistDetail.tasks().stream().filter(t -> t.relatedAssetId() != null).findFirst().orElseThrow();
        assertThat(assetTask.department()).isEqualTo(Department.IT);
        assertThat(assetTask.relatedAssetId()).isEqualTo(assetId);

        // 소속 부서 불일치(LEGAL 담당자가 HR 요청 처리 시도) → 403
        as(legalCoordId, "DEPT_COORDINATOR");
        assertThatThrownBy(() -> requestService.transition(created.id(), new StatusTransitionRequest(EsmRequestStatus.IN_PROGRESS, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void hrCaseSequentialTransitionAndRoleRestriction() {
        long ts = System.nanoTime();
        Long endUserId = insertUser("eu" + ts + "@itsm.local", null);
        Long hrManagerId = insertUser("hrm" + ts + "@itsm.local", Department.HR);

        as(endUserId, "END_USER");
        assertThatThrownBy(() -> hrCaseService.create(new CreateHrCaseRequest("대상자", "고충", "내용")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));

        as(hrManagerId, "HR_CASE_MANAGER");
        var created = hrCaseService.create(new CreateHrCaseRequest("대상자", "고충 상담", "내용"));
        assertThat(created.status()).isEqualTo("INTAKE");

        assertThatThrownBy(() -> hrCaseService.transition(created.id(), new HrCaseStatusTransitionRequest(HrCaseStatus.INVESTIGATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));

        var documented = hrCaseService.transition(created.id(), new HrCaseStatusTransitionRequest(HrCaseStatus.DOCUMENTATION, "기록"));
        assertThat(documented.status()).isEqualTo("DOCUMENTATION");

        var detail = hrCaseService.detail(created.id());
        assertThat(detail.history()).hasSizeGreaterThanOrEqualTo(2);

        var list = hrCaseService.list(null, PageRequest.of(0, 20));
        assertThat(list.content()).isNotEmpty();
    }

    @Test
    void updateCatalogItemReplacesFormSchema() {
        // SRM과 동일한 자체 8×n 그리드 스키마(JSONB)로 저장·교체되는지 회귀 검증(2026-07-19 유지보수 요청, EAV 폐기).
        long ts = System.nanoTime();

        as(1L, "PROCESS_OWNER");
        Map<String, Object> formSchema = Map.of(
                "components", List.of(Map.of("key", "contractType", "label", "계약 유형", "type", "text",
                        "validation", Map.of("required", true))),
                "labels", List.of());
        CatalogItemDetailResponse created = catalogService.create(new CreateCatalogItemRequest(
                "계약서 검토" + ts, "d", Department.LEGAL, ChecklistTemplateType.NONE, null, formSchema));
        assertThat(created.formSchema().get("components")).asList()
                .extracting(c -> ((Map<String, Object>) c).get("key")).containsExactly("contractType");

        Map<String, Object> updatedSchema = Map.of(
                "components", List.of(Map.of("key", "contractType", "label", "계약 유형(수정)", "type", "text",
                        "validation", Map.of("required", true))),
                "labels", List.of());
        CatalogItemDetailResponse updated = catalogService.update(created.id(), new UpdateCatalogItemRequest(
                "계약서 검토" + ts, "updated", Department.LEGAL, ChecklistTemplateType.NONE, null, updatedSchema));

        assertThat(updated.formSchema().get("components")).asList()
                .extracting(c -> ((Map<String, Object>) c).get("label")).containsExactly("계약 유형(수정)");

        as(1L, "END_USER");
        assertThatThrownBy(() -> requestService.create(new CreateRequestRequest(created.id(), Map.of(), null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING));
    }
}
