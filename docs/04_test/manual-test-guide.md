# 수동 통합 테스트 가이드 (전 도메인)

개발자가 아닌 사용자가 브라우저에서 직접 클릭해가며 수행할 수 있도록 작성한 통합 테스트 시나리오다. curl/gradle/API 직접 호출 등 개발자 도구 절차는 포함하지 않는다.

- 근거: `docs/01_analyze/prd/`, `docs/01_analyze/feature/`(PRD·기능명세 핵심 흐름) + `docs/02_plan/screen/`(정확한 화면 ID·경로)
- 각 TC는 참고용이며, 화면 문구·경로는 실제 구현과 다를 수 있다. 다를 경우 실제 화면을 기준으로 판단하고 필요하면 담당자에게 공유해달라.
- 상태 전이·승인 게이트 등 순서가 있는 TC는 문서에 적힌 순서대로 수행하는 것을 권장한다(이전 TC의 결과물을 다음 TC에서 재사용하는 경우가 있음).

## 0. 사전 준비

### 로컬 환경 기동
1. **Database**: `cd source/db/docker` → `.env` 확인 → `docker compose up -d` (최초 기동 시 DDL·시드 데이터 자동 실행)
2. **Backend**: `cd source/backend` → `.env`를 DB 접속 정보와 일치시킴 → `./gradlew.bat bootRun`(Windows) — 기본 포트 `8080`
3. **Frontend**: `cd source/frontend` → `npm install` → `npm run dev` — 기본 포트 `5173`
4. 브라우저에서 `http://localhost:5173` 접속

### 테스트 계정 (공통 비밀번호: `Admin@1234`)

| 이메일 | 역할 | 주요 사용 도메인 |
|---|---|---|
| admin@itsm.local | SYSTEM_ADMIN | auth(관리자 화면 전반), common, ui-revamp |
| user@itsm.local | END_USER | auth, common, service-request, ui-revamp |
| po@itsm.local | PROCESS_OWNER | service-request, esm |
| im@itsm.local | INCIDENT_MANAGER | incident |
| agent@itsm.local | SERVICE_DESK_AGENT | incident |
| pm@itsm.local | PROBLEM_MANAGER | problem |
| cm@itsm.local | CHANGE_MANAGER | change |
| cab@itsm.local | APPROVER (CAB 승인자) | change |
| kc@itsm.local | KNOWLEDGE_CONTRIBUTOR | knowledge |
| kg@itsm.local | KNOWLEDGE_GATEKEEPER | knowledge |
| am@itsm.local | ASSET_MANAGER | asset |
| hr@itsm.local | HR_CASE_MANAGER | esm |
| legal-coord@itsm.local | DEPT_COORDINATOR(법무) | esm |
| facilities-coord@itsm.local | DEPT_COORDINATOR(시설) | esm |
| it-coord@itsm.local | DEPT_COORDINATOR(IT) | esm |
| vm@itsm.local | VULNERABILITY_MANAGER | vulnerability |
| co@itsm.local | COMPLIANCE_OFFICER | compliance |
| io@itsm.local | INFRA_OPERATOR | infra-monitoring |

### 진행 팁
- 서로 다른 두 계정이 동시에 필요한 TC(승인 등)는 하나는 일반 창, 하나는 시크릿(private) 창으로 열어 각각 로그인하면 로그아웃 없이 편하게 확인 가능하다.
- 승인 게이트가 있는 TC(문제/변경/자산 폐기/취약점/컴플라이언스 등)는 관리자가 사전에 승인 프로세스를 구성했는지에 따라 즉시 전이되거나 승인 대기 상태로 멈출 수 있다 — 두 경우 모두 정상이며, 각 TC의 기대결과에 분기로 설명해두었다.
- 목차: 1.auth · 2.common · 3.service-request · 4.incident · 5.problem · 6.change · 7.knowledge · 8.asset · 9.esm · 10.vulnerability · 11.compliance · 12.infra-monitoring · 13.ui-revamp

---

## 1. auth (인증·권한)

### 사전 준비
- 공통 비밀번호: `Admin@1234`
- 테스트 계정: `admin@itsm.local`(SYSTEM_ADMIN), `user@itsm.local`(END_USER)
- 일부 시나리오(TC-AUTH-005~007)는 이후 다른 TC의 전제(감사 로그 데이터)가 되므로, 가능하면 본 문서의 번호 순서대로 수행한다.
- 두 계정을 동시에 확인해야 하는 TC는 하나는 일반 창, 하나는 시크릿(private) 창으로 열어 각각 로그인한다.

### TC-AUTH-001 로그인 성공(관리자)
1. `/login` 접속
2. 이메일 `admin@itsm.local`, 비밀번호 `Admin@1234` 입력 후 "로그인" 버튼 클릭
기대결과: 계정 목록 화면(`/admin/users`)으로 이동한다(관리자 계정은 로그인 후 관리자 홈으로 진입). 좌측 사이드바에 "관리자" 메뉴 그룹(계정 관리·역할 관리·감사 로그·메뉴 관리 등)이 노출된다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-002), @docs/02_plan/screen/auth.md (SCR-AUTH-001), @docs/02_plan/screen/admin.md (SCR-ADMIN-001), @docs/02_plan/screen/common.md (SCR-COM-003)

### TC-AUTH-002 로그인 실패(비밀번호 불일치)
1. `/login` 접속
2. 이메일 `admin@itsm.local`, 비밀번호에 임의의 틀린 값(예: `Wrong@1234`) 입력 후 "로그인" 버튼 클릭
기대결과: 로그인이 거부되고 화면에 "이메일 또는 비밀번호가 올바르지 않습니다" 오류 메시지만 표시된다(계정 존재 여부는 노출되지 않음). 로그인 화면이 유지된다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-002 Unwanted), @docs/02_plan/screen/auth.md (SCR-AUTH-001)

### TC-AUTH-003 로그아웃 및 세션 무효화
사전 준비: `admin@itsm.local` 로그인 상태
1. 헤더 우측 사용자 아바타(사용자 메뉴) 클릭 후 "로그아웃" 클릭, 확인
기대결과: 로그인 화면(`/login`)으로 이동한다.
2. 브라우저 뒤로가기 버튼으로 직전 보호 화면(예: 계정 목록)으로 이동 시도
기대결과: 다시 로그인 화면으로 리다이렉트되어(세션이 무효화되어) 이전 화면 내용이 노출되지 않는다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-004), @docs/02_plan/screen/common.md (SCR-COM-002 사용자 메뉴, SCR-COM-005 인증 가드)

### TC-AUTH-004 비밀번호 변경
사전 준비: `user@itsm.local` / `Admin@1234` 로그인 상태
1. 헤더 사용자 메뉴 > "비밀번호 변경" 클릭(`/profile/password` 이동)
2. 현재 비밀번호에 틀린 값을 입력하고 새 비밀번호/확인은 정상 값으로 입력 후 저장
기대결과: 현재 비밀번호 불일치 오류 메시지가 표시되고 변경이 거부된다.
3. 현재 비밀번호에 `Admin@1234`(정확한 값), 새 비밀번호에 정책 미충족 값(예: `abc`, 8자 미만) 입력 후 저장
기대결과: 비밀번호 정책(8자 이상, 영문+숫자 포함) 위반 인라인 오류가 표시된다.
4. 새 비밀번호/확인에 정책을 충족하는 값(예: `Temp@1234`)을 동일하게 입력 후 저장
기대결과: 성공 토스트가 표시되고 프로필 화면으로 복귀한다.
5. 로그아웃 후 `user@itsm.local` / `Temp@1234`로 재로그인
기대결과: 로그인에 성공한다.
6. (정리) 3~4번과 동일한 절차로 비밀번호를 다시 `Admin@1234`로 변경해 원상 복구한다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-007), @docs/02_plan/screen/auth.md (SCR-AUTH-003)

### TC-AUTH-005 계정 생성·역할 부여/회수·비활성화
사전 준비: `admin@itsm.local` 로그인 상태
1. 사이드바 "관리자" 그룹에서 계정 관리 메뉴 클릭(`/admin/users`), "계정 생성" 버튼 클릭
2. 이메일(예: `qa.tester@itsm.local`)·이름·초기 역할(END_USER 1개)·초기 비밀번호(정책 충족, 예: `Temp@1234`) 입력 후 저장
기대결과: 성공 토스트 표시 후 목록에 신규 계정이 활성 상태로 노출된다.
3. 시크릿 창에서 `/login` 접속 후 방금 만든 계정으로 로그인
기대결과: 로그인에 성공하고 대시보드(`/`)로 이동하며, 사이드바에 관리자 그룹은 노출되지 않는다.
4. admin 창에서 목록의 해당 계정 행을 클릭해 상세 화면(`/admin/users/{id}`) 진입, 역할 부여 패널에서 역할 1개(예: 서비스 데스크 상담원)를 추가로 선택
기대결과: 선택 즉시 역할이 칩으로 추가 반영된다.
5. 시크릿 창에서 새로고침 후 사이드바 확인
기대결과: 새로 부여된 역할에 매핑된 메뉴가 사이드바에 추가로 노출된다.
6. admin 창에서 같은 계정 상세로 재진입, 방금 추가한 역할 칩을 회수(제거)
기대결과: 즉시 반영되어 칩이 사라진다.
7. 같은 화면에서 "비활성화" 버튼 클릭 후 확인 다이얼로그에서 확인
기대결과: 계정 상태가 "비활성" 배지로 변경된다.
8. 시크릿 창에서 로그아웃 후 비활성화된 계정으로 재로그인 시도
기대결과: 로그인이 거부되고 "이메일 또는 비밀번호가 올바르지 않습니다" 오류만 표시된다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-001, REQ-AUTH-006), @docs/02_plan/screen/admin.md (SCR-ADMIN-001, SCR-ADMIN-002, SCR-ADMIN-003)

