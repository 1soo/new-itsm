---
date: 20260719-113616
domain: srm
change_type: [modified]
keywords: [default align center-center, verticalAlign middle, guide-text textAlign center, 소급 적용 없음]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260719-113616 · 도메인: srm

## 1. 요구사항

컴포넌트의 default align을 center-center로 적용한다.
적용 대상은 입력 7종(text/textarea/select/radio/checkbox/date/file)과 guide-text(안내 텍스트) 전부다.
입력 7종은 가로 align이 이미 center라 유지하고, 세로 verticalAlign 기본값만 top에서 middle로 바꾼다.
guide-text는 가로 textAlign 기본값을 left에서 center로, 세로 textVerticalAlign 기본값을 top에서 middle로 바꾼다.
신규 생성되는 컴포넌트에만 적용하고, 기존에 이미 배치·저장된 컴포넌트의 정렬값은 그대로 유지한다(소급 적용 없음).

## 2. 해결 방법

### DB

변경 없음.

### BE

변경 없음.

### FE

`dynamic-form-builder.tsx`의 `buildNewComponent()`를 수정했다.
guide-text 분기의 초기값을 `textAlign: "left", textVerticalAlign: "top"`에서 `textAlign: "center", textVerticalAlign: "middle"`로 바꿨다.
입력 컴포넌트 분기의 초기값 중 `verticalAlign`을 `"top"`에서 `"middle"`로 바꿨다(`align`은 이미 `"center"`라 그대로 유지).
`form-schema.ts`의 `GridComponentInput.verticalAlign`/`GridGuideTextComponent.textAlign`/`textVerticalAlign` JSDoc 주석을 실제 기본값에 맞게 갱신했다.
렌더러(`dynamic-form-renderer.tsx`)의 `?? "top"`/`?? "left"` 폴백 로직과 Content 설정 팝업의 동일 폴백 로직은 그대로 뒀다 — 신규 컴포넌트는 생성 시점에 값이 명시적으로 채워지므로 이 폴백은 값이 아예 없는 구 데이터에만 적용되고, 그 구 데이터는 소급 적용 대상이 아니므로 기존 값(top/left)으로 남는 게 의도한 동작이다.

## 3. 변경 파일

- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/form-schema.ts`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260719-112730/srm/result/srm.md`)는 4건 전부 PASS했다.
신규 생성한 입력 컴포넌트·guide-text가 center-middle로 적용되는지, 기존 저장 컴포넌트(GF8, id=18)에는 소급 적용되지 않고 그대로 유지되는지를 검증했다.
커밋 `4ba82e2`로 origin/main에 push 완료됐다.
