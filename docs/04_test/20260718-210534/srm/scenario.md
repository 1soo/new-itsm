# 통합 테스트 시나리오 — srm (그리드 폼 빌더 4차: 축소 미리보기 재도입·DnD 배치·높이 고정·정규식 text 전용·라벨 태그 개편)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew build`, FE `npm run build`)
- Process Owner 권한 계정(`po@itsm.local` / `Admin@1234`, SCR-SRM-007 Form 설정)
- 신규 카탈로그 항목을 생성해 이번 4차 변경분(A1/A2/B1~B9)을 처음부터 구성하며 검증한다(기존 항목 재사용 시 label 리셋 여부 확인이 섞이므로 분리)
- DB 검증용으로 과거(3차 이전) label 타입을 포함했던 카탈로그 항목이 이미 존재해야 함(리셋 여부 확인 대상) — 없으면 사전에 직접 DB에 `type:"label"` 포함 로우를 생성해 확인
- playwright는 매 TC마다 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-GF4-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/backend`에서 `./gradlew build` 실행
  2. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 둘 다 오류 없이 빌드 성공

### TC-GF4-A01 · CatalogManagePage 축소 미리보기 재도입
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-007 "Form 축소 미리보기" 행), @docs/03_develop/plan/service-request.md (A1)
- 전제: Process Owner로 `/admin/service-catalog` 진입, 카탈로그 항목 편집 폼 오픈
- 절차:
  1. "Form 설정" 버튼 아래에 "설정된 필드 N개" 같은 텍스트가 아니라 실제 축소 렌더링 미리보기가 표시되는지 확인(필드 없는 초기 상태에서는 안내 문구만 표시되는지도 확인)
  2. Form 설정 팝업에서 컴포넌트를 배치·적용한 뒤 팝업을 닫고, 축소 미리보기가 적용된 스키마로 갱신되는지 확인
  3. 축소 미리보기를 클릭하면 Form 설정 팝업이 재오픈되는지 확인
- 기대 결과: 축소 미리보기가 실제 렌더링(비활성)으로 표시되고 클릭 시 팝업 재오픈, 필드 없으면 안내 문구

### TC-GF4-A02 · "양식 필드" 라벨-버튼 여백 확대
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-007 Form 설정 버튼 행 "여백을 주변 폼 필드보다 넓게"), @docs/03_develop/plan/service-request.md (A2)
- 절차:
  1. "양식 필드" 라벨과 "Form 설정" 버튼 사이 간격을 다른 필드(이름/설명/카테고리 등) 라벨-입력 사이 간격과 비교
  2. 좌측 정렬이 유지되는지 확인
- 기대 결과: "양식 필드" 라벨-버튼 간격이 다른 필드보다 넓고 좌측 정렬 유지

### TC-GF4-B01a · 팔레트 클릭 배치(9종, label 없음)
- 근거: @docs/02_plan/screen/service-request.md (5.3절 팔레트 9종), @docs/03_develop/plan/service-request.md (완료 기준 1번째 항목)
- 절차:
  1. Form 설정 팝업의 팔레트 목록에 text/textarea/select/radio/checkbox/date/file/guide-text/guide-file 9종만 있고 `label` 항목이 없는지 확인
  2. 각 유형을 클릭해 캔버스 첫 빈 칸에 자동 배치되는지 확인
- 기대 결과: 팔레트 9종, label 없음, 클릭 시 첫 빈 칸 자동 배치

### TC-GF4-B01b · 팔레트 드래그앤드롭 배치 — 정상 위치 지정
- 근거: @docs/02_plan/screen/service-request.md (5.2절 "배치 방식"), @docs/03_develop/plan/service-request.md (B1)
- 절차:
  1. 팔레트의 text 항목을 캔버스의 특정 빈 셀(예: 3행 2열)로 드래그해 드롭
  2. 드래그 중 1칸 스냅 미리보기가 표시되는지 확인
  3. 드롭 후 정확히 그 위치에 컴포넌트가 생성되는지 확인
- 기대 결과: 드래그앤드롭으로 원하는 위치에 정확히 배치, 드래그 중 스냅 미리보기 표시

### TC-GF4-B01c · 드래그앤드롭 배치 — 겹침 차단
- 근거: 동일(5.2절 "겹치면 배치를 막고 인라인 안내")
- 절차:
  1. 이미 컴포넌트가 배치된 셀 위로 팔레트 항목을 드래그해 드롭 시도
- 기대 결과: 배치가 차단되고 겹침 경고가 표시됨(클릭 배치와 동일한 경고)

### TC-GF4-B01d · 드래그앤드롭 배치 — 캔버스 밖 드롭 취소
- 근거: 동일(5.2절 "캔버스 밖에 놓으면 배치 취소")
- 절차:
  1. 팔레트 항목을 캔버스 바깥(팔레트 영역이나 팝업 여백)으로 드래그해 드롭
- 기대 결과: 배치가 취소되고 캔버스에 아무 변화 없음

### TC-GF4-B01e · 드래그 배치 직후 동일 유형 클릭 배치 정상 동작(회귀 방지 포인트)
- 근거: dev-lead 지정 중점 확인 항목(드래그 후 클릭이 씹히지 않는지)
- 절차:
  1. 팔레트의 select 항목을 드래그해 캔버스에 배치
  2. 드래그 종료 직후(300ms 이내) 같은 select 팔레트 버튼을 드래그 없이 "클릭"해 추가 배치 시도
  3. 정상적으로 두 번째 컴포넌트가 첫 빈 칸에 배치되는지 확인
  4. 별도로 1건 더 배치해 이번엔 드래그 종료 후 500ms 이상 대기 후 클릭했을 때도 정상 동작하는지 확인
- 기대 결과: 드래그 직후 클릭이든 지연 후 클릭이든 모두 정상적으로 컴포넌트가 추가됨(클릭 이벤트가 씹히지 않음)

### TC-GF4-B02 · 컴포넌트별 높이 상한 세분화
- 근거: @docs/02_plan/screen/service-request.md (5.2절 높이 상한 세분화), @docs/03_develop/plan/service-request.md (B2, 완료 기준 2번째 항목)
- 절차:
  1. text/date/file/select/guide-file 컴포넌트를 각각 배치 후 세로 리사이즈(모서리 드래그)로 2칸 시도 → 1칸에서 늘어나지 않는지 확인
  2. radio/checkbox/guide-text는 1→2칸 리사이즈가 정상 동작하는지 확인
  3. textarea는 2칸 이상으로 자유롭게 리사이즈되는지 확인
- 기대 결과: text/date/file/select/guide-file은 1칸 고정, radio/checkbox/guide-text는 1~2칸, textarea는 무제한

### TC-GF4-B03 · 정규식 검증 text 전용 UI
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "정규식 검증은 text 타입 전용"), @docs/03_develop/plan/service-request.md (B3, 완료 기준 3번째 항목)
- 절차:
  1. text 컴포넌트 Content 설정 팝업에 정규식(Validation regex) 입력 UI가 보이는지 확인
  2. textarea/select/radio/checkbox/date/file 6종 Content 설정 팝업에는 정규식 입력 UI가 없는지 확인
  3. text 컴포넌트에 정규식(예: `^[0-9]+$`)을 설정하고 필수 여부도 함께 체크 → 저장 후 요청 제출 폼에서 형식 위반 값 입력 시 제출이 차단되는지 확인(순차 단일 오류 문구 확인)
- 기대 결과: 정규식 UI는 text 전용, text 정규식+필수 동시 적용 시 서버/클라이언트 모두 검증

### TC-GF4-B03b · 서버 재검증 — 비text 타입에 저장된 regex 값 무시
- 근거: @docs/02_plan/api_spec/common.md (0-2절 "정규식 검증을 type=text 전용으로 축소"), @docs/02_plan/api_spec/service-request.md (API-SRM-002 validation.regex 설명)
- 절차:
  1. DB 또는 API로 select/checkbox 등 text가 아닌 컴포넌트의 `validation.regex`에 임의 값을 직접 주입한 카탈로그 항목을 준비(UI로는 설정 불가하므로 API 직접 호출 또는 DB 업데이트로 재현)
  2. 해당 컴포넌트에 값을 채워 요청 제출(`POST /api/v1/service-requests`) 시도
- 기대 결과: 서버가 400을 반환하지 않고 정상 제출됨(regex가 무시됨)

### TC-GF4-B04 · 정렬 9방향(3×3 앵커 위젯)
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "정렬 9방향"), @docs/03_develop/plan/service-request.md (B4, 완료 기준 4번째 항목)
- 절차:
  1. text 컴포넌트 Content 설정 팝업에서 정렬 UI가 기존 가로 3버튼이 아니라 3×3 그리드 앵커 위젯 하나로 통일되어 있는지 확인
  2. "우측-하단" 앵커를 선택 → 캔버스 카드 렌더링에 가로 우측+세로 하단 정렬이 반영되는지 확인
  3. guide-text 컴포넌트에서도 동일한 3×3 위젯으로 textAlign+textVerticalAlign을 "좌측-가운데" 등으로 설정 → 캔버스 카드에 반영되는지 확인
  4. 적용 후 요청 제출 폼(SCR-SRM-002)에서도 동일 정렬이 반영되는지 확인
- 기대 결과: 3×3 단일 위젯, 9방향 선택이 캔버스·요청 제출 폼 양쪽에 정확히 반영(세로 top/middle/bottom 포함)

### TC-GF4-B05 · 라벨 팔레트 제거 확인
- 근거: @docs/02_plan/screen/service-request.md (5.3절 "label을 팔레트에서 제거"), @docs/03_develop/plan/service-request.md (B5, 완료 기준 5번째 항목)
- 절차:
  1. 팔레트 목록에 `label`(라벨) 컴포넌트 항목이 없는지 재확인(TC-GF4-B01a와 동일 관찰 재확인)
- 기대 결과: 팔레트에 라벨 컴포넌트 없음(TC-GF4-B01a 결과 재확인용)

### TC-GF4-B06 · 라벨 생성·칩 스트립 표시
- 근거: @docs/02_plan/screen/service-request.md (5.8절 "생성 UI"), @docs/03_develop/plan/service-request.md (B6)
- 절차:
  1. 팔레트 컬럼 최상단 "라벨 추가" 버튼 클릭
  2. 미니 팝업에서 라벨 텍스트("중요"), 글자색, 테두리색을 입력 후 저장
  3. 팝업 타이틀과 팔레트+캔버스 사이 가로 스트립에 생성한 라벨이 칩(텍스트색·테두리색 반영)으로 표시되는지 확인
- 기대 결과: 라벨 생성 후 칩 스트립에 즉시 표시, 색상 반영

### TC-GF4-B07 · 라벨 칩 클릭 수정, 삭제 시 참조 해제
- 근거: @docs/02_plan/screen/service-request.md (5.8절 "표시·관리 UI"), @docs/03_develop/plan/service-request.md (B7)
- 절차:
  1. 위에서 생성한 라벨 칩을 클릭 → 값 프리필된 수정 팝업이 재오픈되는지 확인, 텍스트를 "긴급"으로 변경 후 저장 → 칩 텍스트 갱신 확인
  2. text 컴포넌트 Content 설정 팝업에서 이 라벨을 지정(아래 B09에서 상세 검증)한 뒤, 칩의 × 버튼으로 라벨을 삭제
  3. 삭제 후 그 라벨을 참조하던 컴포넌트가 캔버스에서 사라지지 않고 그대로 남아있는지, Content 설정 팝업의 라벨 지정 값이 "없음"으로 해제됐는지 확인
- 기대 결과: 칩 클릭 시 수정 재오픈, 삭제 시 컴포넌트는 유지되되 labelId만 null로 해제

### TC-GF4-B08 · Content 설정 팝업 라벨 지정/해제 UI
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "라벨(태그) 지정"), @docs/03_develop/plan/service-request.md (B8)
- 절차:
  1. 신규 라벨 하나를 생성
  2. text 컴포넌트 Content 설정 팝업에서 라벨 Select에 방금 생성한 라벨과 "없음" 옵션이 후보로 보이는지 확인
  3. 라벨을 지정 → 저장 후 다시 열어 지정값이 유지되는지 확인, "없음"으로 되돌려 해제되는지 확인
- 기대 결과: Select로 라벨 지정/해제 가능, 값 유지

### TC-GF4-B09 · 같은 라벨 2개 이상 지정 시 캔버스 경계 테두리 오버레이
- 근거: @docs/02_plan/screen/service-request.md (5.8절 "캔버스 경계 테두리 표시"), @docs/03_develop/plan/service-request.md (B9, 완료 기준 6번째 항목)
- 절차:
  1. 라벨 하나를 생성하고, 서로 떨어진 위치에 배치한 컴포넌트 2개에 같은 라벨을 지정 → 캔버스에 두 컴포넌트를 포함하는 사각형 경계 테두리(해당 라벨의 borderColor)가 표시되는지 확인
  2. 컴포넌트 중 하나를 다른 셀로 이동 → 테두리가 즉시 재계산되는지 확인
  3. 컴포넌트 중 하나를 리사이즈 → 테두리가 즉시 재계산되는지 확인
  4. 컴포넌트 하나의 라벨 지정을 해제(참조 1개로 감소) → 테두리가 사라지는지 확인
  5. 이 테두리가 Content 설정 팝업 개별 미리보기, A1 축소 미리보기, 요청 제출 폼(SCR-SRM-002) 렌더링에는 나타나지 않는지 각각 확인
- 기대 결과: 참조 2개 이상일 때만 경계 테두리 표시, 이동/리사이즈/해제 시 즉시 재계산, 캔버스 외 3곳(개별 미리보기/A1 축소 미리보기/요청 제출 폼)에는 미표시

### TC-GF4-DB01 · DB 리셋 대상/비대상 검증
- 근거: @docs/02_plan/database/service-request.md ("기존 데이터 리셋(2026-07-18 후속 유지보수 요청 4차...)"), @docs/03_develop/plan/service-request.md (완료 기준 7번째 항목)
- 절차:
  1. 과거 `type:"label"` 컴포넌트를 포함했던 카탈로그 항목(예: "라벨아이콘테스트")을 조회해 `form_schema`가 `{"components":[],"labels":[]}`로 리셋됐는지 확인
  2. label 타입을 사용하지 않던 다른 카탈로그 항목의 `form_schema`가 그대로 유지되는지 확인
  3. 신규 카탈로그 항목 생성 시 `form_schema` 기본값이 `{"components":[],"labels":[]}`인지 확인
- 기대 결과: label 포함 로우만 리셋, 미사용 로우는 그대로, 신규 기본값 갱신

### TC-GF4-R01 · 기존 SRM 회귀 확인
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 "기존 SRM 회귀 없음"), 직전 통합 테스트 이력(`docs/04_test/20260718-191941`, `20260718-182813`, `20260718-172626`)
- 절차:
  1. placeholder 7종 정상 표시
  2. guide-text/guide-file 렌더링(안내 텍스트, 다운로드 링크) 정상
  3. date/file 입력 박스+우측 아이콘, 클릭 시 네이티브 picker/파일 선택 오픈 정상
  4. radio/checkbox 배치 방향·옵션 간 여백 설정 정상
  5. 유효성 검증 순차 단일 오류(제출 시 배열 순서상 첫 위반 1건만 표시) 정상
  6. 빌더 팝업 캔버스=미리보기 통합(카드가 실제 렌더링, 상호작용 불가, 드래그 이동만 가능) 정상
- 기대 결과: 이번 유지보수와 무관한 기존 기능 전부 정상 동작(회귀 없음)
