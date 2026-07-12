# 통합 테스트 시나리오 — infra-monitoring (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12, 마지막 도메인)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `io@itsm.local`(INFRA_OPERATOR)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). infra-monitoring은 통합검색(SCR-COM-011) 대상 도메인이 아니므로 통합검색 TC는 시나리오에서 제외
- `status.ts`(metricTypeLabel/thresholdTypeLabel)에 falsy 가드 적용 확인. `metricTypeUnit`(%, ms)은 기호이므로 번역 대상 아님(dev-lead 확인 사항, 소스 리뷰로 확인 완료). `format.ts`는 라벨 없는 순수 `ko-KR` 포맷터로 변경 없음
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/infra-monitoring.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-IOM-I18N-001 · 인프라 지표 등록(SCR-IOM-001) 텍스트 전환 및 회귀 — 임계치 초과 알림
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-001), `features/infra-monitoring/status.ts`
- 전제: io@itsm.local 로그인, English 전환
- 절차: 1) 지표 등록 화면 진입 2) 자산·값 미입력 상태로 등록 시도 3) 지표 항목 셀렉트(Uptime/CPU/Memory/Response Time) 확인 4) 임계치를 초과하는 값으로 등록
- 기대 결과: 폼 라벨(자산/측정 시각/지표 항목/값)·등록 버튼 영어 전환. 필수 미입력 오류 영어 전환. 지표 항목 4종 전부 영어 전환(원시값 노출 없음). 임계치 초과 시 "A threshold alert has been generated."류 토스트 영어 전환(회귀 없음, 단위 %·ms는 기호라 번역 대상 아님을 확인)

### TC-IOM-I18N-002 · 지표 대시보드(SCR-IOM-002) 텍스트 전환 및 회귀 — 시계열·SLA 카드
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-002)
- 전제: io@itsm.local 로그인, English 전환
- 절차: 1) 대시보드 진입 2) 자산·지표 항목·기간 선택 후 시계열 조회 3) SLA 대비 가동률 카드 확인(목표 가동률 설정 폼 포함)
- 기대 결과: 자산/지표 항목/기간 선택 라벨, 시계열 차트 라벨, SLA 카드(목표%/실제%/달성 여부) 영어 전환. 목표 미설정 자산은 실제 가동률만 표시(회귀 없음)

### TC-IOM-I18N-003 · 임계치 설정·알림 목록(SCR-IOM-003) 텍스트 전환 및 회귀 — 확인/미확인 상태
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-003), dev-lead 지시(임계치 초과 알림 확인/미확인 상태 원시값 노출 여부 확인)
- 전제: io@itsm.local 로그인, English 전환
- 절차: 1) 임계치 설정 폼(지표 항목 선택 시 현재 설정값 자동 반영)에서 상한/하한 설정 2) 알림 목록에서 임계치 초과 유형(상한 초과/하한 미달) 라벨 확인 3) 알림 확인 처리(Acknowledge) 클릭
- 기대 결과: 임계치 설정 폼 라벨(지표 항목/상한/하한)·저장 버튼 영어 전환. 알림 목록 표 헤더(자산/지표/초과값/발생시각/확인 여부)·초과 유형 라벨(Upper Exceeded/Lower Below류, 원시값 노출 없음) 영어 전환. 확인 처리 버튼("Acknowledge") 클릭 시 토스트 영어, 확인된 알림 목록에서 흐리게 표시(회귀 없음)

### TC-IOM-I18N-004 · 용량 계획 관리(SCR-IOM-004) 텍스트 전환 및 회귀 — 활용률 배지
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-004)
- 전제: io@itsm.local 로그인, English 전환
- 절차: 1) 용량 계획 등록 화면 진입 2) 역량·수요 미입력 상태로 등록 시도 3) 팀/서비스명·역량·수요 입력 후 등록(활용률 100% 초과하도록 값 설정)
- 기대 결과: 등록 폼 라벨(팀/서비스명/역량/예상 수요)·목록 표 헤더 영어 전환. 필수 미입력 오류 영어 전환. 활용률 배지가 100% 초과 시 Danger 강조로 정상 표시(수치 자체는 원문 유지, 회귀 없음)

### TC-IOM-I18N-005 · 인프라 지표 리포팅(SCR-IOM-005) 텍스트 전환
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-005)
- 전제: io@itsm.local 로그인, English 전환
- 절차: 1) 리포팅 화면 진입 2) 기간·자산 필터·KPI 카드(평균 가동률/CPU/메모리/응답시간/용량 활용률) 확인
- 기대 결과: 필터·KPI 카드 라벨 전부 영어 전환

### TC-IOM-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 알림 목록의 발생시각, 대시보드 시계열의 측정 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-IOM-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 지표 등록/임계치·알림/용량 계획 화면 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
