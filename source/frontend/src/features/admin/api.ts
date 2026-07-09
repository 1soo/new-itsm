import { apiClient } from "@/lib/apiClient";
import type { UserStatus } from "@/features/auth/types";
import type {
  AuditLog,
  AuditLogQuery,
  CreateRoleRequest,
  CreateUserRequest,
  PageResponse,
  Role,
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
};
