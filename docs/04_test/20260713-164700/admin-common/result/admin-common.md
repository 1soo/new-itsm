# 통합 테스트 결과 — 관리자/공통 UI (admin-common) (20260713-164700)

## 요약
- 총 17건 · 성공 16 · 실패 1
- **재테스트(같은 날 후속)**: TC-MENU-002 dev-lead 수정 후 재검증 → PASS로 전환. 최종 17건 전체 PASS.
  - API 직접 호출: `groupCode` 지정 + `groupLabelEn=""` → 400(VALIDATION_ERROR) 확인
  - UI 재현: Create Menu 모달에서 신규 그룹 + 영문 라벨 공란 저장 시도 → 인라인 오류("groupCode 지정 시 groupLabelEn은 필수입니다.") 표시, 데이터 미생성
  - 회귀 확인: 동일 폼에 영문 그룹 라벨 채워 저장 → 201 정상 생성(정상 케이스 유지), 테스트 데이터 삭제로 정리
  - `ScreenAdminServiceTest` 재실행 → BUILD SUCCESSFUL

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `./gradlew clean compileJava test` BUILD SUCCESSFUL (7m22s), 전체 테스트 통과 | - |
| TC-BUILD-002 | PASS | `npm run build`(tsc -b && vite build) 정상 완료, 타입 오류 없음 | - |
| TC-DB-001 | PASS | `30_auth_screen_i18n.sql` 정상 적용. `screen_name_en` NULL 0건(전 80건 NOT NULL), `group_code IS NOT NULL AND group_label_en IS NULL` 0건 | psql 직접 조회로 확인 |
| TC-AUTH-001 | PASS | admin@itsm.local 로그인 성공, Access/Refresh Token 발급 확인 | - |
| TC-I18N-001 | PASS | 언어 전환 전 사이드바 한국어(`screenName`/`groupLabel`) 정상 표시("메뉴 관리","승인 프로세스 목록","관리자" 등) | - |
| TC-I18N-002 | PASS | 헤더 지구본→English 선택 시 새로고침 없이 즉시 `screenNameEn`/`groupLabelEn`로 전환("Menu Management","Admin","Approval Process List" 등), 원시 한국어 노출 없음 | shots/TC-I18N-002_sidebar_en_bg_white.png |
| TC-MENU-001 | PASS | 메뉴 생성(그룹 신규 입력 포함, 영문명 포함)·수정(영문명·그룹영문명 변경) API 201/200 정상, 응답에 `screenNameEn`/`groupLabelEn` 값 일치 확인. 테스트 데이터는 이후 soft delete로 정리 | - |
| TC-MENU-002 | PASS(재테스트) | 최초 FAIL 후 dev-lead 수정 반영해 재검증 → `groupLabelEn` 공란 저장 시 400 정상 반환, UI 인라인 오류 표시 확인 | 아래 "실패 항목 분석"(최초 발견) 참조 |
| TC-MENU-003 | PASS | INCIDENT_MANAGER(im@itsm.local)로 `/admin/menus` 접근 시 FE 라우트 가드가 `/403`으로 리다이렉트("접근 권한이 없습니다"). BE `GET /api/v1/admin/screens` 직접 호출도 403 확인(defense-in-depth) | shots/TC-MENU-003_403.png |
| TC-APR-001 | PASS | "규칙 정보" 카드 필드 순서 Domain → Request Type(도메인 선택 후 hasRequestSubtype=true인 SERVICE_REQUEST에서만 노출) → Rule Name → Description(optional) 확인, 승인 단계 카드 스택과 분리되어 별도 카드로 렌더링 | - |
| TC-APR-002 | PASS | 승인 단계 카드 스택이 "Step 1 · Requester"(고정)→"Step 2 · Approvers"(가변) 순서로 렌더링, 구 0/1단계(도메인/요청유형) 카드 없음 | - |
| TC-APR-003 | PASS | 언어를 English로 전환 후 재진입 시 카드 스택·역할 선택 패널 문구("Select Role","Add Approver","Step 1 · Requester","Step 2 · Approvers", 승인자 없음 확인 다이얼로그 등) 전부 영어로 렌더링, 원시 한국어 노출 없음. 단, 역할 선택 패널의 스크린리더 전용(sr-only) 닫기 버튼 접근성 라벨("닫기")은 미번역 상태(시각적으로는 노출되지 않음) — 아래 참고 | - |
| TC-APR-004 | PASS | 승인자 박스 0개인 채 "Create" 클릭 시 "Proceed without approvers?" 확인 다이얼로그 표시 후 정상 저장(201), 메타데이터 분리 개편 후에도 회귀 없음 | - |
| TC-BG-001 | PASS | 라이트 테마 `--background` 계산값 `#ffffff` 확인(대시보드/메뉴관리 등) | - |
| TC-BG-002 | PASS | 다크 테마 `--background` `#161a1f`(변경 없음), 라이트/다크 무관 `--sidebar` 값(`#1f2937`/`#10141a`) 변경 없음 확인 | - |
| TC-API-001 | PASS | `POST/PATCH /api/v1/admin/screens` 응답에 `screenNameEn`/`groupLabelEn` 포함, 저장·수정 값 round-trip 일치(TC-MENU-001과 함께 검증) | - |
| TC-API-002 | PASS | `GET /api/v1/menus/mine` 응답에 `groups[].groupLabelEn`, `groups[].items[].screenNameEn` 전 그룹·항목 포함 확인 | - |

