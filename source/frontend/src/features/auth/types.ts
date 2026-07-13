/* auth 도메인 타입 — api_spec/auth.md 계약 기준. */

export type UserStatus = "ACTIVE" | "INACTIVE";

export interface AuthUser {
  id: number;
  email: string;
  name: string;
  status?: UserStatus;
  /** 역할명 배열(예: "SYSTEM_ADMIN", "END_USER"). */
  roles: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
}

export interface MeResponse {
  id: number;
  email: string;
  name: string;
  status: UserStatus;
  roles: string[];
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

/* 내 메뉴 조회(API-AUTH-022) — 사이드바 동적 구성(Role-Menu 동적 매핑). */

export interface MenuItem {
  screenCode: string;
  screenName: string;
  /** 영문 메뉴명(사이드바 i18n, common.md 6.8절). 미입력 시 screenName으로 폴백. */
  screenNameEn?: string | null;
  path: string;
  iconName: string | null;
}

export interface MenuGroup {
  groupCode: string | null;
  groupLabel: string | null;
  /** 영문 그룹명(사이드바 i18n, common.md 6.8절). 미입력 시 groupLabel로 폴백. */
  groupLabelEn?: string | null;
  items: MenuItem[];
}

export interface MyMenuResponse {
  groups: MenuGroup[];
}
