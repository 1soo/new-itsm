# 통합 테스트 결과 — asset (ITAM, 7/7 마지막 도메인) (20260710-064414)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 계정: am@itsm.local(ASSET_MANAGER) / agent@itsm.local(SERVICE_DESK_AGENT, 403+비-AM 조회 검증) / im/pm/cm(4종 티켓 연계 검증)
> 범위: API-ITAM-001~012 정상+오류, EAV 속성, 생애주기 이력, 만료 임박 계산, CI/CMDB 관계(자기참조 금지)+영향범위(BFS), 4종 티켓 연계 + 7개 도메인 전체 회귀

## 요약

- 총 **50건** · **성공 49 · 실패 1**
- 실패 1건: **TC-REG-002**(자산→티켓 연계가 자산 쪽에서만 조회되고, 인시던트/문제/변경/서비스요청 등 **티켓 쪽 상세에서는 조회되지 않음** — REQ-ITAM-006 Event-driven 인수기준 위반)

## 상세 — 빌드

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-BUILD-001 (BE gradlew clean test) | PASS | `BUILD SUCCESSFUL`(5 tasks executed, 7개 도메인 전체), asset 패키지 JUnit 포함 |
| TC-BUILD-002 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1866 modules, TS 오류 없음) |

## 상세 — 인증/인가

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-AUTH-001 (미인증 GET assets) | PASS | 401 UNAUTHENTICATED |
| TC-ITAM-RBAC-001 (agent POST assets) | PASS | 403 ACCESS_DENIED |
| TC-ITAM-RBAC-002 (agent PATCH retire) | PASS | 403 ACCESS_DENIED |
| TC-ITAM-RBAC-003 (agent PATCH lifecycle) | PASS | 403 ACCESS_DENIED |
| TC-ITAM-RBAC-004 (agent CI 등록/조회, 자산 연계) | PASS | agent가 CI 등록(201)·GET /assets(200, API 레벨)·자산 연계 모두 정상 수행(403 아님, CI/연계는 역할 제한 없음 확인). 단 FE 화면(/assets)은 asset_manager.md 설계대로 ASSET_MANAGER 전용이라 agent 직접 접근 시 /403(정상, 화면-API 권한 분리 설계) |

## 상세 — 자산 등록·수정

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-001 (정상 등록) | PASS | 201, `assetKey=AST-0002`, `status=PLANNING` |
| TC-ITAM-002 (name 누락) | PASS | 400 VALIDATION_ERROR |
| TC-ITAM-003 (type 누락) | PASS | 400 VALIDATION_ERROR |
| TC-ITAM-004 (목록 조회) | PASS | 200, `{content,page,size,totalElements}` |
| TC-ITAM-005 (상세 구조) | PASS | 200, `expiry.{license,warranty,contract}` 각 `{date,status}` 객체 확인(§8 확정 반영) |
| TC-ITAM-006 (없는 id 상세) | PASS | 404 ASSET_NOT_FOUND |
| TC-ITAM-007 (수정) | PASS | 200 |
| TC-ITAM-008 (만료일 과거 수정) | PASS | 200 + `"warning":"만료일이 과거 날짜로 설정되었습니다."`(§8 확정: 400 아님) |
| TC-ITAM-009 (없는 id 수정) | PASS | 404 ASSET_NOT_FOUND |

## 상세 — 생애주기 전이·폐기

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-010 (임의 순서 전이) | PASS | PLANNING→MAINTENANCE 200(순서 불문, §8 확정), lifecycleHistory에 기록 |
| TC-ITAM-011 (정의되지 않은 단계) | PASS | 400 VALIDATION_ERROR |
| TC-ITAM-012 (없는 id 전이) | PASS | 404 ASSET_NOT_FOUND |
| TC-ITAM-013 (폐기) | PASS | 200, `status="RETIREMENT"`(§8 확정: 계약 RETIRED 아님) |
| TC-ITAM-014 (없는 id 폐기) | PASS | 404 ASSET_NOT_FOUND |

## 상세 — 만료 추적

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-015 (임박 EXPIRING) | PASS | licenseExpiry 10일 후 → `expiryStatus/status="EXPIRING"` |
| TC-ITAM-016 (경과 EXPIRED) | PASS | contractExpiry 과거 → `expiryStatus/status="EXPIRED"` |
| TC-ITAM-017 (만료일 없음) | PASS | 목록 `expiryStatus="OK"`, 상세 해당 필드 빈 값(무영향) |

