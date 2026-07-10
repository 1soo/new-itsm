package com.itsm.esm.infrastructure.persistence;

import com.itsm.esm.domain.EsmCatalogFormField;
import com.itsm.esm.domain.repository.EsmCatalogFormFieldRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsmCatalogFormFieldJpaRepository extends JpaRepository<EsmCatalogFormField, Long>, EsmCatalogFormFieldRepository {
}
