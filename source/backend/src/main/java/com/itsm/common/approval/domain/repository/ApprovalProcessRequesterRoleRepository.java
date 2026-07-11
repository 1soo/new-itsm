package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalProcessRequesterRole;

import java.util.List;

/**
 * 승인 프로세스의 승인요청자 역할 스코프 저장소 포트.
 */
public interface ApprovalProcessRequesterRoleRepository {

    ApprovalProcessRequesterRole save(ApprovalProcessRequesterRole entity);

    List<ApprovalProcessRequesterRole> findByApprovalProcessId(Long approvalProcessId);

    void deleteByApprovalProcessId(Long approvalProcessId);

    /** 삭제 직후 즉시 flush(같은 트랜잭션 내 재삽입이 UNIQUE(approval_process_id, role_id)와 충돌하지 않도록). */
    void flush();
}
