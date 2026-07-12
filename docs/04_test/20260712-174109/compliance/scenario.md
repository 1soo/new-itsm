# 통합 테스트 시나리오 — compliance (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `co@itsm.local`(COMPLIANCE_OFFICER)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). compliance는 통합검색(SCR-COM-011) 대상 도메인이 아니므로 통합검색 TC는 시나리오에서 제외
- `status.ts`(complianceStatusLabel/actionStatusLabel/auditEventTypeLabel)에 falsy 가드 적용 확인, `format.ts`는 라벨 없는 순수 `ko-KR` 포맷터로 변경 없음(사전 확인 완료)
- dev-lead 사전 공지: 감사 로그 나열에서 이벤트 코드(`COMPLIANCE_REQ_CREATE` 등) 원시값 노출 결함을 `auditEventTypeLabel(t, eventType)` 헬퍼로 선제 수정(소스 리뷰로 확인 완료 — `ComplianceDetailPage.tsx:461` 정상 적용, 미지정 코드는 원문 폴백)
- **주의**: compliance는 승인 패널이 요구사항 전체가 아니라 **시정조치 항목마다 개별로** 붙는 구조(전 도메인에서 유일). 시정조치를 2건 이상 만들어 각 항목의 승인 패널이 독립적으로 표시되는지 확인 필요
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/compliance.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-COMP-I18N-001 · 요구사항 목록(SCR-COMP-001) 텍스트 전환 — 준수 상태·책임자 미지정 배지 포함
- 근거: @docs/02_plan/screen/compliance.md (SCR-COMP-001), `features/compliance/status.ts`
- 전제: co@itsm.local 로그인, English 전환
- 절차: 1) 요구사항 목록 진입 2) 필터(준수 상태/책임자 지정 여부)·표 헤더 확인 3) 준수 상태 배지 2종, 책임자 미지정 배지 확인
- 기대 결과: 필터·표 헤더(식별키/이름/근거/책임자/준수 상태/갱신일) 영어 전환, 준수 상태 배지 2종(Compliant/Non-compliant) 필터 드롭다운에서 정상 전환, 책임자 미지정 시 "Unassigned"류 표시 확인

### TC-COMP-I18N-002 · 요구사항 등록(SCR-COMP-002) 텍스트 전환 및 회귀 — 필수 항목 오류
- 근거: @docs/02_plan/screen/compliance.md (SCR-COMP-002)
- 전제: co@itsm.local 로그인, English 전환
- 절차: 1) 요구사항 등록 화면 진입 2) 이름·근거 미입력 상태로 등록 시도 3) 이름·근거·적용범위 입력 후 등록
- 기대 결과: 폼 라벨(이름/근거/적용범위)·등록 버튼 영어 전환. 필수 미입력 오류 영어 전환. 등록 성공 토스트 영어 전환 후 상세 이동(회귀 없음)

### TC-COMP-I18N-003 · 요구사항 상세(SCR-COMP-003) 텍스트 전환 및 회귀 — 책임자·시정조치(2건)·변경 연계·감사 로그·개별 승인 패널
- 근거: @docs/02_plan/screen/compliance.md (SCR-COMP-003), dev-lead 지시(승인 패널이 시정조치 항목별 개별 표시)
- 전제: co@itsm.local 로그인, English 전환, TC-COMP-I18N-002에서 등록한 요구사항으로 진입
- 절차: 1) 책임자 지정 2) 시정조치 2건 등록(내용 입력 → 탐지 상태 생성) 3) 각 시정조치를 조치중으로 전이 4) 각 시정조치를 해결(RESOLVED)로 전이 시도(승인 게이트가 있으면 항목별로 개별 승인 패널 노출 확인, 없으면 즉시 전이) 5) 변경 요청 연계(변경 ID 입력) 6) 감사 로그 목록에서 이벤트 유형 라벨 확인(원시 코드 노출 없는지) 7) "수정" 버튼으로 인라인 편집(이름/근거/적용범위) 확인
- 기대 결과: 책임자 지정 폼·시정조치 등록 폼·시정조치 상태 전이 버튼(탐지→조치중→해결)·변경 연계 버튼 전부 영어 전환. 시정조치 2건 중 하나라도 승인 게이트가 걸리면 해당 항목 옆에만 "Approval Status" 패널이 개별로 표시되고 다른 항목에는 영향 없음(회귀 없음). 감사 로그 목록에 "Requirement Created"/"Requirement Updated"/"Corrective Action Status Changed"류 라벨 표시(원시 코드 `COMPLIANCE_REQ_CREATE` 등 노출 없음). "수정" 인라인 편집 폼 라벨 영어 전환, 저장 후 반영 확인(회귀 없음)

### TC-COMP-I18N-004 · 준수 현황 대시보드(SCR-COMP-004) 텍스트 전환
- 근거: @docs/02_plan/screen/compliance.md (SCR-COMP-004)
- 전제: co@itsm.local 로그인, English 전환
- 절차: 1) 준수 현황 대시보드 진입 2) 기간 필터·KPI 카드(준수율/미해결 시정조치 건수)·요구사항별 상태 표 확인
- 기대 결과: 기간 필터·KPI 카드 라벨·요구사항별 상태 표 헤더(요구사항/책임자/준수 상태) 전부 영어 전환

### TC-COMP-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 요구사항 목록/상세의 갱신일·감사 로그 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-COMP-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 요구사항 목록/상세 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
