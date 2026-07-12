# 통합 테스트 시나리오 — problem (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `pm@itsm.local`(PROBLEM_MANAGER)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). `format.ts`는 순수 ko-KR 날짜 포맷만 있어 변경 없음(대상 아님)
- dev_fe가 개발 중 발견·수정한 회귀 1건(레거시 `origin` null 값의 "origin.undefined" 키 노출) 재확인 포함
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/problem.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-PRB-I18N-001 · 문제 목록(SCR-PRB-001) 텍스트 전환 — 상태/우선순위 배지 포함
- 근거: @docs/02_plan/screen/problem.md (SCR-PRB-001), `features/problem/status.ts`
- 전제: pm@itsm.local 로그인, English 전환
- 절차: 1) 문제 목록 진입 2) 필터·배지 확인
- 기대 결과: 필터·표 헤더 영어 전환, 상태 배지(탐지/분류/조사중/알려진 오류/워크어라운드/종료 → 영어 6단계 전부)·우선순위 배지(P1~P4 코드값 유지) 전환 확인

### TC-PRB-I18N-002 · 문제 등록(SCR-PRB-002) 텍스트 전환 및 회귀 — 영향도/긴급도/우선순위 미리보기
- 근거: @docs/02_plan/screen/problem.md (SCR-PRB-002)
- 전제: pm@itsm.local 로그인, English 전환
- 절차: 1) 등록 화면 진입 2) 요약 미입력 상태로 제출 시도 3) 영향도만 선택(긴급도 미선택) 상태에서 우선순위 미리보기 확인 4) 요약·영향도·긴급도·출처 입력 후 등록
- 기대 결과: 폼 라벨(요약/설명/출처/조사사유/영향도/긴급도/구성요소)·"등록" 버튼 영어 전환, 영향도/긴급도 값(High/Medium/Low 등) 전환, 하나만 선택 시 우선순위 미리보기 "Not calculated"(또는 동등 영어 라벨) 표시, 요약 필수 오류 영어 전환, 정상 등록 시 성공 토스트(영어) 후 상세 이동(회귀 없음)

### TC-PRB-I18N-003 · 문제 상세(SCR-PRB-003) 텍스트 전환 — 전 섹션 및 회귀
- 근거: @docs/02_plan/screen/problem.md (SCR-PRB-003)
- 전제: pm@itsm.local 로그인, English 전환, TC-PRB-I18N-002에서 등록한 문제로 진입
- 절차: 1) 상태 전이(탐지→분류→조사중) 2) RCA 섹션 작성(근본원인·5 Whys·카테고리) 3) 워크어라운드 입력 저장(빈 내용 저장 시도로 거부 확인 포함) 4) 알려진 오류 생성 5) 인시던트 연결 6) 변경 연계(신규) 7) 후속 조치 등록·상태 변경(진행중→완료) 8) 미해결 후속조치 남긴 채 종료 시도(경고 다이얼로그 확인) 9) 후속조치 모두 완료 처리 후 종료
- 기대 결과: 프로세스 상태 전이 버튼·RCA 폼 라벨·워크어라운드 입력 라벨·"알려진 오류 생성"/"인시던트 연결"/"변경 연계" 버튼·후속 조치 리스트(상태 라벨 진행중/완료→영어)·"종료" 버튼 전부 영어 전환. 워크어라운드 빈 내용 저장 거부 메시지, 미해결 후속조치 종료 경고 다이얼로그 영어 전환. 각 액션 기능 자체는 회귀 없이 정상 동작

### TC-PRB-ORIGIN-REG-001 · 출처(origin) 값 없는 레거시 데이터 표시 회귀(결함 수정 확인)
- 근거: dev-lead 지시(dev_fe 자체 발견·수정 회귀 — `originLabel` null 가드)
- 전제: English 상태, `origin` 필드가 비어있는 기존(레거시) 문제 데이터가 목록에 존재하는 경우
- 절차: 1) 문제 목록에서 출처 컬럼(있는 경우) 또는 상세의 출처 표시 영역 확인
- 기대 결과: "origin.undefined" 등 날 것의 번역 키 문자열이 노출되지 않고 빈 문자열로 정상 표시(수정 확인). 출처 값이 있는 문제는 정상적으로 "Reactive"/"Proactive" 등 영어로 표시

### TC-PRB-I18N-004 · KEDB 검색(SCR-PRB-004) 텍스트 전환
- 근거: @docs/02_plan/screen/problem.md (SCR-PRB-004)
- 전제: pm@itsm.local 로그인, English 전환
- 절차: 1) KEDB 검색 화면 진입 2) 키워드 검색(TC-PRB-I18N-003에서 생성한 알려진 오류로 검색) 3) 결과 없는 키워드로 재검색
- 기대 결과: 검색바 placeholder·결과 목록 chrome(제목/근본원인 요약/워크어라운드/연결 문제 라벨) 영어 전환, 결과 없을 시 빈 목록 안내 영어 전환

### TC-SEARCH-PRB-001 · 통합 검색 결과(SCR-COM-011)에서 PROBLEM 상태 배지 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), `features/search/status.ts`
- 전제: 로그인 상태, English 전환
- 절차: 1) 헤더 검색으로 문제 관련 키워드 검색 → 전체 결과 화면 진입 2) PROBLEM 도메인 결과의 상태 배지 확인
- 기대 결과: PROBLEM 결과의 도메인 배지("Problem")·상태 배지가 영어로 전환(`features/search/status.ts`가 `problem/status.ts`의 `statusLabel(t, ...)` 재사용)

### TC-PRB-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 문제 목록의 "갱신일" 또는 상세의 타임라인 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-PRB-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 문제 목록/상세 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
