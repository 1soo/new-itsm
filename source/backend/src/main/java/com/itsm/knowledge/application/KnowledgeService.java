package com.itsm.knowledge.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.knowledge.application.dto.ArticleCreatedResponse;
import com.itsm.knowledge.application.dto.ArticleDetailResponse;
import com.itsm.knowledge.application.dto.ArticleListResponse;
import com.itsm.knowledge.application.dto.ArticleSummaryResponse;
import com.itsm.knowledge.application.dto.CategoryResponse;
import com.itsm.knowledge.application.dto.CreateArticleRequest;
import com.itsm.knowledge.application.dto.FeedbackRequest;
import com.itsm.knowledge.application.dto.FeedbackResponse;
import com.itsm.knowledge.application.dto.KnowledgeMetricsResponse;
import com.itsm.knowledge.application.dto.LinkArticleRequest;
import com.itsm.knowledge.application.dto.LinkArticleResponse;
import com.itsm.knowledge.application.dto.PendingReviewResponse;
import com.itsm.knowledge.application.dto.ReviewRequest;
import com.itsm.knowledge.application.dto.ReviewResponse;
import com.itsm.knowledge.application.dto.StatusResponse;
import com.itsm.knowledge.application.dto.StatusTransitionRequest;
import com.itsm.knowledge.application.dto.UpdateArticleRequest;
import com.itsm.knowledge.domain.ArticleLabel;
import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.KnowledgeCategory;
import com.itsm.knowledge.domain.KnowledgeFeedback;
import com.itsm.knowledge.domain.KnowledgeLabel;
import com.itsm.knowledge.domain.KnowledgeReview;
import com.itsm.knowledge.domain.ReviewDecision;
import com.itsm.knowledge.domain.SearchLog;
import com.itsm.knowledge.domain.repository.ArticleLabelRepository;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import com.itsm.knowledge.domain.repository.KnowledgeCategoryRepository;
import com.itsm.knowledge.domain.repository.KnowledgeFeedbackRepository;
import com.itsm.knowledge.domain.repository.KnowledgeLabelRepository;
import com.itsm.knowledge.domain.repository.KnowledgeReviewRepository;
import com.itsm.knowledge.domain.repository.SearchLogRepository;
import com.itsm.problem.domain.repository.ProblemRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 지식(knowledge) 유스케이스: 검색/목록·상세·작성/수정/삭제·상태전이·검토승인/반려·검토대기·
 * 유용성평가·카테고리·KCS 티켓 연계·지표.
 * RBAC(knowledge_contributor.md/knowledge_gatekeeper.md): 작성/수정/삭제/검토요청/KCS연계는 CONTRIBUTOR 전용,
 * 검토승인·검토대기·지표는 GATEKEEPER 전용. 검색/열람/평가/카테고리는 인증된 모든 사용자에게 개방하되
 * 미게시 기사는 작성자 본인 또는 GATEKEEPER만 열람 가능.
 */
@Service
public class KnowledgeService {

