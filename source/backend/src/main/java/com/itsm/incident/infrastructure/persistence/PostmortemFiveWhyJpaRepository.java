package com.itsm.incident.infrastructure.persistence;

import com.itsm.incident.domain.PostmortemFiveWhy;
import com.itsm.incident.domain.repository.PostmortemFiveWhyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostmortemFiveWhyJpaRepository
        extends JpaRepository<PostmortemFiveWhy, Long>, PostmortemFiveWhyRepository {

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PostmortemFiveWhy f where f.postmortemId = :postmortemId")
    void deleteByPostmortemId(@Param("postmortemId") Long postmortemId);
}
