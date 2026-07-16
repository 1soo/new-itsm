# 역할 정의 — END_USER

> 역할: END_USER · 버전: 0.1 · 작성일: 2026-07-09

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

## 공통 기본 접근 (전 역할 공통)

앱 셸/헤더/사이드바/푸터·토스트(SCR-COM-001~004/009), 로그인·내 프로필·비밀번호 변경(SCR-AUTH-001~003), 404, 자기 인증 API(API-AUTH-003/004/005), 내 메뉴 조회(API-AUTH-022), 통합 검색 API(API-SEARCH-001, 접근 가능 도메인만 결과 포함 — 본 역할은 KNOWLEDGE 게시 기사·SERVICE_REQUEST 본인 요청만 대상). SYSTEM_ADMIN은 모든 역할의 화면·API에 예외적으로 접근 가능하다(system_admin.md 2절 참조). 이하 역할 고유 접근만 기술한다.

## 1. 페르소나

- **역할명**: END_USER (최종 사용자 / 요청자)
- **설명**: 서비스 포털에서 요청을 제출·추적하고 지식베이스를 셀프서비스로 활용하는 직원/고객.
- **주요 목표**: 필요한 서비스를 요청하고 진행 상황을 확인하며, 게시된 지식으로 스스로 문제를 해결(티켓 차단).

## 2. 접근 가능 화면

| 화면 ID | 화면명 | 비고 |
|---------|--------|------|
| SCR-SRM-001 | 서비스 포털(카탈로그 브라우즈) | |
| SCR-SRM-002 | 요청 제출(동적 양식) | 지식 추천 노출 |
| SCR-SRM-003 | 내 요청 목록 | 본인 요청만 |
| SCR-SRM-005 | 요청 상세 | 본인 요청·코멘트·CSAT |
| SCR-KM-001 | 지식베이스 검색/목록 | 게시 기사만 |
| SCR-KM-002 | 기사 열람(셀프서비스) | 유용성 평가 |
| SCR-ESM-001 | 부서 서비스 포털(카탈로그 브라우즈) | |
| SCR-ESM-002 | 부서 요청 제출(동적 양식) | |
| SCR-ESM-003 | 내 부서 요청 목록 | 본인 요청만 |
| SCR-ESM-005 | 부서 요청 상세 | 본인 요청·코멘트·연계 체크리스트 조회 |
| SCR-ESM-009 | 온보딩/오프보딩 체크리스트 상세 | 본인 요청에 연계된 체크리스트만 |

## 3. 접근 가능 API

| API ID | Endpoint | Method | 비고 |
|--------|----------|--------|------|
| API-SRM-001 | /api/v1/service-catalog/items | GET | 카탈로그 목록 |
| API-SRM-002 | /api/v1/service-catalog/items/{id} | GET | 양식 스키마 |
| API-SRM-005 | /api/v1/knowledge/suggestions | GET | 기사 추천 |
| API-SRM-006 | /api/v1/service-requests | POST | 요청 제출 |
| API-SRM-007 | /api/v1/service-requests?scope=mine | GET | 본인 요청 목록 |
| API-SRM-008 | /api/v1/service-requests/{id} | GET | 본인 요청 상세 |
| API-SRM-010 | /api/v1/service-requests/{id}/status | PATCH | 종료 확인(요청자 범위) |
| API-SRM-013 | /api/v1/service-requests/{id}/comments | POST | 본인 요청 코멘트 |
| API-SRM-014 | /api/v1/service-requests/{id}/csat | POST | CSAT 제출 |
| API-KM-001 | /api/v1/knowledge/articles | GET | 게시 기사만 반환 |
| API-KM-002 | /api/v1/knowledge/articles/{id} | GET | 게시 기사 열람 |
| API-KM-009 | /api/v1/knowledge/articles/{id}/feedback | POST | 유용성 평가 |
| API-KM-010 | /api/v1/knowledge/categories | GET | 카테고리 |
| API-ESM-001 | /api/v1/esm/catalog-items | GET | 부서 카탈로그 목록 |
| API-ESM-002 | /api/v1/esm/catalog-items/{id} | GET | 양식 스키마 |
| API-ESM-005 | /api/v1/esm/requests | POST | 부서 요청 제출 |
| API-ESM-006 | /api/v1/esm/requests?scope=mine | GET | 본인 요청 목록 |
| API-ESM-007 | /api/v1/esm/requests/{id} | GET | 본인 요청 상세 |
| API-ESM-009 | /api/v1/esm/requests/{id}/comments | POST | 본인 요청 코멘트 |
| API-ESM-014 | /api/v1/esm/checklists/{id} | GET | 본인 요청에 연계된 체크리스트만 |

## 4. 접근 제어 규칙

- 로그인 필요 화면/요청 접근 시 Access Token의 `role`을 검증한다.
- 이 역할(END_USER)에 없는 화면/요청 → **403**, 있는 경우 → 정상 수행.
- 요청 상세·코멘트·CSAT는 **본인 요청**에 한정(타인 요청 접근 시 403). ESM 부서 요청도 동일 기준.
- 지식 API는 **게시(PUBLISHED)** 기사만 반환하며, 미게시 기사 접근 시 403.
- ESM 체크리스트(API-ESM-014)는 **본인이 제출한 요청에 연계된 체크리스트**만 조회 가능(타인 요청의 체크리스트 접근 시 403).
- 화면 이동마다 Backend에 권한 확인 API를 호출한다.