### TC-AUTH-006 메뉴 관리(사이드바 반영)
사전 준비: `admin@itsm.local` 로그인 상태
1. 사이드바 "관리자" 그룹에서 메뉴 관리 클릭(`/admin/menus`), "메뉴 생성" 버튼 클릭
2. 그룹(신규 입력 시 그룹 영문명도 함께 입력)·메뉴명(한국어)·메뉴 영문명·경로(예: `/qa-test-menu`)·아이콘(lucide 아이콘명 선택)·순서(숫자) 입력 후 저장
기대결과: 성공 토스트 표시 후 목록에 새 메뉴 항목이 노출된다.
3. 새로 만든 메뉴 행의 "역할 매핑" 버튼 클릭 → 우측 슬라이드 패널에서 END_USER 역할 체크박스 선택
기대결과: 체크 즉시 반영(토스트)되고 목록의 "노출 역할 수"가 갱신된다.
4. `user@itsm.local`로 로그인된 다른 창에서 새로고침
기대결과: 방금 매핑한 신규 메뉴 항목이 사이드바에 노출된다.
5. admin 창에서 방금 만든 메뉴를 삭제(휴지통 버튼) 후 확인 다이얼로그에서 확인
기대결과: 목록에서 메뉴가 사라지고, `user@itsm.local` 창에서 새로고침 시 사이드바에서도 함께 사라진다.
근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006), @docs/02_plan/screen/common.md (SCR-COM-003)

### TC-AUTH-007 감사 로그 조회
사전 준비: `admin@itsm.local` 로그인 상태, TC-AUTH-002~006을 먼저 수행해 로그인 실패·계정 생성·역할 변경·비활성화·메뉴 변경 이력이 쌓인 상태
1. 사이드바 "관리자" 그룹에서 감사 로그 클릭(`/admin/audit-logs`)
2. 이벤트 유형 필터에서 "로그인" 선택
기대결과: 표에 로그인 관련 이벤트(시각·이벤트·주체·대상·결과)만 필터링되어 조회되고, 앞서 발생시킨 로그인 실패 이력도 결과에 포함된다.
3. 필터를 "계정/역할 변경"으로 변경
기대결과: 앞서 수행한 계정 생성·역할 부여/회수·비활성화 이력이 조회된다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-008), @docs/02_plan/screen/admin.md (SCR-ADMIN-005)

### TC-AUTH-008 권한 없는 화면 접근 시 403
사전 준비: `user@itsm.local`(END_USER) 로그인 상태
1. 주소창에 직접 `http://localhost:5173/admin/users` 입력해 이동
기대결과: 403 접근 거부 화면("이 페이지에 접근할 권한이 없습니다" 안내와 "이전으로" 버튼)이 표시되고, 계정 목록 데이터는 전혀 노출되지 않는다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-005), @docs/02_plan/screen/common.md (SCR-COM-006)

---

## 2. common (공통)

### 사전 준비
- 테스트 계정: `admin@itsm.local`(SYSTEM_ADMIN), `user@itsm.local`(END_USER), 공통 비밀번호 `Admin@1234`.
- 알림 팝오버 관련 TC는 계정에 승인 대기 또는 자산 만료 임박 데이터가 없으면 알림 벨에 뱃지가 표시되지 않을 수 있다 — 이 경우 "알림 없음" 상태 안내가 정상적으로 표시되는지 확인하는 것도 유효한 검증이다.

### TC-COM-001 헤더 알림 팝오버 조회
사전 준비: 아무 계정으로 로그인한 상태
1. 헤더 우측 알림 벨 아이콘 클릭
기대결과: 벨 하단에 팝오버가 열린다. 대기 중인 알림이 있으면 각 항목이 2줄(1행: 도메인 라벨(Lozenge)·상대 시간 또는 만료일, 2행: 제목)과 "상세 보기" 버튼으로 표시되고, 없으면 "새로운 알림이 없습니다" 안내가 표시된다.
2. (알림이 1건 이상 있는 경우) 임의 알림 항목의 "상세 보기" 버튼 클릭
기대결과: 팝오버가 닫히고 해당 알림의 상세 화면(승인 대기 건이면 해당 티켓 상세, 자산 만료 임박이면 해당 자산 상세)으로 이동한다.
근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/01_analyze/feature/common.md (FEAT-COM-001), @docs/02_plan/screen/common.md (SCR-COM-002)

### TC-COM-002 헤더 알림 확인처리
사전 준비: 알림 벨에 뱃지(대기 건수)가 1건 이상 표시된 상태
1. 알림 벨 클릭 후 팝오버에서 알림 항목 1행 우측의 X(개별 확인처리) 버튼 클릭
기대결과: 해당 항목만 목록에서 즉시 사라지고 벨 뱃지 카운트가 1 감소한다.
2. 팝오버를 다시 열고 우측 상단 "모두 지우기" 버튼 클릭
기대결과: 표시 중이던 나머지 알림이 모두 사라지고 "새로운 알림이 없습니다" 안내로 전환되며 벨 뱃지가 사라진다.
근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/02_plan/screen/common.md (SCR-COM-002)

### TC-COM-003 사용자 가이드 화면
사전 준비: 아무 계정으로 로그인한 상태
1. 헤더의 "?"(사용자 가이드) 아이콘 클릭
기대결과: `/guide` 화면으로 이동한다(사이드바·헤더는 유지된 채 일반 화면 전환, 모달 아님). 좌측에 목차(개요·도메인 및 원칙·역할별 수행 내용과 방법) 패널, 우측에 "사용자 가이드" 제목의 본문이 표시된다.
2. 좌측 목차에서 "도메인 및 원칙" 클릭
기대결과: 해당 섹션으로 스크롤 이동하며, 11개 업무 도메인이 접힌 아코디언 목록으로 노출되고 클릭 시 개별적으로 펼쳐진다.
3. 좌측 목차에서 "역할별 수행 내용과 방법" 클릭
기대결과: 로그인 계정이 보유한 역할이 "내 역할" 배지와 함께 목록 상단에 펼쳐진 상태로 노출되고, 나머지 역할은 원래 순서대로 접힌 채 그 아래 나열된다.
근거: @docs/01_analyze/prd/common.md (REQ-COM-002), @docs/01_analyze/feature/common.md (FEAT-COM-002), @docs/02_plan/screen/common.md (SCR-COM-012)

### TC-COM-004 언어 전환(i18n)
사전 준비: 아무 계정으로 로그인한 상태
1. 헤더의 지구본(언어 선택) 아이콘 클릭
기대결과: "한국어"/"English" 2개 항목의 팝업이 열리고, 현재 선택된 언어 항목에 체크 아이콘이 표시된다.
2. "English" 항목 클릭
기대결과: 팝업이 닫히고 새로고침 없이 헤더·사이드바 메뉴 라벨 등 화면 텍스트가 즉시 영어로 전환된다.
3. 브라우저 새로고침(F5)
기대결과: 언어 선택이 English로 유지된다(재선택 불필요).
4. 다시 지구본 아이콘 클릭 후 "한국어" 선택
기대결과: 화면 텍스트가 즉시 한국어로 복원된다.
근거: @docs/02_plan/screen/common.md (SCR-COM-015, 6절 다국어 아키텍처)

### TC-COM-005 사이드바 접기/펼치기
사전 준비: 아무 계정으로 로그인한 상태
1. 사이드바의 접기 토글 버튼 클릭
기대결과: 사이드바 폭이 좁아지며(펼침 190px→접힘 48px) 그룹 헤더·메뉴 라벨 텍스트는 사라지고 아이콘만 남는다.
2. 토글 버튼을 다시 클릭
기대결과: 사이드바가 원래 폭으로 펼쳐지며 그룹 헤더·메뉴 라벨 텍스트가 다시 노출된다.
근거: @docs/02_plan/screen/common.md (SCR-COM-001, SCR-COM-003)

### TC-COM-006 사이드바 역할별 메뉴 노출(RBAC)
사전 준비: `admin@itsm.local`, `user@itsm.local` 두 계정
1. `admin@itsm.local`로 로그인한 상태의 사이드바 메뉴 구성을 확인
기대결과: "관리자" 메뉴 그룹(계정 관리·역할 관리·감사 로그·메뉴 관리 등)이 노출된다.
2. 로그아웃 후 `user@itsm.local`로 로그인
기대결과: 사이드바에 "관리자" 그룹은 노출되지 않고, END_USER에게 매핑된 메뉴(예: 서비스요청 포털·지식 등 포털 성격 메뉴)만 노출된다.
근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-005), @docs/02_plan/screen/common.md (SCR-COM-003)

---

## 3. service-request (서비스 요청)

### 사전 준비
- `user@itsm.local` / `po@itsm.local` 계정, 공통 비밀번호 `Admin@1234`
- 서비스 카탈로그에 최소 1개 이상의 "승인이 필요하도록 구성된 요청 유형"이 존재해야 함(관리자가 SCR-ADMIN-008에서 사전 구성)

### TC-SRM-001 서비스 카탈로그 조회 및 요청 제출
1. `user@itsm.local` 로그인 후 서비스 포털(카탈로그)에서 요청 유형 카드 선택
2. 동적 양식(필수 필드 포함) 작성 후 제출
기대결과: 접수번호가 발급되며 "내 요청 목록"에 해당 요청이 표시됨
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-002), @docs/02_plan/screen/service-request.md(SCR-SRM-001, SCR-SRM-002)

### TC-SRM-002 필수 필드 미입력 시 제출 차단
1. `user@itsm.local` 로그인 후 요청 제출 화면에서 필수 필드를 비운 채 제출 시도
기대결과: 인라인 오류가 표시되고 제출이 차단됨(요청이 생성되지 않음)
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-002 Unwanted), @docs/02_plan/screen/service-request.md(SCR-SRM-002)

### TC-SRM-003 승인 요청 및 승인 처리
1. `user@itsm.local` 로그인, 승인이 필요하도록 구성된 요청 유형으로 신규 요청 제출
2. `po@itsm.local` 로그인 후 승인 대기함에서 해당 요청을 찾아 승인 처리
기대결과: 요청 상태가 승인 완료로 갱신되고 승인 대기함에서 해당 건이 사라짐. 요청 상세의 승인 패널에 승인 이력이 표시됨
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-005), @docs/02_plan/screen/service-request.md(SCR-SRM-005)

