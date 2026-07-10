package com.itsm.search.application;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.repository.ProblemRepository;
import com.itsm.search.application.dto.SearchResultResponse;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 통합 검색(API-SEARCH-001) 유스케이스: 지식+티켓(SRM/INC/PRB/CHG) 교차 도메인 검색.
 * RBAC(search.md 0.1): 역할의 도메인별 목록 API 접근 가능 여부로 1차 필터링(접근 불가 도메인은 조용히 스킵),
 * 접근 가능 도메인 내에서는 해당 도메인 기존 목록 API와 동일한 행 단위 스코프(KNOWLEDGE=API-KM-001,
 * SERVICE_REQUEST=API-SRM-007)를 적용한다. 도메인별 상한 조회 후 인메모리 병합·updatedAt 내림차순 정렬·페이지네이션.
 */
@Service
public class SearchService {

    private static final String END_USER = "END_USER";
    private static final String AGENT = "SERVICE_DESK_AGENT";
    private static final String KC = "KNOWLEDGE_CONTRIBUTOR";
    private static final String KG = "KNOWLEDGE_GATEKEEPER";
    private static final String IM = "INCIDENT_MANAGER";
    private static final String PM = "PROBLEM_MANAGER";
    private static final String CM = "CHANGE_MANAGER";

    private static final int DOMAIN_FETCH_LIMIT = 100;
    private static final int SNIPPET_LENGTH = 100;
    private static final OffsetDateTime EPOCH = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    private final KnowledgeArticleRepository articleRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceCatalogItemRepository catalogItemRepository;
    private final IncidentRepository incidentRepository;
    private final ProblemRepository problemRepository;
    private final ChangeRequestRepository changeRequestRepository;

    public SearchService(KnowledgeArticleRepository articleRepository,
                         ServiceRequestRepository serviceRequestRepository,
                         ServiceCatalogItemRepository catalogItemRepository,
                         IncidentRepository incidentRepository,
                         ProblemRepository problemRepository,
                         ChangeRequestRepository changeRequestRepository) {
        this.articleRepository = articleRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.incidentRepository = incidentRepository;
        this.problemRepository = problemRepository;
        this.changeRequestRepository = changeRequestRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SearchResultResponse> search(String keyword, Pageable pageable) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        List<String> roles = principal.roles();
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Pageable domainPage = PageRequest.of(0, DOMAIN_FETCH_LIMIT, Sort.by(Sort.Direction.DESC, "updatedAt"));

        List<SearchResultResponse> merged = new ArrayList<>();

        if (hasAny(roles, END_USER, AGENT, KC, KG)) {
            boolean isGatekeeper = roles.contains(KG);
            articleRepository.search(kw, null, null, null, principal.userId(), isGatekeeper, domainPage)
                    .forEach(a -> merged.add(fromArticle(a)));
        }
        if (hasAny(roles, END_USER, AGENT)) {
            Long requesterFilter = roles.contains(AGENT) ? null : principal.userId();
            serviceRequestRepository.searchByKeyword(requesterFilter, kw, domainPage)
                    .forEach(r -> merged.add(fromServiceRequest(r)));
        }
        if (hasAny(roles, AGENT, IM)) {
            incidentRepository.search(null, null, null, kw, EPOCH, farFuture(), domainPage)
                    .forEach(i -> merged.add(fromIncident(i)));
        }
        if (hasAny(roles, PM)) {
            problemRepository.searchByKeyword(kw, domainPage).forEach(p -> merged.add(fromProblem(p)));
        }
        if (hasAny(roles, CM)) {
            changeRequestRepository.searchByKeyword(kw, domainPage).forEach(c -> merged.add(fromChange(c)));
        }

        merged.sort(Comparator.comparing(SearchResultResponse::updatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int from = Math.min((int) pageable.getOffset(), merged.size());
        int to = Math.min(from + pageable.getPageSize(), merged.size());
        return new PageResponse<>(merged.subList(from, to), pageable.getPageNumber(), pageable.getPageSize(), merged.size());
    }

    private boolean hasAny(List<String> roles, String... candidates) {
        for (String candidate : candidates) {
            if (roles.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private OffsetDateTime farFuture() {
        return OffsetDateTime.now().plusYears(100);
    }

    private SearchResultResponse fromArticle(KnowledgeArticle a) {
        return new SearchResultResponse("KNOWLEDGE", "KB-" + a.getId(), a.getTitle(), a.getStatus().name(),
                summarize(a.getBody()), a.getUpdatedAt(), "/knowledge/" + a.getId());
    }

    private SearchResultResponse fromServiceRequest(ServiceRequest r) {
        return new SearchResultResponse("SERVICE_REQUEST", r.getTicketKey(), catalogName(r.getCatalogItemId()),
                r.getStatus().name(), null, r.getUpdatedAt(), "/service-requests/" + r.getId());
    }

    private SearchResultResponse fromIncident(Incident i) {
        return new SearchResultResponse("INCIDENT", i.getTicketKey(), i.getSummary(), i.getStatus().name(),
                summarize(i.getDescription()), i.getUpdatedAt(), "/incidents/" + i.getId());
    }

    private SearchResultResponse fromProblem(Problem p) {
        return new SearchResultResponse("PROBLEM", p.getTicketKey(), p.getSummary(), p.getStatus().name(),
                summarize(p.getDescription()), p.getUpdatedAt(), "/problems/" + p.getId());
    }

    private SearchResultResponse fromChange(ChangeRequest c) {
        return new SearchResultResponse("CHANGE", c.getTicketKey(), c.getSummary(), c.getStatus().name(),
                summarize(c.getDescription()), c.getUpdatedAt(), "/changes/" + c.getId());
    }

    private String catalogName(Long catalogItemId) {
        return catalogItemRepository.findById(catalogItemId).map(ServiceCatalogItem::getName).orElse(null);
    }

    private String summarize(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > SNIPPET_LENGTH ? text.substring(0, SNIPPET_LENGTH) + "..." : text;
    }
}
