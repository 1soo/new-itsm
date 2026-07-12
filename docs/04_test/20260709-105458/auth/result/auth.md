# 통합 테스트 결과 — auth (20260709-105458)

> 대상: auth 도메인 · 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(docker itsm-postgres)
> 초기 계정: admin@itsm.local / Admin@1234 (SYSTEM_ADMIN)

## 요약

- 총 47건 · 성공 45 · 실패 2
- 실패: **TC-AUDIT-003**(로그인 실패 감사로그 미기록), **TC-E2E-008**(FE 401→refresh 재시도 시 refresh 500)
- 두 실패 모두 **백엔드 결함**이며, 특히 TC-E2E-008은 액세스 토큰 만료(5분) 시 FE 무음 재발급이 항상 실패하여 강제 재로그인되는 사용자 영향이 큼.

## 상세

### A. 빌드 테스트
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `gradlew test` BUILD SUCCESSFUL, 단위테스트 45건 전부 통과(6클래스, 0 failures) | `clean`/전체 jar 재조립은 **실행 중 backend가 jar 파일 잠금**으로 인해서만 실패(컴파일/테스트 결함 아님) |
| TC-BUILD-002 | PASS | `npm run build`(tsc+vite) 성공, dist 산출 | - |

### B. 로그인 (API-AUTH-001)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-LOGIN-001 | PASS | 200, accessToken/refreshToken/expiresIn=300/user.roles=[SYSTEM_ADMIN], Set-Cookie refreshToken(HttpOnly) | - |
| TC-LOGIN-002 | PASS | 401 INVALID_CREDENTIALS | - |
| TC-LOGIN-003 | PASS | 401 INVALID_CREDENTIALS(미존재도 동일 코드, 계정 열거 방지) | - |
| TC-LOGIN-004 | PASS | 400 VALIDATION_ERROR | - |
| TC-LOGIN-005 | PASS | 비활성 계정 로그인 403 ACCOUNT_INACTIVE | - |

### C. 토큰 재발급 (API-AUTH-002)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-REFRESH-001 | PASS | Body/Cookie(JSON) 재발급 200, 새 accessToken | JSON content-type 기준 |
| TC-REFRESH-002 | PASS | 401 INVALID_REFRESH_TOKEN | - |
| TC-REFRESH-003 | PASS | 재발급 후 이전 accessToken 401(jti 회전), 새 accessToken 200 | access_token_jti 갱신 확인 |

### D. 로그아웃/세션 무효화 (API-AUTH-003)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-LOGOUT-001 | PASS | 200 {message:"로그아웃 완료"}, refreshToken 쿠키 Max-Age=0 | - |
| TC-LOGOUT-002 | PASS | 무효화된 RT 재발급 401 | refresh_token.revoked=true 확인 |
| TC-LOGOUT-003 | PASS | 로그아웃 후 accessToken /me 401(강제 로그아웃) | access_token_jti NULL |
| TC-LOGOUT-004 | PASS | 미인증 로그아웃 401 | - |

### E. 내 정보 / 비밀번호 (API-AUTH-004/005)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-ME-001 | PASS | 200 {id,email,name,status=ACTIVE,roles} | - |
| TC-ME-002 | PASS | 401 UNAUTHENTICATED | - |
| TC-PW-001 | PASS | 200 변경, 새 비번 로그인 200 / 구 비번 401 | - |
| TC-PW-002 | PASS | 401 PASSWORD_MISMATCH | - |
| TC-PW-003 | PASS | 400 PASSWORD_POLICY_VIOLATION | 8자+영문+숫자 |
| TC-PW-004 | PASS | app_user.password_hash 전부 BCrypt($2a/$2b,60자), 평문 0건 | DB 검증 |

### F. 계정 관리 (API-AUTH-006~010)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-USER-001 | PASS | 200 {content,page,size,totalElements} | - |
| TC-USER-002 | PASS | 201, 응답에 비밀번호/해시 미포함 | - |
| TC-USER-003 | PASS | 409 EMAIL_DUPLICATE | - |
| TC-USER-004 | PASS | 400 VALIDATION_ERROR | - |
| TC-USER-005 | PASS | 상세 200 / 미존재 404 USER_NOT_FOUND | - |
| TC-USER-006 | PASS | 200, name 반영(ASCII·한글 모두) | 한글은 UTF-8 전송 시 정상 |
| TC-USER-007 | PASS | 상태 INACTIVE 200, 이후 로그인 403 ACCOUNT_INACTIVE | - |

### G. 역할 관리 (API-AUTH-011~014)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-ROLE-001 | PASS | 200, 시드 역할 11종, userCount 포함 | - |
| TC-ROLE-002 | PASS | 200, roles에 부여 역할 추가(복수 역할) | 즉시 반영 |
| TC-ROLE-003 | PASS | 400 ROLE_NOT_FOUND | - |
| TC-ROLE-004 | PASS | 200, 역할 회수 반영 | - |
| TC-ROLE-005 | PASS | 201 생성 / 중복 409 ROLE_NAME_DUPLICATE(숫자 포함 코드도 201) | - |

### H. RBAC 인가 (REQ-AUTH-005)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-RBAC-001 | PASS | END_USER의 admin API 접근 403 ACCESS_DENIED | - |
| TC-RBAC-002 | PASS | 미인증 admin API 401 | - |
| TC-RBAC-003 | PASS | 위조/서명오류 토큰 401 | - |

