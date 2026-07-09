# CLAUDE.md

asset 도메인의 엔티티·enum·리포지토리 계약.

## 파일
- `Asset.java` — IT 자산 엔티티(생애주기 상태·만료일 3종)
- `AssetAttribute.java` — 유형별 속성(EAV) 엔티티
- `AssetLifecycleHistory.java` — 생애주기 전이 이력 엔티티(append-only, BaseEntity 미상속)
- `ConfigurationItem.java` — 구성 항목(CI) 엔티티
- `CiRelation.java` — CI 간 자기참조 의존 관계(CMDB) 엔티티
- `AssetType.java` — 자산 유형 enum(HARDWARE, SOFTWARE, CLOUD)
- `AssetStatus.java` — 생애주기 상태 enum(PLANNING, PROCUREMENT, OPERATION, MAINTENANCE, RETIREMENT)
- `ExpiryStatus.java` — 만료 상태 enum(OK, EXPIRING, EXPIRED)
- `RelationType.java` — CI 관계 유형 enum(DEPENDS_ON, RUNS_ON, CONNECTS_TO)

티켓 연계(API-ITAM-007)는 별도 enum 없이 `common.ticket.TicketType`을 직접 사용한다(SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE로 검증).

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
