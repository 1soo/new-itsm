package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 사용자 계정 저장소 포트. 구현은 infrastructure(Spring Data JPA)에서 제공한다.
 */
public interface AppUserRepository {

    AppUser save(AppUser user);

    Optional<AppUser> findById(Long id);

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<AppUser> search(String email, String name, UserStatus status, String roleCode, Pageable pageable);
}
