# 기술스택 정의서 — ITSM 플랫폼

> 프로젝트: new-itsm (티켓 기반 ITSM 웹 플랫폼) · 버전: 0.1 · 작성일: 2026-07-09

## 1. 개요

`docs/source/`의 ITSM 지식 문서를 근거로, **티켓 기반 ITSM 웹 플랫폼(Jira Service Management 유형)**을 신규 구축한다. 서비스 요청·인시던트·문제·변경 관리, 지식베이스, 자산/CMDB, 인증·권한(RBAC)을 제공하는 실사용 플랫폼을 목표로 한다.

## 2. 확정 기술스택

| 구분 | 선택 | 비고 |
|------|------|------|
| **렌더링 방식** | CSR (Client-Side Rendering) | SPA 형태 |
| **Frontend** | React (CSR) | 전역 상태 Redux Toolkit, 공통 apiClient 사용 |
| **Backend** | Spring Boot | DDD·SOLID, Spring Security 기반 인증·인가, springdoc(OpenAPI) 문서화 |
| **Database** | PostgreSQL | snake_case, 정규화, 공통 컬럼 규칙 |
| **컨테이너** | Docker 사용 | docker-compose 기반 local DB/서비스 컨테이너 |
| **배포 환경** | local | CSP(AWS/Azure) 인프라 아키텍처 설계는 이번 범위 제외 |

## 3. 팀 구성 (CSR 5인)

| 역할 | 담당 |
|------|------|
| dev-lead | 개발 계획·조율, 도메인 단위 커밋 |
| developer (UI) | 디자인 시스템·공통 컴포넌트 (`source/frontend/` 공통 영역) |
| developer (FE) | 화면/라우팅/상태/apiClient (`source/frontend/` 기능 영역) |
| developer (BE) | Spring Boot API (`source/backend/`) |
| developer (DB) | PostgreSQL DDL/DML (`source/db/`) |

## 4. 소스 저장 구조

```
source/
├── frontend/   # React (CSR)
├── backend/    # Spring Boot
└── db/         # PostgreSQL (sql/ 하위에 DDL/DML)
```

## 5. 구축 범위 (코어 7개 도메인)

| # | 도메인 | slug | 약어 |
|---|--------|------|------|
| 1 | 인증/계정/권한 (Auth & RBAC) | `auth` | AUTH |
| 2 | 서비스 요청 관리 | `service-request` | SRM |
| 3 | 인시던트 관리 | `incident` | INC |
| 4 | 문제 관리 | `problem` | PRB |
| 5 | 변경 관리 | `change` | CHG |
| 6 | 지식 관리 | `knowledge` | KM |
| 7 | IT 자산 관리 / CMDB | `asset` | ITAM |

> 확장(ESM·ITOps/ITOM·컴플라이언스)은 이번 범위에서 제외한다.
> `itil.md`, `it-management.md`의 상위 개념은 별도 도메인이 아니라 각 도메인의 프로세스·역할·지표에 녹여 반영한다.
