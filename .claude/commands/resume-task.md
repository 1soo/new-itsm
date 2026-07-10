---
description: 토큰 소진 등으로 세션이 끊겨 작업이 중단됐을 때, 전체 Agent를 Teammate로 다시 소집하고 디스크 상태를 근거로 중단된 지점부터 개발을 재개한다.
argument-hint: <추가 요구사항>
arguments: [additional_requirements]
disable-model-invocation: true
---

# /resume-task — Agent Teams 작업 재개

이전 세션의 teammate는 모두 사라진 상태다(이 세션은 완전히 새로 시작됨). 재소집되는 에이전트에게는 이전 대화에 대한 기억이 전혀 없으므로, **디스크에 남은 산출물이 유일한 진실**이다.

## 입력

- 추가 요구사항: $additional_requirements

## 0. 중단 지점 파악 (Main이 먼저 직접 조사)

에이전트를 소집하기 전에, Main이 직접 아래를 확인해 현재 어느 단계·도메인에서 중단됐는지 파악한다:

- `CLAUDE.md`(프로젝트 루트) — 오케스트레이션 규약 재확인.
- `docs/01_analyze/` — 요구사항 정의서·기능 명세서·`tech.md` 존재 여부(분석 완료 여부).
- `docs/02_plan/` — 화면/API/DB/보안/역할 설계 존재 여부(설계 완료 여부).
- `docs/03_develop/plan/` — 도메인별 개발 계획 파일. 어떤 도메인까지 계획이 세워졌는지.
- `source/frontend/`, `source/backend/`, `source/db/` — 실제 구현된 도메인과 각 디렉토리의 `CLAUDE.md`.
- `docs/04_test/{yyyyMMdd-HHmmss}/` — 가장 최근 타임스탬프 폴더의 `scenario/`·`result/`. 실패 항목이 남아있는지.
- `git log`, `git status`, `git diff` — 마지막으로 커밋(=완료)된 도메인과, 커밋되지 않은 변경사항(=진행 중이던 작업).

위 조사로 다음을 판단한다: **① 현재 단계**(분석/설계/개발/테스트) **② 진행 중이던 도메인** **③ 그 도메인의 상태**(계획만 있음 / 개발 중 / 테스트 실패 대기 중 / 재테스트 대기 중).

## 1. 전체 Teammate 소집 (Main만 수행)

`/team-development`와 동일한 8개 역할을 이름을 지정해 **Teammate로(백그라운드)** 소집한다. `docs/01_analyze/tech.md`가 있으면 그 팀 구성을 따른다(없으면 CSR 5인 개발 구성 기본값).

| name | subagent_type |
|---|---|
| `analyzer` | `analyzer` |
| `designer` | `designer` |
| `dev-lead` | `dev-lead` |
| `dev-ui` | `developer` (역할: UI) |
| `dev-frontend` | `developer` (역할: FE) |
| `dev-backend` | `developer` (역할: BE) |
| `dev-database` | `developer` (역할: DB) |
| `tester` | `tester` |

각 소집 시 반드시 **"단계별로 생각할 것"** 과, **"이전 세션에 대한 기억이 없으니 대화 기억을 신뢰하지 말고 `source/`·`docs/`·각 디렉토리의 `CLAUDE.md`를 직접 읽어 현재 상태를 파악할 것"** 을 명시적으로 지시한다.

> 0단계에서 파악한 현재 단계가 분석/설계 단계뿐이라 아직 개발 에이전트가 필요 없는 경우에도, 이 커맨드는 8개 전원을 소집한다(추후 단계에서 바로 합류할 수 있도록). 개발 에이전트들은 자기 차례가 될 때까지 대기한다.

## 2. 재개 킥오프 (Main이 개입하는 시작점)

0단계에서 파악한 **현재 단계를 담당하는 에이전트 한 명에게만** `SendMessage`로 상황을 전달한다:

- **분석 미완료**면 `analyzer`에게: 어디까지 분석됐는지, 남은 도메인/미확정 사항.
- **설계 미완료**면 `designer`에게: 완료된 분석 산출물 위치, 남은 설계 산출물.
- **개발/테스트 단계**면 `dev-lead`에게: 마지막으로 커밋 완료된 도메인, 현재 진행 중이던 도메인과 그 상태(개발 중/테스트 대기/재테스트 대기/실패 항목), 참고할 파일 위치(`source/`, `docs/03_develop/plan/`, `docs/04_test/` 최신 결과).
- 추가 요구사항($additional_requirements)이 있으면 위 전달 내용에 함께 포함한다.

전달받은 에이전트는 `CLAUDE.md` 워크플로우에 따라 필요한 동료 에이전트에게 **직접 SendMessage로** 후속 지시를 이어간다. 추가 요구사항이 있으면 함께 전달한다.

## 3. Main의 역할 제한 (반드시 준수)

- 1·2단계 이후, **Main은 에이전트 간 소통을 절대 중재하지 않는다.**
- Main이 개입하는 경우는 다음 **두 가지뿐**이다:
  1. `analyzer`가 요구사항 분석 중 모호하거나 결정이 필요한 부분을 **Main에게 직접 질문**할 때.
  2. 에이전트 간 소통으로도 결론이 나지 않는 모호하거나 결정이 필요한 사안이 **Main에게 에스컬레이션**될 때.
- 그 외에는 개입하지 않고 진행 상황만 관찰한다.

## 4. 완료

- 모든 도메인의 개발+테스트가 끝나면 `dev-lead`가 Main에게 최종 결과를 보고한다. Main은 이를 사용자에게 요약해 전달한다.
