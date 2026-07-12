# 통합 테스트 결과 — knowledge (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

- 시나리오: `docs/04_test/20260712-162939/knowledge/scenario.md`
- 테스트 계정: `kc@itsm.local`(KNOWLEDGE_CONTRIBUTOR), `kg@itsm.local`(KNOWLEDGE_GATEKEEPER)
- 브라우저: Playwright(새 context, storage 초기화)

## 결과 요약

| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | backend `./gradlew build -x test` BUILD SUCCESSFUL, frontend `npm run build` 성공(사전 실행) |
| TC-KM-I18N-001 | PASS | 지식베이스 목록(SCR-KM-001) 필터(Keyword/Category/Label/Status)·표 헤더(Title/Summary/Category/Status/Helpfulness) 영어 전환. 상태 배지 3종(Draft/In Review/Published) 필터 드롭다운에서 정상 전환 확인. 카테고리 값(네트워크/계정/권한 등)은 DB 데이터라 한국어 유지(기존 컨벤션과 일치, 정상) |
| TC-KM-I18N-002 | PASS | 기사 열람(SCR-KM-002) "Was this helpful?" 위젯·"Comment (optional)"·"Helpful"/"Not Helpful" 버튼 영어 전환. 평가 저장 시 SweetAlert2 토스트 "Feedback saved" 영어 전환, 집계값("Helpful 1 · Not helpful 0")도 정상 갱신(회귀 없음) |
| TC-KM-I18N-003 | PASS | 기사 작성·편집(SCR-KM-003) 에디터 라벨(Title/Body/Category/Labels)·액션 버튼(Save/Request Review/Delete) 영어 전환. 필수 미입력 저장 시 "Title and body are required." 오류 정상. 저장 성공 토스트 "Article created" 영어. "Request Review" 클릭 시 매칭 승인 프로세스가 있어 IN_REVIEW 전환 + "Review requested. Awaiting approval." 토스트, 승인 패널(공용) "Approval Status"/"Step 1"/"In Progress" 정상 전환 |
| TC-KM-I18N-004 | PASS | 지표 대시보드(SCR-KM-005) 타이틀 "Knowledge Metrics", 기간 필터(From/To), KPI 카드(Usage/No-result Searches/Helpfulness/Ticket Deflection Rate), "No-result Search Keywords"/"No data available." 전부 영어 전환 |
| TC-SEARCH-KM-001 | PASS | 통합 검색 결과에서 KNOWLEDGE 도메인 배지 "Knowledge", 상태 배지 "Published" 정상 전환(그동안 미착수라 한국어로 남아있던 부분이 이번 phase로 정상 전환됨을 라이브로 확인) |
| TC-KM-APPROVAL-REG-001 | PASS | kg@itsm.local(지식 게이트키퍼) 계정으로 공용 승인 대기함(`/approvals`)에서 KM-16 항목 확인·승인 처리("Process Approval" 모달의 "Step 1 Role Decisions (All roles required)"·역할명(데이터, "지식 게이트키퍼")·"Pending"·"Approve"/"Reject" 전부 정상). 승인 후 "Approved." 토스트, 원 기사가 Published로 정상 전환 확인(회귀 없음) |
| TC-KM-FORMAT-REG-001 | PASS | English 상태에서도 통합검색 "Updated At" 값이 `2026. 7. 12. 오후 4:33:41`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 |
| TC-KM-KO-ROUNDTRIP-001 | PASS | 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 복귀. 목록 화면 필터(키워드/카테고리/라벨/상태)·표 헤더(제목/요약/카테고리/상태/유용성)·상태 배지(검토/게시)·페이지네이션(이전/다음 페이지) 전부 정상, 누락·깨짐 없음 |

## 결함 분석

없음. 이번 phase에서는 change 도메인에서 발견된 "연계 항목 원시 enum 노출" 패턴과 유사한 결함, problem 도메인에서 발견된 falsy 가드 미적용 회귀 모두 사전 소스 리뷰·라이브 테스트에서 발견되지 않음(`statusLabel`에 `if (!s) return "";` 가드 기 적용, `format.ts`는 라벨 없는 순수 `ko-KR` 포맷터로 대상 외).

## 스크린샷/증적

- 별도 스크린샷 저장 없이 Playwright accessibility snapshot으로 각 화면의 텍스트 상태를 확인함(토스트 메시지는 `browser_run_code_unsafe`로 `.swal2-toast` 텍스트를 즉시 캡처).
- 테스트 중 실제 생성한 데이터: `KM-16`("i18n test article - knowledge phase", 카테고리 네트워크, DRAFT→IN_REVIEW(승인 프로세스 매칭)→승인 처리→PUBLISHED, 유용성 평가 Helpful 1건 등록)

## 종합 판정

knowledge 도메인 i18n 전환 **전 항목 PASS**. 검색/목록·기사 열람(유용성 평가)·작성/편집(검토 요청·승인 패널)·지표 대시보드·통합검색 KNOWLEDGE 배지·검토→공용 승인함 처리 흐름·날짜 포맷(ko-KR 유지)·한국어 재전환 모두 정상 확인. 결함 없음.
