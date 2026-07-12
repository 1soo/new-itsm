# 통합 테스트 시나리오 — auth (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `admin@itsm.local`(SYSTEM_ADMIN, 관리자 8개 화면 접근용)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음) — 회귀 여부는 텍스트 전환과 무관한 기존 기능(생성/수정/삭제/필터 등) 동작 유지 확인에 집중
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/auth.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-AUTH-I18N-001 · 로그인 화면(SCR-AUTH-001) 텍스트 전환 — POC 테스트 계정 표 포함
- 근거: @docs/02_plan/screen/auth.md (SCR-AUTH-001), @docs/03_develop/plan/auth.md (LoginPage.tsx 대상)
- 전제: 미인증 상태, 헤더는 이 화면에 없으므로 언어 전환은 로그인 후 헤더 지구본으로 English 전환 → 재로그아웃 후 로그인 화면 재확인(또는 로그인 상태에서 `/login` 진입 시도)으로 검증
- 절차: 1) 임의 계정으로 로그인 2) English 전환 3) 로그아웃하여 로그인 화면 복귀
- 기대 결과: 로그인 화면 타이틀·이메일/비밀번호 라벨·로그인 버튼·"POC 테스트 계정" 안내 문구, 표 헤더(이메일/비밀번호/역할), 18개 역할 라벨까지 전부 영어로 전환

### TC-AUTH-I18N-002 · 로그인 실패 오류 메시지 전환
- 근거: @docs/02_plan/screen/auth.md (SCR-AUTH-001 상태·인터랙션 — 401/403 통일 메시지)
- 전제: English 상태
- 절차: 1) 잘못된 비밀번호로 로그인 시도
- 기대 결과: "이메일 또는 비밀번호가 올바르지 않습니다" 오류 메시지가 영어로 전환되어 표시

### TC-AUTH-I18N-003 · 내 프로필(SCR-AUTH-002) 텍스트 전환
- 근거: @docs/02_plan/screen/auth.md (SCR-AUTH-002)
- 전제: 로그인 상태, English 전환
- 절차: 1) `/profile`(또는 사이드바 "내 프로필") 진입
- 기대 결과: 페이지 타이틀·정보 카드 라벨(이름/이메일/상태)·역할 배지·"비밀번호 변경" 버튼 전부 영어로 전환

### TC-AUTH-I18N-004 · 비밀번호 변경(SCR-AUTH-003) 텍스트 전환 및 정책 오류 메시지
- 근거: @docs/02_plan/screen/auth.md (SCR-AUTH-003), `features/auth/password.ts`
- 전제: 로그인 상태, English 전환
- 절차: 1) 비밀번호 변경 화면 진입 2) 새 비밀번호에 정책 미충족 값(예: "a") 입력 후 제출 시도
- 기대 결과: 폼 라벨(현재/새/확인 비밀번호)·저장 버튼 전부 영어 전환, 정책 위반 인라인 오류(`password.tooShort`/`password.needsLetterAndDigit`)도 영어로 표시

### TC-AUTH-I18N-005 · 계정 목록(SCR-ADMIN-001) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-001)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) 계정 목록 진입 2) 필터 입력 후 검색
- 기대 결과: 필터 라벨·표 헤더·상태 배지(활성/비활성)·"계정 생성" 버튼 영어 전환. 필터·검색 결과는 회귀 없이 정상 동작

### TC-AUTH-I18N-006 · 계정 생성(SCR-ADMIN-002) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-002)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) "계정 생성" 이동 2) 이메일 중복 값으로 제출 시도(409) 3) 정상 값으로 생성
- 기대 결과: 폼 라벨·버튼 영어 전환, 409 중복 오류 메시지 영어 전환, 정상 생성 시 성공 토스트(영어) 후 목록 복귀(회귀 없음)

### TC-AUTH-I18N-007 · 계정 상세·수정(SCR-ADMIN-003) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-003)
- 전제: SYSTEM_ADMIN 로그인, English 전환, TC-AUTH-I18N-006에서 생성한 계정으로 진입
- 절차: 1) 계정 상세 진입 2) 역할 부여/회수 1회 수행 3) 비활성화 버튼 클릭 → 확인 다이얼로그 확인
- 기대 결과: 정보 수정 폼·역할 부여/회수 패널·비활성화 버튼 영어 전환, SweetAlert2 확인 다이얼로그도 영어 전환. 역할 부여/회수·비활성화 기능 자체는 회귀 없이 정상 동작

