# 통합 테스트 시나리오 — Common (SYSTEM_ADMIN 전체 접근 회귀)

> dev_lead 요청: 신규 요구사항(SYSTEM_ADMIN 전체 접근 허용) FE/BE 반영에 대한 회귀 테스트. 전체 도메인 재검증이 아니라 기존에 역할이 좁게 제한돼 있던 대표 화면 위주로 확인.

## 사전 조건
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): admin@itsm.local(SYSTEM_ADMIN), cm@itsm.local(CHANGE_MANAGER), hr@itsm.local(HR_CASE_MANAGER)
- 근거 문서: `docs/02_plan/security/authorization/system_admin.md` 2절("다른 모든 역할에 정의된 화면·API 전체에 예외적으로 접근 가능, 403 없음")

## 시나리오

### TC-ADMIN-001 · SYSTEM_ADMIN 사이드바 — 전 도메인 메뉴 노출(HR 케이스 포함)
- 근거: @docs/02_plan/security/authorization/system_admin.md (2절)
- 전제: admin@itsm.local 로그인(새 브라우저 컨텍스트)
- 절차: 로그인 후 사이드바 전체 메뉴 그룹 확인(ESM 부서 요청·HR 케이스, 인시던트/문제/변경/지식/자산/취약점/컴플라이언스/인프라모니터링 등)
- 기대 결과: 관리자 전용 메뉴(계정/역할/감사로그)뿐 아니라 기존에 다른 역할 전용이던 도메인 메뉴(HR 케이스 등)도 모두 노출

### TC-ADMIN-002 · SYSTEM_ADMIN — ESM HR 케이스 목록/상세 접근(기존 최우선 회귀 포인트)
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-011/012), `EsmHrCaseService.java` HR_CASE_MANAGER 전용 체크
- 전제: admin@itsm.local 로그인
- 절차:
  1. `GET /api/v1/esm/hr-cases` 직접 호출
  2. 화면 `/esm/hr-cases` 직접 진입
- 기대 결과: 403 없이 200/정상 렌더링

### TC-ADMIN-003 · SYSTEM_ADMIN — 서비스요청/변경 승인 대기함(APPROVER 전용) 접근
- 근거: @docs/02_plan/api_spec/change.md (API-CHG-007), @docs/02_plan/api_spec/service-request.md (API-SRM-012), `ApprovalController.java` `@PreAuthorize("hasAnyRole('APPROVER','SYSTEM_ADMIN')")`
- 전제: admin@itsm.local 로그인
- 절차: `GET /api/v1/approvals?scope=mine` 호출
- 기대 결과: 403 없이 200(빈 배열 허용, admin 개인 소유 승인 건은 없을 수 있음)

### TC-ADMIN-004 · SYSTEM_ADMIN — 변경 상태 전이(CHANGE_MANAGER 전용) 접근
- 근거: @docs/02_plan/api_spec/change.md (API-CHG-004), `ChangeService.requireRole(CM)` → `SecurityUtils.hasAnyRole`
- 전제: admin@itsm.local 로그인, 기존 REQUESTED 상태 변경 요청 1건 확보
- 절차: `PATCH /api/v1/changes/{id}/status`로 REQUESTED→REVIEW 전이 시도
- 기대 결과: 403 없이 200 전이 성공

### TC-ADMIN-005 · SYSTEM_ADMIN — 단일 역할 전용 확장 도메인(취약점/컴플라이언스/인프라모니터링) 접근
- 근거: @docs/02_plan/security/authorization/vulnerability_manager.md, compliance_officer.md, infra_operator.md
- 전제: admin@itsm.local 로그인
- 절차:
  1. `GET /api/v1/vulnerabilities` 호출
  2. `GET /api/v1/infra/metrics` 호출
- 기대 결과: 둘 다 403 없이 200

### TC-ADMIN-006 · SYSTEM_ADMIN — ESM 부서 요청 처리 큐(department 스코프) 부서 무관 전체 조회
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-006 `scope=all`), `EsmRequestService.list()` `SecurityUtils.isSystemAdmin()` 분기
- 전제: admin@itsm.local 로그인(담당 부서 없음)
- 절차: `GET /api/v1/esm/requests?scope=all` 호출
- 기대 결과: 403·빈 결과 강제 없이 전체 부서 요청 목록 정상 조회(부서 필터가 강제되지 않음)

### TC-ADMIN-007 · 회귀 — 기존 역할 계정은 여전히 타 역할 전용 화면/API 403 유지
- 근거: @docs/02_plan/security/authorization/change_manager.md, hr_case_manager.md
- 전제: cm@itsm.local(CHANGE_MANAGER), hr@itsm.local(HR_CASE_MANAGER) 각각 로그인(새 컨텍스트)
- 절차:
  1. cm@itsm.local로 `GET /api/v1/esm/hr-cases` 호출(HR_CASE_MANAGER 전용)
  2. hr@itsm.local로 `PATCH /api/v1/changes/{id}/status` 호출(CHANGE_MANAGER 전용)
- 기대 결과: (1)(2) 모두 여전히 403(다른 역할의 권한이 의도치 않게 넓어지지 않았음)
