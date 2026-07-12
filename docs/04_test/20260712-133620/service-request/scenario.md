# 통합 테스트 시나리오 — service-request (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `user@itsm.local`(END_USER, 요청자), `agent@itsm.local`(SERVICE_DESK_AGENT, 큐/상세 처리), `po@itsm.local`(PROCESS_OWNER, 카탈로그 관리)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음)
- common phase 잔여 결함 2건(`pagination.tsx`/`multi-select.tsx` aria-label·placeholder 등)도 dev-ui가 수정 완료 — 이번 시나리오에서 함께 재검증
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/service-request.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-SRM-I18N-001 · 서비스 포털(SCR-SRM-001) 텍스트 전환
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-001)
- 전제: user@itsm.local 로그인, English 전환
- 절차: 1) 서비스 포털 진입 2) 검색바에 키워드 입력
- 기대 결과: 검색바 placeholder·카탈로그 카드 chrome 텍스트 영어 전환. 카탈로그 항목명·설명(서버 데이터)은 원문 유지

### TC-SRM-I18N-002 · 요청 제출(SCR-SRM-002) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-002)
- 전제: user@itsm.local 로그인, English 전환
- 절차: 1) 카탈로그 카드 클릭해 제출 폼 진입 2) 필수 필드 미입력 상태로 제출 시도 3) 정상 값 입력 후 제출
- 기대 결과: 동적 폼 라벨·"관련 지식 기사" 패널·제출 버튼 영어 전환. 필수 미입력 인라인 오류 영어 전환. 정상 제출 시 접수번호 토스트(영어) 후 상세 이동(회귀 없음)

### TC-SRM-I18N-003 · 내 요청 목록(SCR-SRM-003) 텍스트 전환 — 상태·SLA 배지 포함
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-003), `features/service-request/status.ts`
- 전제: user@itsm.local 로그인, English 전환
- 절차: 1) 내 요청 목록 진입 2) 상태·SLA 배지 확인
- 기대 결과: 필터·표 헤더 영어 전환, 상태 배지(제출됨/검증됨/라우팅됨/승인 대기/이행 중/이행 완료/종료/반려 → 영어)·SLA 배지(준수/임박/위반 → 영어) 전환 확인. 접수번호(서버 데이터)는 원문 유지

### TC-SRM-I18N-004 · 요청 큐(SCR-SRM-004) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-004)
- 전제: agent@itsm.local 로그인, English 전환
- 절차: 1) 요청 큐 진입 2) 미분류 큐 확인 3) 임의 요청에 "배정" 버튼 클릭
- 기대 결과: 큐 목록·"미분류" 배지·배정 버튼 영어 전환. 배정 시 담당자 갱신 정상 동작(회귀 없음)

### TC-SRM-I18N-005 · 요청 상세(SCR-SRM-005) 텍스트 전환 — 승인 패널·SLA 타이머·CSAT 포함
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-005), `components/common/approval-panel.tsx`
- 전제: agent@itsm.local 로그인, English 전환, TC-SRM-I18N-002에서 제출한 요청으로 진입
- 절차: 1) 요청 상세 진입 2) 승인 패널 확인(매칭 프로세스 없으면 "이 요청에는 승인 절차가 없습니다" 안내) 3) 상태 전이 버튼으로 이행완료/종료까지 진행 4) 코멘트 등록
- 기대 결과: 상태 전이 버튼·SLA 타이머·코멘트 입력 라벨·승인 패널 안내 문구(공용 `common:approval.*` 키 재사용) 영어 전환. 종료 처리 후 요청자 계정(user@itsm.local)으로 재진입 시 CSAT 위젯 노출·라벨 영어 전환. 상태 전이·코멘트 등록 기능 회귀 없음

### TC-SRM-I18N-006 · 서비스 카탈로그 관리(SCR-SRM-007) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-007)
- 전제: po@itsm.local 로그인, English 전환
- 절차: 1) 카탈로그 관리 진입 2) 항목 편집 폼에서 이름 누락 상태로 저장 시도(400)
- 기대 결과: 목록·편집 폼 라벨(이름/설명/양식 필드 빌더/담당 큐/SLA 목표)·오류 메시지 영어 전환. "승인 필요 토글"이 제거된 상태(SCR-ADMIN-008로 이관) 유지 확인(회귀 없음)

### TC-SRM-I18N-007 · 요청 지표 대시보드(SCR-SRM-008) 텍스트 전환
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-008)
- 전제: po@itsm.local 또는 agent@itsm.local 로그인, English 전환
- 절차: 1) 지표 대시보드 진입 2) 기간 필터 확인
- 기대 결과: 기간 필터 라벨·KPI 카드 라벨(CSAT/평균 응답/평균 해결/SLA 준수율) 영어 전환, 데이터 없을 시 0값 표시 유지(회귀 없음)

### TC-SEARCH-SRM-001 · 통합 검색 결과(SCR-COM-011)에서 SERVICE_REQUEST 상태 배지 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), `features/search/status.ts`
- 전제: 로그인 상태, English 전환
- 절차: 1) 헤더 검색으로 서비스 요청 관련 키워드 검색 → 전체 결과 화면 진입 2) SERVICE_REQUEST 도메인 결과의 상태 배지 확인
- 기대 결과: SERVICE_REQUEST 결과의 상태 배지가 영어로 전환(`features/search/status.ts`가 `service-request/status.ts`의 `statusLabel(t, ...)`를 재사용). 다른 미착수 도메인(지식 등) 결과는 한국어 유지(정상, 결함 아님)

### TC-COMMONFIX-001 · 페이지네이션 aria-label 재검증(common phase 잔여 결함 1)
- 근거: dev-lead 지시, `components/common/pagination.tsx`
- 전제: English 상태, 요청이 10건 이상인 목록 화면(내 요청 목록 또는 요청 큐)
- 절차: 1) 페이지네이션 컨트롤의 접근성 트리 확인(nav aria-label, 이전/다음 버튼 aria-label)
- 기대 결과: nav aria-label 및 이전/다음 페이지 버튼 aria-label이 영어로 전환("Pagination"/"Previous page"/"Next page" 계열)

### TC-COMMONFIX-002 · MultiSelect 텍스트 재검증(common phase 잔여 결함 2)
- 근거: dev-lead 지시, `components/common/multi-select.tsx`
- 전제: English 상태, MultiSelect를 사용하는 화면(예: 카탈로그 관리 폼 또는 유사 화면)
- 절차: 1) MultiSelect placeholder 확인 2) 옵션 1개 이상 선택 후 선택 개수 표시 확인 3) 선택 칩의 제거 버튼 aria-label 확인 4) 검색 결과 0건 문구 확인
- 기대 결과: placeholder·선택 개수 표시·"선택 해제" aria-label·검색 결과 없음 문구 전부 영어로 전환

### TC-SRM-FORMAT-REG-001 · 날짜/숫자 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 요청 목록의 "갱신일" 또는 상세의 SLA 타이머 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-SRM-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 서비스 포털/요청 목록 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)

### TC-SRM-CROSSREG-001 · 권한 없는 배정/편집 시 403 회귀
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-004/007 "권한 없는 사용자는 배정 불가/프로세스 오너 권한 없으면 403")
- 전제: END_USER(user@itsm.local) 로그인
- 절차: 1) 요청 큐(`/service-requests/queue` 등) 또는 카탈로그 관리 화면 직접 URL 진입 시도
- 기대 결과: 403(SCR-COM-006)으로 이동, 텍스트 전환과 무관하게 접근 통제 회귀 없음
