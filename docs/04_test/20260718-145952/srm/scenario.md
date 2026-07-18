# 통합 테스트 시나리오 — srm (그리드 폼 빌더 세부 개선: label 컴포넌트·date/file 아이콘화·순차 단일 오류)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew build`, FE `npm run build`)
- Process Owner 권한 계정(SCR-SRM-007 Form 설정), End User 권한 계정(SCR-SRM-002 요청 제출)
- 기존 테스트용 카탈로그 항목("그리드 빌더 테스트" id=8, "전체타입테스트" id=9) 활용, 필요 시 신규 배치로 스키마 갱신

## 시나리오

### TC-GF-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/backend`에서 `./gradlew build` 실행
  2. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 둘 다 오류 없이 빌드 성공

### TC-GF-001 · 팔레트 8종 노출 및 label 팝오버 항목
- 근거: @docs/02_plan/screen/service-request.md (5.3절 팔레트 8종, 5.4절 label 컴포넌트 Content 설정)
- 전제: Process Owner로 SCR-SRM-007 "Form 설정" 팝업 오픈
- 절차:
  1. 좌측 팔레트에 text/textarea/select/radio/checkbox/date/file/label 8종 노출 확인
  2. label 컴포넌트 배치 후 Content 설정 팝오버 오픈
  3. 나머지 7종(text 등) Content 설정 팝오버 오픈
- 기대 결과: label 팝오버에는 "표시 텍스트" 입력 + 정렬(좌/가운데/우) 토글만 존재(default/읽기전용/필수/정규식/옵션 없음). 나머지 7종 팝오버에는 "라벨 텍스트"/"label 정렬" 항목이 없음(input 폭/정렬/default/읽기전용/필수/정규식만)

### TC-GF-002 · label 컴포넌트 배치·이동·리사이즈·겹침방지 규칙 동일 적용
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "다른 입력 컴포넌트와 동일 배치 규칙")
- 전제: label 컴포넌트 배치된 상태
- 절차:
  1. label 컴포넌트를 빈 셀로 이동
  2. label 컴포넌트를 다른 컴포넌트가 점유한 셀로 이동 시도
  3. label 컴포넌트 리사이즈(1~2칸×1~2칸 범위 내/초과)
- 기대 결과: 이동은 1칸 스냅으로 정상 동작, 겹침 시도는 차단+경고("이미 배치된 컴포넌트와 겹칩니다"), 리사이즈는 1~2×1~2 범위로 제한(다른 비-textarea 컴포넌트와 동일)

### TC-GF-003 · date/file 아이콘 전용 렌더링 및 네이티브 피커 동작
- 근거: @docs/02_plan/screen/service-request.md (5.4절 date/file 렌더링)
- 전제: date·file 컴포넌트가 배치된 카탈로그 항목으로 SCR-SRM-002 요청 제출 화면 및 SCR-SRM-007 pre-view 확인
- 절차:
  1. 요청 제출 화면에서 date/file 필드가 아이콘(캘린더/파일)만 노출되는지 확인
  2. 아이콘 클릭 시 네이티브 date picker/파일 선택 다이얼로그가 열리는지 확인
  3. 값 선택 후 아이콘 옆에 선택값(날짜 문자열/파일명) 텍스트로 표시되는지 확인
  4. SCR-SRM-007 pre-view에서도 동일하게 아이콘 전용 렌더링 확인
- 기대 결과: 브라우저 기본 date/file input이 그대로 노출되지 않고 아이콘 전용, 클릭 시 네이티브 UI 오픈, 선택값 텍스트 표시, pre-view도 동일 규칙 적용

### TC-GF-004 · 클라이언트 순차 단일 오류 표시(배열 순서 기준)
- 근거: @docs/02_plan/screen/service-request.md (5.5절 "배열 순서상 첫 번째 위반 컴포넌트 오류 1건만 폼 하단에 표시")
- 전제: 그리드 시각적 배치 순서와 `components` 배열 삽입 순서가 다르게 구성된 카탈로그 항목(예: 배열상 먼저 추가된 필드를 그리드 오른쪽/아래로 이동 배치)에 required 필드 2개 이상 존재
- 절차:
  1. 모든 필수 필드를 비운 채 제출 클릭
  2. 폼 하단에 오류가 몇 건 표시되는지, 어떤 필드의 오류인지 확인(그리드 위치가 아니라 배열 순서상 첫 번째인지)
  3. 해당 필드만 채우고 재제출
  4. 폼 하단 오류가 배열상 다음 위반 필드로 바뀌는지 확인
- 기대 결과: 필드별 인라인 오류 없음, 폼 하단에 항상 오류 1건만 표시, 표시 순서는 그리드 시각적 위치가 아니라 `components` 배열 삽입 순서 기준

### TC-GF-005 · 서버 재검증 순차 단일 400 반환
- 근거: @docs/02_plan/api_spec/common.md (0-2절 "첫 번째로 위반이 발견되는 컴포넌트에서 즉시 400 반환"), @docs/02_plan/api_spec/service-request.md (API-SRM-006 변경 이력)
- 절차:
  1. 여러 컴포넌트가 동시에 위반(예: 2개 이상 required 누락 또는 required+regex 위반 혼재)된 상태로 `POST /api/v1/service-requests` 직접 호출
  2. 응답 확인
- 기대 결과: 400 응답 하나만 반환(배열상 첫 번째 위반 컴포넌트 기준), 여러 위반이 한 번에 집계되어 반환되지 않음

### TC-GF-006 · label 타입 서버 검증 제외
- 근거: @docs/02_plan/api_spec/common.md (0-2절 "type=label은 검증 대상에서 제외")
- 절차:
  1. label 컴포넌트가 포함된 카탈로그 항목으로 요청 제출(label 컴포넌트는 값 없음)
- 기대 결과: label 컴포넌트 때문에 400이 발생하지 않음(정상 제출 또는 다른 필드 사유로만 400)

### TC-GF-007 · 기존 SRM 회귀 확인
- 근거: 직전 통합 테스트 이력(`docs/04_test/20260718-135109/srm/result/srm.md`, `docs/04_test/20260718-112351/srm/result/srm.md`)
- 절차:
  1. 팔레트 나머지 기능(배치/이동/리사이즈/겹침방지), Content 설정(required/regex/옵션) 정상 동작 재확인
  2. pre-view 라운드트립(적용→저장→새로고침→재조회) 재확인
  3. 요청 처리함(SCR-SRM-004) 카테고리 필터링 재확인
  4. 카탈로그 카테고리 관리(SCR-SRM-009) CRUD 재확인
- 기대 결과: 전부 이전 통합 테스트 결과와 동일하게 정상 동작(회귀 없음)
