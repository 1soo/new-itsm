/* knowledge(KM) 도메인 타입 — api_spec/knowledge.md 계약 기준. */

export type ArticleStatus = "DRAFT" | "IN_REVIEW" | "PUBLISHED";
export type KcsTicketType = "SERVICE_REQUEST" | "INCIDENT" | "PROBLEM";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  noResult: boolean;
}

export interface ArticleSummary {
  id: number;
  title: string;
  summary: string;
  status: ArticleStatus;
  category: string;
  helpfulRate: number;
  /** 진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null). 2026-07-22 유지보수 요청 신규. */
  pendingApprovalTargetState: ArticleStatus | null;
}

/** 승인 프로세스 커스텀 기능(유지보수 요청) — approvalRequestId=null이면 매칭되는 승인 프로세스가 없어 게이트 없이 진행. */
export interface ArticleApproval {
  approvalRequestId: number | null;
  status: "IN_PROGRESS" | "APPROVED" | "REJECTED" | null;
  /** 원본 코드값(도착 상태, 생성 시점 스냅샷). 2026-07-22 유지보수 요청 신규. */
  targetState: ArticleStatus | null;
}

export interface ArticleDetail {
  id: number;
  title: string;
  body: string;
  status: ArticleStatus;
  category: string;
  labels: string[];
  helpful: number;
  notHelpful: number;
  approval: ArticleApproval;
}

export interface ArticleListQuery {
  keyword?: string;
  category?: string;
  label?: string;
  status?: ArticleStatus;
  page?: number;
  size?: number;
}

export interface ArticleInput {
  title: string;
  body: string;
  categoryId?: number;
  labels?: string[];
}

export interface CreatedArticle {
  id: number;
  status: ArticleStatus;
}

/** API-KM-006 상태 전이 응답. 승인 프로세스 커스텀 기능(유지보수 요청) — 매칭 규칙 없으면 즉시 PUBLISHED. */
export interface ArticleTransitionResult {
  id: number;
  status: Extract<ArticleStatus, "IN_REVIEW" | "PUBLISHED">;
  approvalRequestId: number | null;
}

export interface Category {
  id: number;
  name: string;
}

export interface FeedbackInput {
  helpful: boolean;
  comment?: string;
}

export interface FeedbackResult {
  helpful: number;
  notHelpful: number;
}

export interface LinkInput {
  ticketType: KcsTicketType;
  ticketId: number;
  articleId?: number;
  newArticle?: { title: string; body: string };
}

export interface KnowledgeMetrics {
  usageCount: number;
  noResultSearchCount: number;
  helpfulRate: number;
  deflectionRate: number;
  topNoResultKeywords: string[];
}
