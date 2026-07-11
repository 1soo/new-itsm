package com.itsm.auth.application;

import com.itsm.auth.application.dto.ApprovalDomainResponse;
import com.itsm.auth.application.dto.ApprovalProcessDeletedResponse;
import com.itsm.auth.application.dto.ApprovalProcessDetailResponse;
import com.itsm.auth.application.dto.ApprovalProcessStepInput;
import com.itsm.auth.application.dto.ApprovalProcessSummaryResponse;
import com.itsm.auth.application.dto.CreateApprovalProcessRequest;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.application.dto.UpdateApprovalProcessRequest;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.approval.application.ApprovalRequestSubtypeProvider;
import com.itsm.common.approval.application.RequestSubtypeOption;
import com.itsm.common.approval.domain.ApprovalProcess;
import com.itsm.common.approval.domain.ApprovalProcessRequesterRole;
import com.itsm.common.approval.domain.ApprovalProcessStep;
import com.itsm.common.approval.domain.ApprovalProcessStepRole;
import com.itsm.common.approval.domain.repository.ApprovalProcessRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessRequesterRoleRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessStepRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessStepRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 승인 프로세스 정의 관리자 CRUD 유스케이스(API-AUTH-023~029, SYSTEM_ADMIN 전용).
 * 규칙 우선순위(priorityTier): 1=도메인 기본, 2=요청유형 전용, 3=승인요청자 역할 전용
 * (docs/02_plan/database/common.md approval_process 상세).
 */
@Service
public class ApprovalProcessAdminService {

    /** 요청자가 제출해 진행되는 티켓/요청 개념이 있는 9개 도메인(maintainer 확인, 2026-07-11). */
    private static final Map<String, DomainMeta> DOMAINS = new LinkedHashMap<>();
    private static final short TIER_DOMAIN = 1;
    private static final short TIER_SUBTYPE = 2;
    private static final short TIER_REQUESTER_ROLE = 3;
    private static final int MAX_STEPS = 10;

    static {
        DOMAINS.put("SERVICE_REQUEST", new DomainMeta("서비스 요청", true));
        DOMAINS.put("CHANGE", new DomainMeta("변경 관리", true));
        DOMAINS.put("KNOWLEDGE", new DomainMeta("지식 관리", false));
        DOMAINS.put("INCIDENT", new DomainMeta("인시던트 관리", false));
        DOMAINS.put("PROBLEM", new DomainMeta("문제 관리", false));
        DOMAINS.put("ASSET", new DomainMeta("자산 관리", false));
        DOMAINS.put("VULNERABILITY", new DomainMeta("취약점 관리", false));
        DOMAINS.put("COMPLIANCE", new DomainMeta("컴플라이언스", false));
        DOMAINS.put("ESM", new DomainMeta("엔터프라이즈 서비스 관리", false));
    }

    private static final List<RequestSubtypeOption> CHANGE_SUBTYPES = List.of(
            new RequestSubtypeOption("STANDARD", "표준"),
            new RequestSubtypeOption("NORMAL", "일반"),
            new RequestSubtypeOption("EMERGENCY", "긴급"));

    private final ApprovalProcessRepository approvalProcessRepository;
    private final ApprovalProcessRequesterRoleRepository requesterRoleRepository;
    private final ApprovalProcessStepRepository processStepRepository;
    private final ApprovalProcessStepRoleRepository processStepRoleRepository;
    private final RoleRepository roleRepository;
    private final Map<String, ApprovalRequestSubtypeProvider> subtypeProviders;

    public ApprovalProcessAdminService(ApprovalProcessRepository approvalProcessRepository,
                                       ApprovalProcessRequesterRoleRepository requesterRoleRepository,
                                       ApprovalProcessStepRepository processStepRepository,
                                       ApprovalProcessStepRoleRepository processStepRoleRepository,
                                       RoleRepository roleRepository,
                                       List<ApprovalRequestSubtypeProvider> subtypeProviders) {
        this.approvalProcessRepository = approvalProcessRepository;
        this.requesterRoleRepository = requesterRoleRepository;
        this.processStepRepository = processStepRepository;
        this.processStepRoleRepository = processStepRoleRepository;
        this.roleRepository = roleRepository;
        this.subtypeProviders = subtypeProviders.stream()
                .collect(Collectors.toMap(ApprovalRequestSubtypeProvider::supportedDomain, p -> p));
    }

    // ---------- API-AUTH-023 ----------

    public List<ApprovalDomainResponse> domains() {
        return DOMAINS.entrySet().stream()
                .map(e -> new ApprovalDomainResponse(e.getKey(), e.getValue().label(), e.getValue().hasRequestSubtype()))
                .toList();
    }

    // ---------- API-AUTH-024 ----------

