package com.itsm.srm.integration;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.domain.AssetType;
import com.itsm.common.approval.application.ApprovalInstanceService;
import com.itsm.common.approval.domain.DecisionType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.srm.application.ServiceCatalogService;
import com.itsm.srm.application.ServiceRequestService;
import com.itsm.srm.application.dto.AssignRequest;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.RequestCreatedResponse;
import com.itsm.srm.application.dto.StatusTransitionRequest;
import com.itsm.srm.application.dto.UpdateCatalogItemRequest;
import com.itsm.srm.domain.RequestStatus;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 공용 승인 엔진(common.approval) + SRM 게이트 연동을 검증한다.
 * 게이트 차단(409, approvalRequestId 포함)·승인 후 재시도 통과·이미 결정된 역할 슬롯 재처리 차단(409)을
 * 실 트랜잭션으로 재현한다(단위 mock 사각지대 보완). 실제 DDL(01~26)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class SrmApprovalIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/36_srm_form_schema_jsonb.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

    @Autowired ServiceCatalogService catalogService;
    @Autowired ServiceRequestService requestService;
    @Autowired ApprovalInstanceService approvalInstanceService;
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

    private Map<String, Object> emptyFormSchema() {
        return Map.of("display", "form", "components", List.of());
    }

    private Map<String, Object> formSchemaWith(String key, boolean required) {
        return Map.of("display", "form", "components", List.of(
                Map.of("key", key, "label", key, "type", "textfield", "input", true,
                        "validate", Map.of("required", required))));
    }

    private Long insertUser(String email) {
        jdbc.update("insert into app_user(email, password_hash, name, status, created_by) values (?,?,?,?,?)",
                email, "hash", email, "ACTIVE", "test");
        return jdbc.queryForObject("select id from app_user where email = ?", Long.class, email);
    }

    private Long roleIdOf(String roleCode) {
        jdbc.update("insert into role(role_code, role_name, created_by) values (?,?,?) on conflict (role_code) do nothing",
                roleCode, roleCode, "test");
        return jdbc.queryForObject("select id from role where role_code = ?", Long.class, roleCode);
    }

    /** 도메인+요청유형(tier=23) 규칙 1건 + 1차 OR 승인(주어진 역할)을 카탈로그 항목별로 시딩한다(테스트 간 격리). */
    private void seedSubtypeProcess(String domain, String requestSubtypeKey, String roleCode) {
        jdbc.update("insert into approval_process(domain, request_subtype_key, priority_tier, name, created_by) values (?,?,23,?,?)",
                domain, requestSubtypeKey, "요청유형 규칙", "test");
        Long processId = jdbc.queryForObject(
                "select id from approval_process where domain = ? and request_subtype_key = ?",
                Long.class, domain, requestSubtypeKey);
        jdbc.update("insert into approval_process_step(approval_process_id, step_no, decision_mode, created_by) values (?,1,'OR',?)",
                processId, "test");
        Long stepId = jdbc.queryForObject(
                "select id from approval_process_step where approval_process_id = ? and step_no = 1", Long.class, processId);
        jdbc.update("insert into approval_process_step_role(step_id, role_id, created_by) values (?,?,?)",
                stepId, roleIdOf(roleCode), "test");
    }

    /** 전체 도메인 캐치올(domain=NULL, tier=0) 규칙 1건 + 1차 OR 승인(주어진 역할)을 시딩한다. */
    private Long seedCatchAllProcess(String roleCode, String name) {
        jdbc.update("insert into approval_process(domain, priority_tier, name, created_by) values (null,0,?,?)",
                name, "test");
        Long processId = jdbc.queryForObject(
                "select id from approval_process where domain is null and priority_tier = 0 and name = ?",
                Long.class, name);
        jdbc.update("insert into approval_process_step(approval_process_id, step_no, decision_mode, created_by) values (?,1,'OR',?)",
                processId, "test");
        Long stepId = jdbc.queryForObject(
                "select id from approval_process_step where approval_process_id = ? and step_no = 1", Long.class, processId);
        jdbc.update("insert into approval_process_step_role(step_id, role_id, created_by) values (?,?,?)",
                stepId, roleIdOf(roleCode), "test");
        return processId;
    }

    @Test
    void gateBlocksThenApprovedRetrySucceeds() {
        long ts = System.nanoTime();
        String domain = "SERVICE_REQUEST";

        Long requesterId = insertUser("req" + ts + "@itsm.local");
        Long approverId = insertUser("apr" + ts + "@itsm.local");
        Long agentId = insertUser("agt" + ts + "@itsm.local");

        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse item = catalogService.create(new CreateCatalogItemRequest(
                "Item" + ts, "d", null, null, null, null, null,
                formSchemaWith("note", false)));
        seedSubtypeProcess(domain, String.valueOf(item.id()), "APPROVER");

        as(requesterId, "END_USER");
        RequestCreatedResponse created = requestService.create(new CreateRequestRequest(item.id(), Map.of()));
        Long rid = created.id();

        as(agentId, "SERVICE_DESK_AGENT");
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.VALIDATED, null));
        requestService.assign(rid, new AssignRequest(agentId));
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.ROUTED, null));

        // 게이트 차단: 인스턴스 없음 → 스냅샷 생성 + 409(approvalRequestId 포함)
        BusinessException blocked = (BusinessException) org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null)));
        assertThat(blocked.getErrorCode()).isEqualTo(ErrorCode.APPROVAL_PENDING);
        Long approvalRequestId = blocked.getApprovalRequestId();
        assertThat(approvalRequestId).isNotNull();

        // 이미 진행 중인 인스턴스 재시도도 409
        assertThatThrownBy(() ->
                requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.APPROVAL_PENDING));

        // 승인자 결정 → APPROVED
        as(approverId, "APPROVER");
        var decision = approvalInstanceService.decide(approvalRequestId, DecisionType.APPROVE, "ok");
        assertThat(decision.requestStatus()).isEqualTo("APPROVED");

        // 재시도 시 게이트 통과
        as(agentId, "SERVICE_DESK_AGENT");
        var status = requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null));
        assertThat(status.status()).isEqualTo("IN_FULFILLMENT");

        String approvalStatus = jdbc.queryForObject(
                "select status from approval_request where id = ?", String.class, approvalRequestId);
        assertThat(approvalStatus).isEqualTo("APPROVED");
    }

    @Test
    void reDecideAlreadyDecidedRoleSlotIsBlockedAndStateNotCorrupted() {
        long ts = System.nanoTime();
        String domain = "SERVICE_REQUEST";

        Long requesterId = insertUser("req" + ts + "@itsm.local");
        Long approverId = insertUser("apr" + ts + "@itsm.local");
        Long agentId = insertUser("agt" + ts + "@itsm.local");

        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse item = catalogService.create(new CreateCatalogItemRequest(
                "Item" + ts, "d", null, null, null, null, null,
                formSchemaWith("note", false)));
        seedSubtypeProcess(domain, String.valueOf(item.id()), "APPROVER");

        as(requesterId, "END_USER");
        RequestCreatedResponse created = requestService.create(new CreateRequestRequest(item.id(), Map.of()));
        Long rid = created.id();

        as(agentId, "SERVICE_DESK_AGENT");
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.VALIDATED, null));
        requestService.assign(rid, new AssignRequest(agentId));
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.ROUTED, null));
        assertThatThrownBy(() ->
                requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null)));

        Long approvalRequestId = jdbc.queryForObject(
                "select id from approval_request where ticket_type='SERVICE_REQUEST' and ticket_id = ?", Long.class, rid);

        as(approverId, "APPROVER");
        approvalInstanceService.decide(approvalRequestId, DecisionType.APPROVE, "ok");

        assertThatThrownBy(() -> approvalInstanceService.decide(approvalRequestId, DecisionType.REJECT, "flip"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.APPROVAL_ALREADY_DECIDED));

        String approvalStatus = jdbc.queryForObject(
                "select status from approval_request where id = ?", String.class, approvalRequestId);
        assertThat(approvalStatus).isEqualTo("APPROVED"); // 뒤집히지 않음
    }

    @Test
    void gateMatchesDomainNullCatchAllRuleForDomainSpecificTicket() {
        // 2026-07-15 우선순위 재설계: domain=NULL(전체 도메인) 규칙도 SERVICE_REQUEST 티켓의 게이트
        // 매칭 후보에 포함돼야 한다(ApprovalGateService.matchProcess가 조회하는
        // ApprovalProcessRepository.findByDomain이 domain=null 규칙도 함께 반환하는지 검증).
        long ts = System.nanoTime();
        String domain = "SERVICE_REQUEST";

        Long requesterId = insertUser("catchall-req" + ts + "@itsm.local");
        Long approverId = insertUser("catchall-apr" + ts + "@itsm.local");
        Long agentId = insertUser("catchall-agt" + ts + "@itsm.local");

        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse item = catalogService.create(new CreateCatalogItemRequest(
                "CatchAllItem" + ts, "d", null, null, null, null, null,
                formSchemaWith("note", false)));
        seedCatchAllProcess("APPROVER", "전체 도메인 캐치올 " + ts);

        as(requesterId, "END_USER");
        RequestCreatedResponse created = requestService.create(new CreateRequestRequest(item.id(), Map.of()));
        Long rid = created.id();

        as(agentId, "SERVICE_DESK_AGENT");
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.VALIDATED, null));
        requestService.assign(rid, new AssignRequest(agentId));
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.ROUTED, null));

        BusinessException blocked = (BusinessException) org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null)));
        assertThat(blocked.getErrorCode()).isEqualTo(ErrorCode.APPROVAL_PENDING);
        Long approvalRequestId = blocked.getApprovalRequestId();
        assertThat(approvalRequestId).isNotNull();

        String matchedProcessDomain = jdbc.queryForObject("""
                select ap.domain from approval_request ar join approval_process ap on ap.id = ar.approval_process_id
                where ar.id = ?
                """, String.class, approvalRequestId);
        assertThat(matchedProcessDomain).isNull();

        as(approverId, "APPROVER");
        var decision = approvalInstanceService.decide(approvalRequestId, DecisionType.APPROVE, "ok");
        assertThat(decision.requestStatus()).isEqualTo("APPROVED");

        as(agentId, "SERVICE_DESK_AGENT");
        var status = requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null));
        assertThat(status.status()).isEqualTo("IN_FULFILLMENT");
    }

    @Test
    void detailLinksExposeAssetKeyViaAssetSideLink() {
        // REQ-ITAM-006(TC-REG-002): API-ITAM-007(자산→요청 연계)로 생성된 양방향 ticket_link가
        // 요청 상세(linkedAssets)에도 노출되는지 검증.
        long ts = System.nanoTime();
        Long requesterId = insertUser("req" + ts + "@itsm.local");

        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse item = catalogService.create(new CreateCatalogItemRequest(
                "AssetLinkItem" + ts, "d", null, null, null, null, null, emptyFormSchema()));

        as(requesterId, "END_USER");
        RequestCreatedResponse created = requestService.create(new CreateRequestRequest(item.id(), Map.of()));
        Long rid = created.id();

        as(insertUser("am" + ts + "@itsm.local"), "ASSET_MANAGER");
        var asset = assetService.create(new CreateAssetRequest("요청연계확인자산" + ts, AssetType.HARDWARE,
                null, null, null, null, null, null, null, null));
        assetService.linkAsset(asset.id(), new LinkAssetRequest(TicketType.SERVICE_REQUEST, rid));

        as(requesterId, "END_USER");
        assertThat(requestService.detail(rid).linkedAssets()).extracting("assetKey").containsExactly(asset.assetKey());
    }

    @Test
    void updateCatalogItemReplacesFormSchema() {
        // formSchema(JSONB)는 카탈로그 항목 한 행에 통째로 저장되므로(2026-07-17 유지보수 요청,
        // 기존 catalog_form_field EAV 폐기), 동일 key로 재저장해도 유니크 제약 위반 여지가 없다.
        long ts = System.nanoTime();

        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse created = catalogService.create(new CreateCatalogItemRequest(
                "Laptop" + ts, "d", null, null, null, null, null,
                formSchemaWith("model", true)));

        CatalogItemDetailResponse updated = catalogService.update(created.id(), new UpdateCatalogItemRequest(
                "Laptop" + ts, "updated", null, null, null, null, null,
                formSchemaWith("model", true)));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> components = (List<Map<String, Object>>) updated.formSchema().get("components");
        assertThat(components).extracting(c -> c.get("key")).containsExactly("model");
    }
}
