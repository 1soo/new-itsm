import { apiClient } from "@/lib/apiClient";
import type {
  DismissalItem,
  DismissResult,
  NotificationDismissalListResponse,
} from "@/features/common/types";

/* common(알림 확인처리) API 호출 — 모두 공통 apiClient 경유. */

export const commonApi = {
  // API-COM-001 알림 확인처리(개별/일괄, 멱등)
  async dismissNotifications(items: DismissalItem[]): Promise<DismissResult> {
    const res = await apiClient.post<DismissResult>("/notifications/dismissals", { items });
    return res.data;
  },

  // API-COM-002 확인처리된 알림 목록 조회
  async listDismissals(): Promise<NotificationDismissalListResponse> {
    const res = await apiClient.get<NotificationDismissalListResponse>("/notifications/dismissals");
    return res.data;
  },
};
