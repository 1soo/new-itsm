# CLAUDE.md — Agent Teams 오케스트레이션 규약

이 프로젝트는 요구사항 → 분석 → 설계 → 개발 → 테스트를 **Agent Teams**로 수행한다.
Main(팀 Lead = 나)이 모든 에이전트를 소집하고, 각 단계를 조율한다.

## 사전 조건 (Agent Teams 활성화)

Agent Teams는 실험적 기능이라 `.claude/settings.json`(또는 `settings.local.json`)에서 활성화해야 한다.

```json
{
  "env": { "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1" },
  "teammateMode": "in-process"
}
```

## 보조 스킬 (caveman / ponytail)

`.claude/settings.json`에 프로젝트 범위(project-scoped) 플러그인으로 등록되어 있다 (`extraKnownMarketplaces` + `enabledPlugins`). 최초 사용 시 플러그인 신뢰 확인 프롬프트가 뜰 수 있다.

| 스킬 | 목적 | 명령어 | 사용 대상 |
|------|------|--------|-----------|
| [caveman](https://github.com/JuliusBrussee/caveman) | CLAUDE.md 생성 시, teammate(agent) 간 `SendMessage` 소통 시 불필요한 말을 줄이고 핵심만 간결하게 압축 | `/caveman lite` 또는 `/caveman full` | 모든 Agent |
| [ponytail](https://github.com/DietrichGebert/ponytail) | 작업 계획 수립 시 실제로 필요한 내용만 작업하도록 하고, 기존 컴포넌트·모듈 재사용을 우선하도록 유도 | `/ponytail lite` 또는 `/ponytail full` | `designer`, `dev-lead`, `developer`(UI/FE/BE/DB) |

## 팀 구성 (Roster)

모든 에이전트는 **`model: sonnet`(Sonnet 5) + `effort: high`** 로 동작한다(Sonnet 5 사용이 불가능한 경우 `model: opus` + `effort: high`로 대체한다). **소집(spawn)은 Main만 가능**하며, 에이전트끼리는 `SendMessage`와 공유 task list로 협업한다(중첩 불가, flat peer).

**Main은 요구사항에 맞는 에이전트만 소집한다.** 아래 로스터는 전체 후보 목록이며, 항상 전부를 소집하지 않는다. 요청 범위를 먼저 파악해 실제로 필요한 역할만 소집한다. (예: 화면 변경이 없으면 UI/FE 생략, 스키마 변경이 없으면 DB 생략, 이미 정의된 요구사항의 단순 수정이면 `analyzer` 생략, **이미 개발이 완료된 시스템에 대한 추가 요구사항이면 `analyzer` 대신 `maintainer`를 소집**)

| 역할 | 소집 기준 | 비고 |
|------|-----------|------|
| 분석 | `analyzer` | 요구사항 분석·산출물 작성 |
| 설계 | `designer` | 분석 산출물 기반 설계 |
| 개발 팀장 | `dev-lead` | 계획·조율 (코드 미구현) |
| 개발-UI | `developer` (역할: UI) | `ui-ux-development` skill만 사용 |
| 개발-FE | `developer` (역할: FE) | `react-development` 또는 `next-development` skill만 사용 |
| 개발-BE | `developer` (역할: BE) | `spring-boot-development` skill만 사용. DB 접근 방식(JPA/MyBatis 등)은 임의로 정하지 않고 설계자 결정(`docs/02_plan/database`)을 따른다 |
| 개발-DB | `developer` (역할: DB) | `database-development` skill만 사용 |
| 테스트 | `tester` | 도메인별 통합 테스트 |
| 유지보수 | `maintainer` | 유지보수 요청 분석(이력 조회 후 designer 전달), 유지보수 이력 기록 |

> 개발 구현 4종은 모두 `developer` 정의를 재사용하되, 소집 시 **역할 지시로 담당 skill을 한정**한다.
> **CSR** 구조에서 풀 스코프 개발이면 `dev-lead` + UI + FE + BE + DB = **최대 5개 에이전트**가 후보이며, 실제 소집은 해당 도메인 변경이 걸치는 영역만으로 한정한다.
> **SSR**에서 Next 풀스택(별도 백엔드 없음)이면 BE를 FE(Next)에 흡수해 축소할 수 있다. 실제 구성은 `docs/01_analyze/tech.md`를 따른다.

## 컨텍스트 유지 정책

- **모든 에이전트는 삭제·재소집 없이 동일 인스턴스로 유지된다.** `analyzer`, `designer`, `dev-lead`, `maintainer`는 프로젝트 전체 기간 컨텍스트를 유지하고, 개발 4종(UI/FE/BE/DB)과 `tester`는 도메인이 바뀔 때 컨텍스트만 정리한다.
- **도메인 전환 시 컨텍스트 정리**: 도메인 하나의 개발+테스트가 완료되면, `dev-lead`가 개발 팀원(UI/FE/BE/DB)과 `tester`에게 각각 `SendMessage`로 **`/compact` 수행을 지시**한다(삭제 후 재소집하지 않는다). `dev-lead`는 정리 완료 확인을 받은 뒤에만 새 도메인 범위·계획을 전달한다.
- **컨텍스트 사용량 관리**: 모든 에이전트는 컨텍스트 사용량이 80%에 도달하면 `/compact`를 수행한다. **`/compact` 수행 후에도 사용량이 50% 이상이면 `/clear`를 수행한다.**
- 컨텍스트 정리(compact/clear) 후에는 이전 도메인에 대한 기억이 요약되었거나 사라졌을 수 있다. `dev-lead`는 매번 **현재 도메인의 범위·계획**과 **참고할 기존 코드/컨벤션의 파일 위치**(`source/`, `docs/`)를 메시지에 명시하고, 개발 팀원·`tester`는 컨벤션·직전 진행 상태를 대화 기억이 아니라 디스크(소스코드·산출물, 각 디렉토리의 `CLAUDE.md`)를 직접 읽어 파악한다.

## 워크플로우

### 0. 입력
- 요청 사항은 **text 또는 markdown 파일**로 입력된다.

### 1. 분석 (`analyzer`)
- 입력을 분석하여 산출물을 생성한다. (`docs/01_analyze/`)
- **모호하거나 결정이 필요한 부분은 Main(나)에게 질문**한다.

### 2. 설계 (`designer`)
- `analyzer` 산출물을 기반으로 설계를 진행한다. (`docs/02_plan/`)
- **모호하거나 결정이 필요한 부분은 `analyzer`에게 질문**한다.

### 3. 개발 (`dev-lead` + 개발 에이전트들)
Main이 `dev-lead`와 개발 에이전트(CSR: UI/FE/BE/DB)를 소집한 뒤, 조율은 `dev-lead`가 수행한다.

1. **계획 수립·전달**: `dev-lead`가 설계 산출물 기반으로 **도메인별 개발 계획**을 수립하고, 각 개발 에이전트에게 **현재 개발 범위와 계획**을 전달한다.
2. **개발 진행**: 각 개발 에이전트는 개발을 진행하며, **다른 영역의 도움이 필요하면 해당 개발 에이전트와 직접 소통**(`SendMessage`)한다. 본인이 새로 생성하는 모든 디렉토리에는 해당 디렉토리의 파일·하위 디렉토리를 설명하는 `CLAUDE.md`를 함께 생성한다(결과적으로 모든 디렉토리에 `CLAUDE.md`가 존재해야 한다). 기존 디렉토리에서 작업할 때는 **모든 `CLAUDE.md`를 한꺼번에 읽지 않고, root부터 하위 디렉토리로 한 단계씩 내려가며** 본인 작업 목적에 해당하는 디렉토리를 찾아 그 `CLAUDE.md`를 읽고 참조한다.
3. **질문 에스컬레이션**: 논의 중 모호하거나 결정이 필요하면 **`dev-lead`에게 질문**하고, `dev-lead`가 결정하지 못하면 **`designer`에게 질문**한다.
4. **테스트 요청**: 도메인 하나가 개발 완료되면 `dev-lead`가 **`tester`에게 통합 테스트를 요청**한다.
5. **결과 분석·오류 수정**: `tester`가 결과를 전달하면 `dev-lead`가 분석하여, **실패 항목을 가장 적합한 개발 에이전트에게 오류 해결 요청**한다.
6. **재테스트 루프**: 개발 에이전트가 `dev-lead`에게 **오류 해결 완료**를 알리면, `dev-lead`가 **`tester`에게 재테스트를 요청**한다. (5~6은 **실패가 없어질 때까지 반복**)
7. **도메인 커밋**: 실패 항목이 없어 해당 도메인의 **개발과 테스트가 완료되면**, `dev-lead`가 변경사항을 **직관성 높고 간결한 commit message**로 정리하여 `git add` → `git commit` → `git push`(**main branch**)를 수행한다.
8. **다음 도메인**: `dev-lead`가 **다음 도메인 범위·계획을 개발 에이전트에 전달**한다. (모든 도메인 완료까지 4~7 반복)
9. **완료 보고**: 모든 도메인 개발이 완료되면 `dev-lead`가 **Main(나)에게 결과를 보고**한다.

### 4. 테스트 (`tester`)
- 개발 에이전트가 개발한 **도메인에 대해 통합 테스트**를 진행하고, **결과를 `dev-lead`에게 전달**한다.

## 유지보수 워크플로우

**이미 개발이 완료된 시스템**에 사용자가 추가 요구사항을 제시하는 경우, 위 워크플로우의 1단계(분석) 대신 아래 절차를 따른다. `analyzer` 대신 **`maintainer`**를 소집한다.

1. **요구사항 분석 (`maintainer`)**: `maintenance-request-analysis` skill로 추가 요구사항을 도메인별로 분석한다. 모호하거나 결정이 필요한 부분은 사용자에게 재질문한다. `docs/06_maintenance/CLAUDE.md` 인덱스로 유사 유지보수 이력을 조회하고(전수 조사 금지, 최소 토큰), 있다면 [도메인/요구사항/해결 방법] 위주로 간략히 정리해 `designer`에게 함께 전달한다.
2. **설계 (`designer`)**: 전달받은 내용을 기반으로 설계를 진행한다. (`docs/02_plan/`) 모호하면 `maintainer`에게 질문한다.
3. **개발 (`dev-lead` + 개발 에이전트들)**: 설계를 도메인별 개발 계획으로 나누고, 위 "3. 개발" 절차(계획 수립 → 개발 → 질문 에스컬레이션 → 테스트 요청 → 재테스트 루프 → 도메인 커밋 → 다음 도메인)를 그대로 따른다. **개발 Agent와 Test Agent는 동시간대에 작동하지 않는다** — 한 도메인의 개발이 모두 끝난 뒤에만 그 도메인의 테스트를 요청한다.
4. **이력 전달**: 모든 도메인의 유지보수 개발+테스트가 완료되면 `dev-lead`가 `Bash`로 `date +"%Y%m%d-%H%M%S"`를 실행해 확인한 현재 KST 시각을 타임스탬프로 하여, 도메인별 수정 내역과 함께 `maintainer`에게 전달한다. (`maintainer`는 `Bash`가 없어 시각을 직접 조회할 수 없으므로, 이 타임스탬프는 반드시 `dev-lead`가 전달한다)
5. **이력 생성 (`maintainer`)**: `maintenance-history-report` skill로 `docs/06_maintenance/{yyyyMMdd-HHmmss}/{domain}/report.md`에 이력을 저장하고, `docs/06_maintenance/CLAUDE.md` 인덱스를 갱신한다. (`{yyyyMMdd-HHmmss}`는 `dev-lead`로부터 전달받은 타임스탬프를 그대로 사용하며, 임의로 생성·추측하지 않는다)
6. **이력 커밋**: 이력 생성이 끝나면 `maintainer`가 `dev-lead`에게 완료를 알리고, `dev-lead`가 해당 이력 문서(`docs/06_maintenance/` 변경분)를 `git add` → `commit` → `push`(main)한다. (`maintainer`는 git 권한이 없으므로 직접 커밋하지 않는다.)

## 통신 · 협업 규칙

- **소집**: teammate는 하위 teammate를 만들 수 없다. 필요한 에이전트는 **Main이 모두 소집**하고, `dev-lead`는 메시지로 조율만 한다.
- **역할당 인스턴스 1개 원칙**: 각 역할(`analyzer`/`designer`/`dev-lead`/`maintainer`/`developer`(UI·FE·BE·DB 각각)/`tester`)은 **Main이 정확히 한 번만 소집**한다. 이미 소집된 역할에게 후속 지시·추가 요청을 보낼 때는 **`Agent`로 재소집하지 않고 `SendMessage(to: 기존 이름, ...)`로만 통신**한다. `Agent`를 같은 이름으로 다시 호출하면 이전 기억이 없는 별개의 새 인스턴스가 생성되어(동일 이름이라도 내부적으로 별도 ID 부여) 같은 역할의 인스턴스가 중복되고 컨텍스트가 갈라지므로 반드시 피한다.
- **직접 통신**: 질문·요청·완료 보고는 `SendMessage(to, message)`로 **대상에게 직접** 보낸다(중계 금지).
- **작업 관리**: 진행 상황은 공유 task list(`TaskCreate`/`TaskUpdate`/claim)로 관리한다.
- **파일 소유 분리(충돌 방지)**: 같은 파일을 동시에 수정하지 않는다. 모든 소스코드는 root의 `source/` 하위에 저장한다.
  - UI: 디자인 시스템·공통 컴포넌트 (`source/frontend/`의 공통 영역)
  - FE: 화면/라우팅/상태/apiClient (`source/frontend/`의 기능 영역)
  - BE: `source/backend/`
  - DB: `source/db/`

## Git 규약

- **소스 저장 위치**: 모든 개발 소스코드는 root의 `source/` 하위(`source/frontend/`, `source/backend/`, `source/db/`)에 저장한다.
- **커밋 주체·시점**: `dev-lead`가 **도메인 하나의 개발+테스트가 완료될 때마다** `git add` → `commit` → `push`(main)를 수행한다. 유지보수 워크플로우에서는 이후 `maintainer`가 생성하는 이력 문서(`docs/06_maintenance/`)에 대해서도 `dev-lead`가 동일하게 커밋·push한다.
- **commit message**: 직관성 높고 간결하게 작성한다. (예: `feat(auth): 로그인/토큰 재발급 도메인 구현 및 통합테스트 통과`)
- **버전관리 대상**: `.claude`, `docs`, `source`, `.mcp.json`, `CLAUDE.md`.
- **.gitignore 대상**: `.env` 계열, build 결과물, 캐싱·의존성 디렉토리(예: `node_modules`, `.next`, `target`, `build`).

## 공통 원칙

- 각 단계 산출물의 **저장 위치와 형식**은 해당 에이전트/skill 정의를 따른다.
- **이전 단계 산출물에 없는 내용은 생성/구현하지 않는다.** 모호하면 규정된 대상에게 질문한다.
- 단계별로 생각한다.