    public List<RequestSubtypeOption> requestSubtypes(String domain) {
        if (!DOMAINS.containsKey(domain)) {
            throw new BusinessException(ErrorCode.INVALID_APPROVAL_DOMAIN);
        }
        if (!DOMAINS.get(domain).hasRequestSubtype()) {
            return List.of();
        }
        if ("CHANGE".equals(domain)) {
            return CHANGE_SUBTYPES;
        }
        ApprovalRequestSubtypeProvider provider = subtypeProviders.get(domain);
        return provider != null ? provider.subtypes() : List.of();
    }

    // ---------- API-AUTH-025 ----------

    @Transactional(readOnly = true)
    public PageResponse<ApprovalProcessSummaryResponse> list(String domain, Pageable pageable) {
        return PageResponse.from(approvalProcessRepository.search(domain, pageable), this::toSummary);
    }

    // ---------- API-AUTH-026 ----------

    @Transactional(readOnly = true)
    public ApprovalProcessDetailResponse detail(Long id) {
        return toDetail(findProcess(id));
    }

    // ---------- API-AUTH-027 ----------

    @Transactional
    public ApprovalProcessDetailResponse create(CreateApprovalProcessRequest request) {
        validateDomain(request.domain());
        validateSteps(request.steps());
        List<Long> requesterRoleIds = request.requesterRoleIds() == null ? List.of() : request.requesterRoleIds();
        short tier = computeTier(requesterRoleIds, request.requestSubtypeKey());
        assertNoPriorityConflict(request.domain(), request.requestSubtypeKey(), tier, requesterRoleIds, null);

        ApprovalProcess process = approvalProcessRepository.save(new ApprovalProcess(
                request.domain(), request.requestSubtypeKey(), tier, request.name(), request.description()));
        saveRequesterRoles(process.getId(), requesterRoleIds);
        saveSteps(process.getId(), request.steps());
        return toDetail(process);
    }

    // ---------- API-AUTH-028 ----------

    @Transactional
    public ApprovalProcessDetailResponse update(Long id, UpdateApprovalProcessRequest request) {
        ApprovalProcess process = findProcess(id);
        List<Long> requesterRoleIds = request.requesterRoleIds() != null ? request.requesterRoleIds()
                : requesterRoleRepository.findByApprovalProcessId(id).stream()
                        .map(ApprovalProcessRequesterRole::getRoleId).toList();
        if (request.steps() != null) {
            validateSteps(request.steps());
        }
        short tier = computeTier(requesterRoleIds, process.getRequestSubtypeKey());
        assertNoPriorityConflict(process.getDomain(), process.getRequestSubtypeKey(), tier, requesterRoleIds, id);

        process.update(request.name(), request.description(), tier);
        approvalProcessRepository.save(process);
        if (request.requesterRoleIds() != null) {
            requesterRoleRepository.deleteByApprovalProcessId(id);
            requesterRoleRepository.flush();
            saveRequesterRoles(id, requesterRoleIds);
        }
        if (request.steps() != null) {
            List<ApprovalProcessStep> existingSteps = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(id);
            processStepRoleRepository.deleteByStepIdIn(existingSteps.stream().map(ApprovalProcessStep::getId).toList());
            processStepRepository.deleteByApprovalProcessId(id);
            processStepRepository.flush();
            saveSteps(id, request.steps());
        }
        return toDetail(process);
    }

    // ---------- API-AUTH-029 ----------

    @Transactional
    public ApprovalProcessDeletedResponse delete(Long id) {
        ApprovalProcess process = findProcess(id);
        process.markDeleted();
        approvalProcessRepository.save(process);
        return new ApprovalProcessDeletedResponse(id, true);
    }

    // ---------- helpers ----------

    private void validateDomain(String domain) {
        if (!DOMAINS.containsKey(domain)) {
            throw new BusinessException(ErrorCode.INVALID_APPROVAL_DOMAIN);
        }
    }

    private void validateSteps(List<ApprovalProcessStepInput> steps) {
        if (steps == null) {
            return;
        }
        if (steps.size() > MAX_STEPS) {
            throw new BusinessException(ErrorCode.APPROVAL_STEPS_TOO_MANY);
        }
        for (ApprovalProcessStepInput step : steps) {
            if (step.roleIds() == null || step.roleIds().isEmpty()) {
                throw new BusinessException(ErrorCode.APPROVAL_STEP_ROLES_REQUIRED);
            }
        }
    }

    /** requesterRoleIds 있으면 tier=3, 없고 requestSubtypeKey 있으면 tier=2, 둘 다 없으면 tier=1. */
    private short computeTier(List<Long> requesterRoleIds, String requestSubtypeKey) {
        if (!requesterRoleIds.isEmpty()) {
            return TIER_REQUESTER_ROLE;
        }
        return requestSubtypeKey != null ? TIER_SUBTYPE : TIER_DOMAIN;
    }

