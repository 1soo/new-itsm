# API 명세서 — 통합 검색 (Search)

> 도메인: search · 버전: 0.1 · 작성일: 2026-07-10

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 0. 설계 배경 및 결정 사항

헤더 통합 검색을 지식(KM) 단일 도메인에서 지식+티켓(SRM/INC/PRB/CHG) 교차 도메인 검색으로 확장한다. 기존 도메인별 목록 API 중 SRM/PRB/CHG는 `keyword` 파라미터가 없어 조합만으로는 구현 불가하여 **신규 통합 엔드포인트**를 신설한다(기존 4개 도메인 목록 API는 변경하지 않음).

- **API 형태**: 도메인별 `keyword` 파라미터 개별 추가(5개 API 병렬 호출 후 클라이언트 병합) 대신, **신규 엔드포인트 1개**(`GET /api/v1/search`)로 서버가 도메인들을 조회·병합·정렬 후 **단일 페이지네이션**으로 반환한다. 클라이언트 병합 방식은 "다음 페이지" 개념이 부정확해지는 문제가 있어 채택하지 않는다. 기존 목록 API는 그대로 유지되므로 백엔드 변경 범위·회귀 위험이 작다.
- **정렬**: 관련도 스코어링(풀텍스트 검색 엔진 없이는 구현 부담이 큼)은 MVP 범위에서 제외하고, **`updatedAt` 내림차순 단일 정렬**을 사용한다.
- **페이지네이션**: 서버가 도메인별로 매칭 건을 조회한 뒤 병합·정렬한 **전체 결과 집합 기준**으로 `page`/`size`를 적용한다(도메인별로 각각 페이징하지 않음). 데이터 규모상 초기 구현은 도메인별로 충분한 상한(예: 각 도메인 최대 100건)을 조회해 메모리에서 병합·정렬·슬라이스하는 방식으로 가능하며, 구체적 조회 방식(DB 쿼리 조합/인메모리 병합)은 BE 재량으로 한다.
- **미리보기 vs 전체 결과**: 헤더 검색창은 입력 중 동일 API를 작은 `size`(예: 5~8)로 호출해 드롭다운 미리보기를 표시한다. Enter 또는 "전체 결과 보기" 클릭 시 신규 화면(SCR-COM-011, `docs/02_plan/screen/common.md` 참고)으로 이동해 동일 API를 일반 페이지네이션(`size` 기본 20)으로 재호출한다. 미리보기와 전체 결과 화면은 **동일한 API-SEARCH-001을 공유**하며 별도 엔드포인트를 만들지 않는다.
- **RBAC**: 아래 1.1절 참고.

### 0.1 RBAC 필터링 기준

사이드바 메뉴 노출 기준(도메인 접근 가능 여부)과 **각 도메인 기존 목록 조회 API가 이미 적용 중인 행 단위 스코프 규칙**을 그대로 재사용한다(신규 규칙을 만들지 않음). 역할별 화면 접근 정의(`docs/02_plan/security/authorization/*.md`)가 단일 원천이다.

- **도메인 접근 가능 여부(1차 필터)**: 역할의 "접근 가능 API" 목록에 해당 도메인 API가 없으면 그 도메인은 검색 대상에서 제외한다(오류 아님, 조용히 스킵). 예: END_USER는 INC/PRB/CHG API 접근 권한이 없으므로(`docs/02_plan/security/authorization/end_user.md` 참고) 검색 결과에 인시던트/문제/변경이 전혀 노출되지 않는다.
- **행 단위 스코프(2차 필터)**: 접근 가능한 도메인 내에서는 해당 도메인 기존 목록 API와 동일한 조건을 적용한다.
  - KNOWLEDGE: `API-KM-001`과 동일 — END_USER 등 게시(PUBLISHED) 기사 열람만 가능한 역할은 게시된 기사만 검색된다.
  - SERVICE_REQUEST: `API-SRM-007`과 동일 — END_USER는 본인 요청만(`scope=mine` 상당), Agent 이상은 전체(`scope=all` 상당)가 검색된다.
  - INCIDENT/PROBLEM/CHANGE: 해당 도메인 API 접근 권한이 있는 역할(Service Desk Agent, Incident/Problem/Change Manager 등)은 각 도메인 목록 API와 동일하게 전체 대상에서 키워드 매칭한다(현재 이 세 도메인 목록 API에는 개인 소유 범위 제한이 없으므로 별도 행 필터 없음).

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-SEARCH-001 | 통합 검색(지식+티켓 교차 도메인) | GET | /api/v1/search | 필요 |

## 2. API 상세

### API-SEARCH-001 · 통합 검색

- **Endpoint**: `GET /api/v1/search?keyword=&page=&size=`
- **인증**: 필요(Access Token) — 0.1절 RBAC 기준으로 도메인·행 단위 필터링 후 결과 반환
- **Request Body**: 없음
- **Response Body** (200):
  ```json
  {
    "content": [
      {
        "domain": "KNOWLEDGE|SERVICE_REQUEST|INCIDENT|PROBLEM|CHANGE",
        "key": "string · 예: KB-5678, SRM-2026-0012, INC-2026-0034, PRB-2026-0007, CHG-2026-0021",
        "title": "string",
        "status": "string · 도메인별 상태 원문 값(공통 StatusBadge 렌더용)",
        "snippet": "string|null · 키워드 매칭 본문/설명 발췌(최대 100자), 특정 불가 시 null",
        "updatedAt": "ISO-8601",
        "url": "string · FE 라우팅 경로(해당 상세 화면)"
      }
    ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200(매칭 없으면 빈 목록) / 401
