package com.itsm.common.approval.application;

import java.util.List;

/**
 * 승인 프로세스 관리자 CRUD(API-AUTH-024)가 도메인별 요청유형 후보 목록을 조회하기 위한 확장 포인트.
 * 하위유형 개념이 있는 도메인(SERVICE_REQUEST 등)만 구현 빈을 등록한다(common 모듈이 개별 도메인 저장소에
 * 의존하지 않도록 하는 확장 포인트, {@link ApprovalTicketSummaryProvider}와 동일한 목적).
 */
public interface ApprovalRequestSubtypeProvider {

    /** 예: "SERVICE_REQUEST". */
    String supportedDomain();

    List<RequestSubtypeOption> subtypes();
}
