package com.itsm.common.ticket.persistence;

import com.itsm.common.ticket.Comment;
import com.itsm.common.ticket.repository.CommentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CommentRepository 포트의 Spring Data JPA 구현.
 */
public interface CommentJpaRepository extends JpaRepository<Comment, Long>, CommentRepository {
}
