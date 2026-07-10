package com.itsm.esm.application;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.esm.application.dto.CreateHrCaseRequest;
import com.itsm.esm.application.dto.HrCaseStatusTransitionRequest;
import com.itsm.esm.domain.EsmHrCase;
import com.itsm.esm.domain.HrCaseStatus;
import com.itsm.esm.domain.repository.EsmHrCaseRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EsmHrCaseServiceTest {

    @Mock EsmHrCaseRepository hrCaseRepository;
    @Mock TimelineEventRepository timelineRepository;

    EsmHrCaseService service;

    @BeforeEach
    void setUp() {
        service = new EsmHrCaseService(hrCaseRepository, timelineRepository);
        when(hrCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
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

    private EsmHrCase hrCase(HrCaseStatus status) {
        EsmHrCase h = new EsmHrCase("제목", "설명", "대상자");
        if (status != HrCaseStatus.INTAKE) {
            h.changeStatus(status);
        }
        return h;
    }

    @Test
    void createForbiddenForNonHrCaseManager() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.create(new CreateHrCaseRequest("대상자", "제목", "설명")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void createAllowedForSystemAdmin() {
        login(1L, "SYSTEM_ADMIN");

        var response = service.create(new CreateHrCaseRequest("대상자", "제목", "설명"));

        assertThat(response.status()).isEqualTo("INTAKE");
    }

    @Test
    void createSuccess() {
        login(1L, "HR_CASE_MANAGER");

        var response = service.create(new CreateHrCaseRequest("대상자", "고충 상담", "내용"));

        assertThat(response.status()).isEqualTo("INTAKE");
    }

    @Test
    void listForbiddenForNonHrCaseManager() {
        login(1L, "DEPT_COORDINATOR");

        assertThatThrownBy(() -> service.list(null, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void listSuccess() {
        login(1L, "HR_CASE_MANAGER");
        when(hrCaseRepository.search(any(), any())).thenReturn(new PageImpl<>(List.of(hrCase(HrCaseStatus.INTAKE))));

        var response = service.list(null, PageRequest.of(0, 20));

        assertThat(response.content()).hasSize(1);
    }

    @Test
    void detailNotFoundThrows() {
        login(1L, "HR_CASE_MANAGER");
        when(hrCaseRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESM_HR_CASE_NOT_FOUND));
    }

    @Test
    void detailForbiddenForNonHrCaseManager() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void transitionSkippingSequenceThrows() {
        login(1L, "HR_CASE_MANAGER");
        when(hrCaseRepository.findById(1L)).thenReturn(Optional.of(hrCase(HrCaseStatus.INTAKE)));

        assertThatThrownBy(() -> service.transition(1L, new HrCaseStatusTransitionRequest(HrCaseStatus.INVESTIGATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionBackwardThrows() {
        login(1L, "HR_CASE_MANAGER");
        when(hrCaseRepository.findById(1L)).thenReturn(Optional.of(hrCase(HrCaseStatus.INVESTIGATION)));

        assertThatThrownBy(() -> service.transition(1L, new HrCaseStatusTransitionRequest(HrCaseStatus.DOCUMENTATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionSuccessSequential() {
        login(1L, "HR_CASE_MANAGER");
        when(hrCaseRepository.findById(1L)).thenReturn(Optional.of(hrCase(HrCaseStatus.INTAKE)));

        var response = service.transition(1L, new HrCaseStatusTransitionRequest(HrCaseStatus.DOCUMENTATION, "기록 시작"));

        assertThat(response.status()).isEqualTo("DOCUMENTATION");
    }
}
