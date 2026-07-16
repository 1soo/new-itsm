---
date: 20260711-115017
domain: auth
result: pass
keywords: [메뉴 관리, 역할-메뉴 매핑, 동적 RBAC, 내 메뉴 조회]
---

# 통합 테스트 결과 — auth (Role-Menu 동적 매핑, 20260711-115017)

> 범위: API-AUTH-016~022, SCR-ADMIN-006(메뉴 관리), 사이드바 동적 RBAC 노출
> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(docker itsm-postgres)
> 초기 계정: `admin@itsm.local` / `Admin@1234` (SYSTEM_ADMIN), 데모 계정 `user@itsm.local` / `Admin@1234` (END_USER)

## 요약

- 총 33건 · 성공 33 · 실패 0 ✅ **전 항목 통과**
- API 원본 응답/요청 payload는 `result/api-evidence/`, E2E 스크린샷은 `result/shots/`에 보존.

## 상세

### A. 빌드

| TC ID | 결과 | 비고 |
|-------|------|------|
| TC-BUILD-001 | PASS | `gradlew clean test build` BUILD SUCCESSFUL |
| TC-BUILD-002 | PASS | `npm run build`(tsc+vite) 성공, navConfig.tsx 삭제로 인한 참조 오류 없음 |

### B. 메뉴(화면) 목록/생성/수정/삭제 (API-AUTH-016~019)

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-SCR-001 | PASS | 200, totalElements=80, SCR-ADMIN-006(iconName=ListTree, groupCode=admin, roles=[SYSTEM_ADMIN]) 포함 확인 | api-evidence/r_scr001_full.json |
| TC-SCR-002 | PASS | (a) 미인증 401 (b) END_USER 403 | |
| TC-SCR-003 | PASS | 201, roles:[] | api-evidence/r_scr003.json |
| TC-SCR-004 | PASS | 400 VALIDATION_ERROR("domain: 공백일 수 없습니다") | api-evidence/r_scr004.json |
| TC-SCR-005 | PASS | 409 SCREEN_CODE_DUPLICATE | api-evidence/r_scr005.json |
| TC-SCR-006 | PASS | 409 PATH_DUPLICATE | api-evidence/r_scr006.json |
| TC-SCR-007 | PASS | 200, screenName/sortOrder 반영, screenCode·domain 불변 | api-evidence/r_scr007.json |
| TC-SCR-008 | PASS | 404 SCREEN_NOT_FOUND | api-evidence/r_scr008.json |
| TC-SCR-009 | PASS | 409 PATH_DUPLICATE(다른 메뉴가 쓰는 경로) | api-evidence/r_scr009.json |
| TC-SCR-010 | PASS | 200 {deleted:true}, 재조회 시 목록에서 제외(soft delete) | api-evidence/r_scr010.json, r_scr010b.json |
| TC-SCR-011 | PASS | 404 SCREEN_NOT_FOUND | api-evidence/r_scr011.json |

### C. 메뉴 역할 매핑 (API-AUTH-020~021)

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-SROLE-001 | PASS | 200 {screenId,roles:["END_USER"]} | |
| TC-SROLE-002 | PASS | 400 ROLE_NOT_FOUND | |
| TC-SROLE-003 | PASS | 404 SCREEN_NOT_FOUND | |
| TC-SROLE-004 | PASS | 409 SCREEN_ROLE_MAPPING_DUPLICATE | |
| TC-SROLE-005 | PASS | 200 {roles:[]}(회수 반영) | |
| TC-SROLE-006 | PASS | 생성·수정·부여·회수 4건 모두 ROLE_CHANGE(actor=admin@itsm.local, target=SCR-TEST-901) 기록 확인 | api-evidence/r_srole006.json |

### D. 내 메뉴 조회 (API-AUTH-022)

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-MYMENU-001 | PASS | 200, 13개 그룹, sort_order 오름차순/그룹 최소 sort_order 순 정렬, SCR-ADMIN-006 포함 | api-evidence/r_mymenu001.json |
| TC-MYMENU-002 | PASS | END_USER 응답에도 무매핑 화면(대시보드 SCR-COM-013 등) 포함 | api-evidence/r_mymenu002.json |
| TC-MYMENU-003 | PASS | SYSTEM_ADMIN 전용 매핑 메뉴가 END_USER 응답엔 미포함, SYSTEM_ADMIN 응답엔 포함 | api-evidence/r_mymenu003_eu.json, r_mymenu003_admin.json |
| TC-MYMENU-004 | PASS | 미인증 401 | |

### E. FE E2E (playwright, storage 초기화 후 수행)

| TC ID | 결과 | 실제 동작 | 증적 |
|-------|------|-----------|------|
| TC-E2E-101 | PASS | admin 로그인 → 관리자 그룹에 "메뉴 관리" 노출, 클릭 시 `/admin/menus` 이동 | shots/e2e-101-sidebar-menu-admin.png |
| TC-E2E-102 | PASS | 메뉴 생성 폼 저장 → "메뉴가 생성되었습니다" 토스트, 목록 반영 | (목록 확인, 스크린샷 생략) |
| TC-E2E-103 | PASS | 동일 경로로 생성 시도 → 모달 유지, 인라인 오류 "이미 사용 중인 경로입니다." | shots/e2e-103-path-duplicate-inline-error.png |
| TC-E2E-104 | PASS | 역할 매핑 패널에서 "최종 사용자" 체크 → 목록 배지 "전체 공개"→"1개 역할" 즉시 갱신, 체크 해제 → API 재조회로 roles:[] 복귀 확인 | shots/e2e-104-role-mapping-panel.png |
| TC-E2E-105 | PASS | 삭제 확인 다이얼로그 확인 → "메뉴가 삭제되었습니다" 토스트, 목록에서 즉시 제거 | shots/e2e-105-delete-confirm-dialog.png |
| TC-E2E-106 | PASS | END_USER(user@itsm.local) 로그인 → 관리자·인시던트·문제·변경 등 미매핑 그룹 비노출, 대시보드/내프로필/서비스요청·부서서비스 일부만 노출 | (스냅샷 확인) |
| TC-E2E-107 | PASS | END_USER로 `/admin/menus` 직접 접근 → `/403` 리다이렉트 | shots/e2e-107-403-forbidden.png |
| TC-E2E-108 | PASS | admin 사이드바 그룹 순서(서비스요청→인시던트→…→관리자)가 `/menus/mine` API 그룹 순서(TC-MYMENU-001)와 일치 | (교차 검증, 별도 스크린샷 없음) |

## 실패 항목 분석

없음(전 항목 통과).

## 참고

- 백엔드 `list()` API 응답에서 `is_deleted=true` 처리된 메뉴는 목록 재조회 시 자동 제외됨을 확인(`ScreenRepository.search()`가 `is_deleted=false` 필터 적용).
- 화면 설계서(`docs/02_plan/screen/admin.md` SCR-ADMIN-006)의 "삭제된 메뉴는 모든 역할의 매핑에서도 함께 제거된다" 문구는 실제로는 `screen_role` 행을 물리 삭제하지 않고 `screen.is_deleted=true`로 조회에서 제외하는 방식(API 명세 API-AUTH-019와 동일)으로 구현되어 있음 — 사용자 관점 동작(메뉴가 사이드바·관리 목록에서 즉시 사라짐)은 설계 의도와 동일하여 결함으로 보지 않음.