## 실패 항목 분석

- **TC-MENU-002**: `POST /api/v1/admin/screens`에서 `groupCode`를 지정하고 `groupLabelEn`을 빈 문자열(`""`)로 보내면 400이 아니라 201로 생성됨.
  - 재현: FE 메뉴 생성 모달에서 "Add new group..." 선택 → Group code/Group label(한국어) 입력, Group label (English) 공란 유지 → 저장. 요청 바디: `{"groupCode":"testgrp","groupLabel":"테스트그룹","groupLabelEn":"","...}` → 응답 201, `groupLabelEn:""`로 저장됨.
  - 원인: `source/backend/src/main/java/com/itsm/auth/application/ScreenAdminService.java:61` 검증이 `request.groupLabelEn() == null`만 체크하고 공백 문자열(`""`)은 걸러내지 않음. FE(`source/frontend/src/features/admin/MenuManagementPage.tsx:178` `groupLabelEn: form.groupLabelEn.trim()`)도 값이 없을 때 `null`이 아닌 빈 문자열(`""`)을 그대로 전송하므로, 두 지점 모두 공백을 "누락"으로 취급하지 않아 필수값 검증이 우회됨.
  - 관련 요구사항: `docs/02_plan/api_spec/auth.md` API-AUTH-017 Response Code "400 | 필수 누락·형식 오류(groupCode 지정 시 groupLabelEn 누락 포함)", `docs/02_plan/screen/admin.md` SCR-ADMIN-006 "그룹을 지정하면 그룹 영문명도 필수".
  - 정리: 재현에 사용한 테스트 데이터(SCR-TEST-001, "테스트그룹")는 테스트 종료 후 UI에서 soft delete 처리함.

## 참고(비-실패, 정보성)

- TC-APR-003 관련: 역할 선택 사이드 패널의 닫기 버튼이 `source/frontend/src/components/ui/sheet.tsx:62`의 공용 `sr-only` 텍스트("닫기")를 사용해 i18n 미적용 상태다. 시각적으로 노출되는 문구는 아니라(sr-only) 화면상 결함은 아니지만, 스크린리더 사용자 기준으로는 영어 모드에서도 한국어로 안내된다. `ui/sheet.tsx`는 승인 프로세스 화면 전용이 아닌 공용 UI 프리미티브라 이번 유지보수 범위(approval-process-flow.tsx) 밖일 수 있어 FAIL로 집계하지 않았으나, 접근성 관점에서 개선 여지가 있어 기록해둔다.
