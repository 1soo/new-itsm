# 통합 테스트 시나리오 — srm (form.io 완전 제거 → 자체 8×n 그리드 폼 빌더 전면 재구현)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew build`, FE `npm run build`)
- Process Owner 권한 계정(SCR-SRM-007 Form 설정), End User 권한 계정(SCR-SRM-002 요청 제출)
- dev-fe가 남긴 테스트용 카탈로그 항목("그리드 빌더 테스트", "전체타입테스트") 활용

## 시나리오

### TC-GRID-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/backend`에서 `./gradlew build` 실행
  2. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 둘 다 오류 없이 빌드 성공

### TC-GRID-001 · @formio 완전 제거 확인
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 "전 코드베이스에 `@formio` 관련 의존성·import·CSS가 남아있지 않음")
- 절차:
  1. `source/frontend/package.json`에 `@formio/js`·`@formio/react` 의존성 존재 여부 확인
  2. `source/frontend/src` 전체에서 `formio`(대소문자 무관) 문자열 검색
  3. `source/frontend/src/index.css`에서 `.formio-scope`/`.formio-dialog`/`.formio-render-scope` 규칙 존재 여부 확인
- 기대 결과: 의존성·import·CSS 전부 없음(주석 처리가 아니라 완전 삭제)

### TC-GRID-002 · 팔레트 7종 배치·리사이즈·크기 제약
- 근거: @docs/02_plan/screen/service-request.md (5.2/5.3절), @docs/03_develop/plan/service-request.md (FE 완료 기준)
- 전제: Process Owner로 SCR-SRM-007 접속, "그리드 빌더 테스트" 항목의 "Form 설정" 팝업 오픈
- 절차:
  1. 좌측 팔레트 7종(text/textarea/select/radio/checkbox/date/file) 각각 캔버스에 배치
  2. 임의 컴포넌트 리사이즈 시도(1~2칸×1~2칸)
  3. textarea 컴포넌트 높이를 2칸 초과로 리사이즈 시도
- 기대 결과: 7종 모두 배치 가능, 배치·리사이즈 1칸 단위 스냅, 폭 1~2/높이 1~2 제약 적용, textarea만 높이 제약 없음

### TC-GRID-003 · 겹침 배치 차단
- 근거: @docs/02_plan/screen/service-request.md (5.2절 "이미 다른 컴포넌트가 점유한 셀에 겹쳐 배치를 시도하면 배치를 막고 인라인 안내")
- 전제: TC-GRID-002 상태(캔버스에 컴포넌트 존재)
- 절차:
  1. 기존 컴포넌트가 점유한 셀에 새 컴포넌트 배치 시도(또는 기존 컴포넌트를 다른 컴포넌트 위로 드래그)
- 기대 결과: 배치 차단, "이미 배치된 컴포넌트와 겹칩니다" 인라인 안내 노출

### TC-GRID-004 · Content 설정 팝오버
- 근거: @docs/02_plan/screen/service-request.md (5.4절)
- 전제: 캔버스에 배치된 컴포넌트 하나 hover
- 절차:
  1. 설정 버튼 클릭해 미니 팝업 오픈
  2. label 정렬, input 폭%/정렬/default/읽기전용, 필수 여부, 정규식(regex) 각각 설정
  3. select/radio/checkbox 컴포넌트는 콤마 구분 옵션 입력 + "CI 연계" 라디오 버튼 자리 확인(클릭해도 실제 동작 없음)
- 기대 결과: 각 설정 항목이 정상 반영되고 CI 연계 라디오는 배치만 되고 동작 없음(향후 확장용)

### TC-GRID-005 · select/radio/checkbox 옵션 다수 시 레이아웃 무결성
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "옵션 텍스트가 길거나 개수가 많을 때 셀 폭 안에서 잘리지 않도록 자동 줄바꿈+셀 내부 스크롤")
- 전제: select 또는 checkbox 컴포넌트에 옵션 10개 이상(또는 긴 텍스트) 설정
- 절차:
  1. 옵션을 다수/장문으로 입력 후 적용
  2. 렌더러(pre-view 또는 SCR-SRM-002)에서 해당 컴포넌트 확인
- 기대 결과: 셀 폭 안에서 텍스트가 잘리지 않고 자동 줄바꿈, 셀 높이 고정 유지(내부 스크롤), 그리드 레이아웃 붕괴 없음

### TC-GRID-006 · 적용→pre-view→저장→재조회 라운드트립
- 근거: @docs/02_plan/screen/service-request.md (5.5절)
- 절차:
  1. 그리드 설계 후 "적용" 클릭 → 팝업 닫힘, pre-view 축소판에 반영 확인
  2. 카탈로그 항목 "저장" 클릭
  3. 페이지 새로고침 후 해당 항목 재조회, "Form 설정" 재오픈
- 기대 결과: 저장된 그리드 스키마가 그대로 재조회·재렌더링(위치/크기/Content 설정 동일)

### TC-GRID-007 · 요청 제출 화면 렌더링·클라이언트 검증
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-002)
- 전제: End User로 "그리드 빌더 테스트" 요청 유형 선택(SCR-SRM-002)
- 절차:
  1. 그리드가 설계한 배치 그대로 렌더링되는지 확인
  2. 필수 필드 비우고 제출 시도
  3. 정규식 위반 값 입력 후 제출 시도
  4. 모든 값 정상 입력 후 제출
- 기대 결과: 필수 미입력/정규식 위반 시 제출 차단·인라인 오류, 정상 입력 시 제출 성공(접수번호 토스트·상세 이동)

### TC-GRID-008 · 서버 재검증(FormSubmissionValidator)
- 근거: @docs/02_plan/api_spec/common.md (0-2절), @docs/03_develop/plan/service-request.md (BE 완료 기준)
- 절차:
  1. 클라이언트 검증을 우회해 required 필드 누락 상태로 `POST /api/v1/service-requests` 직접 호출
  2. regex 위반 값으로 직접 호출
  3. 정상 값으로 직접 호출
- 기대 결과: 1·2는 400(각각 REQUIRED_FIELD_MISSING/FORM_FIELD_INVALID), 3은 201 성공·`form_values`에 그대로 저장

### TC-GRID-009 · 기존 카탈로그 항목 form_schema 리셋 확인
- 근거: @docs/02_plan/database/service-request.md (2026-07-18 "기존 데이터 리셋"), @docs/03_develop/plan/service-request.md (DB 완료 기준)
- 전제: dev-fe가 새로 만든 테스트 항목이 아닌 기존(이전 form.io로 설계됐던) 카탈로그 항목
- 절차:
  1. DB에서 기존 카탈로그 항목들의 `form_schema` 값 조회
- 기대 결과: 전부 `{"components":[]}`(빈 그리드)로 리셋되어 있음

### TC-GRID-010 · ESM 화면 회귀 없음(레거시 EAV 그대로 사용)
- 근거: @docs/02_plan/screen/service-request.md (5절 "ESM은 이 아키텍처가 적용된 적 없고 기존 레거시 EAV를 그대로 사용")
- 절차:
  1. ESM 부서 카탈로그 관리 화면(EsmCatalogManagePage) 접속·기존 필드 빌더 정상 동작 확인
  2. ESM 요청 제출 화면(DeptRequestSubmitPage) 접속·기존 동적 폼 정상 렌더링 확인
- 기대 결과: ESM 화면은 이번 변경 영향 없이 기존과 동일하게 동작(field-builder.tsx/dynamic-form.tsx 그대로 사용)
