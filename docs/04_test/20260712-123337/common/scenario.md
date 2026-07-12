# 통합 테스트 시나리오 — common (다국어(i18n) 지원 + SweetAlert2 도입, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `tester-admin@itsm.local`(SYSTEM_ADMIN)
- 알림 벨/승인 대기함 실데이터 확인용 신규 계정(비밀번호 `Test@1234`, `tester-admin@itsm.local`로 생성, VULNERABILITY_MANAGER): `tester_common_req@itsm.local`(요청자)/`tester_common_apv@itsm.local`(승인자) — VULN E2E 승인 규칙(approval_process id=6)을 이용해 `VULN-2026-0006`(id=12) 취약점을 REMEDIATION 전이 시도 → 409 + `approvalRequestId=52` 발급, `tester_common_apv`가 결정하지 않은 채 대기 상태로 유지(알림 벨·승인 대기함 실데이터로 사용)
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처, SCR-COM-009 SweetAlert2)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-I18N-001 · 언어 선택 아이콘 배치·팝업 노출
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002, SCR-COM-015)
- 전제: tester-admin 로그인
- 절차: 1) 헤더에서 "?" 가이드 아이콘과 테마 토글 사이 지구본 아이콘 확인 2) 지구본 아이콘 클릭
- 기대 결과: 지구본 아이콘이 가이드 아이콘 오른쪽·테마 토글 왼쪽에 위치. 클릭 시 아이콘 바로 아래 우측 정렬 팝업에 "한국어"/"English" 2개 항목 노출, 현재 선택(한국어)에 체크 아이콘 표시

### TC-I18N-002 · 언어 전환 — 새로고침 없이 즉시 텍스트 반영
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 상태·인터랙션, 6.4절 알림 조립 텍스트)
- 전제: TC-I18N-001 팝업 오픈 상태
- 절차: 1) "English" 클릭 2) 헤더·사이드바·대시보드 텍스트 확인 3) 팝업 재오픈 후 선택 표시 확인
- 기대 결과: 새로고침 없이 헤더 가이드/언어 선택/알림 aria-label, 대시보드 환영 문구 등이 영어로 즉시 전환. 팝업 재오픈 시 "English" 항목에 체크 아이콘

### TC-I18N-003 · 언어 선택 유지(재방문)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 상태·인터랙션 1번째 EARS, `itsm-language` localStorage 키)
- 전제: TC-I18N-002에서 English로 전환된 상태
- 절차: 1) 페이지 새로고침(F5)
- 기대 결과: 새로고침 후에도 영어로 유지(재로그인 불필요), `localStorage.itsm-language === "en"`

### TC-I18N-004 · 언어 선택 미저장 시 기본값(한국어)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 상태·인터랙션 1번째 EARS)
- 전제: 새 playwright context(storage 초기화, `itsm-language` 키 없음)
- 절차: 1) 로그인 후 첫 화면 진입
- 기대 결과: 저장된 값이 없으므로 한국어 기본 렌더링

### TC-I18N-005 · 사용자 가이드(SCR-COM-012) 언어 전환 — 개요/도메인 11개/역할 16개
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 구현 참고 "다국어", 6.3절 문서 단위 병행 파일)
- 전제: tester-admin 로그인, 한국어 상태에서 시작
- 절차: 1) 헤더 "?" 클릭 → `/guide` 이동 2) 개요·"도메인 및 원칙"(11개 아코디언 중 2~3개 펼침)·"역할별 수행 내용과 방법"(내 역할 배지 확인) 한국어 확인 3) 지구본 아이콘으로 English 전환 4) 동일 섹션 재확인
- 기대 결과: 전환 후 개요 본문, 11개 도메인 아코디언 제목/본문, 16개 역할 아코디언 제목/본문, "내 역할" 배지 라벨이 모두 영어로 렌더링(`user-guide-content.en.md` 기반). TOC 3개 링크도 영어로 전환

### TC-I18N-006 · 403/404 화면 텍스트 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-006, 6.4절 대상 아님이나 forbidden/notFound도 공통 문구 전환 대상)
- 전제: tester-admin 로그인
- 절차: 1) 존재하지 않는 경로(`/no-such-page`) 진입해 404 확인(한국어) 2) 지구본 아이콘으로 English 전환 3) 동일 경로 재진입
- 기대 결과: 한국어에서는 기존 문구, English 전환 후 403/404 안내 문구·버튼 라벨이 영어로 전환("404" 숫자 자체는 불변)

