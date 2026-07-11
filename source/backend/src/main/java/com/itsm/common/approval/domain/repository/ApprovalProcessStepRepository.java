package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalProcessStep;

import java.util.List;

/**
 * 승인 프로세스의 승인자 차수 저장소 포트.
 */
public interface ApprovalProcessStepRepository {

    ApprovalProcessStep save(ApprovalProcessStep step);

    List<ApprovalProcessStep> findByApprovalProcessIdOrderByStepNoAsc(Long approvalProcessId);

    void deleteByApprovalProcessId(Long approvalProcessId);

    /** 삭제 직후 즉시 flush(같은 트랜잭션 내 재삽입이 UNIQUE(approval_process_id, step_no)와 충돌하지 않도록). */
    void flush();
}
