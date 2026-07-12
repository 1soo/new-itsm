# 유지보수 이력 — incident

> 유지보수 일시: 20260712-180448 · 도메인: incident

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
인시던트(INC) 도메인의 목록·등록·상세·포스트모템·지표 화면 전체가 번역 대상이다.

## 2. 해결 방법

목록/등록/상세/포스트모템/지표 대시보드(SCR-INC-001~005) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
통합검색(SCR-COM-011) 상태 배지 전환까지 연동을 완료했다.
결함 수정: `format.ts`의 `formatMinutes`가 값 없음("미산정"→Not calculated)과 값 있음(단위 "분"→분 단위 라벨) 두 분기 모두 원시 한국어를 반환하던 문제를 번역 키 기반으로 통일했다.
티켓 상세의 타임라인 메시지는 BE에서 생성되어 DB에 저장된 하드코딩 데이터임을 확인해 이번 번역 대상에서 제외했다(이후 유사 구조를 가진 다른 도메인도 동일하게 처리).

## 3. 변경 파일

- `source/frontend/src/features/incident/IncidentListPage.tsx`
- `source/frontend/src/features/incident/IncidentCreatePage.tsx`
- `source/frontend/src/features/incident/IncidentDetailPage.tsx`
- `source/frontend/src/features/incident/IncidentMetricsPage.tsx`
- `source/frontend/src/features/incident/PostmortemPage.tsx`
- `source/frontend/src/features/incident/status.ts`
- `source/frontend/src/features/incident/format.ts`(결함 수정 — `formatMinutes`)

## 4. 테스트 결과

통합 테스트 12건 전부 PASS했다(재테스트 포함).
커밋 `348e482`로 반영했다.
