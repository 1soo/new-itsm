package com.itsm.asset.application;

import com.itsm.asset.application.dto.AssetCreatedResponse;
import com.itsm.asset.application.dto.AssetDetailResponse;
import com.itsm.asset.application.dto.AssetMetricsResponse;
import com.itsm.asset.application.dto.AssetSummaryResponse;
import com.itsm.asset.application.dto.CiCreatedResponse;
import com.itsm.asset.application.dto.CiImpactResponse;
import com.itsm.asset.application.dto.CiListResponse;
import com.itsm.asset.application.dto.CiRelationRequest;
import com.itsm.asset.application.dto.CiRelationResponse;
import com.itsm.asset.application.dto.CiSummaryResponse;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.CreateCiRequest;
import com.itsm.asset.application.dto.LifecycleTransitionRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.application.dto.LinkAssetResponse;
import com.itsm.asset.application.dto.StatusResponse;
import com.itsm.asset.application.dto.UpdateAssetRequest;
import com.itsm.asset.application.dto.UpdateAssetResponse;
import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.AssetAttribute;
import com.itsm.asset.domain.AssetLifecycleHistory;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.AssetType;
import com.itsm.asset.domain.CiRelation;
import com.itsm.asset.domain.ConfigurationItem;
import com.itsm.asset.domain.ExpiryStatus;
import com.itsm.asset.domain.repository.AssetAttributeRepository;
import com.itsm.asset.domain.repository.AssetLifecycleHistoryRepository;
import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.asset.domain.repository.CiRelationRepository;
import com.itsm.asset.domain.repository.ConfigurationItemRepository;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.application.TicketCreationGateSupport;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.repository.ProblemRepository;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 자산(asset) 유스케이스: 자산 목록/등록/상세/수정·생애주기 전이·폐기·티켓 연계, CI 목록/등록/관계/영향범위, 지표.
 * RBAC(asset_manager.md): 등록·수정·폐기·생애주기 전이만 ASSET_MANAGER 전용, 조회·CI·연계·지표는 인증된 사용자 전반 허용.
 */
@Service
public class AssetService {

    private static final String AM = "ASSET_MANAGER";
    private static final int EXPIRING_SOON_DAYS = 30;
    private static final TicketType TT = TicketType.ASSET;
    private static final String DOMAIN = "ASSET";

    private final AssetRepository assetRepository;
    private final AssetAttributeRepository attributeRepository;
    private final AssetLifecycleHistoryRepository lifecycleHistoryRepository;
    private final ConfigurationItemRepository ciRepository;
    private final CiRelationRepository ciRelationRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final IncidentRepository incidentRepository;
    private final ProblemRepository problemRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ApprovalGateService approvalGateService;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final TicketCreationGateSupport ticketCreationGateSupport;

    public AssetService(AssetRepository assetRepository,
                        AssetAttributeRepository attributeRepository,
                        AssetLifecycleHistoryRepository lifecycleHistoryRepository,
                        ConfigurationItemRepository ciRepository,
                        CiRelationRepository ciRelationRepository,
                        TicketLinkRepository ticketLinkRepository,
                        ServiceRequestRepository serviceRequestRepository,
                        IncidentRepository incidentRepository,
                        ProblemRepository problemRepository,
                        ChangeRequestRepository changeRequestRepository,
                        ApprovalGateService approvalGateService,
                        ApprovalRequestRepository approvalRequestRepository,
                        TicketCreationGateSupport ticketCreationGateSupport) {
        this.assetRepository = assetRepository;
        this.attributeRepository = attributeRepository;
        this.lifecycleHistoryRepository = lifecycleHistoryRepository;
        this.ciRepository = ciRepository;
        this.ciRelationRepository = ciRelationRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.incidentRepository = incidentRepository;
        this.problemRepository = problemRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.approvalGateService = approvalGateService;
        this.approvalRequestRepository = approvalRequestRepository;
        this.ticketCreationGateSupport = ticketCreationGateSupport;
    }

    // ---------- list (API-ITAM-001) ----------

    @Transactional(readOnly = true)
    public PageResponse<AssetSummaryResponse> list(AssetType type, AssetStatus status, String owner, String keyword,
                                                   Integer expiringWithinDays, Pageable pageable) {
        SecurityUtils.currentPrincipal();
        boolean filterExpiring = expiringWithinDays != null;
        LocalDate threshold = filterExpiring ? LocalDate.now().plusDays(expiringWithinDays) : LocalDate.now();
        String ownerV = StringUtils.hasText(owner) ? owner : null;
        String kw = StringUtils.hasText(keyword) ? keyword : null;
        Page<Asset> page = assetRepository.search(type, status, ownerV, kw, filterExpiring, threshold, pageable);
        Map<Long, String> pendingTargetStates = approvalGateService.pendingApprovalTargetStatesOf(
                TT, page.getContent().stream().map(Asset::getId).toList());
        return PageResponse.from(page, a -> toSummary(a, pendingTargetStates.get(a.getId())));
    }

