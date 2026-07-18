# 통합 테스트 시나리오 — srm (요청 큐 폐지 → 카테고리 기반 분류 일원화)

## 사전 조건
- 빌드 테스트 통과(BE `./gradlew build`, FE `npm run build`)
- Process Owner 권한 계정(카탈로그 항목 카테고리 변경용), Agent 이상 권한 계정(요청 처리함 조회·배정용)
- 최소 2개 이상의 카테고리, 각 카테고리에 속한 카탈로그 항목, 그 항목으로 생성된 미종료 서비스 요청 데이터 존재

## 시나리오

### TC-SRM-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/backend`에서 `./gradlew build` 실행
  2. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 둘 다 오류 없이 빌드 성공

### TC-SRM-Q01 · `/api/v1/queues` 완전 제거 확인
- 근거: @docs/02_plan/api_spec/service-request.md (2026-07-18 변경 이력, API-SRM-016 대체), @docs/03_develop/plan/service-request.md (BE 완료 기준 "`/api/v1/queues` 관련 엔드포인트·클래스 완전 제거")
- 전제: 로그인 상태
- 절차:
  1. `GET /api/v1/queues` 호출
- 기대 결과: 404(경로 자체가 존재하지 않음). 관련 컨트롤러 클래스도 코드베이스에서 삭제되어 있어야 함(코드 확인)

### TC-SRM-Q02 · 요청 처리함 좌측 카테고리 목록 및 건수(API-SRM-016)
- 근거: @docs/02_plan/api_spec/service-request.md (API-SRM-016), @docs/02_plan/screen/service-request.md (SCR-SRM-004)
- 전제: 서로 다른 카테고리에 속한 미종료 요청 데이터 존재, 미분류(카테고리 미지정) 카탈로그 항목의 미종료 요청도 존재
- 절차:
  1. 신규 브라우저 컨텍스트에서 로그인 후 SCR-SRM-004(요청 처리함) 접속
  2. 좌측 카테고리 목록 확인
- 기대 결과: `sort_order` 오름차순으로 카테고리명+미종료 건수 표시, 미분류 항목이 항상 마지막에 고정 노출, 건수가 DB 실제 미종료(status ≠ CLOSED) 요청 수와 일치

### TC-SRM-Q03 · 카테고리 클릭 필터링(숫자 categoryId)
- 근거: @docs/02_plan/api_spec/service-request.md (API-SRM-007 `categoryId=`), @docs/02_plan/screen/service-request.md (SCR-SRM-004)
- 전제: TC-SRM-Q02 상태
- 절차:
  1. 좌측 카테고리 목록에서 특정 카테고리 클릭
- 기대 결과: 우측 표가 해당 카테고리 소속 카탈로그 항목의 요청만 서버 필터링되어 표시(`GET /api/v1/service-requests?categoryId={id}` 호출 확인)

### TC-SRM-Q04 · 미분류 필터링(categoryId=uncategorized)
- 근거: @docs/02_plan/api_spec/service-request.md (API-SRM-007 `categoryId="uncategorized"`)
- 전제: 카테고리 미지정 카탈로그 항목의 요청 존재
- 절차:
  1. 좌측 "미분류" 항목 클릭
- 기대 결과: `GET /api/v1/service-requests?categoryId=uncategorized` 호출, 카테고리 미지정 카탈로그 항목의 요청만 표시

### TC-SRM-Q05 · 카탈로그 항목 카테고리 변경 시 기존 요청 분류 즉시 반영(실시간 조인, 핵심 요구사항)
- 근거: @docs/02_plan/database/service-request.md (2026-07-18 행 "스냅샷 없음"), @docs/02_plan/screen/service-request.md (SCR-SRM-004 "요청의 카테고리는 스냅샷이 아니라 실시간 조인 결과")
- 전제: 카탈로그 항목 A(카테고리=X)로 생성된 기존 요청 R 존재
- 절차:
  1. 요청 처리함에서 카테고리 X 필터 시 R이 보이는지 확인
  2. SCR-SRM-007에서 카탈로그 항목 A의 카테고리를 Y로 변경·저장
  3. 요청 처리함으로 돌아가 카테고리 X, Y 각각 필터링
- 기대 결과: 변경 후 R은 더 이상 카테고리 X 필터에 나타나지 않고 카테고리 Y 필터에 나타남(요청 테이블 자체 수정 없이 카탈로그 항목 카테고리 변경만으로 즉시 반영)

### TC-SRM-Q06 · 카탈로그 관리 화면 "담당 큐" select 제거 확인
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-007, 담당 큐 select 제거), @docs/03_develop/plan/service-request.md (FE 완료 기준)
- 전제: Process Owner 권한으로 SCR-SRM-007 접속
- 절차:
  1. 카탈로그 항목 생성/수정 폼 확인
- 기대 결과: "담당 큐" select가 존재하지 않음. 카테고리 select·담당자 역할 select는 정상 동작(select 후보 로드, 선택·저장 정상)

### TC-SRM-Q07 · 요청 상세 화면 "큐" 표시 제거 확인
- 근거: @docs/02_plan/api_spec/service-request.md (API-SRM-008 응답 queue 필드 제거), @docs/03_develop/plan/service-request.md (FE `RequestDetailPage.tsx` queue MetaRow 삭제)
- 전제: 임의 요청 상세(SCR-SRM-005) 접속
- 절차:
  1. 요청 상세 화면의 메타 정보 영역 확인
- 기대 결과: "큐" 라벨의 메타 항목이 존재하지 않음. `GET /api/v1/service-requests/{id}` 응답에 `queue` 필드 없음

### TC-SRM-Q08 · 배정 버튼 노출 조건 회귀(직전 form.io 유지보수 항목)
- 근거: @docs/02_plan/screen/service-request.md (SCR-SRM-004 배정 버튼 노출 조건), 직전 이력 `docs/04_test/20260717-153547/srm/result/srm.md`
- 전제: 요청 처리함 목록에 본인 배정 요청/ROUTED 이후 요청/미배정 요청 혼재
- 절차:
  1. 카테고리 필터 전환 후 각 상태의 배정 버튼 노출 여부 확인
- 기대 결과: 본인 배정 또는 ROUTED 이후 상태인 행은 배정 버튼 숨김, 그 외에는 노출(큐 제거가 이 로직에 영향 없음)
