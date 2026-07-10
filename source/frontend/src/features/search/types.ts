/* 통합 검색(SEARCH) 도메인 타입 — api_spec/search.md API-SEARCH-001 준수. */

export type SearchDomain =
  | "KNOWLEDGE"
  | "SERVICE_REQUEST"
  | "INCIDENT"
  | "PROBLEM"
  | "CHANGE";

export interface SearchResultItem {
  domain: SearchDomain;
  key: string;
  title: string;
  status: string;
  snippet: string | null;
  updatedAt: string;
  url: string;
}

export interface SearchQuery {
  keyword: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}
