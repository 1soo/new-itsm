---
date: 20260715-142838
domain: service-request
result: pass
keywords: [담당자 후보 배정, 담당 큐 Select, 라우팅 담당자 미배정 가드, assigneeId 결함 수정]
---

# 통합 테스트 결과 — service-request (20260715-142838)

## 요약
- 총 14건 · 성공 14 · 실패 0
- TC-SRM-009는 최초 실행 시 FAIL, developer-BE 수정(assigneeId 필드 추가) 후 재테스트하여 PASS로 전환(아래 "재테스트" 참조)

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-001 | PASS | 백엔드 `./gradlew build` BUILD SUCCESSFUL, 프론트엔드 `npm run build`(tsc+vite) 성공(2136 modules) | - |
| TC-SRM-002 | PASS | po@itsm.local로 신규 카탈로그 항목 생성 시 "담당자 역할" select에 API-AUTH-030 응답(전체 16개 역할) 노출, "승인자" 선택 후 저장 → 재조회 시 select에 "승인자" 유지 확인 | playwright snapshot |
| TC-SRM-003 | PASS | '노트북 신청' 편집 진입 시 담당 큐="IT 서비스", 담당자 역할="서비스 데스크 상담원"으로 정확히 프리필(구 결함 재발 없음) | playwright snapshot |
| TC-SRM-004 | PASS | 신규 항목 폼 기본값 "미분류"/"선택 안 함" 확인, 미선택 상태로 저장 성공(큐 미지정) | playwright snapshot |
| TC-SRM-005 | PASS | `GET /service-requests/{id}/assignee-candidates`(노트북 신청, role=SERVICE_DESK_AGENT) → 200, `[{"id":5,"name":"서비스데스크 담당자"}]` | curl |
| TC-SRM-006 | PASS | 동일 API(비밀번호 초기화, role 미지정) → 200, `[]` | curl |
| TC-SRM-007 | PASS | 큐 화면에서 "배정" 클릭 → 팝업에 후보(서비스데스크 담당자) 노출, 이름 클릭으로 배정 성공(`POST /assign` 200) | playwright snapshot |
| TC-SRM-008 | PASS | 담당자 역할 미지정 요청(SRM-2026-0013)에서 "배정" 클릭 → "지정된 담당자 역할이 없습니다" + "나에게 배정" 버튼만 노출, 클릭으로 본인 배정 성공 | playwright snapshot |
| TC-SRM-009 | FAIL → **PASS**(재테스트) | 최초 실행 시 실패(아래 "실패 항목 분석 — 최초 실행" 참조). developer-BE 수정(`RequestSummaryResponse.assigneeId` 추가 + `toSummary()` 반영) 후 재테스트: `GET /service-requests?scope=all` 응답에 `assigneeId` 정상 포함, agent@itsm.local로 SRM-2026-0012(본인 배정, VALIDATED) 행에서 "배정" 버튼 사라짐 확인. SRM-2026-0009(타인 배정, SUBMITTED)는 계속 "배정" 노출되어 회귀 없음 확인 | playwright evaluate(재테스트) |
| TC-SRM-010 | PASS | 기존 ROUTED 상태·미배정 요청들(SRM-2026-0005~0007)에서 "배정" 버튼이 정상적으로 숨겨짐(상태 조건 단독 확인) | playwright snapshot |
| TC-SRM-011 | PASS | VALIDATED·미배정 요청에 `PATCH .../status {targetStatus:ROUTED}` → 409, `code:"ASSIGNEE_REQUIRED_FOR_ROUTING"` | curl |
| TC-SRM-012 | PASS | 담당자 배정 후 동일 전이 재시도 → 200, `status:"ROUTED"` | curl |
| TC-SRM-013 | PASS | 담당자 미배정 상태에서 상세 화면 "라우팅됨" 버튼 disabled + title(tooltip)="담당자 미배정 상태로는 라우팅 단계로 전이할 수 없습니다" | playwright evaluate |
| TC-SRM-014 | PASS | 배정 후 재조회 시 버튼 활성화, 클릭 시 200 전이 성공(토스트 "상태가 '라우팅됨'로 변경되었습니다") | playwright snapshot |

## 실패 항목 분석 — 최초 실행(수정 전, 참고용)

### TC-SRM-009 — 요청 큐 배정 버튼 노출 조건 (1) "본인 배정됨" 미적용

- **증상**: SCR-SRM-004 스펙상 "이미 로그인 사용자 본인에게 배정됨(목록 응답의 `assigneeId`가 로그인 사용자 id와 일치)"이면 배정 버튼을 숨겨야 하는데, 실제로는 본인에게 배정한 뒤에도 "배정" 버튼이 계속 노출된다.
- **재현 방법**:
  1. agent@itsm.local(SERVICE_DESK_AGENT) 로그인, `/service-requests/queue` 진입
  2. VALIDATED 상태·미배정 요청(예: SRM-2026-0012)의 "배정" 클릭 → 팝업에서 후보(본인) 선택해 배정
  3. 큐 목록이 갱신된 후에도 해당 행에 "배정" 버튼이 그대로 노출됨(담당자 컬럼은 "서비스데스크 담당자"로 정상 갱신됨)
- **근본 원인**: `GET /api/v1/service-requests?scope=all` 응답(API-SRM-007)이 `assigneeId` 필드를 아예 반환하지 않는다. `source/backend/src/main/java/com/itsm/srm/application/dto/RequestSummaryResponse.java`에 `assigneeId` 필드 자체가 없고, `ServiceRequestService.toSummary()`(같은 패키지 `application/ServiceRequestService.java` L378-382)도 이 값을 채우지 않는다. FE `RequestQueuePage.tsx`의 `canAssign(r, myId)`(L39-42)는 `r.assigneeId === myId`로 조건 (1)을 판정하는데, `assigneeId`가 항상 `undefined`라 이 조건이 절대 참이 될 수 없다.
  - 네트워크 캡처: `GET /api/v1/service-requests?scope=all&page=0&size=16` 응답에 `{"id":12,...,"assignee":"서비스데스크 담당자",...}` — `assigneeId` 키 자체가 없음.
- **관련 요구사항**: `docs/02_plan/screen/service-request.md` SCR-SRM-004 "배정 버튼 노출 조건(2026-07-15 유지보수 요청, 신규)", `docs/02_plan/api_spec/service-request.md` API-SRM-007 응답 스펙(`assigneeId` 필드 명시).
- **조건 (2)(상태가 ROUTED 이후)는 정상 동작**(TC-SRM-010) — 이 조건은 `assigneeId`와 무관하게 상태값만으로 판정하므로 영향 없음.
- **제안**: `RequestSummaryResponse`에 `Long assigneeId` 필드 추가 + `toSummary()`에서 `r.getAssigneeId()` 채우기(백엔드, `ServiceRequestService` 담당).
