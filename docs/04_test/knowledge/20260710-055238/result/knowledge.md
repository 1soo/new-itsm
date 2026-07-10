# 통합 테스트 결과 — knowledge (KM) (20260710-055238)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 계정: kc@itsm.local(KNOWLEDGE_CONTRIBUTOR) / kg@itsm.local(KNOWLEDGE_GATEKEEPER) / agent@itsm.local(SERVICE_DESK_AGENT, 게시만 열람+403검증) / pm@itsm.local(PROBLEM_MANAGER, 크로스 도메인 회귀)
> 범위: API-KM-001~012 정상+오류(400/401/403/404), 역할별 검색·열람 가시범위, 작성/수정/삭제(Contributor 전용), 상태전이, 검토승인/반려(Gatekeeper 전용), 유용성평가, 카테고리, KCS 티켓연계, 지표 + problem workaround linkedArticleId 검증(크로스 도메인)

## 요약

- 총 **44건** (빌드 2 · 인증/인가 5 · 작성/편집 8 · 카테고리 1 · 검색/열람 7 · 상태전이 3 · 검토승인 5 · 유용성평가 3 · KCS연계 3 · 지표 2 · 크로스도메인 2 · FE E2E 6) · **성공 44 · 실패 0**

## 상세 — 빌드

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-BUILD-001 (BE gradlew clean test) | PASS | `BUILD SUCCESSFUL`(5 tasks executed, clean 포함 전체 재실행), knowledge 패키지 JUnit 포함 |
| TC-BUILD-002 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1858 modules, TS 오류 없음) |

## 상세 — 인증/인가

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-AUTH-001 (미인증 GET articles) | PASS | 401 UNAUTHENTICATED |
| TC-KM-RBAC-001 (agent POST articles) | PASS | 403 ACCESS_DENIED |
| TC-KM-RBAC-002 (agent POST .../review) | PASS | 403 ACCESS_DENIED |
| TC-KM-RBAC-003 (kc 직접 승인 시도) | PASS | 403 ACCESS_DENIED(게시 승인은 Gatekeeper 전용) |
| TC-KM-RBAC-004 (agent GET reviews?scope=mine) | PASS | 403 ACCESS_DENIED |

## 상세 — 기사 작성·편집

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-001 (정상 작성) | PASS | 201, `{"id":3,"status":"DRAFT"}` |
| TC-KM-002 (title 누락) | PASS | 400 VALIDATION_ERROR |
| TC-KM-003 (body 누락) | PASS | 400 VALIDATION_ERROR |
| TC-KM-004 (없는 categoryId) | PASS | 400 "유효하지 않은 카테고리입니다." |
| TC-KM-005 (기사 수정) | PASS | 200, title 변경 반영 확인(상세 재조회) |
| TC-KM-006 (없는 id 수정) | PASS | 404 ARTICLE_NOT_FOUND |
| TC-KM-007 (기사 삭제) | PASS | 204 |
| TC-KM-008 (없는 id 삭제) | PASS | 404 ARTICLE_NOT_FOUND |

## 상세 — 카테고리

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-009 (카테고리 목록) | PASS | 200, `[{"id":1,"name":"네트워크"},{"id":2,"name":"계정/권한"},{"id":3,"name":"결제"}]` 시드 확인 |

## 상세 — 검색/목록·상세 열람 (역할별 가시범위)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-010 (Gatekeeper 전 상태 조회) | PASS | DRAFT/IN_REVIEW/PUBLISHED 전부 포함(totalElements=4, 상태 혼합 확인) |
| TC-KM-011 (Contributor 본인 초안+게시) | PASS | kg와 동일 결과(본인 작성 DRAFT/IN_REVIEW + 타 게시 PUBLISHED 포함) |
| TC-KM-012 (agent 게시만) | PASS | totalElements=2, PUBLISHED만 반환(DRAFT/IN_REVIEW 미노출) |
| TC-KM-013 (무결과 검색) | PASS | 200, `{"content":[],"totalElements":0,"noResult":true}` |
| TC-KM-014 (게시 기사 상세) | PASS | 200, agent 계정으로 PUBLISHED 상세 조회 성공(title/body/status/category/labels/helpful/notHelpful 구조 확인) |
| TC-KM-015 (미게시 기사 최종사용자 접근) | PASS | agent가 DRAFT 기사 상세 조회 시 403 ACCESS_DENIED |
| TC-KM-016 (없는 id 상세) | PASS | 404 ARTICLE_NOT_FOUND |

## 상세 — 상태 전이

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-017 (DRAFT→IN_REVIEW) | PASS | 200, status=IN_REVIEW |
| TC-KM-018 (허용되지 않은 전이) | PASS | 400 INVALID_STATUS_TRANSITION(IN_REVIEW 상태에서 IN_REVIEW 재요청) |
| TC-KM-019 (없는 id 전이) | PASS | 404 ARTICLE_NOT_FOUND |

