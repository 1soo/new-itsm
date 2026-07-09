package com.itsm.srm.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.Approval;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.ApprovalRepository;
import com.itsm.common.ticket.repository.CommentRepository;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.srm.application.dto.ApprovalDecision;
import com.itsm.srm.application.dto.ApprovalDecisionRequest;
import com.itsm.srm.application.dto.AssignRequest;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.CsatRequest;
import com.itsm.srm.application.dto.StatusTransitionRequest;
import com.itsm.srm.domain.CatalogFormField;
import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.CatalogFormFieldRepository;
import com.itsm.srm.domain.repository.CsatRepository;
import com.itsm.srm.domain.repository.QueueRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import com.itsm.srm.domain.repository.ServiceRequestFormValueRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceRequestServiceTest {

    @Mock ServiceRequestRepository requestRepository;
    @Mock ServiceRequestFormValueRepository formValueRepository;
    @Mock ServiceCatalogItemRepository catalogItemRepository;
    @Mock CatalogFormFieldRepository formFieldRepository;
    @Mock QueueRepository queueRepository;
    @Mock CsatRepository csatRepository;
    @Mock ApprovalRepository approvalRepository;
    @Mock CommentRepository commentRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock AssetService assetService;

    ServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new ServiceRequestService(requestRepository, formValueRepository, catalogItemRepository,
                formFieldRepository, queueRepository, csatRepository, approvalRepository, commentRepository,
                timelineRepository, appUserRepository, ticketLinkRepository, assetService);
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(csatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
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

    private ServiceRequest request(Long requesterId, RequestStatus status) {
        ServiceRequest r = new ServiceRequest("SRM-2026-0001", 10L, requesterId, 1L, null, null);
        if (status != RequestStatus.SUBMITTED) {
            r.changeStatus(status);
        }
        return r;
    }

    private ServiceCatalogItem catalog(boolean approvalRequired) {
        return new ServiceCatalogItem("Laptop", null, null, approvalRequired,
                approvalRequired ? "APPROVER" : null, 1L, null, null);
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    // ---------- create ----------

    @Test
    void createCatalogNotFoundThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createRequiredFieldMissingThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalog(false)));
        when(formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(any()))
                .thenReturn(List.of(new CatalogFormField(10L, "reason", "사유", "text", true, null, 0)));

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING));
    }

    @Test
    void createSuccess() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalog(false)));
        when(formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(requestRepository.countByTicketKeyStartingWith(any())).thenReturn(0L);

        var response = service.create(new CreateRequestRequest(10L, Map.of("reason", "고장")));

        assertThat(response.ticketKey()).startsWith("SRM-");
        assertThat(response.status()).isEqualTo("SUBMITTED");
    }

    // ---------- list ----------

    @Test
    void listScopeAllWithoutRoleForbidden() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.list("all", null, null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- detail ----------

    @Test
    void detailNotFoundThrows() {
        login(1L, "END_USER");
        when(requestRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REQUEST_NOT_FOUND));
    }

    @Test
    void detailForbiddenForOtherUsersRequest() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- transition ----------

    @Test
    void transitionTerminalThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.CLOSED)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(RequestStatus.CLOSED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REQUEST_ALREADY_CLOSED));
    }

    @Test
    void transitionInvalidThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));
        when(catalogItemRepository.findById(any())).thenReturn(Optional.of(catalog(false)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(RequestStatus.FULFILLED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionForbiddenRole() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(RequestStatus.VALIDATED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void transitionApprovalPendingConflict() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.APPROVAL_PENDING)));
        when(catalogItemRepository.findById(any())).thenReturn(Optional.of(catalog(true)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.SERVICE_REQUEST, 1L))
                .thenReturn(Optional.of(new Approval(TicketType.SERVICE_REQUEST, 1L, "APPROVER")));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING));
    }

    @Test
    void transitionSuccessValidated() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));
        when(catalogItemRepository.findById(any())).thenReturn(Optional.of(catalog(false)));

        var response = service.transition(1L, new StatusTransitionRequest(RequestStatus.VALIDATED, "검증 완료"));

        assertThat(response.status()).isEqualTo("VALIDATED");
    }

    @Test
    void routedWithApprovalBecomesApprovalPending() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.VALIDATED)));
        when(catalogItemRepository.findById(any())).thenReturn(Optional.of(catalog(true)));
        when(approvalRepository.findByTicketTypeAndTicketId(any(), any())).thenReturn(Optional.empty());

        var response = service.transition(1L, new StatusTransitionRequest(RequestStatus.ROUTED, null));

        assertThat(response.status()).isEqualTo("APPROVAL_PENDING");
    }

    // ---------- approval ----------

    @Test
    void approvalNotFoundThrows() {
        login(5L, "APPROVER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.APPROVAL_PENDING)));
        when(approvalRepository.findByTicketTypeAndTicketId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.decideApproval(1L, new ApprovalDecisionRequest(ApprovalDecision.APPROVE, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_NOT_FOUND));
    }

    @Test
    void approvalNotDesignatedForbidden() {
        login(5L, "APPROVER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.APPROVAL_PENDING)));
        when(approvalRepository.findByTicketTypeAndTicketId(any(), any()))
                .thenReturn(Optional.of(new Approval(TicketType.SERVICE_REQUEST, 1L, "CAB_APPROVER")));

        assertThatThrownBy(() -> service.decideApproval(1L, new ApprovalDecisionRequest(ApprovalDecision.APPROVE, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void approvalRejectWithoutReasonThrows() {
        login(5L, "APPROVER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.APPROVAL_PENDING)));
        when(approvalRepository.findByTicketTypeAndTicketId(any(), any()))
                .thenReturn(Optional.of(new Approval(TicketType.SERVICE_REQUEST, 1L, "APPROVER")));

        assertThatThrownBy(() -> service.decideApproval(1L, new ApprovalDecisionRequest(ApprovalDecision.REJECT, " ")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REJECT_REASON_REQUIRED));
    }

    @Test
    void approvalAlreadyDecidedThrows() {
        login(5L, "APPROVER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.IN_FULFILLMENT)));
        Approval decided = new Approval(TicketType.SERVICE_REQUEST, 1L, "APPROVER");
        decided.approve(5L, "ok");
        when(approvalRepository.findByTicketTypeAndTicketId(any(), any())).thenReturn(Optional.of(decided));

        assertThatThrownBy(() -> service.decideApproval(1L, new ApprovalDecisionRequest(ApprovalDecision.APPROVE, "again")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_ALREADY_DECIDED));
    }

    @Test
    void approvalApproveSuccess() {
        login(5L, "APPROVER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.APPROVAL_PENDING)));
        when(approvalRepository.findByTicketTypeAndTicketId(any(), any()))
                .thenReturn(Optional.of(new Approval(TicketType.SERVICE_REQUEST, 1L, "APPROVER")));

        var response = service.decideApproval(1L, new ApprovalDecisionRequest(ApprovalDecision.APPROVE, "승인"));

        assertThat(response.approvalStatus()).isEqualTo("APPROVED");
    }

    // ---------- assign ----------

    @Test
    void assignAssigneeNotFoundThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.VALIDATED)));
        when(appUserRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(1L, new AssignRequest(9L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ASSIGNEE_NOT_FOUND));
    }

    // ---------- csat ----------

    @Test
    void csatNotRequesterForbidden() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.CLOSED)));

        assertThatThrownBy(() -> service.submitCsat(1L, new CsatRequest(5, "good")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void csatNotClosedThrows() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(1L, RequestStatus.FULFILLED)));

        assertThatThrownBy(() -> service.submitCsat(1L, new CsatRequest(5, "good")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.CSAT_NOT_ALLOWED));
    }

    @Test
    void csatAlreadySubmittedThrows() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(1L, RequestStatus.CLOSED)));
        when(csatRepository.existsByServiceRequestId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.submitCsat(1L, new CsatRequest(5, "good")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.CSAT_ALREADY_SUBMITTED));
    }

    @Test
    void csatSuccess() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(1L, RequestStatus.CLOSED)));
        when(csatRepository.existsByServiceRequestId(1L)).thenReturn(false);

        var response = service.submitCsat(1L, new CsatRequest(4, "만족"));

        assertThat(response.score()).isEqualTo(4);
    }
}
