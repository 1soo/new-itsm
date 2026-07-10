/* 역할 상수·RBAC 헬퍼 — security/authorization 설계 기준. */

export const ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
export const ROLE_END_USER = "END_USER";
export const ROLE_SERVICE_DESK_AGENT = "SERVICE_DESK_AGENT";
export const ROLE_APPROVER = "APPROVER";
export const ROLE_PROCESS_OWNER = "PROCESS_OWNER";
export const ROLE_INCIDENT_MANAGER = "INCIDENT_MANAGER";
export const ROLE_PROBLEM_MANAGER = "PROBLEM_MANAGER";
export const ROLE_CHANGE_MANAGER = "CHANGE_MANAGER";
export const ROLE_KNOWLEDGE_CONTRIBUTOR = "KNOWLEDGE_CONTRIBUTOR";
export const ROLE_KNOWLEDGE_GATEKEEPER = "KNOWLEDGE_GATEKEEPER";
export const ROLE_ASSET_MANAGER = "ASSET_MANAGER";
export const ROLE_HR_CASE_MANAGER = "HR_CASE_MANAGER";
export const ROLE_DEPT_COORDINATOR = "DEPT_COORDINATOR";

export function isSystemAdmin(roles: string[] | undefined): boolean {
  return !!roles?.includes(ROLE_SYSTEM_ADMIN);
}

/** 사용자 역할이 허용 역할 중 하나라도 포함하면 true. RBAC 라우트 가드·메뉴 노출에 사용. */
export function hasAnyRole(roles: string[] | undefined, allowed: string[]): boolean {
  return !!roles?.some((r) => allowed.includes(r));
}

/** 로그인 후 역할 기본 홈(SCR-AUTH-001). SYSTEM_ADMIN은 관리자 홈, 그 외는 대시보드. */
export function roleHome(roles: string[] | undefined): string {
  return isSystemAdmin(roles) ? "/admin/users" : "/";
}
