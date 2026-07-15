# 역할 정의 — PROCESS_OWNER

> 역할: PROCESS_OWNER · 버전: 0.3 · 작성일: 2026-07-15 · 요청 유형별 담당자 역할 지정 기능(유지보수 요청) 반영 — 카탈로그 항목 생성/수정(API-SRM-003/004)에 담당자 역할(assigneeRoleId) 필드 추가, 역할 선택용 API-AUTH-030 접근 권한 추가
>
> 이전 버전: 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 카탈로그 항목의 승인 설정 권한 제거(SYSTEM_ADMIN 전용 SCR-ADMIN-008로 이관)

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: PROCESS_OWNER (프로세스 오너)
- **설명**: 서비스 카탈로그(요청 유형·양식·SLA·승인·큐)를 정의·정리하고 서비스 요청 지표를 모니터링하는 프로세스 책임자.
- **주요 목표**: 요청 유형을 표준화하고 SLA·CSAT 등 지표를 개선한다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-SRM-007 | 서비스 카탈로그 관리 | 요청 유형·양식·SLA·승인 설정 |
| SCR-SRM-008 | 요청 지표 대시보드 | CSAT·응답/해결·SLA |
| SCR-ESM-006 | 부서별 카탈로그 관리 | 담당 부서·양식·체크리스트 템플릿 정의 |
| SCR-ESM-011 | ESM 지표 대시보드 | 부서별 처리량·처리시간·완료율 |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-SRM-001 | /api/v1/service-catalog/items | GET | 카탈로그 목록 |
| API-SRM-002 | /api/v1/service-catalog/items/{id} | GET | 항목 상세 |
| API-SRM-003 | /api/v1/service-catalog/items | POST | 항목 생성(이름·설명·양식·큐·담당자 역할(선택, 2026-07-15 유지보수 요청)·SLA. 승인 설정은 포함하지 않음 — SYSTEM_ADMIN이 SCR-ADMIN-008에서 도메인=SERVICE_REQUEST·요청유형=이 항목으로 별도 설정) |
| API-SRM-004 | /api/v1/service-catalog/items/{id} | PATCH | 항목 수정(승인 관련 필드 없음) |
| API-AUTH-030 | /api/v1/roles | GET | 역할 목록 조회(공개, 2026-07-15 유지보수 요청 — 담당자 역할 select 후보 조회용) |
| API-SRM-015 | /api/v1/service-requests/metrics | GET | 요청 지표 |
| API-ESM-001 | /api/v1/esm/catalog-items | GET | 부서 카탈로그 목록 |
| API-ESM-002 | /api/v1/esm/catalog-items/{id} | GET | 항목 상세(체크리스트 템플릿 포함) |
| API-ESM-003 | /api/v1/esm/catalog-items | POST | 항목 생성 |
| API-ESM-004 | /api/v1/esm/catalog-items/{id} | PATCH | 항목 수정 |
| API-ESM-017 | /api/v1/esm/metrics | GET | ESM 지표 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **카탈로그 항목 생성·수정(API-SRM-003/004, API-ESM-003/004)은 PROCESS_OWNER 전용**(그 외 → 403). 이름·양식·담당 부서 누락 시 400.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
