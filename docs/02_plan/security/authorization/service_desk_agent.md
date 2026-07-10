# 역할 정의 — SERVICE_DESK_AGENT

> 역할: SERVICE_DESK_AGENT · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: SERVICE_DESK_AGENT (서비스 데스크 상담원)
- **설명**: 큐에서 서비스 요청을 배정받아 이행하고 요청자와 소통하는 1차 대응 담당자. 인시던트 접수도 수행한다.
- **주요 목표**: 요청을 신속·정확히 이행하고 SLA를 준수하며, 필요 시 인시던트를 등록/에스컬레이션.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-SRM-003 | 내 요청 목록 | 담당/전체 조회 |
| SCR-SRM-004 | 요청 큐(상담원) | 배정·처리 |
| SCR-SRM-005 | 요청 상세 | 이행·상태 갱신·코멘트 |
| SCR-INC-001 | 인시던트 목록 | 접수·조회 |
| SCR-INC-002 | 인시던트 등록 | 탐지 시 등록 |
| SCR-INC-003 | 인시던트 상세 | 상태 업데이트·에스컬레이션 |
| SCR-KM-001 | 지식베이스 검색/목록 | 상담 참조 |
| SCR-KM-002 | 기사 열람 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-SRM-007 | /api/v1/service-requests?scope=all&queue= | GET | 큐/전체 목록 |
| API-SRM-008 | /api/v1/service-requests/{id} | GET | 상세 |
| API-SRM-009 | /api/v1/service-requests/{id}/assign | POST | 배정 |
| API-SRM-010 | /api/v1/service-requests/{id}/status | PATCH | 이행·상태 전이 |
| API-SRM-013 | /api/v1/service-requests/{id}/comments | POST | 코멘트 |
| API-INC-001 | /api/v1/incidents | GET | 목록 |
| API-INC-002 | /api/v1/incidents | POST | 등록 |
| API-INC-003 | /api/v1/incidents/{id} | GET | 상세 |
| API-INC-005 | /api/v1/incidents/{id}/status | PATCH | 상태 전이 |
| API-INC-007 | /api/v1/incidents/{id}/escalate | POST | 에스컬레이션 |
| API-INC-008 | /api/v1/incidents/{id}/updates | POST | 상태 업데이트 |
| API-KM-001 | /api/v1/knowledge/articles | GET | 검색 |
| API-KM-002 | /api/v1/knowledge/articles/{id} | GET | 열람 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 요청 배정·이행은 SERVICE_DESK_AGENT만 허용(요청자 등 미보유 시 403).
- 대응 역할 배정(API-INC-006)은 INCIDENT_MANAGER 전용이라 상담원은 403.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
