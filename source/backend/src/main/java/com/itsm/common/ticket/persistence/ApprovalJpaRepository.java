package com.itsm.common.ticket.persistence;

import com.itsm.common.ticket.Approval;
import com.itsm.common.ticket.repository.ApprovalRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ApprovalRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalJpaRepository extends JpaRepository<Approval, Long>, ApprovalRepository {
}
