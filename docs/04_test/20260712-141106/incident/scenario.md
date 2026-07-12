# 통합 테스트 시나리오 — incident (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `agent@itsm.local`(SERVICE_DESK_AGENT, 등록·상세), `im@itsm.local`(INCIDENT_MANAGER, 역할배정·에스컬레이션·해결·포스트모템)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음)
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/incident.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-INC-I18N-001 · 인시던트 목록(SCR-INC-001) 텍스트 전환 — 심각도/상태/PM 필요 배지 포함
- 근거: @docs/02_plan/screen/incident.md (SCR-INC-001), `features/incident/status.ts`
- 전제: agent@itsm.local 로그인, English 전환
- 절차: 1) 인시던트 목록 진입 2) 필터·배지 확인
- 기대 결과: 필터·표 헤더·"인시던트 등록" 버튼 영어 전환, 심각도 배지(SEV1~3, 값 자체는 코드값 유지 확인)·상태 배지(신규→New/대응중→In Progress/해결→Resolved/종료→Closed)·"포스트모템 필요" 태그 영어 전환

### TC-INC-I18N-002 · 인시던트 등록(SCR-INC-002) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/incident.md (SCR-INC-002)
- 전제: agent@itsm.local 로그인, English 전환
- 절차: 1) 등록 화면 진입 2) 요약·심각도 미입력 상태로 제출 시도 3) 정상 값(요약="i18n test incident", 심각도=SEV2)으로 제출
- 기대 결과: 폼 라벨(요약/설명/심각도/영향 서비스/영향 제품)·"등록" 버튼 영어 전환, 필수 미입력 인라인 오류 영어 전환, 정상 등록 시 성공 토스트(영어) 후 상세 이동(회귀 없음)

### TC-INC-I18N-003 · 인시던트 상세(SCR-INC-003) 텍스트 전환 — 전 섹션
- 근거: @docs/02_plan/screen/incident.md (SCR-INC-003)
- 전제: im@itsm.local 로그인, English 전환, TC-INC-I18N-002에서 등록한 인시던트로 진입
- 절차: 1) 심각도/우선순위 편집 2) 상태 전이(NEW→IN_PROGRESS) 3) 대응 역할 배정(Tech Lead 등) 4) 에스컬레이션 5) 상태 업데이트(내부/외부 토글) 입력 6) 시간지표 확인 7) 해결 처리(영향시작·탐지·영향종료 입력) 8) 문제 연계
- 기대 결과: 심각도/우선순위 셀렉트·상태 전이 버튼·역할 배정 패널·에스컬레이션 버튼·상태 업데이트 입력(공개범위 토글 라벨)·시간지표 라벨(MTTD/MTTA/MTTR)·해결 처리 폼 라벨·문제 연계 버튼 전부 영어 전환. 각 액션(전이·역할배정·에스컬레이션·업데이트·해결·연계) 기능 자체는 회귀 없이 정상 동작

### TC-INC-I18N-004 · 시간지표 "미산정"→"Not calculated" 전환 확인
- 근거: dev-lead 지시, `features/incident/format.ts` `formatMinutes`
- 전제: im@itsm.local 로그인, English 전환, 아직 해결되지 않은(MTTR 등 값 없는) 인시던트 상세
- 절차: 1) 시간지표(MTTD/MTTA/MTTR) 영역 확인
- 기대 결과: 값이 없는 지표는 "Not calculated"로 표시, 값이 있는 지표는 숫자+단위(min 등) 그대로 표시(단위 자체의 영어 전환 여부 확인)

### TC-INC-I18N-005 · 포스트모템 편집(SCR-INC-004) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/incident.md (SCR-INC-004)
- 전제: im@itsm.local 로그인, English 전환, TC-INC-I18N-003에서 RESOLVED 처리한 인시던트(SEV1/2 가정 시 "포스트모템 필요" 상태)로 진입
- 절차: 1) 포스트모템 편집 화면 진입 2) 근본원인 비운 채 제출 시도 3) 5 Whys·근본원인·조치항목 입력 후 제출
- 기대 결과: 섹션 라벨(요약/타임라인 요약/5 Whys/근본원인/조치항목)·"제출" 버튼 영어 전환, 근본원인 누락 시 거부 메시지 영어 전환, 정상 제출 시 인시던트에 연결 저장(회귀 없음)

### TC-INC-I18N-006 · 인시던트 지표 대시보드(SCR-INC-005) 텍스트 전환
- 근거: @docs/02_plan/screen/incident.md (SCR-INC-005)
- 전제: im@itsm.local 또는 agent@itsm.local 로그인, English 전환
- 절차: 1) 지표 대시보드 진입 2) 기간 필터·KPI 카드·심각도 분포 차트 확인
- 기대 결과: 기간 필터 라벨·KPI 카드 라벨(건수/평균 MTTR)·심각도 분포 차트 라벨(SEV1/2/3) 영어 전환

### TC-INC-TIMELINE-REG-001 · 타임라인 메시지는 번역 대상 아님(참고, 결함 아님)
- 근거: dev-lead 지시(BE 하드코딩 데이터, 이번 phase 번역 대상 아님)
- 전제: English 상태, 인시던트 상세의 타임라인 섹션
- 절차: 1) 타임라인 항목 문구 확인(예: "인시던트가 등록되었습니다")
- 기대 결과: 타임라인 메시지는 한국어로 남아있어도 정상(섹션 타이틀 "Timeline" 등 chrome은 영어 전환 확인)

### TC-SEARCH-INC-001 · 통합 검색 결과(SCR-COM-011)에서 INCIDENT 상태 배지 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), `features/search/status.ts`
- 전제: 로그인 상태, English 전환
- 절차: 1) 헤더 검색으로 인시던트 관련 키워드 검색 → 전체 결과 화면 진입 2) INCIDENT 도메인 결과의 상태 배지 확인
- 기대 결과: INCIDENT 결과의 도메인 배지("Incident")·상태 배지가 영어로 전환(`features/search/status.ts`가 `incident/status.ts`의 `statusLabel(t, ...)` 재사용)

### TC-INC-FORMAT-REG-001 · 날짜/숫자 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 인시던트 목록의 "갱신일" 또는 상세의 타임라인 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-INC-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 인시던트 목록/상세 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)

### TC-INC-CROSSREG-001 · 권한 없는 역할 배정 시 403/숨김 회귀
- 근거: @docs/02_plan/screen/incident.md (SCR-INC-003 "IM 권한 없으면 역할 배정 버튼 숨김/403")
- 전제: SERVICE_DESK_AGENT(agent@itsm.local, INCIDENT_MANAGER 아님) 로그인
- 절차: 1) 인시던트 상세 진입 후 역할 배정 패널/버튼 노출 여부 확인
- 기대 결과: 역할 배정 관련 UI가 숨겨지거나 403 처리되어 회귀 없음(텍스트 전환과 무관)
