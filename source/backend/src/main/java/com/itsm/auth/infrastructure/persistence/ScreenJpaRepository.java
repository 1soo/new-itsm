package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.Screen;
import com.itsm.auth.domain.repository.ScreenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ScreenRepository 포트의 Spring Data JPA 구현.
 */
public interface ScreenJpaRepository extends JpaRepository<Screen, Long>, ScreenRepository {

    @Override
    boolean existsByScreenCode(String screenCode);

    @Override
    boolean existsByPath(String path);

    @Override
    boolean existsByPathAndIdNot(String path, Long id);

    @Override
    @Query("""
            select s from Screen s
            where s.isDeleted = false
              and (:groupCode is null or s.groupCode = cast(:groupCode as string))
              and (:domain is null or s.domain = cast(:domain as string))
            """)
    Page<Screen> search(@Param("groupCode") String groupCode, @Param("domain") String domain, Pageable pageable);

    @Override
    @Query("select s from Screen s where s.isDeleted = false and s.navVisible = true order by s.sortOrder asc")
    List<Screen> findAllVisible();
}
