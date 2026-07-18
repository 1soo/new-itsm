# 통합 테스트 시나리오 — srm (그리드 폼 빌더 8차: 라벨 경계 표시 확대·placeholder 폴백 제거·기본값 UI 동일화·타이틀 배치·i18n 전환)

## 사전 조건
- 빌드 테스트 통과(FE `npm run build`. API/DB 변경 없음 — 계획서 명시, dev-be/dev-db 소집 없었음)
- Process Owner 권한 계정(`po@itsm.local` / `Admin@1234`, SCR-SRM-007 Form 설정)
- END_USER 계정(`user@itsm.local` / `Admin@1234`, SCR-SRM-002 요청 제출)
- 신규 카탈로그 항목을 생성해 격리된 캔버스에서 검증한다.
- playwright는 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-GF8-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/frontend`에서 `npm run build` 실행(API/DB 변경 없어 BE 빌드는 생략)
- 기대 결과: 오류 없이 빌드 성공

### TC-GF8-001 · 요청 제출 폼·A1 축소 미리보기에서 라벨 경계 표시
- 근거: @docs/02_plan/screen/service-request.md (5.8절 "라벨 경계 그룹 표시를 빌더 캔버스 전용에서 dynamic-form-renderer.tsx 자체로 확대"), @docs/03_develop/plan/service-request.md (8차 요구사항 1번, 핵심)
- 절차:
  1. 라벨을 생성해 컴포넌트 1개 이상에 지정(showBorder 기본값 true)
  2. 카탈로그 항목 저장 후 SCR-SRM-007 A1 축소 미리보기에서 경계 테두리+legend 텍스트가 표시되는지 확인(직전까지는 빌더 캔버스에서만 보였음)
  3. 요청 제출 폼(SCR-SRM-002, `/portal/requests/new`)에서도 동일하게 경계 테두리+legend 텍스트가 표시되는지 확인
- 기대 결과: A1 축소 미리보기·요청 제출 폼 둘 다 라벨 경계(테두리+legend 텍스트) 노출

### TC-GF8-002 · placeholder 하드코딩 폴백 제거
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "하드코딩 기본 placeholder 폴백 완전 제거"), @docs/03_develop/plan/service-request.md (8차 요구사항 2번)
- 절차:
  1. date/file/select 컴포넌트에 placeholder를 지정하지 않은 상태로 요청 제출 폼에서 렌더링 확인 → "날짜를 선택하세요"/"파일을 선택하세요"/"선택" 같은 하드코딩 문구가 전혀 나오지 않고 완전히 빈 상태로 보이는지 확인
  2. 그중 하나에 placeholder를 지정 → 그 값이 그대로 표시되는지 확인
- 기대 결과: 미지정 시 완전히 빈 상태, 지정 시 그 값 표시(하드코딩 폴백 없음)

### TC-GF8-003 · Content 설정 기본값 UI 타입별 동일화
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "기본값 설정 UI를 컴포넌트 실제 입력 타입과 동일화"), @docs/03_develop/plan/service-request.md (8차 요구사항 3번)
- 절차:
  1. select/radio 컴포넌트의 Content 설정 "기본값" 항목이 옵션 목록 중 단일 선택(pill 버튼 등) UI인지 확인, 같은 옵션을 다시 누르면 해제되는지 확인
  2. checkbox 컴포넌트의 "기본값" 항목이 여러 개 체크 가능한 체크박스 그룹인지 확인
  3. date 컴포넌트의 "기본값" 항목이 네이티브 date input인지 확인
  4. file 컴포넌트에는 "기본값" 항목 자체가 없는지 확인
  5. select 단일 선택, checkbox 다중 선택(2개 이상), date 값을 각각 설정한 뒤 요청 제출 폼에서 그 기본값이 정확히 반영되는지 확인(checkbox는 지정한 옵션들이 모두 미리 체크됨)
- 기대 결과: 타입별 기본값 UI가 실제 입력 타입과 동일화, file은 기본값 UI 없음, 설정값이 요청 제출 폼에 정확히 반영

### TC-GF8-004 · 읽기전용·필수 여부 타이틀 배치, 다른 Modal 회귀 없음
- 근거: @docs/02_plan/screen/service-request.md (5.4절 "팝업 타이틀 우측으로 이동"), @docs/03_develop/plan/service-request.md (8차 요구사항 4번)
- 절차:
  1. text(또는 다른 입력 7종) 컴포넌트 Content 설정 팝업을 열어 "읽기 전용"/"필수 여부" 체크박스가 본문이 아니라 팝업 타이틀("컴포넌트 설정") 우측에 있는지 확인
  2. guide-text/guide-file 컴포넌트 Content 설정 팝업에는 이 체크박스 자체가 없는지 확인
  3. 라벨 생성/수정 팝업, SRM 카탈로그 카테고리 관리 "새 카테고리" 모달의 타이틀 레이아웃에 변화가 없는지(`titleExtra` 미지정 시 기존과 동일) 확인
- 기대 결과: 읽기전용·필수 여부가 타이틀 우측에 위치(입력 7종만), guide-text/guide-file은 없음, 다른 Modal은 영향 없음

### TC-GF8-005 · i18n 전환(ko/en)
- 근거: @docs/02_plan/screen/common.md (6.7/6.8절 i18n 전환 방침), @docs/03_develop/plan/service-request.md (8차 요구사항 5번)
- 절차:
  1. 언어를 English로 전환
  2. SCR-SRM-007 "Form 설정" 팝업(팔레트 라벨, Content 설정 항목명, 버튼 텍스트 등)이 영어로 표시되는지 확인
  3. 요청 제출 폼(SCR-SRM-002)의 텍스트(제출/취소 버튼, 오류 메시지 등)가 영어로 표시되는지 확인
  4. 한국어로 재전환해 정상 복귀하는지 확인
- 기대 결과: en 전환 시 빌더·요청 제출 폼 텍스트 모두 영어로 정상 전환, ko 재전환 시 정상 복귀

### TC-GF8-R01 · 이전 차수 항목 회귀 확인
- 근거: @docs/03_develop/plan/service-request.md (8차 완료 기준 "기존 SRM 회귀 없음")
- 절차:
  1. 캔버스 카드=실제 렌더링(순수 시각적, 드래그 이동 가능) 재확인
  2. DnD 배치(드래그 직후 즉시 클릭 포함) 재확인
  3. 팔레트 9종(label 없음) 재확인
  4. 높이 상한 세분화 재확인
  5. 정규식 text 전용 UI 재확인
  6. 9방향 정렬 위젯 재확인
  7. 라벨 생성/수정/삭제 시 참조 컴포넌트의 labelId 해제(컴포넌트 자체 유지) 재확인
  8. 미니팝업 위치 통일(Content 설정·라벨 생성 팝업이 동일한 화면 고정 좌표) 재확인
- 기대 결과: 이전 차수까지의 항목 전부 정상 동작(회귀 없음)
