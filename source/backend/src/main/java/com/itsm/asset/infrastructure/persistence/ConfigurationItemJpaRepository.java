package com.itsm.asset.infrastructure.persistence;

import com.itsm.asset.domain.ConfigurationItem;
import com.itsm.asset.domain.repository.ConfigurationItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConfigurationItemJpaRepository
        extends JpaRepository<ConfigurationItem, Long>, ConfigurationItemRepository {

    @Override
    @Query("""
            select c from ConfigurationItem c
            where c.isDeleted = false
              and (:keyword is null or lower(c.name) like lower(concat('%', cast(:keyword as string), '%')))
              and (:type is null or lower(c.type) = lower(cast(:type as string)))
            """)
    Page<ConfigurationItem> search(@Param("keyword") String keyword, @Param("type") String type, Pageable pageable);

    @Override
    List<ConfigurationItem> findByAssetId(Long assetId);
}
