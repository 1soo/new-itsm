# new-itsm

티켓 기반 ITSM(IT 서비스 관리) 웹 플랫폼. 서비스 요청·인시던트·문제·변경 관리, 지식베이스, IT 자산/CMDB, 인증·권한(RBAC)을 비롯해 엔터프라이즈 서비스 관리(ESM)·취약점 관리·컴플라이언스 관리·인프라 모니터링까지 총 11개 도메인을 제공한다.

## 기술 스택

| 구분 | 선택 |
|------|------|
| Frontend | React 19 (CSR/SPA) + Vite + TypeScript + Redux Toolkit + React Router + shadcn/ui(Tailwind v4) |
| Backend | Spring Boot 3 (Java, Gradle) — DDD 4계층, Spring Security + JWT |
| Database | PostgreSQL 16 |
| 로컬 인프라 | Docker(docker-compose) |

## 디렉토리 구조

```
docs/     # 분석·설계·개발계획·테스트 산출물 (01_analyze ~ 04_test)
source/
├── frontend/   # React(CSR)
├── backend/    # Spring Boot
└── db/         # PostgreSQL DDL/DML(sql/) + 로컬 컨테이너 구성(docker/)
```

각 디렉토리에는 그 위치의 파일·하위 구조를 설명하는 `CLAUDE.md`가 있다.

## 로컬 실행

### 1. Database

```bash
cd source/db/docker
cp .env.example .env   # 값 확인/수정
docker compose up -d
```

최초 기동 시 `source/db/sql/`의 DDL·시드 데이터가 파일명 순서대로 자동 실행된다.

### 2. Backend

```bash
cd source/backend
cp .env.example .env   # DB 접속 정보를 위 .env와 일치시킴
./gradlew.bat bootRun   # Windows, macOS/Linux는 ./gradlew bootRun
```

기본 포트 `8080`. Swagger UI는 `/swagger-ui.html`에서 확인할 수 있다.

### 3. Frontend

```bash
cd source/frontend
npm install
npm run dev
```

기본 포트 `5173`. dev 서버가 `/api`를 `http://localhost:8080`으로 프록시한다.

## 문서

- `docs/01_analyze/` — 요구사항 정의서(PRD)·기능 명세서·기술스택 정의서
- `docs/02_plan/` — 화면/API/DB/보안/역할/인프라 설계
- `docs/03_develop/plan/` — 도메인별 개발 계획
- `docs/04_test/{domain}/{yyyyMMdd-HHmmss}/` — 도메인별 통합 테스트 시나리오·결과

---

# Claude Agent Teams 기반 개발 프로세스

이 저장소는 요구사항 분석 → 설계 → 개발 → 테스트 전 과정을 **Claude Code의 Agent Teams**로 수행한다. Main(사용자와 대화하는 세션)이 모든 에이전트를 소집하고, 소집 이후의 실제 협업은 에이전트끼리 `SendMessage`와 공유 task list로 직접 수행한다. 상세 규약은 [`CLAUDE.md`](./CLAUDE.md) 참고.

## 1. 개요

- **Agent Teams**: `.claude/settings.local.json`에서 `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS`를 활성화하고, 각 역할을 이름이 지정된 Teammate로 소집한다.
- **소집 주체**: teammate는 하위 teammate를 만들 수 없다. 필요한 에이전트는 **Main이 전부 소집**하며, 소집 이후에는 에이전트 간 직접 통신(`SendMessage`)으로 조율한다. Main은 최초 킥오프와, 에이전트 간 소통으로도 해결되지 않는 에스컬레이션에만 개입한다.
- **컨텍스트 유지**: 모든 에이전트는 삭제·재소집 없이 동일 인스턴스로 유지된다. 분석·설계·개발 조율 에이전트는 프로젝트 전체 기간 컨텍스트를 유지하고, 개발/테스트 에이전트는 도메인이 바뀔 때 컨텍스트를 정리(`/compact`, 필요 시 `/clear`)한 뒤 다음 도메인을 이어서 진행한다.
- **파일 소유 분리**: 같은 파일을 동시에 수정하지 않도록 영역별(UI/FE/BE/DB)로 디렉토리를 분리해 작업한다. 모든 소스코드는 `source/` 하위에 저장한다.
- **모델**: 모든 에이전트는 `model: sonnet`(Sonnet 5) + `effort: high`로 동작한다(사용 불가 시 `opus` + `effort: high`로 대체).

