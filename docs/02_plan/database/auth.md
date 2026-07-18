# 테이블 정의서 — 인증/계정/권한 (Auth & RBAC)

> 도메인: auth · 버전: 0.3

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-13 | Role-Menu 동적 매핑 반영 — `screen`에 사이드바 표시 컬럼 추가, `screen_role`을 메뉴 노출 판정에 재사용(신규 테이블 없음); 사이드바 메뉴 i18n용 `screen_name_en`/`group_label_en` 컬럼 추가 |

계정·역할·RBAC·화면 매핑·세션(Refresh Token)·감사 로그를 정의한다. RBAC/화면 매핑 테이블(`screen`, `user_role`, `screen_role`)의 **단일 원천(single source of truth)**이며, 타 도메인 정의서는 이를 참조한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| user ↔ role (다대다) | 3NF | 한 사용자가 복수 역할을 가지므로 `user_role` 매핑 테이블로 분리(반복 컬럼 제거). |
| role ↔ screen (다대다) | 3NF | 역할별 접근 화면이 다대다이므로 `screen_role`로 분리. |
| audit_log | 1NF·정규화 최소화 | 로그는 조회·추적 목적의 append-only 성격이라 조인 최소화를 위해 주체/대상 식별 문자열을 함께 비정규 보관(추적성 우선). |

## 2. 공통 컬럼 규칙

모든 테이블에 포함한다. (snake_case, PostgreSQL 기준 `BIGINT GENERATED ALWAYS AS IDENTITY` PK)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, IDENTITY | sequence PK |
| created_by | VARCHAR(100) | NOT NULL | 등록자 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 등록일 |
| updated_by | VARCHAR(100) | NULL | 수정자 |
| updated_at | TIMESTAMPTZ | NULL | 수정일 |
| is_deleted | BOOLEAN | NOT NULL, DEFAULT false | soft delete 여부 |

> 이하 각 테이블 상세에서는 공통 컬럼을 생략하고 도메인 컬럼만 기술한다. (append-only 성격의 `audit_log`, `refresh_token`은 `updated_*`/`is_deleted` 미사용)

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| app_user | 사용자 계정 | REQ-AUTH-001/002/007 |
| role | 역할 정의 | REQ-AUTH-006 |
| user_role | 사용자-역할 매핑 | REQ-AUTH-005/006 |
| screen | 화면 정보 | REQ-AUTH-005 |
| screen_role | 역할-화면 매핑 | REQ-AUTH-005 |
| refresh_token | Refresh Token 세션(JTI) | REQ-AUTH-002/003/004 |
| audit_log | 감사 로그 | REQ-AUTH-008 |

## 4. 테이블 상세

### app_user

사용자 계정. `user`는 PostgreSQL 예약어라 `app_user`로 명명. 비밀번호는 단방향 해시만 저장.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 로그인 ID |
| password_hash | VARCHAR(255) | NOT NULL | 단방향 해시(BCrypt) |
| name | VARCHAR(100) | NOT NULL | 이름 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE / INACTIVE |
| access_token_jti | UUID | NULL | 현재 세션 Access Token JTI(로그아웃 시 NULL) — [security/authentication.md](../security/authentication.md) |
| department | VARCHAR(20) | NULL | 소속 부서(HR/LEGAL/FACILITIES/FINANCE/IT). ESM 부서 요청·체크리스트 하위 작업의 담당 부서 판정에 사용([esm.md](esm.md)). IT 외 도메인과 무관한 사용자는 NULL. |
| ...공통 컬럼... | | | |

### role

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| role_code | VARCHAR(50) | UNIQUE, NOT NULL | 역할 코드(SYSTEM_ADMIN 등) |
| role_name | VARCHAR(100) | NOT NULL | 표시명 |
| description | VARCHAR(255) | NULL | 설명 |
| ...공통 컬럼... | | | |

### user_role (사용자-역할 매핑)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 사용자 |
| role_id | BIGINT | FK → role.id, NOT NULL | 역할 |
| | | UNIQUE(user_id, role_id) | 중복 부여 방지 |
| ...공통 컬럼... | | | |

### refresh_token (세션·JTI)

Refresh Token 세션 관리. 로그아웃/재발급 무효화 판정에 사용. [security/authentication.md](../security/authentication.md) 참조.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| jti | UUID | UNIQUE, NOT NULL | 토큰 고유 식별자(claim jti) |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 소유자 |
| expires_at | TIMESTAMPTZ | NOT NULL | 만료 시각 |
| revoked | BOOLEAN | NOT NULL, DEFAULT false | 무효화 여부(로그아웃 시 true) |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |

### audit_log

인증·인가·계정/역할 변경 이벤트 append-only 기록. 컴플라이언스 도메인([compliance.md](compliance.md))도 감사 추적을 위해 이 테이블을 공유한다(REQ-COMP-004).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| event_type | VARCHAR(30) | NOT NULL | LOGIN/LOGOUT/REFRESH/USER_CHANGE/ROLE_CHANGE/COMPLIANCE_REQ_CREATE/COMPLIANCE_REQ_UPDATE/COMPLIANCE_ACTION_STATUS_CHANGE |
| actor_id | BIGINT | FK → app_user.id, NULL | 행위 주체(로그인 실패 시 NULL 가능) |
| actor_email | VARCHAR(255) | NULL | 주체 식별(추적용 비정규 보관) |
| target | VARCHAR(255) | NULL | 대상(계정/역할 식별) |
| result | VARCHAR(10) | NOT NULL | SUCCESS / FAILURE |
| occurred_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 발생 시각 |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |

