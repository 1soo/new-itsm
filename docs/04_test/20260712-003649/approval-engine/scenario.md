# 통합 테스트 시나리오 — approval-engine (Stage 5: ASSET + VULNERABILITY 승인 게이트 연동)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- Stage 1~4 산출물(공용 승인 엔진, SCR-ADMIN-007/008, SCR-COM-014) 정상 동작 전제
- backend(:8080)에 Stage 5 코드(`AssetService`/`VulnerabilityService`↔`ApprovalGateService.checkGate`, `AssetApprovalTicketSummaryProvider`, `VulnerabilityApprovalTicketSummaryProvider`) 반영 확인
- 기존 규칙 재사용: `approval_process.id=5`(domain=ASSET, tier=3, 요청자 스코프=ASSET_MANAGER, 1차 AND[ASSET_MANAGER]), `id=6`(domain=VULNERABILITY, tier=3, 요청자 스코프=VULNERABILITY_MANAGER, 1차 AND[VULNERABILITY_MANAGER])
- 병렬 에이전트 세션 충돌 회피를 위한 전용 계정(비밀번호 `Test@1234`, `tester-admin@itsm.local`로 생성): `tester_ast5_req@itsm.local`/`tester_ast5_apv@itsm.local`(ASSET_MANAGER, 요청자·승인자 분리), `tester_vuln5_req@itsm.local`/`tester_vuln5_apv@itsm.local`(VULNERABILITY_MANAGER, 요청자·승인자 분리)
- Stage4(API-INC-009 우회 사례) 교훈: ASSET은 API-ITAM-005(targetStage=RETIREMENT)와 API-ITAM-006(폐기 전용) 두 엔드포인트 모두 게이트가 실제로 걸리는지 각각 직접 호출로 확인한다.

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/asset.md, @docs/02_plan/api_spec/vulnerability.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-ASTGATE-001 · 매칭 규칙 있음 — API-ITAM-005(lifecycle, targetStage=RETIREMENT) 게이트 차단
- 근거: @docs/02_plan/api_spec/asset.md (API-ITAM-005 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: tester_ast5_req로 신규 자산 등록(status=PLANNING)
- 절차: 1) `PATCH .../lifecycle {targetStage:"RETIREMENT"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환. API-COM-004 상세 조회 시 인스턴스 IN_PROGRESS. 자산 상세(API-ITAM-003) `approval` 필드 동일 반영, 자산 상태는 RETIREMENT로 바뀌지 않음

### TC-ASTGATE-002 · API-ITAM-006(retire 전용 엔드포인트) 게이트 차단 — 우회 경로 직접 확인
- 근거: @docs/02_plan/api_spec/asset.md (API-ITAM-006 409), Stage4 API-INC-009 우회 사례 재발 방지
- 전제: TC-ASTGATE-001과 별개의 신규 자산(status=PLANNING)
- 절차: 1) `PATCH .../retire` 직접 호출(lifecycle PATCH 경유 없이)
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 생성 확인, 자산 상태는 RETIREMENT로 바뀌지 않음

### TC-ASTGATE-003 · 승인 후 재시도 허용(lifecycle·retire 각각)
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005), 상동
- 전제: TC-ASTGATE-001, TC-ASTGATE-002 인스턴스
- 절차: 1) tester_ast5_apv(ASSET_MANAGER)로 각 인스턴스 APPROVE 2) TC-ASTGATE-001 자산은 `PATCH .../lifecycle{RETIREMENT}` 재시도, TC-ASTGATE-002 자산은 `PATCH .../retire` 재시도
- 기대 결과: 둘 다 200 성공, `status="RETIREMENT"`, 상세 `approval.status="APPROVED"`

### TC-ASTGATE-004 · 매칭 규칙 없음 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: SYSTEM_ADMIN(ASSET_MANAGER 역할 없음, 요청자 스코프 불일치)이 자산 등록
- 절차: 1) `PATCH .../retire` 호출
- 기대 결과: 게이트 없이 즉시 200

### TC-VULNGATE-001 · 매칭 규칙 있음 — PRIORITIZATION→REMEDIATION 전이 시 게이트 차단(담당자 배정 후)
- 근거: @docs/02_plan/api_spec/vulnerability.md (API-VULN-004 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: tester_vuln5_req로 취약점 등록 후 DISCOVERY→ASSESSMENT→PRIORITIZATION 순차 전이, `POST .../assign`으로 담당자 배정 완료(담당자 미배정 409와는 별개 케이스임을 분리 확인)
- 절차: 1) `PATCH .../status {targetStatus:"REMEDIATION"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 확인(API-COM-004), 취약점 상세(API-VULN-003) `approval` 필드 동일 반영

### TC-VULNGATE-002 · 담당자 미배정 409는 게이트보다 먼저 체크(기존 로직 유지 확인)
- 근거: @docs/02_plan/api_spec/vulnerability.md (API-VULN-004, ASSIGNEE_REQUIRED_FOR_REMEDIATION)
- 전제: 담당자 미배정 상태로 PRIORITIZATION까지 전이한 취약점
- 절차: 1) `PATCH .../status {targetStatus:"REMEDIATION"}`
- 기대 결과: 409(담당자 미배정 사유, `APPROVAL_PENDING`이 아닌 기존 오류코드) — 두 체크가 공존하며 담당자 체크가 선행됨을 확인

