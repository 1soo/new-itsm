# 통합 테스트 결과 — common (20260712-123337)

## 요약
- 1차: 총 22건 · 성공 20 · 실패 2
- 재테스트(결함 2건 수정 후): 2건 모두 PASS 확인 — 최종 22건 전부 PASS

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `./gradlew build -x test` BUILD SUCCESSFUL(32s), `npm run build`(tsc -b && vite build) 성공(경고: 청크 크기 500KB 초과 — 기존부터 존재하던 경고, 이번 변경과 무관) | - |
| TC-I18N-001 | PASS | 헤더에 "?" 가이드 아이콘과 테마 토글 사이 지구본 아이콘 배치 확인. 클릭 시 우측 정렬 팝업에 "한국어"(체크)/"English" 노출 | shots/tc-i18n-001-popup.png |
| TC-I18N-002 | PASS | English 클릭 시 새로고침 없이 헤더·대시보드 즉시 영어 전환("User guide"/"Select language"/"Notifications, N"/"Welcome, {name}" 등). 팝업 재오픈 시 English에 체크 표시 | - |
| TC-I18N-003 | PASS | F5 새로고침 없이 확인(브라우저 정책상 실제로는 `navigate` 재호출로 대체 검증) — `localStorage.itsm-language === "en"` 유지, 재진입 시 영어 유지 확인 | - |
| TC-I18N-004 | PASS | storage 초기화 후 재로그인 시 한국어 기본 렌더링("사용자 가이드"/"언어 선택"/"테마 전환"/"환영합니다, ○○○님" 등) | - |
| TC-I18N-005 | PASS | `/guide` 진입 후 English 전환 시 TOC 3개 링크, 개요 전체 문단, 11개 도메인 아코디언 제목, 16개 역할 아코디언 제목·"My Role" 배지·본문까지 전부 영어로 렌더링(`user-guide-content.en.md` 반영 확인) | - |
| TC-I18N-006 | PASS | `/no-such-page` → 404("404" 숫자 불변 + 영문 안내·"Go Home"), `/403` → "Access Denied"/"You do not have permission..."/"Go Back" 모두 영어 전환 확인 | - |
| TC-SWAL-001 | PASS | 로그아웃 확인 다이얼로그가 `.swal2-popup.itsm-swal-popup` DOM(Radix Dialog 아님)으로 렌더링, Cancel 클릭 시 로그아웃 취소·세션 유지 확인 | shots/tc-swal-001-logout-confirm-light.png |
| TC-SWAL-002 | PASS | 다크 테마 전환 후 팝업이 다크 토큰(카드 배경 어둡게, 텍스트 밝게) 상속해 렌더링 | shots/tc-swal-002-logout-confirm-dark.png |
| TC-SWAL-003 | PASS | 팝업에서 "Log Out" 확인 클릭 시 로그아웃 API 호출 후 `/login`으로 정상 이동 | - |
| TC-SWAL-004 | PASS | 승인 처리(Approve) 시 `.swal2-toast` DOM으로 "Approved." 토스트 확인(우상단, 자동 소멸) | shots/tc-swal-004-toast-success.png |
| TC-SWAL-005 | PASS | 승인 상세(비파괴 폼) 다이얼로그는 `role="dialog"` + sr-only "닫기" 버튼(Radix Dialog 시그니처)로 렌더링, `.swal2-popup` 아님 — `components/common/modal.tsx` 그대로 유지 확인(소스 리뷰로 교차 확인) | - |
| TC-NOTI-001 | PASS | `tester_common_apv`(VULNERABILITY_MANAGER) 계정에 실제 승인 대기 1건(VULN-2026-0006, approvalRequestId=52) 생성 후 알림 벨 클릭 시 크래시 없이 정상 렌더링("Vulnerability Approval" 도메인 라벨 + "5m ago" + 요약 텍스트 truncate 정상) | shots/tc-noti-001-en.png |
| TC-NOTI-002 | PASS | English→한국어 전환 후 동일 알림이 "취약점 승인"/"5분 전"/"모두 지우기"/"상세 보기"/"알림 확인처리"로 정상 전환, 티켓 요약("Common i18n test vulnerability")은 번역되지 않고 원문 유지 확인 | - |
| TC-NOTI-003 | PASS | 개별 X(알림 확인처리) 클릭 시 항목 즉시 제거, 뱃지 배지 사라짐(0건), "새로운 알림이 없습니다" 안내 전환 확인 | - |
| TC-COM014-001 | PASS | `/approvals` 진입 후 English 전환 시 "Approval Inbox" 타이틀·설명, "Domain" 필터, 표 헤더(Ticket Type/Ticket/Step/Requester/Requested At), "Details"/"Approve"/"Reject"/"Cancel" 버튼, 빈 상태("There are no pending approvals.") 모두 영어로 전환 확인 | - |
| TC-SEARCH-001 | PASS | `/search?keyword=test` 진입 시 "Search Results"/"Results for..."/"Domain"·"Title"·"Status"·"Updated At" 헤더/"Search" 버튼/도메인 배지("Knowledge") 영어 전환 확인. 상태 배지 값("게시"/"검토")은 knowledge 도메인 자체 번역 미착수 상태로 한국어 유지 — knowledge 도메인 전환(task #32, 향후 진행 예정) 범위이며 이번 common 도메인 결함 아님 | - |
| TC-DASH-001 | PASS | 로그인 계정마다 "Welcome, {name}" 환영 문구 영어 전환 확인(admin/공통테스트 계정 모두) | - |
| TC-FORMAT-REG-001 | PASS | English 전환 상태에서도 승인 대기함 "Requested At" 값이 `2026. 7. 12. 오후 12:38:29`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 | - |
| TC-DEPREG-001 | PASS | `package.json`에 `sonner` 의존성 없음, `components/ui/sonner.tsx` 파일 없음, 전체 소스(`src/`)에 `sonner` 문자열 잔존 없음(grep 0건) | - |
| TC-CROSSREG-001 | PASS | storage/cookie 초기화 후 보호 라우트(`/approvals`) 직접 진입 시 `/login`으로 정상 리다이렉트. 테마 토글 클릭 시 라이트↔다크 정상 전환(회귀 없음) | - |
| TC-I18N-002(부가) | **FAIL** | 테마 토글 버튼의 `aria-label`이 English 전환 후에도 "테마 전환"(한국어)으로 고정 — `theme-toggle.tsx:44`가 `useTranslation` 없이 `aria-label="테마 전환"`을 하드코딩. 설계서 6.4절이 `theme-toggle.tsx`를 명시적으로 i18n 대상(`common:header.*Aria`)으로 지정했으나 이번 변경에 미포함됨(사이드바 토글 aria-label은 "Toggle sidebar"로 정상 전환되어 대조됨) | - |
| TC-COM014-001(부가) | **FAIL** | 승인 상세 다이얼로그 내부 승인 차수 진행 패널(`components/common/approval-step-progress.tsx`)이 English 전환 후에도 "1차"/"대기중"/"대기"/"1차 역할별 결정 현황 (전체 승인 필요)"/역할명 등 전부 한국어로 고정 — `useTranslation` 미적용(하드코딩 상수 `ROLE_DECISION_LABEL`/`STEP_STATUS_LABEL` 등). SCR-COM-014는 6.7절 `common` 네임스페이스 대상에 명시적으로 포함되어 있어 범위 내 결함. 부가로 다이얼로그 닫기 버튼의 sr-only 텍스트("닫기", `components/ui/dialog.tsx:44`)도 언어 전환과 무관하게 한국어 고정(스크린리더 접근성 텍스트, 시각적 노출은 없음) | - |

## 실패 항목 분석
- **[결함 1] 테마 토글 aria-label 미번역**: `source/frontend/src/components/layout/theme-toggle.tsx`의 `aria-label="테마 전환"`이 하드코딩되어 있어 English 전환 후에도 한국어로 남는다. 설계서 `docs/02_plan/screen/common.md` 6.4절 표에 `header.tsx`/`theme-toggle.tsx`/언어 선택 버튼의 아이콘 전용 버튼 `aria-label`을 `common:header.*Aria`로 전환 대상으로 명시했으나 이번 변경 파일 목록에 `theme-toggle.tsx`가 빠져 누락된 것으로 판단됨. 재현: 로그인 → English 전환 → 테마 토글 버튼 접근성 트리 확인(`aria-label` 값이 "테마 전환"으로 남음, 사이드바 토글은 정상적으로 "Toggle sidebar"로 전환되어 대조 확인).
- **[결함 2] 승인 대기함(SCR-COM-014) 상세 승인 차수 패널 미번역**: `source/frontend/src/components/common/approval-step-progress.tsx`에 `useTranslation` 훅이 전혀 적용되어 있지 않아, 차수 라벨("N차"), 상태 라벨(`STEP_STATUS_LABEL`: 대기중/대기 등), 역할별 결정 라벨(`ROLE_DECISION_LABEL`: 대기/승인/반려), "N차 역할별 결정 현황 (전체 승인 필요/역할 중 하나)" 문구, 반려 사유 문구가 English 전환 후에도 한국어로 고정 렌더링된다. `ApprovalInboxPage.tsx`는 `useTranslation("common")`을 적용해 페이지 타이틀·표 헤더·버튼은 정상 전환되었으나, 이 페이지가 재사용하는 공용 컴포넌트(`approval-step-progress.tsx`, 도메인 상세 화면들의 `approval-panel.tsx`에서도 공유)가 누락되어 화면 하나에 영어/한국어가 섞여 노출된다. `docs/02_plan/screen/common.md` 6.7절 인벤토리 표가 SCR-COM-014를 `common` 네임스페이스 대상으로 명시하고 있어 범위 내 결함이다. 재현: `/approvals`에서 English로 전환 후 대기 항목의 "Details" 클릭 → 상세 다이얼로그의 차수/역할 결정 현황 영역이 한국어로 남음(제목·버튼 등 다이얼로그 바깥 요소는 정상 영어). 부가로 다이얼로그 닫기 버튼의 스크린리더 전용 텍스트("닫기", `components/ui/dialog.tsx:44`)도 하드코딩되어 있어 언어 전환과 무관 — 시각적 노출은 없으나 접근성 관점에서 함께 확인 필요.
- 두 결함 모두 dev-lead가 전달한 변경 파일 목록에 `theme-toggle.tsx`/`approval-step-progress.tsx`가 포함되지 않은 것과 일치 — 개발 단계에서 누락된 것으로 보이며, 해당 두 파일에 `useTranslation("common")` 적용 및 `common.json`(ko/en) 키 추가로 해결 가능할 것으로 판단.

## 테스트 데이터 안내
- 알림/승인 대기함 실데이터 검증을 위해 VULNERABILITY_MANAGER 역할의 임시 계정 `tester_common_req@itsm.local`/`tester_common_apv@itsm.local`(비밀번호 `Test@1234`)을 생성하고, VULN-2026-0006/0007 두 건의 승인 인스턴스(approvalRequestId 52/53)를 만들어 사용했다. 테스트 완료 후 두 계정 모두 비활성화(INACTIVE) 처리했다(계정 id 39/40).

## 재테스트 (결함 2건 수정 후)

dev_ui가 `theme-toggle.tsx`(aria-label)·`approval-step-progress.tsx`(useTranslation 적용) 2건을 수정하고, 추가로 `approval-panel.tsx`(패널 타이틀)·`ui/dialog.tsx`(닫기 버튼 sr-only 텍스트)도 선제적으로 전환했다. `npm run build` 통과(dev_ui 보고). 새 playwright 컨텍스트(storage 초기화)로 재검증했다. 재테스트용으로 임시 계정을 재활성화하고 신규 승인 인스턴스(VULN-2026-0008, approvalRequestId=54)를 생성해 검증 후 계정은 다시 비활성화했다.

| 항목 | 결과 | 실제 동작 |
|------|------|-----------|
| 결함 1 재검증 — 테마 토글 aria-label | **PASS** | English 전환 후 테마 토글 버튼 `aria-label`이 "Toggle theme"로 정상 전환(이전 "테마 전환" 고정 문제 해소) |
| 결함 2 재검증 — 승인 상세 패널(SCR-COM-014) | **PASS** | `/approvals` → Details 진입 시 "Step 1"/"In Progress"/"Step 1 Role Decisions (All roles required)"/"Pending"/다이얼로그 "Close" 버튼까지 전부 영어로 전환. 역할명("취약점 관리 담당자")은 DB `role.name` 데이터 값이라 번역 대상 아님(사용자 입력 데이터와 동일 원칙, 결함 아님) | shots/tc-retest-approval-panel-en.png 참조 |

**재테스트 결론**: 2건 모두 수정 확인. common 도메인 통합 테스트 전 항목 PASS.
