package com.itsm.infra.integration;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.infra.application.InfraMonitoringService;
import com.itsm.infra.application.dto.CapacityPlanCreateRequest;
import com.itsm.infra.application.dto.MetricCreateRequest;
import com.itsm.infra.application.dto.ThresholdUpdateRequest;
import com.itsm.infra.application.dto.UptimeTargetRequest;
import com.itsm.infra.domain.MetricType;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실 PostgreSQL(Testcontainers)로 인프라 모니터링 & 용량관리 전체 흐름을 검증한다: 지표 등록 시 전역 임계치
 * 초과/미초과·미설정 알림 생성 여부→알림 확인처리(idempotent, 존재하지 않는 알림 404)→가동률 목표 대비 현황
 * (목표 있음/없음, 조회 시점 평균 계산)→용량 계획 활용률 계산→리포팅 집계(자산 필터)→RBAC(비-INFRA_OPERATOR 403).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class InfraMonitoringIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
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
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/22_infra_monitoring_schema.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/22_infra_monitoring_schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../db/sql/23_infra_monitoring_seed.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/23_infra_monitoring_seed.sql")
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

    @Autowired InfraMonitoringService infraMonitoringService;
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

    private Long insertAsset(String assetKey) {
        jdbc.update("insert into asset(asset_key, name, type, status, created_by) values (?,?,?,?,?)",
                assetKey, "웹서버", "HARDWARE", "OPERATION", "test");
        return jdbc.queryForObject("select id from asset where asset_key = ?", Long.class, assetKey);
    }

    @Test
    void metricRegistrationGeneratesAlertOnlyWhenThresholdBreached() {
        long ts = System.nanoTime();
        Long ioId = insertUser("io" + ts + "@itsm.local");
        Long assetId = insertAsset("AST-" + (ts % 100000));
        as(ioId, "INFRA_OPERATOR");

        // 시드에서 CPU 상한 90.00으로 사전 설정됨
        var overThreshold = infraMonitoringService.registerMetric(
                new MetricCreateRequest(assetId, MetricType.CPU, new BigDecimal("95.00"), null));
        assertThat(overThreshold.alertGenerated()).isTrue();

        var underThreshold = infraMonitoringService.registerMetric(
                new MetricCreateRequest(assetId, MetricType.CPU, new BigDecimal("50.00"), null));
        assertThat(underThreshold.alertGenerated()).isFalse();

        // MEMORY는 임계치 미설정 -> 값과 무관하게 알림 미생성
        var noThreshold = infraMonitoringService.registerMetric(
                new MetricCreateRequest(assetId, MetricType.MEMORY, new BigDecimal("999.00"), null));
        assertThat(noThreshold.alertGenerated()).isFalse();

        var alerts = infraMonitoringService.listAlerts(assetId, null);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).metricType()).isEqualTo("CPU");
        assertThat(alerts.get(0).value()).isEqualByComparingTo("95.00");
        assertThat(alerts.get(0).thresholdType()).isEqualTo("UPPER");
        assertThat(alerts.get(0).acknowledged()).isFalse();

        // 확인 처리(idempotent) + 존재하지 않는 알림 404
        infraMonitoringService.acknowledgeAlert(alerts.get(0).id());
        infraMonitoringService.acknowledgeAlert(alerts.get(0).id());
        var acknowledged = infraMonitoringService.listAlerts(assetId, true);
        assertThat(acknowledged).extracting(a -> a.id()).contains(alerts.get(0).id());

        assertThatThrownBy(() -> infraMonitoringService.acknowledgeAlert(999999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INFRA_METRIC_ALERT_NOT_FOUND));

        // 임계치 신규 설정(API-IOM-004) 후에는 해당 항목도 초과 시 알림 생성
        infraMonitoringService.setThreshold(MetricType.MEMORY, new ThresholdUpdateRequest(new BigDecimal("500.00"), null));
        var thresholds = infraMonitoringService.listThresholds();
        assertThat(thresholds).anySatisfy(t -> {
            assertThat(t.metricType()).isEqualTo("MEMORY");
            assertThat(t.upperLimit()).isEqualByComparingTo("500.00");
        });
        var memoryAlert = infraMonitoringService.registerMetric(
                new MetricCreateRequest(assetId, MetricType.MEMORY, new BigDecimal("600.00"), null));
        assertThat(memoryAlert.alertGenerated()).isTrue();
    }

    @Test
    void registerMetricOnNonExistentAssetThrows404() {
        long ts = System.nanoTime();
        Long ioId = insertUser("io2" + ts + "@itsm.local");
        as(ioId, "INFRA_OPERATOR");

        assertThatThrownBy(() -> infraMonitoringService.registerMetric(
                new MetricCreateRequest(999999L, MetricType.CPU, new BigDecimal("10.00"), null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ASSET_NOT_FOUND));
    }

    @Test
    void uptimeStatusComputedAtQueryTimeWithAndWithoutTarget() {
        long ts = System.nanoTime();
        Long ioId = insertUser("io3" + ts + "@itsm.local");
        String assetKey = "AST-" + (ts % 100000);
        Long assetId = insertAsset(assetKey);
        as(ioId, "INFRA_OPERATOR");

        OffsetDateTime t1 = OffsetDateTime.parse("2026-07-01T09:00:00+09:00");
        OffsetDateTime t2 = OffsetDateTime.parse("2026-07-05T09:00:00+09:00");
        infraMonitoringService.registerMetric(new MetricCreateRequest(assetId, MetricType.UPTIME, new BigDecimal("99.95"), t1));
        infraMonitoringService.registerMetric(new MetricCreateRequest(assetId, MetricType.UPTIME, new BigDecimal("98.10"), t2));

        // 목표 미설정 상태 -> met은 null
        var beforeTarget = infraMonitoringService.getUptimeStatus(assetId, null, null);
        assertThat(beforeTarget.assetKey()).isEqualTo(assetKey);
        assertThat(beforeTarget.targetPercentage()).isNull();
        assertThat(beforeTarget.actualPercentage()).isEqualByComparingTo("99.03");
        assertThat(beforeTarget.met()).isNull();

        infraMonitoringService.setUptimeTarget(assetId, new UptimeTargetRequest(new BigDecimal("99.00")));
        var afterTarget = infraMonitoringService.getUptimeStatus(assetId, null, null);
        assertThat(afterTarget.targetPercentage()).isEqualByComparingTo("99.00");
        assertThat(afterTarget.met()).isTrue();

        infraMonitoringService.setUptimeTarget(assetId, new UptimeTargetRequest(new BigDecimal("99.90")));
        var notMet = infraMonitoringService.getUptimeStatus(assetId, null, null);
        assertThat(notMet.met()).isFalse();

        assertThatThrownBy(() -> infraMonitoringService.setUptimeTarget(999999L, new UptimeTargetRequest(BigDecimal.TEN)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ASSET_NOT_FOUND));
        assertThatThrownBy(() -> infraMonitoringService.getUptimeStatus(999999L, null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ASSET_NOT_FOUND));
    }

    @Test
    void reportAggregatesPerAssetAndCapacityUtilization() {
        long ts = System.nanoTime();
        Long ioId = insertUser("io4" + ts + "@itsm.local");
        Long assetId = insertAsset("AST-" + (ts % 100000));
        as(ioId, "INFRA_OPERATOR");

        infraMonitoringService.registerMetric(new MetricCreateRequest(assetId, MetricType.CPU, new BigDecimal("40.00"), null));
        infraMonitoringService.registerMetric(new MetricCreateRequest(assetId, MetricType.CPU, new BigDecimal("60.00"), null));
        infraMonitoringService.registerMetric(new MetricCreateRequest(assetId, MetricType.RESPONSE_TIME, new BigDecimal("100.00"), null));

        var created = infraMonitoringService.createCapacityPlan(
                new CapacityPlanCreateRequest("팀A 서버 용량 " + ts, new BigDecimal("200"), new BigDecimal("240")));
        var plans = infraMonitoringService.listCapacityPlans();
        var mine = plans.stream().filter(p -> p.id().equals(created.id())).findFirst().orElseThrow();
        assertThat(mine.utilizationRate()).isEqualByComparingTo("1.20");

        var report = infraMonitoringService.report(null, null, assetId);
        assertThat(report.avgCpu()).isEqualByComparingTo("50.00");
        assertThat(report.avgResponseTime()).isEqualByComparingTo("100.00");
        assertThat(report.avgUptime()).isEqualByComparingTo("0.00");
        assertThat(report.avgMemory()).isEqualByComparingTo("0.00");

        List<java.util.Map<String, Object>> rows = jdbc.queryForList(
                "select capacity, demand from capacity_plan where is_deleted = false");
        BigDecimal sum = BigDecimal.ZERO;
        for (var row : rows) {
            BigDecimal capacity = (BigDecimal) row.get("capacity");
            BigDecimal demand = (BigDecimal) row.get("demand");
            sum = sum.add(demand.divide(capacity, 4, RoundingMode.HALF_UP));
        }
        BigDecimal expectedAvgUtilization = sum.divide(BigDecimal.valueOf(rows.size()), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        assertThat(report.avgCapacityUtilization()).isEqualByComparingTo(expectedAvgUtilization);
    }

    @Test
    void nonInfraOperatorRoleForbidden() {
        long ts = System.nanoTime();
        Long userId = insertUser("nonio" + ts + "@itsm.local");
        as(userId, "END_USER");

        assertThatThrownBy(() -> infraMonitoringService.listMetrics(null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
        assertThatThrownBy(() -> infraMonitoringService.listCapacityPlans())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
