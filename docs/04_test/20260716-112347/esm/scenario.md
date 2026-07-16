# 통합 테스트 시나리오 — esm (배치3: textarea 필드 유형, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(service-request 시나리오에서 1회 수행한 결과 공유 — 모노레포 공용 빌드)
- 계정: po@itsm.local(PROCESS_OWNER), user@itsm.local(END_USER) — 공통 비밀번호 `Admin@1234`

## 시나리오

### TC-ESM-001 · 빌드 테스트(공유)
- 근거: 통합테스트 선행 항목
- 절차: service-request 시나리오 TC-SRM-001 결과 참조(동일 빌드)
- 기대 결과: 오류 없이 성공

### TC-ESM-002 · 부서별 카탈로그 관리 필드 유형 "여러 줄 텍스트" 추가
- 근거: @docs/03_develop/plan/service-request.md "textarea 필드 유형(공통 컴포넌트, SRM+ESM 공유)" 절, `EsmCatalogManagePage.tsx`가 동일한 `components/common/field-builder.tsx` 재사용
- 전제: po@itsm.local 로그인, "부서별 카탈로그 관리" 화면
- 절차: 부서 카탈로그 항목 편집에서 "필드 추가" → 유형 Select에서 "여러 줄 텍스트" 선택 → 라벨 입력 후 저장
- 기대 결과: SRM과 동일하게 유형 Select에 "여러 줄 텍스트" 옵션 노출, 저장 성공 후 유지됨

### TC-ESM-003 · 부서 요청 제출 폼에 textarea 렌더링 및 개행 저장
- 근거: 위와 동일, `DeptRequestSubmitPage.tsx`가 동일한 `components/common/dynamic-form.tsx` 재사용
- 전제: TC-ESM-002에서 textarea 필드가 추가된 부서 카탈로그 항목
- 절차: user@itsm.local로 부서 서비스 포털에서 해당 항목 선택 → 해당 필드가 `<textarea>`로 렌더링되는지 확인 → 개행 포함 여러 줄 텍스트 입력 후 제출
- 기대 결과: SRM과 동일하게 여러 줄 textarea로 렌더링됨. 제출 성공 후 부서 요청 상세에 개행이 유지된 텍스트가 표시됨
