# CLAUDE.md

infra 도메인의 요청·응답 DTO(record).

## 파일
- `MetricCreateRequest.java` / `MetricCreatedResponse.java` — 지표 등록 요청/응답(API-IOM-001, alertGenerated 포함)
- `MetricResponse.java` — 지표 시계열 조회 응답 원소(API-IOM-002)
- `ThresholdResponse.java` / `ThresholdUpdateRequest.java` — 임계치 목록/설정(API-IOM-003/004)
- `AlertResponse.java` — 임계치 초과 알림 목록 응답 원소(API-IOM-005, assetKey 조인)
- `UptimeTargetRequest.java` / `UptimeStatusResponse.java` — 자산 가동률 목표 설정/현황(API-IOM-007/008, met은 목표 미설정 시 null)
- `CapacityPlanCreateRequest.java` / `CapacityPlanCreatedResponse.java` / `CapacityPlanResponse.java` — 용량 계획 등록/목록(API-IOM-009/010, utilizationRate 계산값)
- `InfraReportResponse.java` — 인프라 지표 리포팅 응답(API-IOM-011)
