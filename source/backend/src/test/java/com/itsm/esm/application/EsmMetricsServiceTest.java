package com.itsm.esm.application;

import com.itsm.auth.domain.Department;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.esm.domain.ChecklistStatus;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EsmMetricsServiceTest {

    @Mock EsmRequestRepository requestRepository;
    @Mock EsmChecklistRepository checklistRepository;
    @Mock TimelineEventRepository timelineRepository;

    EsmMetricsService service;

    @BeforeEach
    void setUp() {
        service = new EsmMetricsService(requestRepository, checklistRepository, timelineRepository);
        when(requestRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
        when(checklistRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
        when(timelineRepository.findByTicketTypeAndEventTypeAndTicketIdIn(any(), any(), any())).thenReturn(List.of());
    }

    @Test
    void computeReturnsZerosWhenNoData() {
        var response = service.compute(null, null, null);

        assertThat(response.requestCount()).isZero();
        assertThat(response.avgProcessingMinutes()).isZero();
        assertThat(response.onboardingCompletionRate()).isZero();
        assertThat(response.offboardingCompletionRate()).isZero();
    }

    @Test
    void computeFiltersRequestCountByDepartment() {
        EsmRequest hrRequest = new EsmRequest("ESM-2026-0001", 1L, 1L, Department.HR, null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(hrRequest, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(hrRequest, "createdAt", java.time.OffsetDateTime.now());
        EsmRequest legalRequest = new EsmRequest("ESM-2026-0002", 2L, 1L, Department.LEGAL, null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(legalRequest, "id", 2L);
        org.springframework.test.util.ReflectionTestUtils.setField(legalRequest, "createdAt", java.time.OffsetDateTime.now());
        when(requestRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(hrRequest, legalRequest));

        var response = service.compute(null, null, Department.HR);

        assertThat(response.requestCount()).isEqualTo(1);
    }

    @Test
    void computeCalculatesOnboardingCompletionRate() {
        EsmChecklist completed = new EsmChecklist(ChecklistTemplateType.ONBOARDING, "김철수");
        completed.changeStatus(ChecklistStatus.COMPLETED);
        EsmChecklist inProgress = new EsmChecklist(ChecklistTemplateType.ONBOARDING, "이영희");
        when(checklistRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(completed, inProgress));

        var response = service.compute(null, null, null);

        assertThat(response.onboardingCompletionRate()).isEqualTo(50.0);
        assertThat(response.offboardingCompletionRate()).isZero();
    }
}
