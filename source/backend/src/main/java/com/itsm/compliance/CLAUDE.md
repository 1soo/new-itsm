# CLAUDE.md

컴플라이언스 관리(compliance, Compliance Management) 도메인. 컴플라이언스 요구사항 등록·책임자 지정·시정조치(DETECTED→IN_PROGRESS→RESOLVED 순차 전이) 추적·변경 요청 연계·준수 현황을 담당한다. 단일 역할(COMPLIANCE_OFFICER) 도메인. 준수 상태(complianceStatus)는 저장 컬럼이 아니라 시정조치 미해결(DETECTED/IN_PROGRESS) 존재 여부로 조회 시점에 계산한다. 변경 연계는 common.ticket(source_type='COMPLIANCE_REQUIREMENT', target_type='CHANGE') 재사용, 감사 로그는 auth.audit_log(event_type='COMPLIANCE_REQ_CREATE'/'COMPLIANCE_REQ_UPDATE'/'COMPLIANCE_ACTION_STATUS_CHANGE') 재사용(원본 작업과 같은 트랜잭션). DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스·상태머신과 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
