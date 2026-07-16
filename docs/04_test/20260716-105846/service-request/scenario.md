# 통합 테스트 시나리오 — service-request (배치2: SRM 카탈로그 카테고리 CRUD, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(백엔드 Gradle build, 프론트엔드 tsc+vite build)
- 계정: po@itsm.local(PROCESS_OWNER) — 공통 비밀번호 `Admin@1234`
- 시드 카테고리: 계정/하드웨어/소프트웨어 3종(재정리됨). "노트북 신청"→하드웨어, "비밀번호 초기화"→계정 매핑 가정

## 시나리오

### TC-SRM-001 · 빌드 테스트
- 근거: 통합테스트 선행 항목
- 절차: 1. 백엔드 `./gradlew build` 2. 프론트엔드 `npm run build`
- 기대 결과: 둘 다 오류 없이 성공

### TC-SRM-002 · 카테고리 관리 목록 조회
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-009, @docs/02_plan/api_spec/service-request.md API-SRM-018
- 전제: po@itsm.local 로그인, `/admin/service-catalog` "카테고리 관리" 탭 진입
- 절차: 목록 표 확인
- 기대 결과: 이름·표시 순서(sortOrder 오름차순)·사용 항목수(itemCount) 표시. 시드 카테고리(계정/하드웨어/소프트웨어) 노출

### TC-SRM-003 · 카테고리 생성
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-009, API-SRM-019
- 절차: "새 카테고리" 버튼 클릭 → 이름·표시순서 입력 후 저장
- 기대 결과: 201, 목록에 신규 카테고리 반영

### TC-SRM-004 · 카테고리 이름 중복 생성 시 409
- 근거: API-SRM-019 "409 이름 중복"
- 절차: TC-SRM-003과 동일 이름으로 재생성 시도
- 기대 결과: 409(`CATEGORY_NAME_DUPLICATE`), 목록에 중복 항목 추가되지 않음

### TC-SRM-005 · 카테고리 수정
- 근거: API-SRM-020
- 절차: TC-SRM-003 카테고리 수정 버튼 클릭 → 이름 변경 후 저장
- 기대 결과: 200, 목록에 변경된 이름 반영

### TC-SRM-006 · 참조 중인 카테고리 삭제 시 409(CATEGORY_IN_USE)
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-009 "참조 중인 카테고리 삭제 거부", API-SRM-021
- 전제: 카탈로그 항목 1건 이상이 참조하는 카테고리(예: 하드웨어, "노트북 신청"이 참조)
- 절차: 카테고리 관리 탭에서 해당 카테고리 삭제 시도(확인 다이얼로그 진행)
- 기대 결과: 409(`CATEGORY_IN_USE`), 목록에 해당 카테고리 그대로 남아있음

### TC-SRM-007 · 참조 없는 카테고리 정상 삭제
- 근거: 위와 동일
- 전제: TC-SRM-005에서 수정한 카테고리(참조 없음)
- 절차: 삭제 버튼 클릭 → 확인 다이얼로그 진행
- 기대 결과: 200/204, 목록에서 해당 카테고리 사라짐

### TC-SRM-008 · 카탈로그 항목 폼 카테고리 select — 지정·미분류 모두 가능
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007 "카테고리 선택" 요소, API-SRM-018(후보 목록)
- 절차: "카탈로그 항목" 탭에서 신규 항목 생성 시 카테고리 select 오픈 → 후보(계정/하드웨어/소프트웨어) 확인 → 하나 선택 후 이름·양식 입력해 저장. 별도로 미선택("미분류") 상태로도 저장 시도
- 기대 결과: select에 카테고리 목록 노출, 선택 저장 시 상세 조회 응답에 지정한 `categoryId`/`categoryName` 반영, 미선택 저장 시 `categoryId`=null

### TC-SRM-009 · 카탈로그 항목 편집 진입 시 카테고리 프리필
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007 "편집 진입 시 프리필"
- 전제: "노트북 신청" 카탈로그 항목(카테고리=하드웨어로 지정된 상태)
- 절차: 목록에서 "노트북 신청" 클릭해 편집 진입
- 기대 결과: 카테고리 select 값이 상세 조회 응답(`categoryId`)의 기존 값 "하드웨어"로 프리필됨

### TC-SRM-010 · 서비스 포털 카드 categoryName 배지 표시
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-001, API-SRM-001(`categoryName`)
- 전제: user@itsm.local 로그인, 서비스 포털 진입
- 절차: "노트북 신청"·"비밀번호 초기화" 카드 확인
- 기대 결과: "노트북 신청" 카드에 "하드웨어" 배지, "비밀번호 초기화" 카드에 "계정" 배지 표시

### TC-SRM-011 · 존재하지 않는 categoryId로 카탈로그 항목 생성/수정 시 404
- 근거: API-SRM-003/004 "404 존재하지 않는 categoryId"
- 절차: `POST /api/v1/service-catalog/items`에 존재하지 않는 임의의 큰 `categoryId` 지정해 호출
- 기대 결과: 404
