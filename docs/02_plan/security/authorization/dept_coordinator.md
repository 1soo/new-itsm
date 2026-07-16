# 역할 정의 — DEPT_COORDINATOR

> 역할: DEPT_COORDINATOR · 버전: 0.1 · 작성일: 2026-07-10

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: DEPT_COORDINATOR (부서 처리 담당자)
- **설명**: HR/법무/시설/재무 등 소속 부서(`app_user.department`)의 부서 요청을 처리하고, 온보딩/오프보딩 체크리스트 중 자기 부서에 배정된 하위 작업을 완료 처리하는 담당자.
- **주요 목표**: 소속 부서로 들어온 요청을 신속히 처리하고, 체크리스트 하위 작업을 기한 내 완료해 온보딩/오프보딩을 지연 없이 진행한다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-ESM-004 | 부서 요청 처리 큐 | 소속 부서 요청만 |
| SCR-ESM-005 | 부서 요청 상세 | 소속 부서 요청 처리(상태 전이·코멘트) |
| SCR-ESM-009 | 온보딩/오프보딩 체크리스트 상세 | 조회 |
| SCR-ESM-010 | 내 하위 작업 목록 | 소속 부서 배정 하위 작업만 |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-ESM-006 | /api/v1/esm/requests?department= | GET | 소속 부서 요청 목록 |
| API-ESM-007 | /api/v1/esm/requests/{id} | GET | 소속 부서 요청 상세 |
| API-ESM-008 | /api/v1/esm/requests/{id}/status | PATCH | 소속 부서 요청 상태 전이 |
| API-ESM-009 | /api/v1/esm/requests/{id}/comments | POST | 코멘트 등록 |
| API-ESM-014 | /api/v1/esm/checklists/{id} | GET | 체크리스트 조회 |
| API-ESM-015 | /api/v1/esm/checklist-tasks?scope=mine | GET | 소속 부서 배정 하위 작업 목록 |
| API-ESM-016 | /api/v1/esm/checklist-tasks/{taskId}/status | PATCH | 하위 작업 완료 처리 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할(DEPT_COORDINATOR)에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **부서 요청 처리(API-ESM-006~009)와 하위 작업 완료(API-ESM-015/016)는 사용자의 `department`와 대상 레코드의 `department`가 일치할 때만 허용**하며, 불일치 시 403이다(REQ-ESM-002/007).
- HR 케이스(API-ESM-010~013)는 DEPT_COORDINATOR 접근 대상이 아니다(HR_CASE_MANAGER 전용, 별도 역할).
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
