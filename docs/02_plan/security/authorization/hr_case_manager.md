# 역할 정의 — HR_CASE_MANAGER

> 역할: HR_CASE_MANAGER · 버전: 0.1 · 작성일: 2026-07-10

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: HR_CASE_MANAGER (HR 케이스 담당자)
- **설명**: 민감한 인사 이슈를 접수(Intake)·기록(Documentation)·조사(Investigation)·해결(Resolution) 단계로 관리하는 HR 부서 담당자.
- **주요 목표**: HR 케이스를 절차대로 처리하고 이력을 추적하며, HR 외 역할의 접근을 차단해 민감 정보를 보호한다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-ESM-007 | HR 케이스 목록 | HR 전용 |
| SCR-ESM-008 | HR 케이스 상세 | 4단계 상태 전이 |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-ESM-010 | /api/v1/esm/hr-cases | POST | 케이스 접수 |
| API-ESM-011 | /api/v1/esm/hr-cases | GET | 케이스 목록 |
| API-ESM-012 | /api/v1/esm/hr-cases/{id} | GET | 케이스 상세 |
| API-ESM-013 | /api/v1/esm/hr-cases/{id}/status | PATCH | 상태 전이 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할(HR_CASE_MANAGER)에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **HR 케이스 API(API-ESM-010~013)는 HR_CASE_MANAGER 전용**이며, 그 외 역할이 접근하면 403이다. 단, SYSTEM_ADMIN은 전체 접근 원칙(system_admin.md 2절)에 따라 예외적으로 접근 가능하다(REQ-ESM-004 개정 반영, HR_CASE_MANAGER 및 SYSTEM_ADMIN 외 역할만 403).
- 상태 전이는 정의된 순서(접수→기록→조사→해결)만 허용하며, 순서를 벗어난 요청은 400.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
