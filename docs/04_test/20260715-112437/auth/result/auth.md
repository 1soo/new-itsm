# 통합 테스트 결과 — auth (20260715-112437)

## 요약
- 총 6건 · 성공 6 · 실패 0. 시나리오 수행 중 발견한 결함 1건(아래 "추가 발견 사항")은 developer-fe 수정 완료 후 재테스트 결과 정상 동작 확인됨(FIXED)

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew compileJava -q`, `npm run build` 모두 성공 | |
| TC-AUDIT-ACTOR-001 | PASS | `qa.audit@itsm.local` 계정 생성 후 감사 로그(`/admin/audit-logs`, 필터 "계정/역할 변경") 최상단에 "계정 변경" 이벤트가 `actor=admin@itsm.local`, `target=qa.audit@itsm.local`로 서로 다르게 기록됨 확인 | |
| TC-AUDIT-ACTOR-002 | PASS | `qa.audit@itsm.local`에 `SERVICE_DESK_AGENT` 역할 부여 후 회수, 두 "역할 변경" 이벤트 모두 `actor=admin@itsm.local`, `target=qa.audit@itsm.local`로 기록됨 확인 | |
| TC-AUDIT-ACTOR-003 | PASS | `qa.audit@itsm.local` 비활성화 처리, "계정 변경" 이벤트가 `actor=admin@itsm.local`, `target=qa.audit@itsm.local`로 기록됨 확인(대상 계정 자신이 actor로 오기록되던 기존 결함이 재현되지 않음) | |
| TC-APPR-CREATE-DELETE-001 | PASS | "전체 도메인" 선택 시 요청 유형 select가 화면에서 사라짐(도메인 미지정 시 하위유형 필드 비노출) 확인. 규칙 생성 후 목록에 도메인="전체"·요청유형="전체"·우선순위 배지="전체 적용" 정확히 표시됨. 삭제 버튼 클릭 → 확인 다이얼로그(진행 중 인스턴스 무관 안내 포함) → 확인 시 목록에서 즉시 제거됨 | 아래 "추가 발견 사항" 참조(요청자 박스 역할 0개로는 저장 불가해 승인자 박스에만 역할을 지정한 채 시나리오를 진행함) |
| TC-APPR-EDIT-001 | PASS | 기존 "노트북 신청 승인 규칙"(도메인=SERVICE_REQUEST, 요청유형=노트북 신청) 편집 화면 진입 시 도메인 select="서비스 요청", 요청 유형 select="노트북 신청"으로 정상 표시됨(빈칸 아님). 저장 없이 이탈해 원본 데이터 변경 없음 | |

## 추가 발견 사항 — SCR-ADMIN-008 승인 요청자 박스가 "역할 0개(전체 요청자)"를 저장할 수 없음 (FIXED, 재테스트 완료)

- **증상(최초 발견)**: TC-APPR-CREATE-DELETE-001 수행 중, "1단계 · 승인 요청자" 박스에 역할을 선택하지 않고(0개) "생성 완료"를 클릭하면 "역할을 1개 이상 선택하세요" 인라인 오류가 표시되며 저장이 차단됨(`source/frontend/src/components/common/approval-process-flow.tsx` `hasEmptyBox` 검증이 요청자 박스에도 동일하게 적용됨 — L182 `requester.roleIds.length === 0 || approvers.some(...)`).
- **영향**: `docs/02_plan/database/common.md`(approval_process 4절)·`docs/02_plan/api_spec/auth.md`(API-AUTH-027)는 요청자 역할 축을 비워(0개) "전체 요청자"로 저장하는 것을 3축 우선순위 재설계의 핵심 전제로 명시하며, 이 경우 `priorityTier`는 0(전체 미지정)/11(도메인만)/23(도메인+요청유형)이 된다. 결함 상태에서는 SCR-ADMIN-008 화면에서 tier=0/11/23에 해당하는 규칙을 직접 생성할 방법이 없었음(요청자 역할 축이 항상 채워지므로 tier=14/25/37만 생성 가능했음).
- **수정 내용(developer-fe)**: `hasEmptyBox` → `hasEmptyApproverBox`로 변경해 승인자 박스만 검사하도록 수정(요청자는 0개=전체 허용이 정상 상태). tsc/build 통과 확인.
- **재테스트 결과(FIXED 확인)**: `/admin/approval-processes/new`에서 요청자 박스를 역할 미선택(0개) 상태로 둔 채 아래 3개 조합을 각각 UI로 생성 시도 → 전부 정상 저장(성공 토스트, 목록 반영) 확인.
  - 전체 도메인 + 승인자=APPROVER → 저장 성공, DB `priority_tier=0`, 목록 배지 "전체 적용"
  - 도메인=서비스 요청(요청유형=전체) + 승인자=SERVICE_DESK_AGENT → 저장 성공, DB `priority_tier=11`, 목록 배지 "도메인"
  - 도메인=서비스 요청 + 요청유형=비밀번호 초기화 + 승인자=PROCESS_OWNER → 저장 성공, DB `priority_tier=23`, 목록 배지 "도메인+요청유형"
  - 3건 모두 테스트 후 삭제로 정리, 환경은 사전 데이터(tier=37 규칙 1건)만 남은 상태로 복원됨
- **요청유형 축은 원래부터 정상**: 요청유형은 "전체" 선택지가 select에 있어 0개(무관) 저장이 가능했음(NO_SUBTYPE). 도메인 축도 "전체 도메인" 의사 옵션으로 원래부터 정상 처리됨.
- 공용 승인 엔진 자체의 우선순위 매칭·409 충돌 검증 로직은 (common 도메인 결과의 TC-PRI-001~005 참조) 이번 결함 발견 당시 이미 API 경유로 정상 동작 확인했었음 — 이번 수정으로 해당 로직을 SCR-ADMIN-008 화면에서도 동일하게 이용 가능해짐.

## 실패 항목 분석
없음(전 TC 통과, 추가 발견 결함도 수정·재검증 완료).
