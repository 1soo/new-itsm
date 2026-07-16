---
date: 20260711-204816
domain: approval-engine
result: pass
keywords: [CHANGE 승인 게이트, 전이 버튼 숨김(FE), 승인 대기함 통합 조회]
---

# 통합 테스트 결과 — approval-engine (Stage 2: CHANGE 게이트 실연동) (20260711-204816)

## 요약
- 총 13건 · 성공 13 · 실패 0
- SRM 전이 버튼 숨김 처리(developer-fe-2 수정) 재확인 완료 — Stage 2 최종 완료

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `./gradlew build`, `npm run build` 모두 성공 | |
| TC-CHGADM-001 | PASS | `GET .../domains/CHANGE/request-subtypes` → STANDARD/NORMAL/EMERGENCY 3건 정확 | |
| TC-CHGGATE-001 | PASS | domain=CHANGE, requestSubtypeKey=NORMAL, tier=2 OR 규칙 생성 201 | |
| TC-CHGGATE-002 | PASS | NORMAL RFC(CHANGE_MANAGER 제출, 기존 tier=3 규칙 매칭) REVIEW→PLANNING→APPROVAL 통과 → IMPLEMENTATION 전이 시도 409 + approvalRequestId, 인스턴스 IN_PROGRESS 확인 | |
| TC-CHGGATE-003 | PASS | APPROVER `scope=mine`에 CHANGE 티켓(ticketType=CHANGE, ticketKey/summary/requester) 정상 노출, 기존 SRM 항목과 함께 정상 집계 | |
| TC-CHGGATE-004 | PASS | APPROVER 승인 → requestStatus=APPROVED → IMPLEMENTATION 재시도 200 → 상세 `approval.status`=APPROVED | |
| TC-CHGGATE-005 | PASS | EMERGENCY RFC(SYSTEM_ADMIN 제출, CHANGE_MANAGER 역할 없어 tier=3 미매칭, 별도 규칙 없음) REVIEW~IMPLEMENTATION 전 구간 게이트 없이 통과 | |
| TC-CHGUI-001 | PASS | 승인 대기 중 "구현" 버튼 `disabled=true` + `title="승인 완료 전에는 구현 단계로 전이할 수 없습니다"` 툴팁 확인(SRM과 의도된 차이) | |
| TC-CHGUI-002 | PASS | "구현" 클릭(409 유발) 즉시 토스트 "승인 대기 중에는 이행할 수 없습니다." 노출 + 승인 패널이 새로고침 없이 "1차·대기중"으로 즉시 갱신 | |
| TC-CHGREG-001 | PASS | 목록/생성/분류(API-CHG-005)/일정/지표 전부 정상(일정은 ISO-8601 datetime 파라미터 필요 — 문서 표기와 일치, 결함 아님) | |
| TC-SRMREG-001 | PASS | SRM 게이트 정상 차단(409+토스트) 및 승인 패널 상태 정상 유지, 회귀 없음. 최초 확인 시 "이행 중" 버튼이 숨김 처리 없이 노출됐던 부분은 developer-fe-2가 수정(승인 대기 중이면 FE에서 전이 버튼 자체를 필터링) 완료, 재확인 결과 승인 대기 중인 SRM-2026-0006 상세에서 "이행 중" 버튼이 더 이상 노출되지 않음(헤더 액션 영역에 전이 버튼 없음, 승인 패널은 "1차·대기중" 그대로 정상 표시) 확인 | |
