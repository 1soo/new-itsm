---
name: system-design
description: 화면 설계서·API 명세서·테이블 정의서·보안 설계·역할 정의·인프라 아키텍처를 직관적이고 간결한 markdown으로 작성하는 방법과 표준 양식. 분석 산출물(docs/01_analyze) 기반 시스템 설계가 필요할 때 사용한다.
---

# 시스템 설계 (설계 단계)

`designer`가 사용하는 설계 단계 skill. 작성하는 산출물 종류에 따라 `references`의 하위 폴더를 참조한다.

## 참조 안내

| 산출물 | 참조 시점 | 참고 문서 | 저장 위치 |
|--------|-----------|-----------|-----------|
| 화면 설계서 | 화면 레이아웃·구성 요소·색상 팔레트를 정의할 때 | [references/ui-ux/conventions.md](references/ui-ux/conventions.md), [references/ui-ux/template.md](references/ui-ux/template.md) | `docs/02_plan/screen/{domain}.md` (공통: `common.md`, 404: `error_404.md`, 관리자: `{admin}.md`) |
| API 명세서 | endpoint·method·request/response·인증 포함 여부를 정의할 때 | [references/application/conventions.md](references/application/conventions.md), [references/application/template.md](references/application/template.md) | `docs/02_plan/api_spec/{domain}.md` |
| 테이블 정의서 | 테이블·컬럼·정규화·제약조건·RBAC 매핑을 정의할 때 | [references/database/conventions.md](references/database/conventions.md), [references/database/template.md](references/database/template.md) | `docs/02_plan/database/{domain}.md` |
| 역할 정의 | 역할별 접근 가능 화면·API, 페르소나를 정의할 때 | [references/role/conventions.md](references/role/conventions.md), [references/role/template.md](references/role/template.md) | `docs/02_plan/security/authorization/{역할명}.md` |
| 보안 설계 | JWT 인증 구조(토큰 저장·만료·JTI 세션·XSS/CSRF 방지)를 정의할 때 | [references/security/conventions.md](references/security/conventions.md), [references/security/template.md](references/security/template.md) | `docs/02_plan/security/authentication.md` |
| 인프라 아키텍처 | AWS/Azure 등 CSP 환경의 로드밸런서·네트워크·DB 구성을 정의할 때(**local 환경이면 수행하지 않음**) | [references/infra/conventions.md](references/infra/conventions.md), [references/infra/template.md](references/infra/template.md) | `docs/02_plan/infra/{aws or azure}.md` |

## 실행 순서

1. 위 산출물을 의존성이 낮은 순으로 우선순위를 정해 순차 수행한다.
2. 어떤 산출물이 다른 산출물의 내용을 필요로 하면 그 산출물의 우선순위를 낮춘다(뒤로 미룬다).
   > 참고(의존성 경향, 강제 아님): 화면 → API → Database → 역할 → Security → Infra 순으로 뒤로 갈수록 앞선 산출물에 의존하는 경향이 있다. 실제 순서는 위 규칙에 따라 스스로 판단한다.
3. 각 산출물을 위 표의 저장 위치에 저장한다.

## 공통 원칙

- `docs/01_analyze`에 명시된 내용만 설계한다. 모호하거나 결정이 필요한 부분은 `analyzer`에게 질문한다.
- 도메인 용어는 `docs/00_context/glossary.md`와 일관되게 사용하고, 새로 구체화·추가한 용어는 그 자리에서 반영한다.
- 최초 작성 이후 산출물을 변경할 때는 본문 중간에 날짜를 넣지 않고, 문서 상단 `변경 이력` 섹션에 날짜와 간결한 요약을 추가한다.
