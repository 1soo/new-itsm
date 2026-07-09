/* knowledge(KM) 도메인 타입 — api_spec/knowledge.md 계약 기준. */

export type ArticleStatus = "DRAFT" | "IN_REVIEW" | "PUBLISHED";
export type Decision = "APPROVE" | "REJECT";
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

export interface Category {
  id: number;
  name: string;
}

export interface ReviewQueueItem {
  articleId: number;
  title: string;
  author: string;
  requestedAt: string;
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
