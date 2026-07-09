package com.itsm.incident.domain.repository;

import com.itsm.incident.domain.PostmortemActionItem;

import java.util.List;

/**
 * 포스트모템 조치항목 저장소 포트.
 */
public interface PostmortemActionItemRepository {

    PostmortemActionItem save(PostmortemActionItem actionItem);

    List<PostmortemActionItem> findByPostmortemId(Long postmortemId);

    void deleteByPostmortemId(Long postmortemId);
}
