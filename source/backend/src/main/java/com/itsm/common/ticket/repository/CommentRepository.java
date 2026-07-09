package com.itsm.common.ticket.repository;

import com.itsm.common.ticket.Comment;
import com.itsm.common.ticket.TicketType;

import java.util.List;

/**
 * 코멘트 저장소 포트.
 */
public interface CommentRepository {

    Comment save(Comment comment);

    List<Comment> findByTicketTypeAndTicketIdOrderByCreatedAtAsc(TicketType ticketType, Long ticketId);
}
