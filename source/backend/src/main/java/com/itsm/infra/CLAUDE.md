# CLAUDE.md

IT 인프라 모니터링 & 용량관리(infra, Infra Operations & Capacity Management, IOM) 도메인. 인프라 자산(HW/네트워크) 가동률·성능 지표(UPTIME/CPU/MEMORY/RESPONSE_TIME) 수동 등록·시계열 조회, 지표 항목별(전역) 임계치 설정·초과 알림, 자산별 SLA 대비 가동률 비교, 팀/서비스 용량 계획, 인프라 지표 리포팅을 담당한다. 단일 역할(INFRA_OPERATOR) 도메인. 실시간 수집/에이전트 연동 없이 수동 입력 기반. 대상 자산은 `asset` 테이블을 직접 FK 참조한다(티켓형이 아니라 `common.ticket` 미사용). 가동률(actualPercentage)·용량 활용률(utilizationRate)·리포팅 평균값은 저장 컬럼 없이 조회 시점 계산값이다. DDD 4계층 구조. 4/4(확장 도메인) 마지막 도메인.

## 하위 디렉토리
- `application/` — 유스케이스 서비스(InfraMonitoringService)와 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
