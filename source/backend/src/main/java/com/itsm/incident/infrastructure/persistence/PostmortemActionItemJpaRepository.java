package com.itsm.incident.infrastructure.persistence;

import com.itsm.incident.domain.PostmortemActionItem;
import com.itsm.incident.domain.repository.PostmortemActionItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostmortemActionItemJpaRepository
        extends JpaRepository<PostmortemActionItem, Long>, PostmortemActionItemRepository {

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PostmortemActionItem a where a.postmortemId = :postmortemId")
    void deleteByPostmortemId(@Param("postmortemId") Long postmortemId);
}
