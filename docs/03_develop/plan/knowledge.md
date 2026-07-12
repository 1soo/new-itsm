# 개발 계획 — knowledge (지식 관리, KM)

> 도메인: knowledge (KM) · 개발 순서 6/7 · 작성: dev-lead · 2026-07-09

## 1. 목표

지식 기사 작성·편집·삭제, 상태 관리(DRAFT/IN_REVIEW/PUBLISHED), 게이트키퍼 검토·게시 승인/반려, 지식베이스 검색/열람(셀프서비스), 유용성 평가, 카테고리/라벨 분류, KCS 티켓 연계, 무결과 검색 로그 기반 지식 지표를 구현한다. auth/incident/problem/srm/change 기반과 common(ticket_link) 재사용.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/knowledge.md`(SCR-KM-001~005), 공통 SCR-COM-007/008
- API: `api_spec/knowledge.md`(API-KM-001~012)
- DB: `database/knowledge.md`(knowledge_category/knowledge_label/knowledge_article/article_label/knowledge_feedback/knowledge_review/search_log) + `database/common.md`(ticket_link)
- 역할: `security/authorization/knowledge_gatekeeper.md`(KNOWLEDGE_GATEKEEPER), `security/authorization/knowledge_contributor.md`(KNOWLEDGE_CONTRIBUTOR)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- KM 테이블: knowledge_category(name UNIQUE), knowledge_label(name UNIQUE), knowledge_article(title/body 필수, status DRAFT/IN_REVIEW/PUBLISHED, category_id FK, author_id FK, helpful_count/not_helpful_count/view_count 집계, published_at), article_label(article_id+label_id FK, UNIQUE), knowledge_feedback(article_id/user_id FK, helpful, comment), knowledge_review(article_id/gatekeeper_id FK, decision APPROVE/REJECT, reason), search_log(keyword, result_count, user_id, searched_at). 공통컬럼 동일 적용.
- ticket_link는 기존 공통 테이블 재사용(target_type='KNOWLEDGE'). 신규 테이블 아님.
- screen/screen_role 증분: SCR-KM-001~005 + KNOWLEDGE_GATEKEEPER(001/002/004/005), KNOWLEDGE_CONTRIBUTOR(001/002/003) 역할 매핑. 두 역할 모두 이미 auth 시드(02_seed.sql)에 role 정의 존재 → 신규 role 생성 없이 화면/API 매핑만 증분.
- 카테고리 시드 최소 2~3건(예: "네트워크", "계정/권한", "결제").
- 테스트 유저: KNOWLEDGE_CONTRIBUTOR 계정, KNOWLEDGE_GATEKEEPER 계정 시드(기존 유저 시드 규칙 동일).

### BE (dev-backend) — `source/backend/`
- API-KM-001~012(api_spec). knowledge 패키지 신설(incident/problem/change 패키지 컨벤션 재사용).
- 검색/목록(API-KM-001): keyword/category/label/status 필터. 최종 사용자(그 외 역할)는 PUBLISHED만, Contributor는 본인 초안 포함, Gatekeeper는 전 상태 조회. 매칭 없으면 빈 결과 + search_log 기록(result_count=0). 검색어 있을 때마다 search_log 기록(무결과든 아니든 usageCount 집계용).
- 상세/열람(API-KM-002): 미게시 기사에 최종 사용자(Contributor/Gatekeeper 아닌 경우) 접근 시 403. view_count 증가(열람 시).
- 작성/수정/삭제(API-KM-003~005): Contributor 전용(403), 제목/본문 필수(400), 존재하지 않는 카테고리(400).
- 상태 전이(API-KM-006): DRAFT→IN_REVIEW만 허용(그 외 400).
- 검토·게시 승인/반려(API-KM-007), 검토 대기 목록(API-KM-008): Gatekeeper 전용(403). 반려 시 사유 필수(400)·초안(DRAFT) 복귀. 승인 시 PUBLISHED + published_at 기록. knowledge_review 이력 저장.
- 유용성 평가(API-KM-009): 미게시 기사 평가 거부(400). helpful_count/not_helpful_count 갱신, knowledge_feedback 저장.
- 카테고리 목록(API-KM-010).
- KCS 티켓 연계(API-KM-011): ticketType(SERVICE_REQUEST/INCIDENT/PROBLEM) + ticketId + (기존 articleId 또는 newArticle) → ticket_link(target_type='KNOWLEDGE') 생성. 존재하지 않는 티켓 400.
- 지식 지표(API-KM-012): usageCount(search_log 건수 또는 view_count 합), noResultSearchCount(result_count=0 건수), helpfulRate, deflectionRate, topNoResultKeywords(빈도 상위).
  - **deflectionRate 산식(확정)**: 기간 내 게시된 기사 중 열람(view_count>0)되었으나 KCS 티켓 연계가 없는 기사 비율(%) = "티켓 연계 없이 셀프서비스로 해결된 것으로 추정되는 비율". (설계 문서에 명시적 산식 없어 dev-backend가 근사치로 확정, dev-lead 확인 완료.)
- RBAC는 role 정의서(knowledge_gatekeeper.md, knowledge_contributor.md, 단일 원천) 기준.
- **크로스 도메인(선택, 완료 기준 아님)**: problem의 `WorkaroundRequest.linkedArticleId`는 현재 존재 검증 없이 저장만 함(problem 단계에서 knowledge 미구축이라 스텁). knowledge 엔티티가 생기는 이번 단계에서 여유가 되면 KnowledgeArticleRepository로 존재 검증(400) 추가 권장하되, 시간 부족 시 스킵 가능(설계 문서에 명시적 요구 없음).
- JUnit 통합테스트(incident/problem/change integration 테스트 패턴 재사용).

### UI (dev-ui) — `source/frontend/` 공통 영역
- 기사 에디터(제목/본문 폼)·유용성 위젯(예/아니오+코멘트)·카테고리/라벨 태그 선택 등 신규 요소는 최소 공용화, 기존 목록/상세 패턴 재사용.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- SCR-KM-001 지식베이스 검색/목록(키워드·카테고리·라벨·상태 필터, 상태 배지)
- SCR-KM-002 기사 열람(셀프서비스, 유용성 평가 위젯)
- SCR-KM-003 기사 작성·편집(제목/본문 필수, 카테고리/라벨, 검토 요청, 삭제 확인 다이얼로그)
- SCR-KM-004 검토·게시 승인함(승인/반려+사유)
- SCR-KM-005 지식 지표 대시보드(KPI 카드 + 무결과 키워드 랭킹)
- 사이드바 메뉴 RBAC: KNOWLEDGE_CONTRIBUTOR(001/002/003), KNOWLEDGE_GATEKEEPER(001/002/004/005). roles.ts에 두 상수 추가. routes/index.tsx, navConfig.tsx에 SCR-KM-001~005 라우팅 추가(기존 도메인 패턴 재사용).

## 4. 진행 순서 · 의존성
1. DB(테이블·카테고리 시드·Contributor/Gatekeeper 유저 시드) → BE 연동 → FE 연동. UI 신규 최소.
2. 계약 단일 기준 api_spec/knowledge.md. deflectionRate 산식 등 설계 모호점은 designer에게 질문 후 확정.

## 5. 완료(테스트 통과) 기준
- BE: API-KM-001~012 정상+오류(400/401/403/404), 검색(역할별 상태 필터링·무결과 로깅)·작성/수정/삭제(Contributor 전용)·상태전이·검토승인/반려(Gatekeeper 전용, 사유필수)·유용성평가(미게시 거부)·카테고리·KCS연계·지표.
- FE: 작성→검토요청→(Gatekeeper)승인→목록/열람→유용성평가→지표대시보드 E2E. Contributor/Gatekeeper RBAC 확인.
- tester 통합테스트 실패 0 → `feat(knowledge): ...` 커밋/푸시.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(knowledge 패키지) BE / `source/frontend/` 공통 UI·기능 FE. 기존 도메인 파일 수정 최소(problem의 linkedArticleId 검증 보강 시에만 예외).

## 7. 특이사항
- ticket_link는 기존 공통 테이블 재사용. deflectionRate 정의가 설계 문서에 명시적 산식이 없어 dev-backend 구현 전 애매하면 designer에게 확인 후 진행.

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음. SCR-KM-004(검토·게시 승인함)는 SCR-COM-014로 대체되어 이번 phase 대상 아님.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/knowledge.md` 3절): `KnowledgeListPage.tsx`(SCR-KM-001), `ArticleViewPage.tsx`(002), `ArticleEditPage.tsx`(003), `KnowledgeMetricsPage.tsx`(005).
- `features/knowledge/status.ts` — `t` 인자를 받도록 전환, 호출부(각 Page.tsx, `features/search/status.ts`의 KNOWLEDGE 분기) 갱신. 통합 검색 결과(SCR-COM-011)에서 지식 도메인 상태 배지도 이 시점에 정상 전환됨(problem/incident phase에서 이미 전환된 다른 도메인처럼 검증).
- `format.ts` 확인 필수 — 라벨이 섞여 있으면 그 라벨만 전환.
- `useTranslation(["knowledge", "common"])` 사용. `locales/{ko,en}/knowledge.json`(현재 `{}` 스캐폴딩) 단독 소유, 직접 채운다.
- 값이 비어있는 값에 대한 라벨 조회 시 원시 키 노출 회귀 없는지 라벨 함수에 falsy 가드 확인(problem phase 패턴).
- 링크/연계 타입 등 열거형 값을 그대로 노출하는 부분이 있으면(change phase에서 발견된 것과 같은 유형) 함께 점검.

### 완료 기준
- English 전환 시 검색/목록·기사 열람·작성/편집·지표 대시보드 전체 텍스트(상태·분류 라벨 포함) 영어 전환.
- 검토 요청·유용성 평가·통합검색 연동 등 기존 기능 회귀 없음(텍스트만 치환).