### TC-SRM-004 승인 반려 처리
1. `user@itsm.local` 로그인, 승인이 필요하도록 구성된 요청 유형으로 신규 요청 제출
2. `po@itsm.local` 로그인 후 승인 대기함에서 해당 요청을 반려(사유 입력 후 반려 처리)
기대결과: 요청이 반려 처리되고, 요청자(`user@itsm.local`) 화면의 요청 상세에서 반려 사유를 확인할 수 있음
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-005 Unwanted), @docs/02_plan/screen/service-request.md(SCR-SRM-005)

### TC-SRM-005 요청 배정 및 이행 완료
1. `po@itsm.local` 로그인 후 요청 큐 화면에서 처리 대상 요청에 "나에게 배정" 클릭
2. 요청 상세 화면에서 이행 작업 후 상태를 "이행 완료"로 전환
기대결과: 담당자에 `po@itsm.local`이 표시되고 요청 상태가 이행 완료로 갱신됨
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-006), @docs/02_plan/screen/service-request.md(SCR-SRM-004, SCR-SRM-005)

### TC-SRM-006 완료 확인·종료 및 CSAT 평가
1. `user@itsm.local` 로그인 후 이행 완료된 요청의 상세 화면에서 완료를 확인하고 요청을 종료 처리
2. 종료 후 노출되는 CSAT 위젯에서 만족도 평가 제출
기대결과: 요청 상태가 "종료"로 전환되고, CSAT 평가가 저장됨(재종료 시도 시 중복 처리가 거부됨도 함께 확인)
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-007, REQ-SRM-010), @docs/02_plan/screen/service-request.md(SCR-SRM-005)

### TC-SRM-007 권한 없는 사용자의 카탈로그 관리 접근 시도
1. `user@itsm.local` 로그인 후 서비스 카탈로그 관리 화면 접근 시도(메뉴 미노출 또는 URL 직접 접근)
기대결과: 화면이 노출되지 않거나 접근이 거부됨(프로세스 오너 권한 필요 안내)
근거: @docs/01_analyze/prd/service-request.md(REQ-SRM-001 Unwanted), @docs/02_plan/screen/service-request.md(SCR-SRM-007)

---

## 4. incident (인시던트 관리)

### 사전 준비
- `agent@itsm.local`(SERVICE_DESK_AGENT), `im@itsm.local`(INCIDENT_MANAGER) 계정, 공통 비밀번호 `Admin@1234`

### TC-INC-001 인시던트 등록
1. `agent@itsm.local` 로그인 후 인시던트 등록 화면에서 요약·심각도(SEV1~3)·영향 서비스 입력 후 등록
기대결과: 식별키가 부여된 인시던트가 생성되고 목록에 "신규" 상태로 표시됨
근거: @docs/01_analyze/prd/incident.md(REQ-INC-001), @docs/02_plan/screen/incident.md(SCR-INC-002)

### TC-INC-002 필수 필드 누락 시 등록 거부
1. `agent@itsm.local` 로그인 후 인시던트 등록 화면에서 요약 또는 심각도를 비운 채 등록 시도
기대결과: 인라인 오류가 표시되고 등록이 거부됨
근거: @docs/01_analyze/prd/incident.md(REQ-INC-001 Unwanted), @docs/02_plan/screen/incident.md(SCR-INC-002)

### TC-INC-003 상태 전이 및 심각도·우선순위 변경
1. `agent@itsm.local` 로그인 후 인시던트 상세에서 심각도/우선순위 변경
2. 허용된 상태 전이(신규 → 대응중) 수행
기대결과: 변경 이력이 기록되고 상태·심각도·우선순위가 갱신됨
근거: @docs/01_analyze/prd/incident.md(REQ-INC-002, REQ-INC-003), @docs/02_plan/screen/incident.md(SCR-INC-003)

### TC-INC-004 역할 배정(IM) 및 권한 없는 시도 차단
1. `agent@itsm.local` 로그인 후 인시던트 상세에서 역할 배정 시도(버튼 숨김/거부 확인)
2. `im@itsm.local` 로그인 후 동일 인시던트에서 Tech Lead/Comms/Scribe 등 역할 배정
기대결과: `agent`에게는 역할 배정 기능이 제한되고, `im`의 배정은 성공적으로 기록·표시됨
근거: @docs/01_analyze/prd/incident.md(REQ-INC-004), @docs/02_plan/screen/incident.md(SCR-INC-003)

### TC-INC-005 에스컬레이션
1. `agent@itsm.local` 로그인 후 인시던트 상세에서 에스컬레이션 버튼으로 `im@itsm.local`에게 이관
기대결과: 대상자에게 배정·통지되고 타임라인/이력에 에스컬레이션 기록이 남음
근거: @docs/01_analyze/prd/incident.md(REQ-INC-005), @docs/02_plan/screen/incident.md(SCR-INC-003)

### TC-INC-006 상태 업데이트 및 타임라인 확인
1. `im@itsm.local` 로그인 후 인시던트 상세에서 상태 업데이트(내부/외부 구분 선택) 등록
기대결과: 등록한 업데이트가 타임스탬프와 함께 타임라인에 즉시 표시됨
근거: @docs/01_analyze/prd/incident.md(REQ-INC-006), @docs/02_plan/screen/incident.md(SCR-INC-003)

### TC-INC-007 해결 처리 및 시간 지표 확인
1. `im@itsm.local` 로그인 후 인시던트 상세에서 영향시작·탐지·영향종료 시각을 입력하고 해결 처리
기대결과: 해결 시각이 기록되고 MTTD/MTTA/MTTR 값이 표시됨(SEV1·2이면서 포스트모템 미작성 시 "포스트모템 필요" 배너도 함께 확인)
근거: @docs/01_analyze/prd/incident.md(REQ-INC-007, REQ-INC-008), @docs/02_plan/screen/incident.md(SCR-INC-003)

### TC-INC-008 포스트모템 작성 및 문제 연계
1. `im@itsm.local` 로그인 후 해결된 인시던트에서 포스트모템 편집 화면으로 이동해 5 Whys·근본원인·조치항목을 작성 후 제출
2. 인시던트 상세에서 "문제 연계" 버튼으로 신규 문제 등록·링크
기대결과: 포스트모템이 인시던트에 연결·저장되고, 인시던트-문제 링크가 상세 화면에 표시됨
근거: @docs/01_analyze/prd/incident.md(REQ-INC-008, REQ-INC-009), @docs/02_plan/screen/incident.md(SCR-INC-004, SCR-INC-003)

---

## 5. problem (문제 관리)

### 사전 준비
- pm@itsm.local / `Admin@1234` 계정으로 로그인 가능해야 함 (역할: PROBLEM_MANAGER)
- 이후 각 TC는 별도 언급이 없는 한 직전 TC에서 등록한 동일 문제 티켓을 이어서 사용함(문제 목록에서 해당 식별키로 재진입)

### TC-PRB-001 문제 등록
1. pm@itsm.local 로그인 후 사이드바에서 "문제" 메뉴 진입, 문제 목록(SCR-PRB-001) 화면에서 등록 화면(SCR-PRB-002)으로 이동
2. 요약(필수)·설명·출처(반응/선제 중 선택)·조사사유·구성요소 입력
3. 영향도·긴급도를 각각 선택하고, 화면에 우선순위 미리보기 배지가 실시간으로 산정·표시되는지 확인
4. 등록 버튼 클릭
기대결과: 문제가 생성되고 상세 화면으로 이동하며, 상태가 '탐지'로, 우선순위가 영향도×긴급도 산정 결과로 표시됨. 문제 목록(SCR-PRB-001)에서도 방금 등록한 항목이 조회됨
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-001, REQ-PRB-003), @docs/02_plan/screen/problem.md (SCR-PRB-001, SCR-PRB-002)

### TC-PRB-002 문제 등록 필수 항목 누락 시 오류
1. 문제 등록 화면(SCR-PRB-002)에서 요약을 비운 채 다른 항목만 입력
2. 등록 버튼 클릭
기대결과: 등록이 거부되고 오류 메시지가 표시되며 문제가 생성되지 않음(문제 목록에도 추가되지 않음)
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-001 Unwanted), @docs/01_analyze/feature/problem.md (FEAT-PRB-001), @docs/02_plan/screen/problem.md (SCR-PRB-002)

### TC-PRB-003 문제 프로세스 상태 순차 전이 및 순서 위반 거부
1. TC-PRB-001에서 등록한 문제 상세(SCR-PRB-003) 진입
2. 프로세스 상태 전이 버튼으로 '탐지' → '분류/우선순위' → '조사/진단' 순서대로 한 단계씩 전이
3. 각 단계 전이 후 상태 배지가 정확히 갱신되는지 확인
4. '조사/진단' 상태에서 중간 단계(알려진 오류 기록)를 건너뛰고 곧바로 '워크어라운드' 또는 '해결/종료'로 전이를 시도
기대결과: 3번까지는 순서대로 상태가 정상 갱신되고, 4번의 순서를 건너뛴 전이 시도는 시스템이 거부하고 오류 메시지를 표시함(상태는 '조사/진단'에 그대로 유지)
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-002), @docs/02_plan/screen/problem.md (SCR-PRB-003)

### TC-PRB-004 근본 원인 분석(RCA) 기록
1. '조사/진단' 상태의 문제 상세(SCR-PRB-003)에서 RCA 섹션 진입
2. 근본원인, 5 Whys 단계, 근본원인 카테고리를 입력하고 저장 (특정 개인 이름을 근본원인으로 강제 입력하지 않아도 저장 가능한지 확인)
3. 저장 후 화면 새로고침
기대결과: 입력한 RCA 내용(근본원인·5 Whys·카테고리)이 문제 상세에 그대로 저장·표시되고, 특정 개인을 근본원인으로 지정하지 않아도 저장이 정상 완료됨
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-004), @docs/02_plan/screen/problem.md (SCR-PRB-003)

