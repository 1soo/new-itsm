package com.itsm.knowledge.application;

import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.knowledge.application.dto.CreateArticleRequest;
import com.itsm.knowledge.application.dto.FeedbackRequest;
import com.itsm.knowledge.application.dto.LinkArticleRequest;
import com.itsm.knowledge.application.dto.StatusTransitionRequest;
import com.itsm.knowledge.application.dto.UpdateArticleRequest;
import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.KnowledgeCategory;
import com.itsm.knowledge.domain.repository.ArticleLabelRepository;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import com.itsm.knowledge.domain.repository.KnowledgeCategoryRepository;
import com.itsm.knowledge.domain.repository.KnowledgeFeedbackRepository;
import com.itsm.knowledge.domain.repository.KnowledgeLabelRepository;
import com.itsm.knowledge.domain.repository.SearchLogRepository;
import com.itsm.problem.domain.repository.ProblemRepository;
import com.itsm.srm.domain.ServiceRequest;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeServiceTest {

    @Mock KnowledgeArticleRepository articleRepository;
    @Mock KnowledgeCategoryRepository categoryRepository;
    @Mock KnowledgeLabelRepository labelRepository;
    @Mock ArticleLabelRepository articleLabelRepository;
    @Mock KnowledgeFeedbackRepository feedbackRepository;
    @Mock SearchLogRepository searchLogRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock IncidentRepository incidentRepository;
    @Mock ProblemRepository problemRepository;
    @Mock ServiceRequestRepository serviceRequestRepository;
    @Mock ApprovalGateService approvalGateService;
    @Mock ApprovalRequestRepository approvalRequestRepository;

    KnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeService(articleRepository, categoryRepository, labelRepository, articleLabelRepository,
                feedbackRepository, searchLogRepository, ticketLinkRepository,
                incidentRepository, problemRepository, serviceRequestRepository,
                approvalGateService, approvalRequestRepository);
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleLabelRepository.findByArticleId(any())).thenReturn(List.of());
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

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    private KnowledgeArticle article(ArticleStatus status, Long authorId) {
        KnowledgeArticle a = new KnowledgeArticle("제목", "본문", null, authorId);
        if (status != ArticleStatus.DRAFT) {
            a.changeStatus(status);
        }
        return a;
    }

    // ---------- create ----------

    @Test
    void createForbiddenForNonContributor() {
        login(1L, "SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> service.create(new CreateArticleRequest("제목", "본문", null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void createInvalidCategoryRejected() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(new CreateArticleRequest("제목", "본문", 9L, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createSuccessWithLabels() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(labelRepository.findByNameIgnoreCase("네트워크")).thenReturn(Optional.empty());
        when(labelRepository.save(any())).thenAnswer(inv -> {
            var label = inv.getArgument(0, com.itsm.knowledge.domain.KnowledgeLabel.class);
            return label;
        });
        var response = service.create(new CreateArticleRequest("제목", "본문", null, List.of("네트워크")));
        assertThat(response.status()).isEqualTo("DRAFT");
        verify(articleLabelRepository).save(any());
    }

    // ---------- search ----------

    @Test
    void searchRecordsSearchLogWithKeyword() {
        login(1L, "END_USER");
        Pageable pageable = PageRequest.of(0, 20);
        when(articleRepository.search("결제", null, null, null, 1L, false, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        var response = service.search("결제", null, null, null, pageable);
        assertThat(response.noResult()).isTrue();
        verify(searchLogRepository).save(any());
    }

    @Test
    void searchWithoutKeywordSkipsSearchLog() {
        login(1L, "END_USER");
        Pageable pageable = PageRequest.of(0, 20);
        when(articleRepository.search(null, null, null, null, 1L, false, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        service.search(null, null, null, null, pageable);
        verify(searchLogRepository, times(0)).save(any());
    }

    // ---------- detail ----------

    @Test
    void detailNotFoundThrows() {
        login(1L, "END_USER");
        when(articleRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ARTICLE_NOT_FOUND));
    }

    @Test
    void detailUnpublishedRejectedForEndUser() {
        login(1L, "END_USER");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 2L)));
        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void detailUnpublishedAllowedForGatekeeper() {
        login(1L, "KNOWLEDGE_GATEKEEPER");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 2L)));
        var response = service.detail(1L);
        assertThat(response.status()).isEqualTo("DRAFT");
    }

    @Test
    void detailUnpublishedAllowedForOwner() {
        login(2L, "KNOWLEDGE_CONTRIBUTOR");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 2L)));
        var response = service.detail(1L);
        assertThat(response.status()).isEqualTo("DRAFT");
    }

    @Test
    void detailPublishedAllowedForAnyone() {
        login(99L, "END_USER");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED, 2L)));
        var response = service.detail(1L);
        assertThat(response.status()).isEqualTo("PUBLISHED");
    }

    @Test
    void detailExposesLatestApprovalRequest() {
        login(99L, "END_USER");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED, 2L)));
        var approvalRequest = new com.itsm.common.approval.domain.ApprovalRequest(
                TicketType.KNOWLEDGE, 1L, 100L, (short) 1);
        approvalRequest.approve();
        when(approvalRequestRepository.findTopByTicketTypeAndTicketIdOrderByIdDesc(eq(TicketType.KNOWLEDGE), any()))
                .thenReturn(Optional.of(approvalRequest));
        var response = service.detail(1L);
        assertThat(response.approval().status()).isEqualTo("APPROVED");
    }

    // ---------- update ----------

    @Test
    void updateBlankTitleRejected() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 1L)));
        assertThatThrownBy(() -> service.update(1L, new UpdateArticleRequest("  ", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    // ---------- delete ----------

    @Test
    void deleteForbiddenForNonContributor() {
        login(1L, "KNOWLEDGE_GATEKEEPER");
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- status transition (게이트) ----------

    @Test
    void transitionWithoutMatchingRulePublishesImmediately() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 1L)));
        when(approvalGateService.evaluateAndCreateIfNeeded(eq("KNOWLEDGE"), any(), any(), eq(TicketType.KNOWLEDGE), eq(1L)))
                .thenReturn(new ApprovalGateService.GateDecision(true, null));

        var response = service.transition(1L, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));

        assertThat(response.status()).isEqualTo("PUBLISHED");
        assertThat(response.approvalRequestId()).isNull();
    }

    @Test
    void transitionWithMatchingRuleStaysInReview() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 1L)));
        when(approvalGateService.evaluateAndCreateIfNeeded(eq("KNOWLEDGE"), any(), any(), eq(TicketType.KNOWLEDGE), eq(1L)))
                .thenReturn(new ApprovalGateService.GateDecision(false, 55L));

        var response = service.transition(1L, new StatusTransitionRequest(ArticleStatus.IN_REVIEW));

        assertThat(response.status()).isEqualTo("IN_REVIEW");
        assertThat(response.approvalRequestId()).isEqualTo(55L);
    }

    @Test
    void transitionFromPublishedRejected() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED, 1L)));
        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(ArticleStatus.IN_REVIEW)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    // ---------- feedback ----------

    @Test
    void feedbackOnUnpublishedRejected() {
        login(1L, "END_USER");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.DRAFT, 2L)));
        assertThatThrownBy(() -> service.feedback(1L, new FeedbackRequest(true, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ARTICLE_NOT_PUBLISHED));
    }

    @Test
    void feedbackIncrementsCounts() {
        login(1L, "END_USER");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED, 2L)));
        var response = service.feedback(1L, new FeedbackRequest(true, "도움됐어요"));
        assertThat(response.helpful()).isEqualTo(1);
        assertThat(response.notHelpful()).isEqualTo(0);
        verify(feedbackRepository).save(any());
    }

    // ---------- categories ----------

    @Test
    void categoriesReturnsActiveList() {
        when(categoryRepository.findActive()).thenReturn(List.of(new KnowledgeCategory("네트워크")));
        assertThat(service.categories()).hasSize(1);
    }

    // ---------- KCS link ----------

    @Test
    void linkArticleForbiddenForNonContributor() {
        login(1L, "END_USER");
        assertThatThrownBy(() -> service.linkArticle(new LinkArticleRequest(TicketType.INCIDENT, 1L, 1L, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void linkArticleNonExistentTicketRejected() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        when(incidentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.linkArticle(new LinkArticleRequest(TicketType.INCIDENT, 5L, 1L, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void linkExistingArticleSuccess() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        Incident inc = new Incident("INC-2026-0001", "장애", "설명", Severity.SEV1, "svc", "prod");
        when(incidentRepository.findById(5L)).thenReturn(Optional.of(inc));
        when(articleRepository.findById(9L)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED, 1L)));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);
        var response = service.linkArticle(new LinkArticleRequest(TicketType.INCIDENT, 5L, 9L, null));
        assertThat(response.articleId()).isEqualTo(9L);
        assertThat(response.ticketId()).isEqualTo(5L);
        verify(ticketLinkRepository).save(any());
    }

    @Test
    void linkNewArticleCreatesDraft() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        Incident inc = new Incident("INC-2026-0001", "장애", "설명", Severity.SEV1, "svc", "prod");
        when(incidentRepository.findById(5L)).thenReturn(Optional.of(inc));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);
        var response = service.linkArticle(new LinkArticleRequest(TicketType.INCIDENT, 5L, null,
                new LinkArticleRequest.NewArticleDto("신규 기사", "본문 내용")));
        assertThat(response.ticketId()).isEqualTo(5L);
    }

    @Test
    void linkArticleServiceRequestTicketType() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        ServiceRequest sr = mock(ServiceRequest.class);
        when(sr.isDeleted()).thenReturn(false);
        when(serviceRequestRepository.findById(3L)).thenReturn(Optional.of(sr));
        when(articleRepository.findById(9L)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED, 1L)));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);
        var response = service.linkArticle(new LinkArticleRequest(TicketType.SERVICE_REQUEST, 3L, 9L, null));
        assertThat(response.ticketId()).isEqualTo(3L);
    }

    // ---------- metrics ----------

    @Test
    void metricsForbiddenForNonGatekeeper() {
        login(1L, "KNOWLEDGE_CONTRIBUTOR");
        assertThatThrownBy(() -> service.metrics(null, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void metricsEmptyReturnsZero() {
        login(1L, "KNOWLEDGE_GATEKEEPER");
        when(searchLogRepository.countBySearchedAtBetween(any(), any())).thenReturn(0L);
        when(searchLogRepository.countByResultCountAndSearchedAtBetween(eq(0), any(), any())).thenReturn(0L);
        when(articleRepository.findByStatusAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());
        when(searchLogRepository.findByResultCountAndSearchedAtBetween(eq(0), any(), any())).thenReturn(List.of());
        var metrics = service.metrics(null, null);
        assertThat(metrics.usageCount()).isEqualTo(0);
        assertThat(metrics.helpfulRate()).isEqualTo(0);
        assertThat(metrics.deflectionRate()).isEqualTo(0);
    }
}
