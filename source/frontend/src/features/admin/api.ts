import { apiClient } from "@/lib/apiClient";
import type { UserStatus } from "@/features/auth/types";
import type {
  ApprovalDomainOption,
  ApprovalProcessDetail,
  ApprovalProcessListQuery,
  ApprovalProcessSummary,
  AuditLog,
  AuditLogQuery,
  CreateApprovalProcessRequest,
  CreateMenuRequest,
  CreateRoleRequest,
  CreateUserRequest,
  PageResponse,
  RequestSubtypeOption,
  Role,
  Screen,
  ScreenListQuery,
  TargetStateOption,
  UpdateApprovalProcessRequest,
  UpdateMenuRequest,
  UserDetail,
  UserListQuery,
  UserSummary,
} from "@/features/admin/types";

/* admin API 호출 — 모두 공통 apiClient 경유. System Admin 전용(미보유 시 403). */

/** 빈 값(undefined/"")을 제외한 쿼리 파라미터만 전달. */
function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") {
      params[key] = value;
    }
  }
  return params;
}

export const adminApi = {
  // API-AUTH-006 계정 목록
  async listUsers(query: UserListQuery): Promise<PageResponse<UserSummary>> {
    const res = await apiClient.get<PageResponse<UserSummary>>("/admin/users", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-AUTH-007 계정 생성
  async createUser(body: CreateUserRequest): Promise<UserDetail> {
    const res = await apiClient.post<UserDetail>("/admin/users", body);
    return res.data;
  },

  // API-AUTH-008 계정 상세
  async getUser(userId: number): Promise<UserDetail> {
    const res = await apiClient.get<UserDetail>(`/admin/users/${userId}`);
    return res.data;
  },

  // API-AUTH-009 계정 수정
  async updateUser(userId: number, body: { name: string }): Promise<UserDetail> {
    const res = await apiClient.patch<UserDetail>(`/admin/users/${userId}`, body);
    return res.data;
  },

  // API-AUTH-010 계정 상태 변경(활성/비활성)
  async setUserStatus(
    userId: number,
    status: UserStatus,
  ): Promise<{ id: number; status: UserStatus }> {
    const res = await apiClient.patch<{ id: number; status: UserStatus }>(
      `/admin/users/${userId}/status`,
      { status },
    );
    return res.data;
  },

  // API-AUTH-011 사용자 역할 부여
  async assignRole(userId: number, roleId: number): Promise<{ userId: number; roles: string[] }> {
    const res = await apiClient.post<{ userId: number; roles: string[] }>(
      `/admin/users/${userId}/roles`,
      { roleId },
    );
    return res.data;
  },

  // API-AUTH-012 사용자 역할 회수
  async revokeRole(userId: number, roleId: number): Promise<{ userId: number; roles: string[] }> {
    const res = await apiClient.delete<{ userId: number; roles: string[] }>(
      `/admin/users/${userId}/roles/${roleId}`,
    );
    return res.data;
  },

  // API-AUTH-013 역할 목록
  async listRoles(): Promise<Role[]> {
    const res = await apiClient.get<Role[]>("/admin/roles");
    return res.data;
  },

  // API-AUTH-014 역할 생성
  async createRole(body: CreateRoleRequest): Promise<Role> {
    const res = await apiClient.post<Role>("/admin/roles", body);
    return res.data;
  },

  // API-AUTH-015 감사 로그 조회
  async listAuditLogs(query: AuditLogQuery): Promise<PageResponse<AuditLog>> {
    const res = await apiClient.get<PageResponse<AuditLog>>("/admin/audit-logs", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-AUTH-016 메뉴(화면) 목록 조회
  async listScreens(query: ScreenListQuery): Promise<PageResponse<Screen>> {
    const res = await apiClient.get<PageResponse<Screen>>("/admin/screens", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-AUTH-017 메뉴 생성
  async createScreen(body: CreateMenuRequest): Promise<Screen> {
    const res = await apiClient.post<Screen>("/admin/screens", body);
    return res.data;
  },

  // API-AUTH-018 메뉴 수정
  async updateScreen(screenId: number, body: UpdateMenuRequest): Promise<Screen> {
    const res = await apiClient.patch<Screen>(`/admin/screens/${screenId}`, body);
    return res.data;
  },

  // API-AUTH-019 메뉴 삭제
  async deleteScreen(screenId: number): Promise<{ id: number; deleted: boolean }> {
    const res = await apiClient.delete<{ id: number; deleted: boolean }>(
      `/admin/screens/${screenId}`,
    );
    return res.data;
  },

  // API-AUTH-020 메뉴에 역할 매핑 부여
  async assignScreenRole(
    screenId: number,
    roleId: number,
  ): Promise<{ screenId: number; roles: string[] }> {
    const res = await apiClient.post<{ screenId: number; roles: string[] }>(
      `/admin/screens/${screenId}/roles`,
      { roleId },
    );
    return res.data;
  },

  // API-AUTH-021 메뉴 역할 매핑 회수
  async revokeScreenRole(
    screenId: number,
    roleId: number,
  ): Promise<{ screenId: number; roles: string[] }> {
    const res = await apiClient.delete<{ screenId: number; roles: string[] }>(
      `/admin/screens/${screenId}/roles/${roleId}`,
    );
    return res.data;
  },

  // API-AUTH-023 승인 프로세스 대상 도메인 목록
  async listApprovalDomains(): Promise<ApprovalDomainOption[]> {
    const res = await apiClient.get<ApprovalDomainOption[]>("/admin/approval-processes/domains");
    return res.data;
  },

  // API-AUTH-024 도메인별 요청유형 후보 목록
  async listRequestSubtypes(domain: string): Promise<RequestSubtypeOption[]> {
    const res = await apiClient.get<RequestSubtypeOption[]>(
      `/admin/approval-processes/domains/${domain}/request-subtypes`,
    );
    return res.data;
  },

  // API-AUTH-031 도메인별 유효 상태값(적용 상태) 후보 목록
  async listApprovalStates(domain: string): Promise<TargetStateOption[]> {
    const res = await apiClient.get<TargetStateOption[]>(
      `/admin/approval-processes/domains/${domain}/states`,
    );
    return res.data;
  },

  // API-AUTH-025 승인 프로세스 목록
  async listApprovalProcesses(
    query: ApprovalProcessListQuery,
  ): Promise<PageResponse<ApprovalProcessSummary>> {
    const res = await apiClient.get<PageResponse<ApprovalProcessSummary>>(
      "/admin/approval-processes",
      { params: cleanParams(query) },
    );
    return res.data;
  },

  // API-AUTH-026 승인 프로세스 상세
  async getApprovalProcess(id: number): Promise<ApprovalProcessDetail> {
    const res = await apiClient.get<ApprovalProcessDetail>(`/admin/approval-processes/${id}`);
    return res.data;
  },

  // API-AUTH-027 승인 프로세스 생성
  async createApprovalProcess(body: CreateApprovalProcessRequest): Promise<ApprovalProcessDetail> {
    const res = await apiClient.post<ApprovalProcessDetail>("/admin/approval-processes", body);
    return res.data;
  },

  // API-AUTH-028 승인 프로세스 수정
  async updateApprovalProcess(
    id: number,
    body: UpdateApprovalProcessRequest,
  ): Promise<ApprovalProcessDetail> {
    const res = await apiClient.patch<ApprovalProcessDetail>(
      `/admin/approval-processes/${id}`,
      body,
    );
    return res.data;
  },

  // API-AUTH-029 승인 프로세스 삭제
  async deleteApprovalProcess(id: number): Promise<{ id: number; deleted: boolean }> {
    const res = await apiClient.delete<{ id: number; deleted: boolean }>(
      `/admin/approval-processes/${id}`,
    );
    return res.data;
  },
};