### TC-PRB-005 워크어라운드 등록
1. 문제 상세(SCR-PRB-003)에서 상태 전이 버튼으로 '알려진 오류 기록' → '워크어라운드' 단계까지 순서대로 전이
2. 워크어라운드 입력란에 임시 대응책 내용을 비워둔 채 저장 시도
3. 다시 임시 대응책 내용을 입력하고 저장
기대결과: 2번의 빈 내용 저장은 거부되고, 3번처럼 내용을 입력하면 워크어라운드가 문제 상세에 정상 저장·표시됨
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-006), @docs/01_analyze/feature/problem.md (FEAT-PRB-006), @docs/02_plan/screen/problem.md (SCR-PRB-003)

### TC-PRB-006 알려진 오류(KE) 생성 및 KEDB 키워드 검색
1. RCA와 워크어라운드가 저장된 문제 상세(SCR-PRB-003)에서 "알려진 오류 생성" 버튼 클릭해 KE 레코드 생성
2. 사이드바에서 KEDB 검색 화면(SCR-PRB-004)으로 이동
3. 검색바에 방금 생성한 KE와 연관된 키워드(예: 문제 요약에 포함된 단어)를 입력해 조회
4. 존재하지 않을 법한 임의 키워드로 다시 조회
기대결과: 1번에서 KE 레코드가 생성되고, 3번 조회 시 결과 목록에 해당 KE(제목·근본원인 요약·워크어라운드·연결 문제 포함)가 나타나며, 4번처럼 매칭이 없는 키워드는 빈 목록이 표시됨
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-005), @docs/02_plan/screen/problem.md (SCR-PRB-003, SCR-PRB-004)

### TC-PRB-007 인시던트 연결
1. 문제 상세(SCR-PRB-003)에서 "인시던트 연결" 버튼 클릭
2. 실제 존재하는 인시던트 식별키를 입력해 연결
3. 존재하지 않는 임의의 인시던트 식별키를 입력해 다시 연결 시도
기대결과: 2번은 정상적으로 연결되어 문제 상세에 연결된 인시던트가 양방향으로 표시되고, 3번은 오류 메시지와 함께 연결이 거부됨
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-007), @docs/02_plan/screen/problem.md (SCR-PRB-003)

### TC-PRB-008 변경 연계
1. 문제 상세(SCR-PRB-003)에서 "변경 연계" 버튼 클릭
2. 신규 변경을 생성하거나 기존 변경을 선택해 연계
기대결과: 문제 상세에 연계된 변경 항목이 표시됨(변경 상세에서도 해당 문제와의 연계 확인 가능)
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-008), @docs/02_plan/screen/problem.md (SCR-PRB-003)

### TC-PRB-009 후속 조치 추적 및 문제 해결·종료
1. 문제 상세(SCR-PRB-003)에서 후속 조치 항목을 하나 이상 등록하고 상태를 '진행중'으로 둔 채 종료 버튼 클릭
2. 미해결 후속 조치가 남아있다는 경고 다이얼로그가 표시되는지 확인
3. 후속 조치 상태를 '완료'로 변경한 뒤 다시 종료 버튼 클릭
4. 승인 패널이 나타나면(매칭되는 승인 프로세스가 있는 경우) admin@itsm.local 등 승인 권한 계정으로 로그인해 사이드바의 승인 대기함에서 해당 건을 승인 처리하고, 패널이 나타나지 않으면(승인 절차 없음) 즉시 전이되는지 확인
기대결과: 1번은 경고로 종료가 저지되고, 3번 이후 승인 절차가 있으면 승인 완료 후에만, 없으면 즉시 문제 상태가 '해결/종료'로 전환되고 이력에 기록됨
근거: @docs/01_analyze/prd/problem.md (REQ-PRB-009, REQ-PRB-010), @docs/02_plan/screen/problem.md (SCR-PRB-003), @docs/02_plan/screen/common.md (SCR-COM-014)

---

## 6. change (변경 관리)

### 사전 준비
- cm@itsm.local / `Admin@1234` (CHANGE_MANAGER), cab@itsm.local / `Admin@1234` (APPROVER, CAB 승인자) 계정으로 각각 로그인 가능해야 함
- CAB 승인이 필요한 TC는 cm@itsm.local로 변경을 생성·전이하고, cab@itsm.local로 로그아웃 후 재로그인해 승인 대기함에서 처리함

### TC-CHG-001 변경 요청(RFC) 등록
1. cm@itsm.local 로그인 후 사이드바에서 "변경" 메뉴 진입, 변경 목록(SCR-CHG-001)에서 등록 화면(SCR-CHG-002)으로 이동
2. 요약(필수)·설명·변경 유형(필수, 예: 일반)·위험도(예: 고)·예상 구현·영향 시스템·롤백 방법·예정 일정을 입력
3. 생성 버튼 클릭
기대결과: 변경이 생성되고 상세 화면으로 이동하며 상태가 '요청'으로 표시되고, 목록(SCR-CHG-001)에서도 유형·위험도 배지와 함께 조회됨
근거: @docs/01_analyze/prd/change.md (REQ-CHG-001, REQ-CHG-002), @docs/02_plan/screen/change.md (SCR-CHG-001, SCR-CHG-002)

### TC-CHG-002 RFC 등록 필수 항목 누락 시 오류
1. 변경 등록 화면(SCR-CHG-002)에서 요약 또는 변경 유형을 비운 채 나머지 항목만 입력
2. 생성 버튼 클릭
기대결과: 생성이 거부되고 오류 메시지가 표시되며 변경이 생성되지 않음
근거: @docs/01_analyze/prd/change.md (REQ-CHG-001 Unwanted), @docs/01_analyze/feature/change.md (FEAT-CHG-001), @docs/02_plan/screen/change.md (SCR-CHG-002)

### TC-CHG-003 표준 변경 사전승인 확인
1. 변경 등록 화면(SCR-CHG-002)에서 변경 유형으로 '표준'(사전승인 템플릿) 선택 후 필수 항목 입력, 생성
2. 화면에 승인 단계 생략 안내가 표시되는지 확인
3. 생성된 변경 상세(SCR-CHG-003)에서 별도 승인 절차 없이 계획/구현 단계로 상태 전이 시도
기대결과: 표준 변경은 승인 대기 없이 계획/구현 단계로 정상 전이됨
근거: @docs/01_analyze/prd/change.md (REQ-CHG-006), @docs/02_plan/screen/change.md (SCR-CHG-002, SCR-CHG-003)

### TC-CHG-004 CAB 승인 처리 (고위험 일반 변경)
1. TC-CHG-001에서 생성한 고위험 변경의 상세(SCR-CHG-003)에서 '검토' 단계로 전이해 승인 경로가 결정되도록 함
2. 승인 패널에 진행 상태(대기 차수)가 표시되는지 확인
3. cm@itsm.local 로그아웃 후 cab@itsm.local로 로그인, 사이드바의 승인 대기함(SCR-COM-014)에서 해당 변경 건을 조회
4. 행을 클릭해 역할별 결정 현황을 확인하고 승인 처리
5. cm@itsm.local로 재로그인해 변경 상세의 승인 패널이 '승인 완료'로 갱신되고 계획/구현 단계로 전이 가능한지 확인
기대결과: 고위험 변경은 CAB 승인 경로가 적용되며, 승인 대기함에서 승인 처리하면 변경 상세에 즉시 반영되고 이후 단계 전이가 가능해짐
근거: @docs/01_analyze/prd/change.md (REQ-CHG-004, REQ-CHG-005), @docs/02_plan/screen/change.md (SCR-CHG-003), @docs/02_plan/screen/common.md (SCR-COM-014)

### TC-CHG-005 CAB 반려 처리
1. cm@itsm.local로 새 일반/고위험 변경을 하나 더 등록하고 '검토' 단계로 전이
2. cab@itsm.local로 로그인해 승인 대기함(SCR-COM-014)에서 해당 건 조회 후 반려 처리 시 사유를 비운 채 반려 시도
3. 반려 사유를 입력하고 다시 반려 처리
4. cm@itsm.local로 재로그인해 변경 상세에서 반려 이력을 확인
기대결과: 2번은 사유 누락으로 오류가 표시되어 반려가 처리되지 않고, 3번은 반려가 정상 처리되어 변경 상세 이력에 반려 결정과 사유가 기록됨
근거: @docs/01_analyze/prd/change.md (REQ-CHG-005), @docs/02_plan/screen/common.md (SCR-COM-014)

### TC-CHG-006 승인 완료 전 구현 전이 차단
1. 아직 CAB 승인이 완료되지 않은(승인 대기 중인) 변경의 상세(SCR-CHG-003) 진입
2. 프로세스 상태 전이 버튼으로 곧바로 '구현' 단계 전이를 시도
기대결과: 승인이 완료되지 않았으므로 구현 단계 전이가 거부되고 오류 메시지가 표시됨
근거: @docs/01_analyze/prd/change.md (REQ-CHG-003 Unwanted), @docs/02_plan/screen/change.md (SCR-CHG-003)

### TC-CHG-007 구현 결과 기록 및 변경 일정 조회
1. TC-CHG-004에서 승인 완료된 변경 상세(SCR-CHG-003)에서 '계획' → '구현' 단계로 전이
2. 구현 결과 기록 폼에서 결과(성공 또는 실패)·롤백 여부·비고를 입력해 저장
3. 상태를 '종료'로 전이
4. 사이드바에서 변경 일정(캘린더) 화면(SCR-CHG-005)으로 이동해 예정 일정이 입력된 변경이 해당 날짜에 표시되는지 확인
기대결과: 구현 결과·롤백 여부가 변경 상세에 저장되고 상태가 '종료'로 전환되며, 변경 일정 화면에서 등록된 예정일에 해당 변경 항목이 조회됨
근거: @docs/01_analyze/prd/change.md (REQ-CHG-007, REQ-CHG-008), @docs/02_plan/screen/change.md (SCR-CHG-003, SCR-CHG-005)

