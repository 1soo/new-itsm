package com.itsm.compliance.application;

import com.itsm.auth.application.AuditLogService;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.compliance.application.dto.CorrectiveActionCreateRequest;
import com.itsm.compliance.application.dto.CorrectiveActionStatusTransitionRequest;
import com.itsm.compliance.application.dto.CreateRequirementRequest;
import com.itsm.compliance.application.dto.LinkRequest;
import com.itsm.compliance.application.dto.OwnerRequest;
import com.itsm.compliance.application.dto.UpdateRequirementRequest;
import com.itsm.compliance.domain.ComplianceRequirement;
import com.itsm.compliance.domain.CorrectiveAction;
import com.itsm.compliance.domain.CorrectiveActionStatus;
import com.itsm.compliance.domain.repository.ComplianceRequirementRepository;
import com.itsm.compliance.domain.repository.CorrectiveActionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplianceServiceTest {

    @Mock ComplianceRequirementRepository requirementRepository;
    @Mock CorrectiveActionRepository correctiveActionRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock ChangeRequestRepository changeRequestRepository;
    @Mock AuditLogService auditLogService;
    @Mock ApprovalGateService approvalGateService;
    @Mock ApprovalRequestRepository approvalRequestRepository;

    ComplianceService service;

    @BeforeEach
    void setUp() {
        service = new ComplianceService(requirementRepository, correctiveActionRepository, ticketLinkRepository,
                appUserRepository, changeRequestRepository, auditLogService, approvalGateService, approvalRequestRepository);
        when(requirementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(requirementRepository.countByRequirementKeyStartingWith(any())).thenReturn(0L);
        when(correctiveActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(correctiveActionRepository.findByRequirementId(any())).thenReturn(List.of());
        when(approvalRequestRepository.findTopByTicketTypeAndTicketIdOrderByIdDesc(any(), any()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, String... roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, "u@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    private ComplianceRequirement requirement() {
        return new ComplianceRequirement("COMP-2026-0001", "개인정보보호법 준수", "개인정보보호법 제29조", null);
    }

    private CorrectiveAction correctiveAction(Long requirementId, CorrectiveActionStatus status) {
        CorrectiveAction action = new CorrectiveAction(requirementId, "내용");
        if (status != CorrectiveActionStatus.DETECTED) {
            action.changeStatus(status);
        }
        return action;
    }

    // ---------- create ----------

    @Test
    void createForbiddenForNonOfficer() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.create(new CreateRequirementRequest("이름", "근거", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void createSuccessGeneratesRequirementKey() {
        login(1L, "COMPLIANCE_OFFICER");

        var response = service.create(new CreateRequirementRequest("개인정보보호법 준수", "제29조", "전사"));

        assertThat(response.requirementKey()).startsWith("COMP-");
    }

    // ---------- detail ----------

    @Test
    void detailNotFoundThrows() {
        login(1L, "COMPLIANCE_OFFICER");
        when(requirementRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.COMPLIANCE_REQUIREMENT_NOT_FOUND));
    }

    @Test
    void detailWithUnresolvedActionIsNonCompliant() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));
        when(correctiveActionRepository.findByRequirementId(1L))
                .thenReturn(List.of(correctiveAction(1L, CorrectiveActionStatus.DETECTED)));
        when(ticketLinkRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(List.of());

        var response = service.detail(1L);

        assertThat(response.complianceStatus()).isEqualTo("NON_COMPLIANT");
    }

    @Test
    void detailWithOnlyResolvedActionsIsCompliant() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));
        when(correctiveActionRepository.findByRequirementId(1L))
                .thenReturn(List.of(correctiveAction(1L, CorrectiveActionStatus.RESOLVED)));
        when(ticketLinkRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(List.of());

        var response = service.detail(1L);

        assertThat(response.complianceStatus()).isEqualTo("COMPLIANT");
    }

    // ---------- update ----------

    @Test
    void updateNotFoundThrows() {
        login(1L, "COMPLIANCE_OFFICER");
        when(requirementRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, new UpdateRequirementRequest("이름", "근거", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.COMPLIANCE_REQUIREMENT_NOT_FOUND));
    }

    // ---------- change link ----------

    @Test
    void linkToNonExistentChangeThrows400() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));
        when(changeRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.link(1L, new LinkRequest(999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void linkToExistingChangeSucceeds() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));
        ChangeRequest change = new ChangeRequest("CHG-2026-0001", "요약", null, ChangeType.NORMAL, null,
                null, null, null, null);
        when(changeRequestRepository.findById(5L)).thenReturn(Optional.of(change));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);

        service.link(1L, new LinkRequest(5L));
        // 예외 없이 완료되면 성공(ticketLinkRepository.save 호출은 mock이라 별도 검증 불필요)
    }

    // ---------- owner ----------

    @Test
    void assignOwnerInvalidUserThrows() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));
        when(appUserRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignOwner(1L, new OwnerRequest(9L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void assignOwnerSuccess() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));
        AppUser owner = new AppUser("owner@itsm.local", "hash", "책임자", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(owner, "id", 7L);
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(owner));

        var response = service.assignOwner(1L, new OwnerRequest(7L));

        assertThat(response.owner()).isEqualTo("책임자");
    }

    // ---------- corrective action ----------

    @Test
    void addCorrectiveActionStartsAtDetected() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement req = requirement();
        ReflectionTestUtils.setField(req, "id", 1L);
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(req));

        var response = service.addCorrectiveAction(1L, new CorrectiveActionCreateRequest("보완 조치 필요"));

        assertThat(response.status()).isEqualTo("DETECTED");
    }

    @Test
    void transitionSkippingOrderThrows() {
        login(1L, "COMPLIANCE_OFFICER");
        CorrectiveAction action = correctiveAction(1L, CorrectiveActionStatus.DETECTED);
        when(correctiveActionRepository.findById(1L)).thenReturn(Optional.of(action));

        assertThatThrownBy(() -> service.transitionCorrectiveAction(1L,
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.RESOLVED)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionSequentialSucceeds() {
        login(1L, "COMPLIANCE_OFFICER");
        CorrectiveAction action = correctiveAction(1L, CorrectiveActionStatus.DETECTED);
        when(correctiveActionRepository.findById(1L)).thenReturn(Optional.of(action));

        var response = service.transitionCorrectiveAction(1L,
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.IN_PROGRESS));

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void transitionNotFoundThrows() {
        login(1L, "COMPLIANCE_OFFICER");
        when(correctiveActionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transitionCorrectiveAction(9L,
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.IN_PROGRESS)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.CORRECTIVE_ACTION_NOT_FOUND));
    }

    @Test
    void transitionToResolvedGatePassSucceeds() {
        login(1L, "COMPLIANCE_OFFICER");
        CorrectiveAction action = correctiveAction(1L, CorrectiveActionStatus.IN_PROGRESS);
        when(correctiveActionRepository.findById(1L)).thenReturn(Optional.of(action));
        doNothing().when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        var response = service.transitionCorrectiveAction(1L,
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.RESOLVED));

        assertThat(response.status()).isEqualTo("RESOLVED");
    }

    @Test
    void transitionToResolvedGateBlockedPropagates409() {
        login(1L, "COMPLIANCE_OFFICER");
        CorrectiveAction action = correctiveAction(1L, CorrectiveActionStatus.IN_PROGRESS);
        when(correctiveActionRepository.findById(1L)).thenReturn(Optional.of(action));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 77L))
                .when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.transitionCorrectiveAction(1L,
                new CorrectiveActionStatusTransitionRequest(CorrectiveActionStatus.RESOLVED)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(77L);
                });
    }

    // ---------- metrics ----------

    @Test
    void metricsReturnsZerosWhenNoData() {
        login(1L, "COMPLIANCE_OFFICER");
        when(requirementRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        var response = service.metrics(null, null);

        assertThat(response.totalRequirements()).isZero();
        assertThat(response.complianceRate()).isZero();
    }

    @Test
    void metricsComputesComplianceRate() {
        login(1L, "COMPLIANCE_OFFICER");
        ComplianceRequirement compliant = requirement();
        ReflectionTestUtils.setField(compliant, "id", 1L);
        ComplianceRequirement nonCompliant = requirement();
        ReflectionTestUtils.setField(nonCompliant, "id", 2L);
        when(requirementRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(compliant, nonCompliant));
        when(correctiveActionRepository.findByRequirementIdIn(any()))
                .thenReturn(List.of(correctiveAction(2L, CorrectiveActionStatus.DETECTED)));

        var response = service.metrics(null, null);

        assertThat(response.totalRequirements()).isEqualTo(2);
        assertThat(response.compliantCount()).isEqualTo(1);
        assertThat(response.nonCompliantCount()).isEqualTo(1);
        assertThat(response.openCorrectiveActionCount()).isEqualTo(1);
        assertThat(response.complianceRate()).isEqualTo(0.5);
    }
}
