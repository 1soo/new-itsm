import { apiClient } from "@/lib/apiClient";
import type { PageResponse, SearchQuery, SearchResultItem } from "@/features/search/types";

/* SEARCH API 호출 — 공통 apiClient 경유. api_spec/search.md API-SEARCH-001 준수. */

export const searchApi = {
  // API-SEARCH-001 통합 검색(지식+티켓 교차 도메인)
  async search(query: SearchQuery): Promise<PageResponse<SearchResultItem>> {
    const res = await apiClient.get<PageResponse<SearchResultItem>>("/search", {
      params: { keyword: query.keyword, page: query.page, size: query.size },
    });
    return res.data;
  },
};
