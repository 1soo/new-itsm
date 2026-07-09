package com.itsm.asset.infrastructure.persistence;

import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.AssetType;
import com.itsm.asset.domain.repository.AssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface AssetJpaRepository extends JpaRepository<Asset, Long>, AssetRepository {

    @Override
    @Query("""
            select a from Asset a
            where a.isDeleted = false
              and (:type is null or a.type = :type)
              and (:status is null or a.status = :status)
              and (:owner is null or lower(a.owner) = lower(cast(:owner as string)))
              and (:keyword is null
                   or lower(a.name) like lower(concat('%', cast(:keyword as string), '%'))
                   or lower(a.assetKey) like lower(concat('%', cast(:keyword as string), '%')))
              and (:filterExpiring = false or (
                    (a.licenseExpiry is not null and a.licenseExpiry <= :expiringThreshold)
                    or (a.warrantyExpiry is not null and a.warrantyExpiry <= :expiringThreshold)
                    or (a.contractExpiry is not null and a.contractExpiry <= :expiringThreshold)
              ))
            """)
    Page<Asset> search(@Param("type") AssetType type,
                       @Param("status") AssetStatus status,
                       @Param("owner") String owner,
                       @Param("keyword") String keyword,
                       @Param("filterExpiring") boolean filterExpiring,
                       @Param("expiringThreshold") LocalDate expiringThreshold,
                       Pageable pageable);

    @Override
    List<Asset> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
