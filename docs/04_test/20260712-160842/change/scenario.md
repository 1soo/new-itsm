# 통합 테스트 시나리오 — change (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `cm@itsm.local`(CHANGE_MANAGER)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). SCR-CHG-004(CAB 승인 대기함)는 SCR-COM-014로 대체되어 대상 아님
- `status.ts`(statusLabel/typeLabel/riskLabel)에 problem phase와 동일한 falsy 가드가 이미 적용되어 있음(사전 확인 완료) — 레거시 빈 값 노출 회귀는 소스 리뷰로 확인, 실데이터로 추가 재확인
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/change.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-CHG-I18N-001 · 변경 목록(SCR-CHG-001) 텍스트 전환 — 유형/상태/위험도 배지 포함
- 근거: @docs/02_plan/screen/change.md (SCR-CHG-001), `features/change/status.ts`
- 전제: cm@itsm.local 로그인, English 전환
- 절차: 1) 변경 목록 진입 2) 필터·배지 확인
- 기대 결과: 필터(유형/상태/위험도/기간)·표 헤더 영어 전환, 유형 배지(표준/일반/긴급 → Standard/Normal/Emergency)·상태 배지(요청~종료 6단계 전부 영어) 전환 확인. 레거시(값 없는) 데이터가 있는 경우 원시 키 노출 없이 빈 문자열 표시 확인

### TC-CHG-I18N-002 · RFC 생성(SCR-CHG-002) 텍스트 전환 및 회귀 — 표준 변경 템플릿 안내
- 근거: @docs/02_plan/screen/change.md (SCR-CHG-002)
- 전제: cm@itsm.local 로그인, English 전환
- 절차: 1) RFC 생성 화면 진입 2) 요약·변경 유형 미입력 상태로 제출 시도 3) 변경 유형을 "표준"(사전승인 템플릿)으로 선택해 승인 생략 안내 확인 4) 요약·유형(일반)·위험도·롤백 방법 등 입력 후 생성
- 기대 결과: 폼 라벨(요약/설명/변경 유형/위험도/예상 구현/영향 시스템/롤백 방법/예정 일정)·"생성" 버튼 영어 전환, 표준 선택 시 "승인 단계 생략" 안내 영어 전환, 필수 미입력 오류 영어 전환, 정상 생성 시 성공 토스트(영어) 후 상세 이동(회귀 없음)

### TC-CHG-I18N-003 · 변경 상세(SCR-CHG-003) 텍스트 전환 — 전 섹션 및 회귀
- 근거: @docs/02_plan/screen/change.md (SCR-CHG-003)
- 전제: cm@itsm.local 로그인, English 전환, TC-CHG-I18N-002에서 생성한 변경으로 진입
- 절차: 1) 상태 전이(요청→검토→계획) 2) 승인 패널(공용) 확인(매칭 프로세스 없으면 "이 변경에는 승인 절차가 없습니다" 안내, 있으면 차수 진행 상태) 3) 구현 전이 시도(승인 완료 전 차단 확인 포함) 4) 구현 결과 기록(성공/실패·롤백 여부·비고) 5) 인시던트/문제 연계
- 기대 결과: 프로세스 상태 전이 버튼·승인 패널 안내 문구(공용 `common:approval.*` 키 재사용)·구현 결과 폼 라벨·"인시던트/문제 연계" 버튼 전부 영어 전환. 승인 완료 전 구현 전이 거부, 승인 안 된 변경에 결과 기록 거부 등 기존 동작 회귀 없음

### TC-CHG-I18N-004 · 변경 일정 캘린더(SCR-CHG-005) 텍스트 전환 — 요일/월 라벨
- 근거: @docs/02_plan/screen/change.md (SCR-CHG-005), dev-lead 지시(요일/월 라벨 확인)
- 전제: cm@itsm.local 로그인, English 전환
- 절차: 1) 변경 일정 화면 진입 2) 요일 헤더·월 라벨("{{month}}/{{year}}" 등)·이전/다음 달 버튼 확인 3) 이전/다음 달 이동
- 기대 결과: "변경 일정" 타이틀, 유형 필터, 요일 헤더(일~토 → Sun~Sat), 월 라벨(영어 포맷으로 전환, 예: "7/2026"), "이전 달"/"다음 달" aria-label 전부 영어 전환. 이전/다음 달 이동 기능 정상(회귀 없음)

### TC-CHG-I18N-005 · 변경 지표 대시보드(SCR-CHG-006) 텍스트 전환
- 근거: @docs/02_plan/screen/change.md (SCR-CHG-006)
- 전제: cm@itsm.local 로그인, English 전환
- 절차: 1) 지표 대시보드 진입 2) 기간 필터·KPI 카드 확인
- 기대 결과: 기간 필터 라벨·KPI 카드 라벨(성공률/실패율/긴급 비율) 영어 전환

### TC-SEARCH-CHG-001 · 통합 검색 결과(SCR-COM-011)에서 CHANGE 상태 배지 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), `features/search/status.ts`
- 전제: 로그인 상태, English 전환
- 절차: 1) 헤더 검색으로 변경 관련 키워드 검색 → 전체 결과 화면 진입 2) CHANGE 도메인 결과의 상태 배지 확인
- 기대 결과: CHANGE 결과의 도메인 배지("Change")·상태 배지가 영어로 전환(`features/search/status.ts`가 `change/status.ts`의 `statusLabel(t, ...)` 재사용)

### TC-CHG-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 변경 목록의 "예정일" 또는 상세의 타임라인 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음(캘린더 월 라벨은 별도 번역 키로 전환되는 것과는 별개 — 실제 날짜/시각 값 포맷은 유지)

### TC-CHG-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 변경 목록/상세/일정 캘린더 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
