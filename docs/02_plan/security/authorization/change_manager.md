# 역할 정의 — CHANGE_MANAGER

> 역할: CHANGE_MANAGER · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: CHANGE_MANAGER (변경 관리자)
- **설명**: 변경 요청(RFC)의 분류·프로세스·승인 경로·일정·구현 결과를 총괄한다. CAB 운영을 조율한다.
- **주요 목표**: 위험을 통제하며 변경 성공률을 높이고, 긴급 변경 비율을 관리한다. (승인 결정 자체는 APPROVER/CAB 권한)

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-CHG-001 | 변경 목록 | |
| SCR-CHG-002 | 변경 요청(RFC) 생성 | |
| SCR-CHG-003 | 변경 상세 | 프로세스·구현결과·연계 |
| SCR-CHG-005 | 변경 일정(캘린더) | |
| SCR-CHG-006 | 변경 지표 대시보드 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-CHG-001 | /api/v1/changes | GET | 목록 |
| API-CHG-002 | /api/v1/changes | POST | RFC 생성 |
| API-CHG-003 | /api/v1/changes/{id} | GET | 상세 |
| API-CHG-004 | /api/v1/changes/{id}/status | PATCH | 6단계 전이 |
| API-CHG-005 | /api/v1/changes/{id}/classification | PATCH | 유형·위험·승인경로 |
| API-CHG-008 | /api/v1/changes/{id}/result | POST | 구현 결과 기록 |
| API-CHG-009 | /api/v1/changes/{id}/links | POST | 인시던트/문제 연계 |
| API-CHG-010 | /api/v1/changes/schedule | GET | 변경 일정 |
| API-CHG-011 | /api/v1/change-templates | GET | 표준 변경 템플릿 |
| API-CHG-012 | /api/v1/changes/metrics | GET | 지표 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **승인 결정(API-CHG-006)은 APPROVER/CAB 전용**이며 CHANGE_MANAGER 단독으로는 승인 불가(승인 권한은 별도 부여). 승인 완료 전 구현 전이 시도는 409.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
