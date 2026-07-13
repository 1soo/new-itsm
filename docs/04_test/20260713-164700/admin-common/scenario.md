# 통합 테스트 시나리오 — 관리자/공통 UI (admin-common)

> 유지보수 요청(2026-07-13): 사이드바/승인프로세스 i18n 커버리지 결함 수정, 디자인 배경색 흰색 전환, 승인 프로세스 메타데이터 분리.
> 근거 설계: `docs/02_plan/database/auth.md`(v0.3), `docs/02_plan/api_spec/auth.md`(v0.5), `docs/02_plan/screen/admin.md`(v0.4), `docs/02_plan/screen/common.md`(v0.14, 2.1절/6.8절)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew clean compileJava test`, FE `npm run build`)
- 로컬 DB 컨테이너(`source/db/docker`) 기동, 마이그레이션(`sql/01~30`) 적용 완료
- BE 서버 기동(`--server.port=8081`), FE dev 서버 기동
- 테스트 계정: `admin@itsm.local`/`Admin@1234`(SYSTEM_ADMIN), `im@itsm.local`/`Admin@1234`(INCIDENT_MANAGER, 비관리자 403 검증용)
- playwright는 매 TC마다 새 브라우저 컨텍스트(storage 초기화)에서 수행

## 시나리오

### TC-BUILD-001 · 백엔드 빌드
- 근거: `docs/02_plan/database/auth.md` v0.3, `docs/02_plan/api_spec/auth.md` v0.5 (신규 컬럼·API 반영 후 컴파일/테스트 성공 여부)
- 절차: `source/backend`에서 `./gradlew clean compileJava test` 실행
- 기대 결과: BUILD SUCCESSFUL, 전체 테스트 PASS

### TC-BUILD-002 · 프론트엔드 빌드
- 근거: `docs/02_plan/screen/admin.md` v0.4, `docs/02_plan/screen/common.md` v0.14(6.8절)
- 절차: `source/frontend`에서 `npm run build` 실행
- 기대 결과: 타입 오류·빌드 실패 없이 성공

### TC-DB-001 · screen 테이블 이중언어 컬럼·백필·제약
- 근거: `docs/02_plan/database/auth.md` 5절(`screen.screen_name_en` NOT NULL, `group_label_en`)
- 전제: DB 컨테이너 기동, 마이그레이션(`30_auth_screen_i18n.sql` 포함) 적용
- 절차:
  1. `screen` 테이블 컬럼 목록 조회(`screen_name_en`/`group_label_en` 존재·타입 확인)
  2. `screen_name_en IS NULL` 인 행이 없는지 확인(백필 완료·NOT NULL 제약)
  3. `group_code IS NOT NULL AND group_label_en IS NULL` 인 행이 없는지 확인
- 기대 결과: 컬럼 존재, `screen_name_en` 전 행 NOT NULL, 그룹 있는 행은 `group_label_en`도 채워짐

### TC-AUTH-001 · 로그인(사전 인증 플로우)
- 근거: `docs/02_plan/api_spec/auth.md` API-AUTH-001
- 절차: 새 컨텍스트에서 `admin@itsm.local`/`Admin@1234`로 로그인
- 기대 결과: 로그인 성공, 대시보드로 이동, Access Token 발급

### TC-I18N-001 · 사이드바 메뉴 i18n(한국어 기본)
- 근거: `docs/02_plan/screen/common.md` 6.8절, `docs/02_plan/api_spec/auth.md` API-AUTH-022
- 전제: TC-AUTH-001 로그인 상태
- 절차: 사이드바 메뉴·그룹 라벨 확인(언어 전환 전)
- 기대 결과: `screenName`/`groupLabel`(한국어 DB 원문) 표시

### TC-I18N-002 · 사이드바 메뉴 i18n(영어 전환)
- 근거: `docs/02_plan/database/auth.md` 5절, `docs/02_plan/api_spec/auth.md` API-AUTH-022, `AppLayout.tsx`
- 전제: TC-AUTH-001 로그인 상태
- 절차:
  1. 헤더 지구본 아이콘(SCR-COM-015) 클릭 → English 선택
  2. 사이드바 메뉴·그룹 라벨 재확인
- 기대 결과: `screenNameEn`/`groupLabelEn` 값으로 즉시 전환(새로고침 불필요), 원시 한국어 노출 없음

### TC-MENU-001 · 메뉴 관리 영문명 필드 CRUD(정상)
- 근거: `docs/02_plan/screen/admin.md` SCR-ADMIN-006, `docs/02_plan/api_spec/auth.md` API-AUTH-017/018
- 전제: SYSTEM_ADMIN 로그인, `/admin/menus` 진입
- 절차:
  1. "메뉴 생성" → 그룹(신규 입력)·메뉴명(한국어)·메뉴 영문명·경로 입력 후 저장
  2. 생성된 메뉴 "수정"에서 메뉴 영문명·그룹 영문명 값 변경 후 저장
- 기대 결과: 생성/수정 성공, 목록·모달에 영문명 값 정상 반영(API-AUTH-017/018 응답 `screenNameEn`/`groupLabelEn` 포함)

### TC-MENU-002 · 그룹 지정 시 그룹 영문명 누락(Unwanted)
- 근거: `docs/02_plan/api_spec/auth.md` API-AUTH-017 400(`groupCode` 지정 시 `groupLabelEn` 누락)
- 절차: 신규 그룹 코드만 입력하고 그룹 영문명 비운 채 저장 시도
- 기대 결과: 400 인라인 오류, 저장 차단

### TC-MENU-003 · 비관리자 접근 거부
- 근거: `docs/02_plan/screen/admin.md` SCR-ADMIN-006 "System Admin만 접근(그 외 403)"
- 절차: `im@itsm.local`(INCIDENT_MANAGER)로 로그인 후 `/admin/menus` 접근
- 기대 결과: 403 접근 거부(SCR-COM-006)

### TC-APR-001 · 승인 프로세스 생성 "규칙 정보" 카드 필드 순서
- 근거: `docs/02_plan/screen/admin.md` SCR-ADMIN-008(메타데이터 분리, 도메인→요청유형→규칙명→설명 순)
- 전제: SYSTEM_ADMIN 로그인, `/admin/approval-processes/new` 진입
- 절차: "규칙 정보" 카드 내 필드 순서 확인(도메인 선택 → 요청유형 선택(하위유형 있는 도메인 선택 시만 노출) → 규칙명 → 설명)
- 기대 결과: 명시된 순서로 렌더링, 승인 단계 카드 스택과 분리된 별도 카드로 표시

### TC-APR-002 · 승인 단계 1/2단계 재번호
- 근거: `docs/02_plan/screen/admin.md` SCR-ADMIN-008(구 2단계→1단계 승인 요청자, 구 3단계→2단계 승인자)
- 절차: 도메인 선택 후 승인 단계 카드 스택 확인
- 기대 결과: "1단계 · 승인 요청자" 박스(고정, 드래그 불가) → "2단계 · 승인자" 박스 스택(가변, 드래그 가능) 순서로 표시, 도메인/요청유형 선택 카드는 별도 표시되지 않음(구 0/1단계 없음)

### TC-APR-003 · 승인 프로세스 생성/편집 화면 i18n(영어 전환)
- 근거: `docs/02_plan/screen/common.md` 6.8절("`approval-process-flow.tsx` 카드 스택·역할 선택 패널·드래그 앤 드롭 UI 문구" 누락 보완)
- 절차: 헤더 언어를 English로 전환 후 `/admin/approval-processes/new` 재진입, 카드 스택 문구(1단계/2단계 타이틀, "역할 선택", "승인자 추가", 승인자 없음 안내 등) 확인
- 기대 결과: 원시 한국어 노출 없이 전 문구 영어로 렌더링

### TC-APR-004 · 승인자 0개 저장(Unwanted, 회귀 확인)
- 근거: `docs/02_plan/api_spec/auth.md` API-AUTH-027(steps 빈 배열 허용), `docs/02_plan/screen/admin.md` "승인자 없음 안내"
- 절차: 도메인·규칙명만 입력하고 승인자 박스 추가 없이 "생성 완료" 클릭
- 기대 결과: "승인자 없이 바로 진행됩니다" 확인 다이얼로그 표시 후 저장 성공(메타데이터 분리 이후에도 동작 유지)

### TC-BG-001 · 페이지 배경색 라이트 테마 흰색 전환
- 근거: `docs/02_plan/screen/common.md` 2.1절(`--background` #F9FAFB→#FFFFFF)
- 절차: 라이트 테마 상태에서 대시보드·메뉴 관리 화면의 콘텐츠 영역 배경색(`--background`) 계산값 확인
- 기대 결과: `#FFFFFF`(순백), 카드(`--card`)는 Elevation 그림자/테두리로 배경과 구분됨

