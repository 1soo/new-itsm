# 역할 정의 — KNOWLEDGE_GATEKEEPER

> 역할: KNOWLEDGE_GATEKEEPER · 버전: 0.2 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 게시 승인 전담 역할에서 "승인 프로세스 규칙에 지정될 수 있는 범용 역할"로 일반화. SYSTEM_ADMIN이 KNOWLEDGE 도메인 규칙에 이 역할을 지정하지 않으면 게시 승인에 전혀 관여하지 않을 수 있음(그 경우 검토 요청은 즉시 게시됨)

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: KNOWLEDGE_GATEKEEPER (지식 게이트키퍼 / 승인자)
- **설명**: SYSTEM_ADMIN이 정의한 승인 프로세스 규칙(SCR-ADMIN-008, domain=KNOWLEDGE)의 승인자 차수에 지정될 수 있는 역할. 규칙에 지정되어 있으면 검토 요청된 기사의 게시 승인/반려에 관여하며, 지정되어 있지 않으면 게시 절차 자체에 관여하지 않는다(그 경우 검토 요청은 매칭 규칙이 없어 즉시 게시됨).
- **주요 목표**: 본인 역할이 지정된 검토 건에 한해 정확·최신 지식만 게시되도록 품질 관문 역할 수행. 지식 지표 모니터링.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-KM-001 | 지식베이스 검색/목록 | 전 상태 조회 |
| SCR-KM-002 | 기사 열람 | |
| SCR-COM-014 | 승인 대기함(전 도메인 공용) | 특정 역할 전용 화면이 아니라 인증된 모든 사용자가 접근 가능. KNOWLEDGE 도메인 승인 건은 이 역할이 규칙에 지정된 경우에만 대기 목록에 나타남 |
| SCR-KM-005 | 지식 지표 대시보드 | |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-KM-001 | /api/v1/knowledge/articles | GET | 전 상태 검색 |
| API-KM-002 | /api/v1/knowledge/articles/{id} | GET | 열람 |
| API-COM-003 | /api/v1/approvals?scope=mine | GET | 전 도메인 공용 승인 대기(특정 역할 전용 API 아님, 결과는 role claim 기준으로 필터링됨) |
| API-COM-004 | /api/v1/approvals/{approvalRequestId} | GET | 승인 인스턴스 상세(차수별 진행 상태) |
| API-COM-005 | /api/v1/approvals/{approvalRequestId}/decisions | POST | **게시 승인/반려**(대상 인스턴스의 현재 차수 필요 역할에 KNOWLEDGE_GATEKEEPER가 포함된 경우만 처리 가능) |
| API-KM-012 | /api/v1/knowledge/metrics | GET | 지식 지표 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- **SCR-COM-014·API-COM-003/004는 역할 제한 없이 인증된 모든 사용자가 접근할 수 있다**(화면·API 자체에 대한 403은 없음). 대신 **게시 승인/반려(API-COM-005)는 인스턴스별 동적 판정**을 거친다: 대상 인스턴스의 현재 대기 차수가 요구하는 역할 목록에 KNOWLEDGE_GATEKEEPER가 포함되지 않으면 → **403**.
- 반려 시 사유 필수(누락 400), 기사 초안(DRAFT) 복귀는 도메인이 자동 처리한다([knowledge.md](../../02_plan/api_spec/knowledge.md) API-KM-006 참조).
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
