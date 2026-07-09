# 역할 정의 — SYSTEM_ADMIN

> 역할: SYSTEM_ADMIN · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

모든 인증 역할은 다음에 기본 접근한다: 앱 셸/헤더/사이드바/푸터·토스트(SCR-COM-001~004/009), 로그인·내 프로필·비밀번호 변경(SCR-AUTH-001~003), 404(SCR-ERR-404), 자기 인증 API(API-AUTH-003 로그아웃·004 내 정보·005 비밀번호 변경). 이하 표에서는 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: SYSTEM_ADMIN (시스템 관리자)
- **설명**: 플랫폼의 사용자 계정·역할·접근제어·감사 로그를 관리하는 최고 관리자.
- **주요 목표**: 계정 생성·비활성화, 역할 정의·부여/회수, 감사 추적으로 플랫폼 거버넌스 유지.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-ADMIN-001 | 계정 목록 | |
| SCR-ADMIN-002 | 계정 생성 | |
| SCR-ADMIN-003 | 계정 상세·수정 | 비활성화·역할 부여/회수 |
| SCR-ADMIN-004 | 역할 관리 | |
| SCR-ADMIN-005 | 감사 로그 조회 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-AUTH-006 | /api/v1/admin/users | GET | 계정 목록 |
| API-AUTH-007 | /api/v1/admin/users | POST | 계정 생성 |
| API-AUTH-008 | /api/v1/admin/users/{userId} | GET | 계정 상세 |
| API-AUTH-009 | /api/v1/admin/users/{userId} | PATCH | 계정 수정 |
| API-AUTH-010 | /api/v1/admin/users/{userId}/status | PATCH | 활성/비활성 |
| API-AUTH-011 | /api/v1/admin/users/{userId}/roles | POST | 역할 부여 |
| API-AUTH-012 | /api/v1/admin/users/{userId}/roles/{roleId} | DELETE | 역할 회수 |
| API-AUTH-013 | /api/v1/admin/roles | GET | 역할 목록 |
| API-AUTH-014 | /api/v1/admin/roles | POST | 역할 생성 |
| API-AUTH-015 | /api/v1/admin/audit-logs | GET | 감사 로그 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할(SYSTEM_ADMIN)에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다(admin 라우트는 SYSTEM_ADMIN만 허용).
- 관리자 API는 SYSTEM_ADMIN 미보유 시 403.
