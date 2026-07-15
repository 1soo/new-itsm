# 통합 테스트 시나리오 — auth

> 대상: ① 감사 로그 actor 오기록 결함 수정(관리자가 타 사용자 변경 시 actor=관리자로 정확히 기록) ② 승인 프로세스 관리 화면(SCR-ADMIN-007/008) 삭제 기능 신규 ③ 편집 진입 시 요청유형 select 미렌더링 결함 수정
> 근거 문서: `docs/02_plan/api_spec/auth.md`(공통 규약, API-AUTH-006~029), `docs/02_plan/screen/admin.md`(SCR-ADMIN-005/007/008), `docs/01_analyze/feature/auth.md`(FEAT-AUTH-001/006/008)
> 변경 파일: `source/backend/.../auth/application/UserAdminService.java`, `.../auth/application/ApprovalProcessAdminService.java`, `source/frontend/src/features/admin/ApprovalProcessListPage.tsx`, `ApprovalProcessFormPage.tsx`

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- 로컬 환경: docker `itsm-postgres`(:5432), backend(:8080), frontend(:5173) 기동 상태
- 테스트 계정(공통 비밀번호 `Admin@1234`): `admin@itsm.local`(SYSTEM_ADMIN)
- 사전 데이터: 승인 프로세스 규칙 1건 존재(`노트북 신청 승인 규칙`, domain=SERVICE_REQUEST, requestSubtypeKey=노트북 신청 카탈로그 id, requesterRole=END_USER, 1차 승인자=PROCESS_OWNER, priorityTier=37) — 이전 유지보수(20260712-014911)에서 생성된 기존 규칙, 이번 TC-APPR-EDIT-001에서 조회만 하고 저장하지 않음(다른 도메인 시나리오가 재사용하는 기존 데이터라 변경 금지)

## 시나리오

### TC-BUILD-001 · 빌드 테스트
- 근거: @docs/02_plan/api_spec/auth.md
- 절차: 1) `./gradlew compileJava -q`(backend) 2) `npm run build`(frontend)
- 기대 결과: 오류 없이 성공

### TC-AUDIT-ACTOR-001 · 계정 생성 감사 로그 — actor=관리자
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-008), @docs/01_analyze/feature/compliance.md (FEAT-COMP-004 유사 원칙), @docs/02_plan/api_spec/auth.md (API-AUTH-007/015 감사 로그 actor 기록 규칙)
- 전제: `admin@itsm.local` 로그인 상태(`/admin/users`)
- 절차:
  1. "계정 생성" 버튼 클릭, 이메일 `qa.audit@itsm.local`·이름·초기 역할(END_USER)·초기 비밀번호(`Temp@1234`) 입력 후 저장
  2. 사이드바 "감사 로그" 메뉴(`/admin/audit-logs`) 진입, 이벤트 유형 필터 "계정/역할 변경" 선택
- 기대 결과: 방금 생성 이벤트가 목록에 조회되고, `actor` 컬럼은 `admin@itsm.local`(로그인한 관리자), `target` 컬럼은 `qa.audit@itsm.local`(대상 계정)로 서로 다르게 표시된다(대상자 자신이 actor로 오기록되지 않음)

### TC-AUDIT-ACTOR-002 · 역할 부여/회수 감사 로그 — actor=관리자
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-006/008), @docs/02_plan/api_spec/auth.md (API-AUTH-011/012)
- 전제: TC-AUDIT-ACTOR-001에서 생성한 `qa.audit@itsm.local` 계정 상세(`/admin/users/{id}`) 화면
- 절차:
  1. 역할 부여 패널에서 `SERVICE_DESK_AGENT` 역할 추가
  2. 감사 로그(`/admin/audit-logs`, 필터 "계정/역할 변경") 재조회
  3. 계정 상세로 돌아가 방금 추가한 역할을 회수(제거)
  4. 감사 로그 재조회
