# 통합 테스트 시나리오 — srm (그리드 폼 빌더 3차 유지보수: 팝업 3분할 미리보기·radio/checkbox 배치·guide 2종 분리)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew build`, FE `npm run build`)
- Process Owner 권한 계정(`po@itsm.local` / `Admin@1234`, SCR-SRM-007 Form 설정), End User 권한 계정(`user@itsm.local` / `Admin@1234`, SCR-SRM-002 요청 제출)
- 신규 카탈로그 항목을 생성해 인터랙티브 테스트에 사용(삭제 API 없어 잔여 데이터로 유지), 기존 테스트 픽스처는 읽기 전용으로만 활용
- playwright는 매 TC마다 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-GF3-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/backend`에서 `./gradlew build` 실행
  2. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 둘 다 오류 없이 빌드 성공

### TC-GF3-001 · Form 설정 팝업 3분할 동시 표시·폭 확대
- 근거: @docs/02_plan/screen/service-request.md (5.6절 "좌 팔레트/중앙 캔버스/우 전체 레이아웃 미리보기 3분할, 탭 아님", "팝업 폭 뷰포트 90% 이상 권장")
- 전제: Process Owner로 `/admin/service-catalog` "Form 설정" 팝업 오픈
- 절차:
  1. 팝업 내부에 팔레트/캔버스/미리보기 3영역이 탭 전환 없이 항상 동시에 보이는지 확인
  2. 팝업(모달) 실제 렌더링 폭을 측정해 기존(약 896px, max-w-4xl) 대비 크게 확대됐는지 확인(뷰포트 90% 근접 수준)
- 기대 결과: 3영역 항상 동시 표시, 팝업 폭이 뷰포트 90% 수준으로 확대(w-[90vw] max-w-[1600px])

### TC-GF3-002 · 편집 중(적용 전) 실시간 전체 미리보기 반영
- 근거: @docs/02_plan/screen/service-request.md (5.6절 "현재 편집 중(적용 전)인 components 상태를 실시간 반영")
- 전제: TC-GF3-001 팝업 오픈 상태
- 절차:
  1. 캔버스에 컴포넌트 배치 → 우측 미리보기에 즉시 반영되는지 확인(적용 버튼 클릭 전)
  2. 배치된 컴포넌트를 이동/리사이즈 → 미리보기 즉시 갱신 확인
  3. Content 설정(예: placeholder 변경) → 미리보기 즉시 갱신 확인
- 기대 결과: 적용 클릭 없이도 캔버스 변경사항이 우측 미리보기에 실시간 반영

### TC-GF3-003 · CatalogManagePage 외부 pre-view 제거·필드 개수 안내로 대체
- 근거: @docs/03_develop/plan/service-request.md (3차 유지보수 절, "45% 축소 pre-view 제거 → 필드 개수 안내 문구") — @docs/02_plan/screen/service-request.md 5.6/5.7절
- 전제: 카탈로그 항목 편집 폼(팝업 바깥)
- 절차:
  1. "Form 설정" 버튼 아래에 45% 축소 렌더링 pre-view가 더 이상 없는지 확인
  2. 필드 미설정 시 "설정된 양식이 없습니다..." 안내, 필드 설정 후 "설정된 필드 N개" 텍스트로 대체되는지 확인
- 기대 결과: 축소판 pre-view 완전 제거, 텍스트 안내로 대체

### TC-GF3-004 · 팔레트 10종·guide 폐기 확인
- 근거: @docs/02_plan/screen/service-request.md (5.3절 팔레트 10종, "guide 타입 폐기 → guide-text/guide-file 2종")
- 절차:
  1. 좌측 팔레트에 text/textarea/select/radio/checkbox/date/file/label/guide-text(안내 텍스트)/guide-file(가이드 파일) 10종 노출 확인(`guide` 통합 항목 없음)
- 기대 결과: 10종 노출, 기존 `guide` 팔레트 항목은 존재하지 않음

### TC-GF3-005 · guide-text Content 설정(텍스트+정렬만)
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "guide-text: 안내 텍스트+정렬만 제공")
- 절차:
  1. guide-text 컴포넌트 배치 후 Content 설정 팝오버 오픈 — "표시 텍스트"+정렬(좌/가운데/우)만 존재하는지 확인(default/읽기전용/필수/정규식/첨부파일 없음)
- 기대 결과: label과 동일한 텍스트+정렬 UI만 노출

### TC-GF3-006 · guide-file Content 설정(첨부파일만)
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "guide-file: 가이드 파일 첨부만 제공, 텍스트 입력 없음")
- 절차:
  1. guide-file 컴포넌트 배치 후 Content 설정 팝오버 오픈 — "첨부 파일" 입력만 존재하는지 확인(텍스트/정렬 등 다른 설정 없음)
  2. 파일 첨부 후 파일명 표시+제거 버튼 동작 확인
- 기대 결과: 첨부 파일 UI만 노출, 첨부/제거 정상 동작

### TC-GF3-007 · guide-text/guide-file 렌더링(요청 제출 화면)
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "guide-text 렌더링 시 안내문임을 시각적으로 구분", "guide-file 미첨부 시 빈 상태 안내")
- 전제: guide-text(텍스트 설정)+guide-file(파일 미첨부/첨부 각각) 컴포넌트가 포함된 카탈로그 항목으로 SCR-SRM-002 진입
- 절차:
  1. guide-text가 안내 아이콘 등으로 label과 시각적으로 구분되어 렌더링되는지 확인
  2. guide-file 미첨부 시 빈 상태 안내 문구 표시 확인
  3. guide-file 첨부 시 다운로드 버튼/링크 표시 및 실제 다운로드 가능 확인
- 기대 결과: guide-text 안내문 구분 렌더링, guide-file 빈 상태/다운로드 링크 정상

### TC-GF3-008 · guide-text/guide-file 제출 데이터·검증 제외(클라이언트+서버)
- 근거: @docs/02_plan/api_spec/common.md (0-2절 "type=label/guide-text/guide-file은 검증 대상에서 제외"), @docs/02_plan/screen/service-request.md 5.4절
- 절차:
  1. guide-text/guide-file만 있고 다른 필수 필드 없는 폼을 요청 제출 화면에서 제출 → 정상 제출(400 없음) 확인
  2. `POST /api/v1/service-requests` 실제 요청 바디에 guide-text/guide-file의 key가 `formValues`에 포함되지 않는지 확인
- 기대 결과: guide 2종 모두 제출 데이터 미포함, 400 없이 201 정상 처리

### TC-GF3-009 · radio/checkbox 배치 방향·여백 설정 UI 및 기본값
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "radio/checkbox에 배치방향(가로/세로)·여백 3단계, 기본값 가로·좁게", "select는 대상 아님")
- 절차:
  1. radio 컴포넌트 Content 설정 팝오버에 "옵션 배치 방향"(가로/세로 토글, 기본 가로 선택 상태)·"옵션 간 여백"(좁게/보통/넓게, 기본 좁게 선택 상태) UI 존재 확인
  2. checkbox도 동일 UI 존재 확인
  3. select 컴포넌트 Content 설정 팝오버에는 이 UI가 없는지 확인
- 기대 결과: radio/checkbox에만 배치방향·여백 UI 노출(기본값 가로/좁게), select에는 없음

### TC-GF3-010 · radio/checkbox 배치 방향·여백 렌더링 반영
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "가로 배치 시 옵션이 셀 폭 넘으면 자동 줄바꿈+셀 내부 스크롤")
- 절차:
  1. radio에 옵션 여러 개(셀 폭 초과 가능한 양) 설정 후 기본(가로) 렌더링에서 옵션이 가로로 배치되고 폭 초과 시 줄바꿈되는지 확인
  2. 배치 방향을 "세로"로 변경 → 렌더링(개별 미리보기·요청 제출 화면)에서 세로 배치로 전환되는지 확인
  3. 여백 3단계 변경 시 옵션 간 간격이 시각적으로 달라지는지 확인
- 기대 결과: 가로/세로 전환 및 여백 변경이 렌더링에 즉시 반영, 가로 배치 줄바꿈 정상

### TC-GF3-011 · BE guide-text/guide-file 검증 스킵 및 guide 코드 제거 확인
- 근거: @docs/02_plan/api_spec/common.md (0-2절), 3차 유지보수 계획서 BE 담당 범위
- 절차:
  1. `FormSubmissionValidatorTest` 내 guide-text/guide-file 스킵 테스트가 존재하고 빌드(TC-GF3-B01)에서 통과했는지 확인
  2. `FormSubmissionValidator.java` 소스에 `"guide".equals(type)` 등 단일 `guide` 타입 관련 코드가 완전히 제거됐는지 코드 확인
- 기대 결과: guide-text/guide-file 각각 스킵 테스트 통과, `guide` 단일 타입 코드 잔존 없음

### TC-GF3-012 · 기존 SRM 회귀 확인
- 근거: 직전 통합 테스트 이력(`docs/04_test/20260718-172626/srm/result/srm.md`, `docs/04_test/20260718-145952/srm/result/srm.md`)
- 절차:
  1. placeholder(text/textarea/select/date/file만 UI 노출, radio/checkbox 없음) 재확인
  2. 빌더 캔버스 1×1 카드 아이콘 전용 캡션 재확인
  3. Content 설정 팝업 내 개별 컴포넌트 실시간 미리보기 재확인
  4. date/file 입력박스+우측아이콘+박스 전체 클릭(네이티브 피커 오픈) 재확인
  5. label 컴포넌트(텍스트+정렬만) 재확인
  6. 유효성 순차 단일 오류 표시(배열 순서 기준, 클라이언트+서버 동일 계약) 재확인
- 기대 결과: 전부 이전 통합 테스트 결과와 동일하게 정상 동작(회귀 없음)
