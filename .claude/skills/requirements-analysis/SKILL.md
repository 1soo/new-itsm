---
name: requirements-analysis
description: 요구사항 정의서(PRD)와 기능 명세서를 EARS 기반 인수 기준과 함께 작성하는 방법과 표준 양식. 도메인별 요구사항 정리, 기능 상세 동작 명세가 필요할 때 사용한다.
---

# 요구사항 분석 (분석 단계)

`analyzer`가 사용하는 분석 단계 skill. 작성하는 산출물 종류에 따라 `references`의 하위 폴더를 참조한다.

## 참조 안내

| 산출물 | 참조 시점 | 참고 문서 | 저장 위치 |
|--------|-----------|-----------|-----------|
| 요구사항 정의서(PRD) | 도메인별 사용자 요구사항을 처음 정리·명세화할 때 | [references/requirements-definition/conventions.md](references/requirements-definition/conventions.md), [references/requirements-definition/template.md](references/requirements-definition/template.md) | `docs/01_analyze/prd/{domain}.md` |
| 기능 명세서 | 요구사항 정의서 작성 이후, 각 기능의 상세 동작(입출력·처리 흐름·예외)을 명세할 때 | [references/feature-specification/conventions.md](references/feature-specification/conventions.md), [references/feature-specification/template.md](references/feature-specification/template.md) | `docs/01_analyze/feature/{domain}.md` |

## 공통 원칙

- 요구사항에 명시된 내용만 기술한다.
- 인수 기준은 EARS 표기법(6가지 기본 패턴)으로 작성한다. 패턴 정의는 `analyzer` 에이전트 정의를 따른다.
- 요구사항(REQ-ID)과 기능(FEAT-ID)은 서로 추적 가능하게 연결한다.
- 도메인 용어는 `docs/00_context/glossary.md`와 일관되게 사용하고, 새 용어는 그 자리에서 추가·갱신한다.
