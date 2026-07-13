---
name: ui-ux-design
description: 화면 설계서를 직관적이고 간결한 markdown으로 작성하는 방법과 표준 양식. UI/UX 설계, 화면 레이아웃·구성 요소·색상 팔레트 정의가 필요할 때 사용한다.
---

# UI/UX 설계 (화면 설계서)

요구사항·기능 명세서 바탕으로 화면 설계서를 **직관적이고 간결한** markdown으로 작성.

## 작성 원칙

- 요구사항/기능 명세서에 명시된 화면과 요소만 설계. 추측·확장 금지.
- **색상**: 요구사항에 적절한 base 색상 먼저 선택, 화면 요소 색상은 **그 base 색상에서 크게 벗어나지 않는 범위로 variation**하여 구성. (일관된 톤 유지)
- 각 화면은 연관 요구사항(REQ-ID)/기능(FEAT-ID)과 연결.

## 산출물 저장 위치

| 대상 | 위치 |
|------|------|
| 도메인 화면 | `docs/02_plan/screen/{domain}.md` |
| 공통 컴포넌트 (헤더·푸터·사이드바) | `docs/02_plan/screen/common.md` |
| 404 오류 화면 | `docs/02_plan/screen/error_404.md` |
| 관리자 화면 | `docs/02_plan/screen/{admin}.md` |

## 양식

표준 양식은 [references/template.md](references/template.md) 사용.
