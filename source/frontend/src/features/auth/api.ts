import { apiClient } from "@/lib/apiClient";
import type {
  ChangePasswordRequest,
  LoginRequest,
  LoginResponse,
  MeResponse,
} from "@/features/auth/types";

/* auth API 호출 — 모두 공통 apiClient 경유(직접 fetch/axios 금지). */

export const authApi = {
  // API-AUTH-001 로그인
  async login(body: LoginRequest): Promise<LoginResponse> {
    const res = await apiClient.post<LoginResponse>("/auth/login", body);
    return res.data;
  },

  // API-AUTH-003 로그아웃 (Refresh는 httpOnly Cookie로 서버가 무효화)
  async logout(): Promise<void> {
    await apiClient.post("/auth/logout", {});
  },

  // API-AUTH-004 내 정보 조회
  async me(): Promise<MeResponse> {
    const res = await apiClient.get<MeResponse>("/auth/me");
    return res.data;
  },

  // API-AUTH-005 비밀번호 변경
  async changePassword(body: ChangePasswordRequest): Promise<{ message: string }> {
    const res = await apiClient.patch<{ message: string }>("/auth/me/password", body);
    return res.data;
  },
};
