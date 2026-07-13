# CLAUDE.md

IT 자산 관리/CMDB(ITAM) 기능. 자산 목록·등록/수정·상세(생애주기·폐기·만료·티켓 연계·연결 CI)·CI·CMDB 관계 뷰(CI 등록·관계 추가·영향 범위)·지표 대시보드 화면·API·타입, 유형/상태/만료 표시 매핑 제공. ASSET_MANAGER 역할 기반. API 계약: asset.md. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 폐기(RETIREMENT) 전이에 공용 게이트 연동(매칭 승인 프로세스 없으면 기존처럼 즉시 처리). 처리는 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용).

## 파일
- `api.ts` — ITAM API 호출(`assetApi`: 자산 목록/등록/상세/수정, 생애주기 전이, 폐기, 티켓 연계, CI 목록/등록/관계 등록/영향 범위, 지표).
- `types.ts` — ITAM 도메인 타입(`AssetType`/`AssetStatus`/`ExpiryStatus`, `AssetSummary`/`AssetDetail`/`Ci`/`ImpactItem`/`AssetMetrics` 등). `AssetDetail.approval`(`AssetApproval`)은 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004).
- `status.ts` — 유형·상태·만료 라벨(`typeLabel`/`statusLabel`/`expiryLabel`, `(t, value)` 시그니처)/tone 매핑, 연계 티켓 유형 라벨(`ticketTypeLabel`, AssetDetailPage 연계 폼·연결 티켓 나열 공용), CI 관계 유형 라벨(`relationTypeLabel`, CiRelationPage 관계 추가 폼·영향 범위 나열 공용), 상수 목록(`ASSET_TYPES`/`ASSET_STATUSES`).
- `format.ts` — 날짜·일시 표시 포맷터.
- `AssetListPage.tsx` — 자산 목록(SCR-ITAM-001).
- `AssetFormPage.tsx` — 자산 등록/수정(SCR-ITAM-002). id 없으면 신규 등록, 있으면 수정(같은 컴포넌트 재사용). 유형별 속성은 EAV 키-값 동적 입력.
- `AssetDetailPage.tsx` — 자산 상세·생애주기 전이·폐기(확인 다이얼로그, 승인 게이트로 버튼 disable+tooltip)·만료 강조·티켓 연계·연결 CI(SCR-ITAM-003). 승인 패널은 공용 `ApprovalPanel`(`components/common`)에 API-COM-004 조회 결과 주입해 진행 상태만 표시(매칭 없으면 패널 미노출).
- `CiRelationPage.tsx` — CI·CMDB 관계 뷰(SCR-ITAM-004). CI 목록/등록·관계 추가·영향 범위. 그래프 시각화 없이 리스트(깊이 표시) 최소 구현.
- `AssetMetricsPage.tsx` — 자산 지표 대시보드(SCR-ITAM-005).
