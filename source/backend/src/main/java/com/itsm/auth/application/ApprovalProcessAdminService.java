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
 * 규칙 우선순위(priorityTier, 2026-07-15 3축 재설계): domain/requestSubtypeKey/requesterRoleIds 3축을
 * 각각 독립 지정 가능(축 비우면 해당 축 전체 매칭). 산정식 = (지정 축 개수×10) + (역할 지정 시 4) +
 * (요청유형 지정 시 2) + (도메인 지정 시 1). 실제 발생 가능 값: 0(전체 미지정)/11(도메인만)/14(역할만)/
 * 23(도메인+요청유형)/25(도메인+역할)/37(도메인+요청유형+역할) (docs/02_plan/database/common.md approval_process 상세).
 */
@Service
public class ApprovalProcessAdminService {

    /** 요청자가 제출해 진행되는 티켓/요청 개념이 있는 9개 도메인(maintainer 확인, 2026-07-11). */
    private static final Map<String, DomainMeta> DOMAINS = new LinkedHashMap<>();
    private static final short TIER_NONE = 0;
    private static final short TIER_DOMAIN_ONLY = 11;
    private static final short TIER_ROLE_ONLY = 14;
    private static final short TIER_DOMAIN_SUBTYPE = 23;
    private static final short TIER_DOMAIN_ROLE = 25;
    private static final short TIER_DOMAIN_SUBTYPE_ROLE = 37;
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
        validateSubtypeRequiresDomain(request.domain(), request.requestSubtypeKey());
        validateSteps(request.steps());
        List<Long> requesterRoleIds = request.requesterRoleIds() == null ? List.of() : request.requesterRoleIds();
        short tier = computeTier(request.domain(), request.requestSubtypeKey(), requesterRoleIds);
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
        short tier = computeTier(process.getDomain(), process.getRequestSubtypeKey(), requesterRoleIds);
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

    /** domain은 선택(null=전체 도메인 적용). 지정 시에는 9개 후보 중 하나여야 한다. */
    private void validateDomain(String domain) {
        if (domain != null && !DOMAINS.containsKey(domain)) {
            throw new BusinessException(ErrorCode.INVALID_APPROVAL_DOMAIN);
        }
    }

    /** requestSubtypeKey는 domain에 종속된 어휘라, domain이 null이면 반드시 null이어야 한다. */
    private void validateSubtypeRequiresDomain(String domain, String requestSubtypeKey) {
        if (domain == null && requestSubtypeKey != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "domain이 없으면 requestSubtypeKey도 지정할 수 없습니다.");
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

    /** (지정 축 개수×10) + (역할 지정 시 4) + (요청유형 지정 시 2) + (도메인 지정 시 1). */
    private short computeTier(String domain, String requestSubtypeKey, List<Long> requesterRoleIds) {
        boolean hasDomain = domain != null;
        boolean hasSubtype = requestSubtypeKey != null;
        boolean hasRole = !requesterRoleIds.isEmpty();
        int axisCount = (hasDomain ? 1 : 0) + (hasSubtype ? 1 : 0) + (hasRole ? 1 : 0);
        return (short) (axisCount * 10 + (hasRole ? 4 : 0) + (hasSubtype ? 2 : 0) + (hasDomain ? 1 : 0));
    }

    private void assertNoPriorityConflict(String domain, String requestSubtypeKey, short tier,
                                          List<Long> requesterRoleIds, Long excludeId) {
        boolean conflict = switch (tier) {
            case TIER_NONE -> excludeId == null
                    ? approvalProcessRepository.existsByPriorityTier(TIER_NONE)
                    : approvalProcessRepository.existsByPriorityTierAndIdNot(TIER_NONE, excludeId);
            case TIER_DOMAIN_ONLY -> excludeId == null
                    ? approvalProcessRepository.existsByDomainAndPriorityTier(domain, TIER_DOMAIN_ONLY)
                    : approvalProcessRepository.existsByDomainAndPriorityTierAndIdNot(domain, TIER_DOMAIN_ONLY, excludeId);
            case TIER_DOMAIN_SUBTYPE -> excludeId == null
                    ? approvalProcessRepository.existsByDomainAndRequestSubtypeKeyAndPriorityTier(domain, requestSubtypeKey, TIER_DOMAIN_SUBTYPE)
                    : approvalProcessRepository.existsByDomainAndRequestSubtypeKeyAndPriorityTierAndIdNot(domain, requestSubtypeKey, TIER_DOMAIN_SUBTYPE, excludeId);
            case TIER_ROLE_ONLY -> hasOverlappingRequesterRoles(
                    approvalProcessRepository.findByPriorityTier(TIER_ROLE_ONLY), requesterRoleIds, excludeId);
            case TIER_DOMAIN_ROLE -> hasOverlappingRequesterRoles(
                    approvalProcessRepository.findByDomainAndPriorityTier(domain, TIER_DOMAIN_ROLE), requesterRoleIds, excludeId);
            default -> hasOverlappingRequesterRoles(
                    approvalProcessRepository.findByDomainAndRequestSubtypeKeyAndPriorityTier(domain, requestSubtypeKey, TIER_DOMAIN_SUBTYPE_ROLE),
                    requesterRoleIds, excludeId);
        };
        if (conflict) {
            throw new BusinessException(ErrorCode.APPROVAL_PROCESS_PRIORITY_CONFLICT);
        }
    }

    private boolean hasOverlappingRequesterRoles(List<ApprovalProcess> candidates, List<Long> requesterRoleIds, Long excludeId) {
        Set<Long> mine = Set.copyOf(requesterRoleIds);
        for (ApprovalProcess candidate : candidates) {
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