### TC-BG-002 · 다크 테마·사이드바 배경 미변경(회귀)
- 근거: `docs/02_plan/screen/common.md` 2.1절("사이드바(`--sidebar`)와 다크 테마 값은 변경하지 않는다")
- 절차: 테마 토글로 다크 전환 후 `--background` 값 확인, 라이트/다크 공통으로 사이드바(`--sidebar`) 배경값 확인
- 기대 결과: 다크 `--background` #161A1F 유지(변경 없음), 사이드바 #1F2937 유지(라이트/다크 무관)

### TC-API-001 · 메뉴 목록/생성/수정 API 이중언어 필드(BE)
- 근거: `docs/02_plan/api_spec/auth.md` API-AUTH-016/017/018
- 절차: 로그인 후 Access Token으로 `GET/POST/PATCH /api/v1/admin/screens` 직접 호출(신규 메뉴 생성 → 응답에 `screenNameEn`/`groupLabelEn` 포함 확인 → 수정 → 재조회)
- 기대 결과: 응답 스키마에 `screenNameEn`/`groupLabelEn` 포함, 저장·수정 값 일치

### TC-API-002 · 내 메뉴 조회 API 이중언어 필드(BE)
- 근거: `docs/02_plan/api_spec/auth.md` API-AUTH-022
- 절차: `GET /api/v1/menus/mine` 호출
- 기대 결과: `groups[].groupLabelEn`, `groups[].items[].screenNameEn` 포함
