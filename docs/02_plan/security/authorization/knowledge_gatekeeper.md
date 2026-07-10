# 역할 정의 — KNOWLEDGE_GATEKEEPER

> 역할: KNOWLEDGE_GATEKEEPER · 버전: 0.1 · 작성일: 2026-07-09

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: KNOWLEDGE_GATEKEEPER (지식 게이트키퍼 / 승인자)
- **설명**: 검토 상태 기사를 검토해 게시 승인 또는 반려하며 지식베이스의 품질·최신성을 유지한다.
- **주요 목표**: 정확·최신 지식만 게시되도록 품질 관문 역할 수행. 지식 지표 모니터링.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-KM-001 | 지식베이스 검색/목록 | 전 상태 조회 |
| SCR-KM-002 | 기사 열람 | |
| SCR-KM-004 | 검토·게시 승인함 | 승인/반려 |
| SCR-KM-005 | 지식 지표 대시보드 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-KM-001 | /api/v1/knowledge/articles | GET | 전 상태 검색 |
| API-KM-002 | /api/v1/knowledge/articles/{id} | GET | 열람 |
| API-KM-007 | /api/v1/knowledge/articles/{id}/review | POST | **게시 승인/반려(게이트키퍼 전용)** |
| API-KM-008 | /api/v1/knowledge/reviews?scope=mine | GET | 검토 대기 목록 |
| API-KM-012 | /api/v1/knowledge/metrics | GET | 지식 지표 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **게시 승인/반려(API-KM-007)는 KNOWLEDGE_GATEKEEPER 전용**(그 외 → 403). 반려 시 사유 필수(누락 400), 기사 초안 복귀.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