## 2. 에이전트 구성과 역할

| 역할 | 에이전트 | 수행 내용 |
|------|----------|-----------|
| 분석 | `analyzer` | 요구사항을 도메인별로 분석해 요구사항 정의서(PRD)·기능 명세서를 EARS 기반 인수 기준과 함께 작성. 모호한 부분은 Main에게 질문. |
| 설계 | `designer` | 분석 산출물을 기반으로 화면 설계서·API 명세서·테이블 정의서·보안 설계·역할 정의·인프라 아키텍처를 작성. 모호한 부분은 `analyzer`에게 질문. |
| 개발 팀장 | `dev-lead` | 설계 산출물 기반 도메인별 개발 계획 수립, 개발 에이전트에 범위·계획 전달, 질문 조율, 테스트 요청·오류 수정 루프 관리, 도메인 완료 시 커밋. 코드는 직접 구현하지 않음. |
| 개발-UI | `developer`(역할: UI) | 디자인 시스템·공통 컴포넌트 구현. |
| 개발-FE | `developer`(역할: FE) | 화면/라우팅/상태관리/API 연동 구현. |
| 개발-BE | `developer`(역할: BE) | 백엔드 API 구현(도메인 로직, 인증·인가, 예외처리 등). |
| 개발-DB | `developer`(역할: DB) | 데이터베이스 스키마·초기 데이터 구현. |
| 테스트 | `tester` | 기능 명세·요구사항 기준으로 도메인별 통합 테스트 시나리오 작성·수행, 결과를 `dev-lead`에게 전달. |

> 개발 구현 4종(UI/FE/BE/DB)은 모두 `developer` 정의를 재사용하며, 소집 시 역할 지시로 담당 범위를 한정한다. 렌더링 방식(CSR/SSR)에 따라 실제 소집 인원 구성이 달라질 수 있다.

## 3. Custom Command

| 명령어 | 용도 |
|--------|------|
| `/team-development <구축 서비스명> <참조파일 위치>` | 신규 서비스 구축 착수. Main이 전체 에이전트를 한 번에 Teammate로 소집한 뒤 `analyzer`에게만 최초 킥오프를 전달하고, 이후 에이전트 간 소통에 개입하지 않는다. |
| `/resume-task` | 토큰 소진 등으로 세션이 끊겨 작업이 중단됐을 때 사용. 디스크에 남은 산출물(문서·소스코드·git 이력)로 중단 지점을 파악한 뒤, 전체 에이전트를 다시 Teammate로 소집해 이어서 진행한다. |

두 명령어 모두 Main이 개입하는 경우를 다음 두 가지로 제한한다: ① 최초 킥오프(또는 재개 시 현재 단계 담당자에게 상황 전달), ② 에이전트 간 소통으로도 결론이 나지 않는 모호하거나 결정이 필요한 사안의 에스컬레이션.

## 4. 토큰 절약 정책

- **디렉토리별 `CLAUDE.md`**: 개발 에이전트는 작업 중 새로 생성하는 모든 디렉토리에 그 디렉토리의 파일·하위 디렉토리를 설명하는 `CLAUDE.md`를 함께 생성한다(결과적으로 모든 디렉토리에 `CLAUDE.md`가 존재해야 한다). 기존 디렉토리에서 작업할 때는 모든 `CLAUDE.md`를 한꺼번에 읽지 않고, root부터 하위 디렉토리로 한 단계씩 내려가며 작업 목적에 맞는 디렉토리를 찾아 그 `CLAUDE.md`만 읽고 참조한다. 이를 통해 컨벤션·구조 파악에 드는 컨텍스트 사용을 최소화한다.
- **`/compact`**: 모든 에이전트는 컨텍스트 사용량이 80%에 도달하면 `/compact`를 수행한다. 도메인 전환 시에도 삭제·재소집 대신 개발/테스트 에이전트에게 `/compact` 수행을 지시해 컨텍스트를 정리한다.
- **`/clear`**: `/compact` 수행 후에도 컨텍스트 사용량이 50% 이상이면 `/clear`를 수행해 컨텍스트를 완전히 초기화한다. 정리(compact/clear) 이후에는 이전 작업에 대한 기억이 요약되었거나 사라질 수 있으므로, 컨벤션·직전 진행 상태는 대화 기억이 아니라 디스크(소스코드·산출물, 각 디렉토리의 `CLAUDE.md`)를 직접 읽어 파악한다.
