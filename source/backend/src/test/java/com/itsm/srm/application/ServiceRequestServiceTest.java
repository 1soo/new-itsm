package com.itsm.srm.application;

import tools.jackson.databind.ObjectMapper;
import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.ApprovalRequestStatus;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.CommentRepository;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.srm.application.dto.AssignRequest;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.CsatRequest;
import com.itsm.srm.application.dto.StatusTransitionRequest;
import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.CsatRepository;
import com.itsm.srm.domain.repository.ServiceCatalogCategoryRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceRequestServiceTest {

    @Mock ServiceRequestRepository requestRepository;
    @Mock ServiceCatalogItemRepository catalogItemRepository;
    @Mock ServiceCatalogCategoryRepository categoryRepository;
    @Mock CsatRepository csatRepository;
    @Mock CommentRepository commentRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock RoleRepository roleRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock AssetService assetService;
    @Mock ApprovalGateService approvalGateService;
    @Mock ApprovalRequestRepository approvalRequestRepository;

    ServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new ServiceRequestService(requestRepository, catalogItemRepository,
                categoryRepository, csatRepository, commentRepository,
                timelineRepository, appUserRepository, roleRepository, ticketLinkRepository, assetService,
                approvalGateService, approvalRequestRepository, new ObjectMapper());
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
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
        ServiceRequest r = new ServiceRequest("SRM-2026-0001", 10L, requesterId, null, null, "{}");
        if (status != RequestStatus.SUBMITTED) {
            r.changeStatus(status);
        }
        return r;
    }

    private ServiceCatalogItem catalog() {
        return new ServiceCatalogItem("Laptop", null, null, null, null, null,
                "{\"display\":\"form\",\"components\":[]}");
    }

    private ServiceCatalogItem catalogWithRequiredField() {
        return new ServiceCatalogItem("Laptop", null, null, null, null, null,
                "{\"display\":\"form\",\"components\":[{\"key\":\"reason\",\"label\":\"사유\",\"type\":\"textfield\","
                        + "\"input\":true,\"validate\":{\"required\":true}}]}");
    }

    private ServiceCatalogItem catalogWithAssigneeRole(Long roleId) {
        return new ServiceCatalogItem("Laptop", null, null, null, null, roleId,
                "{\"display\":\"form\",\"components\":[]}");
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
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogWithRequiredField()));

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING));
    }

    @Test
    void createSuccess() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalog()));
        when(requestRepository.countByTicketKeyStartingWith(any())).thenReturn(0L);

        var response = service.create(new CreateRequestRequest(10L, Map.of("reason", "고장")));

        assertThat(response.ticketKey()).startsWith("SRM-");
        assertThat(response.status()).isEqualTo("SUBMITTED");
    }

    // ---------- list ----------

    @Test
    void listScopeAllWithoutRoleForbidden() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.list("all", null, false, null, null, null, PageRequest.of(0, 20)))
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

    @Test
    void detailExposesLatestApprovalRequest() {
        login(2L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.ROUTED)));
        ApprovalRequest approvalRequest = new ApprovalRequest(TicketType.SERVICE_REQUEST, 1L, 100L, (short) 1);
        when(approvalRequestRepository.findTopByTicketTypeAndTicketIdOrderByIdDesc(TicketType.SERVICE_REQUEST, 1L))
                .thenReturn(Optional.of(approvalRequest));

        var response = service.detail(1L);

        assertThat(response.approval().status()).isEqualTo(ApprovalRequestStatus.IN_PROGRESS.name());
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
    void transitionSuccessValidated() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));

        var response = service.transition(1L, new StatusTransitionRequest(RequestStatus.VALIDATED, "검증 완료"));

        assertThat(response.status()).isEqualTo("VALIDATED");
    }

    @Test
    void transitionInFulfillmentGateBlockedPropagates409() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.ROUTED)));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 55L))
                .when(approvalGateService).checkGate(eq("SERVICE_REQUEST"), any(), anyLong(), eq(TicketType.SERVICE_REQUEST), eq(1L));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(55L);
                });
    }

    @Test
    void transitionInFulfillmentGatePassSuccess() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.ROUTED)));
        doNothing().when(approvalGateService).checkGate(any(), any(), anyLong(), any(), anyLong());

        var response = service.transition(1L, new StatusTransitionRequest(RequestStatus.IN_FULFILLMENT, null));

        assertThat(response.status()).isEqualTo("IN_FULFILLMENT");
    }

    @Test
    void transitionRoutedWithoutAssigneeThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.VALIDATED)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(RequestStatus.ROUTED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ASSIGNEE_REQUIRED_FOR_ROUTING));
    }

    @Test
    void transitionRoutedWithAssigneeSucceeds() {
        login(1L, "SERVICE_DESK_AGENT");
        ServiceRequest sr = request(2L, RequestStatus.VALIDATED);
        sr.assignTo(5L);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(sr));

        var response = service.transition(1L, new StatusTransitionRequest(RequestStatus.ROUTED, null));

        assertThat(response.status()).isEqualTo("ROUTED");
    }

    // ---------- assignee candidates (API-SRM-017) ----------

    @Test
    void assigneeCandidatesEmptyWhenRoleNotAssigned() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalog()));

        assertThat(service.assigneeCandidates(1L)).isEmpty();
    }

    @Test
    void assigneeCandidatesReturnsRoleHolders() {
        login(1L, "SERVICE_DESK_AGENT");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request(2L, RequestStatus.SUBMITTED)));
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogWithAssigneeRole(3L)));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(new Role("SERVICE_DESK_AGENT", "상담원", null)));
        AppUser candidate = new AppUser("agent@itsm.local", "hash", "상담원A", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(candidate, "id", 8L);
        when(appUserRepository.search(null, null, UserStatus.ACTIVE, "SERVICE_DESK_AGENT", Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(candidate)));

        var candidates = service.assigneeCandidates(1L);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).id()).isEqualTo(8L);
        assertThat(candidates.get(0).name()).isEqualTo("상담원A");
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