### TC-CHG-008 인시던트/문제 연계 및 지표 대시보드 확인
1. 변경 상세(SCR-CHG-003)에서 "인시던트/문제 연계" 버튼으로 실제 존재하는 인시던트(또는 문제) 식별키를 입력해 연결
2. 존재하지 않는 임의 식별키로 다시 연결 시도
3. 사이드바에서 변경 지표 대시보드(SCR-CHG-006)로 이동, 기간 필터를 조회 기간으로 설정
기대결과: 1번은 정상 연결되어 변경 상세에 연결 항목이 표시되고, 2번은 오류 메시지와 함께 연결이 거부됨. 3번에서는 성공률·실패율·긴급 변경 비율 KPI 카드가 집계되어 표시됨(데이터가 없는 기간이면 빈 결과가 표시됨)
근거: @docs/01_analyze/prd/change.md (REQ-CHG-009, REQ-CHG-010), @docs/02_plan/screen/change.md (SCR-CHG-003, SCR-CHG-006)

---

## 7. knowledge (지식베이스)

### 사전 준비
- kc@itsm.local(KNOWLEDGE_CONTRIBUTOR, 작성자), kg@itsm.local(KNOWLEDGE_GATEKEEPER, 게시 승인자) 계정, 공통 비밀번호 `Admin@1234`.
- 지식(knowledge) 도메인에 게시 승인 프로세스가 사전 구성되어 있어야 한다(관리자 설정). 미구성 상태라면 "검토 요청" 클릭 시 승인 절차 없이 즉시 게시(PUBLISHED)되므로, 아래 승인/반려 관련 TC(TC-KM-003, TC-KM-004)는 승인 프로세스가 구성된 상태를 전제로 한다.

### TC-KM-001 지식 기사 작성 및 초안 저장
1. kc@itsm.local 로그인
2. 사이드바 "지식" 메뉴에서 기사 작성·편집 화면으로 이동, 제목·본문·카테고리 입력 후 저장(검토 요청 없이 저장만 클릭)
기대결과: 기사가 초안(DRAFT) 상태로 생성되어 목록/작성 화면에 표시됨
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-001), @docs/02_plan/screen/knowledge.md (SCR-KM-003)

### TC-KM-002 필수 입력값 누락 시 저장 거부
1. kc@itsm.local 로그인 상태에서 기사 작성 화면 진입
2. 제목 또는 본문을 비운 채 저장 클릭
기대결과: 저장이 거부되고 오류 안내가 표시되며 기사가 생성되지 않음
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-001 Unwanted), @docs/02_plan/screen/knowledge.md (SCR-KM-003)

### TC-KM-003 검토 요청 및 게이트키퍼 승인 → 게시
1. kc@itsm.local 로그인, 작성한 초안 기사에서 "검토 요청" 버튼 클릭
2. (1차 확인) 기사 상태가 검토(IN_REVIEW)로 전환되고 작성 화면의 승인 패널에 진행 상태가 표시되는지 확인
3. kg@itsm.local 로그인, "승인 대기함" 화면으로 이동해 해당 지식 건 선택 후 승인 처리
4. kc@itsm.local로 재로그인해 해당 기사의 작성 화면에서 상태 확인
기대결과: 승인 처리 즉시 승인 대기함 목록에서 해당 건이 사라지고, 기사 상태가 게시(PUBLISHED)로 전환됨
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-002/003), @docs/02_plan/screen/knowledge.md (SCR-KM-003, SCR-COM-014)

### TC-KM-004 게이트키퍼 반려 → 초안 복귀
1. kc@itsm.local 로그인, 새 기사 작성 후 "검토 요청" 클릭(검토 상태 전환)
2. kg@itsm.local 로그인, "승인 대기함"에서 해당 건을 반려 처리(반려 사유 입력)
3. kc@itsm.local로 재로그인해 해당 기사 작성 화면 확인
기대결과: 기사가 초안(DRAFT) 상태로 되돌아가고, 작성 화면 승인 패널에 반려 사유가 표시됨
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-003 Unwanted), @docs/02_plan/screen/knowledge.md (SCR-KM-003, SCR-COM-014)

### TC-KM-005 키워드 검색(결과 있음)
1. kc@itsm.local 로그인(또는 게시 기사를 열람할 수 있는 임의 계정)
2. 지식베이스 검색/목록 화면에서 게시된 기사 제목에 포함된 키워드로 검색
기대결과: 해당 키워드가 매칭되는 기사가 목록에 표시됨(상태 배지 확인 가능)
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-004), @docs/02_plan/screen/knowledge.md (SCR-KM-001)

### TC-KM-006 무결과 검색
1. 지식베이스 검색/목록 화면에서 존재하지 않을 법한 임의 키워드(예: 무작위 문자열)로 검색
기대결과: 빈 결과가 표시됨("검색 결과 없음" 등 안내)
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-004 Unwanted), @docs/02_plan/screen/knowledge.md (SCR-KM-001)

### TC-KM-007 게시 기사 열람 및 유용성 평가
1. 게시된 기사를 목록에서 클릭해 기사 열람(셀프서비스) 화면 진입
2. 하단 "도움이 되었나요?" 위젯에서 "예"(또는 "아니오") 클릭 후 코멘트 입력
기대결과: 평가가 저장되고 토스트로 결과가 안내됨
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-006), @docs/02_plan/screen/knowledge.md (SCR-KM-002)

### TC-KM-008 지식 지표 대시보드 조회
1. 지식 지표 대시보드 화면으로 이동
2. 기간 필터 조회 후 KPI 카드(사용량·무결과 검색·유용성·차단율)와 무결과 키워드 랭킹 확인
기대결과: 집계된 지표 값이 표시되고, 앞선 TC-KM-006에서 발생한 무결과 검색 키워드가 랭킹에 반영됨
근거: @docs/01_analyze/prd/knowledge.md (REQ-KM-009), @docs/02_plan/screen/knowledge.md (SCR-KM-005)

---

## 8. asset (IT 자산/CMDB)

### 사전 준비
- am@itsm.local(ASSET_MANAGER) 계정, 공통 비밀번호 `Admin@1234`.
- 자산 폐기 전이에 대한 승인 프로세스가 사전 구성되어 있어야 한다(관리자 설정). 미구성 상태라면 폐기 버튼 클릭 시 승인 절차 없이 즉시 폐기 전환되므로, 아래 TC-ITAM-004/005는 승인 프로세스가 구성된 상태를 전제로 하며 승인 처리 계정은 시스템에 설정된 승인자(예시로 admin@itsm.local 사용)로 로그인해 처리한다.

### TC-ITAM-001 자산 등록
1. am@itsm.local 로그인, 사이드바 "자산" 메뉴에서 자산 목록 화면 진입 후 "자산 등록" 버튼 클릭
2. 유형(HW/SW/클라우드 중 택1) 선택 후 이름·소유자·위치·구매일·비용 등 입력, 저장
기대결과: 자산이 목록에 표시되고 지정한 유형·상태 배지가 노출됨
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-001/003), @docs/02_plan/screen/asset.md (SCR-ITAM-001, SCR-ITAM-002)

### TC-ITAM-002 필수값 누락 시 등록 거부
1. am@itsm.local 로그인 상태에서 자산 등록 화면 진입
2. 이름 또는 유형을 비운 채 저장 클릭
기대결과: 저장이 거부되고 오류 안내가 표시되며 자산이 생성되지 않음
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-001 Unwanted), @docs/02_plan/screen/asset.md (SCR-ITAM-002)

### TC-ITAM-003 자산 생애주기 단계 전이
1. am@itsm.local 로그인, TC-ITAM-001에서 등록한 자산의 상세 화면 진입
2. 생애주기 단계 전이 버튼으로 다음 허용 단계(예: 조달→운영)로 전이
기대결과: 자산 상태가 갱신되고 좌측 생애주기 이력에 전이 내역이 기록됨
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-002), @docs/02_plan/screen/asset.md (SCR-ITAM-003)

### TC-ITAM-004 자산 폐기 요청 → 승인 → 폐기 완료
1. am@itsm.local 로그인, 자산 상세 화면에서 "폐기" 버튼 클릭(확인 다이얼로그 확인)
2. (1차 확인) 매칭되는 승인 프로세스가 있으면 자산이 즉시 폐기되지 않고, 상세 화면에 승인 패널(진행 상태)이 나타나는지 확인
3. 승인자 계정(예: admin@itsm.local)으로 로그인, "승인 대기함"에서 해당 자산 폐기 건을 승인 처리
4. am@itsm.local로 재로그인해 해당 자산 상세 화면 확인
기대결과: 승인 완료 후 자산 상태가 폐기로 전환됨
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-001/002), @docs/02_plan/screen/asset.md (SCR-ITAM-003, SCR-COM-014)

### TC-ITAM-005 자산 폐기 반려
1. am@itsm.local 로그인, 다른 자산 상세 화면에서 "폐기" 버튼 클릭해 승인 대기 상태로 전환
2. 승인자 계정(예: admin@itsm.local)으로 로그인, "승인 대기함"에서 해당 건을 반려 처리(반려 사유 입력)
3. am@itsm.local로 재로그인해 해당 자산 상세 화면 확인
기대결과: 자산이 폐기로 전환되지 않고 기존 생애주기 단계가 유지되며, 상세 화면 승인 패널에 반려 사유가 표시됨
근거: @docs/02_plan/screen/asset.md (SCR-ITAM-003, SCR-COM-014)

### TC-ITAM-006 만료 임박 자산 조회
1. am@itsm.local 로그인, 자산 등록/수정 화면에서 자산의 라이선스(또는 보증/계약) 만료일을 임박한 날짜로 설정 후 저장
2. 자산 목록 화면에서 "만료 임박" 필터로 조회
기대결과: 해당 자산이 목록에 만료 임박 배지(Warning)와 함께 표시되고, 헤더 알림 벨의 알림 드롭다운에도 "자산 만료" 항목으로 노출됨
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-004), @docs/02_plan/screen/asset.md (SCR-ITAM-001), @docs/02_plan/screen/common.md (SCR-COM-002)

