import { apiClient } from "@/lib/apiClient";
import type {
  ApprovalDecisionRequest,
  ApprovalDecisionResult,
  ApprovalDetail,
  ApprovalListItem,
  ApprovalListQuery,
  ApprovalResubmitRequest,
  ApprovalResubmitResult,
  DismissalItem,
  DismissResult,
  NotificationDismissalListResponse,
  PageResponse,
} from "@/features/common/types";

/* common(알림 확인처리 · 전 도메인 공용 승인) API 호출 — 모두 공통 apiClient 경유. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

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

  // API-COM-003 승인 대기함 목록 조회(전 도메인 공용, scope=mine 고정)
  async listMyApprovals(query: ApprovalListQuery = {}): Promise<PageResponse<ApprovalListItem>> {
    const res = await apiClient.get<PageResponse<ApprovalListItem>>("/approvals", {
      params: { scope: "mine", ...cleanParams(query) },
    });
    return res.data;
  },

  // API-COM-004 승인 인스턴스 상세 조회
  async getApproval(approvalRequestId: number): Promise<ApprovalDetail> {
    const res = await apiClient.get<ApprovalDetail>(`/approvals/${approvalRequestId}`);
    return res.data;
  },

  // API-COM-005 승인/반려 결정
  async decide(
    approvalRequestId: number,
    body: ApprovalDecisionRequest,
  ): Promise<ApprovalDecisionResult> {
    const res = await apiClient.post<ApprovalDecisionResult>(
      `/approvals/${approvalRequestId}/decisions`,
      body,
    );
    return res.data;
  },

  // API-COM-006 반려 후 재승인요청
  async resubmitApproval(body: ApprovalResubmitRequest): Promise<ApprovalResubmitResult> {
    const res = await apiClient.post<ApprovalResubmitResult>("/approvals/resubmit", body);
    return res.data;
  },
};
