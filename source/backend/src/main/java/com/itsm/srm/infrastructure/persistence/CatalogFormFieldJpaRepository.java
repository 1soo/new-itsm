package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.CatalogFormField;
import com.itsm.srm.domain.repository.CatalogFormFieldRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogFormFieldJpaRepository extends JpaRepository<CatalogFormField, Long>, CatalogFormFieldRepository {
}