### TC-ITAM-007 CI 등록·의존 관계 설정 및 영향 범위 조회
1. am@itsm.local 로그인, "CI·CMDB 관계 뷰" 화면에서 CI 2건을 등록
2. 한 CI를 선택해 관계 추가 폼으로 다른 CI와의 의존 관계를 지정
3. 관계를 설정한 CI를 선택해 우측 영향 범위 패널 확인
기대결과: 좌측 CI 목록에 등록한 CI가 표시되고, 관계 그래프에 의존 노드가 나타나며, 영향 범위 패널에 연결된 CI 목록이 표시됨
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-005/007), @docs/02_plan/screen/asset.md (SCR-ITAM-004)

### TC-ITAM-008 자산 지표 대시보드 조회
1. am@itsm.local 로그인, 자산 지표 대시보드 화면으로 이동
2. 기간 필터 조회 후 KPI 카드(활용률·만료 임박)와 유형 분포 차트 확인
기대결과: 집계된 활용률·만료 임박 건수·유형별(HW/SW/클라우드) 분포가 표시됨
근거: @docs/01_analyze/prd/asset.md (REQ-ITAM-008), @docs/02_plan/screen/asset.md (SCR-ITAM-005)

---

## 9. esm (엔터프라이즈 서비스 관리)

### 사전 준비
- 계정: `po@itsm.local`(PROCESS_OWNER, 요청 제출·카탈로그 관리 겸용) / `hr@itsm.local`(HR_CASE_MANAGER) / `legal-coord@itsm.local`, `facilities-coord@itsm.local`, `it-coord@itsm.local`(DEPT_COORDINATOR) / 공통 비밀번호 `Admin@1234`
- 온보딩/오프보딩 시나리오(TC-ESM-006, 007)를 수행하려면 사전에 `po@itsm.local`로 부서별 카탈로그 관리 화면에서 온보딩/오프보딩 유형 카탈로그 항목에 체크리스트 템플릿(IT/시설/HR 등 하위 작업)이 정의되어 있어야 한다. 정의되어 있지 않으면 TC-ESM-008을 먼저 수행해 템플릿을 구성한다.

### TC-ESM-001 부서 요청 제출(법무)
1. `po@itsm.local` 로그인 후 부서 서비스 포털 진입
2. 상단 "법무" 부서 탭 선택 → 카탈로그 카드 중 하나 선택
3. 동적 양식의 필수 필드를 입력하고 제출 버튼 클릭
기대결과: 요청이 생성되어 법무 부서 처리 큐로 라우팅되고, "내 부서 요청 목록"에서 방금 제출한 요청이 상태 "제출"로 조회됨
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-002), @docs/02_plan/screen/esm.md (SCR-ESM-001, SCR-ESM-002, SCR-ESM-003)

### TC-ESM-002 부서 요청 처리(완료 처리)
1. `legal-coord@itsm.local` 로그인 후 부서 요청 처리 큐 진입, 부서 필터를 "법무"로 설정
2. TC-ESM-001에서 제출된 요청을 목록에서 선택해 상세 화면 진입
3. 상태 전이 버튼으로 "처리중" → "완료"로 순서대로 전이
기대결과: 상태 배지가 완료(Success)로 변경되고, `po@itsm.local`로 재로그인해 "내 부서 요청 목록"에서 동일 요청의 상태가 완료로 표시됨
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-002), @docs/02_plan/screen/esm.md (SCR-ESM-004, SCR-ESM-005)

### TC-ESM-003 부서 요청 반려 처리(예외)
1. `po@itsm.local` 로그인 후 법무 부서 카탈로그 항목을 선택해 신규 요청 제출
2. `legal-coord@itsm.local` 로그인 후 처리 큐에서 해당 요청 선택, 상세 화면에서 코멘트를 남기고 반려 버튼 클릭
기대결과: 요청 상태가 "반려"(Danger)로 표시되고, `po@itsm.local`로 재로그인해 "내 부서 요청 목록"에서 반려 상태와 코멘트를 확인 가능
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-002), @docs/02_plan/screen/esm.md (SCR-ESM-005)

### TC-ESM-004 HR 케이스 접수→기록→조사→해결 처리
1. `hr@itsm.local` 로그인 후 HR 케이스 목록 화면 진입, "케이스 접수" 버튼 클릭해 신규 케이스 생성
2. 케이스 상세 화면에서 상태 전이 버튼을 이용해 접수 → 기록 → 조사 → 해결 순서로 전이
기대결과: 각 단계 전이마다 상태 이력 타임라인에 기록되고, 최종적으로 케이스 상태가 "해결"로 표시됨
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-003), @docs/02_plan/screen/esm.md (SCR-ESM-007, SCR-ESM-008)

### TC-ESM-005 HR 케이스 권한없음(예외)
1. `it-coord@itsm.local`(또는 `legal-coord@itsm.local`) 로그인
2. 사이드바 메뉴에서 HR 케이스 메뉴 노출 여부 확인, 브라우저 주소창에 HR 케이스 목록 화면 URL을 직접 입력해 접속 시도
기대결과: 사이드바에 HR 케이스 메뉴가 노출되지 않으며, 직접 접속 시도 시에도 접근이 거부됨(403)
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-004), @docs/02_plan/screen/esm.md (SCR-ESM-007)

### TC-ESM-006 온보딩 체크리스트 자동 생성 및 하위 작업 처리
1. `po@itsm.local` 로그인 후 HR 부서 탭에서 신규입사(온보딩) 관련 카탈로그 항목 선택해 양식 작성 후 제출
2. 제출 성공 시 노출되는 체크리스트 자동 생성 안내 토스트 확인, 요청 상세 화면에서 연계 체크리스트 카드 클릭해 체크리스트 상세로 이동
3. `it-coord@itsm.local` 로그인 후 "내 하위 작업 목록"에서 본인 담당 온보딩 하위 작업을 확인하고 완료 처리 버튼 클릭
4. `facilities-coord@itsm.local` 로그인 후 동일하게 본인 담당 하위 작업을 확인하고 완료 처리
기대결과: 체크리스트 상세에서 IT/시설 하위 작업은 완료로 표시되나 HR 하위 작업이 남아있어 전체 진행률은 "진행중"(Warning)으로 유지됨(모든 하위 작업 완료 전에는 전체 완료로 전환되지 않음)
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-005, REQ-ESM-007), @docs/02_plan/screen/esm.md (SCR-ESM-002, SCR-ESM-005, SCR-ESM-009, SCR-ESM-010)

### TC-ESM-007 오프보딩 체크리스트 자산 회수 확인
1. `po@itsm.local` 로그인 후 HR 부서 탭에서 퇴사(오프보딩) 관련 카탈로그 항목 선택해 제출
2. 요청 상세 화면에서 연계 체크리스트 카드 클릭해 체크리스트 상세 이동
3. 하위 작업 표에서 자산 회수 관련 하위 작업과 회수 대상 자산 링크 확인(연결된 자산이 있는 경우 자산 상세로 이동 확인), `it-coord@itsm.local` 로그인 후 "내 하위 작업 목록"에서 본인 담당 접근 권한 해제 하위 작업 완료 처리
기대결과: 대상자에게 연결된 자산이 있으면 회수 자산명·링크가 표시되고 클릭 시 자산 상세로 이동하며, 없으면 자산 회수 하위 작업 없이 체크리스트가 생성됨. IT 하위 작업 완료 처리 후 해당 작업 상태가 완료로 갱신됨
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-006), @docs/02_plan/screen/esm.md (SCR-ESM-009, SCR-ESM-010)

### TC-ESM-008 부서별 카탈로그 관리(담당 부서 필수 검증)
1. `po@itsm.local` 로그인 후 부서별 카탈로그 관리 화면 진입, 신규 항목 추가 시 담당 부서를 지정하지 않고 저장 시도
2. 저장 거부(오류) 확인 후, 담당 부서(예: 시설)를 선택하고 이름·설명·양식 필드를 입력해 다시 저장
기대결과: 1단계는 저장이 거부되고, 2단계는 카탈로그 항목이 정상 등록되어 목록에 노출됨
근거: @docs/01_analyze/prd/esm.md(REQ-ESM-001), @docs/02_plan/screen/esm.md (SCR-ESM-006)

---

## 10. vulnerability (취약점 관리)

### 사전 준비
- 계정: `vm@itsm.local`(VULNERABILITY_MANAGER), 필요 시 `admin@itsm.local`(SYSTEM_ADMIN) / 공통 비밀번호 `Admin@1234`
- 자산/CI 연계 시나리오(TC-VULN-008)를 수행하려면 자산(ITAM) 도메인에 연계 가능한 자산이 최소 1건 등록되어 있어야 한다.
- 승인 게이트 확인(TC-VULN-005)은 우선순위화→개선 전이에 매칭되는 승인 프로세스가 설정되어 있는지 여부에 따라 결과가 달라진다(설정 여부는 사전 확인 불필요, 화면에 나타나는 결과로 판단).

### TC-VULN-001 취약점 등록(정상/필수 필드 누락)
1. `vm@itsm.local` 로그인 후 취약점 등록 화면 진입
2. 제목과 심각도를 비운 채 등록 버튼 클릭
3. 오류 확인 후 제목·심각도(예: HIGH)·발견일·영향 자산/CI를 입력해 다시 등록
기대결과: 2단계는 등록이 거부되고, 3단계는 취약점이 생성되어 상세 화면으로 이동하며 초기 상태가 "발견"으로 표시됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-001), @docs/02_plan/screen/vulnerability.md (SCR-VULN-002)

### TC-VULN-002 리스크 스코어 산정 및 정렬 확인
1. `vm@itsm.local` 로그인 후 TC-VULN-001에서 등록한 취약점 상세 화면 진입
2. 리스크 스코어 산정 폼에서 심각도·악용 가능성 값을 선택해 점수 계산
3. 취약점 목록 화면으로 이동해 리스크 스코어 컬럼 기준 정렬 상태 확인
기대결과: 상세 화면에 리스크 스코어가 계산·표시되고, 목록 화면은 기본적으로 리스크 스코어 내림차순으로 정렬되어 해당 취약점을 상단 근처에서 확인 가능
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-003), @docs/02_plan/screen/vulnerability.md (SCR-VULN-001, SCR-VULN-003)

