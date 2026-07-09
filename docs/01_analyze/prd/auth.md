# 요구사항 정의서 — 인증/계정/권한 (Auth & RBAC)

> 도메인: auth · 버전: 0.1 · 작성일: 2026-07-09

## 1. 개요

ITSM 플랫폼의 모든 도메인은 역할(상담원·관리자·승인자·최종 사용자 등)과 접근제어를 전제로 동작한다. 본 도메인은 사용자 계정, 로그인 인증(JWT), 역할 기반 접근제어(RBAC), 감사 로깅을 제공하여 나머지 6개 도메인의 공통 기반을 이룬다.

## 2. 범위

### 포함 (In Scope)

- 사용자 계정 등록·조회·수정·비활성화
- 로그인/로그아웃 및 JWT(Access/Refresh Token) 기반 인증
- Access Token 재발급(Refresh)
- 역할(Role) 정의·부여 및 역할 기반 접근제어(RBAC, 미인가 시 403)
- 비밀번호 저장(단방향 암호화)·변경·재설정
- 인증·인가·계정 변경에 대한 감사 로그 기록

### 미포함 (Out of Scope)

- 외부 IdP 연동(SSO/OAuth/SAML), 소셜 로그인
- 다중 인증(MFA)
- 조직도/부서 계층 관리(각 도메인에서 필요한 최소 사용자 속성만 사용)

## 3. 요구사항 목록

| ID | 유형 | 요구사항 | 우선순위 | 출처 |
|----|------|----------|----------|------|
| REQ-AUTH-001 | 기능 | 관리자는 사용자 계정을 생성·조회·수정·비활성화할 수 있다 | High | 전 도메인 역할/접근관리 서술(problem·incident·change·service-request·knowledge·asset) |
| REQ-AUTH-002 | 기능 | 사용자는 이메일·비밀번호로 로그인하고 Access/Refresh Token을 발급받는다 | High | it-operations(접근 관리), it-management(데이터 컴플라이언스), security 요구 |
| REQ-AUTH-003 | 기능 | 사용자는 Refresh Token으로 만료된 Access Token을 재발급받을 수 있다 | High | 세션·토큰 관리 요구 |
| REQ-AUTH-004 | 기능 | 사용자는 로그아웃하여 세션(토큰)을 무효화할 수 있다 | High | 접근 관리 |
| REQ-AUTH-005 | 기능 | 시스템은 역할 기반 접근제어(RBAC)로 화면·API 접근을 통제한다 | High | 전 도메인 역할표, it-operations-management(접근 관리), asset(접근 제어) |
| REQ-AUTH-006 | 기능 | 관리자는 역할을 정의하고 사용자에게 하나 이상의 역할을 부여할 수 있다 | High | problem/incident/change/service-request/knowledge/asset 역할과 책임 |
| REQ-AUTH-007 | 기능 | 사용자는 비밀번호를 변경할 수 있고, 시스템은 비밀번호를 단방향 암호화하여 저장한다 | High | it-management(데이터 컴플라이언스·보안) |
| REQ-AUTH-008 | 비기능 | 시스템은 인증·인가·계정 변경 이벤트를 감사 로그로 기록한다 | High | it-management(감사 추적), asset(감사 추적), change(auditable record) |

## 4. 인수 기준 (Acceptance Criteria · EARS)

### REQ-AUTH-001

- (Event-driven) **WHEN** 관리자가 유효한 계정 정보(이메일·이름·역할)로 계정 생성을 요청하면, 시스템은 계정을 생성하고 결과를 반환해야 한다.
- (Unwanted) **IF** 이미 존재하는 이메일로 계정 생성을 요청하면, **THEN** 시스템은 생성을 거부하고 중복 오류를 반환해야 한다.
- (Event-driven) **WHEN** 관리자가 계정을 비활성화하면, 시스템은 해당 계정의 로그인을 차단해야 한다.

### REQ-AUTH-002

- (Event-driven) **WHEN** 사용자가 올바른 이메일·비밀번호로 로그인하면, 시스템은 Access Token과 Refresh Token을 발급해야 한다.
- (Unwanted) **IF** 비밀번호가 일치하지 않으면, **THEN** 시스템은 인증을 거부하고 인증 실패를 반환해야 한다.
- (Unwanted) **IF** 비활성화된 계정으로 로그인하면, **THEN** 시스템은 로그인을 거부해야 한다.

### REQ-AUTH-003

- (Event-driven) **WHEN** 유효한 Refresh Token으로 재발급을 요청하면, 시스템은 새 Access Token을 발급해야 한다.
- (Unwanted) **IF** Refresh Token이 만료되었거나 무효하면, **THEN** 시스템은 재발급을 거부하고 재로그인을 요구해야 한다.

### REQ-AUTH-004

- (Event-driven) **WHEN** 사용자가 로그아웃하면, 시스템은 해당 세션의 Refresh Token을 무효화해야 한다.
- (Unwanted) **IF** 무효화된 Refresh Token으로 재발급을 요청하면, **THEN** 시스템은 이를 거부해야 한다.

### REQ-AUTH-005

- (Ubiquitous) 시스템은 모든 보호된 화면·API 요청에 대해 유효한 Access Token과 역할 권한을 검증해야 한다.
- (Unwanted) **IF** 인증되지 않은 요청이 보호된 리소스에 접근하면, **THEN** 시스템은 401을 반환해야 한다.
- (Unwanted) **IF** 인증된 사용자가 권한이 없는 리소스에 접근하면, **THEN** 시스템은 403을 반환해야 한다.

### REQ-AUTH-006

- (Event-driven) **WHEN** 관리자가 사용자에게 역할을 부여·회수하면, 시스템은 변경된 권한을 즉시 반영해야 한다.
- (Ubiquitous) 시스템은 하나의 사용자에게 복수 역할을 부여할 수 있어야 한다.

### REQ-AUTH-007

- (Ubiquitous) 시스템은 비밀번호를 단방향 암호화(해시)하여 저장하고, 평문으로 저장하지 않아야 한다.
- (Event-driven) **WHEN** 사용자가 현재 비밀번호와 새 비밀번호로 변경을 요청하면, 시스템은 현재 비밀번호를 검증한 뒤 변경해야 한다.
- (Unwanted) **IF** 현재 비밀번호가 일치하지 않으면, **THEN** 시스템은 변경을 거부해야 한다.

### REQ-AUTH-008

- (Event-driven) **WHEN** 로그인·로그아웃·토큰 재발급·계정/역할 변경이 발생하면, 시스템은 발생 시각·주체·대상을 감사 로그로 기록해야 한다.