## 5. RBAC · 화면 관리 테이블

정석 RBAC 2단 매핑. `user_role`(위 4절) + `screen_role`.

### screen

화면 정보. `screen_code`는 화면 설계서의 SCR-* 코드와 1:1 매핑. **사이드바 메뉴 마스터 데이터도 겸한다**(Role-Menu 동적 매핑) — 별도 `menu` 테이블을 신설하지 않고 이 테이블에 사이드바 표시용 컬럼을 추가해 SCR-ADMIN-006(메뉴 관리)에서 관리자가 CRUD한다. `nav_visible=false`인 화면(상세/서브 화면 등 사이드바 미노출 대상)은 화면 접근 제어(screen_role) 판정에는 그대로 참여하되 사이드바에는 나타나지 않는다.

> **이중언어 컬럼**: 사이드바 메뉴명·그룹명은 관리자가 SCR-ADMIN-006(메뉴 관리)에서 동적으로 CRUD하는 데이터라 FE 번역 키 방식(6절 i18n 아키텍처, `screen/common.md` 6.4절, 정적 UI 문구 전용)을 적용할 수 없으므로, `screen_name_en`/`group_label_en` 이중언어 컬럼으로 다국어를 지원한다(신규 테이블 없음). 관리자가 영문값도 함께 입력하고, `GET /api/v1/menus/mine`(API-AUTH-022) 응답에 두 언어 값을 모두 포함해 FE가 현재 `i18n.language`에 따라 선택한다(`AppLayout.tsx`).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| screen_code | VARCHAR(50) | UNIQUE, NOT NULL | 화면 식별 코드(예: SCR-INC-001) |
| screen_name | VARCHAR(100) | NOT NULL | 화면명(= 메뉴명, 한국어) |
| screen_name_en | VARCHAR(100) | NOT NULL | 화면명(= 메뉴명, 영어). 마이그레이션 시 기존 데이터는 `screen_name` 번역값으로 백필 |
| path | VARCHAR(255) | UNIQUE, NOT NULL | 라우팅 경로(하나의 경로는 하나의 화면만 가리킴 — 중복 시 사이드바 라우팅 충돌) |
| domain | VARCHAR(30) | NOT NULL | 소속 도메인(auth/incident 등) |
| icon_name | VARCHAR(50) | NULL | 사이드바 아이콘(lucide-react 컴포넌트명, 예: `LayoutDashboard`). `nav_visible=false`면 미사용 |
| group_code | VARCHAR(30) | NULL | 사이드바 그룹 키(예: srm/inc/admin). NULL은 그룹 라벨 없이 표시(대시보드 등 최상단 그룹) |
| group_label | VARCHAR(50) | NULL | 사이드바 그룹 표시명(예: "서비스 요청", 한국어). group_code가 NULL이면 함께 NULL |
| group_label_en | VARCHAR(50) | NULL | 사이드바 그룹 표시명(영어). group_code가 NULL이면 함께 NULL, 그 외에는 NOT NULL로 관리(마이그레이션 시 백필) |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서(오름차순). 그룹 표시 순서는 별도 컬럼 없이 그룹별 최소 sort_order 값으로 정렬 |
| nav_visible | BOOLEAN | NOT NULL, DEFAULT true | 사이드바 노출 여부(false면 화면 접근 통제만 하고 메뉴에는 미노출) |
| ...공통 컬럼... | | | |

### screen_role (역할-화면 매핑, 역할-메뉴 매핑 겸용)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| screen_id | BIGINT | FK → screen.id, NOT NULL | 화면(메뉴) |
| role_id | BIGINT | FK → role.id, NOT NULL | 역할 |
| | | UNIQUE(screen_id, role_id) | 중복 방지 |
| ...공통 컬럼... | | | |

> 역할 코드·화면별 접근 매핑의 초기 값은 [security/authorization/](../security/authorization/)의 역할 정의서를 seed 근거로 사용하며, SCR-ADMIN-006(메뉴 관리)에서 관리자가 이 매핑을 동적으로 변경할 수 있다. 화면 접근 제어(403 판정)와 사이드바 메뉴 노출이 항상 동일한 `screen_role` 데이터를 기준으로 판정되므로 두 기능 간 불일치가 발생하지 않는다(단일 원천 유지). 메뉴 삭제는 soft delete(`screen.is_deleted=true`)로 처리하며, 연결된 `screen_role` 매핑 행은 별도로 정리하지 않는다(모든 조회가 `screen.is_deleted=false` 조건을 거치므로 정합성에 영향 없음).

## 6. 관계 · 제약조건 요약

- user_role.user_id → app_user.id (FK), user_role.role_id → role.id (FK), UNIQUE(user_id, role_id)
- screen_role.screen_id → screen.id (FK), screen_role.role_id → role.id (FK), UNIQUE(screen_id, role_id)
- refresh_token.user_id → app_user.id (FK), UNIQUE(jti)
- audit_log.actor_id → app_user.id (FK, nullable)
- app_user.email UNIQUE, role.role_code UNIQUE, screen.screen_code UNIQUE, screen.path UNIQUE