## 상세 — 티켓 연계 (4종)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-018 (asset→SERVICE_REQUEST) | PASS | 200, `{assetId,ticketId}` |
| TC-ITAM-019 (asset→INCIDENT) | PASS | 200 |
| TC-ITAM-020 (asset→PROBLEM) | PASS | 200 |
| TC-ITAM-021 (asset→CHANGE) | PASS | 200 |
| TC-ITAM-022 (없는 티켓 연계) | PASS | 400 LINK_TARGET_NOT_FOUND |
| TC-ITAM-023 (자산 상세 linkedTickets 반영) | PASS | 자산(6) 상세 `linkedTickets`에 SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE 4종 모두 ticketKey 형식으로 반영 확인(자산 쪽 조회는 정상) |

## 상세 — CI·CMDB 관계

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-024 (CI 등록) | PASS | 201(agent 권한으로도 성공, RBAC-004와 연계 확인) |
| TC-ITAM-025 (CI 목록) | PASS | 200, 생성분 포함 |
| TC-ITAM-026 (CI 관계 등록) | PASS | 200, DEPENDS_ON 관계 저장 |
| TC-ITAM-027 (없는 대상 CI) | PASS | 400 LINK_TARGET_NOT_FOUND |
| TC-ITAM-028 (자기참조) | PASS | 400 CI_SELF_RELATION_NOT_ALLOWED |
| TC-ITAM-029 (자산 linkedCis 반영) | PASS | 자산(6) 상세 `linkedCis`에 연결 CI 반영 |

## 상세 — CI 영향 범위 (BFS)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-030 (의존 관계 영향 범위) | PASS | 200, `[{ciId,name,relationType,depth}]` |
| TC-ITAM-031 (관계 없음) | PASS | 200, `[]` |
| TC-ITAM-032 (다단계 depth) | PASS | CI3→CI4→CI5 등록 후 impact 조회 시 depth 1(CI4)/depth 2(CI5) 정확히 반환(BFS 확인) |

## 상세 — 자산 지표

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ITAM-033 (지표 조회) | PASS | `{"utilizationRate":25.0,"expiringCount":4,"typeDistribution":{"HARDWARE":2,"SOFTWARE":1,"CLOUD":1}}`(OPERATION 1/4=25%, §8 확정 산식) |
| TC-ITAM-034 (데이터 없는 기간) | PASS | 전 항목 0/빈 분포 |

## 상세 — FE E2E

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-E2E-001 (자산 등록) | PASS | 유형 선택 시 "유형별 속성" 동적 섹션(속성 추가 버튼) 노출, 등록 성공 후 상세(AST-0005) 이동 |
| TC-E2E-002 (자산 목록) | PASS | 필터, 만료 배지("임박"/"경과") 정상 표시 |
| TC-E2E-003 (자산 상세) | PASS | 생애주기 전이 버튼(순서 불문 전체 노출)·전이 이력 반영·폐기 확인 다이얼로그("폐기 후에는 되돌릴 수 없습니다")·폐기 후 상태 배지 갱신 |
| TC-E2E-004 (CI·CMDB 관계 뷰) | PASS | CI 선택 시 관계 추가 폼 노출, 영향 범위 패널에 depth 1/2 항목 정상 표시 |
| TC-E2E-005 (자산 지표 대시보드) | PASS | KPI 카드(활용률 20%, 만료 임박 4건) + 유형 분포 차트(HW3/SW1/CLOUD1) |
| TC-E2E-006 (agent RBAC) | PASS | `/assets` 직접 접근 시 `/403`(asset_manager.md 화면 전용 설계와 일치), 사이드바에 자산 메뉴 비노출 |

