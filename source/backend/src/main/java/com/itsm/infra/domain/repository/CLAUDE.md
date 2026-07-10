# CLAUDE.md

infra 도메인의 리포지토리 인터페이스.

## 파일
- `InfraMetricRepository.java` — 지표 저장·시계열 검색·구간 평균(가동률·리포팅 계산용)
- `InfraMetricThresholdRepository.java` — 지표 항목(전역) 임계치 저장·조회
- `InfraMetricAlertRepository.java` — 임계치 초과 알림 저장·조회·검색(assetId·acknowledged 필터)
- `UptimeTargetRepository.java` — 자산별 가동률 목표(SLA) 저장·조회
- `CapacityPlanRepository.java` — 용량 계획 저장·전체 조회