### TC-SWAL-001 · 확인 다이얼로그 SweetAlert2 렌더링 — 로그아웃(라이트)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-009 구현 참고, `ConfirmDialog` 래퍼)
- 전제: tester-admin 로그인, 라이트 테마
- 절차: 1) 사용자 메뉴 > 로그아웃 클릭
- 기대 결과: Radix Dialog가 아닌 SweetAlert2 팝업(`.swal2-popup` DOM, `itsm-swal-popup` 커스텀 클래스)으로 확인/취소 버튼 노출, 프로젝트 토큰 기반 스타일(카드 배경·둥근 모서리) 적용. 취소 클릭 시 로그아웃 취소되고 세션 유지

### TC-SWAL-002 · 확인 다이얼로그 다크모드 렌더링
- 근거: @docs/02_plan/screen/common.md (SCR-COM-009 구현 참고 "다크모드")
- 전제: tester-admin 로그인, 테마 토글로 다크 전환
- 절차: 1) 사용자 메뉴 > 로그아웃 클릭 2) 팝업 확인
- 기대 결과: 팝업이 `data-theme="dark"` 토큰(다크 배경/전경색)을 상속해 다크 스타일로 렌더링(라이트 스타일 잔존 없음)

### TC-SWAL-003 · 확인 다이얼로그 확인 클릭 — 실제 동작(로그아웃) 정상
- 근거: 상동, SCR-COM-005
- 절차: 1) 사용자 메뉴 > 로그아웃 클릭 2) 팝업에서 확인 클릭
- 기대 결과: 로그아웃 API 호출 후 로그인 화면으로 이동(회귀 없음)

### TC-SWAL-004 · 토스트 SweetAlert2 렌더링 — 성공(우상단)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-009 구현 참고 "토스트")
- 전제: tester-admin 로그인
- 절차: 1) 저장류 성공 액션 1건 수행(예: 프로필 저장 또는 테마 전환 등 토스트를 띄우는 기존 동작)
- 기대 결과: 화면 우상단에 SweetAlert2 토스트(`.swal2-toast`)로 성공 메시지 노출, 일정 시간 후 자동 소멸(회귀 없음)

### TC-SWAL-005 · 범용 모달(Modal)은 SweetAlert2 미적용(Radix Dialog 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-009 구현 참고 "확정된 결정 1", `components/common/modal.tsx` 제외)
- 절차: 1) 폼/상세 등 비파괴 콘텐츠를 여는 기존 Modal 사용 화면 진입(예: 관리자 화면의 생성/편집 모달)
- 기대 결과: 해당 모달은 여전히 Radix Dialog(`role="dialog"`, `.swal2-popup` 아님)로 렌더링, 레이아웃/동작 변경 없음

### TC-NOTI-001 · 알림 벨 — 실 데이터(승인 대기) 정상 표시, title 누락 방어 로직 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 알림 드롭다운·매핑 표), `routes/AppLayout.tsx` `truncateNotificationText`(null/undefined 방어)
- 전제: `tester_common_apv@itsm.local` 로그인(VULN-2026-0006 승인 대기 1건 존재, approvalRequestId=52)
- 절차: 1) 알림 벨 클릭
- 기대 결과: 드롭다운에 "취약점 승인" 계열 도메인 라벨 + `VULN-2026-0006` 관련 요약(`Common i18n test vulnerability`) 1행, 상대 시간(방금 전/N분 전) 정상 표시. 크래시 없이 정상 렌더링(자산 만료 등 `title` 필드가 없는 유형이 섞여도 방어적으로 빈 문자열 처리되어 레이아웃 깨짐 없음)

### TC-NOTI-002 · 알림 벨 — 언어 전환 시 도메인 라벨/상대시간/버튼 텍스트 전환
- 근거: @docs/02_plan/screen/common.md (6.4절 알림 메시지 번역 처리)
- 전제: TC-NOTI-001과 동일 계정, 알림 드롭다운에 항목 1건 존재
- 절차: 1) 지구본 아이콘으로 English 전환 2) 알림 벨 재클릭
- 기대 결과: 도메인 라벨(예: "Vulnerability Approval"), "상세 보기"/"모두 지우기" 버튼 라벨이 영어로 전환. 티켓 요약(`ticketSummary`, 사용자 입력 데이터)은 번역되지 않고 원문 그대로 유지(확정된 결정 — 라벨만 번역)

