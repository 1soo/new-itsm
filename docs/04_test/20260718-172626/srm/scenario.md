# 통합 테스트 시나리오 — srm (그리드 폼 빌더 후속 개선: placeholder·guide 컴포넌트·1×1 캡션 아이콘화·개별 실시간 미리보기·date/file 롤백)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew build`, FE `npm run build`)
- Process Owner 권한 계정(`po@itsm.local` / `Admin@1234`, SCR-SRM-007 Form 설정), End User 권한 계정(`user@itsm.local` / `Admin@1234`, SCR-SRM-002 요청 제출)
- 기존 테스트용 카탈로그 항목("그리드 빌더 테스트" 등) 활용, 필요 시 신규 배치로 스키마 갱신(테스트 종료 후 원복)
- playwright는 매 TC마다 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-GF2-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/backend`에서 `./gradlew build` 실행
  2. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 둘 다 오류 없이 빌드 성공

### TC-GF2-001 · 팔레트 9종 노출 및 placeholder 입력 UI 노출 조건
- 근거: @docs/02_plan/screen/service-request.md (5.3절 팔레트 9종, 5.4절 "placeholder 7종 입력 컴포넌트 모두... hasPlaceholderUi인 유형에만 노출")
- 전제: Process Owner로 `/admin/service-catalog` "Form 설정" 팝업 오픈
- 절차:
  1. 좌측 팔레트에 text/textarea/select/radio/checkbox/date/file/label/guide 9종 노출 확인
  2. text/textarea/select/date/file 배치 후 각각 Content 설정 팝오버에 Placeholder 입력 항목 존재 확인
  3. radio/checkbox 배치 후 Content 설정 팝오버에 Placeholder 입력 항목이 없는지 확인(옵션 콤마 입력만 존재)
- 기대 결과: text/textarea/select/date/file 5종만 Placeholder 입력 UI 노출, radio/checkbox는 미노출(스키마 필드 자체는 있으나 UI만 미노출)

### TC-GF2-002 · placeholder 렌더링(요청 제출 화면)
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "값이 비어 있으면 placeholder를, 값이 있으면 실제 값을 표시. 미지정 시 유형별 폴백 문구")
- 전제: text/select에 placeholder 지정한 카탈로그 항목으로 SCR-SRM-002 요청 제출 화면 진입
- 절차:
  1. 값 입력 전 필드에 지정한 placeholder 텍스트가 표시되는지 확인
  2. select는 placeholder 미지정 시 기존과 동일하게 "선택" 폴백 문구 표시되는지 확인
  3. 값 입력 후 placeholder 대신 실제 값이 표시되는지 확인
- 기대 결과: 미입력 시 placeholder(또는 유형별 폴백), 입력 후 실제 값 표시

### TC-GF2-003 · guide 컴포넌트 배치·Content 설정(안내 텍스트+첨부 파일)
- 근거: @docs/02_plan/screen/service-request.md (5.3절 guide 9번째 팔레트, 5.4절 "안내 텍스트 여러 줄 입력+가이드 파일 첨부만 제공, 정렬·default·필수·정규식 없음")
- 전제: Form 설정 팝업에서 guide 컴포넌트 배치
- 절차:
  1. guide 컴포넌트 Content 설정 팝오버 오픈 — "안내 텍스트"(여러 줄)와 "첨부 파일"(선택)만 존재하는지 확인(정렬/default/읽기전용/필수/정규식 없음)
  2. 안내 텍스트 입력, 파일 첨부(base64 변환) 후 파일명 표시 확인, "제거" 버튼으로 첨부 해제 확인
  3. guide 컴포넌트 이동/겹침 시도/리사이즈(1~2×1~2 범위) — label과 동일 배치 규칙 적용되는지 확인
- 기대 결과: Content 설정 항목이 안내 텍스트+첨부 파일로 한정, 배치·이동·겹침방지·리사이즈 규칙은 다른 정적 컴포넌트(label)와 동일

### TC-GF2-004 · guide 컴포넌트 렌더링(요청 제출 화면·pre-view) 및 제출 데이터 제외
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "렌더링에서는 안내 텍스트와 함께... 다운로드 버튼/링크... 제출 데이터에는 포함되지 않음")
- 전제: 안내 텍스트+첨부 파일이 설정된 guide 컴포넌트가 포함된 카탈로그 항목으로 SCR-SRM-002 진입
- 절차:
  1. 요청 제출 화면에 안내 텍스트와 첨부 파일 다운로드 링크가 표시되는지 확인
  2. SCR-SRM-007 pre-view에도 동일하게 표시되는지 확인
  3. 요청 제출 후(네트워크 요청 바디 확인) guide 컴포넌트의 key가 제출 데이터(`formValues`)에 포함되지 않는지 확인
- 기대 결과: 안내 텍스트+다운로드 링크 렌더링, 제출 데이터에는 guide 값 미포함

### TC-GF2-005 · guide 컴포넌트 서버 검증 제외
- 근거: @docs/02_plan/api_spec/common.md (0-2절 "type=label/guide는 값이 없는 정적 컴포넌트라 검증 대상에서 제외")
- 절차:
  1. guide 컴포넌트가 포함된(값 없음) 카탈로그 항목으로 `POST /api/v1/service-requests` 직접 호출(다른 필수 필드는 채움)
- 기대 결과: guide 컴포넌트로 인한 400 없음(정상 201 생성)

### TC-GF2-006 · 빌더 캔버스 1×1 카드 캡션 아이콘 전환
- 근거: @docs/02_plan/screen/service-request.md (5.2절 "1×1 크기일 때는 카드 상단 캡션을 컴포넌트 유형 아이콘만 표시하고 텍스트는 숨긴다. 1×1보다 크면 아이콘+텍스트")
- 전제: Form 설정 팝업 캔버스에 컴포넌트 배치
- 절차:
  1. 컴포넌트를 1×1 크기로 배치/리사이즈 후 카드 캡션에 아이콘만 표시되는지(텍스트 숨김) 확인
  2. 동일 컴포넌트를 2×1, 1×2, 2×2 등으로 리사이즈 후 아이콘+텍스트(유형명 또는 label의 text)가 함께 표시되는지 확인
- 기대 결과: 1×1일 때만 아이콘 전용, 그 외 크기는 아이콘+텍스트

### TC-GF2-007 · Content 설정 팝업 폭 확대
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "팝업 폭을 기존(320px)보다 확장... 최소 400px 이상 권장")
- 절차:
  1. 임의 컴포넌트의 Content 설정 팝오버를 열고 렌더링된 팝업 요소의 실제 폭(px)을 측정
- 기대 결과: 팝업 폭 400px 이상(guide의 텍스트+파일, 개별 미리보기 영역이 잘리지 않고 표시됨)

### TC-GF2-008 · Content 설정 팝업 내 개별 컴포넌트 실시간 미리보기
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "설정을 변경하면 미리보기가 즉시 갱신... 미리보기 안에서 값을 입력해볼 수 있으나... defaultValue나 실제 제출 데이터에는 반영되지 않는다")
- 전제: text(또는 select) 컴포넌트 Content 설정 팝오버 오픈
- 절차:
  1. Placeholder/폭%/정렬/기본값/필수 여부/정규식(select는 옵션)을 변경할 때마다 팝업 하단 미리보기 영역이 즉시 갱신되는지 확인
  2. 미리보기 영역 내부 입력 필드에 임의 값을 입력
  3. 팝업을 닫지 않고 상단 설정(예: 폭%)을 다시 변경 — 미리보기에 입력했던 값이 실제 컴포넌트의 기본값(Content 설정의 "기본값" 입력란)에 반영되지 않았는지 확인
  4. 적용 버튼 클릭 후 실제 렌더러(요청 제출 화면)에 미리보기에서 입력한 로컬 값이 반영되지 않았는지(defaultValue 그대로인지) 확인
- 기대 결과: 설정 변경 시 미리보기 즉시 갱신, 미리보기 내 입력은 로컬 상태로만 존재하고 실제 defaultValue/제출 데이터에 영향 없음

### TC-GF2-009 · date/file 입력 박스+우측 아이콘 롤백 및 클릭 동작
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "아이콘만 남기는 표시를 되돌리고... 입력 박스+박스 우측 끝 아이콘... 박스 전체가 클릭 영역")
- 전제: date·file 컴포넌트가 배치된 카탈로그 항목으로 SCR-SRM-002 요청 제출 화면 및 SCR-SRM-007 pre-view 확인(직전 통합 테스트 `20260718-145952`에서는 아이콘 전용이었음 — 이번에 롤백 검증)
- 절차:
  1. 요청 제출 화면에서 date/file 필드가 입력 박스(테두리)+박스 우측 끝 아이콘(달력/파일) 형태로 렌더링되는지 확인(아이콘 전용 아님)
  2. 값 없을 때 박스 안에 placeholder(또는 유형별 기본 안내 "날짜를 선택하세요"/"파일을 선택하세요") 문구가 표시되는지 확인
  3. 박스의 텍스트 영역과 우측 아이콘 각각을 클릭 — 두 경우 모두 네이티브 date picker/파일 선택 다이얼로그가 열리는지 확인
  4. 값 선택 후 박스 안에 값(날짜 `yyyy-MM-dd`/파일명)이 표시되는지 확인
  5. SCR-SRM-007 pre-view에서도 동일하게 입력 박스+아이콘 형태로 렌더링되는지 확인
- 기대 결과: 아이콘 전용이 아니라 입력 박스+우측 아이콘, 박스 전체가 클릭 영역, 값 표시·placeholder 폴백 정상

### TC-GF2-010 · 기존 SRM 회귀 확인
- 근거: 직전 통합 테스트 이력(`docs/04_test/20260718-145952/srm/result/srm.md`, `docs/04_test/20260718-135109/srm/result/srm.md`)
- 절차:
  1. label 컴포넌트(팝오버 항목·배치·이동·겹침·리사이즈) 재확인
  2. 순차 단일 오류 표시(배열 순서 기준, 클라이언트) 및 서버 재검증 동일 계약(400 순차 1건) 재확인
  3. pre-view 라운드트립(적용→저장→새로고침→재조회) 재확인
  4. 요청 처리함(SCR-SRM-004) 카테고리 필터링, 카탈로그 카테고리 관리(SCR-SRM-009) CRUD 재확인
- 기대 결과: 전부 이전 통합 테스트 결과와 동일하게 정상 동작(회귀 없음)
