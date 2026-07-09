/* 역할 상수·RBAC 헬퍼 — security/authorization 설계 기준. */

export const ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

export function isSystemAdmin(roles: string[] | undefined): boolean {
  return !!roles?.includes(ROLE_SYSTEM_ADMIN);
}

/** 로그인 후 역할 기본 홈(SCR-AUTH-001). SYSTEM_ADMIN은 관리자 홈, 그 외는 대시보드. */
export function roleHome(roles: string[] | undefined): string {
  return isSystemAdmin(roles) ? "/admin/users" : "/";
}
