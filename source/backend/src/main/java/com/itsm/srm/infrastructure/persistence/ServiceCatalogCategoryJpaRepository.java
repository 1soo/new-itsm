package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.ServiceCatalogCategory;
import com.itsm.srm.domain.repository.ServiceCatalogCategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceCatalogCategoryJpaRepository extends JpaRepository<ServiceCatalogCategory, Long>, ServiceCatalogCategoryRepository {

    @Override
    @Query("select c from ServiceCatalogCategory c where c.isDeleted = false order by c.sortOrder asc")
    List<ServiceCatalogCategory> findAllOrderBySortOrderAsc();

    @Override
    boolean existsByName(String name);

    @Override
    boolean existsByNameAndIdNot(String name, Long id);
}
