# CLAUDE.md

infra 도메인의 엔티티·enum·리포지토리 계약.

## 파일
- `InfraMetric.java` — 인프라 지표 레코드 엔티티(수동 입력, 자산·지표항목·측정시각·값의 단순 시계열 append 데이터)
- `InfraMetricThreshold.java` — 지표 항목(전역) 단위 임계치 엔티티(metricType UNIQUE, upsert)
- `InfraMetricAlert.java` — 임계치 초과 알림 엔티티(asset_id/metric_type 비정규화, acknowledged 플래그)
- `UptimeTarget.java` — 자산별 가동률 목표(SLA) 엔티티(asset_id UNIQUE, upsert)
- `CapacityPlan.java` — 팀/서비스별 용량 계획 엔티티(활용률은 저장하지 않는 계산값)
- `MetricType.java` — 지표 항목 enum(UPTIME, CPU, MEMORY, RESPONSE_TIME)
- `ThresholdType.java` — 임계치 초과 방향 enum(UPPER, LOWER)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
