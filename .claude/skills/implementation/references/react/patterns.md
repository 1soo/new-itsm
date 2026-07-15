# React 컴포넌트 설계 패턴 (CSR)

[conventions.md](conventions.md)를 보완하는 컴포넌트·훅 설계 패턴 상세. 공식 문서(react.dev) 원칙을 기준으로 한다.

## 1. 컴포넌트 설계 패턴

### Composition over Inheritance

React는 상속 대신 **합성(composition)**으로 컴포넌트를 재사용한다. 컴포넌트를 상속·확장하지 말고, `children`이나 JSX prop으로 내용을 주입한다.

| 방식             | 용도                                         |
| ---------------- | -------------------------------------------- |
| `children` prop  | 범용 컨테이너(Card, Panel, Layout 등)에 내용 삽입 |
| 명명된 JSX prop  | 여러 슬롯이 필요할 때 (`<Layout left={...} right={...} />`) |

```jsx
function Card({ title, children }) {
  return (
    <div className="card">
      {title && <h3>{title}</h3>}
      {children}
    </div>
  );
}
// <Card title="프로필"><Avatar /></Card>
```

### Container / Presentational 분리

- **Presentational**: props만 받아 UI를 렌더링. 상태·데이터 fetching 없음.
- **Container**: 데이터 조회·상태 관리를 담당하고 Presentational에 props로 전달.
- 오늘날엔 이 분리를 대부분 **Custom Hook**으로 대체한다(로직은 훅으로, 화면은 컴포넌트로). 아래 Custom Hook 패턴 우선.

### Compound Component

관련된 하위 컴포넌트들이 부모의 상태를 **Context로 공유**하며 함께 동작하는 패턴. 부모가 `<Tabs>`, 자식이 `<Tabs.List>`/`<Tabs.Panel>` 형태로 결합된다. 유연한 조합이 필요한 공통 UI(Tabs, Accordion, Select 등)에 쓴다. 상태 공유는 [Context](#3-상태-관리-패턴)로 구현한다.

### Custom Hook (로직 재사용의 공식 권장 방식)

상태를 가진 로직을 재사용할 땐 **Custom Hook**을 사용한다. `use`로 시작하는 함수로 `useState`/`useEffect` 등을 감싸 추출한다.

```jsx
function useOnlineStatus() {
  const [isOnline, setIsOnline] = useState(true);
  useEffect(() => {
    const on = () => setIsOnline(true);
    const off = () => setIsOnline(false);
    window.addEventListener('online', on);
    window.addEventListener('offline', off);
    return () => {
      window.removeEventListener('online', on);
      window.removeEventListener('offline', off);
    };
  }, []);
  return isOnline;
}
```

- Custom Hook은 **상태 자체가 아니라 상태 로직(stateful logic)**을 공유한다. 같은 훅을 쓰는 두 컴포넌트는 각자 독립된 상태를 갖는다.
- 훅 이름은 반환값을 설명하도록(`useData`, `useChatRoom`). Effect가 있으면 훅으로 감싸 컴포넌트에서 직접 노출된 Effect를 줄인다.

### HOC / Render Props (레거시)

HOC(Higher-Order Component)와 Render Props는 과거의 로직 재사용 방식이다. 공식 문서 기조상 **신규 코드는 Custom Hook을 우선**한다. 레거시 코드 유지보수 시에만 기존 패턴을 따른다.

> 출처: https://react.dev/learn/passing-props-to-a-component · https://react.dev/learn/reusing-logic-with-custom-hooks

## 2. SOLID / OOP를 컴포넌트·훅에 적용

| 원칙 | 컴포넌트·훅 적용                                                          |
| ---- | ------------------------------------------------------------------------ |
| SRP  | 컴포넌트·훅은 하나의 책임만. 렌더링과 데이터 로직이 섞이면 훅으로 분리한다. |
| OCP  | 동작 변경은 컴포넌트 수정이 아니라 **props로 확장**한다(콜백·렌더 slot 주입). |
| DIP  | 컴포넌트가 구체 구현(fetch, 특정 store)에 직접 의존하지 않게 **Context/Custom Hook 뒤로 숨긴다**. 컴포넌트는 추상(훅 인터페이스)에 의존한다. |

- LSP/ISP는 컴포넌트 단위에선 props 인터페이스를 작게(필요한 값만) 유지하는 것으로 반영한다. 거대한 props 묶음 대신 목적별로 나눈다.

> 출처: https://react.dev/learn/reusing-logic-with-custom-hooks

## 3. 상태 관리 패턴

### Lifting State Up (공식 패턴)

두 컴포넌트가 같은 상태를 공유해야 하면, 상태를 **가장 가까운 공통 부모**로 올리고 props로 내려보낸다. 자식은 상태를 소유하지 않고 값·콜백만 받는다.

### 로컬 vs 전역 상태 판단 기준

| 구분      | 두는 위치                          | 예                                    |
| --------- | ---------------------------------- | ------------------------------------- |
| 로컬 상태 | 컴포넌트/Custom Hook               | 입력값, 토글, 모달 open 여부          |
| 공통 상태 | 공통 부모(Lifting State Up)        | 형제 컴포넌트 간 동기화되는 선택 index |
| 전역 상태 | Redux(Redux Toolkit) 또는 Context  | 인증 상태·사용자/역할 등 앱 전역 값   |

- 먼저 로컬로 두고, 공유가 필요해질 때만 위로 올린다. 앱 전역 공유 값만 전역 상태로 승격한다([conventions.md 1항](conventions.md)).

### Context / Reducer로 prop drilling 회피

- 중간 컴포넌트를 여러 단계 거쳐 같은 값을 내려보내야 하면(prop drilling) **Context**로 "teleport"한다.
- 상태 로직이 복잡하면 **reducer + Context** 조합으로 state와 dispatch를 트리 하위에 공급한다.
- 단, 앱 전역 도메인 상태는 프로젝트 표준인 **Redux Toolkit slice**를 쓴다. Context는 주로 테마·로케일·Compound Component 내부 공유 등 좁은 범위에 사용하고, Redux와 역할을 겹치지 않게 나눈다.

```js
// Redux Toolkit slice (전역 상태 예)
const authSlice = createSlice({
  name: 'auth',
  initialState: { user: null, roles: [] },
  reducers: {
    setUser: (state, action) => { state.user = action.payload; },
    logout: (state) => { state.user = null; state.roles = []; },
  },
});
```

> 출처: https://react.dev/learn/sharing-state-between-components · https://react.dev/learn/passing-data-deeply-with-context · https://react.dev/learn/scaling-up-with-reducer-and-context

## 4. 폴더 / 아키텍처 패턴

[conventions.md 4항](conventions.md)의 feature 기반 구조를 **계층(layer)** 관점으로 본다.

| 계층        | feature 내 위치       | 책임                                   |
| ----------- | --------------------- | -------------------------------------- |
| 표현(UI)    | `components/`         | 렌더링. props 기반 Presentational 컴포넌트 |
| 로직        | `hooks/`, `slice/`    | Custom Hook·Redux slice. 상태·비즈니스 로직 |
| 데이터 접근 | `api/`                | 공통 `apiClient` 호출을 감싼 함수      |

- 의존 방향은 **표현 → 로직 → 데이터 접근** 한 방향으로 흐른다(DIP). 컴포넌트는 `api/`를 직접 부르지 않고 훅을 통한다.
- 여러 feature가 공유하는 컴포넌트·훅은 공통 영역(UI 담당의 디자인 시스템·공통 컴포넌트)에 둔다([conventions.md](conventions.md) 참고).

> 출처: https://react.dev/learn/thinking-in-react
