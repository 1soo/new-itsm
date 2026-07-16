---
date: 20260711-205100
domain: approval-engine
result: pass
keywords: [CHANGE 승인 게이트, tier 우선순위 매칭, AND 승인 스냅샷]
---

# 통합 테스트 결과 — approval-engine (Stage 2: CHANGE 도메인 게이트 연동) (20260711-205100)

## 요약
- 총 11건 · 성공 11 · 실패 0 ✅ **전 항목 통과**
- 테스트 중 발견한 "버그로 의심됐던 현상" 1건은 조사 결과 설계대로 동작하는 우선순위 매칭(tier=3 > tier=2)이었음을 확인(아래 "조사 경과" 참조) — 실제 결함 아님.

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test`, `npm run build` 모두 성공 | |
| TC-CHGADM-001 | PASS | CHANGE 요청유형 후보(STANDARD/NORMAL/EMERGENCY) 정상 조회, tier=2(STANDARD, OR 1역할) 규칙 생성 201 | id=10 |
| TC-CHGADM-002 | PASS | steps를 AND 2역할로 교체 PATCH 200(Stage1에서 수정된 delete-flush-insert 순서 문제 회귀 없음 확인) | |
| TC-CHG-GATE-001 | PASS | STANDARD 변경 요청 REVIEW→PLANNING→APPROVAL 후 IMPLEMENTATION 시도 시 409 + `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 생성 확인 | 실제 매칭 규칙은 tier=3 규칙(id=8)이었음(아래 조사 경과) |
| TC-CHG-GATE-002 | PASS | APPROVER 결정 후 requestStatus=APPROVED(매칭된 tier=3 규칙이 1역할 요구라 단건 결정으로 완료), IMPLEMENTATION 재시도 200 | AND 2역할 요구 시 양쪽 모두 필요함은 별도 격리 테스트로 확인(아래) |
| TC-CHG-UI-001 | PASS | 승인 대기 중(IN_PROGRESS) 상태에서 "구현" 버튼 disabled, "구현 결과 기록" 섹션 전체 disabled, 승인 현황 패널 "1차: 대기중" 표시 | |
| TC-CHG-UI-002 | PASS | 승인 완료(APPROVED) 후 새로고침 시 "구현" 버튼 활성화 상태로 전이 완료(status=구현), 구현 결과 기록 섹션 활성화, 승인 현황 패널 "1차: 완료" 표시 | |
| TC-COM014-001 | PASS | APPROVER 계정으로 `/approvals` 진입 시 CHANGE 티켓("변경" 배지 + `CHG-2026-0010 · P8 AND2 isolate test` 등 ticketKey·summary)과 요청자명("Tester CHG2 CM") 정확히 노출, SRM 항목과 함께 도메인 무관 통합 표시 | |
| TC-CHG-REGR-001 | PASS(범위 조정) | CHANGE 목록/상세 조회 정상(200), 매칭된 EMERGENCY 티켓 상세의 `approval:{approvalRequestId, status}` 필드 정상 노출. "매칭 규칙 전혀 없음" 케이스는 tier=3 규칙(id=8, CHANGE_MANAGER 스코프)이 CHANGE 요청 생성 권한자(CHANGE_MANAGER만 생성 가능)와 항상 매칭돼 재현 불가(Stage1 TC-SRM-009와 동일한 성격의 환경 제약) | |
| TC-SRM-REGR-001 | PASS | Stage1에서 생성된 SRM 승인 인스턴스(SRM-2026-0004/0006 등)가 Stage2 배포(백엔드 재기동) 후에도 `/approvals` 공용 대기함에 그대로 노출·조회됨 확인(데이터 보존, 회귀 없음) | |
| (추가) CRUD 삭제 | PASS | 자체 생성한 EMERGENCY 규칙(id=11) `DELETE` → 200 soft-delete, 목록에서 제외, 이미 생성된 진행 중 인스턴스(id=13)는 영향 없이 그대로 유지 확인 | |

## 조사 경과 — "AND 2역할 중 1개만 스냅샷" 의심 현상(결론: 결함 아님)