- 기대 결과: 부여·회수 각 이벤트 모두 `actor=admin@itsm.local`, `target=qa.audit@itsm.local`로 기록된다(2건 모두 actor가 대상 계정 이메일이 아님)

### TC-AUDIT-ACTOR-003 · 계정 상태 변경(비활성화) 감사 로그 — actor=관리자
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-001, "IF 요청자가 관리자 역할이 아니면" 대비 정상 관리자 경로), @docs/02_plan/api_spec/auth.md (API-AUTH-010)
- 전제: `qa.audit@itsm.local` 계정 상세 화면
- 절차:
  1. "비활성화" 버튼 클릭 후 확인 다이얼로그에서 확인
  2. 감사 로그(`/admin/audit-logs`, 필터 "계정/역할 변경") 재조회
- 기대 결과: 상태 변경 이벤트의 `actor=admin@itsm.local`, `target=qa.audit@itsm.local`로 기록된다(기존 결함이었다면 actor가 `qa.audit@itsm.local` 자신으로 오기록됐을 케이스)

### TC-APPR-CREATE-DELETE-001 · 승인 프로세스 생성/목록 반영/삭제
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-007, "삭제 버튼" 신규), @docs/02_plan/api_spec/auth.md (API-AUTH-027/029)
- 전제: `admin@itsm.local` 로그인 상태, `/admin/approval-processes` 목록
- 절차:
  1. "프로세스 생성" 버튼 클릭(`/admin/approval-processes/new`)
  2. 도메인 select에서 "전체 도메인" 선택 → 요청 유형 select가 화면에서 사라지는지 확인(도메인 미지정 시 하위유형 필드 자체 비노출)
  3. 규칙명 `QA 삭제테스트 규칙` 입력, 승인 요청자 박스는 역할 미선택(0개, 전체 요청자)로 둔 채 "승인자 추가" 클릭 후 1차 박스에 역할 1개(예: 시스템 관리자) 선택
  4. "생성 완료" 클릭
  5. 목록(`/admin/approval-processes`)에서 방금 생성한 행 확인(도메인="전체", 요청유형="전체", 우선순위 배지="전체 적용")
  6. 해당 행의 "삭제" 버튼 클릭(행 클릭=편집 이동과 분리되어 그대로 목록에 머무는지 확인) → 확인 다이얼로그("...삭제하시겠습니까? 진행 중인 승인 인스턴스는 삭제 영향을 받지 않습니다" 안내 포함)에서 확인
- 기대 결과: 2번에서 요청유형 select 비노출 확인. 4번 저장 성공 토스트 후 목록 복귀. 5번 목록에 신규 행이 정확한 배지로 조회됨. 6번 삭제 확인 후 성공 토스트와 함께 목록에서 해당 행이 즉시 사라짐(재조회 없이도 반영)

### TC-APPR-EDIT-001 · 편집 진입 시 요청유형 select 정상 렌더링(결함 수정)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-008, "편집 모드 요청유형 select 미렌더링 결함 수정"), @docs/02_plan/api_spec/auth.md (API-AUTH-026/024)
- 전제: 사전 데이터의 `노트북 신청 승인 규칙`(domain=SERVICE_REQUEST, requestSubtypeKey=노트북 신청)
- 절차:
  1. `/admin/approval-processes` 목록에서 `노트북 신청 승인 규칙` 행 클릭(편집 화면 진입)
  2. "규칙 정보" 카드의 도메인 select(비활성)·요청 유형 select(비활성) 값 확인
  3. 저장 없이 목록으로 돌아가기(브라우저 뒤로가기 또는 상단 이동, "저장" 버튼 클릭하지 않음)
- 기대 결과: 도메인 select에 "서비스 요청"(SERVICE_REQUEST) 값이, 요청 유형 select에 저장된 값(노트북 신청 카탈로그 항목명)이 빈칸이 아니라 정상 표시된다(기존 결함이었다면 요청 유형 select가 빈칸으로 보였을 케이스). 3번에서 변경 없이 이탈했으므로 규칙 데이터는 그대로 유지된다
