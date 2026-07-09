import type { UserStatus } from "@/features/auth/types";

/* admin(계정/역할/감사) 도메인 타입 — api_spec/auth.md(API-AUTH-006~015) 기준. */

/** 공통 페이지 응답 형태. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface UserSummary {
  id: number;
  email: string;
  name: string;
  status: UserStatus;
  roles: string[];
  createdAt: string;
}

export interface UserDetail extends UserSummary {
  updatedAt: string;
}

export interface Role {
  id: number;
  /** 역할 코드(대문자 스네이크) — user.roles와 매핑되는 식별 토큰. */
  roleCode: string;
  /** 표시명. */
  name: string;
  description: string;
  userCount?: number;
}

export type AuditEventType =
  | "LOGIN"
  | "LOGOUT"
  | "REFRESH"
  | "USER_CHANGE"
  | "ROLE_CHANGE";

export type AuditResult = "SUCCESS" | "FAILURE";

export interface AuditLog {
  id: number;
  eventType: AuditEventType;
  actor: string;
  target: string;
  result: AuditResult;
  occurredAt: string;
}

export interface UserListQuery {
  email?: string;
  name?: string;
  status?: UserStatus | "";
  role?: string;
  page?: number;
  size?: number;
}

export interface CreateUserRequest {
  email: string;
  name: string;
  initialPassword: string;
  roleIds: number[];
}

export interface CreateRoleRequest {
  roleCode: string;
  name: string;
  description: string;
}

export interface AuditLogQuery {
  eventType?: AuditEventType | "";
  actor?: string;
  target?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}
