import type { TicketType } from "@/features/common/types";

/* 승인 대상 티켓 유형(TicketType) 표시 라벨·상세 경로 매핑 — 헤더 알림(SCR-COM-002)·공용 승인 대기함(SCR-COM-014) 공유. */

const TICKET_TYPE_APPROVAL_LABEL: Record<TicketType, string> = {
  SERVICE_REQUEST: "서비스요청 승인",
  CHANGE: "변경 승인",
  KNOWLEDGE: "지식 승인",
  INCIDENT: "인시던트 승인",
  PROBLEM: "문제 승인",
  ASSET: "자산 승인",
  VULNERABILITY: "취약점 승인",
  COMPLIANCE: "컴플라이언스 승인",
  ESM: "부서 서비스 승인",
};

const TICKET_TYPE_LABEL: Record<TicketType, string> = {
  SERVICE_REQUEST: "서비스요청",
  CHANGE: "변경",
  KNOWLEDGE: "지식",
  INCIDENT: "인시던트",
  PROBLEM: "문제",
  ASSET: "자산",
  VULNERABILITY: "취약점",
  COMPLIANCE: "컴플라이언스",
  ESM: "부서 서비스",
};

const TICKET_TYPE_DETAIL_PATH: Record<TicketType, (ticketId: number) => string> = {
  SERVICE_REQUEST: (id) => `/service-requests/${id}`,
  CHANGE: (id) => `/changes/${id}`,
  KNOWLEDGE: (id) => `/knowledge/${id}`,
  INCIDENT: (id) => `/incidents/${id}`,
  PROBLEM: (id) => `/problems/${id}`,
  ASSET: (id) => `/assets/${id}`,
  VULNERABILITY: (id) => `/vulnerabilities/${id}`,
  COMPLIANCE: (id) => `/compliance/requirements/${id}`,
  ESM: (id) => `/esm/requests/${id}`,
};

/** 헤더 알림 드롭다운 1행 좌측 도메인 라벨(common.md SCR-COM-002 매핑표). */
export function ticketTypeApprovalLabel(type: TicketType): string {
  return TICKET_TYPE_APPROVAL_LABEL[type] ?? type;
}

/** SCR-COM-014 목록/필터의 티켓 유형 표시 라벨. */
export function ticketTypeLabel(type: TicketType): string {
  return TICKET_TYPE_LABEL[type] ?? type;
}

/** 승인 항목 클릭·"상세 보기" 이동 경로. */
export function ticketDetailPath(type: TicketType, ticketId: number): string {
  const build = TICKET_TYPE_DETAIL_PATH[type];
  return build ? build(ticketId) : "/";
}
