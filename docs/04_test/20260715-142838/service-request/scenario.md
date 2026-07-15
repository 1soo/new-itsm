# 통합 테스트 시나리오 — service-request (요청 유형별 담당자 역할 지정, 유지보수 요청 2026-07-15)

## 사전 조건
- 빌드 테스트 통과(백엔드 Gradle build, 프론트엔드 tsc+vite build)
- 계정: agent@itsm.local(SERVICE_DESK_AGENT), po@itsm.local(PROCESS_OWNER), user@itsm.local(END_USER), cab@itsm.local(APPROVER) — 전부 비밀번호 `Admin@1234`
- 시드 데이터: `service_catalog_item` '노트북 신청'에 `assignee_role_id`=SERVICE_DESK_AGENT 지정, '비밀번호 초기화'는 NULL

## 시나리오

### TC-SRM-001 · 빌드 테스트
- 근거: 통합테스트 선행 항목
- 절차: 1. 백엔드 `./gradlew build` 2. 프론트엔드 `npm run build`
- 기대 결과: 둘 다 오류 없이 성공

### TC-SRM-002 · 카탈로그 관리 화면 담당자 역할 select 노출·선택
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007, @docs/02_plan/api_spec/service-request.md API-SRM-003/004/API-AUTH-030
- 전제: po@itsm.local 로그인, `/admin/service-catalog` 진입
- 절차:
  1. 새 항목 생성 폼에서 "담당자 역할" select 오픈
  2. 역할 목록(API-AUTH-030 응답) 중 하나 선택 후 이름·양식 필드 입력해 저장
- 기대 결과: select에 역할 목록이 노출되고, 저장된 항목의 상세 조회 시 지정한 `assigneeRoleId`/`assigneeRoleName`이 반영됨

### TC-SRM-003 · 카탈로그 관리 화면 편집 진입 시 큐·담당자 역할 프리필(결함 수정 확인)
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007 "편집 진입 시 프리필"
- 전제: po@itsm.local 로그인, 기존 '노트북 신청' 항목 편집 진입
- 절차: 1. 목록에서 '노트북 신청' 클릭 2. 폼의 담당 큐·담당자 역할 select 값 확인
- 기대 결과: 두 select 모두 상세 조회 응답(`queueId`/`assigneeRoleId`) 값으로 프리필됨(빈 값으로 리셋되던 구 결함 재발 없음)

### TC-SRM-004 · 담당 큐 Select 미선택 시 "미분류" 처리
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007 담당 큐 select
- 절차: 신규 항목 생성 시 담당 큐 select에서 "미분류" 선택(또는 미선택) 후 저장
- 기대 결과: 저장 성공, 상세 조회 시 `queueId`=null

### TC-SRM-005 · API-SRM-017 담당자 후보 목록 — 역할 지정 카탈로그
- 근거: @docs/02_plan/api_spec/service-request.md API-SRM-017
- 전제: '노트북 신청'으로 제출된 요청 1건, agent@itsm.local 로그인(SERVICE_DESK_AGENT)
- 절차: `GET /api/v1/service-requests/{id}/assignee-candidates` 호출
- 기대 결과: 200, SERVICE_DESK_AGENT 역할을 보유한 ACTIVE 사용자 목록 반환(agent@itsm.local 포함)

### TC-SRM-006 · API-SRM-017 담당자 후보 목록 — 역할 미지정 카탈로그
- 근거: 위와 동일, API-SRM-017 "미지정 시 빈 배열"
- 전제: '비밀번호 초기화'로 제출된 요청 1건
- 절차: 위 API 호출
- 기대 결과: 200, 빈 배열([])

### TC-SRM-007 · 요청 큐 배정 팝업 — 역할 후보 선택 배정
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-004 담당자 배정 팝업
- 전제: agent@itsm.local 로그인, '노트북 신청' 요청이 큐에 존재(미배정)
- 절차: 1. 큐 화면에서 해당 행 "배정" 버튼 클릭 2. 팝업에서 후보 이름 클릭
- 기대 결과: 팝업에 SERVICE_DESK_AGENT 보유자 후보 목록 노출, 클릭 시 `POST /assign`(assigneeId) 호출되어 담당자 갱신

### TC-SRM-008 · 요청 큐 배정 팝업 — 후보 없음(본인 배정)
- 근거: 위와 동일
- 전제: '비밀번호 초기화' 요청(담당자 역할 미지정)이 큐에 존재
- 절차: "배정" 버튼 클릭 → 팝업 확인
- 기대 결과: "지정된 담당자 역할이 없습니다" 문구 + "나에게 배정" 버튼 노출, 클릭 시 본인으로 배정(assigneeId 미지정 요청)

### TC-SRM-009 · 요청 큐 배정 버튼 노출 조건 (1) 본인 배정됨
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-004 "배정 버튼 노출 조건"
- 전제: agent@itsm.local 로그인, 이미 본인(agent)에게 배정된 요청 1건(상태 VALIDATED 등 ROUTED 이전)
- 절차: 큐 화면에서 해당 행 확인
- 기대 결과: "배정" 버튼이 숨겨짐(응답 `assigneeId`==로그인 사용자 id)

### TC-SRM-010 · 요청 큐 배정 버튼 노출 조건 (2) 상태가 ROUTED 이후
- 근거: 위와 동일
- 전제: 다른 사용자에게 배정되었으나 상태가 ROUTED/IN_FULFILLMENT/FULFILLED/CLOSED 중 하나인 요청 1건
- 절차: 큐 화면에서 해당 행 확인
- 기대 결과: "배정" 버튼이 숨겨짐(assigneeId 불일치여도 상태 조건으로 숨김)

### TC-SRM-011 · API-SRM-010 라우팅 전이 시 담당자 미배정 409
- 근거: @docs/02_plan/api_spec/service-request.md API-SRM-010 409 `ASSIGNEE_REQUIRED_FOR_ROUTING`
- 전제: VALIDATED 상태이고 담당자 미배정인 요청 1건
- 절차: `PATCH /api/v1/service-requests/{id}/status` `{targetStatus: "ROUTED"}` 호출
- 기대 결과: 409, code=`ASSIGNEE_REQUIRED_FOR_ROUTING`

### TC-SRM-012 · 담당자 배정 후 정상 라우팅
- 근거: 위와 동일
- 전제: TC-SRM-011과 동일 요청에 담당자 배정 완료
- 절차: 배정 후 동일 전이 재시도
- 기대 결과: 200, status=ROUTED

### TC-SRM-013 · 서비스 요청 상세 라우팅 버튼 비활성화(담당자 미배정)
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-005 "라우팅 버튼 비활성화"
- 전제: VALIDATED 상태·담당자 미배정 요청 상세 화면(agent 로그인)
- 절차: 상세 화면에서 "ROUTED" 전이 버튼 상태 확인(hover 시 tooltip)
- 기대 결과: 버튼 비활성화(disabled) + tooltip "담당자 미배정 상태로는 라우팅 단계로 전이할 수 없습니다"

### TC-SRM-014 · 담당자 배정 후 라우팅 버튼 활성화
- 근거: 위와 동일
- 전제: TC-SRM-013 요청에 배정 완료 후 재조회
- 기대 결과: ROUTED 버튼 활성화, 클릭 시 정상 전이(200)
