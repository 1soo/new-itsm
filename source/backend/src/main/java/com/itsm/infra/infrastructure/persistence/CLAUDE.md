# CLAUDE.md

infra 도메인 리포지토리 인터페이스의 Spring Data JPA 구현체.

## 파일
- `InfraMetricJpaRepository.java` — InfraMetricRepository 구현(동적 필터 시계열 검색, JPQL avg 구간 평균)
- `InfraMetricThresholdJpaRepository.java` — InfraMetricThresholdRepository 구현
- `InfraMetricAlertJpaRepository.java` — InfraMetricAlertRepository 구현(동적 필터 검색)
- `UptimeTargetJpaRepository.java` — UptimeTargetRepository 구현
- `CapacityPlanJpaRepository.java` — CapacityPlanRepository 구현
