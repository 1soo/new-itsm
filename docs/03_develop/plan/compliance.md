# 개발 계획 — compliance (컴플라이언스 관리, COMP)

> 도메인: compliance · 개발 순서 3/4(확장 도메인) · 작성: dev-lead · 2026-07-10

## 1. 목표

컴플라이언스 요구사항 등록·목록/상세(책임자 지정·시정조치 추적·변경 요청 연계·감사 로그 조회), 준수 현황 대시보드를 구현한다. auth(audit_log 재사용)·change(ticket_link 연계) 기반. 단일 역할(COMPLIANCE_OFFICER) 도메인이라 규모는 작지만, 감사 로그 트랜잭션 처리와 계산형 상태(compliance_status)가 핵심 난이도다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/compliance.md`(SCR-COMP-001~004), 공통 SCR-COM-007/008
- API: `api_spec/compliance.md`(API-COMP-001~010) — 0절 설계 배경(준수상태 계산값, 감사로그 트랜잭션 동봉, ticket_link 재사용, 감사로그 조회 범위 제한) 필독
- DB: `database/compliance.md`(compliance_requirement/corrective_action) + `database/common.md`(ticket_link) + `database/auth.md`(audit_log)
- 역할: `security/authorization/compliance_officer.md`(COMPLIANCE_OFFICER, 신규 단일 역할)
- 연계 변경: `api_spec/change.md`(API-CHG-003 응답 `links`에 `COMPLIANCE_REQUIREMENT` 타입 이미 반영됨 — 문서만 갱신된 상태, 코드 반영은 이번 도메인에서)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- 신규 테이블 2개: `compliance_requirement`(requirement_key COMP-YYYY-####, name/basis NOT NULL, scope NULL, owner_id FK→app_user.id NULL), `corrective_action`(requirement_id FK NOT NULL, description NOT NULL, status DETECTED/IN_PROGRESS/RESOLVED DEFAULT 'DETECTED'). 공통 컬럼은 auth.md 2절 동일.
- (requirement_id, status) 인덱스 권장(미해결 건수 집계용).
- `ticket_link` CHECK 제약에 `COMPLIANCE_REQUIREMENT`(source_type) 추가 필요 여부 확인 — VULN 때처럼 CHECK가 source_type 목록을 제한하고 있다면 추가해주세요(target_type은 기존 'CHANGE' 그대로 사용).
- `auth.audit_log`의 `event_type` 컬럼은 이미 문자열 저장이라 값 추가에 스키마 변경 불필요(코드의 enum만 증분, BE 담당) — DB 쪽 스키마 변경 없음.
- 신규 역할 시드: `COMPLIANCE_OFFICER`. screen/screen_role: SCR-COMP-001~004 등록.
- 테스트 유저: COMPLIANCE_OFFICER 계정 1개 이상(예: co@itsm.local / Admin@1234).
- 시드 데이터: 요구사항 몇 건(책임자 지정/미지정 각 1건 이상, 시정조치 미해결 건 포함해 NON_COMPLIANT 케이스도 1건 이상 만들어주세요). 변경 연계까지 포함한 요구사항 1건 있으면 좋습니다(기존 CHG 시드 중 하나와 연계).

### BE (dev-backend) — `source/backend/`
- API-COMP-001~010(api_spec 기준). `compliance` 패키지 신설(problem/vulnerability 패키지 컨벤션 재사용).
- **공통 enum 증분**: `common.ticket.TicketType`에 `COMPLIANCE_REQUIREMENT` 추가(추가만). `auth.domain.EventType`에 `COMPLIANCE_REQ_CREATE`, `COMPLIANCE_REQ_UPDATE`, `COMPLIANCE_ACTION_STATUS_CHANGE` 추가(추가만).
- 등록(API-COMP-002): name·basis 필수(400). 등록 성공 시 **같은 트랜잭션**으로 `AuditLogService.record(COMPLIANCE_REQ_CREATE, ...)` 기록(0절: "로그 기록 실패 시 원본 작업도 롤백" — 기존 `AuditLogService.record()`가 이미 호출자 트랜잭션에 합류하는 방식이라 그대로 재사용하면 됩니다, `recordSeparately`는 사용하지 마세요).
- 수정(API-COMP-004): 수정 시에도 동일하게 `COMPLIANCE_REQ_UPDATE` 감사 로그 기록.
- 상세 조회(API-COMP-003): `complianceStatus`는 저장 컬럼이 아니라 **조회 시점에 계산**(해당 요구사항의 `corrective_action` 중 DETECTED/IN_PROGRESS 상태가 하나라도 있으면 NON_COMPLIANT, 없으면 COMPLIANT). 목록(API-COMP-001)도 동일 계산 적용(N+1 주의, 배치 조회 권장).
- 변경 요청 연계(API-COMP-005): `ticket_link`(source_type='COMPLIANCE_REQUIREMENT', target_type='CHANGE') 재사용, 존재하지 않는 변경 요청 400. **`change` 도메인 API-CHG-003 응답의 `links`에도 `COMPLIANCE_REQUIREMENT` 타입이 함께 노출되도록** change 쪽 링크 조회 로직이 이 신규 source_type을 이미 포함해서 반환하는지 확인(기존 링크 조회가 특정 타입만 필터링하고 있다면 최소 수정 필요, 아니면 손댈 필요 없음).
- 책임자 지정(API-COMP-006): 존재하지 않는 사용자 400.
- 시정조치 등록(API-COMP-007): description 필수(400), 상태 DETECTED로 생성.
- 시정조치 상태 전이(API-COMP-008): DETECTED→IN_PROGRESS→RESOLVED 순차만 허용(400), 전이마다 **`COMPLIANCE_ACTION_STATUS_CHANGE`** 감사 로그 기록(같은 트랜잭션).
- 컴플라이언스 감사 로그 조회(API-COMP-009): **기존 `AuditLogService.search()`는 단일 EventType만 받는데, 이 API는 `event_type LIKE 'COMPLIANCE_%'`(3개 이벤트 타입 전체)를 조회해야 합니다.** `AuditLogRepository`에 이벤트 타입 목록(Collection) 기반 조회 메서드를 신규 추가(기존 `search()` 시그니처는 건드리지 마세요). `requirementId` 파라미터는 `target`(예: 'COMPLIANCE_REQUIREMENT:{id}' 또는 'CORRECTIVE_ACTION:{id}') 매칭에 활용.
- 준수 현황(API-COMP-010): totalRequirements/compliantCount/nonCompliantCount/openCorrectiveActionCount/complianceRate(=compliantCount/totalRequirements, 0건이면 0). 데이터 없으면 빈 결과.
- RBAC: compliance_officer.md 기준 — COMPLIANCE_OFFICER만 전 API 접근, 그 외 403. **API-COMP-009(컴플라이언스 감사 로그)는 SYSTEM_ADMIN도 접근 불가**(설계 명시: 최소 권한 원칙, HR 케이스와 유사한 우선순위 — 다만 이쪽은 그냥 "이 역할에 없으면 403"이라 SYSTEM_ADMIN이 별도로 이 API에 접근 권한을 가진 것도 아니므로 자연히 403이 됨. ESM HR케이스처럼 "SYSTEM_ADMIN도 명시적으로 차단"까지는 아니고 단순 RBAC 매핑 부재로 이해하면 됩니다).
- JUnit 통합테스트: 준수상태 계산(미해결 시정조치 있음/없음), 시정조치 순차전이 위반 400, 감사로그 트랜잭션 동봉(등록 실패 시 로그도 없어야 함 등 필요시), 컴플라이언스 감사로그 조회(이벤트 타입 필터링), 변경 연계(존재하지 않는 변경 400, change 상세 응답에 COMPLIANCE_REQUIREMENT 링크 노출) 포함.
- 컨벤션대로 완료 보고 전 로컬 백엔드 재기동+curl 자체 검증 부탁드립니다.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 신규 UI 요소 없어 보임(기존 목록/상세 패턴, 배지 재사용). 확인만 부탁드립니다.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 신규 기능 모듈 `features/compliance/`(api.ts/types.ts/status.ts/format.ts + 4개 Page).
- SCR-COMP-001 목록(필터: 준수상태·책임자지정여부) / SCR-COMP-002 등록(이름·근거·적용범위) / SCR-COMP-003 상세(책임자 지정, 시정조치 등록·전이, 변경 연계(숫자 ID 입력, vulnerability/change의 기존 LinkCard 패턴 재사용), 감사 로그 목록) / SCR-COMP-004 준수 현황 대시보드.
- 역할 상수 `ROLE_COMPLIANCE_OFFICER` 추가, `navConfig.tsx`에 컴플라이언스 메뉴 그룹(COMPLIANCE_OFFICER 전용) 추가, `routes/index.tsx`에 라우팅 4개 추가.
- 변경 상세 화면(`ChangeDetailPage.tsx`)의 연계 항목 목록에 `COMPLIANCE_REQUIREMENT` 타입이 추가로 나타날 수 있는데(BE가 links에 포함시키면), 기존 렌더링(`l.type · l.targetKey`)이 이미 타입 무관하게 범용 표시라 별도 수정 없이 자동으로 커버될 것으로 예상됩니다 — 실제 확인만 해주세요(코드 변경 불필요 예상).

## 4. 진행 순서 · 의존성
1. DB(테이블 2개+역할/유저/시드, ticket_link CHECK 확인) → BE(enum 증분 2건 먼저 → compliance 패키지) → FE 연동. UI는 확인만.
2. 계약 단일 기준 `api_spec/compliance.md`. 모호점은 저에게 질문 → 제가 판단 못 하면 designer에게 확인.

## 5. 완료(테스트 통과) 기준
- BE: API-COMP-001~010 정상+오류(400/401/403/404), 준수상태 계산(목록/상세 일관), 시정조치 순차전이, 감사로그 트랜잭션 동봉 및 컴플라이언스 전용 조회(이벤트타입 필터), 변경 연계(change 상세에도 노출).
- FE: 등록→목록(필터)→상세(책임자·시정조치·연계·감사로그)→대시보드 E2E. COMPLIANCE_OFFICER RBAC 확인(타 역할 403+사이드바 미노출).
- `tester` 통합테스트 실패 0 → `feat(compliance): ...` 커밋/푸시.
- **테스트 산출물 경로 변경**: 이번부터 `docs/04_test/{domain}/{yyyyMMdd-HHmmss}/{scenario,result}/` 구조 사용(예: `docs/04_test/compliance/{timestamp}/scenario/compliance.md`). tester에게 안내 예정.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(compliance 패키지 + TicketType/EventType 증분 + AuditLogRepository 신규 조회 메서드) BE / `source/frontend/` 공통 UI·기능 FE.

## 7. 특이사항
- 감사 로그 기록은 **원본 작업과 같은 트랜잭션**이어야 하므로 `AuditLogService.record()`(REQUIRES_NEW 아닌 기본 전파)를 사용해야 합니다. `recordSeparately()`(REQUIRES_NEW, 로그인 실패 등 별도 보존용)를 쓰면 요구사항(0절)에 어긋나니 주의.
- `AuditLogService.search()`는 단일 `EventType`만 받는 기존 시그니처라 컴플라이언스 전용 다중 이벤트타입 조회에는 신규 리포지토리 메서드가 필요합니다(위 BE 항목 참고).
