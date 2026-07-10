# 역할 정의 — COMPLIANCE_OFFICER

> 역할: COMPLIANCE_OFFICER · 버전: 0.1 · 작성일: 2026-07-10

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: COMPLIANCE_OFFICER (컴플라이언스 담당자)
- **설명**: 규제 의무·내부 정책을 요구사항으로 관리하고 책임자를 지정하며, 위반 이슈를 시정조치로 추적해 준수 현황을 리포팅하는 담당자.
- **주요 목표**: 요구사항별 책임 소재를 명확히 하고 시정조치를 신속히 해결해 전체 준수율을 유지·개선한다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-COMP-001 | 컴플라이언스 요구사항 목록 | |
| SCR-COMP-002 | 요구사항 등록 | |
| SCR-COMP-003 | 요구사항 상세 | 책임자·시정조치·변경 연계·감사 로그 |
| SCR-COMP-004 | 준수 현황 대시보드 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-COMP-001 | /api/v1/compliance/requirements | GET | 목록 |
| API-COMP-002 | /api/v1/compliance/requirements | POST | 등록 |
| API-COMP-003 | /api/v1/compliance/requirements/{id} | GET | 상세 |
| API-COMP-004 | /api/v1/compliance/requirements/{id} | PATCH | 수정 |
| API-COMP-005 | /api/v1/compliance/requirements/{id}/links | POST | 변경 요청 연계 |
| API-COMP-006 | /api/v1/compliance/requirements/{id}/owner | POST | 책임자 지정 |
| API-COMP-007 | /api/v1/compliance/requirements/{id}/corrective-actions | POST | 시정조치 등록 |
| API-COMP-008 | /api/v1/compliance/corrective-actions/{actionId}/status | PATCH | 시정조치 상태 전이 |
| API-COMP-009 | /api/v1/compliance/audit-logs | GET | 컴플라이언스 감사 로그(컴플라이언스 이벤트만) |
| API-COMP-010 | /api/v1/compliance/metrics | GET | 준수 현황 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 감사 로그 조회(API-COMP-009)는 컴플라이언스 관련 이벤트로 범위가 제한되며, 전체 시스템 감사 로그(API-AUTH-015)는 SYSTEM_ADMIN 전용이라 이 역할에는 없다.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
