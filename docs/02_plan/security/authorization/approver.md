# 역할 정의 — APPROVER

> 역할: APPROVER · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: APPROVER (승인자 · CAB 멤버 포함)
- **설명**: 서비스 요청 및 변경(RFC)에 대한 승인/반려 권한을 가진 의사결정자. 변경의 경우 CAB 멤버로서 승인한다.
- **주요 목표**: 지정된 승인 건을 검토해 승인/반려하고 사유를 남긴다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-SRM-006 | 승인 대기함 | 서비스 요청 승인 |
| SCR-CHG-004 | CAB 승인 대기함 | 변경 승인 |
| SCR-SRM-005 | 요청 상세 | 승인 대상 조회(읽기) |
| SCR-CHG-003 | 변경 상세 | 승인 대상 조회(읽기) |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-SRM-012 | /api/v1/approvals?scope=mine&type=service-request | GET | 요청 승인 대기 |
| API-SRM-011 | /api/v1/service-requests/{id}/approval | POST | 요청 승인/반려 |
| API-SRM-008 | /api/v1/service-requests/{id} | GET | 요청 상세 조회 |
| API-CHG-007 | /api/v1/approvals?scope=mine&type=change | GET | 변경 승인 대기 |
| API-CHG-006 | /api/v1/changes/{id}/approval | POST | 변경 승인/반려 |
| API-CHG-003 | /api/v1/changes/{id} | GET | 변경 상세 조회 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **역할 기반 승인**: 승인/반려는 `approval.approver_role`(SRM=요청 유형별 지정 역할·기본 APPROVER, CHANGE=승인 경로 CAB→APPROVER)을 **보유한 사용자**면 처리할 수 있고, 공유 대기함에서 먼저 처리한 사용자가 결정한다. role claim에 해당 approver_role 미포함 시 → 403.
- 반려 시 사유 필수(누락 시 400). 이미 결정된 승인 재처리 시 409.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
