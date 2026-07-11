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
  path: string;
  iconName: string | null;
}

export interface MenuGroup {
  groupCode: string | null;
  groupLabel: string | null;
  items: MenuItem[];
}

export interface MyMenuResponse {
  groups: MenuGroup[];
}
