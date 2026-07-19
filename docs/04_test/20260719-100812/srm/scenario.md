# 통합 테스트 시나리오 — srm (버그 수정 재테스트: labels 없는 구 스키마 로드 시 크래시)

## 사전 조건
- 빌드 테스트 통과(FE `npm run build`. 설계 변경 없는 순수 버그 수정, API/DB 무관)
- END_USER 계정(`user@itsm.local` / `Admin@1234`, 요청 제출)
- Process Owner 계정(`po@itsm.local` / `Admin@1234`, A1 축소 미리보기)
- playwright는 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-BUGFIX-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 오류 없이 빌드 성공

### TC-BUGFIX-001 · 구 스키마(labels 없음) 요청 제출 폼 크래시 없음
- 근거: @docs/03_develop/plan/service-request.md (버그 수정 절, 완료 기준 1번)
- 절차:
  1. "GF2 인터랙티브 테스트"(id=12) 카탈로그 항목의 요청 제출 폼(SCR-SRM-002, `/portal/requests/new?item=12`)을 연다
  2. 크래시(흰 화면/에러 바운더리) 없이 폼이 정상 렌더링되는지 확인
- 기대 결과: 크래시 없이 정상 렌더링(3차 이전 구 guide 타입 필드의 시각적 오표시는 이번 범위 아니므로 실패로 처리하지 않음)

### TC-BUGFIX-002 · 구 스키마 A1 축소 미리보기 크래시 없음
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 1번)
- 절차:
  1. Process Owner로 SCR-SRM-007에서 "GF2 인터랙티브 테스트" 항목을 선택
  2. A1 축소 미리보기 영역이 크래시 없이 정상 렌더링되는지 확인
- 기대 결과: 크래시 없이 정상 렌더링

### TC-BUGFIX-003 · labels 있는 항목 라벨 경계 오버레이 회귀 없음
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 2번)
- 절차:
  1. 4차 이후 라벨을 지정해 저장한 카탈로그 항목(예: 8차 테스트에서 생성한 "GF8 라벨확대 placeholder 기본값 테스트")의 요청 제출 폼과 A1 축소 미리보기를 연다
  2. 라벨 경계 테두리+legend 텍스트가 기존처럼 정상 표시되는지 확인
- 기대 결과: labels 필드가 있는 항목은 오버레이 정상 표시(회귀 없음)
