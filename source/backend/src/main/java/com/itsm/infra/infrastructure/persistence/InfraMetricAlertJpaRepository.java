package com.itsm.infra.infrastructure.persistence;

import com.itsm.infra.domain.InfraMetricAlert;
import com.itsm.infra.domain.repository.InfraMetricAlertRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InfraMetricAlertJpaRepository
        extends JpaRepository<InfraMetricAlert, Long>, InfraMetricAlertRepository {

    @Override
    @Query("""
            select a from InfraMetricAlert a
            where a.isDeleted = false
              and (:assetId is null or a.assetId = :assetId)
              and (:acknowledged is null or a.acknowledged = :acknowledged)
            order by a.createdAt desc
            """)
    List<InfraMetricAlert> search(@Param("assetId") Long assetId, @Param("acknowledged") Boolean acknowledged);
}