### TC-VULN-003 라이프사이클 순차 전이(발견→평가→우선순위화)
1. `vm@itsm.local` 로그인 후 취약점 상세 화면에서 상태 전이 버튼으로 "발견" → "평가" → "우선순위화" 순서로 전이
기대결과: 각 전이 후 상태 배지가 순서대로 갱신되고 상태 이력(타임라인)에 전이 내역이 기록됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-002), @docs/02_plan/screen/vulnerability.md (SCR-VULN-003)

### TC-VULN-004 담당자 미배정 상태 개선 전이 거부(예외)
1. `vm@itsm.local` 로그인 후 담당자가 배정되지 않은 취약점(우선순위화 상태)의 상세 화면 진입
2. 담당자 배정 없이 "개선" 단계로 상태 전이 시도
기대결과: 전이가 거부되고 담당자 미배정으로 인한 오류가 표시됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-004), @docs/02_plan/screen/vulnerability.md (SCR-VULN-003)

### TC-VULN-005 담당자 배정 및 개선 단계 전이(승인 게이트 확인)
1. `vm@itsm.local` 로그인 후 취약점 상세 화면에서 담당자 배정(셀렉트+버튼)으로 담당자 지정
2. "개선" 단계로 상태 전이 시도
기대결과: 매칭되는 승인 프로세스가 있으면 즉시 전이되지 않고 승인 패널이 나타나 차수 진행 상태가 표시되며, 매칭되는 승인 프로세스가 없으면 즉시 "개선" 상태로 전이됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-004), @docs/02_plan/screen/vulnerability.md (SCR-VULN-003)

### TC-VULN-006 개선 조치 기록
1. `vm@itsm.local` 로그인 후 "개선" 상태인 취약점 상세 화면에서 개선 조치 등록 폼에 조치 유형(패치/구성변경/보완통제)·설명·조치일 입력 후 저장
기대결과: 등록한 개선 조치 내용이 취약점 상세에 기록·표시됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-005), @docs/02_plan/screen/vulnerability.md (SCR-VULN-003)

### TC-VULN-007 검증 실패→개선 복귀→재검증 통과→종료
1. `vm@itsm.local` 로그인 후 개선 조치가 기록된 취약점 상세 화면에서 검증 결과 등록 버튼으로 "실패" 등록
2. 상태가 "개선"으로 되돌아간 것을 확인
3. 다시 검증 결과 등록 버튼으로 "통과" 등록
기대결과: 1단계 이후 상태가 자동으로 "개선"으로 복귀하고, 3단계 이후 취약점이 "보고"(종료) 상태로 자동 전이되며 Success 색상으로 표시됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-006), @docs/02_plan/screen/vulnerability.md (SCR-VULN-003)

### TC-VULN-008 자산/CI 연계 확인
1. `vm@itsm.local` 로그인 후 취약점 상세 화면에서 자산/CI 연계 버튼 클릭
2. 기존에 등록된 자산을 검색·선택해 연결
기대결과: 연결된 자산/CI가 취약점 상세의 연계 자산/CI 영역에 표시됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-007), @docs/02_plan/screen/vulnerability.md (SCR-VULN-003)

### TC-VULN-009 취약점 지표 대시보드 확인
1. `vm@itsm.local` 로그인 후 취약점 지표 대시보드 화면 진입
2. 기간 필터를 변경하며 KPI 카드(단계별 건수·심각도 분포·평균 해결시간) 값 확인
기대결과: 선택한 기간에 데이터가 있으면 집계값이 표시되고, 데이터가 없는 기간을 선택하면 0값/빈 결과가 표시됨
근거: @docs/01_analyze/prd/vulnerability.md(REQ-VULN-008), @docs/02_plan/screen/vulnerability.md (SCR-VULN-004)

---

## 11. compliance (컴플라이언스 관리)

### 사전 준비
- co@itsm.local / `Admin@1234` 로 로그인해 사용(권한: COMPLIANCE_OFFICER)
- 시정조치 상태 전이(해결) 테스트를 위해 사전에 임의의 컴플라이언스 요구사항 1건과 시정조치 항목 1건이 등록되어 있으면 편리하나, 없어도 TC 진행 중 직접 등록 가능

### TC-COMP-001 요구사항 등록
1. co@itsm.local 로그인
2. 요구사항 등록 화면에서 이름·근거(규제/정책) 입력 후 등록 버튼 클릭
기대결과: 요구사항이 생성되고 상세 화면으로 이동하며, 목록에서도 새 요구사항이 조회됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-001), @docs/02_plan/screen/compliance.md (SCR-COMP-002)

### TC-COMP-002 요구사항 등록 시 필수 항목 누락
1. co@itsm.local 로그인
2. 요구사항 등록 화면에서 이름 또는 근거를 비운 채 등록 버튼 클릭
기대결과: 등록이 거부되고 오류가 표시됨(요구사항이 생성되지 않음)
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-001 Unwanted), @docs/02_plan/screen/compliance.md (SCR-COMP-002)

### TC-COMP-003 책임자 지정
1. co@itsm.local 로그인 후 임의의 요구사항 상세 화면 진입
2. 책임자 셀렉트에서 담당자를 선택 후 지정 버튼 클릭
기대결과: 지정 내역이 상세 화면에 표시되고, 목록에서도 해당 요구사항의 책임자로 조회됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-002), @docs/02_plan/screen/compliance.md (SCR-COMP-003)

### TC-COMP-004 책임자 미지정 표시 확인
1. co@itsm.local 로그인
2. 책임자가 아직 지정되지 않은 요구사항을 목록에서 확인
기대결과: 해당 요구사항 행에 "책임자 미지정" 배지(Danger)가 표시됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-002 Unwanted), @docs/02_plan/screen/compliance.md (SCR-COMP-001)

### TC-COMP-005 시정조치 등록
1. co@itsm.local 로그인 후 요구사항 상세 화면 진입
2. 시정조치 등록 폼에 내용을 입력 후 저장
기대결과: 시정조치 항목이 "탐지(Detected)" 상태로 목록에 추가됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-003), @docs/02_plan/screen/compliance.md (SCR-COMP-003)

### TC-COMP-006 시정조치 상태 전이(탐지→조치중→해결)
1. co@itsm.local 로그인 후 시정조치 항목이 있는 요구사항 상세 화면 진입
2. 대상 시정조치의 상태 전이 버튼으로 "조치중"으로 전이
3. 이어서 "해결"로 전이 시도
기대결과: 조치중 전이는 정상 반영되며 이력이 표시됨. 해결 전이 시 매칭되는 승인 프로세스가 없으면 즉시 "해결"로 전이되고, 매칭되는 승인 프로세스가 있으면 전이가 보류되며 해당 항목 옆에 승인 진행 상태 패널이 표시됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-003), @docs/02_plan/screen/compliance.md (SCR-COMP-003)

### TC-COMP-007 변경 요청 연계
1. co@itsm.local 로그인 후 요구사항 상세 화면 진입
2. 변경 연계 버튼으로 기존 변경 요청 ID를 입력해 연결
기대결과: 연결된 변경 요청이 상세 화면의 메타 영역에 표시됨. 존재하지 않는 변경 요청 ID로 연결 시도 시 오류가 표시되고 연결되지 않음
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-005), @docs/02_plan/screen/compliance.md (SCR-COMP-003)

### TC-COMP-008 감사 로그 조회
1. co@itsm.local 로그인 후 임의의 요구사항 상세 화면 진입(사전에 등록/상태 전이 등 활동이 있는 요구사항)
2. 감사 로그 목록 영역 확인
기대결과: 요구사항 등록·수정·상태 전이·시정조치 처리 등 수행 이력이 수행자·시각과 함께 표시됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-004), @docs/02_plan/screen/compliance.md (SCR-COMP-003)

### TC-COMP-009 준수 현황 대시보드 확인
1. co@itsm.local 로그인 후 준수 현황 대시보드 화면 진입
2. 준수율·미해결 시정조치 건수 KPI 카드와 요구사항별 상태 표 확인
기대결과: 준수율(Success/Danger)·미해결 건수가 집계되어 표시되고, 요구사항별 상태 표에 책임자·준수 상태가 정상 표시됨
근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-006), @docs/02_plan/screen/compliance.md (SCR-COMP-004)

---

## 12. infra-monitoring (인프라 모니터링)

### 사전 준비
- io@itsm.local / `Admin@1234` 로 로그인해 사용(권한: INFRA_OPERATOR)
- 임계치 초과 알림 테스트를 위해 사전에 대상 지표 항목에 임계치가 설정되어 있으면 편리하나, 없으면 TC 진행 중 직접 설정 가능

### TC-IOM-001 인프라 지표 등록
1. io@itsm.local 로그인
2. 인프라 지표 등록 화면에서 대상 자산 선택, 측정 시각, 지표 항목(UPTIME/CPU/MEMORY/RESPONSE_TIME) 및 값 입력 후 등록
기대결과: 지표 레코드가 생성되고 대시보드에서 해당 값이 조회됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-001), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-001)

### TC-IOM-002 지표 등록 시 필수 항목 누락
1. io@itsm.local 로그인
2. 인프라 지표 등록 화면에서 대상 자산 또는 값을 비운 채 등록 시도
기대결과: 등록이 거부되고 오류가 표시됨(지표 레코드가 생성되지 않음)
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-001 Unwanted), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-001)

### TC-IOM-003 지표 대시보드 시계열 조회
1. io@itsm.local 로그인 후 지표 대시보드 화면 진입
2. 자산·기간을 선택해 시계열 차트 확인
기대결과: 등록된 지표가 측정 시각 순으로 차트에 표시됨. 조회 기간 내 등록된 지표가 없으면 빈 결과가 표시됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-002), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-002)

