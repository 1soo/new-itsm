# 통합 테스트 시나리오 — knowledge (KM)

> 실행 타임스탬프: 20260710-055238 · 도메인: knowledge
> 범위: API-KM-001~012 정상+오류(400/401/403/404), 역할별 검색/열람 가시범위, 작성/수정/삭제(Contributor 전용), 상태전이(DRAFT→IN_REVIEW), 검토승인/반려(Gatekeeper 전용), 유용성평가(미게시 400), 카테고리, KCS 티켓연계, 지표(deflectionRate 포함)
> **크로스 도메인 회귀**: problem workaround의 `linkedArticleId` 존재 검증(없으면 400) 추가분 확인

## 사전 조건

- 빌드 테스트 통과(BE `gradlew clean test`, FE `npm run build`)
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 테스트 계정:
  - `kc@itsm.local` — KNOWLEDGE_CONTRIBUTOR (기사 작성/수정/삭제/검토요청/피드백/KCS연계)
  - `kg@itsm.local` — KNOWLEDGE_GATEKEEPER (전 상태 조회/검토승인·반려/지표)
  - `agent@itsm.local` — SERVICE_DESK_AGENT (게시 기사만 검색/열람, 작성 계열 403 검증용)
  - `pm@itsm.local` — PROBLEM_MANAGER (workaround linkedArticleId 크로스 도메인 회귀용)
- 근거 기준: @docs/01_analyze/prd/knowledge.md, @docs/01_analyze/feature/knowledge.md, @docs/02_plan/api_spec/knowledge.md, @docs/02_plan/screen/knowledge.md, @docs/02_plan/security/authorization/knowledge_gatekeeper.md, @docs/02_plan/security/authorization/knowledge_contributor.md, @docs/02_plan/security/authorization/end_user.md, @docs/02_plan/security/authorization/service_desk_agent.md
- 격리: playwright 매 항목 새 context/storage 초기화. API는 계정당 로그인 세션 재사용(TTL 300s 내 그룹 실행).

## 시나리오

### A. 빌드
- **TC-BUILD-001** · BE `gradlew clean test` 통과(knowledge 패키지 JUnit 포함) — @docs/01_analyze/feature/knowledge.md 전 FEAT
- **TC-BUILD-002** · FE `npm run build`(tsc + vite build) 통과 — @docs/02_plan/screen/knowledge.md SCR-KM-001~005

### B. 인증/인가 (401/403)
- **TC-AUTH-001** · 미인증 `GET /api/v1/knowledge/articles` 401 — 공통 규약
- **TC-KM-RBAC-001** · agent(권한 없음) `POST /knowledge/articles` 작성 시도 403 — knowledge_contributor.md(Contributor 전용)
- **TC-KM-RBAC-002** · agent `POST /knowledge/articles/{id}/review` 승인 시도 403 — knowledge_gatekeeper.md(Gatekeeper 전용)
- **TC-KM-RBAC-003** · kc(Contributor)가 직접 게시 승인(`POST .../review`) 시도 403 — REQ-KM-003(게시 승인은 Gatekeeper 전용)
- **TC-KM-RBAC-004** · agent `GET /knowledge/reviews?scope=mine` 검토 대기 목록 조회 시도 403 — knowledge_gatekeeper.md

### C. 기사 작성·편집 (FEAT-KM-001 / API-KM-003/004/005)
- **TC-KM-001** · Contributor 정상 작성(title+body+categoryId) 201, `status=DRAFT` — REQ-KM-001 (Event-driven)
- **TC-KM-002** · title 누락 작성 400 — REQ-KM-001 (Unwanted)
- **TC-KM-003** · body 누락 작성 400 — REQ-KM-001 (Unwanted)
- **TC-KM-004** · 존재하지 않는 categoryId 작성 400 — FEAT-KM-007 (Unwanted)
- **TC-KM-005** · 기사 수정(PATCH, title 변경) 200 — API-KM-004
- **TC-KM-006** · 존재하지 않는 id 수정 404 — API-KM-004
- **TC-KM-007** · 기사 삭제(별도 생성분) 204 — API-KM-005
- **TC-KM-008** · 존재하지 않는 id 삭제 404 — API-KM-005

### D. 카테고리 (FEAT-KM-007 / API-KM-010)
- **TC-KM-009** · 카테고리 목록 조회 200, 시드 카테고리(2~3건) 포함 — API-KM-010

### E. 검색/목록·상세 열람 (역할별 가시범위) (FEAT-KM-004/005 / API-KM-001/002)
- **TC-KM-010** · Gatekeeper 검색 시 DRAFT 포함 전 상태 기사 조회 가능 — knowledge_gatekeeper.md("전 상태 조회")
- **TC-KM-011** · Contributor 검색 시 본인 DRAFT 포함 + 타 게시(PUBLISHED) 기사 조회, 타인 DRAFT는 미노출 — knowledge_contributor.md
- **TC-KM-012** · agent(END_USER 계열) 검색 시 PUBLISHED 기사만 반환(DRAFT 미노출) — end_user.md/service_desk_agent.md
- **TC-KM-013** · 매칭 없는 키워드 검색 200, 빈 결과 + 무결과 검색 기록(noResult 또는 이후 지표 반영) — REQ-KM-004 (Unwanted)
- **TC-KM-014** · 게시 기사 상세 조회 200, `{title,body,status,category,labels,helpful,notHelpful}` 구조 — API-KM-002
- **TC-KM-015** · 미게시(DRAFT) 기사에 agent(최종 사용자 계열) 접근 403 — REQ-KM-005 (Unwanted)
- **TC-KM-016** · 존재하지 않는 id 상세 404 — API-KM-002

