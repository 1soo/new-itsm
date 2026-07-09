package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * AppUserRepository 포트의 Spring Data JPA 구현.
 */
public interface AppUserJpaRepository extends JpaRepository<AppUser, Long>, AppUserRepository {

    @Override
    @Query("""
            select distinct u from AppUser u
            where u.isDeleted = false
              and (:email is null or lower(u.email) like lower(concat('%', cast(:email as string), '%')))
              and (:name is null or lower(u.name) like lower(concat('%', cast(:name as string), '%')))
              and (:status is null or u.status = :status)
              and (:roleCode is null or exists (
                    select 1 from UserRole ur, Role r
                    where ur.userId = u.id and ur.roleId = r.id and r.roleCode = cast(:roleCode as string)))
            """)
    Page<AppUser> search(@Param("email") String email,
                         @Param("name") String name,
                         @Param("status") UserStatus status,
                         @Param("roleCode") String roleCode,
                         Pageable pageable);
}
