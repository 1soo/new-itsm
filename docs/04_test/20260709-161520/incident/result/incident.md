---
date: 20260709-161520
domain: incident
result: pass
keywords: [상태전이, 역할배정RBAC, 에스컬레이션, 포스트모템, MTTx지표]
---

# 통합 테스트 결과 — incident (INC) (20260709-161520)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 계정(admin@itsm.local 생성): im(INCIDENT_MANAGER, uid47) / agent(SERVICE_DESK_AGENT, uid48) / user(END_USER, uid49)
> 범위: API-INC-001~011, 013 + FE E2E. **API-INC-012 문제연계는 범위 제외**(스텁 PROBLEM_LINK_UNAVAILABLE 400 = 정상 판정).

## 요약

- 총 **52건** (빌드 2 · 인증 1 · API/도메인 42 · FE E2E 7) · **성공 52 · 실패 0**.
- 실제 결함 **0건**. 최초 실행에서 4건이 FAIL로 표기됐으나 전부 **테스트 입력/판정 오류**로 확인되어 재판정 PASS (상세 하단).

## 상세 — 빌드/인증

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-BUILD-001 (BE gradlew test) | PASS | dev-lead 보고 107테스트 통과, 라이브 API 45항목으로 런타임 교차검증 |
| TC-BUILD-002 (FE npm run build) | PASS | tsc -b && vite build 성공(1833 modules, 오류 0, chunk-size 경고만) |
| TC-AUTH-001 (미인증 401) | PASS | GET /incidents 무토큰 → 401 UNAUTHENTICATED |

## 상세 — API/도메인 (im/agent/user 토큰)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-INC-001 등록 정상 | PASS | 201, ticketKey=INC-2026-####, status=NEW |
| TC-INC-002 요약 누락 | PASS | 400 VALIDATION_ERROR(summary) |
| TC-INC-003 심각도 누락 | PASS | 400 VALIDATION_ERROR(severity) |
| TC-INC-004 정의외 심각도(SEV9) | PASS | 400 |
| TC-INC-005 목록 조회 | PASS | 200, {content,page,size,totalElements}, 생성분 포함 |
| TC-INC-006 목록 필터(severity/status) | PASS | 200, 필터 반영 |
| TC-INC-007 상세 구조 | PASS | 200, severity/priority/status/responders/metrics/timeline/links/allowedTransitions (priority는 미설정 시 null 생략, 설정 후 노출) |
| TC-INC-008 없는 id 상세 | PASS | 404 INCIDENT_NOT_FOUND |
| TC-INC-009 심각도/우선순위 변경(IM) | PASS | 200 |
| TC-INC-009-RBAC agent | PASS | 403 ACCESS_DENIED (severity=IM 전용) |
| TC-INC-010 정의외 severity/priority | PASS | 400 (둘 다) |
| TC-INC-011 없는 id severity | PASS | 404 |
| TC-INC-012 허용 전이 NEW→IN_PROGRESS | PASS | 200 |
| TC-INC-013 IN_PROGRESS→CLOSED 불허 | PASS | 400 INVALID_STATUS_TRANSITION |
| TC-INC-013b NEW→CLOSED 불허 | PASS | 400 |
| TC-INC-014 종료 후 재전이 | PASS | RESOLVED→CLOSED 200 후 CLOSED→IN_PROGRESS 400 |
| TC-INC-015 IM TECH_LEAD 배정 | PASS | 200, responders 반영 |
| TC-INC-016 IM SCRIBE 추가 | PASS | 200 |
| TC-INC-017 agent 역할배정 | PASS | 403 (IM 전용) |
| TC-INC-018 user 역할배정 | PASS | 403 |
| TC-INC-019 없는 사용자 배정 | PASS | 404 ASSIGNEE_NOT_FOUND |
| TC-INC-020 없는 인시던트 배정 | PASS | 404 |
| TC-INC-021 에스컬레이션 HIERARCHICAL | PASS | 200, 이력 기록 |
| TC-INC-022 에스컬레이션 FUNCTIONAL | PASS | 200 |
| TC-INC-023 대상 없음 | PASS | 400 ESCALATION_TARGET_NOT_FOUND |
| TC-INC-024 없는 인시던트 | PASS | 404 |
| TC-INC-025 타임라인 INTERNAL | PASS | 201, {id,at} |
| TC-INC-026 타임라인 EXTERNAL | PASS | 201 |
| TC-INC-027 user 업데이트 | PASS | 403 |
| TC-INC-028 없는 인시던트 업데이트 | PASS | 404 |
| TC-INC-029 해결·MTTx 계산 | PASS | 200 RESOLVED, metrics{mttd10,mtta40,mttr50} |
| TC-INC-029-RBAC agent resolve | PASS | 403 (resolve=IM 전용) |
| TC-INC-030 시각 일부 누락 | PASS | 200, 미산정 지표 null 생략(metrics={}) — 미산정 의미 충족 |
| TC-INC-031 없는 인시던트 해결 | PASS | 404 |
| TC-INC-032 미작성 PM 조회 | PASS | 404 POSTMORTEM_NOT_FOUND |
| TC-INC-033 PM 작성(PUT) | PASS | 200, 인시던트 연결 |
| TC-INC-034 PM 조회 | PASS | 200, rootCause/fiveWhys/actionItems 저장 확인 |
| TC-INC-035 rootCause 누락 | PASS | 400 ROOT_CAUSE_REQUIRED |
| TC-INC-036 PM 수정(재PUT) | PASS | 200, 갱신 반영 |
| TC-INC-011-RBAC agent PM PUT | PASS | 403 (PM PUT=IM 전용) |
| TC-INC-037 postmortemRequired | PASS | 목록에서 SEV1/2 해결·PM미작성=true → PM 작성 후 false |
| TC-INC-038 지표 조회 | PASS | 200, {count, severityDistribution{SEV1/2/3}, avgMttrMinutes} |
| TC-INC-039 빈 기간 조회 | PASS | ISO-8601 datetime(from/to)로 200, count=0 빈 결과 |

