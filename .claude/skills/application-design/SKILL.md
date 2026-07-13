---
name: application-design
description: API 명세서를 직관적이고 간결한 markdown으로 작성하는 방법과 표준 양식. endpoint·method·header·request/response body·response code와 토큰 포함 여부를 명시한 Application 설계가 필요할 때 사용한다.
---

# Application 설계 (API 명세서)

기능 명세서·화면 설계서를 바탕으로 API 명세서를 **직관적이고 간결한** markdown으로 작성한다.

## 작성 원칙

- 각 API마다 명시한다: **endpoint, method, header, request body, response body, response code**.
- 각 요청이 **토큰 포함 요청인지, 미포함 요청인지** 명확히 표기한다. (예: `인증: 필요(Access Token)` / `인증: 불필요`)
- 요구사항·기능 명세서에 없는 API는 만들지 않는다.

## 산출물 저장 위치

- `docs/02_plan/api_spec/{domain}.md`

## 양식

표준 양식은 [references/template.md](references/template.md)를 사용한다.
