---
date: 20260719-101551
domain: srm
change_type: [modified]
keywords: [GridLabelOverlays labels undefined 크래시, 구 스키마 카탈로그 항목, 방어 코드]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260719-101551 · 도메인: srm

## 1. 요구사항

버그 리포트: 서비스 포털에서 "GF2 인터랙티브 테스트" 카탈로그 아이템(id=12) 클릭 시 오류가 발생한다.

## 2. 해결 방법

### 원인

4차 유지보수(20260718-214053)에서 `GridFormSchema.labels` 필드가 신규 도입됐다.
그 이전 저장된 카탈로그 항목의 `form_schema`(JSONB)에는 `labels` 키가 없다(BE는 opaque JSONB라 별도 기본값 보정을 하지 않는다).
8차 유지보수(20260719-082912)에서 `dynamic-form-renderer.tsx`에 `GridLabelOverlays`를 게이팅 없이 추가하면서, `labels` 폴백 없이 `labels={schema.labels}`로 호출했다.
이로 인해 `labels`가 `undefined`인 구 스키마를 불러오면 `GridLabelOverlays` 내부의 `labels.map(...)`에서 `TypeError`가 발생해 렌더링 전체가 크래시했다.
"GF2 인터랙티브 테스트"(id=12)는 4차 이전(2차 유지보수 테스트 라운드, 20260718-172626)에 생성됐고, 카탈로그 항목은 삭제 API가 없어 그 이후로 재저장된 적이 없어 이 결함이 재현됐다.
4차 이전 저장 후 재저장하지 않은 다른 카탈로그 항목에도 동일하게 재현되는 일반 회귀였다.

### DB

변경 없음.

### BE

변경 없음.

### FE

`dynamic-form-renderer.tsx`의 `<GridLabelOverlays components={schema.components} labels={schema.labels} />`를 `labels={schema.labels ?? []}`로 수정했다(1줄, 설계 변경 없음).

## 3. 변경 파일

- `source/frontend/src/components/common/dynamic-form-renderer.tsx`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260719-100812/srm/result/srm.md`)는 4건 전부 PASS했다.
GF2(labels 없는 구 스키마) 클릭 시 크래시가 해소됐는지, GF8(labels 있는 신규 스키마)에서 회귀가 없는지를 검증했다.
커밋 `9d58c8f`로 origin/main에 push 완료됐다.
