# 통합 테스트 시나리오 — auth (인증/계정/권한)

> 실행 타임스탬프: 20260709-105458 · 도메인: auth

## 사전 조건

- 빌드 테스트 통과(Backend Gradle, Frontend Vite build)
- PostgreSQL 컨테이너(itsm-postgres) 기동·healthy, 시드(02_seed.sql) 적용
- Backend(:8080)·Frontend dev(:5173) 기동
- 초기 계정: `admin@itsm.local` / `Admin@1234` (SYSTEM_ADMIN)
- 격리: playwright는 매 항목 새 context(새 창)·storage 초기화. API는 항목별 신규 토큰 발급.

## 시나리오

### A. 빌드 테스트

#### TC-BUILD-001 · Backend 빌드·단위테스트
- 근거: @docs/01_analyze/feature/auth.md (전 FEAT), spring-boot-development(예외별 JUnit 필수)
- 절차: `./gradlew clean test build` 실행
- 기대 결과: 컴파일·전체 테스트 통과, BUILD SUCCESSFUL, 실행 가능한 jar 생성

#### TC-BUILD-002 · Frontend 빌드
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-001~007 화면), react-development(빌드 테스트)
- 절차: `npm run build` (tsc + vite build)
- 기대 결과: 타입체크·번들 성공, dist 산출

---

### B. BE API — 로그인 (FEAT-AUTH-002 / API-AUTH-001)

#### TC-LOGIN-001 · 정상 로그인·토큰 발급
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-002 정상), @docs/02_plan/api_spec/auth.md (API-AUTH-001 200)
- 절차: `POST /api/v1/auth/login` {admin@itsm.local, Admin@1234}
- 기대 결과: 200, body에 accessToken·refreshToken·tokenType=Bearer·expiresIn=300·user{roles:[SYSTEM_ADMIN]}, Set-Cookie refreshToken(HttpOnly)

#### TC-LOGIN-002 · 비밀번호 불일치 401
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-002 Unwanted: 비밀번호 불일치→401)
- 절차: `POST /login` {admin@itsm.local, wrongpw}
- 기대 결과: 401, code=INVALID_CREDENTIALS

#### TC-LOGIN-003 · 미존재 계정 401
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-002 미존재→로그인 거부)
- 절차: `POST /login` {none@itsm.local, whatever1}
- 기대 결과: 401, code=INVALID_CREDENTIALS (계정 열거 방지 동일 코드)

#### TC-LOGIN-004 · 입력 형식 오류 400
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-001 400)
- 절차: `POST /login` {email:"", password:""}
- 기대 결과: 400, code=VALIDATION_ERROR

#### TC-LOGIN-005 · 비활성 계정 로그인 차단 403
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-002 Unwanted 비활성), @docs/02_plan/api_spec/auth.md (API-AUTH-001 403)
- 절차: 임시계정 생성→INACTIVE 처리→해당 계정으로 로그인
- 기대 결과: 403, code=ACCOUNT_INACTIVE

---

### C. BE API — 토큰 재발급 (FEAT-AUTH-003 / API-AUTH-002)

#### TC-REFRESH-001 · 유효 Refresh Token 재발급
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-003 정상)
- 절차: 로그인 후 refreshToken으로 `POST /auth/refresh`
- 기대 결과: 200, 새 accessToken·tokenType=Bearer·expiresIn

#### TC-REFRESH-002 · 무효 Refresh Token 401
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-003 Unwanted 만료·무효→401)
- 절차: `POST /auth/refresh` {refreshToken:"invalid.token"}
- 기대 결과: 401, code=INVALID_REFRESH_TOKEN

#### TC-REFRESH-003 · 재발급 시 Access Token JTI 갱신(세션 매핑)
- 근거: @docs/02_plan/security/authentication.md (5절 재발급 시 access_token_jti 갱신)
- 절차: 로그인(A토큰)→refresh(B토큰)→A토큰으로 `GET /auth/me`
- 기대 결과: 재발급 후 이전 A accessToken은 jti 불일치로 401, 새 B accessToken은 200

---

### D. BE API — 로그아웃/세션 무효화 (FEAT-AUTH-004 / API-AUTH-003)

#### TC-LOGOUT-001 · 정상 로그아웃
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-004 정상)
- 절차: 로그인→`POST /auth/logout` (Bearer + refreshToken)
- 기대 결과: 200, {message:"로그아웃 완료"}, refreshToken 쿠키 만료(Max-Age=0)

#### TC-LOGOUT-002 · 무효화된 Refresh Token 재발급 거부 401
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-004 Unwanted: 무효화 토큰 재발급→401)
- 절차: 로그인→로그아웃→동일 refreshToken으로 `POST /auth/refresh`
- 기대 결과: 401, code=INVALID_REFRESH_TOKEN

#### TC-LOGOUT-003 · 로그아웃 후 Access Token 무효(강제 로그아웃)
- 근거: @docs/02_plan/security/authentication.md (3절 로그아웃 시 access_token_jti NULL)
- 절차: 로그인→로그아웃→동일 accessToken으로 `GET /auth/me`
- 기대 결과: 401 (jti 불일치)

