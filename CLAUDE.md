# CLAUDE.md — Agent Teams 오케스트레이션 규약

요구사항 → 분석 → 설계 → 개발 → 테스트를 **Agent Teams**로 수행한다.
Main(팀 Lead = 나)이 모든 에이전트를 소집하고 각 단계를 조율한다.

## 사전 조건 (Agent Teams 활성화)

Agent Teams는 실험적 기능이라 `.claude/settings.json`(또는 `settings.local.json`)에서 활성화한다.

```json
{
  "env": { "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1" },
  "teammateMode": "in-process"
}
```

## 보조 스킬 (caveman / ponytail)

`.claude/settings.json`에 프로젝트 범위(project-scoped) 플러그인으로 등록됨 (`extraKnownMarketplaces` + `enabledPlugins`). 최초 사용 시 플러그인 신뢰 확인 프롬프트가 뜰 수 있다.

| 스킬 | 목적 | 명령어 | 사용 대상 |
|------|------|--------|-----------|
| [caveman](https://github.com/JuliusBrussee/caveman) | CLAUDE.md 생성 및 teammate 간 `SendMessage` 소통 시 핵심만 간결히 압축 | `/caveman lite` 또는 `/caveman full` | 모든 Agent |
| [ponytail](https://github.com/DietrichGebert/ponytail) | 계획 수립 시 꼭 필요한 것만 작업, 기존 컴포넌트·모듈 재사용 우선 | `/ponytail lite` 또는 `/ponytail full` | `designer`, `dev-lead`, `developer`(UI/FE/BE/DB) |

## 팀 구성 (Roster)

모든 에이전트는 **`model: sonnet`(Sonnet 5) + `effort: high`**로 동작한다(Sonnet 5 불가 시 `model: opus` + `effort: high`로 대체). **소집(spawn)은 Main만 가능**, 에이전트끼리는 `SendMessage`와 공유 task list로 협업한다(중첩 불가, flat peer).

**Main은 요구사항에 맞는 에이전트만 소집한다.** 아래 로스터는 전체 후보 목록이며 항상 전부 소집하지 않는다. 요청 범위를 먼저 파악해 필요한 역할만 소집한다. (예: 화면 변경 없으면 UI/FE 생략, 스키마 변경 없으면 DB 생략, 이미 정의된 요구사항의 단순 수정이면 `analyzer` 생략, **이미 개발 완료된 시스템의 추가 요구사항이면 `analyzer` 대신 `maintainer` 소집**)

| 역할 | 소집 기준 | 비고 |
|------|-----------|------|
| 분석 | `analyzer` | 요구사항 분석·산출물 작성 |
| 설계 | `designer` | 분석 산출물 기반 설계 |
| 개발 팀장 | `dev-lead` | 계획·조율 (코드 미구현) |
| 개발-UI | `developer` (역할: UI) | `implementation` skill 사용(`references/ui-ux` 참고) |
| 개발-FE | `developer` (역할: FE) | `implementation` skill 사용(`references/react` 또는, CSR에서 Next가 Frontend로 지정된 경우 `references/next` 참고) |
| 개발-BE | `developer` (역할: BE) | `implementation` skill 사용(`references/spring-boot` 또는, SSR에서 Next가 지정된 경우 `references/next` 참고). DB 접근 방식(JPA/MyBatis 등)은 설계자 결정(`docs/02_plan/database`)을 따른다 |
| 개발-DB | `developer` (역할: DB) | `implementation` skill 사용(`references/database`, Supabase 지정 시 `references/database/supabase` 참고) |
| 테스트 | `tester` | 도메인별 통합 테스트 |
| 유지보수 | `maintainer` | 유지보수 요청 분석(이력 조회 후 designer 전달), 유지보수 이력 기록 |

> 개발 구현 4종은 모두 `developer` 정의를 재사용하되, 소집 시 **역할 지시로 담당 skill을 한정**한다.
> **CSR** 풀 스코프 개발이면 `dev-lead` + UI + FE + BE + DB = **최대 5개 에이전트**가 후보이며, 실제 소집은 해당 도메인 변경이 걸치는 영역만으로 한정한다.
> **SSR** Next 풀스택(별도 백엔드 없음)이면 Next가 서버 역할까지 수행한다. `implementation` skill의 `references/next`는 Frontend로 고정되지 않으며, CSR에서 Frontend로 지정된 경우 클라이언트 Frontend로, SSR에서 지정된 경우 서버 역할(Route Handler·Server Action 등)로 사용한다. 이 경우 개발-BE가 `references/spring-boot` 대신 `references/next`로 서버 로직을 담당해 별도 Backend 에이전트를 축소할 수 있다. 실제 구성은 `docs/01_analyze/tech.md`를 따른다.
> **Supabase**를 Database/BaaS로 지정한 경우(`docs/01_analyze/tech.md` 결정에 따름), 인증·DB·File Storage·Realtime·자동 생성 API 중 요구사항에 필요한 기능만 개발-DB(`implementation` skill의 `references/database/supabase` 하위 문서)가 구성한다. 그만큼 개발-BE의 커스텀 구현 범위가 줄어들며, 자동 생성 API로 전부 커버되는 도메인은 개발-BE를 생략할 수 있다.

## 컨텍스트 유지 정책

- **모든 에이전트는 삭제·재소집 없이 동일 인스턴스로 유지된다.** `analyzer`, `designer`, `dev-lead`, `maintainer`는 프로젝트 전체 기간 컨텍스트 유지, 개발 4종(UI/FE/BE/DB)과 `tester`는 도메인이 바뀔 때 컨텍스트만 정리한다.
- **도메인 전환 시 컨텍스트 정리**: 도메인 하나의 개발+테스트 완료 시, `dev-lead`가 개발 팀원(UI/FE/BE/DB)과 `tester`에게 `SendMessage`로 **`/compact` 수행을 지시**한다(삭제 후 재소집 안 함). `dev-lead`는 정리 완료 확인 후에만 새 도메인 범위·계획을 전달한다.
- **컨텍스트 사용량 관리**: 사용량 80% 도달 시 `/compact`. **`/compact` 후에도 50% 이상이면 `/clear`.**
- 컨텍스트 정리(compact/clear) 후엔 이전 도메인 기억이 요약·소실됐을 수 있다. `dev-lead`는 매번 **현재 도메인 범위·계획**과 **참고할 기존 코드/컨벤션 파일 위치**(`source/`, `docs/`, `docs/00_context/glossary.md`)를 메시지에 명시하고, 개발 팀원·`tester`는 컨벤션·직전 진행 상태를 대화 기억이 아니라 디스크(소스코드·산출물, 각 디렉토리의 `CLAUDE.md`)를 직접 읽어 파악한다.

## 공유 용어집

도메인 용어(엔티티명·상태값·약어 등)를 정의하는 `docs/00_context/glossary.md`를 둔다.

- **갱신**: `analyzer`가 요구사항 분석 중 새 용어를 정의하고, `designer`가 설계 중 용어를 구체화·추가한다.
- **참조**: 모든 에이전트가 작업 시작 시 참조한다. `dev-lead`는 도메인 전달 메시지에 이 파일 위치를 함께 명시한다.
- **형식**: `- **용어**: 정의 (관련 도메인)` 목록. 간결하게 유지하고 정의가 바뀌면 그 자리에서 갱신한다(이력 남기지 않음).

## 워크플로우

### 0. 입력
- 요청 사항은 **text 또는 markdown 파일**로 입력된다.

### 1. 분석 (`analyzer`)
- 입력을 분석해 산출물 생성 (`docs/01_analyze/`)
- **모호하거나 결정 필요한 부분은 Main(나)에게 질문**한다.

### 2. 설계 (`designer`)
- `analyzer` 산출물 기반으로 설계 (`docs/02_plan/`)
- **모호하거나 결정 필요한 부분은 `analyzer`에게 질문**한다.

### 3. 개발 (`dev-lead` + 개발 에이전트들)
Main이 `dev-lead`와 개발 에이전트(CSR: UI/FE/BE/DB)를 소집한 뒤, 조율은 `dev-lead`가 수행한다.

1. **계획 수립·전달**: `dev-lead`가 설계 산출물 기반 **도메인별 개발 계획**을 수립하고 각 개발 에이전트에 **현재 개발 범위와 계획**을 전달한다.
2. **개발 진행**: 각 개발 에이전트는 개발하며, **다른 영역 도움이 필요하면 해당 개발 에이전트와 직접 소통**(`SendMessage`)한다. 새로 생성하는 모든 디렉토리에는 그 디렉토리의 파일·하위 디렉토리를 설명하는 `CLAUDE.md`를 함께 생성한다(모든 디렉토리에 `CLAUDE.md` 존재해야 함). 기존 디렉토리 작업 시 **모든 `CLAUDE.md`를 한꺼번에 읽지 않고 root부터 하위로 한 단계씩 내려가며** 작업 목적에 해당하는 디렉토리의 `CLAUDE.md`를 읽고 참조한다.
3. **질문 에스컬레이션**: 모호하거나 결정 필요 시 **`dev-lead`에게 질문**, `dev-lead`가 결정 못 하면 **`designer`에게 질문**한다.
4. **테스트 요청**: 도메인 하나 개발 완료 시 `dev-lead`가 **`tester`에게 통합 테스트를 요청**한다.
5. **결과 분석·오류 수정**: `tester`가 결과를 전달하면 `dev-lead`가 분석해 **실패 항목을 가장 적합한 개발 에이전트에게 오류 해결 요청**한다. 개발 에이전트는 **실패를 재현해 원인을 확인한 뒤 수정**하고, 수정 완료 보고 시 원인과 수정 내용을 함께 전달한다.
6. **재테스트 루프**: 개발 에이전트가 **오류 해결 완료**를 알리면 `dev-lead`가 **`tester`에게 재테스트를 요청**한다. (5~6은 **실패가 없어질 때까지 반복**)
7. **코드 리뷰**: 통합 테스트 통과 후, `dev-lead`가 커밋 전 **Standards축**(코딩 컨벤션·`implementation` skill 컨벤션 준수)과 **Spec축**(`docs/01_analyze`·`docs/02_plan` 충실도)을 각각 독립적으로 점검한다. Standards축은 `code-review` skill로 diff를 점검하고, Spec축은 산출물과 직접 대조한다. 두 축의 결과는 섞지 않고 축별로 보고한다. 발견 사항이 있으면 5~6과 동일하게 담당 개발 에이전트에게 수정을 요청하고 재점검한다(발견 사항이 없어질 때까지 반복).
8. **도메인 커밋**: 코드 리뷰까지 발견 사항 없이 해당 도메인 **개발·테스트·리뷰 완료 시**, `dev-lead`가 변경사항을 **직관적·간결한 commit message**로 정리해 `git add` → `git commit` → `git push`(**main branch**)한다.
9. **다음 도메인**: `dev-lead`가 **다음 도메인 범위·계획을 개발 에이전트에 전달**한다. (모든 도메인 완료까지 4~8 반복)
10. **전체 구조 점검**: 모든 도메인 개발이 끝나면(유지보수 워크플로우에서는 아래 조건에 해당할 때만) `dev-lead`가 지금까지 생성된 모든 디렉토리의 `CLAUDE.md` 인덱스를 모아 훑어보고 중복·불일치 후보를 추린다. 후보로 지목된 부분만 실제 코드를 확인하고, 발견 사항이 있으면 담당 개발 에이전트에게 정리를 요청한 뒤 그 부분만 재확인한다(발견 사항이 없어질 때까지 반복).
11. **완료 보고**: 모든 도메인 개발 완료 시 `dev-lead`가 **Main(나)에게 결과를 보고**한다.

### 4. 테스트 (`tester`)
- 개발한 **도메인에 대해 통합 테스트**를 진행하고 **결과를 `dev-lead`에게 전달**한다.

## 유지보수 워크플로우

**이미 개발 완료된 시스템**에 추가 요구사항이 제시되면, 위 1단계(분석) 대신 아래 절차를 따른다. `analyzer` 대신 **`maintainer`**를 소집한다.

1. **요구사항 분석 (`maintainer`)**: `maintenance` skill(`references/request-analysis`)로 추가 요구사항을 도메인별로 분석한다. 모호하거나 결정 필요 시 사용자에게 재질문한다. `docs/06_maintenance/CLAUDE.md` 인덱스로 유사 이력을 조회하고(전수 조사 금지, 최소 토큰), 있으면 [도메인/요구사항/해결 방법] 위주로 간략히 정리해 `designer`에게 전달한다.
2. **설계 (`designer`)**: 전달받은 내용 기반 설계 (`docs/02_plan/`). 모호하면 `maintainer`에게 질문한다.
3. **개발 (`dev-lead` + 개발 에이전트들)**: 설계를 도메인별 개발 계획으로 나누고 위 "3. 개발" 절차(계획 수립 → 개발 → 질문 에스컬레이션 → 테스트 요청 → 재테스트 루프 → 코드 리뷰 → 도메인 커밋 → 다음 도메인)를 그대로 따른다. **개발 Agent와 Test Agent는 동시간대에 작동하지 않는다** — 한 도메인 개발이 모두 끝난 뒤에만 그 도메인 테스트를 요청한다.
4. **전체 구조 점검 (조건부)**: 이번 유지보수가 **2개 이상 도메인**에 걸치거나 **공통 요소**(UI 공통 컴포넌트·공통 `apiClient`·DB 공통 스키마 등)를 건드렸을 때만, 위 "3. 개발" 절차의 전체 구조 점검을 수행한다. (단일 도메인의 사소한 수정이면 생략 — 해당 도메인 코드 리뷰가 이미 커버함)
5. **이력 전달**: 모든 도메인의 유지보수 개발+테스트 완료 시 `dev-lead`가 `Bash`로 `date +"%Y%m%d-%H%M%S"`를 실행해 확인한 현재 KST 시각을 타임스탬프로, 도메인별 수정 내역과 함께 `maintainer`에게 전달한다.
6. **이력 생성 (`maintainer`)**: `maintenance` skill(`references/history-report`)로 `docs/06_maintenance/{yyyyMMdd-HHmmss}/{domain}/report.md`에 이력을 저장하고 `docs/06_maintenance/CLAUDE.md` 인덱스를 갱신한다. (`{yyyyMMdd-HHmmss}`는 `dev-lead`가 전달한 타임스탬프를 그대로 사용, 임의 생성·추측 금지)
7. **이력 커밋**: 이력 생성 후 `maintainer`가 `dev-lead`에게 완료를 알리고, `dev-lead`가 이력 문서(`docs/06_maintenance/` 변경분)를 `git add` → `commit` → `push`(main)한다. (`maintainer`는 git 권한 없어 직접 커밋 안 함)

## 통신 · 협업 규칙

- **소집**: teammate는 하위 teammate를 만들 수 없다. 필요한 에이전트는 **Main이 모두 소집**하고 `dev-lead`는 메시지로 조율만 한다.
- **역할당 인스턴스 1개 원칙**: 각 역할(`analyzer`/`designer`/`dev-lead`/`maintainer`/`developer`(UI·FE·BE·DB 각각)/`tester`)은 **Main이 정확히 한 번만 소집**한다. 이미 소집된 역할에 후속 지시·추가 요청 시 **`Agent`로 재소집하지 않고 `SendMessage(to: 기존 이름, ...)`로만 통신**한다.
- **직접 통신**: 질문·요청·완료 보고는 `SendMessage(to, message)`로 **대상에게 직접** 보낸다(중계 금지).
- **작업 관리**: 진행 상황은 공유 task list(`TaskCreate`/`TaskUpdate`/claim)로 관리한다.
- **파일 소유 분리(충돌 방지)**: 같은 파일을 동시에 수정하지 않는다. 모든 소스코드는 root의 `source/` 하위에 저장한다.
  - UI: 디자인 시스템·공통 컴포넌트 (`source/frontend/`의 공통 영역)
  - FE: 화면/라우팅/상태/apiClient (`source/frontend/`의 기능 영역)
  - BE: `source/backend/`
  - DB: `source/db/`

## Git 규약

- **소스 저장 위치**: 모든 소스코드는 root의 `source/` 하위(`source/frontend/`, `source/backend/`, `source/db/`)에 저장한다.
- **커밋 주체·시점**: `dev-lead`가 **도메인 하나의 개발+테스트 완료 때마다** `git add` → `commit` → `push`(main)한다. 유지보수 워크플로우에서는 `maintainer`가 생성하는 이력 문서(`docs/06_maintenance/`)도 `dev-lead`가 동일하게 커밋·push한다.
- **commit message**: 직관적·간결하게. (예: `feat(auth): 로그인/토큰 재발급 도메인 구현 및 통합테스트 통과`)
- **버전관리 대상**: `.claude`, `docs`, `source`, `.mcp.json`, `CLAUDE.md`.
- **.gitignore 대상**: `.env` 계열, build 결과물, 캐싱·의존성 디렉토리(예: `node_modules`, `.next`, `target`, `build`).

## 공통 원칙

- 각 단계 산출물의 **저장 위치와 형식**은 해당 에이전트/skill 정의를 따른다.
- **이전 단계 산출물에 명시된 내용만 생성/구현한다.** 모호하면 규정된 대상에게 질문한다.
