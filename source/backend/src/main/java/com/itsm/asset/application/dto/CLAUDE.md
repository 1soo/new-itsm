# CLAUDE.md

asset 도메인 애플리케이션 계층의 요청·응답 DTO(record).

## 파일
- `CreateAssetRequest.java` — 자산 등록 요청(이름·유형 필수, 만료일 3종, 유형별 속성)
- `UpdateAssetRequest.java` — 자산 수정 요청(부분 갱신)
- `AssetCreatedResponse.java` — 등록 응답(id, assetKey, status)
- `AssetSummaryResponse.java` — 목록 요약 응답(만료일·만료상태·pendingApprovalTargetState(진행 중 승인 인스턴스 targetState, 2026-07-22 신규) 포함)
- `AssetDetailResponse.java` — 상세 응답(속성·만료(license/warranty/contract 각각 날짜+상태 OK/EXPIRING/EXPIRED)·생애주기이력·연계티켓·연결CI, `approval`(ApprovalInfo)에 targetState 포함(2026-07-22 신규))
- `UpdateAssetResponse.java` — 수정 응답(만료일 과거 입력 시 경고 포함)
- `LifecycleTransitionRequest.java` — 생애주기 전이 요청(targetStage)
- `StatusResponse.java` — 상태 응답(id, status)
- `LinkAssetRequest.java` — 자산 티켓 연계 요청(ticketType, ticketId)
- `LinkAssetResponse.java` — 연계 응답(assetId, ticketId)
- `CreateCiRequest.java` — CI 등록 요청(이름 필수, 유형, 연결 자산)
- `CiCreatedResponse.java` — CI 등록 응답
- `CiSummaryResponse.java` — CI 요약 응답
- `CiListResponse.java` — CI 목록 응답(content, totalElements)
- `CiRelationRequest.java` — CI 관계 등록 요청(targetCiId, relationType)
- `CiRelationResponse.java` — CI 관계 등록 응답
- `CiImpactResponse.java` — CI 영향 범위 항목 응답(ciId, name, relationType, depth)
- `AssetMetricsResponse.java` — 자산 지표 응답(utilizationRate/expiringCount/typeDistribution)
