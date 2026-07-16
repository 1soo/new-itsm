---
name: integration-testing
description: 도메인별 통합 테스트 시나리오 작성과 수행 규칙. 요구사항·기능 명세(@docs/01_analyze) 기반으로 빌드 테스트를 포함한 시나리오를 만들고, 새 창에서 수행하며 결과를 기록한다.
---

# 통합 테스트 수행

요구사항·기능 명세 기준으로 도메인별 통합 테스트 시나리오를 작성·수행.

## MCP

- **playwright MCP**: 화면/플로우 E2E 수행. 매 테스트마다 **새 창(새 context)** 에서 실행해 캐싱 오작동 방지.
- **Database MCP**(구성된 경우): 데이터 정합성·암호화 저장 등 DB 검증.

## 시나리오 작성 규칙

- 요구사항의 **EARS 인수 기준(정상 + 예외/Unwanted)** 을 테스트 항목으로 매핑.
- **빌드 테스트를 시나리오에 포함**.
- 인증·인가 플로우(로그인, 토큰 검증, 권한 부족 403) 포함.
- 각 항목에 `@docs/01_analyze/...` 로 근거 요구사항/기능 위치 명시.
- 시나리오에 없는 내용은 만들지도, 수행하지도 않는다.

## 산출물

**한 번의 테스트 실행마다 실행 시작 시각 폴더 `docs/04_test/{yyyyMMdd-HHmmss}/{domain}/` 를 만들고**, 그 안에 시나리오를 먼저 작성한 뒤 테스트를 수행하고 결과를 기록한다. (`{yyyyMMdd-HHmmss}` = 테스트 수행 시작 시각)

- 시나리오: `docs/04_test/{yyyyMMdd-HHmmss}/{domain}/scenario.md`
- 결과: `docs/04_test/{yyyyMMdd-HHmmss}/{domain}/result/{domain}.md` (스크린샷·API 근거 등 증적 파일도 `result/` 하위에 저장)
- 허브: `docs/04_test/CLAUDE.md` (없으면 최초 1회 아래 형식으로 신규 생성)
  - `tester`는 `result/{domain}.md` 작성 후 이 표에 한 줄 추가한다.
  - 이전 이력 확인 시 이 표만 먼저 보고 관련 있는 항목만 선택 열람한다(전수 조사 금지).

```markdown
# docs/04_test — 통합 테스트 이력 인덱스

도메인별 통합 테스트 실행 이력을 기록한다.

## 구조

- `{yyyyMMdd-HHmmss}/{domain}/scenario.md`: 실행 시나리오
- `{yyyyMMdd-HHmmss}/{domain}/result/{domain}.md`: 실행 결과 (증적 파일은 result/ 하위에 저장)

## 인덱스

`tester` 에이전트는 결과 작성 후 아래 표에 한 줄씩 추가한다.
이전 실행 이력 확인 시 **이 표만 먼저 확인**해 관련 있어 보이는 항목의
`result/{domain}.md`만 선택 열람한다. (전수 조사 금지 — 토큰 최소화)

| 일시 | 도메인 | 결과 | 요약 | 경로 |
|------|--------|------|------|------|
```

## docs

시나리오·결과 양식과 규칙은 [references/scenario-rules.md](references/scenario-rules.md)를 따른다.
