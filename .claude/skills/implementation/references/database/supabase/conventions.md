# Supabase 개발 컨벤션

`docs/01_analyze/tech.md`에서 Database/BaaS로 **Supabase**가 지정된 경우에만 적용한다.

## 0. 선택 원칙

- 위임 가능 기능: **인증(Auth) / DB(Postgres) / File Storage / Realtime / 자동 생성 API(REST·GraphQL)**.
- **5개 기능을 항상 모두 켜지 않는다.** `docs/02_plan`(API 명세서·화면 설계서·테이블 정의서)에 실제로 필요한 기능만 선택적으로 활성화한다. (예: 파일 업로드 요구사항이 없으면 Storage 미사용, 실시간 갱신 요구사항이 없으면 Realtime 미사용)
- 도메인별로 어떤 기능을 사용할지는 `dev-lead`가 개발 계획에 명시해 개발-DB에게 전달한다.

## 1. 인증 (Auth)

- Supabase Auth(GoTrue) 사용 시 비밀번호 해시는 Supabase가 내부 처리한다. 위 Database 개발 컨벤션의 "비밀번호 단방향 해시" 규칙은 별도로 구현하지 않는다.
- 커스텀 사용자 속성(닉네임, 권한 등)은 도메인 테이블(예: `public.users`)에 저장하고 `auth.users.id`를 FK로 참조한다.
- RBAC 매핑 테이블(`user_role`, `screen_role`, `screen`)은 system-design 산출물 그대로 유지하되, `user_role.user_id`가 `auth.users.id`를 참조하도록 연결한다.
- `docs/02_plan/security/authentication.md`가 Supabase Auth를 전제로 작성된 경우 자체 JWT 발급/JTI 세션 관리 대신 Supabase Auth의 세션·토큰 관리를 따른다. 문서가 자체 JWT 발급을 전제로 한다면 `dev-lead`에게 확인 후 진행한다.

## 2. Database (Postgres)

- 테이블 정의서(`docs/02_plan/database/{domain}.md`) 규칙(snake_case, 공통 컬럼, 제약조건)을 그대로 적용한다.
- 접근 제어는 **RLS(Row Level Security)** 정책으로 구현한다. 역할별 접근 범위는 `docs/02_plan/security/authorization/*`를 참고한다.
- 개인정보 양방향 암호화는 기존 정책(대칭키 암복호화 계층)을 그대로 적용한다. Supabase가 자동으로 암호화해주지 않는다.
- 마이그레이션은 Supabase CLI(`supabase migration new {name}`)로 생성한 SQL 파일을 사용한다.
- 저장 위치: `source/db/supabase/migrations/`(마이그레이션 SQL), `source/db/supabase/config.toml`(프로젝트 설정).
- Local 개발은 Supabase CLI(`supabase start`)가 실행하는 자체 Docker 스택을 사용한다. 위 Database 개발 컨벤션의 `source/db/docker/docker-compose.yml` 수동 구성은 필요 없다.

## 3. File Storage

- 요구사항에 **파일 업로드/다운로드 기능이 있는 도메인에 한해** 사용한다.
- 버킷은 도메인 단위로 분리한다(예: `avatars`, `attachments`). public/private 여부와 접근 정책(RLS)을 테이블 정의서 또는 API 명세서에 명시한다.
- 이미지 리사이즈/변환이 필요하면 Storage의 이미지 변환 옵션을 사용한다(별도 서버 처리 구현 금지).

## 4. Realtime

- 요구사항에 **실시간 갱신(알림, 채팅, 라이브 대시보드 등)이 있는 테이블에 한해** publication을 활성화한다.
- 활성화 대상 테이블·이벤트(INSERT/UPDATE/DELETE)는 API 명세서 또는 화면 설계서에 명시한다.
- FE는 `postgres_changes` 구독으로 처리하고, 폴링 방식과 병행하지 않는다.

## 5. 자동 생성 API (REST/GraphQL)

- 단순 CRUD로 커버되는 endpoint는 **PostgREST 기반 자동 생성 REST API**(또는 GraphQL)를 사용한다. 이 경우 API 명세서에는 "Supabase 자동생성 API 사용"으로 명시하고 별도 커스텀 endpoint를 설계·구현하지 않는다.
- 복잡한 비즈니스 로직(여러 테이블 조합, 외부 연동 등)은 API 명세서에 커스텀 endpoint로 정의하고 필요한 만큼만 별도 구현한다(Backend 서버 또는 Next Route Handler).
- Supabase Edge Functions는 현재 위임 범위에 포함하지 않는다.

## 6. FE/BE 통합 시 참고

- FE(React/Next) 또는 BE(Spring Boot/Next 서버 역할)에서 Supabase 클라이언트(supabase-js 등)로 위 기능을 호출할 때도 본 문서를 참고한다. 자신의 담당 역할에서 이 문서를 `Read`로 직접 참조한다.
- 환경변수: `SUPABASE_URL`, `SUPABASE_ANON_KEY`(클라이언트), `SUPABASE_SERVICE_ROLE_KEY`(서버 전용, 클라이언트 번들에 노출 금지).

## 7. 검증

- **Supabase MCP**가 구성되어 있으면 이를 통해 스키마·RLS·Storage 정책을 검증한다.
- 없으면 test code로 RLS 정책·제약조건·Storage 접근 제어를 검증한다.