    // ---------- create (API-ITAM-002) ----------

    @Transactional
    public AssetCreatedResponse create(CreateAssetRequest request) {
        requireRole(AM);
        Long requesterId = SecurityUtils.currentPrincipal().userId();
        Asset saved = ticketCreationGateSupport.createThenGate(
                () -> {
                    Asset asset = assetRepository.save(new Asset(nextAssetKey(), request.name(), request.type(),
                            request.owner(), request.location(), request.purchaseDate(), request.cost(),
                            request.licenseExpiry(), request.warrantyExpiry(), request.contractExpiry()));
                    applyAttributes(asset.getId(), request.attributes());
                    return asset;
                },
                Asset::getId,
                DOMAIN, null, requesterId, TT, AssetStatus.PLANNING.name());
        return new AssetCreatedResponse(saved.getId(), saved.getAssetKey(), saved.getStatus().name());
    }

    // ---------- detail (API-ITAM-003) ----------

    @Transactional(readOnly = true)
    public AssetDetailResponse detail(Long id) {
        SecurityUtils.currentPrincipal();
        return toDetail(findAsset(id));
    }

    // ---------- update (API-ITAM-004) ----------

    @Transactional
    public UpdateAssetResponse update(Long id, UpdateAssetRequest request) {
        requireRole(AM);
        Asset asset = findAsset(id);
        asset.updateContent(request.name(), request.owner(), request.location(), request.purchaseDate(),
                request.cost(), request.licenseExpiry(), request.warrantyExpiry(), request.contractExpiry());
        assetRepository.save(asset);
        if (request.attributes() != null) {
            attributeRepository.deleteByAssetId(id);
            applyAttributes(id, request.attributes());
        }
        String warning = pastDueWarning(request.licenseExpiry(), request.warrantyExpiry(), request.contractExpiry());
        return new UpdateAssetResponse(id, warning);
    }

    // ---------- lifecycle transition (API-ITAM-005) ----------

    @Transactional
    public StatusResponse transition(Long id, LifecycleTransitionRequest request) {
        requireRole(AM);
        Asset asset = findAsset(id);
        approvalGateService.checkGate(DOMAIN, null, SecurityUtils.currentPrincipal().userId(), TT, id,
                request.targetStage().name());
        asset.changeStatus(request.targetStage());
        assetRepository.save(asset);
        lifecycleHistoryRepository.save(new AssetLifecycleHistory(id, request.targetStage()));
        return new StatusResponse(id, asset.getStatus().name());
    }

    // ---------- retire (API-ITAM-006) ----------

    @Transactional
    public StatusResponse retire(Long id) {
        requireRole(AM);
        Asset asset = findAsset(id);
        approvalGateService.checkGate(DOMAIN, null, SecurityUtils.currentPrincipal().userId(), TT, id,
                AssetStatus.RETIREMENT.name());
        asset.changeStatus(AssetStatus.RETIREMENT);
        assetRepository.save(asset);
        lifecycleHistoryRepository.save(new AssetLifecycleHistory(id, AssetStatus.RETIREMENT));
        return new StatusResponse(id, asset.getStatus().name());
    }

    // ---------- ticket link (API-ITAM-007) ----------

    @Transactional
    public LinkAssetResponse linkAsset(Long id, LinkAssetRequest request) {
        SecurityUtils.currentPrincipal();
        Asset asset = findAsset(id);
        if (!ticketExists(request.ticketType(), request.ticketId())) {
            throw new BusinessException(ErrorCode.LINK_TARGET_NOT_FOUND);
        }
        saveLinkOnce(TicketType.ASSET, asset.getId(), request.ticketType(), request.ticketId());
        saveLinkOnce(request.ticketType(), request.ticketId(), TicketType.ASSET, asset.getId());
        return new LinkAssetResponse(asset.getId(), request.ticketId());
    }

    // ---------- CI list (API-ITAM-008) ----------

    @Transactional(readOnly = true)
    public CiListResponse listCis(String keyword, String type, Pageable pageable) {
        SecurityUtils.currentPrincipal();
        Page<ConfigurationItem> page = ciRepository.search(
                StringUtils.hasText(keyword) ? keyword : null, StringUtils.hasText(type) ? type : null, pageable);
        List<CiSummaryResponse> content = page.getContent().stream()
                .map(c -> new CiSummaryResponse(c.getId(), c.getName(), c.getType()))
                .toList();
        return new CiListResponse(content, page.getTotalElements());
    }

    // ---------- CI create (API-ITAM-009) ----------

