package com.itsm.auth.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할-화면 매핑(다대다). 역할-메뉴 매핑도 겸한다. UNIQUE(screen_id, role_id)로 중복 부여를 방지한다.
 */
@Getter
@Entity
@Table(name = "screen_role", uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "role_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScreenRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "screen_id", nullable = false)
    private Long screenId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public ScreenRole(Long screenId, Long roleId) {
        this.screenId = screenId;
        this.roleId = roleId;
    }
}
