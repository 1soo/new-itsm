import type { TFunction } from "i18next";

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
  ESM_REQUEST: "부서 서비스 승인",
  CORRECTIVE_ACTION: "컴플라이언스 시정조치 승인",
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
  ESM_REQUEST: "부서 서비스",
  CORRECTIVE_ACTION: "시정조치",
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
  ESM_REQUEST: (id) => `/esm/requests/${id}`,
  // ticketId가 시정조치 id라 소속 요구사항 id를 알 수 없어 요구사항 목록으로 연결한다.
  CORRECTIVE_ACTION: () => `/compliance/requirements`,
};

/** 헤더 알림 드롭다운 1행 좌측 도메인 라벨(common.md SCR-COM-002 매핑표, 6.4절 `common:notification.domainLabel.*`). */
export function ticketTypeApprovalLabel(t: TFunction, type: TicketType): string {
  const defaultValue = TICKET_TYPE_APPROVAL_LABEL[type] ?? type;
  return t(`notification.domainLabel.${type}`, { ns: "common", defaultValue });
}

/** SCR-COM-014 목록/필터의 티켓 유형 표시 라벨(신규 제안 키 `common:approval.domainOption.*`). */
export function ticketTypeLabel(t: TFunction, type: TicketType): string {
  const defaultValue = TICKET_TYPE_LABEL[type] ?? type;
  return t(`approval.domainOption.${type}`, { ns: "common", defaultValue });
}

/** 승인 항목 클릭·"상세 보기" 이동 경로. */
export function ticketDetailPath(type: TicketType, ticketId: number): string {
  const build = TICKET_TYPE_DETAIL_PATH[type];
  return build ? build(ticketId) : "/";
}
