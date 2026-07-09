package com.itsm.asset.infrastructure.persistence;

import com.itsm.asset.domain.CiRelation;
import com.itsm.asset.domain.RelationType;
import com.itsm.asset.domain.repository.CiRelationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CiRelationJpaRepository extends JpaRepository<CiRelation, Long>, CiRelationRepository {

    @Override
    List<CiRelation> findBySourceCiId(Long sourceCiId);

    @Override
    boolean existsBySourceCiIdAndTargetCiIdAndRelationType(Long sourceCiId, Long targetCiId, RelationType relationType);
}
