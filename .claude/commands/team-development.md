---
description: Agent Teams로 신규 서비스 구축에 착수한다. Main이 전체 에이전트를 Teammate로 소집한 뒤 analyzer부터 시작해, 이후 각 에이전트가 SendMessage로 직접 소통하며 분석→설계→개발→테스트를 진행한다.
argument-hint: <구축 서비스명> <참조파일 위치>
arguments: [service_name, reference_path]
disable-model-invocation: true
---

# /team-development — Agent Teams 개발 착수

당신(Main)은 이 프로젝트의 `CLAUDE.md` 오케스트레이션 규약에 따라 전체 팀을 **한 번에** 소집하고, 이후에는 에이전트 간 소통에 개입하지 않는다.

## 입력

- 구축 서비스명: $service_name
- 참조파일(가이드/요구사항/설계 문서 등) 위치: $reference_path

## 0. 사전 확인

- `CLAUDE.md`(프로젝트 루트)를 읽고 에이전트 로스터·통신 규칙·컨텍스트 정책을 확인한다.
- `.claude/settings.local.json`에 Agent Teams 활성화 설정(`CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS`, `teammateMode: in-process`)이 되어 있는지 확인한다. 없으면 먼저 설정한다.
- `docs/01_analyze/tech.md`가 이미 있으면 그 내용(CSR/SSR, 팀 구성)을 확인해 아래 소집 목록을 그에 맞게 조정한다. 없으면(완전 신규 구축) 기본값인 CSR 5인 개발 구성으로 소집하고, `analyzer`가 기술스택을 확정하며 필요 시 조정하도록 한다.

## 1. 전체 Teammate 소집 (Main만 수행, 이 커맨드에서 유일하게 수행하는 소집)

`Agent` 툴로 아래 전원을 **이름을 지정해 Teammate로(백그라운드)** 소집한다. 각 소집 시 반드시 **"단계별로 생각할 것"** 을 명시적으로 지시한다.

| name | subagent_type | 소집 시 지시 요지 |
|---|---|---|
| `analyzer` | `analyzer` | 요구사항 분석 담당. 단계별로 생각할 것. 모호하거나 결정이 필요한 부분은 Main에게 직접 질문할 것. |
| `designer` | `designer` | 설계 담당. 단계별로 생각할 것. 모호하거나 결정이 필요한 부분은 `analyzer`에게 SendMessage로 질문할 것. |
| `dev-lead` | `dev-lead` | 개발 조율 담당(코드 미구현). 단계별로 생각할 것. 결정하지 못하는 설계 이슈는 `designer`에게 질문할 것. |
| `dev-ui` | `developer` | 역할: UI. `ui-ux-development` skill만 사용. 단계별로 생각할 것. |
| `dev-frontend` | `developer` | 역할: FE. `react-development` 또는 `next-development` skill만 사용(확정 스택에 맞춰). 단계별로 생각할 것. |
| `dev-backend` | `developer` | 역할: BE. `spring-boot-development` skill만 사용. 단계별로 생각할 것. |
| `dev-database` | `developer` | 역할: DB. `database-development` skill만 사용. 단계별로 생각할 것. |
| `tester` | `tester` | 도메인별 통합 테스트 담당. 단계별로 생각할 것. |

> SSR Next 풀스택으로 확정되어 `dev-backend`가 불필요해지면, `dev-lead`가 이를 확인한 뒤 Main에게 SendMessage로 알리고, Main이 해당 teammate를 정리한다. 그 외의 경우 위 8개 전원을 그대로 유지한다.

## 2. 최초 킥오프 (Main이 개입하는 시작점)

전원 소집이 끝나면 **`analyzer`에게만** `SendMessage`로 아래를 전달한다:

- 구축 서비스명: $service_name
- 참조파일 위치: $reference_path
- `CLAUDE.md`의 워크플로우(분석 → 설계 → 개발 → 테스트)를 따르고, 각 단계 산출물이 나오면 다음 에이전트에게 **직접 SendMessage로** 전달할 것.
- 단계별로 생각할 것.

## 3. Main의 역할 제한 (반드시 준수)

- 1·2단계 이후, **Main은 에이전트 간 소통을 절대 중재하지 않는다.** 모든 협업은 에이전트끼리 `SendMessage`와 공유 task list로 직접 수행한다.
- Main이 개입하는 경우는 다음 **두 가지뿐**이다:
  1. `analyzer`가 요구사항 분석 중 모호하거나 결정이 필요한 부분을 **Main에게 직접 질문**할 때.
  2. 에이전트 간 소통으로도 결론이 나지 않는 모호하거나 결정이 필요한 사안이 **Main에게 에스컬레이션**될 때(예: `dev-lead` → `designer`로도 해결되지 않아 Main에게 전달되는 경우).
- 위 두 경우가 아니면 Main은 개입하지 않고 진행 상황만 관찰한다. 에이전트에게 먼저 말을 걸거나, 대신 결정하거나, 메시지를 대신 전달(중계)하지 않는다.

## 4. 완료

- 모든 도메인의 개발+테스트가 끝나면 `dev-lead`가 Main에게 최종 결과를 보고한다. Main은 이를 사용자에게 요약해 전달한다.
