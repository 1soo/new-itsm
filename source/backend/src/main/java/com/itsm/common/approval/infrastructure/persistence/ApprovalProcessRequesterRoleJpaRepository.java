package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalProcessRequesterRole;
import com.itsm.common.approval.domain.repository.ApprovalProcessRequesterRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ApprovalProcessRequesterRoleRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalProcessRequesterRoleJpaRepository
        extends JpaRepository<ApprovalProcessRequesterRole, Long>, ApprovalProcessRequesterRoleRepository {

    @Override
    List<ApprovalProcessRequesterRole> findByApprovalProcessId(Long approvalProcessId);

    @Override
    void deleteByApprovalProcessId(Long approvalProcessId);
}