### I. 감사 로깅 (API-AUTH-015)
| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-AUDIT-001 | PASS | 200 페이지 응답 | - |
| TC-AUDIT-002 | PASS | LOGIN/LOGOUT/REFRESH/USER_CHANGE/ROLE_CHANGE 모두 기록됨 | actor/target/occurredAt 포함 |
| TC-AUDIT-003 | **FAIL** | 로그인 실패(FAILURE) 이벤트가 **DB에 전혀 기록되지 않음**(SUCCESS 93건, FAILURE 0건) | 결함 #2 참조 |

### J. FE E2E (playwright, 매 항목 새 context)
| TC ID | 결과 | 실제 동작 | 증적 |
|-------|------|-----------|------|
| TC-E2E-001 | PASS | 관리자 로그인 → /admin/users(계정 관리) 이동 | shots/e2e-001-login-home.png |
| TC-E2E-002 | PASS | 로그인 실패 시 "이메일 또는 비밀번호가 올바르지 않습니다." 통일 표기, /login 유지 | shots/e2e-002-login-fail.png |
| TC-E2E-003 | PASS | 미인증 /admin/users 접근 → /login 리다이렉트 | shots/e2e-003-guard.png |
| TC-E2E-004 | PASS | 계정 생성 후 목록에 신규 계정 노출 | shots/e2e-004-created.png |
| TC-E2E-005 | PASS | 상세에서 역할 부여·비활성화 화면 반영 | shots/e2e-005-detail.png |
| TC-E2E-006 | PASS | 역할 관리 화면 역할 12행(시드11+테스트생성) 표시 | shots/e2e-006-roles.png |
| TC-E2E-007 | PASS | 감사 로그 목록 표시 | shots/e2e-007-audit.png |
| TC-E2E-008 | **FAIL** | accessToken 만료 시 apiClient가 `POST /auth/refresh`(form-urlencoded, 빈 body) 호출 → **500** → 세션 만료 처리되어 /login으로 강제 이동 | shots/e2e-008-refresh.png · 결함 #1 |
| TC-E2E-009 | PASS | 헤더 로그아웃(확인 다이얼로그) → /login, 이후 보호 라우트 재차 /login | shots/e2e-009-logout.png |

## 실패 항목 분석

### 결함 #1 (CRITICAL) — TC-E2E-008 : refresh 엔드포인트 500 (FE 무음 재발급 전면 실패)
- **증상**: `POST /api/v1/auth/refresh` 요청이 `Content-Type: application/x-www-form-urlencoded` + 빈 body일 때 **500 INTERNAL_ERROR** 반환.
- **재현(결정적)**:
  ```
  curl -b <refreshCookie> -X POST /api/v1/auth/refresh -H "Content-Type: application/x-www-form-urlencoded"
  → 500 {"code":"INTERNAL_ERROR",...}
  # 동일 요청을 Content-Type: application/json(또는 미지정)으로 보내면 200
  ```
- **근본 원인**: FE 공통 `apiClient.doRefresh`가 bare `axios.post(url, null, ...)`로 호출 → axios가 null body에 대해 기본 Content-Type을 `application/x-www-form-urlencoded`로 전송. BE `AuthController.refresh(@RequestBody(required=false) RefreshRequest ...)`는 form-urlencoded 요청을 JSON 컨버터로 바인딩하지 못해 예외 발생, GlobalExceptionHandler에 매핑되지 않아 500으로 떨어짐(415가 아니라 500).
- **영향**: authentication.md §4/§5 및 apiClient 설계의 "401 → refresh 1회 재시도"(REQ-AUTH-003, SCR-COM-005)가 **실제 FE 경로에서 항상 실패**. 액세스 토큰 5분 만료 시 사용자는 무음 재발급 없이 매번 강제 재로그인됨.
- **권장 수정(택1로 해소)**:
  - BE: refresh를 content-type 무관하게 빈/비-JSON body를 허용(쿠키 우선 검증)하거나, 미디어타입 예외를 표준 오류로 매핑.
  - FE: `doRefresh`에서 `Content-Type: application/json` 명시(예: `axios.post(url, {}, {headers:{'Content-Type':'application/json'}, withCredentials:true})`).

### 결함 #2 (MEDIUM) — TC-AUDIT-003 : 실패(FAILURE) 감사 로그 미기록
- **증상**: 로그인 실패 등 FAILURE 이벤트가 audit_log에 전혀 저장되지 않음(SUCCESS 93 / FAILURE 0).
- **근본 원인**: `AuditLogService.record()`가 기본 전파(`@Transactional` REQUIRED)라 호출자(`AuthService.login()`)의 트랜잭션에 합류. 로그인 실패 시 FAILURE 기록 직후 `BusinessException`을 던져 **트랜잭션이 롤백되며 감사 insert도 함께 소멸**.
- **영향**: REQ-AUTH-008 / FEAT-AUTH-008(인증 이벤트 감사 기록 의무). 실패 로그인은 보안상 핵심 감사 대상이며 api_spec도 `result: SUCCESS|FAILURE`를 명시.
- **권장 수정**: `AuditLogService.record()`에 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 적용하여 원 트랜잭션 롤백과 무관하게 감사 로그가 독립 커밋되도록.

## 참고(테스트 환경 특이사항, 결함 아님)
- 백엔드 CORS 허용 오리진은 `http://localhost:5173`만 등록되어 있어, dev server를 `127.0.0.1:5173`으로 띄우면 "Invalid CORS request"(403)가 발생. E2E는 `localhost:5173`으로 수행하여 정상. (운영/설정 관점 참고)
- Windows 셸에서 curl `-d`로 한글을 인라인 전송하면 인코딩 깨짐으로 400이 날 수 있어, 한글 body는 UTF-8 파일(`--data-binary @file`)로 검증(엔드포인트 자체는 정상).
</content>
