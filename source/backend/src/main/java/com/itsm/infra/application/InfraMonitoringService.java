package com.itsm.infra.application;

import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.infra.application.dto.AlertResponse;
import com.itsm.infra.application.dto.CapacityPlanCreateRequest;
import com.itsm.infra.application.dto.CapacityPlanCreatedResponse;
import com.itsm.infra.application.dto.CapacityPlanResponse;
import com.itsm.infra.application.dto.InfraReportResponse;
import com.itsm.infra.application.dto.MetricCreateRequest;
import com.itsm.infra.application.dto.MetricCreatedResponse;
import com.itsm.infra.application.dto.MetricResponse;
import com.itsm.infra.application.dto.ThresholdResponse;
import com.itsm.infra.application.dto.ThresholdUpdateRequest;
import com.itsm.infra.application.dto.UptimeStatusResponse;
import com.itsm.infra.application.dto.UptimeTargetRequest;
import com.itsm.infra.domain.CapacityPlan;
import com.itsm.infra.domain.InfraMetric;
import com.itsm.infra.domain.InfraMetricAlert;
import com.itsm.infra.domain.InfraMetricThreshold;
import com.itsm.infra.domain.MetricType;
import com.itsm.infra.domain.ThresholdType;
import com.itsm.infra.domain.UptimeTarget;
import com.itsm.infra.domain.repository.CapacityPlanRepository;
import com.itsm.infra.domain.repository.InfraMetricAlertRepository;
import com.itsm.infra.domain.repository.InfraMetricRepository;
import com.itsm.infra.domain.repository.InfraMetricThresholdRepository;
import com.itsm.infra.domain.repository.UptimeTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 인프라 지표 등록·조회, 임계치 설정·초과알림, 자산 가동률(SLA), 용량 계획, 리포팅 유스케이스.
 * RBAC(infra_operator.md): 전 API가 INFRA_OPERATOR 전용(그 외 역할 403).
 * 가동률(actualPercentage)·용량 활용률(utilizationRate)·리포팅 평균값은 저장하지 않는 조회 시점 계산값이다.
 */
@Service
public class InfraMonitoringService {

    private static final String IO = "INFRA_OPERATOR";
    private static final OffsetDateTime EPOCH = OffsetDateTime.parse("1970-01-01T00:00:00Z");
    private static final OffsetDateTime FAR_FUTURE = OffsetDateTime.parse("9999-12-31T23:59:59Z");

    private final InfraMetricRepository metricRepository;
    private final InfraMetricThresholdRepository thresholdRepository;
    private final InfraMetricAlertRepository alertRepository;
    private final UptimeTargetRepository uptimeTargetRepository;
    private final CapacityPlanRepository capacityPlanRepository;
    private final AssetRepository assetRepository;

    public InfraMonitoringService(InfraMetricRepository metricRepository,
                                   InfraMetricThresholdRepository thresholdRepository,
                                   InfraMetricAlertRepository alertRepository,
                                   UptimeTargetRepository uptimeTargetRepository,
                                   CapacityPlanRepository capacityPlanRepository,
                                   AssetRepository assetRepository) {
        this.metricRepository = metricRepository;
        this.thresholdRepository = thresholdRepository;
        this.alertRepository = alertRepository;
        this.uptimeTargetRepository = uptimeTargetRepository;
        this.capacityPlanRepository = capacityPlanRepository;
        this.assetRepository = assetRepository;
    }

    // ---------- metric register (API-IOM-001) ----------

    @Transactional
    public MetricCreatedResponse registerMetric(MetricCreateRequest request) {
        requireRole();
        requireAssetExists(request.assetId());
        OffsetDateTime measuredAt = request.measuredAt() != null ? request.measuredAt() : OffsetDateTime.now();
        InfraMetric metric = metricRepository.save(
                new InfraMetric(request.assetId(), request.metricType(), request.value(), measuredAt));

        boolean alertGenerated = false;
        InfraMetricThreshold threshold = thresholdRepository.findByMetricType(request.metricType()).orElse(null);
        if (threshold != null) {
            if (threshold.getUpperLimit() != null && request.value().compareTo(threshold.getUpperLimit()) > 0) {
                alertRepository.save(new InfraMetricAlert(metric.getId(), request.assetId(), request.metricType(),
                        request.value(), ThresholdType.UPPER));
                alertGenerated = true;
            } else if (threshold.getLowerLimit() != null && request.value().compareTo(threshold.getLowerLimit()) < 0) {
                alertRepository.save(new InfraMetricAlert(metric.getId(), request.assetId(), request.metricType(),
                        request.value(), ThresholdType.LOWER));
                alertGenerated = true;
            }
        }
        return new MetricCreatedResponse(metric.getId(), alertGenerated);
    }

    // ---------- metric timeseries (API-IOM-002) ----------

    @Transactional(readOnly = true)
    public List<MetricResponse> listMetrics(Long assetId, MetricType metricType, OffsetDateTime from,
                                             OffsetDateTime to) {
        requireRole();
        return metricRepository.search(assetId, metricType, orEpoch(from), orFarFuture(to)).stream()
                .map(m -> new MetricResponse(m.getId(), m.getMetricType().name(), m.getValue(), m.getMeasuredAt()))
                .toList();
    }

    // ---------- threshold list (API-IOM-003) ----------

    @Transactional(readOnly = true)
    public List<ThresholdResponse> listThresholds() {
        requireRole();
        return thresholdRepository.findAll().stream()
                .map(t -> new ThresholdResponse(t.getMetricType().name(), t.getUpperLimit(), t.getLowerLimit()))
                .toList();
    }

    // ---------- threshold set (API-IOM-004) ----------

