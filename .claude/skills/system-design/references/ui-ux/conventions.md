# 화면 설계 컨벤션

요구사항·기능 명세서를 바탕으로 화면 설계서를 **직관적이고 간결한** markdown으로 작성한다.

## 작성 원칙

- 요구사항/기능 명세서에 명시된 화면과 요소만 설계한다.
- **색상**: 요구사항에 적절한 base 색상을 먼저 선택하고, 화면 요소 색상은 그 base 색상에서 크게 벗어나지 않는 범위로 variation하여 구성한다. (일관된 톤 유지)
- 각 화면은 연관 요구사항(REQ-ID)/기능(FEAT-ID)과 연결한다.

## 저장 위치

| 대상 | 위치 |
|------|------|
| 도메인 화면 | `docs/02_plan/screen/{domain}.md` |
| 공통 컴포넌트(헤더·푸터·사이드바) | `docs/02_plan/screen/common.md` |
| 404 오류 화면 | `docs/02_plan/screen/error_404.md` |
| 관리자 화면 | `docs/02_plan/screen/{admin}.md` |

## 양식

표준 양식은 [template.md](template.md)를 사용한다.