    private static final String KC = "KNOWLEDGE_CONTRIBUTOR";
    private static final String KG = "KNOWLEDGE_GATEKEEPER";
    private static final int SUMMARY_LENGTH = 100;
    private static final int TOP_KEYWORD_LIMIT = 5;

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeLabelRepository labelRepository;
    private final ArticleLabelRepository articleLabelRepository;
    private final KnowledgeFeedbackRepository feedbackRepository;
    private final KnowledgeReviewRepository reviewRepository;
    private final SearchLogRepository searchLogRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final AppUserRepository appUserRepository;
    private final IncidentRepository incidentRepository;
    private final ProblemRepository problemRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public KnowledgeService(KnowledgeArticleRepository articleRepository,
                            KnowledgeCategoryRepository categoryRepository,
                            KnowledgeLabelRepository labelRepository,
                            ArticleLabelRepository articleLabelRepository,
                            KnowledgeFeedbackRepository feedbackRepository,
                            KnowledgeReviewRepository reviewRepository,
                            SearchLogRepository searchLogRepository,
                            TicketLinkRepository ticketLinkRepository,
                            AppUserRepository appUserRepository,
                            IncidentRepository incidentRepository,
                            ProblemRepository problemRepository,
                            ServiceRequestRepository serviceRequestRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
        this.articleLabelRepository = articleLabelRepository;
        this.feedbackRepository = feedbackRepository;
        this.reviewRepository = reviewRepository;
        this.searchLogRepository = searchLogRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.appUserRepository = appUserRepository;
        this.incidentRepository = incidentRepository;
        this.problemRepository = problemRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    // ---------- search/list (API-KM-001) ----------

    @Transactional
    public ArticleListResponse search(String keyword, Long categoryId, String label, ArticleStatus status,
                                      Pageable pageable) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        boolean isGatekeeper = principal.roles().contains(KG);
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Page<KnowledgeArticle> page = articleRepository.search(kw, categoryId, label, status,
                principal.userId(), isGatekeeper, pageable);

        boolean noResult = false;
        if (kw != null) {
            searchLogRepository.save(new SearchLog(kw, (int) page.getTotalElements(), principal.userId()));
            noResult = page.getTotalElements() == 0;
        }
        List<ArticleSummaryResponse> content = page.getContent().stream().map(this::toSummary).toList();
        return new ArticleListResponse(content, page.getNumber(), page.getSize(), page.getTotalElements(), noResult);
    }

    // ---------- detail (API-KM-002) ----------