    private void assertNoPriorityConflict(String domain, String requestSubtypeKey, short tier,
                                          List<Long> requesterRoleIds, Long excludeId) {
        boolean conflict = switch (tier) {
            case TIER_DOMAIN -> excludeId == null
                    ? approvalProcessRepository.existsByDomainAndPriorityTier(domain, TIER_DOMAIN)
                    : approvalProcessRepository.existsByDomainAndPriorityTierAndIdNot(domain, TIER_DOMAIN, excludeId);
            case TIER_SUBTYPE -> excludeId == null
                    ? approvalProcessRepository.existsByDomainAndRequestSubtypeKeyAndPriorityTier(domain, requestSubtypeKey, TIER_SUBTYPE)
                    : approvalProcessRepository.existsByDomainAndRequestSubtypeKeyAndPriorityTierAndIdNot(domain, requestSubtypeKey, TIER_SUBTYPE, excludeId);
            default -> hasOverlappingRequesterRoles(domain, requestSubtypeKey, requesterRoleIds, excludeId);
        };
        if (conflict) {
            throw new BusinessException(ErrorCode.APPROVAL_PROCESS_PRIORITY_CONFLICT);
        }
    }

    private boolean hasOverlappingRequesterRoles(String domain, String requestSubtypeKey,
                                                 List<Long> requesterRoleIds, Long excludeId) {
        Set<Long> mine = Set.copyOf(requesterRoleIds);
        for (ApprovalProcess candidate : approvalProcessRepository
                .findByDomainAndRequestSubtypeKeyAndPriorityTier(domain, requestSubtypeKey, TIER_REQUESTER_ROLE)) {
            if (excludeId != null && candidate.getId().equals(excludeId)) {
                continue;
            }
            boolean overlap = requesterRoleRepository.findByApprovalProcessId(candidate.getId()).stream()
                    .map(ApprovalProcessRequesterRole::getRoleId)
                    .anyMatch(mine::contains);
            if (overlap) {
                return true;
            }
        }
        return false;
    }

    private void saveRequesterRoles(Long processId, List<Long> requesterRoleIds) {
        for (Long roleId : requesterRoleIds) {
            requesterRoleRepository.save(new ApprovalProcessRequesterRole(processId, roleId));
        }
    }

    private void saveSteps(Long processId, List<ApprovalProcessStepInput> steps) {
        if (steps == null) {
            return;
        }
        short stepNo = 1;
        for (ApprovalProcessStepInput input : steps) {
            ApprovalProcessStep step = processStepRepository.save(
                    new ApprovalProcessStep(processId, stepNo, input.decisionMode()));
            for (Long roleId : input.roleIds()) {
                processStepRoleRepository.save(new ApprovalProcessStepRole(step.getId(), roleId));
            }
            stepNo++;
        }
    }

    private ApprovalProcess findProcess(Long id) {
        return approvalProcessRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_PROCESS_NOT_FOUND));
    }

    private ApprovalProcessSummaryResponse toSummary(ApprovalProcess p) {
        List<String> requesterRoles = requesterRoleRepository.findByApprovalProcessId(p.getId()).stream()
                .map(r -> roleRepository.findById(r.getRoleId()).map(Role::getRoleCode).orElse(null))
                .toList();
        int stepCount = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(p.getId()).size();
        return new ApprovalProcessSummaryResponse(p.getId(), p.getDomain(), p.getRequestSubtypeKey(),
                resolveSubtypeLabel(p.getDomain(), p.getRequestSubtypeKey()), p.getPriorityTier(), p.getName(),
                requesterRoles, stepCount);
    }

    private ApprovalProcessDetailResponse toDetail(ApprovalProcess p) {
        List<Long> requesterRoleIds = requesterRoleRepository.findByApprovalProcessId(p.getId()).stream()
                .map(ApprovalProcessRequesterRole::getRoleId).toList();
        List<ApprovalProcessDetailResponse.StepDto> steps = processStepRepository
                .findByApprovalProcessIdOrderByStepNoAsc(p.getId()).stream()
                .map(step -> new ApprovalProcessDetailResponse.StepDto(step.getStepNo(), step.getDecisionMode().name(),
                        processStepRoleRepository.findByStepId(step.getId()).stream()
                                .map(ApprovalProcessStepRole::getRoleId).toList()))
                .toList();
        return new ApprovalProcessDetailResponse(p.getId(), p.getDomain(), p.getRequestSubtypeKey(), p.getName(),
                p.getDescription(), requesterRoleIds, steps);
    }

    private String resolveSubtypeLabel(String domain, String requestSubtypeKey) {
        if (requestSubtypeKey == null) {
            return null;
        }
        return requestSubtypes(domain).stream()
                .filter(o -> o.key().equals(requestSubtypeKey))
                .map(RequestSubtypeOption::label)
                .findFirst().orElse(null);
    }

    private record DomainMeta(String label, boolean hasRequestSubtype) {
    }
}
