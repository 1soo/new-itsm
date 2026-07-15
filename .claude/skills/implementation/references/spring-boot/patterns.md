# Spring / Spring Boot 디자인 패턴

Backend 구현 시 따르는 아키텍처·설계 패턴 상세. 컨벤션 요약은 [conventions.md](conventions.md) 참조.

## 1. MVC 아키텍처 (Front Controller)

Spring MVC는 **Front Controller 패턴**으로 동작한다. 모든 HTTP 요청은 단일 진입점인 `DispatcherServlet`이 받아, 요청 매핑·뷰 리졸빙·예외 처리 등 실제 작업을 설정 가능한 컴포넌트에 위임한다.

| 요소 | 역할 |
|------|------|
| `DispatcherServlet` | Front Controller. 모든 요청 수신·위임 (Spring Boot가 자동 등록) |
| `HandlerMapping` | 요청 URL → 처리할 핸들러(Controller 메서드) 결정 |
| Controller (`@RestController`) | presentation 계층. 요청 파싱·검증, application 계층 호출, 응답 반환 |
| `HttpMessageConverter` | 요청/응답 body ↔ 객체 직렬화(JSON) |

- 이 프로젝트의 REST Controller는 presentation 계층에 위치하며, 얇게(thin) 유지한다. 비즈니스 로직은 application/domain 계층에 두고 Controller는 위임만 한다.
- 요청/응답은 DTO로 주고받고 도메인 Entity를 직접 노출하지 않는다([8. DTO 패턴](#8-dto-패턴) 참조).

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService; // 생성자 주입

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest req) {
        OrderResponse res = orderService.create(req.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
```

참고: <https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html>

## 2. SOLID 5원칙

| 원칙 | 정의 | Spring 적용 |
|------|------|-------------|
| SRP (단일 책임) | 클래스는 하나의 변경 이유만 갖는다 | Controller=요청 처리, Service=유스케이스, Repository=영속성으로 책임 분리 |
| OCP (개방-폐쇄) | 확장에 열리고 수정에 닫힌다 | interface + 구현체 추가로 기능 확장, 기존 코드 미수정 |
| LSP (리스코프 치환) | 하위 타입은 상위 타입을 대체 가능 | interface 계약(예외·반환 규약) 위반 없는 구현 |
| ISP (인터페이스 분리) | 사용하지 않는 메서드에 의존하지 않는다 | 역할별 좁은 interface (`OrderReader`/`OrderWriter` 분리) |
| DIP (의존성 역전) | 구체가 아닌 추상에 의존 | domain이 Repository **interface**를 정의, infrastructure가 구현 |

- **생성자 주입을 기본으로 한다**(필드 주입 금지). 불변(`final`) 보장, 테스트 시 mock 주입 용이, 순환 의존 조기 발견.
- Service는 interface에 의존하고, 구현체는 DI로 주입받는다.

```java
public interface PaymentGateway {           // DIP: 추상에 의존
    PaymentResult pay(PaymentCommand cmd);
}

@Service
public class OrderService {
    private final PaymentGateway paymentGateway; // 구현체 아닌 interface

    public OrderService(PaymentGateway paymentGateway) { // 생성자 주입
        this.paymentGateway = paymentGateway;
    }
}
```

참고: <https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html>

## 3. Spring이 활용하는 GoF 디자인 패턴

Spring은 아래 패턴을 프레임워크 차원에서 제공한다. 각 패턴을 어디에 어떻게 적용하는지 정리한다.

| 패턴 | Spring 구현 | 적용 가이드 |
|------|-------------|-------------|
| **Singleton** | 기본 Bean scope(`singleton`) | 상태 없는(stateless) Service/Repository는 기본 scope 사용. 요청 상태는 파라미터·지역변수로 |
| **Factory** | `@Bean` 메서드, `BeanFactory`, `FactoryBean` | 생성 로직이 복잡하거나 외부 라이브러리 객체는 `@Bean`으로 등록 |
| **Proxy** | AOP 프록시(`@Transactional`, `@Async`) | 횡단 관심사는 어노테이션으로. self-invocation은 프록시 우회됨에 주의([4](#4-proxy--aop-transactional)) |
| **Template Method** | `JdbcTemplate`, `RestClient`, `TransactionTemplate` | 반복되는 리소스 관리(연결·해제·예외 변환)를 프레임워크에 위임 |
| **Strategy** | interface 구현체 교체, DI | 알고리즘 계열(할인 정책·인증 방식)을 interface로 추상화, 런타임 주입으로 전환 |
| **Builder** | `ResponseEntity`, `UriComponentsBuilder` | 다단계 조립이 필요한 응답/URI 생성에 사용 |
| **Repository** | Spring Data (`JpaRepository`) | 영속성 접근을 컬렉션처럼 추상화([5](#5-repository--spring-data)) |
| **Adapter** | `HandlerAdapter`, `HandlerInterceptor` | 이질적 인터페이스를 표준 계약에 맞출 때 |
| **Observer** | `ApplicationEvent`/`@EventListener` | 도메인 간 느슨한 결합 이벤트 전파([6](#6-observer--application-event)) |

### Strategy 예시

```java
public interface DiscountPolicy {
    Money discount(Order order);
}

@Component("rateDiscount")
public class RateDiscountPolicy implements DiscountPolicy { /* ... */ }

@Component("fixedDiscount")
public class FixedDiscountPolicy implements DiscountPolicy { /* ... */ }

// @Qualifier로 런타임 전략 선택
public OrderService(@Qualifier("rateDiscount") DiscountPolicy policy) { ... }
```

참고: <https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html>

## 4. Proxy · AOP (@Transactional)

`@Transactional`은 **AOP 프록시**로 구현된다. 선언적 트랜잭션은 프록시가 대상 객체를 감싸(wrap) 메서드 호출을 가로채고, `TransactionInterceptor`가 적절한 `TransactionManager`로 트랜잭션을 시작·커밋·롤백한다. 비즈니스 코드는 트랜잭션 처리 코드를 갖지 않는다.

- 트랜잭션 경계는 **application 계층(Service)**에 둔다.
- **self-invocation 주의**: 같은 클래스 내 메서드가 다른 `@Transactional` 메서드를 직접 호출하면 프록시를 거치지 않아 적용되지 않는다. 별도 Bean으로 분리한다.
- `public` 메서드에만 적용된다(프록시 기반 한계).

```java
@Service
public class OrderService {
    @Transactional
    public OrderResponse create(OrderCommand cmd) {
        Order order = Order.create(cmd);   // 도메인 로직
        orderRepository.save(order);        // 예외 발생 시 자동 롤백
        return OrderResponse.from(order);
    }
}
```

참고: <https://docs.spring.io/spring-framework/reference/core/aop/introduction-proxies.html>, <https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html>

## 5. Repository · Spring Data

Spring Data는 **Repository 패턴**을 인터페이스 선언만으로 제공한다. 영속성 접근을 도메인 객체 컬렉션처럼 다루게 하여, 구현 코드 없이 CRUD·쿼리 메서드를 자동 생성한다. DIP를 프레임워크가 강제하는 셈이다.

- domain 계층은 Repository **interface**를 정의(또는 Spring Data interface 상속), infrastructure가 구현/자동생성을 담당한다.
- DB 접근 방식(JPA/MyBatis)은 설계자 결정(`docs/02_plan/database`)을 따른다. Template Method(`JdbcTemplate`)를 쓸 경우도 DAO를 interface 뒤에 둔다.

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);   // 쿼리 메서드 자동 구현
}
```

참고: <https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html>, <https://docs.spring.io/spring-data/jpa/reference/repositories.html>

## 6. Observer · Application Event

`ApplicationContext`는 `ApplicationEvent`와 `ApplicationListener`로 **Observer 패턴**을 지원한다. 도메인 간 직접 호출 대신 이벤트를 발행해 느슨하게 결합한다. 발행자는 구독자를 알 필요가 없다.

- 도메인 경계를 넘는 부수효과(알림 발송·통계 갱신 등)는 이벤트로 분리한다.
- `@TransactionalEventListener`로 커밋 이후 처리를 보장할 수 있다.

```java
public record OrderCreatedEvent(Long orderId) {}

@Service
public class OrderService {
    private final ApplicationEventPublisher publisher;
    // ...create() 내부에서
    publisher.publishEvent(new OrderCreatedEvent(order.getId()));
}

@Component
public class OrderNotifier {
    @EventListener
    public void on(OrderCreatedEvent event) { /* 알림 발송 */ }
}
```

참고: <https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html>

## 7. DDD 전술 패턴

도메인 모델을 풍부하게(rich) 유지한다. 아래 전술 패턴으로 도메인 로직이 흩어지지 않게 한다.

| 패턴 | 정의 | 지침 |
|------|------|------|
| **Entity** | 식별자(ID)로 구별되는 객체 | 생명주기·불변식을 스스로 관리. setter 남발 금지 |
| **Value Object** | 값으로 동등성 판단, 불변 | `Money`·`Address` 등. 생성 시 유효성 검증, 변경은 새 객체 반환 |
| **Aggregate** | 일관성 경계를 갖는 Entity 묶음 | Aggregate Root를 통해서만 내부 접근, 불변식을 Root가 보장 |
| **Domain Event** | 도메인에서 발생한 사건 | Aggregate 상태 변화를 이벤트로 표현([6](#6-observer--application-event)) |
| **Repository** | Aggregate 단위 영속성 추상화 | Aggregate Root당 하나. 내부 Entity 전용 Repository 금지 |
| **Factory** | 복잡한 생성 로직 캡슐화 | 불변식을 만족하는 Aggregate 생성을 정적 팩토리 메서드로 |
| **Specification** | 도메인 규칙을 객체로 표현 | 복잡한 조회·검증 조건을 재사용 가능한 명세로 |

### Anemic Domain Model(빈약한 도메인 모델) 지양

getter/setter만 있고 로직이 없는 Entity는 안티패턴이다. 도메인 로직이 Service로 새어나가면 응집도가 떨어지고 중복·불변식 위반이 생긴다. **로직은 그 데이터를 가진 Entity/도메인 서비스에 둔다.**

```java
// 지양: 로직이 Service에, Entity는 데이터 주머니
order.setStatus(CANCELED); // Service가 상태 규칙을 직접 판단

// 지향: Entity가 불변식을 스스로 보장
public class Order {
    private OrderStatus status;
    public void cancel() {                       // 도메인 로직 = Entity 책임
        if (status == SHIPPED)
            throw new IllegalStateException("배송 후 취소 불가");
        this.status = OrderStatus.CANCELED;
    }
}
```

- Entity로 표현하기 애매한 로직(여러 Aggregate 협력)은 **도메인 서비스**에 둔다(application Service와 구분).

참고: <https://docs.spring.io/spring-data/jpa/reference/repositories/core-domain-events.html>

## 8. DTO 패턴

Entity를 API 요청/응답에 직접 노출하지 않는다. presentation 계층 DTO와 도메인 Entity를 분리해 순환 참조·과다 노출·API-DB 결합을 방지한다.

- 요청 DTO는 `@Valid`로 검증하고, 도메인 Command/Entity로 변환한다.
- 응답 DTO는 정적 팩토리(`from`)로 Entity에서 생성한다.

```java
public record OrderResponse(Long id, String status, long totalAmount) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(),
                order.getStatus().name(), order.totalAmount().value());
    }
}
```
