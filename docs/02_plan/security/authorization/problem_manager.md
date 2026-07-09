# 역할 정의 — PROBLEM_MANAGER

> 역할: PROBLEM_MANAGER · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: PROBLEM_MANAGER (문제 관리자)
- **설명**: 인시던트의 근본 원인을 조사·진단하고 알려진 오류(KEDB)·워크어라운드를 관리하며 재발을 예방한다.
- **주요 목표**: RCA로 근본 원인 제거, 변경 연계로 항구 조치, KEDB로 지식 축적.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-PRB-001 | 문제 목록 | |
| SCR-PRB-002 | 문제 등록 | |
| SCR-PRB-003 | 문제 상세 | RCA·워크어라운드·연계·후속조치·종료 |
| SCR-PRB-004 | KEDB 검색 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-PRB-001 | /api/v1/problems | GET | 목록 |
| API-PRB-002 | /api/v1/problems | POST | 등록 |
| API-PRB-003 | /api/v1/problems/{id} | GET | 상세 |
| API-PRB-004 | /api/v1/problems/{id}/status | PATCH | 6단계 전이 |
| API-PRB-005 | /api/v1/problems/{id}/rca | PUT | RCA 기록 |
| API-PRB-006 | /api/v1/problems/{id}/workaround | POST | 워크어라운드 |
| API-PRB-007 | /api/v1/problems/{id}/known-errors | POST | KE 생성 |
| API-PRB-008 | /api/v1/known-errors | GET | KEDB 검색 |
| API-PRB-009 | /api/v1/problems/{id}/links | POST | 인시던트/변경 연계 |
| API-PRB-010 | /api/v1/problems/{id}/actions | POST | 후속 조치 등록 |
| API-PRB-011 | /api/v1/problems/{id}/actions/{actionId} | PATCH | 조치 상태 |
| API-PRB-012 | /api/v1/problems/{id}/close | POST | 문제 종료 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 문제 접근 권한 없는 사용자의 RCA/조치 → 403.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