    @Transactional
    public ArticleDetailResponse detail(Long id) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        KnowledgeArticle article = findArticle(id);
        boolean isGatekeeper = principal.roles().contains(KG);
        boolean isOwner = article.getAuthorId().equals(principal.userId());
        if (!article.isPublished() && !isGatekeeper && !isOwner) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        article.incrementView();
        articleRepository.save(article);
        return toDetail(article);
    }

    // ---------- create (API-KM-003) ----------

    @Transactional
    public ArticleCreatedResponse create(CreateArticleRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        requireRole(KC);
        validCategoryOrThrow(request.categoryId());
        KnowledgeArticle saved = articleRepository.save(
                new KnowledgeArticle(request.title(), request.body(), request.categoryId(), principal.userId()));
        applyLabels(saved.getId(), request.labels());
        return new ArticleCreatedResponse(saved.getId(), saved.getStatus().name());
    }

    // ---------- update (API-KM-004) ----------

    @Transactional
    public StatusResponse update(Long id, UpdateArticleRequest request) {
        requireRole(KC);
        KnowledgeArticle article = findArticle(id);
        if (request.title() != null && !StringUtils.hasText(request.title())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "제목은 비어 있을 수 없습니다.");
        }
        if (request.body() != null && !StringUtils.hasText(request.body())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "본문은 비어 있을 수 없습니다.");
        }
        validCategoryOrThrow(request.categoryId());
        article.updateContent(request.title(), request.body(), request.categoryId());
        articleRepository.save(article);
        if (request.labels() != null) {
            articleLabelRepository.deleteByArticleId(id);
            applyLabels(id, request.labels());
        }
        return new StatusResponse(id, article.getStatus().name());
    }

    // ---------- delete (API-KM-005) ----------

    @Transactional
    public void delete(Long id) {
        requireRole(KC);
        KnowledgeArticle article = findArticle(id);
        article.markDeleted();
        articleRepository.save(article);
    }

    // ---------- status transition (API-KM-006) ----------

    @Transactional
    public StatusResponse transition(Long id, StatusTransitionRequest request) {
        requireRole(KC);
        KnowledgeArticle article = findArticle(id);
        if (article.getStatus() != ArticleStatus.DRAFT || request.targetStatus() != ArticleStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        article.changeStatus(ArticleStatus.IN_REVIEW);
        articleRepository.save(article);
        return new StatusResponse(id, article.getStatus().name());
    }

    // ---------- review (API-KM-007) ----------

    @Transactional
    public ReviewResponse review(Long id, ReviewRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        requireRole(KG);
        KnowledgeArticle article = findArticle(id);
        if (article.getStatus() != ArticleStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (request.decision() == ReviewDecision.REJECT && !StringUtils.hasText(request.reason())) {
            throw new BusinessException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        if (request.decision() == ReviewDecision.APPROVE) {
            article.publish();
        } else {
            article.changeStatus(ArticleStatus.DRAFT);
        }
        articleRepository.save(article);
        reviewRepository.save(new KnowledgeReview(id, principal.userId(), request.decision(), request.reason()));
        return new ReviewResponse(id, article.getStatus().name());
    }

    // ---------- pending reviews (API-KM-008) ----------

    @Transactional(readOnly = true)
    public List<PendingReviewResponse> pendingReviews() {
        requireRole(KG);
        return articleRepository.search(null, null, null, ArticleStatus.IN_REVIEW, null, true, Pageable.unpaged())
                .stream()
                .map(a -> new PendingReviewResponse(a.getId(), a.getTitle(), userName(a.getAuthorId()), a.getUpdatedAt()))
                .toList();
    }

    // ---------- feedback (API-KM-009) ----------

    @Transactional
    public FeedbackResponse feedback(Long id, FeedbackRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        KnowledgeArticle article = findArticle(id);
        if (!article.isPublished()) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_PUBLISHED);
        }
        article.recordFeedback(request.helpful());
        articleRepository.save(article);
        feedbackRepository.save(new KnowledgeFeedback(id, principal.userId(), request.helpful(), request.comment()));
        return new FeedbackResponse(article.getHelpfulCount(), article.getNotHelpfulCount());
    }

    // ---------- categories (API-KM-010) ----------

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categoryRepository.findActive().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    // ---------- KCS ticket link (API-KM-011) ----------

    @Transactional
    public LinkArticleResponse linkArticle(LinkArticleRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        requireRole(KC);
        if (!ticketExists(request.ticketType(), request.ticketId())) {
            throw new BusinessException(ErrorCode.LINK_TARGET_NOT_FOUND);
        }
        Long articleId;
        if (request.articleId() != null) {
            findArticle(request.articleId());
            articleId = request.articleId();
        } else if (request.newArticle() != null) {
            if (!StringUtils.hasText(request.newArticle().title()) || !StringUtils.hasText(request.newArticle().body())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "신규 기사는 제목·본문이 필수입니다.");
            }
            KnowledgeArticle created = articleRepository.save(new KnowledgeArticle(
                    request.newArticle().title(), request.newArticle().body(), null, principal.userId()));
            articleId = created.getId();
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "articleId 또는 newArticle 중 하나가 필요합니다.");
        }
        saveLinkOnce(request.ticketType(), request.ticketId(), TicketType.KNOWLEDGE, articleId);
        return new LinkArticleResponse(articleId, request.ticketId());
    }

    private boolean ticketExists(TicketType ticketType, Long ticketId) {
        return switch (ticketType) {
            case SERVICE_REQUEST -> serviceRequestRepository.findById(ticketId).filter(r -> !r.isDeleted()).isPresent();
            case INCIDENT -> incidentRepository.findById(ticketId).filter(i -> !i.isDeleted()).isPresent();
            case PROBLEM -> problemRepository.findById(ticketId).filter(p -> !p.isDeleted()).isPresent();
            default -> false;
        };
    }

    // ---------- metrics (API-KM-012) ----------

    @Transactional(readOnly = true)
    public KnowledgeMetricsResponse metrics(OffsetDateTime from, OffsetDateTime to) {
        requireRole(KG);
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);

        long usageCount = searchLogRepository.countBySearchedAtBetween(fromV, toV);
        long noResultSearchCount = searchLogRepository.countByResultCountAndSearchedAtBetween(0, fromV, toV);

        List<KnowledgeArticle> published = articleRepository.findByStatusAndCreatedAtBetween(
                ArticleStatus.PUBLISHED, fromV, toV);
        long totalHelpful = published.stream().mapToLong(KnowledgeArticle::getHelpfulCount).sum();
        long totalNotHelpful = published.stream().mapToLong(KnowledgeArticle::getNotHelpfulCount).sum();
        double helpfulRate = (totalHelpful + totalNotHelpful) == 0 ? 0
                : (double) totalHelpful / (totalHelpful + totalNotHelpful) * 100.0;

        // deflectionRate = 게시 기사 중 열람(view_count>0)되었으나 티켓(KCS)에 연계되지 않은 기사 비율(%).
        // 연계 없이도 스스로 해결(셀프서비스)된 것으로 간주하는 근사치.
        List<KnowledgeArticle> viewed = published.stream().filter(a -> a.getViewCount() > 0).toList();
        double deflectionRate;
        if (viewed.isEmpty()) {
            deflectionRate = 0;
        } else {
            long notLinked = viewed.stream()
                    .filter(a -> !ticketLinkRepository.existsByTargetTypeAndTargetId(TicketType.KNOWLEDGE, a.getId()))
                    .count();
            deflectionRate = (double) notLinked / viewed.size() * 100.0;
        }

        List<SearchLog> noResultLogs = searchLogRepository.findByResultCountAndSearchedAtBetween(0, fromV, toV);
        List<String> topKeywords = noResultLogs.stream()
                .collect(Collectors.groupingBy(SearchLog::getKeyword, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_KEYWORD_LIMIT)
                .map(Map.Entry::getKey)
                .toList();

        return new KnowledgeMetricsResponse(usageCount, noResultSearchCount,
                round(helpfulRate), round(deflectionRate), topKeywords);
    }

    // ---------- helpers ----------

    private KnowledgeArticle findArticle(Long id) {
        return articleRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
    }

    private void requireRole(String... roles) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        for (String role : roles) {
            if (principal.roles().contains(role)) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private void validCategoryOrThrow(Long categoryId) {
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .filter(c -> !c.isDeleted())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 카테고리입니다."));
        }
    }

    private void applyLabels(Long articleId, List<String> labelNames) {
        if (labelNames == null) {
            return;
        }
        for (String name : labelNames) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            KnowledgeLabel label = labelRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> labelRepository.save(new KnowledgeLabel(name)));
            articleLabelRepository.save(new ArticleLabel(articleId, label.getId()));
        }
    }

    private void saveLinkOnce(TicketType sourceType, Long sourceId, TicketType targetType, Long targetId) {
        if (!ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                sourceType, sourceId, targetType, targetId)) {
            ticketLinkRepository.save(new TicketLink(sourceType, sourceId, targetType, targetId, "KCS"));
        }
    }

    private ArticleSummaryResponse toSummary(KnowledgeArticle a) {
        return new ArticleSummaryResponse(a.getId(), a.getTitle(), summarize(a.getBody()), a.getStatus().name(),
                categoryName(a.getCategoryId()), helpfulRateOf(a));
    }

    private ArticleDetailResponse toDetail(KnowledgeArticle a) {
        List<String> labels = articleLabelRepository.findByArticleId(a.getId()).stream()
                .map(al -> labelRepository.findById(al.getLabelId()).map(KnowledgeLabel::getName).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        return new ArticleDetailResponse(a.getId(), a.getTitle(), a.getBody(), a.getStatus().name(),
                categoryName(a.getCategoryId()), labels, a.getHelpfulCount(), a.getNotHelpfulCount());
    }

    private String summarize(String body) {
        if (body == null) {
            return null;
        }
        return body.length() > SUMMARY_LENGTH ? body.substring(0, SUMMARY_LENGTH) + "..." : body;
    }

    private double helpfulRateOf(KnowledgeArticle a) {
        int total = a.getHelpfulCount() + a.getNotHelpfulCount();
        return total == 0 ? 0 : round((double) a.getHelpfulCount() / total * 100.0);
    }

    private String categoryName(Long categoryId) {
        return categoryId == null ? null
                : categoryRepository.findById(categoryId).map(KnowledgeCategory::getName).orElse(null);
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
