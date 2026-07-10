# 모션 & 인터랙션 (Motion)

> **모션은 장식이 아니라 "명확화 계층(clarifying layer)"이다.** 모든 모션은 duration·easing·property 3요소로 정의되며, 팀은 raw 값 대신 의도(intent) 기반의 모션 토큰(예: `motion.popup.enter`)을 선택한다.

본 문서는 [atlassian.design/foundations/motion](https://atlassian.design/foundations/motion)을 분석하여 재구성했다.

---

## 🌀 모션 원칙

| 원칙 | 설명 |
|---|---|
| **Human (인간적)** | 유기적이고 인간적인 움직임 — 섬세하고 리드미컬하며 직관적 |
| **Clarity (명확성)** | 장식이 아닌 명확화 계층. 목적 없는 모션은 지양 |
| **Accessible (접근 가능)** | 편안함을 해치지 않으면서 이해를 돕는다 |
| **Performant (성능)** | 속도감을 강화하도록 절제된 모션 |

> 현재 일부 컴포넌트에만 모션 토큰이 제한적으로 적용되어 있으며 확대 적용 중이다.

---

## ⏱ Duration 토큰

| 토큰 | 값 | 용도 |
|---|---|---|
| `motion.duration.instant` | 0ms | 즉각적 피드백 (포커스 상태) |
| `motion.duration.xxshort` | 50ms | 고빈도 인터랙션 (hover) |
| `motion.duration.xshort` | 100ms | 빠른 exit, 미묘한 pressed 상태 |
| `motion.duration.short` | 150ms | 인터랙션 강조, 드롭다운 진입 |
| `motion.duration.medium` | 200ms | 중간 전환 (모달/플래그 퇴장) |
| `motion.duration.long` | 250ms | 진입 전환 (모달 진입) |
| `motion.duration.xlong` | 400ms | 큰 전환 |
| `motion.duration.xxlong` | 600ms | 전체 화면 오버레이 |

대략적 구분: **Interactions(hover/press) ≈ 50~150ms**, **Transitions(진입/퇴장/이동) ≈ 150~400ms**.

---

## 📈 이징 커브

| 토큰 | cubic-bezier | 용도 |
|---|---|---|
| Ease-out bold | `cubic-bezier(0, 0.4, 0, 1)` | 패널/플래그 진입 — 빠르게 시작해 안정적으로 착지, 주목도 高 |
| Ease-in-out bold | `cubic-bezier(0.4, 0, 0, 1)` | 모달 스케일링, 위치 이동(transform) |
| Ease-in practical | `cubic-bezier(0.6, 0, 0.8, 0.6)` | 퇴장 전환 — 빠르게 가속하며 사라짐 |
| Ease-out practical | `cubic-bezier(0.4, 1, 0.6, 1)` | 팝업/hover 페이드, 일상적 인터랙션 — 자연스러운 감속 |

---

## 🧩 인터랙션 패턴

### 컴포넌트별 시맨틱 토큰

- 모달: `motion.modal.enter` / `.exit`
- 드롭다운/팝업: `motion.popup.enter.{top|bottom|left|right}` (+ 대응 exit)
- 플래그(토스트): `motion.flag.enter` / `.exit`
- 스포트라이트: `motion.spotlight.enter` / `.exit`

### Keyframe 토큰

| 토큰 | 범위 | 용도 |
|---|---|---|
| `motion.keyframe.fade.in` | 0→100% | 요소 등장 |
| `motion.keyframe.fade.out` | 100→0% | 요소 소멸 |
| `motion.keyframe.scale.in.small` | 95→100% | 모달/스포트라이트 진입 |
| `motion.keyframe.scale.out.small` | 100→95% | 모달/스포트라이트 퇴장 |

### 핵심 원칙

- **진입과 퇴장은 대칭이 아니다** — 진입은 새 콘텐츠에 방향성/맥락을 제공하고, 퇴장은 닫힘을 확인시키는 역할이라 서로 다르게 설계한다.
- **hover 상태**는 `motion.duration.xxshort`(50ms) + ease-out practical 조합을 사용한다.
- **모션의 방향은 컴포넌트의 출처와 공간적으로 연결**되어야 한다 (예: 드롭다운은 트리거 위치에서 열리고 닫힘).
- 한 전환에는 **1~2개 속성만 애니메이션**한다 (fade+slide 정도는 무방, 동시다발적 다중 속성 변화는 지양).
- **GPU 합성 속성(`transform`, `opacity`)만 애니메이션**하고, 레이아웃을 유발하는 `width`/`height`는 피한다.
- 포커스/스크린리더 알림은 애니메이션 **시작 시점**에 발생시키며, 완료를 기다리지 않는다.

---

## ♿ 모션 감소 (Reduced Motion)

> "**reduced motion 설정을 존중한다.** 활성화 시 모션은 꺼지고 즉시(instant) 전환된다."

깜빡임이나 빠른 진동 효과는 사용하지 않으며, 모션을 끈 상태에서도 전체 기능이 정상 동작하는지 검증해야 한다.

---

## 📚 참고 자료

- [Foundations · Motion](https://atlassian.design/foundations/motion)
- [Foundations · Motion · Applying motion](https://atlassian.design/foundations/motion/applying-motion)
- [Components · Motion](https://atlassian.design/components/motion)
