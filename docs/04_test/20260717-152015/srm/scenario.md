# 통합 테스트 시나리오 — srm (재테스트: 폼 빌더 pattern 버그 수정, 팔레트 구성, 제출/취소 버튼 배치)

## 사전 조건
- 빌드 테스트 통과(백엔드 Gradle build, 프론트엔드 tsc+vite build)
- 계정: po@itsm.local(PROCESS_OWNER), user@itsm.local(END_USER) — 공통 비밀번호 `Admin@1234`
- 서버: 백엔드 `localhost:8080`, 프론트엔드 `localhost:5173`
- 참고: 이전 실행 `docs/04_test/20260717-145302/srm/result/srm.md`(TC-SRM-002/004/006 FAIL), `docs/03_develop/plan/common.md` "추가 요구사항(2026-07-17)" 절(버튼 배치), `docs/02_plan/screen/common.md` 8.2절(팔레트 구성)
- 재테스트 범위(dev-lead 지시): TC-SRM-002/004/006 재검증 + 신규 버튼 배치 확인만 수행. 이전 PASS 항목(TC-SRM-001/003/005/007~010)은 재검증하지 않음(단, 빌드 테스트는 선행 항목으로 포함)

## 시나리오

### TC-SRM-101 · 빌드 테스트
- 근거: 통합테스트 선행 항목
- 절차: 1. 백엔드 `./gradlew build -x test` 2. 프론트엔드 `npm run build`
- 기대 결과: 둘 다 오류 없이 성공

### TC-SRM-102 · FormSubmissionValidator pattern 버그 수정 확인(TC-SRM-004 재검증)
- 근거: @docs/02_plan/api_spec/common.md 0-2절, `FormSubmissionValidator.java`, 이전 실행 TC-SRM-004(FAIL)
- 전제: 텍스트 필드(minLength=5, required)를 포함한 폼빌더 카탈로그 항목(기존 id=7 "폼빌더 회귀 테스트 항목" 재사용 또는 신규 생성)
- 절차: 모든 필드에 유효한 값(minLength 이상, required 충족)으로 `POST /api/v1/service-requests` 제출
- 기대 결과: 201 성공(더 이상 "형식이 올바르지 않습니다" 400이 발생하지 않음)

### TC-SRM-103 · minLength/maxLength/min/max 개별 위반 및 정상 대조군 확인(TC-SRM-006 재검증)
- 근거: @docs/02_plan/api_spec/common.md 0-2절, 이전 실행 TC-SRM-006(FAIL)
- 전제: TC-SRM-102의 카탈로그 항목(텍스트 필드 minLength=5, Panel 내부 Number 필드 min=1/max=100)
- 절차:
  1. minLength 위반(짧은 문자열) 제출 → 400 확인
  2. 숫자 필드 max 초과 값 + 텍스트 필드는 유효값으로 제출 → max 위반 메시지로 400 확인(더 이상 텍스트 필드 pattern 오류에 가려지지 않는지 확인)
  3. 모든 값 유효 → 201 성공(대조군)
- 기대 결과: 각 위반은 해당 규칙 메시지로 400, 정상 제출은 201

### TC-SRM-104 · 폼 빌더 팔레트 구성 재검증(TC-SRM-002 재검증)
- 근거: @docs/02_plan/screen/common.md 8.2절, 이전 실행 TC-SRM-002(FAIL)
- 전제: po@itsm.local, `/admin/service-catalog` 카탈로그 항목 편집(폼 빌더 진입)
- 절차: 팔레트의 Advanced/Premium 그룹 확인
- 기대 결과: Advanced 그룹에 Email/Phone Number/Date-Time/File 포함, Premium(및 Data) 그룹은 노출되지 않음(팔레트 탭 자체가 없어야 함)

### TC-SRM-105 · 요청 제출 폼 제출/취소 버튼 우측 하단 배치(신규)
- 근거: @docs/03_develop/plan/common.md "추가 요구사항(2026-07-17) — 폼 렌더러 제출/취소 버튼 우측 하단 배치" 절
- 전제: user@itsm.local, `/portal/requests/new?item=` (임의 카탈로그 항목)
- 절차: 요청 제출 화면 하단 버튼 영역 확인(DOM 순서·정렬), 폼 내부에 중복된 Form.io 기본 제출 버튼이 없는지 확인
- 기대 결과: 하단에 취소·제출 버튼만 존재(우측 정렬, `취소` 좌측 · `제출` 가장 오른쪽), 스키마 내장 submit 컴포넌트는 화면에 중복 노출되지 않음. sticky 처리 없이 폼 콘텐츠 흐름에 포함(스크롤 시 함께 이동)
