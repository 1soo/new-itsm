package com.itsm.asset.domain.repository;

import com.itsm.asset.domain.CiRelation;
import com.itsm.asset.domain.RelationType;

import java.util.List;

/**
 * CI 관계 저장소 포트.
 */
public interface CiRelationRepository {

    CiRelation save(CiRelation relation);

    List<CiRelation> findBySourceCiId(Long sourceCiId);

    boolean existsBySourceCiIdAndTargetCiIdAndRelationType(Long sourceCiId, Long targetCiId, RelationType relationType);
}
