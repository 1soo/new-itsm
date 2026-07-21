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

/* 메뉴(화면) 관리(SCR-ADMIN-006, Role-Menu 동적 매핑) — api_spec/auth.md(API-AUTH-016~022) 기준. */

export interface Screen {
  id: number;
  screenCode: string;
  screenName: string;
  /** 영문 메뉴명(사이드바 i18n, common.md 6.8절). */
  screenNameEn: string | null;
  path: string;
  domain: string;
  iconName: string | null;
  groupCode: string | null;
  groupLabel: string | null;
  /** 영문 그룹명(사이드바 i18n, common.md 6.8절). */
  groupLabelEn: string | null;
  sortOrder: number;
  navVisible: boolean;
  /** 매핑된 역할 코드(비어있으면 전체 인증 사용자 공개). */
  roles: string[];
}

export interface ScreenListQuery {
  groupCode?: string;
  domain?: string;
  page?: number;
  size?: number;
}

export interface CreateMenuRequest {
  screenCode: string;
  screenName: string;
  screenNameEn: string;
  path: string;
  domain: string;
  iconName?: string;
  groupCode?: string;
  groupLabel?: string;
  groupLabelEn?: string;
  sortOrder?: number;
  navVisible?: boolean;
}

export interface UpdateMenuRequest {
  screenName?: string;
  screenNameEn?: string;
  path?: string;
  iconName?: string;
  groupCode?: string;
  groupLabel?: string;
  groupLabelEn?: string;
  sortOrder?: number;
  navVisible?: boolean;
}

/* 승인 프로세스 관리(SCR-ADMIN-007/008, 승인 프로세스 커스텀 기능) — api_spec/auth.md(API-AUTH-023~029) 기준. */

/** 승인 프로세스 대상 도메인(요청자가 제출하는 티켓 개념이 있는 9개 도메인). */
export type ApprovalDomain =
  | "SERVICE_REQUEST"
  | "CHANGE"
  | "KNOWLEDGE"
  | "INCIDENT"
  | "PROBLEM"
  | "ASSET"
  | "VULNERABILITY"
  | "COMPLIANCE"
  | "ESM";

export interface ApprovalDomainOption {
  domain: ApprovalDomain;
  label: string;
  hasRequestSubtype: boolean;
}

export interface RequestSubtypeOption {
  /** request_subtype_key로 저장될 값. */
  key: string;
  label: string;
}

/** 도메인별 유효 상태값(적용 상태) 후보(API-AUTH-031, 2026-07-22 유지보수 요청). */
export interface TargetStateOption {
  /** target_state로 저장될 상태 코드. */
  value: string;
  label: string;
}

export type DecisionMode = "AND" | "OR";

export interface ApprovalProcessStep {
  stepNo: number;
  decisionMode: DecisionMode;
  roleIds: number[];
}

export interface ApprovalProcessSummary {
  id: number;
  /** null이면 전체 도메인 적용(2026-07-15 우선순위 재설계). */
  domain: ApprovalDomain | null;
  /** null이면 전체 상태 공통(2026-07-22 유지보수 요청, 4번째 매칭 축). */
  targetState: string | null;
  targetStateLabel: string | null;
  requestSubtypeKey: string | null;
  requestSubtypeLabel: string | null;
  priorityTier: number;
  name: string;
  requesterRoles: string[];
  stepCount: number;
}

export interface ApprovalProcessDetail {
  id: number;
  /** null이면 전체 도메인 적용(2026-07-15 우선순위 재설계). */
  domain: ApprovalDomain | null;
  /** null이면 전체 상태 공통(2026-07-22 유지보수 요청, 4번째 매칭 축). */
  targetState: string | null;
  requestSubtypeKey: string | null;
  name: string;
  description: string | null;
  requesterRoleIds: number[];
  steps: ApprovalProcessStep[];
}

export interface ApprovalProcessListQuery {
  domain?: ApprovalDomain | "";
  page?: number;
  size?: number;
}

export interface CreateApprovalProcessRequest {
  /** null이면 전체 도메인 적용(2026-07-15 우선순위 재설계로 필수→선택 변경). */
  domain: ApprovalDomain | null;
  /** null이면 전체 상태 공통. domain이 null이면 반드시 null(2026-07-22 유지보수 요청). */
  targetState: string | null;
  requestSubtypeKey: string | null;
  name: string;
  description?: string;
  requesterRoleIds: number[];
  steps: { decisionMode: DecisionMode; roleIds: number[] }[];
}

export interface UpdateApprovalProcessRequest {
  name?: string;
  description?: string;
  requesterRoleIds?: number[];
  steps?: { decisionMode: DecisionMode; roleIds: number[] }[];
}
