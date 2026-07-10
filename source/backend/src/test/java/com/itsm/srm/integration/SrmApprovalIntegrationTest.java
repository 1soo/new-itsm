package com.itsm.srm.integration;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.domain.AssetType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.srm.application.ServiceCatalogService;
import com.itsm.srm.application.ServiceRequestService;
import com.itsm.srm.application.dto.ApprovalDecision;
import com.itsm.srm.application.dto.ApprovalDecisionRequest;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.FormFieldDto;
import com.itsm.srm.application.dto.RequestCreatedResponse;
import com.itsm.srm.application.dto.StatusTransitionRequest;
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
import org.testcontainers.containers.PostgreSQLContainer;
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
 * 실 PostgreSQL(Testcontainers)로 승인 흐름을 검증한다. 특히 이미 결정된 승인 재처리 차단(409)과
 * 상태 오염 방지를 실 트랜잭션으로 재현·방지한다(단위 mock 사각지대 보완). 실제 DDL(01/03/04)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class SrmApprovalIntegrationTest {

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

    @Autowired ServiceCatalogService catalogService;
    @Autowired ServiceRequestService requestService;
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

    @Test
    void reDecideAlreadyApprovedIsBlockedAndStateNotCorrupted() {
        long ts = System.nanoTime();
        Long requesterId = insertUser("req" + ts + "@itsm.local");
        Long approverId = insertUser("apr" + ts + "@itsm.local");

        // 카탈로그(승인 필요, approver_role=APPROVER)
        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse item = catalogService.create(new CreateCatalogItemRequest(
                "Item" + ts, "d", true, "APPROVER", null, null, null,
                List.of(new FormFieldDto("note", "Note", "text", false, null))));

        // 요청 제출(요청자)
        as(requesterId, "END_USER");
        RequestCreatedResponse created = requestService.create(new CreateRequestRequest(item.id(), Map.of()));
        Long rid = created.id();

        // 상담원: VALIDATED → ROUTED(승인 필요 → APPROVAL_PENDING, approval 생성)
        as(2L, "SERVICE_DESK_AGENT");
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.VALIDATED, null));
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.ROUTED, null));

        // 승인자: 최초 승인 → APPROVED, 요청 ROUTED
        as(approverId, "APPROVER");
        requestService.decideApproval(rid, new ApprovalDecisionRequest(ApprovalDecision.APPROVE, "ok"));

        // 상담원: 이행 진행
        as(2L, "SERVICE_DESK_AGENT");
        requestService.transition(rid, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null));

        // 승인자: 이미 결정된 승인 재처리 → 409, 상태 오염 없음
        as(approverId, "APPROVER");
        assertThatThrownBy(() -> requestService.decideApproval(rid, new ApprovalDecisionRequest(ApprovalDecision.REJECT, "flip")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.APPROVAL_ALREADY_DECIDED));

        String status = jdbc.queryForObject("select status from service_request where id = ?", String.class, rid);
        String approvalStatus = jdbc.queryForObject(
                "select status from approval where ticket_type='SERVICE_REQUEST' and ticket_id = ?", String.class, rid);
        assertThat(status).isEqualTo("IN_FULFILLMENT"); // 되돌려지지 않음
        assertThat(approvalStatus).isEqualTo("APPROVED"); // 뒤집히지 않음
    }

    @Test
    void detailLinksExposeAssetKeyViaAssetSideLink() {
        // REQ-ITAM-006(TC-REG-002): API-ITAM-007(자산→요청 연계)로 생성된 양방향 ticket_link가
        // 요청 상세(linkedAssets)에도 노출되는지 검증.
        long ts = System.nanoTime();
        Long requesterId = insertUser("req" + ts + "@itsm.local");

        as(1L, "PROCESS_OWNER");
        CatalogItemDetailResponse item = catalogService.create(new CreateCatalogItemRequest(
                "AssetLinkItem" + ts, "d", false, null, null, null, null, List.of()));

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
}
