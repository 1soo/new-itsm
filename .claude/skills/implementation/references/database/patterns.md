# Database 데이터 접근 패턴·설계 원칙

[conventions.md](conventions.md)의 스키마·암호화 규칙을 전제로, 데이터 접근 디자인 패턴과 DB 설계 원칙을 정리한다. 구체 결정은 **설계자의 `docs/02_plan/database` 산출물을 우선**하며, 산출물에 없는 부분만 본 문서 기본값을 따른다.

> **출처 구분**: PostgreSQL 공식 문서는 **표준 제품 문서**, Martin Fowler의 *Patterns of Enterprise Application Architecture*(PoEAA)는 **업계 표준 저작물**이다. 후자는 W3C·PostgreSQL 같은 표준기구 공식 문서가 아니라 업계 관례로 자리 잡은 참고 서적임을 구분해 인용한다.

## 1. 데이터 접근 패턴

| 패턴 | 개념 | 이 프로젝트 |
|------|------|-------------|
| **Repository** | 도메인 객체 컬렉션처럼 보이는 접근 계층. 질의 로직을 캡슐화해 도메인·매핑 계층을 분리 | **권장.** Spring Data JPA `Repository`/`JpaRepository` 인터페이스로 구현 |
| **Data Mapper** | 도메인 객체와 DB를 서로 모르게 유지하며 매핑 계층이 중개 | **권장 지향.** JPA(Hibernate)가 Data Mapper 역할 수행 |
| **Active Record** | 도메인 객체가 자신의 저장/조회 로직을 직접 보유 | 지양 (도메인과 영속성 결합도 높음) |

### 핵심 규칙

- 데이터 접근은 **Repository 인터페이스로 추상화**하고, 서비스 계층은 구현이 아닌 인터페이스에 의존한다.
- Spring Data JPA는 `CrudRepository`/`ListCrudRepository`/`JpaRepository`를 확장한 인터페이스만 선언하면 구현을 자동 제공하므로, boilerplate 없이 finder 메서드(`findByLastname` 등)를 선언한다.
- Active Record(도메인 객체에 저장 로직 포함)는 지양하고 Data Mapper(JPA) 지향으로 도메인과 영속성을 분리한다.
- **단, DB 접근 방식(JPA/MyBatis 등)은 설계자 결정(`docs/02_plan/database`)을 우선한다.** MyBatis 지정 시에도 Mapper 인터페이스로 접근 계층을 분리한다는 원칙은 동일하게 유지한다.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastname(String lastname);
    User findByEmailAddress(String emailAddress);
}
```

- 출처: [Spring Data JPA — JPA Repositories](https://docs.spring.io/spring-data/jpa/reference/jpa.html) (표준 제품 문서) · PoEAA, *Repository* / *Data Mapper* / *Active Record* 패턴 (업계 표준 저작물)

## 2. 정규화 원칙

[conventions.md](conventions.md)의 "2. 스키마 규칙"을 정규화 관점에서 보강한다.

| 단계 | 조건 |
|------|------|
| **1NF** | 모든 컬럼이 원자값(반복 그룹·다중값 컬럼 없음) |
| **2NF** | 1NF + 부분 함수 종속 제거(복합 PK의 일부에만 종속되는 컬럼 없음) |
| **3NF** | 2NF + 이행 함수 종속 제거(비키 컬럼이 다른 비키 컬럼에 종속되지 않음) |

### 핵심 규칙

- 기본은 **3NF까지 정규화**해 데이터 중복·갱신 이상을 제거한다.
- **의도적 비정규화**는 조회 성능이 실측·예측상 병목일 때만 한정 적용한다. 판단 기준: (1) 조인 비용이 큰 고빈도 조회, (2) 갱신 빈도가 낮아 중복 데이터 정합성 유지 부담이 작을 때. 비정규화한 컬럼·의도는 테이블 정의서에 명시한다.
- 정규화 이론은 관계형 모델(E.F. Codd)에 근거하며, 특정 제품에 종속되지 않는 **업계 표준 이론**이다.
- 출처: 관계형 데이터베이스 정규화 이론(E.F. Codd, 업계 표준 이론) · [PostgreSQL: Data Definition](https://www.postgresql.org/docs/current/ddl.html) (표준 제품 문서)

## 3. 인덱싱 전략 패턴

### 핵심 규칙

- 기본 인덱스는 **B-tree**를 사용한다. 등호·범위·정렬(`ORDER BY`)에 두루 쓰이는 범용 타입이다.
- **복합 인덱스 컬럼 순서**: 등호 조건으로 자주 쓰는 컬럼(leading/leftmost)을 앞에 둔다. B-tree는 선행 컬럼에 대한 등호 제약 + 첫 비등호 컬럼의 범위 제약까지만 인덱스 스캔 범위를 제한하므로, 선행 컬럼 선택이 효율을 좌우한다.
- **카디널리티 고려**: 선택도가 높은(distinct 값이 많은) 컬럼을 선행에 두면 스캔 대상이 크게 줄어든다.
- **과용 금지**: 대부분 단일 컬럼 인덱스로 충분하며, 3개 초과 컬럼의 복합 인덱스는 매우 정형화된 사용 패턴이 아니면 도움이 되지 않는다. 인덱스는 공간·쓰기 비용을 수반하므로 실제 질의 패턴에 근거해 추가한다.
- 출처: [PostgreSQL: B-Tree Index Types](https://www.postgresql.org/docs/current/indexes-types.html) · [Multicolumn Indexes](https://www.postgresql.org/docs/current/indexes-multicolumn.html) · [Indexes and ORDER BY](https://www.postgresql.org/docs/current/indexes-ordering.html) (표준 제품 문서)

## 4. 트랜잭션 · Unit of Work 패턴

### 핵심 규칙

- **격리 수준 기본값은 Read Committed**(PostgreSQL 기본)를 따른다. `SELECT`는 쿼리 시작 시점의 커밋된 스냅샷만 보며, 미커밋 데이터나 실행 중 커밋된 변경은 보지 않는다. 팬텀·반복 불가능 읽기가 문제되는 경우에 한해 Repeatable Read/Serializable로 격상한다.
- **트랜잭션 경계는 애플리케이션 계층에 둔다.** Spring `@Transactional`을 서비스 계층 메서드에 선언해 하나의 논리 작업 = 하나의 트랜잭션이 되게 한다. 이는 여러 변경을 모아 커밋/롤백하는 **Unit of Work** 패턴의 구현에 해당한다(JPA `EntityManager`의 영속성 컨텍스트가 Unit of Work 역할).
- 트랜잭션 경계·격리 수준의 구체 설정은 Backend 구현과 맞물리므로 [spring-boot 컨벤션](../spring-boot/conventions.md)의 서비스 계층 `@Transactional` 규칙과 접점을 맞춘다.
- 출처: [PostgreSQL: Transaction Isolation](https://www.postgresql.org/docs/current/transaction-iso.html) (표준 제품 문서) · [Spring Framework — Declarative Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative.html) (표준 제품 문서) · PoEAA, *Unit of Work* 패턴 (업계 표준 저작물)
