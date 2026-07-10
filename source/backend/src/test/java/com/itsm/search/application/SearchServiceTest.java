package com.itsm.search.application;

import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import com.itsm.problem.domain.Level;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.repository.ProblemRepository;
import com.itsm.search.application.dto.SearchResultResponse;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.ServiceRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchServiceTest {

    @Mock KnowledgeArticleRepository articleRepository;
    @Mock ServiceRequestRepository serviceRequestRepository;
    @Mock ServiceCatalogItemRepository catalogItemRepository;
    @Mock IncidentRepository incidentRepository;
    @Mock ProblemRepository problemRepository;
    @Mock ChangeRequestRepository changeRequestRepository;

    SearchService service;

    @BeforeEach
    void setUp() {
        service = new SearchService(articleRepository, serviceRequestRepository, catalogItemRepository,
                incidentRepository, problemRepository, changeRequestRepository);
        when(articleRepository.search(any(), any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(serviceRequestRepository.searchByKeyword(any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(incidentRepository.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(problemRepository.searchByKeyword(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(changeRequestRepository.searchByKeyword(any(), any())).thenReturn(new PageImpl<>(List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, String... roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, "u" + userId + "@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private KnowledgeArticle article(String title, Long authorId, OffsetDateTime updatedAt) {
        KnowledgeArticle a = new KnowledgeArticle(title, "본문 내용", null, authorId);
        a.changeStatus(ArticleStatus.PUBLISHED);
        ReflectionTestUtils.setField(a, "id", 1L);
        ReflectionTestUtils.setField(a, "updatedAt", updatedAt);
        return a;
    }

    private ServiceRequest serviceRequest(Long requesterId, OffsetDateTime updatedAt) {
        ServiceRequest r = new ServiceRequest("SRM-2026-0001", 10L, requesterId, 1L, null, null);
        ReflectionTestUtils.setField(r, "id", 2L);
        ReflectionTestUtils.setField(r, "updatedAt", updatedAt);
        return r;
    }

    private Incident incident(OffsetDateTime updatedAt) {
        Incident i = new Incident("INC-2026-0001", "요약", "설명", Severity.SEV2, null, null);
        ReflectionTestUtils.setField(i, "id", 3L);
        ReflectionTestUtils.setField(i, "updatedAt", updatedAt);
        return i;
    }

    private Problem problem(OffsetDateTime updatedAt) {
        Problem p = new Problem("PRB-2026-0001", "요약", "설명", ProblemOrigin.REACTIVE, null, Level.HIGH, Level.HIGH, null);
        ReflectionTestUtils.setField(p, "id", 4L);
        ReflectionTestUtils.setField(p, "updatedAt", updatedAt);
        return p;
    }

    private ChangeRequest change(OffsetDateTime updatedAt) {
        ChangeRequest c = new ChangeRequest("CHG-2026-0001", "요약", "설명", ChangeType.NORMAL, null, null, null, null, null, null);
        ReflectionTestUtils.setField(c, "id", 5L);
        ReflectionTestUtils.setField(c, "updatedAt", updatedAt);
        return c;
    }

    // ---------- domain access (1차 필터) ----------

    @Test
    void endUserSeesOnlyKnowledgeAndOwnServiceRequests() {
        login(1L, "END_USER");
        when(articleRepository.search(any(), isNull(), isNull(), isNull(), eq(1L), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(article("공지", 9L, OffsetDateTime.now()))));
        when(serviceRequestRepository.searchByKeyword(eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(serviceRequest(1L, OffsetDateTime.now()))));
        when(catalogItemRepository.findById(10L))
                .thenReturn(Optional.of(new ServiceCatalogItem("노트북 지급", null, null, false, null, 1L, null, null)));

        var response = service.search("키워드", PageRequest.of(0, 20));

        assertThat(response.content()).extracting(SearchResultResponse::domain)
                .containsExactlyInAnyOrder("KNOWLEDGE", "SERVICE_REQUEST");
        verify(serviceRequestRepository).searchByKeyword(eq(1L), any(), any());
        verify(incidentRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
        verify(problemRepository, never()).searchByKeyword(any(), any());
        verify(changeRequestRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void serviceDeskAgentSeesAllRequestsAndIncidentsNotProblemOrChange() {
        login(2L, "SERVICE_DESK_AGENT");
        when(serviceRequestRepository.searchByKeyword(isNull(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(serviceRequest(9L, OffsetDateTime.now()))));
        when(catalogItemRepository.findById(any())).thenReturn(Optional.empty());
        when(incidentRepository.search(isNull(), isNull(), isNull(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(incident(OffsetDateTime.now()))));

        var response = service.search("장애", PageRequest.of(0, 20));

        assertThat(response.content()).extracting(SearchResultResponse::domain)
                .containsExactlyInAnyOrder("SERVICE_REQUEST", "INCIDENT");
        verify(articleRepository).search(any(), any(), any(), any(), eq(2L), eq(false), any());
        verify(serviceRequestRepository).searchByKeyword(isNull(), any(), any());
        verify(problemRepository, never()).searchByKeyword(any(), any());
        verify(changeRequestRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void knowledgeGatekeeperSearchesAllStatuses() {
        login(3L, "KNOWLEDGE_GATEKEEPER");

        service.search("키워드", PageRequest.of(0, 20));

        verify(articleRepository).search(any(), isNull(), isNull(), isNull(), eq(3L), eq(true), any());
        verify(serviceRequestRepository, never()).searchByKeyword(any(), any(), any());
        verify(incidentRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void problemManagerSeesOnlyProblemDomain() {
        login(4L, "PROBLEM_MANAGER");
        when(problemRepository.searchByKeyword(any(), any()))
                .thenReturn(new PageImpl<>(List.of(problem(OffsetDateTime.now()))));

        var response = service.search("문제", PageRequest.of(0, 20));

        assertThat(response.content()).extracting(SearchResultResponse::domain).containsExactly("PROBLEM");
        verify(articleRepository, never()).search(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(serviceRequestRepository, never()).searchByKeyword(any(), any(), any());
        verify(incidentRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
        verify(changeRequestRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void changeManagerSeesOnlyChangeDomain() {
        login(5L, "CHANGE_MANAGER");
        when(changeRequestRepository.searchByKeyword(any(), any()))
                .thenReturn(new PageImpl<>(List.of(change(OffsetDateTime.now()))));

        var response = service.search("변경", PageRequest.of(0, 20));

        assertThat(response.content()).extracting(SearchResultResponse::domain).containsExactly("CHANGE");
        verify(problemRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void roleWithNoAccessibleDomainReturnsEmptyResult() {
        login(6L, "PROCESS_OWNER");

        var response = service.search("키워드", PageRequest.of(0, 20));

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        verify(articleRepository, never()).search(any(), any(), any(), any(), any(), anyBoolean(), any());
        verify(serviceRequestRepository, never()).searchByKeyword(any(), any(), any());
        verify(incidentRepository, never()).search(any(), any(), any(), any(), any(), any(), any());
        verify(problemRepository, never()).searchByKeyword(any(), any());
        verify(changeRequestRepository, never()).searchByKeyword(any(), any());
    }

    // ---------- 병합 정렬·페이지네이션 ----------

    @Test
    void mergesAndSortsResultsByUpdatedAtDescendingAcrossDomains() {
        login(7L, "SERVICE_DESK_AGENT");
        OffsetDateTime oldest = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        OffsetDateTime middle = OffsetDateTime.parse("2026-03-01T00:00:00Z");
        OffsetDateTime newest = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        when(articleRepository.search(any(), any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(article("지식", 9L, middle))));
        when(serviceRequestRepository.searchByKeyword(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(serviceRequest(9L, oldest))));
        when(catalogItemRepository.findById(any())).thenReturn(Optional.empty());
        when(incidentRepository.search(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(incident(newest))));

        var response = service.search("키워드", PageRequest.of(0, 20));

        assertThat(response.content()).extracting(SearchResultResponse::domain)
                .containsExactly("INCIDENT", "KNOWLEDGE", "SERVICE_REQUEST");
        assertThat(response.totalElements()).isEqualTo(3);
    }

    @Test
    void paginatesMergedResults() {
        login(4L, "PROBLEM_MANAGER");
        OffsetDateTime t1 = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        OffsetDateTime t2 = OffsetDateTime.parse("2026-02-01T00:00:00Z");
        Problem p1 = problem(t1);
        Problem p2 = problem(t2);
        ReflectionTestUtils.setField(p2, "id", 41L);
        when(problemRepository.searchByKeyword(any(), any())).thenReturn(new PageImpl<>(List.of(p1, p2)));

        var response = service.search("문제", PageRequest.of(1, 1));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).updatedAt()).isEqualTo(t1);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.totalElements()).isEqualTo(2);
    }

    @Test
    void knowledgeResultMapsDomainKeyAndUrl() {
        login(8L, "KNOWLEDGE_CONTRIBUTOR");
        when(articleRepository.search(any(), any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(article("사용 가이드", 8L, OffsetDateTime.now()))));

        var response = service.search("가이드", PageRequest.of(0, 20));

        assertThat(response.content()).extracting(SearchResultResponse::domain, SearchResultResponse::key,
                        SearchResultResponse::title, SearchResultResponse::url)
                .containsExactly(tuple("KNOWLEDGE", "KB-1", "사용 가이드", "/knowledge/1"));
    }
}
