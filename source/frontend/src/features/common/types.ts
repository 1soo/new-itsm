/* common(알림 확인처리) 도메인 타입 — api_spec/common.md(API-COM-001/002) 기준. */

export type NotificationType = "SERVICE_REQUEST_APPROVAL" | "CHANGE_APPROVAL" | "ASSET_EXPIRY";

export interface DismissalItem {
  notificationType: NotificationType;
  sourceId: number;
}

export interface DismissResult {
  dismissedCount: number;
}

export interface NotificationDismissal {
  notificationType: NotificationType;
  sourceId: number;
  dismissedAt: string;
}

export interface NotificationDismissalListResponse {
  items: NotificationDismissal[];
}
