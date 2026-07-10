# 역할 정의 — ASSET_MANAGER

> 역할: ASSET_MANAGER · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: ASSET_MANAGER (자산 관리자)
- **설명**: HW/SW/클라우드 자산의 전 생애주기와 CI·CMDB 관계를 관리하고 만료를 추적한다.
- **주요 목표**: 정확한 자산 인벤토리·CMDB 유지, 만료 임박 관리, 변경 영향 범위 분석 지원.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-ITAM-001 | 자산 목록 | |
| SCR-ITAM-002 | 자산 등록/수정 | |
| SCR-ITAM-003 | 자산 상세 | 생애주기·만료·폐기·티켓 연계 |
| SCR-ITAM-004 | CI·CMDB 관계 뷰 | 의존 관계·영향 범위 |
| SCR-ITAM-005 | 자산 지표 대시보드 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-ITAM-001 | /api/v1/assets | GET | 목록 |
| API-ITAM-002 | /api/v1/assets | POST | 등록 |
| API-ITAM-003 | /api/v1/assets/{id} | GET | 상세 |
| API-ITAM-004 | /api/v1/assets/{id} | PATCH | 수정 |
| API-ITAM-005 | /api/v1/assets/{id}/lifecycle | PATCH | 생애주기 전이 |
| API-ITAM-006 | /api/v1/assets/{id}/retire | PATCH | 폐기 |
| API-ITAM-007 | /api/v1/assets/{id}/links | POST | 티켓 연계 |
| API-ITAM-008 | /api/v1/cis | GET | CI 목록 |
| API-ITAM-009 | /api/v1/cis | POST | CI 등록 |
| API-ITAM-010 | /api/v1/cis/{id}/relations | POST | CI 관계 |
| API-ITAM-011 | /api/v1/cis/{id}/impact | GET | 영향 범위 |
| API-ITAM-012 | /api/v1/assets/metrics | GET | 지표 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 자산 등록·수정·폐기·생애주기 전이는 ASSET_MANAGER 전용(그 외 → 403).
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