## 상세 — FE E2E (playwright)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-E2E-001 등록(SCR-INC-002) | PASS | 요약/심각도 입력 등록 → 상세(INC-2026-0009) 이동 |
| TC-E2E-002 목록(SCR-INC-001) | PASS | 필터(상태·심각도·담당자·키워드)·SEV/상태 배지·PM 필요 태그 표시 |
| TC-E2E-003 상세(SCR-INC-003) | PASS | 역할배정/에스컬레이션/해결/타임라인/심각도·우선순위 패널; 상태 전이(NEW→대응중) 후 타임라인 기록 |
| TC-E2E-004 해결·MTTx | PASS | 해결 처리 폼 제출 → RESOLVED, MTTx 계산(mttd10/mtta45/mttr55) 지속 확인(API 교차검증); 미설정 시 "미산정" 표시 |
| TC-E2E-005 포스트모템(SCR-INC-004) | PASS | rootCause 미입력 저장 차단(toast "근본 원인은 필수입니다.") → 입력 후 저장 성공, 상세 복귀 |
| TC-E2E-006 지표 대시보드(SCR-INC-005) | PASS | 기간필터·KPI(건수/평균MTTR)·심각도 분포 차트(SEV1/2/3 막대) 렌더 |
| TC-E2E-007 비-IM RBAC(agent) | PASS | 상세에서 역할배정·해결 패널 비노출, 심각도 저장 disabled; 에스컬레이션·상태업데이트는 노출(허용역할) |

## 최초 FAIL → 재판정 PASS 근거 (실제 결함 아님)

- **TC-INC-007**: 상세에 `priority` 키가 없어 FAIL로 표기 → 미설정 시 null 필드 JSON 생략(전 API 일관 동작). priority 설정 후 재조회 시 정상 노출 확인 → PASS.
- **TC-INC-030**: 부분 시각 해결 시 `metrics={}` → mttd/mtta/mttr가 null이라 생략된 것으로, REQ-INC-007 "미산정" 의미 충족(FE는 "미산정" 표기) → PASS.
- **TC-INC-037**: `postmortemRequired`를 상세(API-INC-003)에서 조회해 None → 이 필드는 **목록(API-INC-001)** 전용. 목록에서 확인 시 true→false 정상 → PASS.
- **TC-INC-039**: date-only(`2020-01-01`) 입력으로 400 → 계약상 ISO-8601 **datetime** 필요. `...T00:00:00Z`로 재요청 시 200 count=0 → PASS.

## 환경 관찰(결함 아님)

- 단일 공유 playwright 브라우저를 다른 에이전트(FE E2E)가 동시 사용해 로그인 세션이 수차례 교체됨. 항목별로 storage 초기화·재로그인으로 격리했고, UI 액션 지속 여부는 API로 교차검증하여 결과 신뢰성 확보.
- 포스트모템 편집 진입 시 미작성 인시던트에 대한 `GET .../postmortem` 404 콘솔 오류는 정상 동작(TC-INC-032)로, 결함 아님.

## 결론

- incident 도메인 통합테스트 **전 항목 통과(실패 0)**. 상태전이·역할배정(IM 전용)·에스컬레이션·MTTx·포스트모템(rootCause 필수/postmortemRequired)·지표 모두 명세대로 동작.
- API-INC-012 문제연계는 지시대로 범위 제외(스텁 400 정상).
