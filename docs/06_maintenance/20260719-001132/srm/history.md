---
date: 20260719-001132
domain: srm
change_type: [modified]
keywords: [라벨 지정 UI 최상단 이동, 미니팝업 위치 통일, Popover→Modal 전환, MINI_POPUP_POSITION_CLASS]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260719-001132 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260718-234334, 개별 미리보기 제거+라벨 표시 보정 6차)에 이은 후속 보정 요청 2건이다.
컴포넌트 세팅 미니 팝업(Content 설정 팝업)에서 라벨 설정하는 부분(라벨 지정 UI)을 팝업 최상단으로 이동한다.
컴포넌트 세팅 미니 팝업의 생성 위치를 화면 정중앙이 아니라 그보다 조금 위쪽에 고정 생성한다.
모든 미니 팝업이므로 라벨 생성/수정 미니팝업 등 다른 미니팝업들도 동일한 위치로 통일한다.

## 2. 해결 방법

### DB

변경 없음.

### BE

변경 없음.

### FE

`ComponentSettingsPopover`(Content 설정 팝업) 하단에 있던 `labelId` Select 블록을 팝업 최상단(타입별 설정 항목보다 앞)으로 옮겼다.
Content 설정 팝업(기존 Radix Popover, 트리거인 톱니 아이콘에 상대적으로 배치)과 라벨 생성/수정 팝업(기존 Modal, 화면 정중앙 고정)의 위치를 화면 뷰포트 기준 동일한 고정 좌표로 통일했다.
Content 설정 팝업은 트리거 상대 배치를 포기하고 Modal(Radix Dialog) 방식으로 전환했다.
두 팝업이 `MINI_POPUP_POSITION_CLASS` 상수를 공유해 가로는 정중앙, 세로는 정중앙보다 살짝 위(`top-[42%]`)인 동일한 위치에 뜨도록 했다.
공용 `ui/dialog.tsx`는 변경하지 않아 다른 화면의 Modal에는 영향이 없다.

### 코드 리뷰 중 확인한 사항

Standards축 수동 리뷰에서 `top-[42%]` className 오버라이드가 공용 `top-1/2`를 실제로 이기는지(Tailwind 캐스케이드 순서 이슈 가능성) 의문이 제기됐다.
dev-ui가 `getComputedStyle` 픽셀 측정으로 실제 42%가 적용됨을 검증했고, `cn()`이 tailwind-merge 기반이라 결정적으로 해소됨을 코드로도 확인했다.
Spec축은 설계 2건 요구사항 전부 부합했다.

## 3. 변경 파일

- `source/frontend/src/components/common/dynamic-form-builder.tsx`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260719-000230/srm/result/srm.md`)는 7건 전부 PASS했다.
라벨 지정 UI 최상단 이동, Content 설정 팝업과 라벨 생성/수정 팝업이 동일한 고정 좌표(가로 정중앙·세로 정중앙보다 살짝 위)에 뜨는지를 검증했다.
Popover→Modal 구조 전환에 따른 회귀(팝업 내부 조작이 캔버스 드래그로 오인되는지, 다른 화면 Modal 영향 등)도 함께 확인했으며 회귀는 없었다.
커밋 `4b170f3`으로 origin/main에 push 완료됐다.
