package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.CatalogFormField;
import com.itsm.srm.domain.repository.CatalogFormFieldRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogFormFieldJpaRepository extends JpaRepository<CatalogFormField, Long>, CatalogFormFieldRepository {

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CatalogFormField f where f.catalogItemId = :catalogItemId")
    void deleteByCatalogItemId(@Param("catalogItemId") Long catalogItemId);
}
