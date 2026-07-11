package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.ScreenRole;

import java.util.List;
import java.util.Optional;

/**
 * 역할-화면(메뉴) 매핑 저장소 포트.
 */
public interface ScreenRoleRepository {

    ScreenRole save(ScreenRole screenRole);

    List<ScreenRole> findByScreenId(Long screenId);

    List<ScreenRole> findByRoleId(Long roleId);

    boolean existsByScreenIdAndRoleId(Long screenId, Long roleId);

    Optional<ScreenRole> findByScreenIdAndRoleId(Long screenId, Long roleId);

    void delete(ScreenRole screenRole);
}