### F. 상태 전이 (FEAT-KM-002 / API-KM-006)
- **TC-KM-017** · DRAFT→IN_REVIEW 전이(검토 요청) 200 — REQ-KM-002 (Event-driven)
- **TC-KM-018** · 허용되지 않은 전이(예: IN_REVIEW 상태에서 다시 IN_REVIEW, 또는 DRAFT가 아닌데 IN_REVIEW 요청) 400 — FEAT-KM-002 (Unwanted)
- **TC-KM-019** · 존재하지 않는 id 전이 404 — API-KM-006

### G. 검토·게시 승인/반려 (FEAT-KM-003 / API-KM-007/008)
- **TC-KM-020** · 검토 대기 목록(Gatekeeper, scope=mine) 200, IN_REVIEW 대상 포함 — API-KM-008
- **TC-KM-021** · Gatekeeper 승인(decision=APPROVE) 200, `status=PUBLISHED` — REQ-KM-003 (Event-driven)
- **TC-KM-022** · Gatekeeper 반려(decision=REJECT, reason 포함) 200, `status=DRAFT` 복귀 + 사유 확인 — REQ-KM-003 (Unwanted, 반려)
- **TC-KM-023** · 반려 시 사유(reason) 누락 400 — API-KM-007 400
- **TC-KM-024** · 존재하지 않는 id 검토 404 — API-KM-007 404

### H. 유용성 평가 (FEAT-KM-006 / API-KM-009)
- **TC-KM-025** · 게시 기사 유용성 평가(helpful=true) 200, `{helpful,notHelpful}` 집계 갱신 — REQ-KM-006 (Event-driven)
- **TC-KM-026** · 미게시(DRAFT) 기사 평가 시도 400 — FEAT-KM-006 (Unwanted)
- **TC-KM-027** · 존재하지 않는 id 평가 404 — API-KM-009 404

### I. KCS 티켓 연계 (FEAT-KM-008 / API-KM-011)
- **TC-KM-028** · 기존 게시 기사 + 인시던트 연계(ticketType=INCIDENT, articleId) 200, `{articleId,ticketId}` — REQ-KM-008 (Event-driven)
- **TC-KM-029** · newArticle로 신규 기사 작성+문제(PROBLEM) 연계 200 — REQ-KM-008 (Event-driven)
- **TC-KM-030** · 존재하지 않는 티켓(ticketId)에 연계 400 — FEAT-KM-008 (Unwanted)

### J. 지식 지표 (FEAT-KM-009 / API-KM-012)
- **TC-KM-031** · 지표 조회 200, `{usageCount,noResultSearchCount,helpfulRate,deflectionRate,topNoResultKeywords}` 구조, 앞선 검색/평가 반영(상대검증) — REQ-KM-009 (Ubiquitous)
- **TC-KM-032** · 데이터 없는 기간 조회 200, 빈/0 결과 — FEAT-KM-009 (Unwanted)

### K. 크로스 도메인 회귀 — problem workaround linkedArticleId 검증
- **TC-CROSS-001** · `POST /problems/{id}/workaround {content, linkedArticleId: 존재하는 게시 기사}` 200 — 회귀 없음 확인
- **TC-CROSS-002** · `POST /problems/{id}/workaround {content, linkedArticleId: 존재하지 않는 id}` 400 — 개발계획 knowledge.md §크로스 도메인(신규 검증 추가분)

### L. FE E2E (playwright, http://localhost:5173, 매 항목 새 context/storage)
- **TC-E2E-001** · Contributor 로그인 → 기사 작성(SCR-KM-003) 제목/본문/카테고리 입력 → 저장(DRAFT) → 검토 요청(IN_REVIEW 전이) — SCR-KM-003
- **TC-E2E-002** · Gatekeeper 로그인 → 검토·게시 승인함(SCR-KM-004) 대기 목록 확인 → 승인 처리(PUBLISHED) — SCR-KM-004
- **TC-E2E-003** · 지식베이스 검색/목록(SCR-KM-001) 키워드 검색·상태 배지 표시 — SCR-KM-001
- **TC-E2E-004** · 기사 열람(SCR-KM-002) 게시 기사 열람 + 유용성 평가(도움됨) 위젯 동작 — SCR-KM-002
- **TC-E2E-005** · 지식 지표 대시보드(SCR-KM-005) KPI 카드 + 무결과 키워드 랭킹 표시 — SCR-KM-005
- **TC-E2E-006** · 비-Contributor/Gatekeeper(agent) 로그인 시 기사 작성/검토승인함 메뉴 비노출, `/knowledge/new` 직접 접근 시 `/403` 리다이렉트 — knowledge_contributor.md/knowledge_gatekeeper.md 인가