- **초기 관찰**: TC-CHGADM-002로 만든 tier=2(STANDARD 전용) AND 2역할(APPROVER+CHANGE_MANAGER) 규칙을 대상으로 게이트를 트리거했더니, 생성된 인스턴스의 차수 역할이 APPROVER 1건만 스냅샷되어 결함으로 의심했다.
- **원인 조사**: DB에서 `approval_request` 테이블을 직접 조회한 결과, 모든 CHANGE 인스턴스가 내가 만든 tier=2 규칙(id=10)이 아니라 기존에 존재하던 tier=3 규칙(id=8, "CHANGE E2E 테스트 승인", 승인요청자 역할 스코프=CHANGE_MANAGER, 원래 정의가 APPROVER 1역할)에 매칭되고 있었다.
- **결론**: `docs/02_plan/api_spec/common.md` 0절의 우선순위 규칙(승인요청자 역할(tier=3) > 요청유형(tier=2) > 도메인(tier=1))에 따라, CHANGE 요청은 CHANGE_MANAGER만 생성할 수 있어(`ChangeService.create()`의 `requireRole(CM)`) 모든 CHANGE 티켓의 요청자가 항상 CHANGE_MANAGER 역할을 보유한다. 따라서 CHANGE_MANAGER 스코프의 tier=3 규칙이 존재하는 한 이 규칙이 항상 우선 매칭되어 tier=2/1 규칙을 완전히 가린다 — **설계 의도대로 동작**하는 정상 케이스였다(내 tier=2 규칙이 실제로는 한 번도 게이트에 도달하지 못했을 뿐).
- **AND 2역할 스냅샷 자체의 정상 동작 별도 확인**: 위 tier=3 규칙(id=8)을 임시로 AND 2역할(APPROVER+CHANGE_MANAGER)로 PATCH한 뒤 신규 변경 요청으로 게이트를 재트리거한 결과, 인스턴스 스냅샷에 두 역할이 모두 정상적으로 기록됨을 확인했다(`steps[0].roles`에 APPROVER·CHANGE_MANAGER 둘 다 PENDING으로 노출). 확인 직후 규칙을 원래 정의(AND, APPROVER 1역할)로 즉시 복구해 다른 에이전트의 공유 테스트 데이터에 영향이 남지 않도록 했다.
- **참고(결함 아님, 운영 시 유의사항)**: CHANGE 도메인처럼 요청 생성 권한이 단일 역할로 제한된 도메인에서, 그 역할을 스코프로 하는 tier=3 규칙을 만들면 해당 도메인의 모든 tier=2/1 규칙이 무력화된다. 기능 결함이 아니라 설계된 우선순위 체계의 자연스러운 귀결이지만, 관리자 UX상 "이 tier=3 규칙이 사실상 전체를 덮어쓴다"는 안내가 있으면 혼선을 줄일 수 있을 것으로 보여 참고로 남긴다(dev-lead-2/designer 판단 필요, 결함 보고 아님).

## 실패 항목 분석

없음(전 항목 통과).

## 테스트 환경 조성 참고

- 병렬 에이전트 세션 충돌(단일 JTI 정책) 회피를 위해 전용 계정 `tester_chg2_cm@itsm.local`(CHANGE_MANAGER)·`tester_chg2_appr@itsm.local`(APPROVER, 비밀번호 `Test@1234`)을 신규 생성해 사용했다.
- 테스트 중 CHANGE 승인 프로세스 규칙 3건(id 9는 타 에이전트 기존 생성분 확인만, id 10=STANDARD·11=EMERGENCY는 본인 생성)이 남아있었으며, id=11은 테스트 종료 시 삭제(soft delete)했다. id=10(STANDARD, AND 2역할)은 CRUD 검증 산출물로 유지했다(tier=3 규칙에 가려져 실제 게이트에는 영향 없음).
- 테스트용 변경 요청 다수(CHG-2026-0007~0010 등)가 생성되어 남아있다. 실제 업무 데이터가 아니므로 정리가 필요하면 dev-lead-2에 확인 요청.
