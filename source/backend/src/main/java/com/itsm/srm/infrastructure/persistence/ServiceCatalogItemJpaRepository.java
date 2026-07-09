package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceCatalogItemJpaRepository extends JpaRepository<ServiceCatalogItem, Long>, ServiceCatalogItemRepository {

    @Override
    @Query("""
            select i from ServiceCatalogItem i
            where i.isDeleted = false
              and (:category is null or i.category = cast(:category as string))
              and (:keyword is null or lower(i.name) like lower(concat('%', cast(:keyword as string), '%')))
            order by i.name asc
            """)
    List<ServiceCatalogItem> search(@Param("category") String category, @Param("keyword") String keyword);
}
