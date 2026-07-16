# 역할 정의 — KNOWLEDGE_CONTRIBUTOR

> 역할: KNOWLEDGE_CONTRIBUTOR · 버전: 0.1 · 작성일: 2026-07-09

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

## 공통 기본 접근 (전 역할 공통)

앱 셸/공통 컴포넌트(SCR-COM-*), 로그인·프로필·비밀번호(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: KNOWLEDGE_CONTRIBUTOR (지식 기여자)
- **설명**: 지식 기사를 작성·수정·삭제하고 검토를 요청하며, 티켓 처리 중 기사를 작성/연결(KCS)한다.
- **주요 목표**: 재사용 가능한 지식을 축적하여 셀프서비스·티켓 차단을 높인다.

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-KM-001 | 지식베이스 검색/목록 | 초안 포함 본인 기사 조회 |
| SCR-KM-002 | 기사 열람 | |
| SCR-KM-003 | 기사 작성·편집 | 검토 요청·삭제 |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-KM-001 | /api/v1/knowledge/articles | GET | 검색(본인 초안 포함) |
| API-KM-002 | /api/v1/knowledge/articles/{id} | GET | 열람 |
| API-KM-003 | /api/v1/knowledge/articles | POST | 작성 |
| API-KM-004 | /api/v1/knowledge/articles/{id} | PATCH | 수정 |
| API-KM-005 | /api/v1/knowledge/articles/{id} | DELETE | 삭제 |
| API-KM-006 | /api/v1/knowledge/articles/{id}/status | PATCH | 검토 요청(초안→검토) |
| API-KM-009 | /api/v1/knowledge/articles/{id}/feedback | POST | 유용성 평가 |
| API-KM-010 | /api/v1/knowledge/categories | GET | 카테고리 |
| API-KM-011 | /api/v1/knowledge/articles/link | POST | KCS 티켓 연계 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- **게시 승인(API-KM-007)은 KNOWLEDGE_GATEKEEPER 전용**이라 기여자는 게시 불가(403).
- 제목/본문 누락 시 400, 존재하지 않는 카테고리 지정 시 400.
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
