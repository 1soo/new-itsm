package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.Screen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 화면(메뉴) 저장소 포트.
 */
public interface ScreenRepository {

    Screen save(Screen screen);

    Optional<Screen> findById(Long id);

    boolean existsByScreenCode(String screenCode);

    boolean existsByPath(String path);

    boolean existsByPathAndIdNot(String path, Long id);

    Page<Screen> search(String groupCode, String domain, Pageable pageable);

    List<Screen> findAllVisible();
}
