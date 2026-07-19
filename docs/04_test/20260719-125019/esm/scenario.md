# 통합 테스트 시나리오 — esm (유지보수: 동적 양식을 SRM 그리드 폼 빌더로 전면 전환)

## 사전 조건
- 빌드 테스트 통과(FE `npm run build`, BE 빌드/테스트는 dev-be가 전체 392건 통과 확인 완료 — 참고로 명시만, 재실행은 tester 범위 아님)
- Process Owner 계정(`po@itsm.local` / `Admin@1234`, SCR-ESM-006 카탈로그 관리)
- END_USER 계정(`user@itsm.local` / `Admin@1234`, SCR-ESM-002 부서 요청 제출)
- playwright는 새 창(새 browser context)에서 수행, storage 초기화

## 시나리오

### TC-ESM-B01 · 빌드 테스트
- 근거: 통합 테스트 공통 선행 항목
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 오류 없이 빌드 성공

### TC-ESM-001 · SCR-ESM-006 그리드 폼 빌더로 신규 카탈로그 항목 설계·저장·재조회
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-006, 2026-07-19 변경 이력), @docs/02_plan/api_spec/esm.md (API-ESM-002/003)
- 절차:
  1. Process Owner로 SCR-ESM-006(부서별 카탈로그 관리)에서 새 항목을 생성하고 담당 부서를 지정한다
  2. "Form 설정" 버튼으로 팝업을 열어 text/textarea/select/radio/checkbox/date/file/guide-text/guide-file 9종을 배치하고 "적용" → 저장한다
  3. 저장 후 A1 축소 미리보기에 배치가 그대로 반영되는지 확인한다
  4. 페이지를 재조회(재진입)해 동일 스키마가 유지되는지 확인한다
- 기대 결과: 팔레트 9종 모두 정상 배치, 저장·재조회 시 스키마(배치·설정) 그대로 유지, 축소 미리보기 반영

### TC-ESM-002 · SCR-ESM-002 그리드 폼 렌더링·제출(정상 경로)
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-002), @docs/02_plan/api_spec/esm.md (API-ESM-005)
- 절차:
  1. TC-ESM-001에서 만든 카탈로그 항목으로 END_USER가 부서 요청 제출 폼을 연다
  2. 그리드 배치 그대로 렌더링되는지 확인 후 필수값을 채워 제출한다
- 기대 결과: 카탈로그의 그리드 배치가 그대로 렌더링되고, 정상 제출 시 201 성공

### TC-ESM-003 · 클라이언트 필수/정규식 검증(첫 위반 1건) + 서버 재검증(400)
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-002 상태·인터랙션), @docs/02_plan/api_spec/esm.md (API-ESM-005 400 응답)
- 절차:
  1. 같은 폼에서 필수 필드를 비운 채(또는 정규식 위반 값으로) 제출을 시도한다
  2. 폼 하단에 표시되는 오류가 `components` 배열 순서상 첫 번째 위반 1건만인지 확인한다
  3. 클라이언트 검증을 우회해 서버로 직접 위반 데이터를 보냈을 때(또는 서버 재검증 경로) 400이 반환되는지 확인한다
- 기대 결과: 클라이언트는 첫 번째 위반 1건만 표시, 서버도 동일 계약으로 400 재검증

### TC-ESM-004 · 온보딩/오프보딩 유형 회귀(대상자명 필수 + 체크리스트 자동 생성)
- 근거: @docs/01_analyze/feature/esm.md (FEAT-ESM-005 REQ-ESM-005, FEAT-ESM-006 REQ-ESM-006), @docs/02_plan/api_spec/esm.md (API-ESM-005 targetUserName)
- 절차:
  1. 온보딩(또는 오프보딩) 유형 카탈로그 항목으로 요청을 제출한다(체크리스트 템플릿이 정의된 기존 항목 사용)
  2. 대상자명(targetUserName)을 비우고 제출 시 거부되는지 확인한다
  3. 대상자명을 채워 정상 제출 시 체크리스트 자동 생성 안내 토스트가 노출되는지, 하위 작업이 관련 부서에 배정되는지 확인한다
- 기대 결과: 대상자명 미입력 시 제출 거부, 정상 제출 시 안내 토스트 노출 및 체크리스트/하위 작업 자동 생성(REQ-ESM-005/006 회귀 없음)

### TC-ESM-005 · 마이그레이션 리셋·백필 데이터 확인
- 근거: @docs/02_plan/database/esm.md (4/6절, 기존 데이터 리셋·백필)
- 절차:
  1. 마이그레이션 이전부터 존재하던 기존 카탈로그 항목(양식 필드가 있었던 것)을 SCR-ESM-006에서 열어 `form_schema`가 빈 그리드(`{"components":[],"labels":[]}`)로 리셋됐는지 확인한다
  2. 마이그레이션 이전 제출 이력이 있는 부서 요청 상세(SCR-ESM-005)를 열어 백필된 `form_values`가 정상 표시되는지 확인한다
- 기대 결과: 기존 카탈로그 항목은 빈 그리드로 리셋되어 있고, 기존 제출 건은 상세 화면에서 백필된 값이 정상 표시됨

### TC-ESM-006 · 레거시 EAV 잔재 없음(참고 확인)
- 근거: @docs/03_develop/plan/esm.md (2026-07-19 유지보수 절, 완료 기준)
- 절차:
  1. TC-ESM-B01 FE 빌드 로그에 `field-builder`/`dynamic-form.tsx`(레거시) 관련 미해결 참조·타입 오류가 없는지 확인한다(BE 전체 테스트 통과는 dev-be가 이미 확인했으므로 재실행하지 않고 그 결과를 신뢰)
- 기대 결과: 레거시 파일 참조로 인한 빌드 오류 없음