### TC-NOTI-003 · 알림 개별 확인처리(X) 정상 동작(회귀)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 개별 확인처리)
- 전제: TC-NOTI-001 상태
- 절차: 1) 알림 라인의 X 버튼 클릭
- 기대 결과: 해당 알림이 목록에서 즉시 제거, 뱃지 카운트 -1(SweetAlert2 도입과 무관하게 회귀 없음)

### TC-COM014-001 · 승인 대기함(SCR-COM-014) 언어 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-014, 6.7절 `common` 네임스페이스 대상 SCR-COM-014)
- 전제: `tester_common_apv@itsm.local` 로그인, `/approvals` 진입(한국어 상태)
- 절차: 1) 목록 표 헤더·도메인 필터 라벨 한국어 확인 2) 지구본 아이콘으로 English 전환 3) 동일 화면 재확인
- 기대 결과: 목록 표 헤더·필터·차수 진행 상태 라벨이 영어로 전환. 승인/반려 버튼 정상 동작(회귀 확인은 범위 내 언어 전환만, 결정 로직 자체는 approval-engine 도메인에서 이미 검증됨)

### TC-SEARCH-001 · 통합 검색 결과(SCR-COM-011) 언어 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), 6.7절(`features/search/status.ts`)
- 전제: tester-admin 로그인
- 절차: 1) 헤더 검색창에 키워드 입력 후 Enter → `/search` 결과 페이지 이동(한국어 확인) 2) English 전환 3) 재조회 없이 화면 텍스트 재확인(또는 재검색)
- 기대 결과: 도메인 배지 라벨·페이지네이션 등 chrome 텍스트가 영어로 전환. 결과 원문(제목·발췌)은 서버 응답 그대로 유지

### TC-DASH-001 · 대시보드(SCR-COM-013) 언어 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-013)
- 전제: tester-admin 로그인
- 절차: 1) 로그인 성공 시 대시보드 진입(한국어 환영 문구 확인) 2) English 전환
- 기대 결과: 환영 메시지(사용자 이름 포함)가 영어로 전환

### TC-FORMAT-REG-001 · 날짜/숫자 포맷은 언어 전환과 무관하게 `ko-KR` 유지(회귀)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 상태·인터랙션 "확정된 결정 2")
- 전제: 날짜/숫자가 표시되는 화면(예: 알림 상대 시간의 절대 날짜 포맷, 승인 대기함 요청일) English 전환 상태
- 절차: 1) English 전환 후 날짜 표시 형식 확인
- 기대 결과: 날짜 포맷은 영어 로케일(`M/D/YYYY` 등)로 바뀌지 않고 기존 `ko-KR`(예: `2026-07-12` 계열) 포맷 그대로 유지

### TC-DEPREG-001 · sonner 패키지/컴포넌트 완전 제거 확인(orphan 정리 회귀)
- 근거: dev-lead 변경 요약("삭제: `components/ui/sonner.tsx` + `sonner` 패키지")
- 절차: 1) `source/frontend/package.json`에 `sonner` 의존성 부재 확인 2) `source/frontend/src/components/ui/sonner.tsx` 파일 부재 확인 3) 전체 소스에서 `sonner` import 잔존 여부 검색
- 기대 결과: 의존성·파일·import 모두 제거되어 잔존하지 않음(빌드 경고/오류 없음)

### TC-CROSSREG-001 · 인증 가드(SCR-COM-005)·테마 토글(SCR-COM-010) 회귀
- 근거: @docs/02_plan/screen/common.md (SCR-COM-005, SCR-COM-010)
- 절차: 1) 미인증 상태로 보호 라우트 진입 → 로그인 리다이렉트 확인 2) 로그인 후 테마 토글 클릭(라이트↔다크) 정상 전환 확인
- 기대 결과: i18n/SweetAlert2 도입과 무관하게 기존 동작 그대로 유지(회귀 없음)
