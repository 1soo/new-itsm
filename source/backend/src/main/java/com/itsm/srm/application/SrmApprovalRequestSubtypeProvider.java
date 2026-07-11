package com.itsm.srm.application;

import com.itsm.common.approval.application.ApprovalRequestSubtypeProvider;
import com.itsm.common.approval.application.RequestSubtypeOption;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 승인 프로세스 관리자 CRUD(API-AUTH-024)의 SERVICE_REQUEST 요청유형 후보(서비스 카탈로그 항목) 어댑터.
 */
@Component
public class SrmApprovalRequestSubtypeProvider implements ApprovalRequestSubtypeProvider {

    private final ServiceCatalogItemRepository catalogItemRepository;

    public SrmApprovalRequestSubtypeProvider(ServiceCatalogItemRepository catalogItemRepository) {
        this.catalogItemRepository = catalogItemRepository;
    }

    @Override
    public String supportedDomain() {
        return "SERVICE_REQUEST";
    }

    @Override
    public List<RequestSubtypeOption> subtypes() {
        return catalogItemRepository.search(null, null).stream()
                .map(item -> new RequestSubtypeOption(String.valueOf(item.getId()), item.getName()))
                .toList();
    }
}