### TC-VULNGATE-003 · 승인 후 재시도 허용
- 근거: 상동
- 전제: TC-VULNGATE-001 인스턴스
- 절차: 1) tester_vuln5_apv(VULNERABILITY_MANAGER)로 APPROVE 2) `PATCH .../status {targetStatus:"REMEDIATION"}` 재시도
- 기대 결과: 200 성공, `status="REMEDIATION"`, 상세 `approval.status="APPROVED"`

### TC-VULNGATE-004 · 매칭 규칙 없음 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: SYSTEM_ADMIN(VULNERABILITY_MANAGER 역할 없음)이 취약점 등록, 담당자 배정 후 PRIORITIZATION까지 전이
- 절차: 1) `PATCH .../status {targetStatus:"REMEDIATION"}`
- 기대 결과: 게이트 없이 즉시 200

### TC-ASTUI-001 · 자산 상세 화면 — 폐기 관련 버튼 disabled+tooltip(승인 대기 중)
- 근거: Stage2/4 UI 패턴 선례, `source/frontend/src/features/asset/AssetDetailPage.tsx`
- 전제: 승인 대기 중(인스턴스 IN_PROGRESS)인 자산 상세 진입
- 절차: 1) playwright 새 컨텍스트로 tester_ast5_req 로그인 2) 해당 자산 상세 진입
- 기대 결과: RETIREMENT 관련 전이 버튼(들)이 `disabled` + 승인 대기 안내 tooltip 노출, 승인 현황 패널에 "1차·대기중" 표시

### TC-ASTUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: Stage2/4 UI 패턴 선례
- 절차: 1) 매칭 규칙이 있는 신규 자산에서 폐기 관련 버튼 최초 클릭(409 유발) 2) 승인 패널 확인
- 기대 결과: 새로고침 없이 토스트 노출 + 승인 패널이 즉시 "1차·대기중"으로 반영

### TC-VULNUI-001 · 취약점 상세 화면 — REMEDIATION 전이 버튼 disabled+tooltip(승인 대기 중)
- 근거: 상동, `source/frontend/src/features/vulnerability/VulnerabilityDetailPage.tsx`
- 전제: 승인 대기 중(담당자 배정 완료, 인스턴스 IN_PROGRESS)인 취약점 상세 진입
- 절차: 1) playwright 새 컨텍스트로 tester_vuln5_req 로그인 2) 해당 취약점 상세 진입
- 기대 결과: REMEDIATION 전이 버튼 `disabled` + tooltip 노출, 승인 현황 패널 "1차·대기중" 표시

### TC-VULNUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: 상동
- 절차: 1) 담당자 배정 완료·매칭 규칙 있는 신규 취약점에서 REMEDIATION 전이 최초 시도(409 유발) 2) 승인 패널 확인
- 기대 결과: 새로고침 없이 토스트 노출 + 승인 패널 즉시 "1차·대기중" 반영

### TC-COM014-AST-001 · SCR-COM-014 승인 대기함 — ASSET ticketKey(AST-{id}) 노출
- 근거: `AssetApprovalTicketSummaryProvider.java`, @docs/02_plan/api_spec/common.md (API-COM-003)
- 전제: 진행 중인 ASSET 승인 인스턴스 존재
- 절차: 1) playwright로 tester_ast5_apv(ASSET_MANAGER) 로그인 2) `/approvals` 진입
- 기대 결과: "자산" 유형 배지 + assetKey(AST-{id}) 형태 ticketKey, 요청자명(tester_ast5_req) 정확히 노출

### TC-COM014-VULN-001 · SCR-COM-014 승인 대기함 — VULNERABILITY ticketKey(VULN-{id}) 노출
- 근거: `VulnerabilityApprovalTicketSummaryProvider.java`, 상동
- 전제: 진행 중인 VULNERABILITY 승인 인스턴스 존재
- 절차: 1) playwright로 tester_vuln5_apv(VULNERABILITY_MANAGER) 로그인 2) `/approvals` 진입
- 기대 결과: "취약점" 유형 배지 + ticketKey(VULN-YYYY-####), 요청자명(tester_vuln5_req) 정확히 노출

### TC-ASTREG-001 · ASSET 목록/등록/수정/CI/지표 회귀
- 근거: @docs/02_plan/api_spec/asset.md (API-ITAM-001/002/004/008/012)
- 절차: 1) 목록 조회 2) 신규 등록 3) 수정 4) CI 목록 조회 5) 지표 조회
- 기대 결과: 전부 정상 응답(200/201), 게이트 도입 전과 동일하게 동작(회귀 없음)

### TC-VULNREG-001 · VULNERABILITY 목록/등록/리스크스코어/개선조치/지표 회귀
- 근거: @docs/02_plan/api_spec/vulnerability.md (API-VULN-001/002/005/007/010)
- 절차: 1) 목록 조회 2) 신규 등록 3) 리스크 스코어 산정 4) 개선 조치 등록 5) 지표 조회
- 기대 결과: 전부 정상 응답(200/201), 게이트 도입 전과 동일하게 동작(회귀 없음)

### TC-CROSSREG-001 · 타 도메인 승인 흐름 소스 무변경 확인(회귀 스팟)
- 근거: Stage1~4 선례
- 절차: 1) `git status`/`git diff` 기준 `common/approval/**`, `incident/**`, `problem/**`, `change/**`, `srm/**`, `knowledge/**` 소스가 Stage5 변경분에 포함되지 않았는지 확인
- 기대 결과: 공용 엔진·타 도메인 서비스 미변경 확인(회귀 없음)