### TC-AUTH-I18N-008 · 역할 관리(SCR-ADMIN-004) 텍스트 전환
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-004)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) 역할 관리 진입 2) 표·모달 확인
- 기대 결과: 역할 목록 표 헤더·"역할 생성" 버튼·생성/수정 모달 라벨 영어 전환

### TC-AUTH-I18N-009 · 감사 로그 조회(SCR-ADMIN-005) 텍스트 전환 — 이벤트 유형 라벨 포함
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-005)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) 감사 로그 진입 2) 이벤트 유형 필터 셀렉트 옵션 확인 3) 결과 표의 이벤트 열 확인
- 기대 결과: 필터·표 헤더 영어 전환, 이벤트 유형 라벨(로그인/로그아웃/토큰 재발급/계정 변경/역할 변경 → Login/Logout/Refresh/User Change/Role Change 등) 전부 영어로 전환. 결과 값(성공/실패 배지)도 영어 전환

### TC-AUTH-I18N-010 · 메뉴 관리(SCR-ADMIN-006) 텍스트 전환 — 역할 매핑 슬라이드 패널 포함
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) 메뉴 관리 진입 2) 임의 메뉴의 "역할 매핑" 버튼 클릭 → 우측 슬라이드 패널 오픈 3) 체크박스 1개 토글
- 기대 결과: 메뉴 목록 표·"메뉴 생성" 버튼·모달 라벨 영어 전환, 역할 매핑 패널 타이틀·안내 문구("체크된 역할만...")·역할 체크박스 리스트 라벨 전부 영어 전환. 체크박스 토글 즉시 반영(회귀 없음)

### TC-AUTH-I18N-011 · 승인 프로세스 목록(SCR-ADMIN-007) 텍스트 전환 — 우선순위 tier 라벨 포함
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-007)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) 승인 프로세스 목록 진입 2) 도메인 필터 확인 3) tier 배지 확인
- 기대 결과: 도메인 필터·표 헤더·"프로세스 생성" 버튼 영어 전환, tier 배지 라벨("도메인 기본"/"요청유형 전용"/"요청자 역할 전용" → 영어) 전환 확인

### TC-AUTH-I18N-012 · 승인 프로세스 생성/편집(SCR-ADMIN-008) 화면 자체 텍스트 전환 — 공용 ApprovalProcessFlow 예외 확인
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-008), dev-lead 지시(공용 `ApprovalProcessFlow`는 이번 phase 대상 아님)
- 전제: SYSTEM_ADMIN 로그인, English 전환
- 절차: 1) 승인 프로세스 생성 화면 진입 2) 화면 자체 소유 필드("규칙 정보" 카드 — 규칙명·설명)와 공용 `ApprovalProcessFlow`(0~3단계 카드 스택) 영역을 구분해 확인
- 기대 결과: "규칙 정보" 카드(규칙명·설명 라벨, 이 화면 소유)는 영어로 전환. 공용 `ApprovalProcessFlow` 영역(0~3단계 카드·역할 선택 패널 등)은 한국어로 남아있어도 **결함 아님**(dev-ui 미착수, 향후 별도 처리 확정)

### TC-AUTH-FORMAT-REG-001 · 날짜/숫자 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 감사 로그의 "시각" 컬럼 또는 계정 상세의 생성일 값 확인
- 기대 결과: 날짜 포맷이 ko-KR 로케일(`2026. 7. 12. 오후 ...`) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-AUTH-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 로그인/프로필/관리자 화면 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)

### TC-AUTH-CROSSREG-001 · 403 접근 통제 회귀 (System Admin 전용 화면)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-001~008 "System Admin만 접근")
- 전제: SYSTEM_ADMIN이 아닌 계정(예: user@itsm.local, END_USER) 로그인
- 절차: 1) `/admin/users` 등 관리자 화면 직접 URL 진입 시도
- 기대 결과: 403(SCR-COM-006)으로 이동, 텍스트 전환 작업과 무관하게 접근 통제 회귀 없음
