/* asset(ITAM) 도메인 타입 — api_spec/asset.md 계약 기준. */

export type AssetType = "HARDWARE" | "SOFTWARE" | "CLOUD";
export type AssetStatus = "PLANNING" | "PROCUREMENT" | "OPERATION" | "MAINTENANCE" | "RETIREMENT";
export type ExpiryStatus = "OK" | "EXPIRING" | "EXPIRED";
export type TicketType = "SERVICE_REQUEST" | "INCIDENT" | "PROBLEM" | "CHANGE";
export type CiRelationType = "DEPENDS_ON" | "RUNS_ON" | "CONNECTS_TO";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface AssetSummary {
  id: number;
  assetKey: string;
  name: string;
  type: AssetType;
  status: AssetStatus;
  owner: string;
  expiryDate: string | null;
  expiryStatus: ExpiryStatus;
}

export interface ExpiryField {
  date: string | null;
  status: ExpiryStatus | null;
}

export interface AssetExpiry {
  license: ExpiryField;
  warranty: ExpiryField;
  contract: ExpiryField;
}

export interface LifecycleEntry {
  stage: string;
  at: string;
}

export interface LinkedTicket {
  type: TicketType;
  ticketKey: string;
}

export interface LinkedCi {
  ciId: number;
  name: string;
}

/** 승인 프로세스 커스텀 기능(유지보수 요청) — approvalRequestId=null이면 매칭되는 승인 프로세스가 없어 게이트 없이 진행. */
export interface AssetApproval {
  approvalRequestId: number | null;
  status: "IN_PROGRESS" | "APPROVED" | "REJECTED" | null;
}

export interface AssetDetail {
  id: number;
  assetKey: string;
  name: string;
  type: AssetType;
  status: AssetStatus;
  owner: string;
  location: string;
  attributes: Record<string, string>;
  expiry: AssetExpiry;
  approval: AssetApproval;
  lifecycleHistory: LifecycleEntry[];
  linkedTickets: LinkedTicket[];
  linkedCis: LinkedCi[];
}

export interface AssetInput {
  name: string;
  type: AssetType;
  owner?: string;
  location?: string;
  purchaseDate?: string;
  cost?: number;
  licenseExpiry?: string;
  warrantyExpiry?: string;
  contractExpiry?: string;
  attributes?: Record<string, string>;
}

export interface CreatedAsset {
  id: number;
  assetKey: string;
  status: AssetStatus;
}

export interface AssetListQuery {
  type?: AssetType;
  status?: AssetStatus;
  owner?: string;
  expiringWithinDays?: number;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface LinkInput {
  ticketType: TicketType;
  ticketId: number;
}

export interface Ci {
  id: number;
  name: string;
  type: string;
}

export interface CiInput {
  name: string;
  type?: string;
  assetId?: number;
}

export interface CreatedCi {
  id: number;
  name: string;
}

export interface RelationInput {
  targetCiId: number;
  relationType: CiRelationType;
}

export interface ImpactItem {
  ciId: number;
  name: string;
  relationType: string;
  depth: number;
}

export interface AssetMetrics {
  utilizationRate: number;
  expiringCount: number;
  typeDistribution: Record<AssetType, number>;
}