    @Transactional
    public CiCreatedResponse createCi(CreateCiRequest request) {
        SecurityUtils.currentPrincipal();
        if (request.assetId() != null) {
            boolean exists = assetRepository.findById(request.assetId()).filter(a -> !a.isDeleted()).isPresent();
            if (!exists) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 연결 자산입니다.");
            }
        }
        ConfigurationItem saved = ciRepository.save(new ConfigurationItem(request.name(), request.type(), request.assetId()));
        return new CiCreatedResponse(saved.getId(), saved.getName());
    }

    // ---------- CI relation (API-ITAM-010) ----------

    @Transactional
    public CiRelationResponse createRelation(Long id, CiRelationRequest request) {
        SecurityUtils.currentPrincipal();
        ConfigurationItem source = findCi(id);
        if (source.getId().equals(request.targetCiId())) {
            throw new BusinessException(ErrorCode.CI_SELF_RELATION_NOT_ALLOWED);
        }
        boolean targetExists = ciRepository.findById(request.targetCiId()).filter(c -> !c.isDeleted()).isPresent();
        if (!targetExists) {
            throw new BusinessException(ErrorCode.LINK_TARGET_NOT_FOUND);
        }
        CiRelation saved = ciRelationRepository.save(
                new CiRelation(source.getId(), request.targetCiId(), request.relationType()));
        return new CiRelationResponse(saved.getId(), saved.getSourceCiId(), saved.getTargetCiId(),
                saved.getRelationType().name());
    }

    // ---------- CI impact (API-ITAM-011) ----------

    @Transactional(readOnly = true)
    public List<CiImpactResponse> impact(Long id) {
        SecurityUtils.currentPrincipal();
        findCi(id);
        List<CiImpactResponse> result = new java.util.ArrayList<>();
        Set<Long> visited = new HashSet<>();
        visited.add(id);
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(id);
        int depth = 0;
        while (!queue.isEmpty()) {
            depth++;
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Long current = queue.poll();
                for (CiRelation rel : ciRelationRepository.findBySourceCiId(current)) {
                    if (visited.add(rel.getTargetCiId())) {
                        String name = ciRepository.findById(rel.getTargetCiId())
                                .map(ConfigurationItem::getName).orElse(null);
                        result.add(new CiImpactResponse(rel.getTargetCiId(), name, rel.getRelationType().name(), depth));
                        queue.add(rel.getTargetCiId());
                    }
                }
            }
        }
        return result;
    }

    // ---------- metrics (API-ITAM-012) ----------

    @Transactional(readOnly = true)
    public AssetMetricsResponse metrics(OffsetDateTime from, OffsetDateTime to) {
        SecurityUtils.currentPrincipal();
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        List<Asset> assets = assetRepository.findByCreatedAtBetween(fromV, toV);

        Map<String, Long> typeDistribution = new LinkedHashMap<>();
        for (AssetType type : AssetType.values()) {
            typeDistribution.put(type.name(), assets.stream().filter(a -> a.getType() == type).count());
        }
        if (assets.isEmpty()) {
            return new AssetMetricsResponse(0, 0, typeDistribution);
        }
        long operationCount = assets.stream().filter(a -> a.getStatus() == AssetStatus.OPERATION).count();
        double utilizationRate = (double) operationCount / assets.size() * 100.0;

        LocalDate expiringThreshold = LocalDate.now().plusDays(EXPIRING_SOON_DAYS);
        long expiringCount = assets.stream()
                .map(Asset::earliestExpiry)
                .filter(java.util.Objects::nonNull)
                .filter(d -> !d.isAfter(expiringThreshold))
                .count();

        return new AssetMetricsResponse(round(utilizationRate), expiringCount, typeDistribution);
    }

    // ---------- helpers ----------

    private boolean ticketExists(TicketType type, Long ticketId) {
        return switch (type) {
            case SERVICE_REQUEST -> serviceRequestRepository.findById(ticketId).filter(r -> !r.isDeleted()).isPresent();
            case INCIDENT -> incidentRepository.findById(ticketId).filter(i -> !i.isDeleted()).isPresent();
            case PROBLEM -> problemRepository.findById(ticketId).filter(p -> !p.isDeleted()).isPresent();
            case CHANGE -> changeRequestRepository.findById(ticketId).filter(c -> !c.isDeleted()).isPresent();
            default -> false;
        };
    }

    private String ticketKeyOf(TicketType type, Long ticketId) {
        return switch (type) {
            case SERVICE_REQUEST -> serviceRequestRepository.findById(ticketId).map(ServiceRequest::getTicketKey).orElse(null);
            case INCIDENT -> incidentRepository.findById(ticketId).map(Incident::getTicketKey).orElse(null);
            case PROBLEM -> problemRepository.findById(ticketId).map(Problem::getTicketKey).orElse(null);
            case CHANGE -> changeRequestRepository.findById(ticketId).map(ChangeRequest::getTicketKey).orElse(null);
            default -> null;
        };
    }

    /** 자산 assetKey 조회(없으면 null). 인시던트/문제/변경/서비스요청 상세의 연결 자산 노출(REQ-ITAM-006)에 사용. */
    @Transactional(readOnly = true)
    public String assetKeyOf(Long assetId) {
        return assetRepository.findById(assetId).map(Asset::getAssetKey).orElse(null);
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSET_NOT_FOUND));
    }

    private ConfigurationItem findCi(Long id) {
        return ciRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.CI_NOT_FOUND));
    }

    private void requireRole(String... roles) {
        if (!SecurityUtils.hasAnyRole(roles)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void applyAttributes(Long assetId, Map<String, String> attributes) {
        if (attributes == null) {
            return;
        }
        attributes.forEach((key, value) -> {
            if (StringUtils.hasText(key)) {
                attributeRepository.save(new AssetAttribute(assetId, key, value));
            }
        });
    }

    private void saveLinkOnce(TicketType sourceType, Long sourceId, TicketType targetType, Long targetId) {
        if (!ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                sourceType, sourceId, targetType, targetId)) {
            ticketLinkRepository.save(new TicketLink(sourceType, sourceId, targetType, targetId, "RELATED"));
        }
    }

    private String pastDueWarning(LocalDate... dates) {
        LocalDate today = LocalDate.now();
        boolean anyPast = java.util.Arrays.stream(dates)
                .filter(java.util.Objects::nonNull)
                .anyMatch(d -> d.isBefore(today));
        return anyPast ? "만료일이 과거 날짜로 설정되었습니다." : null;
    }

    private ExpiryStatus expiryStatusOf(Asset asset) {
        LocalDate earliest = asset.earliestExpiry();
        return earliest == null ? ExpiryStatus.OK : statusFor(earliest);
    }

    private ExpiryStatus statusFor(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return ExpiryStatus.EXPIRED;
        }
        if (!date.isAfter(today.plusDays(EXPIRING_SOON_DAYS))) {
            return ExpiryStatus.EXPIRING;
        }
        return ExpiryStatus.OK;
    }

    private AssetDetailResponse.ExpiryDate expiryDateOf(LocalDate date) {
        return new AssetDetailResponse.ExpiryDate(date, date == null ? null : statusFor(date).name());
    }

    private String nextAssetKey() {
        String prefix = "AST-";
        long seq = assetRepository.countByAssetKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private AssetSummaryResponse toSummary(Asset a, String pendingApprovalTargetState) {
        return new AssetSummaryResponse(a.getId(), a.getAssetKey(), a.getName(), a.getType().name(),
                a.getStatus().name(), a.getOwner(), a.earliestExpiry(), expiryStatusOf(a).name(),
                pendingApprovalTargetState);
    }

    private AssetDetailResponse toDetail(Asset a) {
        Map<String, String> attrs = attributeRepository.findByAssetId(a.getId()).stream()
                .collect(Collectors.toMap(AssetAttribute::getAttrKey, AssetAttribute::getAttrValue,
                        (x, y) -> y, LinkedHashMap::new));
        List<AssetDetailResponse.LifecycleEntry> history = lifecycleHistoryRepository
                .findByAssetIdOrderByChangedAtAsc(a.getId()).stream()
                .map(h -> new AssetDetailResponse.LifecycleEntry(h.getStage().name(), h.getChangedAt()))
                .toList();
        List<AssetDetailResponse.LinkedTicket> tickets = ticketLinkRepository
                .findBySourceTypeAndSourceId(TicketType.ASSET, a.getId()).stream()
                .map(l -> new AssetDetailResponse.LinkedTicket(l.getTargetType().name(),
                        ticketKeyOf(l.getTargetType(), l.getTargetId())))
                .toList();
        List<AssetDetailResponse.LinkedCi> cis = ciRepository.findByAssetId(a.getId()).stream()
                .map(c -> new AssetDetailResponse.LinkedCi(c.getId(), c.getName()))
                .toList();

        ApprovalRequest latestApproval = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(TT, a.getId()).orElse(null);
        AssetDetailResponse.ApprovalInfo approvalInfo = new AssetDetailResponse.ApprovalInfo(
                latestApproval != null ? latestApproval.getId() : null,
                latestApproval != null ? latestApproval.getStatus().name() : null,
                latestApproval != null ? latestApproval.getTargetState() : null);

        return new AssetDetailResponse(a.getId(), a.getAssetKey(), a.getName(), a.getType().name(),
                a.getStatus().name(), a.getOwner(), a.getLocation(), attrs,
                new AssetDetailResponse.Expiry(expiryDateOf(a.getLicenseExpiry()),
                        expiryDateOf(a.getWarrantyExpiry()), expiryDateOf(a.getContractExpiry())),
                history, approvalInfo, tickets, cis);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
