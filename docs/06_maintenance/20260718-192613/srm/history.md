---
date: 20260718-192613
domain: srm
change_type: [modified, removed]
keywords: [3분할 미리보기 폐기, 캔버스=미리보기 통합, GridComponentBody 오버레이]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-192613 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260718-184021, 커밋 c5232d5)의 요구사항 1번("Form 설정" 팝업 3분할 미리보기)이 사용자 피드백으로 정정된 후속 수정 건이다.
사용자가 구현 결과를 보고, 원한 것은 컴포넌트 배치 레이아웃과 미리보기가 합쳐진 형태라고 정정했다.
배치한 컴포넌트 카드 안의 content 칸에 실제 렌더링되는 내용이 보여야 한다.
그러면 컴포넌트 타입 아이콘은 없어야 자연스럽다.
별도의 미리보기 패널은 만들지 않는다.
radio/checkbox 배치방향·여백 설정, guide-text/guide-file 분리는 이번 정정과 무관하며 그대로 유지한다.

## 2. 해결 방법

### DB

변경 없음.

### BE

변경 없음.

### FE

직전 건에서 만든 팔레트/캔버스/우측 미리보기 3분할 구현을 폐기했다.
우측 별도 미리보기 패널(`DynamicFormRenderer` 45% 축소)을 제거했다.
캔버스 카드 자체가 `GridComponentBody`로 실제 렌더링 모습을 직접 보여주는 팔레트/캔버스 2분할("캔버스=미리보기 통합")로 되돌렸다.
캔버스 카드 안의 렌더링은 순수 시각적 표현으로 처리했다(`pointer-events-none`으로 상호작용을 차단하고, 카드 드래그 이동은 정상 동작).
헤더의 타입 식별용 아이콘, 캡션 텍스트, 점선 placeholder를 제거했다.
설정(톱니)·삭제 버튼은 렌더링된 콘텐츠 위에 오버레이 형태로 이동했다.
1×1 크기일 때 캡션 텍스트 대신 아이콘만 표시하던 로직(직전전 2차 건, 20260718-175042)은 타입 아이콘 자체가 없어지면서 자연히 폐기했다.
Content 설정 팝업 안 개별 실시간 미리보기(상호작용 가능, `GridComponentBody` 재사용)는 변경하지 않았다.
`form-schema.ts`에서 더는 쓰이지 않는 `GRID_PREVIEW_SCALE` 상수를 제거했다.
`index.ts` 배럴 export를 갱신했다.
`docs/02_plan/screen/service-request.md` 5.1/5.2/5.4/5.6절과 `docs/03_develop/plan/service-request.md`를 정정된 설계에 맞게 갱신했다.

### 코드 리뷰

Standards축은 diff를 직접 정독해 CLAUDE.md 문서 갱신 누락 1건을 발견했고, dev-ui가 즉시 수정해 재확인을 완료했다.
Spec축은 정정된 설계와 정확히 일치했다.

## 3. 변경 파일

- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/index.ts`
- `source/frontend/src/components/common/CLAUDE.md`
- `source/frontend/src/features/service-request/CLAUDE.md`

## 4. 테스트 결과

통합 재검증 테스트(`docs/04_test/20260718-191941/srm/result/srm.md`)는 8건 전부 PASS했다.
캔버스 카드 내 실제 렌더링 표시(비활성·드래그만 동작), 아이콘/캡션/점선 placeholder 제거, 오버레이 형태의 설정·삭제 버튼 동작을 검증했다.
radio/checkbox 배치방향·여백, guide-text/guide-file 분리 등 직전 건의 나머지 변경 사항은 회귀 없이 그대로 유지됐다.
커밋 `f0b23f2`로 origin/main에 push 완료됐다.
