import type { StatusTone } from "@/components/common";
import type { SearchDomain } from "@/features/search/types";
import { statusLabel as knowledgeStatusLabel, statusTone as knowledgeStatusTone } from "@/features/knowledge/status";
import { statusLabel as srStatusLabel, statusTone as srStatusTone } from "@/features/service-request/status";
import { statusLabel as incidentStatusLabel, statusTone as incidentStatusTone } from "@/features/incident/status";
import { statusLabel as problemStatusLabel, statusTone as problemStatusTone } from "@/features/problem/status";
import { statusLabel as changeStatusLabel, statusTone as changeStatusTone } from "@/features/change/status";
import type { ArticleStatus } from "@/features/knowledge/types";
import type { SrStatus } from "@/features/service-request/types";
import type { IncidentStatus } from "@/features/incident/types";
import type { ProblemStatus } from "@/features/problem/types";
import type { ChangeStatus } from "@/features/change/types";

/* SEARCH 결과 도메인 배지·상태 배지 표시 매핑 — 각 도메인 기존 status.ts를 재사용(common.md SCR-COM-011). */

const DOMAIN_LABEL: Record<SearchDomain, string> = {
  KNOWLEDGE: "지식",
  SERVICE_REQUEST: "서비스 요청",
  INCIDENT: "인시던트",
  PROBLEM: "문제",
  CHANGE: "변경",
};

export function domainLabel(domain: SearchDomain): string {
  return DOMAIN_LABEL[domain] ?? domain;
}

/** 결과 상태 배지 — 도메인별 기존 statusLabel/statusTone 재사용(원문 상태값 매핑 실패 시 원문 그대로 표시). */
export function resultStatusLabel(domain: SearchDomain, status: string): string {
  switch (domain) {
    case "KNOWLEDGE":
      return knowledgeStatusLabel(status as ArticleStatus);
    case "SERVICE_REQUEST":
      return srStatusLabel(status as SrStatus);
    case "INCIDENT":
      return incidentStatusLabel(status as IncidentStatus);
    case "PROBLEM":
      return problemStatusLabel(status as ProblemStatus);
    case "CHANGE":
      return changeStatusLabel(status as ChangeStatus);
    default:
      return status;
  }
}

export function resultStatusTone(domain: SearchDomain, status: string): StatusTone {
  switch (domain) {
    case "KNOWLEDGE":
      return knowledgeStatusTone(status as ArticleStatus);
    case "SERVICE_REQUEST":
      return srStatusTone(status as SrStatus);
    case "INCIDENT":
      return incidentStatusTone(status as IncidentStatus);
    case "PROBLEM":
      return problemStatusTone(status as ProblemStatus);
    case "CHANGE":
      return changeStatusTone(status as ChangeStatus);
    default:
      return "muted";
  }
}
