# CLAUDE.md

IT 자산 관리/CMDB(ITAM) 기능. 자산 목록·등록/수정·상세(생애주기·폐기·만료·티켓 연계·연결 CI)·CI·CMDB 관계 뷰(CI 등록·관계 추가·영향 범위)·지표 대시보드 화면과 API·타입, 유형/상태/만료 표시 매핑을 제공한다. ASSET_MANAGER 역할 기반. API 계약은 asset.md 기준.

## 파일
- `api.ts` — ITAM API 호출(`assetApi`: 자산 목록/등록/상세/수정, 생애주기 전이, 폐기, 티켓 연계, CI 목록/등록/관계 등록/영향 범위, 지표).
- `types.ts` — ITAM 도메인 타입(`AssetType`/`AssetStatus`/`ExpiryStatus`, `AssetSummary`/`AssetDetail`/`Ci`/`ImpactItem`/`AssetMetrics` 등).
- `status.ts` — 유형·상태·만료 라벨/tone 매핑, 상수 목록(`ASSET_TYPES`/`ASSET_STATUSES`).
- `format.ts` — 날짜·일시 표시 포맷터.
- `AssetListPage.tsx` — 자산 목록(SCR-ITAM-001).
- `AssetFormPage.tsx` — 자산 등록/수정(SCR-ITAM-002). id 없으면 신규 등록, 있으면 수정(같은 컴포넌트 재사용). 유형별 속성은 EAV 키-값 동적 입력.
- `AssetDetailPage.tsx` — 자산 상세·생애주기 전이·폐기(확인 다이얼로그)·만료 강조·티켓 연계·연결 CI(SCR-ITAM-003).
- `CiRelationPage.tsx` — CI·CMDB 관계 뷰(SCR-ITAM-004). CI 목록/등록·관계 추가·영향 범위. 그래프 시각화 없이 리스트(깊이 표시)로 최소 구현.
- `AssetMetricsPage.tsx` — 자산 지표 대시보드(SCR-ITAM-005).
