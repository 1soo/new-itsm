# 통합 테스트 결과 — auth (20260712-131458)

## 요약
- 총 17건 · 성공 17 · 실패 0
- 참고: 테스트 중 auth 도메인 파일 목록에 없는 공용 컴포넌트(`common` 소유) 2건에서 미번역 aria-label을 발견해 별도로 보고(auth phase 결함 아님, 하단 "참고 사항" 절 참조)

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test` BUILD SUCCESSFUL(14s, up-to-date), `npm run build` 성공(청크 크기 경고는 기존부터 존재, 무관) | - |
| TC-AUTH-I18N-001 | PASS | 로그아웃 후 로그인 화면 재확인: "ITSM Platform Login" 타이틀, Email/Password 라벨, "Log In" 버튼, "POC Test Accounts (demo by role — remove before production)" 안내, 표 헤더(Email/Password/Role), 17개 역할 라벨 전부 영어 전환 확인 | - |
| TC-AUTH-I18N-002 | PASS | 잘못된 비밀번호로 로그인 시도 시 "The email or password is incorrect." 오류 메시지 영어 전환 확인(401/403 통일 메시지 정책 유지) | - |
| TC-AUTH-I18N-003 | PASS | 내 프로필: "My Profile"/"Change Password" 버튼/"Account Info"/"Name"/"Email"/"Status"/"Active"/"Roles" 전부 영어 전환 | - |
| TC-AUTH-I18N-004 | PASS | 비밀번호 변경: 라벨(Current/New/Confirm Password)·힌트·"Cancel"/"Save" 영어 전환. 정책 미충족 값("a") 제출 시 "Password must be at least 8 characters." 인라인 오류 영어 전환 확인 | - |
| TC-AUTH-I18N-005 | PASS | 계정 목록: "Account Management"/"Create Account", 필터(Email/Name/Status/Role)/"Search", 표 헤더(Name/Email/Roles/Status/Created At), 상태 배지(Inactive) 영어 전환. 필터·검색 기능 회귀 없음 | - |
| TC-AUTH-I18N-006 | PASS | 계정 생성: 폼 라벨·힌트·"Cancel"/"Save" 영어 전환. 중복 이메일(admin@itsm.local) 제출 시 409 "This email is already in use." 인라인 오류 확인. 정상 값(tester_auth_check@itsm.local)으로 재제출 시 SweetAlert2 토스트 "Account created successfully" 확인 후 목록 복귀(회귀 없음, 계정 id 41 생성) | - |
| TC-AUTH-I18N-007 | PASS | 계정 상세(id 41): "Account Details"/"Back to List"/"Account ID"/"Edit Info"/"Role Management"/"Assign Role"/"Select role"/"Account Status" 영어 전환. 역할 부여 시 토스트 "Role assigned successfully", 회수 시 "Role revoked successfully" 확인(기능 정상). "Deactivate" 클릭 시 SweetAlert2 확인 다이얼로그("Deactivate Account"/"Are you sure you want to deactivate this account? A deactivated account cannot log in."/"Cancel"/"Deactivate") 영어 전환, 확인 후 상태 Inactive 전환 및 "Activating will allow this account to log in again."/"Activate" 버튼으로 정상 토글(회귀 없음) | - |
| TC-AUTH-I18N-008 | PASS | 역할 관리: "Role Management"/"Create Role", 표 헤더(Role Code/Role Name/Description/Assigned Users) 영어 전환. 역할명·설명 값은 DB 데이터(role.name/description)라 번역 대상 아님(원문 유지, 결함 아님) | - |
| TC-AUTH-I18N-009 | PASS | 감사 로그: "Audit Log", 필터(Event Type/Actor/Target/From Date/To Date)/"Search", 표 헤더(Time/Event/Actor/Target/Result), 이벤트 유형 필터 옵션 전체(All/Login/Logout/Token Refresh/Account Change/Role Change), 결과값(Success) 영어 전환. 날짜는 ko-KR 포맷 유지 | - |
| TC-AUTH-I18N-010 | PASS | 메뉴 관리: "Menu Management"/"Create Menu", 표 헤더(Group/Menu Name/Path/Icon/Order/Visible Roles/Actions), 값(Public/"N roles"), 액션 버튼(Role Mapping/Edit/Delete) 영어 전환. 역할 매핑 패널: 타이틀("Role Mapping — {screenName}")·안내 문구("Only checked roles can see this menu in the sidebar. If no role is checked, it is visible to all authenticated users.")·"Close" 버튼 영어 전환, 체크박스 토글 즉시 반영 확인(회귀 없음, 원상 복구함). 메뉴명 값(screen.screenName)은 DB 데이터라 번역 대상 아님 | - |
| TC-AUTH-I18N-011 | PASS | 승인 프로세스 목록: "Approval Processes"·설명 문구, "Domain" 필터, "Create Process", 표 헤더(Rule Name/Domain/Request Type/Requester Roles/Priority/Steps), tier 배지("Requester Role Specific"), "All"/"N steps" 영어 전환. 도메인 값은 BE 제공 마스터 데이터(API-AUTH-023)라 번역 대상 아님(BE/DB 변경 없음 원칙과 일치) | - |
| TC-AUTH-I18N-012 | PASS | 승인 프로세스 생성/편집: 이 화면 소유 "Rule Info" 카드(Rule Name/Description (optional)/Save) 영어 전환 확인. 공용 `ApprovalProcessFlow` 영역(0~3단계 카드, "역할 선택"/"승인자 추가" 등)은 예상대로 한국어 유지 — dev-lead 사전 안내대로 이번 phase 대상 아님, 결함 아님 | - |
| TC-AUTH-FORMAT-REG-001 | PASS | English 상태에서도 감사 로그 시각·승인 프로세스 생성일 등 날짜 표시가 ko-KR 포맷(`2026. 7. 12. 오후 ...`) 그대로 유지, 영어 로케일로 전환되지 않음 확인 | - |
| TC-AUTH-KO-ROUNDTRIP-001 | PASS | English 상태에서 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 한국어로 복귀("계정 관리"/"계정 생성"/필터/표 헤더/상태값 전부), 누락·깨짐 없음 | - |
| TC-AUTH-CROSSREG-001 | PASS | END_USER(user@itsm.local) 로그인 후 `/admin/users` 직접 진입 시 `/403`(접근 권한이 없습니다)으로 정상 리다이렉트, 사이드바에도 관리자 메뉴 그룹 비노출(RBAC 회귀 없음) | - |

## 참고 사항 (auth phase 결함 아님 — 공용 컴포넌트 소유 이슈)

테스트 중 auth 도메인 담당 파일 목록(`LoginPage.tsx` 등 auth/admin 8개 화면)에 포함되지 않은 **공용 컴포넌트**에서 미번역 aria-label 2건을 발견했다. 두 파일 모두 `common` 도메인(dev-ui) 소유이며 SCR-COM-007 등 여러 도메인 화면이 공유하므로, common phase에서 처리되지 못하고 남은 잔여 항목으로 판단된다. auth phase 완료 기준(대상 8+3개 화면 텍스트 전환)과는 무관하여 auth 실패로 집계하지 않았으나, 향후 정리가 필요해 참고로 남긴다.

1. `source/frontend/src/components/common/pagination.tsx` — `aria-label="페이지네이션"`(nav)/`aria-label="이전 페이지"`(이전 버튼)/`aria-label="다음 페이지"`(다음 버튼) 3곳 모두 하드코딩, `useTranslation` 미적용. 계정 목록·감사 로그 등 페이지네이션이 있는 모든 화면에서 English 전환 후에도 계속 한국어로 노출(시각적 텍스트는 없고 스크린리더 접근성 라벨만 영향).
2. `source/frontend/src/components/common/multi-select.tsx` — 선택된 항목 칩의 `aria-label="선택 해제"`(제거 버튼) 하드코딩, `useTranslation` 미적용. 계정 생성 화면의 "Initial Roles" 멀티셀렉트 등에서 확인.

## 회귀 확인 요약
- 계정 생성/수정/역할 부여·회수/비활성화·활성화, 메뉴 역할 매핑 토글, 감사 로그 필터, 승인 프로세스 목록 진입 등 기존 기능 전부 텍스트 전환과 무관하게 정상 동작(회귀 없음).
- 403 접근 통제(RBAC) 회귀 없음.
