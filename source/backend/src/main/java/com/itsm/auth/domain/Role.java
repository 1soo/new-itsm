package com.itsm.auth.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할 정의. role_code는 RBAC 인가 판정에 사용하는 고유 코드(SYSTEM_ADMIN 등).
 */
@Getter
@Entity
@Table(name = "role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(length = 255)
    private String description;

    public Role(String roleCode, String roleName, String description) {
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
    }
}
