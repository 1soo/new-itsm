# 통합 테스트 결과 — Common (SYSTEM_ADMIN 전체 접근 회귀) (20260711-011815)

## 요약
- 총 7건 · 성공 7 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ADMIN-001 | PASS | admin@itsm.local(SYSTEM_ADMIN) 로그인(새 컨텍스트) 시 사이드바에 전 도메인 메뉴 그룹 모두 노출(서비스요청/인시던트/문제/변경/지식/자산/부서 서비스/**HR 케이스**/취약점/컴플라이언스/인프라 모니터링/관리자) | - |
| TC-ADMIN-002 | PASS | `GET /api/v1/esm/hr-cases` 200(403 없음, 케이스 2건 조회). 화면 `/esm/hr-cases` 직접 진입 시 정상 렌더링(케이스 목록 표시) | - |
| TC-ADMIN-003 | PASS | `GET /api/v1/approvals?scope=mine` 200(빈 배열, admin 개인 배정 승인 건 없음). 사이드바 "CAB 승인 대기함" 화면 진입 시 403 없이 정상 렌더링("승인 대기 건이 없습니다") | - |
| TC-ADMIN-004 | PASS | CHG-2026-0014(id=18, REQUESTED)에 대해 `PATCH /api/v1/changes/18/status`(targetStatus=REVIEW) 200, 상태 REVIEW로 전이 성공(CHANGE_MANAGER 전용 라우트, admin 예외 허용 확인) | - |
| TC-ADMIN-005 | PASS | `GET /api/v1/vulnerabilities` 200, `GET /api/v1/infra/metrics` 200(둘 다 403 없음). 화면 `/vulnerabilities` 직접 진입 시 정상 렌더링 | - |
| TC-ADMIN-006 | PASS | admin(담당 부서 없음)으로 `GET /api/v1/esm/requests?scope=all` 200, 부서 필터 강제 없이 HR/LEGAL 등 전 부서 요청 목록 정상 조회 | - |
| TC-ADMIN-007 | PASS | cm@itsm.local(CHANGE_MANAGER)로 `GET /api/v1/esm/hr-cases` 호출 시 403 유지. hr@itsm.local(HR_CASE_MANAGER)로 `PATCH /api/v1/changes/17/status` 호출 시 403 `ACCESS_DENIED` 유지. 대조군으로 cm@itsm.local이 본인 권한 범위 내 변경 상태 전이(CHG-2026-0013)는 정상 200 성공, 다른 역할의 권한이 의도치 않게 확장되지 않았음 확인 | - |

## 실패 항목 분석
- 없음
