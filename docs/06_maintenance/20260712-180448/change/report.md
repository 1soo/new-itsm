# 유지보수 이력 — change

> 유지보수 일시: 20260712-180448 · 도메인: change

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
변경(CHG) 도메인의 목록·RFC 생성·상세·일정 캘린더·지표 화면이 번역 대상이다(SCR-CHG-004는 SCR-COM-014로 이미 대체되어 제외).

## 2. 해결 방법

목록/RFC 생성/상세/일정 캘린더/지표 대시보드(SCR-CHG-001~003,005~006) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
통합검색(SCR-COM-011) 상태 배지 전환까지 연동을 완료했다.
결함 수정: 상세 화면의 "연계 항목" 패널에서 링크 유형 원시값(INCIDENT 등)이 그대로 노출되던 문제를, `linkTargetLabel` 헬퍼를 추가해 INCIDENT/PROBLEM/ASSET/COMPLIANCE_REQUIREMENT 4종을 모두 매핑하도록 해결했다.
개발 진행 중 FE 담당 에이전트가 세션 한도로 교체되는 이슈(`developer_fe`→`developer_fe-2`)가 있었으나, 파일 상태를 재검증한 뒤 안전하게 이어받아 완료했다.

## 3. 변경 파일

- `source/frontend/src/features/change/ChangeListPage.tsx`
- `source/frontend/src/features/change/ChangeCreatePage.tsx`
- `source/frontend/src/features/change/ChangeDetailPage.tsx`
- `source/frontend/src/features/change/ChangeSchedulePage.tsx`
- `source/frontend/src/features/change/ChangeMetricsPage.tsx`
- `source/frontend/src/features/change/format.ts`
- `source/frontend/src/features/change/status.ts`(결함 수정 — `linkTargetLabel`)

## 4. 테스트 결과

통합 테스트 전 항목 PASS했다(재테스트 포함).
커밋 `73807d3`로 반영했다.
