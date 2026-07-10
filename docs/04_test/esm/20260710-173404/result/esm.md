# 통합 테스트 결과 — ESM (엔터프라이즈 서비스 관리) (20260710-173404)

## 요약
- 총 20건 · 성공 20 · 실패 0
- (재테스트 2026-07-10) TC-ESM-018 수정 확인 후 PASS로 갱신. 나머지 19건은 최초 실행에서 이미 PASS.

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ESM-BUILD-001 | PASS | frontend `npm run build`, backend `gradlew.bat build -x test` 모두 성공 | - |
| TC-ESM-001 | PASS | po@itsm.local 카탈로그 관리 화면에서 시드 5건(HR 2·LEGAL 1·FACILITIES 1·FINANCE 1) 전체 조회. 부서 선택 콤보박스는 HR/LEGAL/FACILITIES/FINANCE 4개만 노출(IT 제외, 설계 의도대로 IT는 SRM 유지) | - |
| TC-ESM-002 | PASS | (1) 이름·부서(시설)·필드 1개로 신규 항목 생성 성공, 목록에 즉시 반영 (2) department 누락 시 `POST /api/v1/esm/catalog-items` 400 `VALIDATION_ERROR` | - |
| TC-ESM-003 | PASS | department=HR, checklistTemplateType=ONBOARDING, checklistTemplate=[] 로 API 직접 호출 시 201 정상 저장(id=8) | - |
| TC-ESM-004 | PASS | user@itsm.local(END_USER)로 카탈로그 생성 API 호출 시 403 `ACCESS_DENIED` | - |
| TC-ESM-005 | PASS | user@itsm.local이 "계약서 검토 요청" 제출 → ESM-2026-0004, 상태=제출됨, 연계 체크리스트 카드 미노출(일반 요청) | - |
| TC-ESM-006 | PASS | (1) 대상자명 미입력 시 FE 인라인 오류("대상자명은 필수 항목입니다")로 제출 차단(페이지 이동 없음) (2) 대상자명 입력 후 제출 → ESM-2026-0005, 체크리스트 자동생성 토스트, 진행률 0/2. 체크리스트 상세에서 HR "인사 서류 접수 확인"·IT "계정·장비 지급" 2건 정확히 생성 확인 | - |
| TC-ESM-007 | PASS | 대상자=am@itsm.local(활성 자산 AST-0002/AST-0004, 폐기 자산 AST-0003 보유)로 오프보딩 제출 → 진행률 0/4. 체크리스트 상세에서 템플릿 2건(IT 계정 비활성화, FACILITIES 출입카드 회수) + 자산회수 2건(AST-0002, AST-0004, IT 배정) 정확히 생성, 폐기된 AST-0003은 하위작업 미생성 확인 | - |
| TC-ESM-008 | PASS | 템플릿 빈 카탈로그(id=8)로 요청 제출 시 400 `ESM_CHECKLIST_TEMPLATE_REQUIRED` | - |
| TC-ESM-009 | PASS | (1) legal-coord@itsm.local 처리 큐에는 LEGAL 요청(ESM-2026-0004)만 노출 (2) HR 요청(id=5) 상세 직접 접근 시 403(화면은 "요청을 찾을 수 없습니다" 안내+토스트, API 직접 호출 시 403 `ACCESS_DENIED` 확인) | - |
| TC-ESM-010 | PASS | legal-coord가 ESM-2026-0004를 제출됨→처리중(담당자 자동배정)→코멘트 등록→완료로 정상 전이, 타임라인·코멘트 모두 반영 | - |
| TC-ESM-011 | PASS | it-coord@itsm.local "내 하위 작업" 목록에 IT 배정 작업만 노출(HR·FACILITIES 작업 미노출). facilities-coord도 동일하게 FACILITIES 작업만 노출 확인 | - |
| TC-ESM-012 | PASS | 오프보딩 체크리스트(id=5, am@itsm.local): it-coord가 IT 작업 3건(계정 비활성화+자산회수 2건) 완료 후 3/4·IN_PROGRESS, facilities-coord가 FACILITIES 작업 완료 후 4/4·COMPLETED 자동 전환 확인 | - |
| TC-ESM-013 | PASS | 온보딩 체크리스트(id=4, 테스트 온보딩 대상자): it-coord가 IT 작업만 완료 처리 → 1/2·IN_PROGRESS 유지(HR 담당 계정 부재로 전체완료 미검증, 사전 조건에 명시) | - |
| TC-ESM-014 | PASS | legal-coord가 IT 배정 하위작업(FACILITIES 작업으로 대체 검증: id=12) 완료 시도 시 403 `ACCESS_DENIED` | - |
| TC-ESM-015 | PASS | hr@itsm.local "케이스 접수"로 HR-2 생성(status=INTAKE/접수), 목록·상세에 정상 노출 | - |
| TC-ESM-016 | PASS | (1) 접수→조사(건너뛰기) API 직접 호출 400 `INVALID_STATUS_TRANSITION` (2) 접수→기록→조사→해결 UI 버튼으로 순차 진행, 각 단계 상태 이력에 정확히 기록 (3) 해결→기록(역행) API 직접 호출 400 `INVALID_STATUS_TRANSITION` | - |
| TC-ESM-017 | PASS | admin@itsm.local(SYSTEM_ADMIN)·po@itsm.local(PROCESS_OWNER) 모두 `GET /api/v1/esm/hr-cases` 403, `/esm/hr-cases` 화면 진입 시 `/403` 리다이렉트, 두 계정 사이드바 모두 "HR 케이스" 메뉴 미노출 | - |
| TC-ESM-018 | PASS(재테스트) | 부서 필터(department=HR) 적용 시 처리건수 6→5건 정상 반영(최초 실행 확인). 재테스트: 동일 기간(2030-01-01~2030-01-31)으로 재현 시도 시 `GET /api/v1/esm/metrics?from=2029-12-31T15:00:00.000Z&to=2030-01-31T14:59:59.000Z` 200 OK, 값 전부 0으로 정상 응답(400 재현 안 됨). 실데이터가 걸리는 기간(2026-07-01~2026-07-10)으로도 6건/3분/66.7% 베이스라인과 일치, 콘솔 오류 없음 | 수정 확인: `EsmMetricsPage.tsx`가 date input 값을 `new Date(...).toISOString()`로 변환해 전송하도록 반영됨 |
| TC-ESM-019 | PASS | END_USER(부서 서비스: 포털·내 부서요청만), DEPT_COORDINATOR(부서 서비스: 처리 큐·내 하위작업만), PROCESS_OWNER(부서 서비스: 카탈로그 관리·ESM 지표만), HR_CASE_MANAGER(별도 "HR 케이스" 그룹만, 부서 서비스 그룹 없음) 모두 역할별 노출 항목 일치 | - |

## 실패 항목 분석
- 없음(TC-ESM-018 수정 확인 완료)