#### TC-LOGOUT-004 · 미인증 로그아웃 401
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-003 401)
- 절차: Authorization 없이 `POST /auth/logout`
- 기대 결과: 401

---

### E. BE API — 내 정보 (API-AUTH-004) / 비밀번호 변경 (FEAT-AUTH-007 / API-AUTH-005)

#### TC-ME-001 · 내 정보 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-004 200)
- 절차: 로그인→`GET /auth/me`
- 기대 결과: 200, {id,email,name,status=ACTIVE,roles:[SYSTEM_ADMIN]}

#### TC-ME-002 · 미인증 내 정보 401
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-005 미인증→401)
- 절차: Authorization 없이 `GET /auth/me`
- 기대 결과: 401, code=UNAUTHENTICATED

#### TC-PW-001 · 비밀번호 변경 정상
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-007 정상)
- 절차: 임시계정 로그인→`PATCH /auth/me/password` {current, newValid1234}→새 비번 재로그인
- 기대 결과: 200 {message}, 새 비밀번호 로그인 성공, 구 비밀번호 로그인 401

#### TC-PW-002 · 현재 비밀번호 불일치 401
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-007 Unwanted 현재 비번 불일치)
- 절차: 로그인→`PATCH /auth/me/password` {current:wrong, new:Valid1234}
- 기대 결과: 401, code=PASSWORD_MISMATCH

#### TC-PW-003 · 새 비밀번호 정책 위반 400
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-005 400), PasswordPolicy(8자+영문+숫자)
- 절차: 로그인→`PATCH /auth/me/password` {current 정상, new:"short"}
- 기대 결과: 400, code=PASSWORD_POLICY_VIOLATION

#### TC-PW-004 · 비밀번호 단방향 해시 저장 검증(DB)
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-007 Ubiquitous 평문 저장 금지)
- 절차: DB `app_user.password_hash` 조회
- 기대 결과: BCrypt 해시($2*)만 저장, 평문 없음

---

### F. BE API — 계정 관리 (FEAT-AUTH-001 / API-AUTH-006~010)

#### TC-USER-001 · 계정 목록 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-006 200)
- 절차: 관리자 토큰→`GET /admin/users`
- 기대 결과: 200, {content[],page,size,totalElements}

#### TC-USER-002 · 계정 생성 정상 201
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-001 정상), API-AUTH-007 201
- 절차: `POST /admin/users` {email,name,roleIds:[END_USER id],initialPassword:Init@1234}
- 기대 결과: 201, {id,email,name,status=ACTIVE,roles}, 비밀번호/해시 미포함

#### TC-USER-003 · 이메일 중복 409
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-001 IF 중복→409)
- 절차: 기존 이메일로 재생성 요청
- 기대 결과: 409, code=EMAIL_DUPLICATE

#### TC-USER-004 · 필수 누락/형식 오류 400
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-007 400)
- 절차: `POST /admin/users` {email:"bad", name:"", roleIds:[], initialPassword:"x"}
- 기대 결과: 400, code=VALIDATION_ERROR

#### TC-USER-005 · 계정 상세 조회 200 / 없음 404
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-008 200/404)
- 절차: 생성된 userId `GET /admin/users/{id}`, 없는 id `GET /admin/users/999999`
- 기대 결과: 존재 200, 미존재 404(code=USER_NOT_FOUND)

#### TC-USER-006 · 계정 수정(name) 200
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-009 200)
- 절차: `PATCH /admin/users/{id}` {name:"수정됨"}
- 기대 결과: 200, name 반영

#### TC-USER-007 · 계정 상태 변경(INACTIVE) 후 로그인 차단
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-001 비활성화→로그인 차단), API-AUTH-010
- 절차: `PATCH /admin/users/{id}/status` {status:INACTIVE}→해당 계정 로그인
- 기대 결과: 200 {status:INACTIVE}, 이후 로그인 403(ACCOUNT_INACTIVE)

---

### G. BE API — 역할 관리 (FEAT-AUTH-006 / API-AUTH-011~014)

#### TC-ROLE-001 · 역할 목록 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-013 200)
- 절차: `GET /admin/roles`
- 기대 결과: 200, 시드 역할 11종, {id,roleCode,name,description,userCount}

#### TC-ROLE-002 · 사용자 역할 부여(즉시 반영·복수 역할)
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-006 정상·복수 역할)
- 절차: `POST /admin/users/{id}/roles` {roleId: 추가 역할}
- 기대 결과: 200, {userId, roles[]}에 부여 역할 포함(복수)

#### TC-ROLE-003 · 존재하지 않는 역할 부여 400
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-006 IF 미존재 역할→400)
- 절차: `POST /admin/users/{id}/roles` {roleId: 999999}
- 기대 결과: 400, code=ROLE_NOT_FOUND