### TC-IOM-004 SLA 대비 가동률 비교 확인
1. io@itsm.local 로그인 후 지표 대시보드 화면에서 가동률 목표(SLA)가 설정된 자산 선택
2. SLA 대비 가동률 카드 확인
기대결과: 목표%·실제%와 달성 여부(Success/Danger)가 표시됨. 목표가 설정되지 않은 자산은 실제 가동률만 표시됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-005), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-002)

### TC-IOM-005 지표 임계치 설정
1. io@itsm.local 로그인 후 임계치 설정·알림 목록 화면 진입
2. 지표 항목을 선택해 상한/하한 값을 입력 후 저장
기대결과: 설정한 임계치가 저장되어 화면에 반영됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-003), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-003)

### TC-IOM-006 임계치 초과 시 알림 생성 확인
1. io@itsm.local 로그인 후 인프라 지표 등록 화면에서 임계치가 설정된 지표 항목에 임계치를 벗어나는 값을 입력해 등록
2. 등록 후 표시되는 토스트 확인 및 임계치 설정·알림 목록 화면에서 알림 목록 확인
기대결과: "임계치 초과 알림이 생성되었습니다" 토스트가 표시되고, 알림 목록에 해당 자산·지표·초과값·발생시각이 추가됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-003), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-001, SCR-IOM-003)

### TC-IOM-007 알림 확인 처리
1. io@itsm.local 로그인 후 임계치 설정·알림 목록 화면에서 미확인 알림 선택
2. 확인 처리 버튼 클릭
기대결과: 해당 알림이 확인 처리되어 목록에서 흐리게(비활성) 표시됨
근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-003)

### TC-IOM-008 용량 계획 등록 및 활용률 확인
1. io@itsm.local 로그인 후 용량 계획 관리 화면 진입
2. 팀/서비스명·처리 역량·예상 수요 입력 후 등록
기대결과: 용량 계획이 목록에 추가되고 활용률(수요/역량 비율) 배지가 표시됨(100% 초과 시 Danger 강조). 역량 또는 수요 누락 시 등록이 거부됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-004), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-004)

### TC-IOM-009 인프라 지표 리포팅 조회
1. io@itsm.local 로그인 후 인프라 지표 리포팅 화면 진입
2. 기간·자산 필터 설정 후 KPI 카드 확인
기대결과: 평균 가동률·평균 성능 지표·평균 용량 활용률이 집계되어 표시됨. 데이터가 없으면 빈 결과가 표시됨
근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-006), @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-005)

---

## 13. ui-revamp (UI 개편)

### 사전 준비
- `admin@itsm.local` / `user@itsm.local` 계정 모두 비밀번호는 `Admin@1234`
- admin은 SYSTEM_ADMIN, user는 END_USER 역할이며, 사이드바 메뉴는 역할별로 다르게 노출됨(관리자 메뉴는 admin에게만 노출)

### TC-UI-001 앱 셸(헤더·사이드바·푸터) 레이아웃 확인
1. `admin@itsm.local`로 로그인
2. 로그인 후 기본 화면(대시보드)에서 상단 헤더, 좌측 사이드바, 하단 푸터가 모두 표시되는지 확인
기대결과: 상단 고정 헤더 + 좌측 사이드바(펼침 상태) + 우측 콘텐츠 + 하단 푸터 구조가 깨짐 없이 표시된다. 사이드바 배경은 진한 남색 계열, 헤더/콘텐츠 배경은 흰색 계열로 표시된다.
근거: @docs/02_plan/screen/common.md (SCR-COM-001)

### TC-UI-002 사이드바 접기/펼치기 및 폭·폰트 확인
1. `admin@itsm.local`로 로그인한 상태에서 사이드바 상단(또는 헤더)의 사이드바 토글 버튼 클릭
2. 사이드바가 접힌 좁은 상태로 전환되는지, 메뉴 라벨 텍스트가 사라지고 아이콘만 남는지 확인
3. 다시 토글 버튼을 클릭해 펼침 상태로 복귀시키고 메뉴 라벨이 다시 나타나는지 확인
4. 메뉴 그룹 헤더(예: 대문자 그룹명)와 메뉴 항목 라벨의 글자 크기가 작게(그룹헤더가 메뉴 라벨보다 더 작게) 표시되는지 확인
기대결과: 펼침↔접힘 전환이 부드럽게(순간적으로 어긋나거나 깨지지 않고) 이루어지고, 접힘 상태에서는 아이콘만 남으며 라벨이 잘리거나 겹치지 않는다. 펼침 상태에서 폭이 지나치게 넓거나 좁지 않고, 그룹 헤더 라벨이 메뉴 항목 라벨보다 시각적으로 더 작다.
근거: @docs/02_plan/screen/common.md (SCR-COM-003, 2.6절 모션)

### TC-UI-003 다크모드 토글 및 유지 확인
1. `admin@itsm.local`로 로그인한 상태에서 헤더의 테마 토글(달/해 아이콘) 버튼 클릭
2. 화면 전체(헤더·사이드바·콘텐츠·카드·텍스트)가 다크 색상으로 즉시 전환되는지 확인
3. 브라우저를 새로고침(F5)하여 다크 테마가 유지되는지 확인
4. 다시 토글을 클릭해 라이트 테마로 되돌리고 정상 전환되는지 확인
기대결과: 토글 클릭 시 배경·텍스트·테두리·상태 색상이 즉시 다크 값으로 전환되며 깨지거나 이전 라이트 색상이 남아있는 부분이 없다. 새로고침 후에도 마지막에 선택한 테마가 그대로 유지된다.
근거: @docs/01_analyze/prd/ui-revamp.md (REQ-UIX-002), @docs/02_plan/screen/common.md (SCR-COM-010)

### TC-UI-004 역할별 사이드바 메뉴 차이 확인
1. `admin@itsm.local`로 로그인해 사이드바 메뉴 목록(관리자 메뉴 그룹 포함 여부) 확인 후 로그아웃
2. `user@itsm.local`로 로그인해 사이드바 메뉴 목록 확인
기대결과: admin 계정에서는 관리자 전용 메뉴 그룹이 사이드바에 노출되고, user(END_USER) 계정에서는 관리자 메뉴가 보이지 않으며 이용 가능한 메뉴(서비스요청·지식 등 포털 성격 메뉴)만 표시된다. 두 계정 모두 레이아웃 구조(헤더·사이드바·푸터)와 디자인(색상·폰트·간격)은 동일하게 보인다.
근거: @docs/02_plan/screen/common.md (SCR-COM-003)

### TC-UI-005 브라우저 창 크기 조절 시 반응형 확인
1. `admin@itsm.local`로 로그인한 상태에서 브라우저 창을 최대화 크기로 확인
2. 브라우저 창 폭을 점진적으로 줄여가며(예: 절반 크기) 헤더·사이드바·콘텐츠 영역의 레이아웃이 깨지지 않는지 확인
3. 목록 화면(예: 서비스 요청 목록 등 표가 있는 화면)으로 이동해 창을 좁혔을 때 표·필터바가 겹치거나 잘리지 않는지 확인
기대결과: 창 크기를 줄여도 헤더·사이드바·콘텐츠 영역이 서로 겹치지 않고, 표 컬럼이나 버튼이 화면 밖으로 밀려나거나 뭉개지지 않는다.
근거: @docs/01_analyze/prd/ui-revamp.md (범위 — 레이아웃 셸 비주얼 정렬), @docs/02_plan/screen/common.md (SCR-COM-001)

### TC-UI-006 상태·우선순위 배지(Lozenge) 표시 확인
1. `admin@itsm.local`로 로그인해 티켓 목록 화면(예: 서비스 요청 목록, 인시던트 목록 등 상태·우선순위 컬럼이 있는 화면) 진입
2. 목록의 상태 배지, 우선순위 배지 모양(모서리 각짐 정도)과 테두리 유무 확인
3. 배지에 색상뿐 아니라 텍스트 라벨이 함께 표시되는지 확인
기대결과: 상태/우선순위 배지가 완전히 둥근 알약(pill) 모양이 아니라 살짝 각진 사각형(모서리가 약간만 둥근) 형태로 표시되고, 배경색과 구분되는 테두리 선이 있다. 색상만으로 구분되지 않고 항상 상태/우선순위를 나타내는 텍스트가 함께 보인다.
근거: @docs/01_analyze/prd/ui-revamp.md (REQ-UIX-007), @docs/02_plan/screen/common.md (2.2절, SCR-COM-007)

### TC-UI-007 공통 컴포넌트(모달·토스트) 회귀 확인
1. `admin@itsm.local`로 로그인해 아무 도메인 화면에서 상세/생성 폼 등 모달(팝업) 창을 여는 동작 수행
2. 모달이 열리고 닫힐 때 부드럽게 나타나고 사라지는지(순간적으로 깜빡이거나 갑자기 튀지 않는지) 확인
3. 저장/처리 등 액션을 수행해 화면 우측 상단에 성공/오류 안내(토스트) 메시지가 나타나는지 확인
기대결과: 모달이 화면 중앙에 그림자와 함께 뜨고 배경과 구분되며, 열림/닫힘 시 자연스러운 확대/페이드 효과로 전환된다. 토스트 메시지는 우측 상단에 나타났다가 자연스럽게 사라지며 레이아웃이 흔들리지 않는다.
근거: @docs/02_plan/screen/common.md (SCR-COM-009, 2.5절 Elevation, 2.6절 모션)

### TC-UI-008 키보드 포커스 링(접근성) 확인
1. `admin@itsm.local`로 로그인한 상태에서 마우스를 사용하지 않고 `Tab` 키만으로 화면 내 버튼·입력창·링크 등을 순서대로 이동
2. 현재 포커스가 위치한 요소 주변에 표시되는 테두리(포커스 링) 확인
기대결과: `Tab`으로 이동할 때마다 현재 포커스된 요소 둘레에 뚜렷하게 보이는 색상 테두리(링)가 표시되며, 어떤 요소에 포커스가 있는지 육안으로 명확히 구분된다.
근거: @docs/01_analyze/prd/ui-revamp.md (REQ-UIX-011), @docs/02_plan/screen/common.md (2.8절)
