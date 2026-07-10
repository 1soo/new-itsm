# 통합 테스트 시나리오 — Change (변경 상세 구현결과 폼 가드)

## 사전 조건
- 빌드 테스트 통과(공통, `docs/04_test/20260710-131137/scenario/common.md` TC-BUILD-001)
- POC 계정(비밀번호 공통 `Admin@1234`): cm@itsm.local(CHANGE_MANAGER)
- 시드 데이터(2026-07-10 기준): CHG-2026-0002(REVIEW, CAB 경로, 미승인), CHG-2026-0011(IMPLEMENTATION, CAB 경로, 승인 완료·구현결과 미기록), CHG-2026-0009(IMPLEMENTATION, AUTO 경로/표준 변경, 승인 이력 없음·이미 결과 기록됨)

## 시나리오

### TC-CHG-001 · 미승인 변경 — 구현 결과 폼 비활성화
- 근거: @docs/01_analyze/feature/change.md (FEAT-CHG-008 Unwanted), @docs/02_plan/screen/change.md (SCR-CHG-003)
- 전제: cm@itsm.local 로그인, CHG-2026-0002(REVIEW, CAB 경로, 승인 이력 없음) 상세 진입
- 절차:
  1. `/changes/{CHG-2026-0002 id}` 진입
  2. "구현 결과 기록" 폼(결과 셀렉트/롤백여부/비고/저장 버튼) 활성 상태 확인
  3. "승인 완료 전에는 구현 결과를 기록할 수 없습니다" 안내 문구 노출 확인
  4. 상태 전이 버튼 중 "구현" 전이 가능 여부 확인(회귀)
- 기대 결과: 폼 전체 비활성화(disabled), 안내 문구 노출, "구현" 상태 전이 버튼도 비활성/미노출

### TC-CHG-002 · CAB 승인 완료 변경 — 구현 결과 폼 정상 동작
- 근거: @docs/01_analyze/feature/change.md (FEAT-CHG-008), @docs/02_plan/screen/change.md (SCR-CHG-003)
- 전제: cm@itsm.local 로그인, CHG-2026-0011(IMPLEMENTATION, CAB 경로, 승인 완료·결과 미기록) 상세 진입
- 절차:
  1. `/changes/{CHG-2026-0011 id}` 진입
  2. "구현 결과 기록" 폼 활성 상태 확인(안내 문구 미노출)
  3. 결과=성공, 롤백 여부=아니오, 비고 입력 후 저장
- 기대 결과: 폼이 활성화되어 있고 저장 시 성공 토스트, 새로고침 후에도 기록된 결과가 유지됨

### TC-CHG-003 · 표준 변경(자동승인) — 구현 결과 폼 정상 동작
- 근거: @docs/01_analyze/feature/change.md (FEAT-CHG-006, FEAT-CHG-008), @docs/02_plan/screen/change.md (SCR-CHG-003)
- 전제: cm@itsm.local 로그인, CHG-2026-0009(IMPLEMENTATION, AUTO 경로/표준 변경) 상세 진입
- 절차:
  1. `/changes/{CHG-2026-0009 id}` 진입
  2. "구현 결과 기록" 폼 활성 상태 확인(안내 문구 미노출, 승인 이력이 없어도 AUTO 경로라 활성화되어야 함)
- 기대 결과: 폼 활성화(기존 기록값 성공/미롤백/비고가 정상 표시)

### TC-CHG-004 · 백엔드 우회 방어 — 미승인 변경에 강제 API 호출 시 거부
- 근거: @docs/01_analyze/feature/change.md (FEAT-CHG-008 Unwanted), @docs/02_plan/screen/change.md (SCR-CHG-003 — "승인 안 된 변경에 결과 기록 거부")
- 전제: cm@itsm.local 로그인 세션(Access Token 보유), CHG-2026-0002(미승인)
- 절차:
  1. 브라우저 devtools/콘솔에서 `POST /api/v1/changes/{CHG-2026-0002 id}/result`를 Authorization 헤더 포함해 직접 호출(예: `fetch`)
- 기대 결과: 400대 오류 응답(`CHANGE_NOT_APPROVED` 등)으로 거부, 200 성공 아님
