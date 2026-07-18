# 통합 테스트 시나리오 — srm (그리드 폼 빌더 7차: 미니팝업 위치·순서 보정)

## 사전 조건
- 빌드 테스트 통과(FE `npm run build`. API/DB 변경 없음 — 계획서 명시, dev-be/dev-db 소집 없었음)
- Process Owner 권한 계정(`po@itsm.local` / `Admin@1234`, SCR-SRM-007 Form 설정)
- 신규 카탈로그 항목을 생성해 격리된 캔버스에서 검증한다.
- playwright는 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-GF7-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/frontend`에서 `npm run build` 실행(API/DB 변경 없어 BE 빌드는 생략)
- 기대 결과: 오류 없이 빌드 성공

### TC-GF7-001 · Content 설정 팝업 라벨(태그) Select 최상단 위치
- 근거: @docs/02_plan/screen/service-request.md (5.4절 라벨 지정 위치 변경), @docs/03_develop/plan/service-request.md (7차 요구사항 1번)
- 절차:
  1. text 컴포넌트의 Content 설정 팝업을 열고 "라벨(태그)" Select가 팝업 맨 위(다른 설정 항목보다 앞)에 있는지 확인
  2. 그 아래로 input 폭/정렬/Placeholder/기본값/필수 여부/정규식 등 타입별 설정 항목이 이어지는지 확인
- 기대 결과: 라벨(태그) Select가 최상단, 타입별 설정 항목이 그 아래로 이어짐

### TC-GF7-002 · Content 설정 팝업 고정 위치(트리거 무관)
- 근거: @docs/02_plan/screen/service-request.md (5.8절 팝업 위치 통일), @docs/03_develop/plan/service-request.md (7차 요구사항 2번)
- 절차:
  1. 캔버스 좌상단 카드의 Content 설정 팝업을 열어 화면상 팝업 위치(좌표)를 확인
  2. 캔버스 우하단(또는 스크롤 필요한 먼 위치) 카드의 Content 설정 팝업을 열어 화면상 팝업 위치를 확인
  3. 두 위치가 동일한 화면 기준 고정 좌표(가로 정중앙, 세로는 정중앙보다 살짝 위)인지 비교
- 기대 결과: 트리거(카드) 위치와 무관하게 항상 동일한 화면 고정 좌표에 팝업이 뜸

### TC-GF7-003 · 라벨 생성/수정 팝업과 Content 설정 팝업 위치 일치
- 근거: @docs/02_plan/screen/service-request.md (5.8절 팝업 위치 통일), @docs/03_develop/plan/service-request.md (7차 요구사항 2번)
- 절차:
  1. Content 설정 팝업을 열어 위치(좌표)를 기록
  2. 닫고 라벨 생성 팝업("라벨 추가" 버튼)을 열어 위치(좌표)를 기록
  3. 두 좌표가 정확히 일치하는지 비교
- 기대 결과: 두 미니 팝업이 동일한 화면 고정 좌표를 공유

### TC-GF7-004 · 팝업 내부 조작이 캔버스 드래그로 오인되지 않음(Popover→Modal 전환 회귀)
- 근거: @docs/03_develop/plan/service-request.md (7차 회귀 중요 포인트, dev-lead 지정)
- 절차:
  1. Content 설정 팝업을 열고 내부 입력(라벨 Select, Placeholder 텍스트, 체크박스, 3×3 정렬 위젯 등)을 조작
  2. 조작 중 뒤의 캔버스 카드가 움직이거나 드래그로 오인되지 않는지 확인
  3. 설정 변경 후 팝업을 닫고(바깥 클릭 또는 X) 캔버스 카드에 변경사항(예: placeholder)이 정상 반영됐는지 확인
  4. 라벨 생성 팝업에서도 동일하게 내부 입력 조작이 팔레트/캔버스에 영향 주지 않는지 확인
- 기대 결과: 팝업 내부 조작이 캔버스에 전혀 영향 없음, 설정 변경이 정상 반영됨, 팝업 열기/닫기 정상 동작

### TC-GF7-005 · 다른 화면 Modal 회귀(공용 ui/dialog.tsx 미변경 확인)
- 근거: @docs/03_develop/plan/service-request.md (7차 완료 기준 "공용 ui/dialog.tsx는 변경되지 않아... 회귀 없음")
- 절차:
  1. SRM 카탈로그 카테고리 관리 탭의 "새 카테고리" 모달을 열어 화면 정중앙에 정상 표시되는지 확인(7차 변경의 영향을 받지 않아야 함)
- 기대 결과: 다른 Modal 사용처는 기존과 동일하게 화면 정중앙에 정상 표시(회귀 없음)

### TC-GF7-R01 · 이전 차수 항목 회귀 확인
- 근거: @docs/03_develop/plan/service-request.md (7차 완료 기준 "기존 SRM 회귀 없음")
- 절차:
  1. 캔버스 카드=실제 렌더링(순수 시각적, 드래그 이동 가능) 재확인
  2. DnD 배치(드래그 직후 즉시 클릭 포함) 재확인
  3. 팔레트 9종(label 없음) 재확인
  4. 높이 상한 세분화 재확인
  5. 정규식 text 전용 UI 재확인
  6. 9방향 정렬 위젯 재확인
  7. 라벨 legend 스타일·showBorder(체크 시 텍스트만 유지) 재확인
- 기대 결과: 이전 차수까지의 항목 전부 정상 동작(회귀 없음)
