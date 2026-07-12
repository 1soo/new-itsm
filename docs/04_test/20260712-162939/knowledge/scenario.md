# 통합 테스트 시나리오 — knowledge (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `kc@itsm.local`(KNOWLEDGE_CONTRIBUTOR, 작성/편집), `kg@itsm.local`(KNOWLEDGE_GATEKEEPER, 지표), `user@itsm.local`(END_USER, 셀프서비스 열람)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). SCR-KM-004(검토·게시 승인함)는 SCR-COM-014로 대체되어 대상 아님
- `status.ts`(statusLabel)에 problem phase와 동일한 falsy 가드 적용, `format.ts`는 라벨 없는 순수 `ko-KR` 포맷터로 변경 없음(사전 확인 완료)
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/knowledge.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-KM-I18N-001 · 지식베이스 검색/목록(SCR-KM-001) 텍스트 전환 — 상태 배지 포함
- 근거: @docs/02_plan/screen/knowledge.md (SCR-KM-001), `features/knowledge/status.ts`
- 전제: kc@itsm.local 로그인, English 전환
- 절차: 1) 지식베이스 목록 진입 2) 검색바·분류 필터·결과 목록 확인 3) 상태 배지(초안/검토/게시) 확인
- 기대 결과: 검색바 placeholder·분류 필터 라벨·결과 목록 헤더 영어 전환, 상태 배지 3종(Draft/In Review/Published 등) 전환 확인. 무결과 검색 시 안내 문구 영어 전환

### TC-KM-I18N-002 · 기사 열람(SCR-KM-002) 텍스트 전환 및 유용성 평가 회귀
- 근거: @docs/02_plan/screen/knowledge.md (SCR-KM-002)
- 전제: user@itsm.local 로그인(END_USER, 셀프서비스), English 전환, 게시된 기사로 진입
- 절차: 1) 게시 기사 열람 2) "도움이 되었나요?" 위젯에서 예/아니오 평가 3) 코멘트 입력 후 저장
- 기대 결과: 본문 영역 라벨·유용성 위젯 문구("Was this helpful?" 등) 영어 전환, 평가 저장 시 SweetAlert2 토스트 영어 전환(회귀 없음)

### TC-KM-I18N-003 · 기사 작성·편집(SCR-KM-003) 텍스트 전환 — 검토 요청 포함
- 근거: @docs/02_plan/screen/knowledge.md (SCR-KM-003)
- 전제: kc@itsm.local 로그인, English 전환
- 절차: 1) 신규 기사 작성 화면 진입 2) 제목/본문 미입력 상태로 저장 시도 3) 제목·본문·카테고리 입력 후 저장(DRAFT) 4) "검토 요청" 클릭
- 기대 결과: 에디터 라벨(제목/본문/카테고리/라벨)·액션 버튼(저장/검토 요청/삭제) 영어 전환. 필수 미입력 오류 영어 전환. 저장 성공 토스트 영어. "검토 요청" 클릭 시 매칭 승인 프로세스 없으면 "즉시 게시" 안내 토스트 영어 전환, 있으면 승인 패널(공용)에 차수 진행 상태 영어 전환 표시(반려 시 사유 노출 포함)

### TC-KM-I18N-004 · 지식 지표 대시보드(SCR-KM-005) 텍스트 전환
- 근거: @docs/02_plan/screen/knowledge.md (SCR-KM-005)
- 전제: kg@itsm.local 로그인, English 전환
- 절차: 1) 지표 대시보드 진입 2) 기간 필터·KPI 카드·무결과 검색 키워드 랭킹 확인
- 기대 결과: 기간 필터 라벨·KPI 카드 라벨(사용량/무결과 검색/유용성/차단율)·무결과 키워드 표 헤더 영어 전환

### TC-SEARCH-KM-001 · 통합 검색 결과(SCR-COM-011)에서 KNOWLEDGE 상태 배지 전환
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), `features/search/status.ts`
- 전제: 로그인 상태, English 전환
- 절차: 1) 헤더 검색으로 지식 기사 관련 키워드 검색 → 전체 결과 화면 진입 2) KNOWLEDGE 도메인 결과의 상태 배지 확인
- 기대 결과: KNOWLEDGE 결과의 도메인 배지("Knowledge")·상태 배지가 영어로 전환(그동안 미착수라 한국어로 남아있던 부분이 이번 phase로 정상 전환됨을 확인)

### TC-KM-APPROVAL-REG-001 · 검토 요청 → 승인함(SCR-COM-014, 공용) 처리 흐름 회귀
- 근거: @docs/02_plan/screen/common.md (SCR-COM-014)
- 전제: kc@itsm.local(작성), kg@itsm.local 또는 매칭 승인자 역할(결정)
- 절차: 1) 매칭되는 지식 승인 프로세스가 있는 경우 검토 요청으로 IN_REVIEW 전환 2) 승인 대기함(`/approvals`)에서 지식 기사 항목 확인·승인 처리 3) 원 기사 상태가 PUBLISHED로 전환됐는지 확인
- 기대 결과: 승인 대기함 목록/상세 처리 정상 동작(공용 컴포넌트, common phase에서 이미 검증됨). 승인 후 기사 상태 PUBLISHED 반영 회귀 없음

### TC-KM-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 기사 목록/열람의 작성일·수정일 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-KM-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 지식 목록/기사 열람/작성 화면 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
