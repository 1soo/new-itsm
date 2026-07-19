---
date: 20260719-100812
domain: srm
result: pass
keywords: [labels옵셔널체이닝, 구스키마크래시수정, GridLabelOverlays, 회귀없음]
---

# 통합 테스트 결과 — srm 버그 수정 재테스트: labels 없는 구 스키마 로드 시 크래시 (20260719-100812)

## 요약
- 총 4건 · 성공 4 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUGFIX-B01 | PASS | FE `npm run build`(tsc -b && vite build) 성공 | - |
| TC-BUGFIX-001 | PASS | `dynamic-form-renderer.tsx` L241 `labels={schema.labels ?? []}` 수정 확인 후, END_USER로 "GF2 인터랙티브 테스트"(id=12, labels 필드 없는 4차 이전 구 스키마) 요청 제출 폼(`/portal/requests/new?item=12`)을 열어도 크래시(흰 화면/에러 바운더리) 없이 정상 렌더링됨(콘솔에도 TypeError 없음, 기존부터 있던 무관한 401만 존재). 구 `type:"guide"` 필드가 빈 텍스트 input으로 오표시되는 현상은 계획서에 명시된 대로 이번 범위 밖이라 실패로 처리하지 않음(참고 확인만) | - |
| TC-BUGFIX-002 | PASS | Process Owner로 SCR-SRM-007에서 "GF2 인터랙티브 테스트" 선택 시 A1 축소 미리보기도 크래시 없이 정상 렌더링됨(콘솔 TypeError 없음) | shots/bugfix_002_gf2_a1_no_crash.png |
| TC-BUGFIX-003 | PASS | labels 필드가 있는(8차에서 생성) "GF8 라벨확대 placeholder 기본값 테스트" 항목의 A1 축소 미리보기·요청 제출 폼 양쪽 모두에서 "필수그룹" 라벨 경계 테두리+legend 텍스트가 기존과 동일하게 정상 표시됨(회귀 없음) | shots/bugfix_003_gf8_labels_regression.png, shots/bugfix_003_gf8_request_form_regression.png |

## 테스트 데이터 처리
- 기존 데이터만 조회(신규 생성 없음).

## 실패 항목 분석
없음.
