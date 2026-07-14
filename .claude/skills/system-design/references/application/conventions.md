# API 명세 컨벤션

기능 명세서·화면 설계서를 바탕으로 API 명세서를 **직관적이고 간결한** markdown으로 작성한다.

## 작성 원칙

- 각 API마다 명시한다: **endpoint, method, header, request body, response body, response code**.
- 각 요청이 **토큰 포함 요청인지, 미포함 요청인지** 명확히 표기한다. (예: `인증: 필요(Access Token)` / `인증: 불필요`)
- 요구사항·기능 명세서에 없는 API는 만들지 않는다.
- Backend가 Supabase로 지정되고 단순 CRUD로 커버되는 endpoint는 커스텀 명세 대신 **"Supabase 자동생성 API 사용"**으로 표기한다. 복잡한 비즈니스 로직만 커스텀 endpoint로 설계한다. (`implementation` skill의 `references/database/supabase/conventions.md` 참고)

## 양식

표준 양식은 [template.md](template.md)를 사용한다.
