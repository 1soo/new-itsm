import { apiClient } from "@/lib/apiClient";
import type {
  AssetDetail,
  AssetInput,
  AssetListQuery,
  AssetMetrics,
  AssetStatus,
  AssetSummary,
  Ci,
  CiInput,
  CreatedAsset,
  CreatedCi,
  ImpactItem,
  LinkInput,
  PageResponse,
  RelationInput,
} from "@/features/asset/types";

/* ITAM API 호출 — 모두 공통 apiClient 경유. api_spec/asset.md 준수. */

function cleanParams<T extends object>(query: T): Record<string, unknown> {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(query as Record<string, unknown>)) {
    if (value !== undefined && value !== null && value !== "") params[key] = value;
  }
  return params;
}

export const assetApi = {
  // API-ITAM-001 자산 목록
  async list(query: AssetListQuery): Promise<PageResponse<AssetSummary>> {
    const res = await apiClient.get<PageResponse<AssetSummary>>("/assets", {
      params: cleanParams(query),
    });
    return res.data;
  },

  // API-ITAM-002 자산 등록 (Asset Manager)
  async create(body: AssetInput): Promise<CreatedAsset> {
    const res = await apiClient.post<CreatedAsset>("/assets", body);
    return res.data;
  },

  // API-ITAM-003 자산 상세
  async get(id: number): Promise<AssetDetail> {
    const res = await apiClient.get<AssetDetail>(`/assets/${id}`);
    return res.data;
  },

  // API-ITAM-004 자산 수정 (Asset Manager)
  async update(id: number, body: Partial<AssetInput>): Promise<void> {
    await apiClient.patch(`/assets/${id}`, body);
  },

  // API-ITAM-005 생애주기 단계 전이 (Asset Manager)
  async transition(id: number, targetStage: AssetStatus): Promise<{ id: number; status: string }> {
    const res = await apiClient.patch<{ id: number; status: string }>(
      `/assets/${id}/lifecycle`,
      { targetStage },
    );
    return res.data;
  },

  // API-ITAM-006 자산 폐기 (Asset Manager)
  async retire(id: number): Promise<{ id: number; status: string }> {
    const res = await apiClient.patch<{ id: number; status: string }>(`/assets/${id}/retire`);
    return res.data;
  },

  // API-ITAM-007 자산 티켓 연계
  async link(id: number, body: LinkInput): Promise<void> {
    await apiClient.post(`/assets/${id}/links`, body);
  },

  // API-ITAM-008 CI 목록
  async listCis(params: { keyword?: string; type?: string; page?: number; size?: number } = {}): Promise<{ content: Ci[]; totalElements: number }> {
    const res = await apiClient.get<{ content: Ci[]; totalElements: number }>("/cis", {
      params: cleanParams(params),
    });
    return res.data;
  },

  // API-ITAM-009 CI 등록
  async createCi(body: CiInput): Promise<CreatedCi> {
    const res = await apiClient.post<CreatedCi>("/cis", body);
    return res.data;
  },

  // API-ITAM-010 CI 관계 등록
  async createRelation(id: number, body: RelationInput): Promise<void> {
    await apiClient.post(`/cis/${id}/relations`, body);
  },

  // API-ITAM-011 CI 영향 범위 조회
  async impact(id: number): Promise<ImpactItem[]> {
    const res = await apiClient.get<ImpactItem[]>(`/cis/${id}/impact`);
    return res.data;
  },

  // API-ITAM-012 자산 지표
  async metrics(params: { from?: string; to?: string } = {}): Promise<AssetMetrics> {
    const res = await apiClient.get<AssetMetrics>("/assets/metrics", {
      params: cleanParams(params),
    });
    return res.data;
  },
};
