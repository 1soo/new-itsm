package com.itsm.esm.infrastructure.persistence;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EsmCatalogItemJpaRepository extends JpaRepository<EsmCatalogItem, Long>, EsmCatalogItemRepository {

    @Override
    @Query("""
            select i from EsmCatalogItem i
            where i.isDeleted = false
              and (:department is null or i.department = :department)
              and (:keyword is null or lower(i.name) like lower(concat('%', cast(:keyword as string), '%')))
            order by i.name asc
            """)
    List<EsmCatalogItem> search(@Param("department") Department department, @Param("keyword") String keyword);
}
