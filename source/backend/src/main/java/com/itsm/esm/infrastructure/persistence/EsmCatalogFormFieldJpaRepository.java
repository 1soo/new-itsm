package com.itsm.esm.infrastructure.persistence;

import com.itsm.esm.domain.EsmCatalogFormField;
import com.itsm.esm.domain.repository.EsmCatalogFormFieldRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EsmCatalogFormFieldJpaRepository extends JpaRepository<EsmCatalogFormField, Long>, EsmCatalogFormFieldRepository {

    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from EsmCatalogFormField f where f.catalogItemId = :catalogItemId")
    void deleteByCatalogItemId(@Param("catalogItemId") Long catalogItemId);
}
