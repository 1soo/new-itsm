---
name: integration-testing
description: 도메인별 통합 테스트 시나리오 작성과 수행 규칙. 요구사항·기능 명세(@docs/01_analyze) 기반으로 빌드 테스트를 포함한 시나리오를 만들고, 새 창에서 수행하며 결과를 기록한다.
---

# 통합 테스트 수행

요구사항·기능 명세를 기준으로 도메인별 통합 테스트 시나리오를 작성하고 수행한다.

## MCP

- **playwright MCP**: 화면/플로우 E2E 수행. 매 테스트마다 **새 창(새 context)** 에서 실행하여 캐싱 오작동을 방지한다.
- **Database MCP**(구성된 경우): 데이터 정합성·암호화 저장 등 DB 검증.

## 시나리오 작성 규칙

- 요구사항의 **EARS 인수 기준(정상 + 예외/Unwanted)** 을 테스트 항목으로 매핑한다.
- **빌드 테스트를 시나리오에 포함**한다.
- 인증·인가 플로우(로그인, 토큰 검증, 권한 부족 403)를 포함한다.
- 각 항목에 `@docs/01_analyze/...` 로 근거 요구사항/기능 위치를 명시한다.
- 시나리오에 없는 내용은 만들지도, 수행하지도 않는다.

## 산출물

**한 번의 테스트 실행마다 실행 시작 시각 폴더 `docs/04_test/{yyyyMMdd-HHmmss}/{domain}/` 를 만들고**, 그 안에 시나리오를 먼저 작성한 뒤 테스트를 수행하고 결과를 기록한다. (`{yyyyMMdd-HHmmss}` = 테스트 수행 시작 시각)

- 시나리오: `docs/04_test/{yyyyMMdd-HHmmss}/{domain}/scenario/{domain}.md`
- 결과: `docs/04_test/{yyyyMMdd-HHmmss}/{domain}/result/{domain}.md`

## docs

시나리오·결과 양식과 규칙은 [references/scenario-rules.md](references/scenario-rules.md)를 따른다.
