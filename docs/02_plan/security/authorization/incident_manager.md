# 역할 정의 — INCIDENT_MANAGER

> 역할: INCIDENT_MANAGER · 버전: 0.1 · 작성일: 2026-07-09

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: INCIDENT_MANAGER (인시던트 관리자)
- **설명**: 인시던트 대응을 총괄하며 대응 역할(Tech Lead·Comms·Scribe)을 배정하고, 심각도·상태·해결·포스트모템을 관리한다.
- **주요 목표**: 신속한 복구(MTTR 단축)와 blameless 포스트모템을 통한 재발 방지, 문제 관리 연계.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-INC-001 | 인시던트 목록 | |
| SCR-INC-002 | 인시던트 등록 | |
| SCR-INC-003 | 인시던트 상세 | 역할 배정·상태·해결·연계 |
| SCR-INC-004 | 포스트모템 편집 | |
| SCR-INC-005 | 인시던트 지표 대시보드 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-INC-001 | /api/v1/incidents | GET | 목록 |
| API-INC-002 | /api/v1/incidents | POST | 등록 |
| API-INC-003 | /api/v1/incidents/{id} | GET | 상세 |
| API-INC-004 | /api/v1/incidents/{id}/severity | PATCH | 심각도·우선순위 |
| API-INC-005 | /api/v1/incidents/{id}/status | PATCH | 상태 전이 |
| API-INC-006 | /api/v1/incidents/{id}/roles | POST | **대응 역할 배정(IM 전용)** |
| API-INC-007 | /api/v1/incidents/{id}/escalate | POST | 에스컬레이션 |
| API-INC-008 | /api/v1/incidents/{id}/updates | POST | 상태 업데이트 |
| API-INC-009 | /api/v1/incidents/{id}/resolve | POST | 해결·시간 지표 |
| API-INC-010 | /api/v1/incidents/{id}/postmortem | GET | 포스트모템 조회 |
| API-INC-011 | /api/v1/incidents/{id}/postmortem | PUT | 포스트모템 작성 |
| API-INC-012 | /api/v1/incidents/{id}/links | POST | 문제 연계 |
| API-INC-013 | /api/v1/incidents/metrics | GET | 지표 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **대응 역할 배정(API-INC-006)은 INCIDENT_MANAGER 전용**(그 외 역할 → 403).
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
