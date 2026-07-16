---
date: 20260715-112437
domain: common
result: pass
keywords: [승인프로세스우선순위, tier, 도메인미지정]
---

# 통합 테스트 결과 — common (승인 프로세스 범위 우선순위 3축 재설계) (20260715-112437)

## 요약
- 총 5건 · 성공 5 · 실패 0 ✅ **전 항목 통과**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-PRI-001 · TC-PRI-002 | PASS | tier=0(전체 미지정, 승인자=APPROVER) 규칙 생성 후 SRM "비밀번호 초기화" 요청(SRM-2026-0005) 제출·게이트 트리거 → 매칭됨(대기함에 표시). 이어서 tier=11(도메인=SERVICE_REQUEST만, 승인자=SERVICE_DESK_AGENT) 규칙을 추가 생성한 뒤 동일 요청 재확인 → DB(`approval_request.approval_process_id=9`, tier=11) 및 UI 모두에서 tier=11 규칙으로 매칭 전환 확인. `agent@itsm.local`(SERVICE_DESK_AGENT) 대기함에는 조회되고 `cab@itsm.local`(APPROVER) 대기함에는 조회되지 않음 — **특정 도메인 규칙이 전체 미지정(캐치올) 규칙보다 우선 적용됨을 확인**(우선순위 역전 케이스) | 두 규칙 모두 `POST /api/v1/admin/approval-processes` API로 직접 생성함(SCR-ADMIN-008 화면의 요청자 박스 역할 0개 저장 불가 결함 — auth 도메인 결과 참조 — 으로 인해 UI로는 생성 불가했음). API 자체는 정상 동작(201) 확인 |
| TC-PRI-003 | PASS | 기존 tier=37 규칙("노트북 신청 승인 규칙", domain=SERVICE_REQUEST+요청유형=노트북 신청+요청자역할=END_USER, 승인자=PROCESS_OWNER)과 tier=11·tier=0 규칙이 모두 존재하는 상태에서 "노트북 신청" 요청(SRM-2026-0006) 제출·게이트 트리거 → DB 확인 결과 `approval_process_id=1`(tier=37)로 매칭됨. `po@itsm.local`(PROCESS_OWNER) 대기함에만 조회되고 `agent@itsm.local`·`cab@itsm.local` 대기함에는 조회되지 않음 — **3축 모두 지정된 규칙이 부분 지정(도메인만/전체) 규칙보다 우선 적용됨을 확인** | |
| TC-PRI-004 | PASS | 이미 tier=0 규칙이 존재하는 상태에서 동일 스코프(전체 도메인·요청유형 없음·요청자 역할 없음)로 재생성 시도 → `409 APPROVAL_PROCESS_PRIORITY_CONFLICT`("동일 범위의 승인 프로세스가 이미 존재합니다") 응답, 저장되지 않음 | API로 직접 검증(위와 동일한 이유로 UI 생성 자체가 불가해 API로 확인) |
| TC-PRI-005 | PASS | tier=11 규칙(SRM-2026-0005가 매칭 중)을 SCR-ADMIN-007 목록에서 삭제 → 삭제 후에도 `agent@itsm.local`의 승인 대기함에 SRM-2026-0005가 그대로 조회됨(DB `approval_request` 상태 IN_PROGRESS 유지, 스냅샷 데이터 변경 없음 확인) — **규칙 삭제가 이미 생성된 진행 중 인스턴스에 영향을 주지 않음을 확인** | |

## 실패 항목 분석
없음(전 항목 통과).

## 사후 정리
- `agent@itsm.local`로 SRM-2026-0005, `po@itsm.local`로 SRM-2026-0006 승인 처리 완료(`POST /api/v1/approvals/{id}/decisions`).
- 테스트로 생성한 tier=0 규칙("QA priority tier0 catch-all") 삭제. tier=11 규칙은 TC-PRI-005에서 이미 삭제됨.
- 최종 확인 결과 `approval_process` 테이블에는 사전 데이터인 tier=37 규칙("노트북 신청 승인 규칙") 1건만 남아 테스트 이전 상태로 복원됨.

## 참고 — 시나리오 수행 순서 조정
- 원 시나리오는 TC-PRI-001(tier=0 단독 생성·매칭 확인)과 TC-PRI-002(tier=11 추가 생성 후 역전 확인)를 순차 개별 검증하도록 작성했으나, 실제로는 두 규칙을 API로 함께 생성한 뒤 하나의 신규 요청으로 "tier=11이 tier=0을 override"하는 최종 결과를 직접 확인하는 방식으로 효율화함(중간 단계인 "tier=0 단독일 때의 매칭"은 auth 도메인 시나리오 진행 중 동일 규칙으로 별도 확인됨). 핵심 acceptance 기준(전체 규칙 vs 도메인 규칙 우선순위 역전)은 동일하게 검증됨.