    @Transactional
    public void setThreshold(MetricType metricType, ThresholdUpdateRequest request) {
        requireRole();
        InfraMetricThreshold threshold = thresholdRepository.findByMetricType(metricType).orElse(null);
        if (threshold == null) {
            thresholdRepository.save(new InfraMetricThreshold(metricType, request.upperLimit(), request.lowerLimit()));
        } else {
            threshold.update(request.upperLimit(), request.lowerLimit());
            thresholdRepository.save(threshold);
        }
    }

    // ---------- alert list (API-IOM-005) ----------

    @Transactional(readOnly = true)
    public List<AlertResponse> listAlerts(Long assetId, Boolean acknowledged) {
        requireRole();
        return alertRepository.search(assetId, acknowledged).stream()
                .map(a -> new AlertResponse(a.getId(), assetKeyOf(a.getAssetId()), a.getMetricType().name(),
                        a.getBreachedValue(), a.getThresholdType().name(), a.isAcknowledged(), a.getCreatedAt()))
                .toList();
    }

    // ---------- alert acknowledge (API-IOM-006) ----------

    @Transactional
    public void acknowledgeAlert(Long id) {
        requireRole();
        InfraMetricAlert alert = alertRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.INFRA_METRIC_ALERT_NOT_FOUND));
        if (!alert.isAcknowledged()) {
            alert.acknowledge();
            alertRepository.save(alert);
        }
    }

    // ---------- uptime target set (API-IOM-007) ----------

    @Transactional
    public void setUptimeTarget(Long assetId, UptimeTargetRequest request) {
        requireRole();
        requireAssetExists(assetId);
        UptimeTarget target = uptimeTargetRepository.findByAssetId(assetId).orElse(null);
        if (target == null) {
            uptimeTargetRepository.save(new UptimeTarget(assetId, request.targetPercentage()));
        } else {
            target.update(request.targetPercentage());
            uptimeTargetRepository.save(target);
        }
    }

    // ---------- uptime status (API-IOM-008) ----------

    @Transactional(readOnly = true)
    public UptimeStatusResponse getUptimeStatus(Long assetId, OffsetDateTime from, OffsetDateTime to) {
        requireRole();
        Asset asset = findAsset(assetId);
        BigDecimal targetPercentage = uptimeTargetRepository.findByAssetId(assetId)
                .map(UptimeTarget::getTargetPercentage).orElse(null);
        BigDecimal actualPercentage = average(assetId, MetricType.UPTIME, from, to);
        Boolean met = (targetPercentage == null || actualPercentage == null)
                ? null : actualPercentage.compareTo(targetPercentage) >= 0;
        return new UptimeStatusResponse(asset.getAssetKey(), targetPercentage, actualPercentage, met);
    }

    // ---------- capacity plan create (API-IOM-009) ----------

    @Transactional
    public CapacityPlanCreatedResponse createCapacityPlan(CapacityPlanCreateRequest request) {
        requireRole();
        CapacityPlan saved = capacityPlanRepository.save(
                new CapacityPlan(request.teamOrService(), request.capacity(), request.demand()));
        return new CapacityPlanCreatedResponse(saved.getId());
    }

    // ---------- capacity plan list (API-IOM-010) ----------

    @Transactional(readOnly = true)
    public List<CapacityPlanResponse> listCapacityPlans() {
        requireRole();
        return capacityPlanRepository.findAll().stream()
                .map(p -> new CapacityPlanResponse(p.getId(), p.getTeamOrService(), p.getCapacity(), p.getDemand(),
                        utilizationRate(p)))
                .toList();
    }

    // ---------- report (API-IOM-011) ----------

    @Transactional(readOnly = true)
    public InfraReportResponse report(OffsetDateTime from, OffsetDateTime to, Long assetId) {
        requireRole();
        BigDecimal avgUptime = orZero(average(assetId, MetricType.UPTIME, from, to));
        BigDecimal avgCpu = orZero(average(assetId, MetricType.CPU, from, to));
        BigDecimal avgMemory = orZero(average(assetId, MetricType.MEMORY, from, to));
        BigDecimal avgResponseTime = orZero(average(assetId, MetricType.RESPONSE_TIME, from, to));

        List<CapacityPlan> plans = capacityPlanRepository.findAll();
        BigDecimal avgCapacityUtilization = plans.isEmpty() ? BigDecimal.ZERO
                : round(plans.stream().map(this::utilizationRate)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(plans.size()), 4, RoundingMode.HALF_UP));

        return new InfraReportResponse(avgUptime, avgCpu, avgMemory, avgResponseTime, avgCapacityUtilization);
    }

    // ---------- helpers ----------

    private void requireRole() {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        if (!principal.roles().contains(IO)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireAssetExists(Long assetId) {
        findAsset(assetId);
    }

    private Asset findAsset(Long assetId) {
        return assetRepository.findById(assetId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSET_NOT_FOUND));
    }

    private String assetKeyOf(Long assetId) {
        return assetRepository.findById(assetId).map(Asset::getAssetKey).orElse(null);
    }

    private BigDecimal average(Long assetId, MetricType metricType, OffsetDateTime from, OffsetDateTime to) {
        Double avg = metricRepository.average(assetId, metricType, orEpoch(from), orFarFuture(to));
        return avg == null ? null : round(BigDecimal.valueOf(avg));
    }

    private OffsetDateTime orEpoch(OffsetDateTime from) {
        return from != null ? from : EPOCH;
    }

    private OffsetDateTime orFarFuture(OffsetDateTime to) {
        return to != null ? to : FAR_FUTURE;
    }

    private BigDecimal utilizationRate(CapacityPlan plan) {
        return round(plan.getDemand().divide(plan.getCapacity(), 4, RoundingMode.HALF_UP));
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