## 상세 — 7개 도메인 전체 회귀

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-REG-001 (SRM↔ASSET 연계) | PASS(단, REG-002 결함과 연동, 자산 쪽 조회만 정상) | TC-ITAM-018/023과 동일 근거 |
| **TC-REG-002 (티켓 쪽에서 ASSET 링크 조회)** | **FAIL** | 자산→인시던트/문제/변경/서비스요청 연계(TC-ITAM-018~021) 후 **각 티켓 상세를 조회하면 ASSET 링크가 전혀 노출되지 않음**. incident(23)/problem(27)/change(18)/service-request(21) 상세 응답에 연결된 자산 정보 없음(문제/서비스요청은 애초에 자산 링크 필드 자체가 없고, 인시던트는 계약상 `links[].type`에 `ASSET`이 명시되어 있음에도 빈 배열 반환) |
| TC-REG-003 (역할별 사이드바 메뉴 회귀) | PASS | CHANGE_MANAGER(cm) 로그인 시 변경 메뉴만 정상 노출(자산 메뉴 신설로 인한 오염 없음), SERVICE_DESK_AGENT는 자산 메뉴 비노출 확인(스팟체크) |

## 실패 항목 분석

- **TC-REG-002 (자산-티켓 연계 단방향성)**: `AssetService.linkAsset()`(API-ITAM-007)가 `saveLinkOnce(TicketType.ASSET, assetId, ticketType, ticketId)`만 호출하고 반대 방향(`ticketType, ticketId → TicketType.ASSET, assetId`)은 저장하지 않음(`source/backend/src/main/java/com/itsm/asset/application/AssetService.java` 약 191~195행). 그 결과:
  - **인시던트**: `docs/02_plan/api_spec/incident.md`의 상세 응답 계약이 이미 `"links": [ { "type": "PROBLEM|ASSET", ... } ]`로 ASSET을 명시하고 있고, `IncidentService.detail()`도 `ticketLinkRepository.findBySourceTypeAndSourceId(TT, id)`로 조회하므로 **역방향 링크만 추가되면 코드 변경 없이 즉시 노출 가능**한 상태. 하지만 역방향 저장이 없어 빈 배열 반환.
  - **문제/변경/서비스요청**: 각 도메인 상세 응답 계약 자체에 ASSET 링크를 표현할 필드가 없음(문제는 `linkedIncidents/linkedChanges`만 존재, 변경의 `links[].type`은 `INCIDENT|PROBLEM`만 정의, 서비스요청은 `linkedArticles`만 존재) — 계약 확장이 필요.
  - 근거: `docs/01_analyze/prd/asset.md` REQ-ITAM-006 "(Event-driven) WHEN 사용자가 자산/CI를 티켓에 연결하면, 시스템은 링크를 저장하고 **티켓에서 조회할 수 있게** 해야 한다", `docs/01_analyze/feature/asset.md` FEAT-ITAM-006 동일 인수기준.
  - 제안: (1) BE에서 `linkAsset` 시 역방향 `saveLinkOnce`도 함께 호출(인시던트는 계약 그대로 바로 노출), (2) problem/change/service-request 상세 응답에 자산 링크 노출 필드 추가 여부는 계약 변경이 필요해 dev-lead/designer 확인 필요. 담당은 BE(및 필요 시 계약 변경 협의)로 판단됩니다.
  - 참고: 자산→CI 관계(`linkedCis`, TC-ITAM-029)와 자산 자신의 `linkedTickets`(TC-ITAM-023)는 정상 동작하므로, 자산을 "허브"로 조회하는 흐름 자체는 문제없음. 회귀는 "티켓을 허브로 자산을 조회"하는 반대 방향에 한정됨.

## 결론

- asset 도메인 핵심 기능(등록·수정(만료일 과거 warning)·생애주기 전이(순서 불문)·폐기(RETIREMENT)·만료 추적(임박/경과)·EAV 속성·CI 등록/관계(자기참조 금지)·영향 범위(BFS, depth)·지표(utilizationRate/expiringCount/typeDistribution)·RBAC(등록/수정/폐기/전이는 AM 전용, CI/연계는 개방)) 및 FE 6화면 **정상 동작**. §8에 기록된 5개 확정 사항 모두 코드 동작과 일치 확인.
- **잔여 실패 1건**: 자산→티켓 연계가 단방향이라 티켓 쪽 상세에서 연결된 자산이 조회되지 않음(TC-REG-002, REQ-ITAM-006 인수기준 미충족). 7/7 마지막 도메인 완료 전 수정 필요 여부는 dev-lead 판단 요청.
