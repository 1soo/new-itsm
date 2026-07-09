import type { StatusTone } from "@/components/common";
import type { ArticleStatus } from "@/features/knowledge/types";

/* KM 상태 표시 매핑 — common.md 시맨틱 색상, knowledge.md 팔레트. */

const STATUS_LABEL: Record<ArticleStatus, string> = {
  DRAFT: "초안",
  IN_REVIEW: "검토",
  PUBLISHED: "게시",
};

const STATUS_TONE: Record<ArticleStatus, StatusTone> = {
  DRAFT: "muted",
  IN_REVIEW: "warning",
  PUBLISHED: "success",
};

export function statusLabel(s: ArticleStatus): string {
  return STATUS_LABEL[s] ?? s;
}
export function statusTone(s: ArticleStatus): StatusTone {
  return STATUS_TONE[s] ?? "muted";
}

export const ARTICLE_STATUSES: ArticleStatus[] = ["DRAFT", "IN_REVIEW", "PUBLISHED"];