## 상세 — 검토·게시 승인/반려

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-020 (검토 대기 목록) | PASS | 200, IN_REVIEW 대상 포함 |
| TC-KM-021 (승인) | PASS | 200, status=PUBLISHED |
| TC-KM-022 (반려, 사유 포함) | PASS | 200, status=DRAFT 복귀(상세 재조회로 확인) |
| TC-KM-023 (반려 사유 누락) | PASS | 400 REJECT_REASON_REQUIRED |
| TC-KM-024 (없는 id 검토) | PASS | 404 ARTICLE_NOT_FOUND |

## 상세 — 유용성 평가

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-025 (게시 기사 평가) | PASS | 200, `{"helpful":1,"notHelpful":0}` |
| TC-KM-026 (미게시 기사 평가) | PASS | 400 ARTICLE_NOT_PUBLISHED |
| TC-KM-027 (없는 id 평가) | PASS | 404 ARTICLE_NOT_FOUND |

## 상세 — KCS 티켓 연계

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-028 (기존 기사+인시던트 연계) | PASS | 200, `{"articleId":3,"ticketId":22}` |
| TC-KM-029 (newArticle+문제 연계) | PASS | 200, `{"articleId":7,"ticketId":26}`(신규 기사 생성·연결) |
| TC-KM-030 (없는 티켓 연계) | PASS | 400 LINK_TARGET_NOT_FOUND |

## 상세 — 지식 지표

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-KM-031 (지표 조회) | PASS | 200, `{"usageCount":2,"noResultSearchCount":2,"helpfulRate":100.0,"deflectionRate":50.0,"topNoResultKeywords":[...]}` |
| TC-KM-032 (데이터 없는 기간) | PASS | 200, 전 항목 0/빈 배열 |

## 상세 — 크로스 도메인 회귀 (problem workaround linkedArticleId)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CROSS-001 (유효한 linkedArticleId) | PASS | `POST /problems/{id}/workaround {linkedArticleId:3(게시됨)}` → 200 |
| TC-CROSS-002 (존재하지 않는 linkedArticleId) | PASS | 400 LINK_TARGET_NOT_FOUND(신규 검증 추가분 정상 동작, 이전 problem 단계 스텁에서 개선됨) |

## 상세 — FE E2E (playwright, localhost:5173, 매 항목 새 context/storage)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-E2E-001 (Contributor 작성→검토요청) | PASS | 기사 작성(SCR-KM-003) 제목/본문 입력 후 저장(DRAFT) → 상세 편집 이동 → "검토 요청" 클릭 → 상태 배지 "검토"로 전환 확인 |
| TC-E2E-002 (Gatekeeper 승인) | PASS | 검토·게시 승인함(SCR-KM-004) 대기 목록에 작성분 노출 → 승인 다이얼로그(의견 입력) → 승인 처리 후 목록에서 제거·"게시되었습니다" 토스트 |
| TC-E2E-003 (지식베이스 목록) | PASS | 필터(키워드/카테고리/라벨/상태), 상태 배지(게시/초안/검토), 유용성 퍼센트 표시, Gatekeeper 전 상태 노출 확인 |
| TC-E2E-004 (기사 열람+유용성 평가) | PASS | 게시 기사 열람(SCR-KM-002), "도움됨" 클릭 시 집계(1→2) 즉시 반영 + "평가가 저장되었습니다" 토스트 |
| TC-E2E-005 (지식 지표 대시보드) | PASS | KPI 카드(사용량/무결과 검색/유용성/티켓 차단율) + 무결과 검색 키워드 랭킹 목록 표시 |
| TC-E2E-006 (agent RBAC) | PASS | 사이드바에 "지식베이스"만 노출(기사 작성/검토·게시 승인함/지식 지표 비노출), 목록도 PUBLISHED만 표시, `/knowledge/new` 직접 접근 시 `/403` 리다이렉트 |

## 실패 항목 분석

- 없음

## 결론

- knowledge 도메인 핵심 기능(작성·수정·삭제(Contributor 전용)·상태전이(DRAFT→IN_REVIEW)·검토승인/반려(Gatekeeper 전용, 반려사유 필수)·역할별 검색/열람 가시범위(Gatekeeper 전 상태/Contributor 본인초안+게시/최종사용자 게시만)·유용성평가(미게시 400)·카테고리·KCS 티켓연계(기존연결/신규작성)·지표(usageCount/noResultSearchCount/helpfulRate/deflectionRate/topNoResultKeywords)) 및 RBAC(Contributor/Gatekeeper/agent 403), FE 5+1 화면(작성·편집/검토승인함/목록/열람/지표/RBAC) **전부 정상 동작**.
- **크로스 도메인 회귀 정상**: problem workaround의 `linkedArticleId` 존재 검증 추가분(없으면 400 LINK_TARGET_NOT_FOUND, 있으면 200) 확인.
- 잔여 실패 없음. knowledge 도메인 통합테스트 전건 통과.