#### TC-ROLE-004 · 사용자 역할 회수
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-012 200)
- 절차: `DELETE /admin/users/{id}/roles/{roleId}`
- 기대 결과: 200, roles[]에서 제거

#### TC-ROLE-005 · 역할 생성 201 / 중복 409
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-014 201/409)
- 절차: `POST /admin/roles` {roleCode:TEST_ROLE_XXX,...}, 이어 동일 코드 재생성
- 기대 결과: 최초 201, 중복 409(code=ROLE_NAME_DUPLICATE)

---

### H. BE API — RBAC 인가 (FEAT-AUTH-005 / REQ-AUTH-005)

#### TC-RBAC-001 · 비관리자 admin API 접근 403
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-005 Unwanted 권한 부족→403), SecurityConfig(admin=SYSTEM_ADMIN)
- 절차: END_USER 계정 로그인→`GET /admin/users`
- 기대 결과: 403, code=ACCESS_DENIED

#### TC-RBAC-002 · 미인증 admin API 접근 401
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-005 Unwanted 미인증→401)
- 절차: Authorization 없이 `GET /admin/users`
- 기대 결과: 401

#### TC-RBAC-003 · 위조/서명오류 토큰 401
- 근거: @docs/02_plan/security/authentication.md (4절 서명 무효→401)
- 절차: 임의 조작 Bearer 토큰으로 `GET /auth/me`
- 기대 결과: 401

---

### I. BE API — 감사 로깅 (FEAT-AUTH-008 / API-AUTH-015)

#### TC-AUDIT-001 · 감사 로그 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-015 200)
- 절차: `GET /admin/audit-logs`
- 기대 결과: 200, {content[],page,size,totalElements}

#### TC-AUDIT-002 · 로그인/로그아웃/재발급 이벤트 기록
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-008), authentication.md
- 절차: 로그인·재발급·로그아웃 수행 후 audit-logs 조회
- 기대 결과: LOGIN·REFRESH·LOGOUT 이벤트가 actor/target/occurredAt과 함께 기록

#### TC-AUDIT-003 · 로그인 실패 FAILURE 기록
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-008 인증 이벤트)
- 절차: 잘못된 비밀번호 로그인 시도 후 `GET /admin/audit-logs?eventType=LOGIN`
- 기대 결과: result=FAILURE LOGIN 기록 존재

---

### J. FE E2E (playwright, 매 항목 새 context)

#### TC-E2E-001 · 관리자 로그인→역할 기본 홈
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-002), roles.roleHome(SYSTEM_ADMIN→/admin/users)
- 절차: /login 접속→admin 계정 입력→로그인
- 기대 결과: /admin/users로 이동, 계정 목록 화면 표시

#### TC-E2E-002 · 로그인 실패 통일 메시지
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-002), LoginPage(401/403 통일 문구)
- 절차: /login→잘못된 비밀번호 로그인
- 기대 결과: "이메일 또는 비밀번호가 올바르지 않습니다." 표시, /login 유지

#### TC-E2E-003 · 인증 가드(미인증 보호 라우트→로그인)
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-005), AuthGuard(SCR-COM-005)
- 절차: storage 초기화 상태로 /admin/users 직접 접근
- 기대 결과: /login으로 리다이렉트

#### TC-E2E-004 · 관리자 계정 생성 플로우
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-001), SCR-ADMIN-002
- 절차: 로그인→/admin/users/new→폼 입력→생성
- 기대 결과: 생성 성공(토스트/목록 반영), 목록에 신규 계정 표시

#### TC-E2E-005 · 계정 상세·역할 부여·비활성화
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-001/006), SCR-ADMIN-003
- 절차: 목록→상세 진입→역할 부여→상태 INACTIVE 변경
- 기대 결과: 역할·상태 변경이 화면에 반영

#### TC-E2E-006 · 역할 관리 화면
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-006), SCR-ADMIN-004
- 절차: /admin/roles 접속
- 기대 결과: 역할 목록(11종) 표시, 역할 생성 UI 동작

#### TC-E2E-007 · 감사 로그 화면
- 근거: @docs/01_analyze/feature/auth.md (FEAT-AUTH-008), SCR-ADMIN-005
- 절차: /admin/audit-logs 접속
- 기대 결과: 감사 로그 목록 표시(로그인 이벤트 등)

#### TC-E2E-008 · 401 → refresh 1회 재시도(세션 유지)
- 근거: @docs/02_plan/security/authentication.md (4절), apiClient(401 refresh once)
- 절차: 로그인 상태에서 accessToken을 만료/무효로 강제한 뒤 보호 API 호출 유발
- 기대 결과: apiClient가 쿠키 refresh로 1회 재발급 후 요청 성공(로그인 화면으로 튕기지 않음)

#### TC-E2E-009 · 로그아웃
- 근거: @docs/01_analyze/prd/auth.md (REQ-AUTH-004), 헤더 로그아웃
- 절차: 로그인→헤더 로그아웃
- 기대 결과: /login 이동, 이후 보호 라우트 접근 시 재로그인 요구
</content>
