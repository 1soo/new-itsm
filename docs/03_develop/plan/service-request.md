# 개발 계획 — service-request (서비스 요청 관리, SRM)

> 도메인: service-request (SRM) · 개발 순서 2/7 · 작성: dev-lead · 2026-07-09

## 1. 목표

셀프서비스 포털(카탈로그·동적 양식·지식 추천), 요청 제출/큐/상세(검증·라우팅·승인·이행·종료), SLA·코멘트·타임라인·CSAT, 카탈로그 관리, 지표 대시보드를 구현한다. auth 기반(인증·RBAC·공통 컴포넌트·apiClient)을 재사용하고, **common 교차 테이블(approval/comment/timeline_event)**을 이 단계에서 도입한다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/service-request.md`(SCR-SRM-001~008), 공통 패턴 SCR-COM-007/008 재사용
- API: `api_spec/service-request.md`(API-SRM-001~015, Base `/api/v1`)
- DB: `database/service-request.md`(queue/service_catalog_item/catalog_form_field/service_request/service_request_form_value/csat) + `database/common.md`(approval/comment/timeline_event)
- 역할: `security/authorization/`(END_USER, SERVICE_DESK_AGENT, APPROVER, PROCESS_OWNER 등)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- SRM 테이블: queue, service_catalog_item, catalog_form_field(JSONB options), service_request(ticket_key SRM-YYYY-####, status 상태셋, SLA 컬럼), service_request_form_value(EAV), csat(1:1, CHECK 1~5). 공통컬럼·FK·UNIQUE(6절).
- **common 교차 테이블 도입**: approval(ticket_type='SERVICE_REQUEST'), comment, timeline_event. (ticket_link는 incident 단계에서 도입 — SRM 미사용)
- screen/screen_role 증분: SCR-SRM-001~008 화면 seed + 역할정의서(END_USER/AGENT/APPROVER/PROCESS_OWNER) 기반 screen_role 매핑 추가. auth seed는 유지.
- 시연용 최소 seed 선택: 기본 queue(is_default), 예시 카탈로그 항목 1~2개(양식 필드 포함) — tester/데모 편의(과하지 않게).

### BE (dev-backend) — `source/backend/`
- API-SRM-001~015 전부(api_spec 준수). SRM 도메인 패키지 추가(기존 DDD 구조 재사용).
- 상태 전이 머신: SUBMITTED→VALIDATED→ROUTED→APPROVAL_PENDING→IN_FULFILLMENT→FULFILLED→CLOSED/REJECTED. 허용되지 않은 전이 400, 승인 대기 중 이행 409, 전이별 권한 상이(403).
- 승인: common.approval 사용(지정 승인자만 200, 그 외 403, 반려 사유 필수 400). 승인 대기 목록(API-SRM-012).
- SLA: 카탈로그 SLA 분값으로 sla_response_due/resolve_due 산정, sla_status(OK/WARNING/BREACHED) 계산.
- 코멘트(common.comment)/타임라인(common.timeline_event) 기록. CSAT(종료 요청만, 요청자만). 지표 집계(API-SRM-015).
- RBAC: scope=mine(본인)/all·queue(Agent 이상), 카탈로그 생성·수정(Process Owner), 배정(Agent), 승인(지정 Approver).
- **지식 추천(API-SRM-005) + 상세 linkedArticles**: knowledge 도메인 미구축이므로 **빈 배열([]) 반환(스펙의 "매칭 없으면 빈 배열" 준수)**. KM 도메인 개발 시 실제 연동으로 교체(코드에 TODO 표기). 감사/OpenAPI 규약은 auth와 동일.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 재사용 컴포넌트 추가(도메인 걸쳐 재사용될 것): KPI 카드, 간단 추이 차트, 별점(Rating) 위젯, 동적 폼 렌더러(스키마 기반)·양식 필드 빌더(반복 입력). 기존 팔레트/토큰·공통 컴포넌트 활용.
- 도메인 전용 조립은 FE. dev-frontend와 어떤 것을 common으로 올릴지 착수 초기 합의.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 화면: SCR-SRM-001 포털, 002 요청제출(동적양식+추천패널), 003 내 요청목록, 004 요청 큐, 005 요청 상세(상태전이·승인표시·SLA타이머·코멘트·CSAT), 006 승인 대기함, 007 카탈로그 관리(양식 빌더), 008 지표 대시보드.
- 라우팅/사이드바 메뉴에 서비스요청 항목 추가(RBAC: END_USER 포털·내요청 / Agent 큐·상세 / Approver 승인함 / Process Owner 카탈로그관리). screen.path 정합(dev-database와 확인).
- 상태 전이 버튼은 허용 전이만 노출(403 방지), 승인 대기 중 이행 버튼 숨김, 종료 요청 재종료 차단, CSAT는 종료+요청자만.

## 4. 진행 순서 · 의존성

1. DB 스키마·common 테이블·screen seed 먼저(BE 영속성 기반). BE는 스키마 확정 후 연동.
2. BE API 계약(api_spec) 확정 → FE 연동. UI 신규 컴포넌트 → FE 조립.
3. 계약 단일 기준 api_spec/service-request.md. 이견은 dev-lead, 설계 이슈는 designer.

## 5. 완료(테스트 통과) 기준

- BE: API-SRM-001~015 정상 + 오류(400/401/403/404/409), 상태 전이 규칙, 승인 권한, SLA 계산, CSAT 제약, 지표. 지식 추천 []는 정상 취급.
- FE: 포털→제출→목록/큐→상세(전이·승인·코멘트·CSAT)→승인함→카탈로그관리→지표 대시보드 E2E.
- tester 통합테스트 실패 0 → `feat(service-request): ...` 커밋/푸시.

## 6. 파일 소유 (충돌 방지)

- `source/db/` = DB / `source/backend/`(srm 패키지 신설) = BE / `source/frontend/` 공통 = UI, 기능 = FE. auth 산출물 파일은 수정 최소화(공유 apiClient/레이아웃 확장은 협의).

## 7. 특이사항

- 지식 도메인(KM) 미구축: 추천·linkedArticles는 빈 배열. KM 단계에서 연동(양쪽 계획에 교차 표기).
