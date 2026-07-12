# 개발 계획 — asset (IT 자산 관리 / CMDB, ITAM)

> 도메인: asset (ITAM) · 개발 순서 7/7(마지막) · 작성: dev-lead · 2026-07-10

## 1. 목표

IT 자산(HW/SW/클라우드) 등록·수정·조회·폐기, 생애주기 관리(5단계), 유형별 속성(EAV), 만료 추적(라이선스/보증/계약), 구성 항목(CI)·CMDB 의존 관계 관리, 영향 범위 조회, 티켓 연계, 자산 지표 대시보드를 구현한다. auth/incident/problem/change/knowledge 기반과 common(ticket_link) 재사용. 이 도메인이 마지막(7/7)이다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/asset.md`(SCR-ITAM-001~005), 공통 SCR-COM-007/008
- API: `api_spec/asset.md`(API-ITAM-001~012)
- DB: `database/asset.md`(asset/asset_attribute/asset_lifecycle_history/expiry_alert/configuration_item/ci_relation) + `database/common.md`(ticket_link)
- 역할: `security/authorization/asset_manager.md`(ASSET_MANAGER)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- ITAM 테이블: asset(asset_key AST-####, name/type NOT NULL, status 5단계 PLANNING/PROCUREMENT/OPERATION/MAINTENANCE/RETIREMENT, owner/location/purchase_date/cost, license_expiry/warranty_expiry/contract_expiry), asset_attribute(EAV, UNIQUE(asset_id, attr_key)), asset_lifecycle_history(append), expiry_alert(expiry_type LICENSE/WARRANTY/CONTRACT, due_date, notified), configuration_item(asset_id FK nullable), ci_relation(자기참조, source/target_ci_id FK, relation_type DEPENDS_ON/RUNS_ON/CONNECTS_TO, UNIQUE(source,target,relation_type), CHECK 자기참조 금지). 공통컬럼 동일 적용.
- ticket_link는 기존 공통 테이블 재사용(source_type='ASSET' 또는 'CI'). 신규 테이블 아님. TicketType enum에 ASSET/CI는 이미 존재 확인됨(common/ticket/TicketType.java) — BE 수정 불필요.
- screen/screen_role 증분: SCR-ITAM-001~005 등록 + ASSET_MANAGER 역할 매핑(asset_manager.md 기준). 신규 role 생성 없음(기존 auth 시드에 없다면 추가).
- 테스트 유저: ASSET_MANAGER 계정 시드(기존 유저 시드 규칙 동일 패턴, 예: am@itsm.local).
- 자산 유형별(HARDWARE/SOFTWARE/CLOUD) 표준 속성은 스키마 강제 없이 EAV 저장이므로 시드 데이터에 예시 속성 포함 권장(선택).

### BE (dev-backend) — `source/backend/`
- API-ITAM-001~012(api_spec). asset 패키지 신설(incident/problem/change/knowledge 패키지 컨벤션 재사용).
- 목록(API-ITAM-001): type/status/owner/expiringWithinDays/keyword 필터. expiryStatus(OK/EXPIRING/EXPIRED)는 license/warranty/contract 만료일 중 가장 임박한 값 기준 서버 계산.
- 등록(API-ITAM-002)/수정(API-ITAM-004): ASSET_MANAGER 전용(403). 이름·유형·유형별 필수 속성 누락 400. 수정 시 만료일이 과거면 400이 아니라 warning 응답(설계: "400(만료일 과거 시 warning 포함)" — 실제로는 200 + warning 필드로 해석, 계획에 명확히 하고 필요시 designer 확인).
- 생애주기 전이(API-ITAM-005): 5단계, 정의되지 않은 단계 400, asset_lifecycle_history 이력 기록. 순차 강제 여부는 설계 문서에 순서 제약이 명시되어 있지 않으므로 임의 전이 허용(단, 값 자체가 유효한 stage여야 함 — 이 부분 애매하면 저에게 질문).
- 폐기(API-ITAM-006): status=RETIREMENT 전이 + 이력 기록.
- 자산 티켓 연계(API-ITAM-007): ticket_link(source_type='ASSET'), ticketType(SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE) 존재 검증(모두 이미 구축된 도메인이므로 스텁 없이 바로 정상 구현), 없는 티켓 400.
- CI 목록/등록(API-ITAM-008/009): keyword/type 필터, CI 등록 시 assetId 선택적 연결.
- CI 관계 등록(API-ITAM-010): 자기참조 금지, 존재하지 않는 CI 400.
- CI 영향 범위 조회(API-ITAM-011): ci_relation 그래프 탐색(BFS/DFS), depth 포함, 관계 없으면 빈 목록.
- 자산 지표(API-ITAM-012): utilizationRate(정의 모호 — "OPERATION 상태 자산 비율" 등으로 근사, 산식을 계획에 명시), expiringCount, typeDistribution.
- RBAC는 asset_manager.md 기준(등록/수정/폐기/생애주기 전이만 ASSET_MANAGER 전용, 조회는 인증된 사용자 전반 — CI/영향범위/연계도 설계상 특정 역할 제한 없음, RBAC 표 재확인 후 구현).
- JUnit 통합테스트(기존 도메인 패턴 재사용).

### UI (dev-ui) — `source/frontend/` 공통 영역
- CI 관계 그래프 시각화가 신규 요소(의존 관계 노드 뷰). 라이브러리 신규 도입 여부는 dev-frontend와 상의, 과도한 신규 라이브러리 도입보다 목록/트리 형태의 단순 표현으로 최소 구현 권장.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- SCR-ITAM-001 자산 목록(필터: 유형·상태·소유자·만료임박·기간, 만료 배지)
- SCR-ITAM-002 자산 등록/수정(유형 선택 시 유형별 속성 동적 필드, 만료일 입력)
- SCR-ITAM-003 자산 상세(생애주기 전이, 폐기 확인 다이얼로그, 만료 정보 강조, 티켓 연계, 연결 CI 표시)
- SCR-ITAM-004 CI·CMDB 관계 뷰(CI 목록/등록, 관계 추가 폼, 영향 범위 패널 — 그래프는 단순 리스트/트리로 최소 구현)
- SCR-ITAM-005 자산 지표 대시보드(KPI 카드 + 유형 분포)
- 사이드바 메뉴 RBAC: ASSET_MANAGER(001~005). roles.ts에 ROLE_ASSET_MANAGER 추가. routes/index.tsx, navConfig.tsx에 SCR-ITAM-001~005 라우팅 추가(기존 패턴 재사용).
- 이 도메인이 마지막이므로, 완료 후 전체 7개 도메인 사이드바 메뉴가 설계된 역할별로 모두 노출되는지 간단히 확인 권장(회귀).

## 4. 진행 순서 · 의존성
1. DB(테이블·ASSET_MANAGER 유저 시드) → BE 연동 → FE 연동. UI 신규 최소(CI 그래프는 단순화).
2. 계약 단일 기준 api_spec/asset.md. utilizationRate 산식, 생애주기 전이 순서 제약 등 모호점은 저에게 질문 → 제가 판단 못 하면 designer에게 확인.

## 5. 완료(테스트 통과) 기준
- BE: API-ITAM-001~012 정상+오류(400/401/403/404), 생애주기 전이·폐기·만료 상태 계산·EAV 속성·CI 관계(자기참조 금지)·영향 범위 조회·티켓 연계(4종 모두)·지표.
- FE: 등록→목록→상세(생애주기·폐기·연계)→CI 관계 뷰(등록·영향범위)→지표 대시보드 E2E. ASSET_MANAGER RBAC 확인.
- tester 통합테스트 실패 0 → `feat(asset): ...` 커밋/푸시. (7/7 마지막 도메인 — 완료 시 Main에게 전체 완료 보고)

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(asset 패키지) BE / `source/frontend/` 공통 UI·기능 FE.

## 7. 특이사항
- ticket_link/TicketType(ASSET, CI)는 기존 공통 인프라 그대로 재사용, 신규 enum 추가 불필요.
- utilizationRate 산식, 자산 수정 시 "만료일 과거 warning" 응답 형태, 생애주기 전이 순서 제약 여부 등 설계 문서에 명시가 부족한 부분은 개발 중 확인 후 계획 문서에 확정 산식/규칙을 추가 기록한다(problem/change/knowledge 단계와 동일 원칙).

## 8. 개발 중 확정 사항 (dev-lead 승인 완료)

- **API-ITAM-004 자산 수정**: api_spec의 "400(만료일 과거 시 warning 포함)" 표기는 실패로 막는 것이 아니라 **200 정상 응답 + `warning` 필드**(과거 날짜 설정 시 문구, 아니면 null)로 구현. 이름/유형 등 필수값 누락만 순수 400.
- **API-ITAM-012 자산 지표 utilizationRate**: 설계 미명시로 **"전체 자산 중 status=OPERATION 비율(%)"**로 확정(대상 0건이면 0).
- **API-ITAM-005 생애주기 전이**: 설계 문서에 순차 강제 언급이 없어 **순서 불문 임의 전이 허용**(정의되지 않은 값만 400). 매 전이마다 asset_lifecycle_history 이력 기록.
- **API-ITAM-006 자산 폐기 응답**: api_spec은 `status: "RETIRED"`로 표기되어 있으나 이는 오탈자로 판단(자산 status 컬럼/enum은 PLANNING/PROCUREMENT/OPERATION/MAINTENANCE/**RETIREMENT** 5단계, DB CHECK 제약과 일치). 응답도 실제 persist된 값인 **"RETIREMENT"**로 반환.
- **API-ITAM-003 자산 상세 만료 정보**: 최초 구현 시 `expiry: {license, warranty, contract}`가 원시 날짜만 반환해 SCR-ITAM-003의 "임박/경과 강조" 요구를 FE에서 판정할 수 없었음 → 목록(API-ITAM-001)과 동일한 임박 판정 로직(만료 30일 이내 EXPIRING, 경과 EXPIRED)을 재사용해 **각 필드를 `{date, status}` 객체로 확장**(날짜 없으면 status도 null). dev-frontend에 안내 완료.

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/asset.md` 3절): `AssetListPage.tsx`(SCR-ITAM-001), `AssetFormPage.tsx`(002), `AssetDetailPage.tsx`(003), `CiRelationPage.tsx`(004), `AssetMetricsPage.tsx`(005).
- `features/asset/status.ts` — `t` 인자를 받도록 전환, 호출부(각 Page.tsx, `features/search/status.ts`의 ASSET 분기, `AppLayout.tsx`의 헤더 알림 자산 만료 항목은 이미 common phase에서 전환 완료라 재작업 불필요) 갱신.
- `format.ts` 확인 필수 — 만료 상태(EXPIRING/EXPIRED) 등 라벨이 섞여 있으면 그 라벨만 전환, ko-KR 날짜 포맷 자체는 유지.
- `useTranslation(["asset", "common"])` 사용. `locales/{ko,en}/asset.json`(현재 `{}` 스캐폴딩) 단독 소유, 직접 채운다.
- 자산 유형별(속성 다름)·생애주기 상태(PLANNING/PROCUREMENT/OPERATION/MAINTENANCE/RETIREMENT)·CI 관계 유형 등 열거형 라벨 전부 점검(change phase 원시값 노출 패턴 재확인).
- 값이 비어있는 필드(만료일 없음 등) 조회 시 원시 키 노출 없는지 falsy 가드 확인(problem phase 패턴).

### 완료 기준
- English 전환 시 목록/등록·수정/상세/CI·CMDB 관계 뷰/지표 대시보드 전체 텍스트(상태·유형·생애주기·만료 라벨 포함) 영어 전환.
- 자산 등록/수정(만료일 과거 warning)·생애주기 전이·CI 의존 관계·통합검색 연동 등 기존 기능 회귀 없음(텍스트만 치환).
