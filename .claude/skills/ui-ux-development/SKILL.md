---
name: ui-ux-development
description: 프레임워크 무관 UI/UX 구현 표준(디자인 토큰·레이아웃·그리드·공통 컴포넌트·화면 UX·인터랙션·접근성)과 shadcn 기반 컴포넌트 구성 방법. 화면 개발의 공통 디자인 시스템 기반이 필요할 때 사용한다.
---

# UI/UX 개발

화면 설계서(`docs/02_plan/screen/*`)를 실제 UI로 구현할 때의 **프레임워크 무관 공통 표준**을 정의한다. 프레임워크별 구현(폴더 구조·상태관리 등)은 React/Next skill을 따른다.

## MCP

- **shadcn MCP**를 사용하여 컴포넌트를 검색·추가한다. (`list_items_in_registries`, `get_add_command_for_items` 등)

## 원칙

- 화면 설계서에 정의된 화면·요소·색상 팔레트만 구현한다. 임의 색상·요소 추가 금지.
- 디자인 값은 **토큰**으로 관리하고, 컴포넌트는 토큰을 참조한다. (하드코딩 금지)

## docs

상세 표준은 [references/design-system.md](references/design-system.md)를 따른다.
