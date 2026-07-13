---
name: feature-specification
description: 기능 명세서를 직관적이고 간결한 형태로, EARS 기반 인수 기준과 함께 작성하는 방법과 표준 양식. 요구사항 정의서를 바탕으로 각 기능의 동작을 상세히 명세할 때 사용한다.
---

# 기능 명세서 작성

요구사항 정의서 바탕으로 각 기능 동작을 **직관적이고 간결하게** 명세.

## 작성 원칙

- 요구사항(REQ-ID)과 기능(FEAT-ID)을 추적 가능하게 연결.
- 요구사항에 명시된 동작만 기술. 추측·확장 금지.
- 인수 기준은 EARS 표기법으로 작성, 예외/오류 처리(Unwanted Behaviour) 반드시 포함.

## 양식

표준 양식은 [references/template.md](references/template.md)를 그대로 사용.

## 절차

1. 기능 목록 표 작성, 각 기능을 요구사항(REQ-ID)에 매핑.
2. 기능별 상세(설명·입출력·처리 흐름·인수 기준·예외 처리) 작성.
3. 완성 문서를 `docs/01_analyze/feature/{domain}.md`에 저장.
