package com.itsm.asset.integration;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.CreateCiRequest;
import com.itsm.asset.application.dto.CiRelationRequest;
import com.itsm.asset.application.dto.LifecycleTransitionRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.application.dto.UpdateAssetRequest;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.AssetType;
import com.itsm.asset.domain.RelationType;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
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

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 자산(asset, ITAM/CMDB) 전체 흐름을 검증한다.
 * 자산 등록(EAV 속성)→목록(만료임박 필터)→상세→수정(만료일 과거 경고)→생애주기 임의 전이→폐기→
 * 티켓 연계(4종+미존재 400)→CI 등록(미존재 자산 400)→CI 관계(자기참조·미존재 400)→영향 범위(BFS)→지표를
 * 실 트랜잭션·실 FK로 재현한다. 실제 DDL(01/03/04/06/08/10/12/14)을 마운트한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class AssetIntegrationTest {

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
                    "/docker-entrypoint-initdb.d/14_asset_schema.sql");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==");
    }

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

    private Long insertChangeRequest(String key) {
        jdbc.update("insert into change_request(ticket_key, summary, type, status, created_by) values (?,?,?,?,?)",
                key, "변경", "NORMAL", "REQUESTED", "test");
        return jdbc.queryForObject("select id from change_request where ticket_key = ?", Long.class, key);
    }

    private Long insertServiceRequest(String key) {
        jdbc.update("insert into service_catalog_item(name, created_by) values (?,?)", "카탈로그", "test");
        Long catalogItemId = jdbc.queryForObject("select id from service_catalog_item where name = ? order by id desc limit 1",
                Long.class, "카탈로그");
        Long requesterId = insertUser("req" + System.nanoTime() + "@itsm.local");
        jdbc.update("insert into service_request(ticket_key, catalog_item_id, requester_id, created_by) values (?,?,?,?)",
                key, catalogItemId, requesterId, "test");
        return jdbc.queryForObject("select id from service_request where ticket_key = ?", Long.class, key);
    }

    private Long createAsset(Long managerId, String name) {
        as(managerId, "ASSET_MANAGER");
        var created = assetService.create(new CreateAssetRequest(name, AssetType.HARDWARE, "홍길동", "서울",
                LocalDate.now().minusYears(1), BigDecimal.valueOf(1000000),
                LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1),
                Map.of("cpu", "8core")));
        return created.id();
    }

    @Test
    void createAndRetrieveAssetWithAttributesAndExpiry() {
        long ts = System.nanoTime();
        Long managerId = insertUser("am" + ts + "@itsm.local");
        as(managerId, "ASSET_MANAGER");

        var created = assetService.create(new CreateAssetRequest("서버-A", AssetType.HARDWARE, "홍길동", "서울 IDC",
                LocalDate.now().minusYears(1), BigDecimal.valueOf(2000000),
                LocalDate.now().plusDays(10), LocalDate.now().plusYears(2), LocalDate.now().plusYears(1),
                Map.of("cpu", "16core", "ram", "64GB")));
        assertThat(created.assetKey()).startsWith("AST-");
        assertThat(created.status()).isEqualTo("PLANNING");

        var detail = assetService.detail(created.id());
        assertThat(detail.name()).isEqualTo("서버-A");
        assertThat(detail.attributes()).containsEntry("cpu", "16core").containsEntry("ram", "64GB");
        assertThat(detail.expiry().license().date()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(detail.expiry().license().status()).isEqualTo("EXPIRING");
        assertThat(detail.expiry().warranty().status()).isEqualTo("OK");
        assertThat(detail.linkedTickets()).isEmpty();
        assertThat(detail.linkedCis()).isEmpty();
    }

    @Test
    void nonManagerCannotCreateAsset() {
        as(insertUser("agent" + System.nanoTime() + "@itsm.local"), "SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> assetService.create(new CreateAssetRequest("서버-B", AssetType.SOFTWARE,
                null, null, null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void detailNotFoundReturns404Code() {
        as(insertUser("u" + System.nanoTime() + "@itsm.local"), "SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> assetService.detail(999999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ASSET_NOT_FOUND));
    }

    @Test
    void listFiltersByExpiringWithinDaysAndKeyword() {
        long ts = System.nanoTime();
        Long managerId = insertUser("am" + ts + "@itsm.local");
        as(managerId, "ASSET_MANAGER");
        String uniqueName = "만료임박노트북" + ts;
        assetService.create(new CreateAssetRequest(uniqueName, AssetType.HARDWARE, null, null, null, null,
                LocalDate.now().plusDays(5), null, null, null));
        assetService.create(new CreateAssetRequest("정상장비" + ts, AssetType.HARDWARE, null, null, null, null,
                LocalDate.now().plusYears(3), null, null, null));

        var page = assetService.list(null, null, null, uniqueName, null, PageRequest.of(0, 20));
        assertThat(page.content()).extracting("name").containsExactly(uniqueName);
        assertThat(page.content().get(0).expiryStatus()).isEqualTo("EXPIRING");

        var expiringPage = assetService.list(null, null, null, null, 10, PageRequest.of(0, 50));
        assertThat(expiringPage.content()).extracting("name").contains(uniqueName);
    }

    @Test
    void updateWithPastExpiryReturnsWarning() {
        Long managerId = insertUser("am" + System.nanoTime() + "@itsm.local");
        Long assetId = createAsset(managerId, "업데이트대상" + System.nanoTime());

        as(managerId, "ASSET_MANAGER");
        var updated = assetService.update(assetId, new UpdateAssetRequest(null, null, null, null, null,
                LocalDate.now().minusDays(1), null, null, null));
        assertThat(updated.warning()).isNotNull();

        var okUpdate = assetService.update(assetId, new UpdateAssetRequest("새이름", null, null, null, null,
                null, null, null, Map.of("os", "linux")));
        assertThat(okUpdate.warning()).isNull();
        var detail = assetService.detail(assetId);
        assertThat(detail.name()).isEqualTo("새이름");
        assertThat(detail.attributes()).containsEntry("os", "linux").doesNotContainKey("cpu");
    }

    @Test
    void lifecycleTransitionAllowsArbitraryOrderAndRecordsHistory() {
        Long managerId = insertUser("am" + System.nanoTime() + "@itsm.local");
        Long assetId = createAsset(managerId, "생애주기자산" + System.nanoTime());

        as(managerId, "ASSET_MANAGER");
        var toOperation = assetService.transition(assetId, new LifecycleTransitionRequest(AssetStatus.OPERATION));
        assertThat(toOperation.status()).isEqualTo("OPERATION");
        var toMaintenance = assetService.transition(assetId, new LifecycleTransitionRequest(AssetStatus.MAINTENANCE));
        assertThat(toMaintenance.status()).isEqualTo("MAINTENANCE");
        var backToPlanning = assetService.transition(assetId, new LifecycleTransitionRequest(AssetStatus.PLANNING));
        assertThat(backToPlanning.status()).isEqualTo("PLANNING");

        var detail = assetService.detail(assetId);
        assertThat(detail.lifecycleHistory()).extracting("stage")
                .containsExactly("OPERATION", "MAINTENANCE", "PLANNING");
    }

    @Test
    void retireSetsRetirementStatus() {
        Long managerId = insertUser("am" + System.nanoTime() + "@itsm.local");
        Long assetId = createAsset(managerId, "폐기대상" + System.nanoTime());

        as(managerId, "ASSET_MANAGER");
        var retired = assetService.retire(assetId);
        assertThat(retired.status()).isEqualTo("RETIREMENT");
    }

    @Test
    void linksAllFourTicketTypesAndRejectsMissingTicket() {
        long ts = System.nanoTime();
        Long managerId = insertUser("am" + ts + "@itsm.local");
        Long assetId = createAsset(managerId, "연계자산" + ts);
        as(managerId, "ASSET_MANAGER");

        Long srId = insertServiceRequest("SRM-" + ts);
        Long incId = insertIncident("INC-" + ts);
        Long prbId = insertProblem("PRB-" + ts);
        Long chgId = insertChangeRequest("CHG-" + ts);

        assetService.linkAsset(assetId, new LinkAssetRequest(TicketType.SERVICE_REQUEST, srId));
        assetService.linkAsset(assetId, new LinkAssetRequest(TicketType.INCIDENT, incId));
        assetService.linkAsset(assetId, new LinkAssetRequest(TicketType.PROBLEM, prbId));
        assetService.linkAsset(assetId, new LinkAssetRequest(TicketType.CHANGE, chgId));

        var detail = assetService.detail(assetId);
        assertThat(detail.linkedTickets()).hasSize(4);

        Long linkCount = jdbc.queryForObject(
                "select count(*) from ticket_link where (source_type='ASSET' and source_id=?) "
                        + "or (target_type='ASSET' and target_id=?)", Long.class, assetId, assetId);
        assertThat(linkCount).isEqualTo(8L); // REQ-ITAM-006: 4종 각각 양방향 저장(정방향+역방향)

        assertThatThrownBy(() -> assetService.linkAsset(assetId, new LinkAssetRequest(TicketType.INCIDENT, 999999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void createsCiWithOptionalAssetLinkAndRejectsInvalidAsset() {
        Long managerId = insertUser("am" + System.nanoTime() + "@itsm.local");
        Long assetId = createAsset(managerId, "CI연결자산" + System.nanoTime());
        as(managerId, "ASSET_MANAGER");

        var ci = assetService.createCi(new CreateCiRequest("웹서버-01", "SERVER", assetId));
        assertThat(ci.name()).isEqualTo("웹서버-01");

        assertThatThrownBy(() -> assetService.createCi(new CreateCiRequest("웹서버-02", "SERVER", 999999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));

        var detail = assetService.detail(assetId);
        assertThat(detail.linkedCis()).extracting("name").contains("웹서버-01");
    }

    @Test
    void ciRelationRejectsSelfReferenceAndMissingTarget() {
        as(insertUser("u" + System.nanoTime() + "@itsm.local"));
        var ci1 = assetService.createCi(new CreateCiRequest("CI-A" + System.nanoTime(), "SERVER", null));

        assertThatThrownBy(() -> assetService.createRelation(ci1.id(), new CiRelationRequest(ci1.id(), RelationType.DEPENDS_ON)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CI_SELF_RELATION_NOT_ALLOWED));

        assertThatThrownBy(() -> assetService.createRelation(ci1.id(), new CiRelationRequest(999999L, RelationType.DEPENDS_ON)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void ciImpactTraversesDependencyGraphWithDepth() {
        long ts = System.nanoTime();
        as(insertUser("u" + ts + "@itsm.local"));
        var ciA = assetService.createCi(new CreateCiRequest("A" + ts, "SERVER", null));
        var ciB = assetService.createCi(new CreateCiRequest("B" + ts, "SERVER", null));
        var ciC = assetService.createCi(new CreateCiRequest("C" + ts, "SERVER", null));

        assetService.createRelation(ciA.id(), new CiRelationRequest(ciB.id(), RelationType.DEPENDS_ON));
        assetService.createRelation(ciB.id(), new CiRelationRequest(ciC.id(), RelationType.RUNS_ON));

        var impact = assetService.impact(ciA.id());
        assertThat(impact).hasSize(2);
        assertThat(impact).filteredOn(i -> i.ciId().equals(ciB.id())).extracting("depth").containsExactly(1);
        assertThat(impact).filteredOn(i -> i.ciId().equals(ciC.id())).extracting("depth").containsExactly(2);

        var noRelation = assetService.createCi(new CreateCiRequest("D" + ts, "SERVER", null));
        assertThat(assetService.impact(noRelation.id())).isEmpty();
    }

    @Test
    void metricsComputeUtilizationAndTypeDistribution() {
        long ts = System.nanoTime();
        Long managerId = insertUser("am" + ts + "@itsm.local");
        Long assetId = createAsset(managerId, "지표자산" + ts);
        as(managerId, "ASSET_MANAGER");
        assetService.transition(assetId, new LifecycleTransitionRequest(AssetStatus.OPERATION));

        var metrics = assetService.metrics(OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));
        assertThat(metrics.typeDistribution().get("HARDWARE")).isGreaterThanOrEqualTo(1L);
        assertThat(metrics.utilizationRate()).isGreaterThan(0);
    }
}
