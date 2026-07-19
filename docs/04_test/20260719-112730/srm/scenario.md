# 통합 테스트 시나리오 — srm (유지보수: 신규 컴포넌트 default align을 center-center로 변경)

## 사전 조건
- 빌드 테스트 통과(FE `npm run build`. 설계 변경 없는 순수 기본값 변경, API/DB 무관)
- Process Owner 계정(`po@itsm.local` / `Admin@1234`, SCR-SRM-007 Form 설정 팝업)
- END_USER 계정(`user@itsm.local` / `Admin@1234`, 요청 제출 폼 확인)
- playwright는 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-ALIGN-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 오류 없이 빌드 성공

### TC-ALIGN-001 · 입력 컴포넌트 신규 추가 시 기본 정렬 center-middle
- 근거: @docs/03_develop/plan/service-request.md (유지보수 절 "신규 컴포넌트 default align을 center-center로 변경", 완료 기준 1번)
- 절차:
  1. SCR-SRM-007에서 새 카탈로그 항목(또는 신규 항목)의 Form 설정 팝업을 연다
  2. 팔레트에서 text/select/radio/checkbox/date/file 중 대표로 text, select, checkbox 3종을 새로 추가한다
  3. 각 컴포넌트의 Content 설정 팝업을 열어 정렬 위젯의 선택 상태를 확인한다
  4. 캔버스 카드에서 실제 텍스트/컨트롤이 가로/세로 중앙에 위치하는지 확인한다
  5. 저장 후 요청 제출 폼(SCR-SRM-002)에서도 동일하게 중앙 정렬로 보이는지 확인한다
- 기대 결과: Content 설정 팝업 정렬 위젯이 center-middle(가로 중앙 열, 세로 중앙 행)로 기본 선택돼 있고, 캔버스·요청 제출 폼 모두 가로/세로 중앙 정렬로 렌더링됨

### TC-ALIGN-002 · guide-text 신규 추가 시 기본 정렬 center-middle
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 1번, guide-text 별도 언급)
- 절차:
  1. 같은 Form 설정 팝업에서 guide-text를 새로 추가한다
  2. Content 설정 팝업의 정렬 위젯 선택 상태 확인
  3. 캔버스 카드에서 안내 텍스트가 가로/세로 중앙에 위치하는지 확인
- 기대 결과: 정렬 위젯이 center-middle 기본 선택(기존엔 left-top이었음), 텍스트가 카드 중앙에 표시됨

### TC-ALIGN-003 · 기존 저장 컴포넌트 소급 적용 없음(회귀 없음, 중요)
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 2번, "소급 적용 없음")
- 절차:
  1. 이전 라운드에 이미 저장된 카탈로그 항목(예: 8차 테스트 "GF8 라벨확대 placeholder 기본값 테스트" 또는 4차 테스트 항목)을 Form 설정 팝업에서 연다(재편집·재저장 없이 그냥 열어서 확인만)
  2. 기존 컴포넌트들의 캔버스 위치(정렬)가 이번 변경 이전과 동일하게 유지되는지 확인
- 기대 결과: 기존 컴포넌트 정렬이 이번 기본값 변경으로 바뀌지 않고 그대로 유지됨(재편집하지 않는 한 변화 없음)

### TC-ALIGN-004 · 이전 차수 회귀(팔레트·DnD·라벨·기본값 UI)
- 근거: @docs/03_develop/plan/service-request.md (완료 기준 3번, 이전 차수 통합 항목)
- 절차:
  1. 팔레트 9종(text/textarea/select/radio/checkbox/date/file/guide-text/guide-file) 모두 캔버스에 정상 추가되는지 확인
  2. 신규 추가 컴포넌트를 드래그로 재배치(DnD)해 정상 이동하는지 확인
  3. 라벨을 하나 생성해 컴포넌트에 연결하고 경계 오버레이가 정상 표시되는지 확인
  4. select/checkbox/date 기본값 UI(각 타입별 지정 방식)가 정상 동작하는지 확인
- 기대 결과: 팔레트·DnD·라벨 오버레이·기본값 UI 모두 이전과 동일하게 정상 동작(회귀 없음)
