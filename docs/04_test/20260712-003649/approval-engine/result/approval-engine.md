---
date: 20260712-003649
domain: approval-engine
result: pass
keywords: [ASSET 승인 게이트, VULNERABILITY 승인 게이트, lifecycle/retire 엔드포인트 우회 재발방지]
---

# 통합 테스트 결과 — approval-engine (Stage 5: ASSET + VULNERABILITY 승인 게이트 연동) (20260712-003649)

## 요약
- 총 18건 · 성공 18 · 실패 0 ✅ **전 항목 통과**
- Stage4 API-INC-009 우회 사례에 대한 재발 방지 확인: ASSET의 두 엔드포인트(API-ITAM-005 lifecycle, API-ITAM-006 retire) 모두 각각 직접 호출로 게이트 차단 확인 완료(우회 없음)

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test`, `npm run build` 모두 오류 없이 성공 | |
| TC-ASTGATE-001 | PASS | 매칭 규칙 있음(id=5, ASSET_MANAGER 스코프, AND 1역할 ASSET_MANAGER) 상태에서 `PATCH .../lifecycle {targetStage:"RETIREMENT"}` 시도 → 409(`APPROVAL_PENDING`) + `approvalRequestId=35`, 인스턴스 IN_PROGRESS 확인(API-COM-004), 자산 상태 PLANNING 유지(AST-0002) | |
| TC-ASTGATE-002 | PASS | 별개 자산(AST-0003)에 `PATCH .../retire` **직접 호출**(lifecycle 경유 없이) → 409 + `approvalRequestId=36`(신규 인스턴스), 자산 상태 PLANNING 유지 — Stage4 API-INC-009 우회 사례와 달리 이번엔 두 엔드포인트 모두 정상 차단 확인 | |
| TC-ASTGATE-003 | PASS | tester_ast5_apv로 두 인스턴스(35, 36) 각각 APPROVE → AST-0002는 `lifecycle{RETIREMENT}` 재시도 200, AST-0003은 `retire` 재시도 200, 둘 다 `status="RETIREMENT"`, `approval.status="APPROVED"` | |
| TC-ASTGATE-004 | PASS | SYSTEM_ADMIN(ASSET_MANAGER 역할 없음)이 자산 등록 후 `retire` 호출 → 게이트 없이 즉시 200(AST-0004) | SYSTEM_ADMIN은 전 도메인 API 접근 허용 정책(f6e3745 커밋)으로 Asset Manager 전용 API도 호출 가능, 요청자 스코프만 불일치 |
| TC-VULNGATE-001 | PASS | tester_vuln5_req로 취약점 등록 후 DISCOVERY→ASSESSMENT→PRIORITIZATION 전이, 담당자 배정 완료 후 `PATCH .../status{REMEDIATION}` → 409(`APPROVAL_PENDING`) + `approvalRequestId=39`, 인스턴스 IN_PROGRESS 확인(VULN-2026-0006) | |
| TC-VULNGATE-002 | PASS | 담당자 미배정 상태로 PRIORITIZATION까지 전이한 취약점(VULN-2026-0007)에 REMEDIATION 시도 → 409(`ASSIGNEE_REQUIRED_FOR_REMEDIATION`, `APPROVAL_PENDING`이 아님) — 담당자 체크가 게이트보다 선행되어 두 체크가 공존함을 확인 | |
| TC-VULNGATE-003 | PASS | tester_vuln5_apv APPROVE(200, APPROVED) → REMEDIATION 재시도 200 성공, `status="REMEDIATION"`, `approval.status="APPROVED"` | |
| TC-VULNGATE-004 | PASS | SYSTEM_ADMIN(VULNERABILITY_MANAGER 역할 없음)이 취약점 등록·전이·배정 후 REMEDIATION 시도 → 게이트 없이 즉시 200(VULN-2026-0008) | |
| TC-ASTUI-001 | PASS | tester_ast5_req로 승인 대기 중(AST-0005) 상세 진입 시 "폐기" 버튼 `disabled` + `title="승인 완료 전에는 폐기할 수 없습니다"`, 승인 현황 패널 "1차·대기중" 확인 | |
| TC-ASTUI-002 | PASS | 신규 자산(AST-0006)에서 "폐기" 클릭 → 확인 다이얼로그("폐기하시겠습니까?") → 확인 클릭 시 최초 409 유발 → 새로고침 없이 토스트 + "폐기" 버튼 disabled 전환 + 승인 패널 "1차·대기중" 즉시 반영 | |
| TC-VULNUI-001 | PASS | tester_vuln5_req로 승인 대기 중(VULN-2026-0009) 상세 진입 시 "개선" 버튼 `disabled` + `title="승인 완료 전에는 개선 단계로 전이할 수 없습니다"`, 승인 현황 패널 "1차·대기중" 확인 | |
| TC-VULNUI-002 | PASS | 담당자 배정 완료·PRIORITIZATION 상태의 신규 취약점(VULN-2026-0010)에서 "개선" 최초 클릭 시 409 유발 → 새로고침 없이 토스트 + 버튼 disabled 전환 + 승인 패널 즉시 "1차·대기중" 반영 | |
| TC-COM014-AST-001 | PASS | tester_ast5_apv로 `/approvals` 진입 시 "자산" 유형 배지 + `AST-0006`/`AST-0005` ticketKey, 요청자명("Stage5 Asset Requester") 정확히 노출 | |
| TC-COM014-VULN-001 | PASS | tester_vuln5_apv로 `/approvals` 진입 시 "취약점" 유형 배지 + `VULN-2026-0010`/`VULN-2026-0009` ticketKey, 요청자명("Stage5 Vuln Requester") 정확히 노출 | |
| TC-ASTREG-001 | PASS | 목록 200, 등록 201, 수정 200, CI 목록 200, 지표 조회 200 — 게이트 도입 전과 동일하게 동작 | |
| TC-VULNREG-001 | PASS | 목록 200, 등록 201, 리스크 스코어 산정 200(6=MEDIUM(2)×HIGH(3)), 개선 조치 등록 201, 지표 조회 200 | |
| TC-CROSSREG-001 | PASS | `git status` 기준 `common/approval/**`, `incident/**`, `problem/**`, `change/**`, `srm/**`, `knowledge/**` 소스가 Stage5 변경분에 전혀 포함되지 않음 확인(공용 엔진·타 도메인 서비스 미변경, 회귀 없음) | |

## 실패 항목 분석

없음(전 항목 통과).

## 테스트 환경 조성 참고

- 병렬 에이전트 세션 충돌 회피를 위해 전용 계정(비밀번호 `Test@1234`)을 API-AUTH-007로 신규 생성해 사용: `tester_ast5_req@itsm.local`/`tester_ast5_apv@itsm.local`(ASSET_MANAGER, 요청자·승인자 분리), `tester_vuln5_req@itsm.local`/`tester_vuln5_apv@itsm.local`(VULNERABILITY_MANAGER, 요청자·승인자 분리).
- 기존 규칙(id=5 ASSET, id=6 VULNERABILITY)은 변경 없이 그대로 재사용했다.
- 테스트용 자산(id 2~7, AST-0002~0007)·취약점(id 6~11, VULN-2026-0006~0011)이 생성되어 남아있다. dev-lead 확인 후 정리 필요 여부 알려달라.
