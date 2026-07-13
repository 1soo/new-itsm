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
 * 화면 정보. screen_code는 화면 설계서의 SCR-* 코드와 1:1 매핑.
 * 사이드바 메뉴 마스터도 겸한다(Role-Menu 동적 매핑, 유지보수 요청 2026-07-11).
 */
@Getter
@Entity
@Table(name = "screen")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Screen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "screen_code", nullable = false, unique = true, length = 50)
    private String screenCode;

    @Column(name = "screen_name", nullable = false, length = 100)
    private String screenName;

    @Column(name = "screen_name_en", nullable = false, length = 100)
    private String screenNameEn;

    @Column(nullable = false, unique = true, length = 255)
    private String path;

    @Column(nullable = false, length = 30)
    private String domain;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "group_code", length = 30)
    private String groupCode;

    @Column(name = "group_label", length = 50)
    private String groupLabel;

    @Column(name = "group_label_en", length = 50)
    private String groupLabelEn;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "nav_visible", nullable = false)
    private boolean navVisible;

    public Screen(String screenCode, String screenName, String screenNameEn, String path, String domain,
                  String iconName, String groupCode, String groupLabel, String groupLabelEn,
                  int sortOrder, boolean navVisible) {
        this.screenCode = screenCode;
        this.screenName = screenName;
        this.screenNameEn = screenNameEn;
        this.path = path;
        this.domain = domain;
        this.iconName = iconName;
        this.groupCode = groupCode;
        this.groupLabel = groupLabel;
        this.groupLabelEn = groupLabelEn;
        this.sortOrder = sortOrder;
        this.navVisible = navVisible;
    }

    public void update(String screenName, String screenNameEn, String path, String iconName, String groupCode,
                        String groupLabel, String groupLabelEn, Integer sortOrder, Boolean navVisible) {
        if (screenName != null) {
            this.screenName = screenName;
        }
        if (screenNameEn != null) {
            this.screenNameEn = screenNameEn;
        }
        if (path != null) {
            this.path = path;
        }
        if (iconName != null) {
            this.iconName = iconName;
        }
        if (groupCode != null) {
            this.groupCode = groupCode;
        }
        if (groupLabel != null) {
            this.groupLabel = groupLabel;
        }
        if (groupLabelEn != null) {
            this.groupLabelEn = groupLabelEn;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (navVisible != null) {
            this.navVisible = navVisible;
        }
    }
}
