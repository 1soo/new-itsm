# 통합 테스트 시나리오 — service-request (배치3: textarea 필드 유형, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(백엔드 Gradle build, 프론트엔드 tsc+vite build)
- 계정: po@itsm.local(PROCESS_OWNER), user@itsm.local(END_USER) — 공통 비밀번호 `Admin@1234`

## 시나리오

### TC-SRM-001 · 빌드 테스트
- 근거: 통합테스트 선행 항목
- 절차: 1. 백엔드 `./gradlew build` 2. 프론트엔드 `npm run build`
- 기대 결과: 둘 다 오류 없이 성공

### TC-SRM-002 · 카탈로그 관리 필드 유형 "여러 줄 텍스트" 추가
- 근거: @docs/03_develop/plan/service-request.md "textarea 필드 유형(공통 컴포넌트)" 절, `components/common/field-builder.tsx`
- 전제: po@itsm.local 로그인, `/admin/service-catalog` "카탈로그 항목" 탭
- 절차: 신규(또는 기존) 항목 편집에서 "필드 추가" → 유형 Select에서 "여러 줄 텍스트" 선택 → 라벨 입력 후 저장
- 기대 결과: 유형 Select에 "여러 줄 텍스트" 옵션 노출, 저장 성공 후 재조회 시 해당 필드 유형이 유지됨

### TC-SRM-003 · 요청 제출 폼에 textarea 렌더링 및 개행 저장
- 근거: @docs/03_develop/plan/service-request.md, `components/common/dynamic-form.tsx`
- 전제: TC-SRM-002에서 textarea 필드가 추가된 카탈로그 항목
- 절차: user@itsm.local로 서비스 포털에서 해당 항목 선택 → 해당 필드가 `<textarea>`로 렌더링되는지 확인 → 개행 포함 여러 줄 텍스트 입력 후 제출
- 기대 결과: 입력 필드가 한 줄 입력창이 아닌 여러 줄 textarea로 렌더링됨. 제출 성공 후 요청 상세의 요청 내용에 개행이 유지된 텍스트가 표시됨
