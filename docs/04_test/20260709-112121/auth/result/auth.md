---
date: 20260709-112121
domain: auth
result: partial
keywords: [토큰 재발급 수정확인, 감사 로그 수정확인, 계정 생성 회귀, 트랜잭션 전파]
---

# 통합 테스트 결과(재테스트) — auth (20260709-112121)

> 대상: auth 도메인 재테스트 · 수정본 빌드(BE PID 8168, 재기동) 기준
> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(docker itsm-postgres)
> 초기 계정: admin@itsm.local / Admin@1234 (SYSTEM_ADMIN)

## 요약

- 총 47건 · 성공 45 · 실패 2
- **직전 결함 2건 모두 수정 확인(PASS)**: #1 refresh 500 → 해결, #2 FAILURE 감사로그 미기록 → 해결.
- **신규 회귀 1건(BLOCKER)**: `POST /api/v1/admin/users`(계정 생성)가 **500** 반환. 동일 원인으로 실패 2건: **TC-USER-002**, **TC-E2E-004**.
- 회귀는 이번 수정 빌드에서 발생(직전 사이클에서는 계정 생성 201 정상). 유일한 계정생성 경로 변경점은 AuditLogService.record()의 REQUIRES_NEW 전환이며, 성공-생성 경로(신규 app_user INSERT 직후 감사 기록)에서만 500이 발생.

## 수정 확인 (직전 결함 재검증)

| 항목 | 직전 | 이번 | 실제 동작 |
|------|------|------|-----------|
| 결함 #1 refresh 500 | FAIL | **PASS** | `POST /auth/refresh` — 쿠키+form-urlencoded 빈body → 200(TC-REFRESH-001b), 쿠키 없음+form → 401(TC-REFRESH-002b). FE E2E TC-E2E-008(401→refresh 1회 재시도로 세션 유지) PASS(토큰 재발급·화면 유지 확인) |
| 결함 #2 FAILURE 감사로그 | FAIL | **PASS** | 로그인 실패 유발 후 audit_log FAILURE 13건 기록, `GET /admin/audit-logs?eventType=LOGIN`에서 result=FAILURE 조회됨(TC-AUDIT-003) |

## 상세

### A. 빌드
| TC ID | 결과 | 비고 |
|-------|------|------|
| TC-BUILD-001 | PASS | `gradlew test` BUILD SUCCESSFUL, 45단위테스트 0 failures. **주의: UserAdminServiceTest가 auditLogService를 mock하여 REQUIRES_NEW 실 트랜잭션을 타지 않으므로 계정생성 500 회귀를 단위테스트가 잡지 못함(런타임에서만 발현).** |
| TC-BUILD-002 | PASS | FE는 이번 수정 대상 아님(변경 없음), 직전 빌드 성공 유효 |

### B. 로그인
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-LOGIN-001 | PASS | 200, roles=[SYSTEM_ADMIN], expiresIn=300 |
| TC-LOGIN-002 | PASS | 401 |
| TC-LOGIN-003 | PASS | 401 |
| TC-LOGIN-004 | PASS | 400 |
| TC-LOGIN-005 | PASS | 비활성(id11) 로그인 403 |

### C. 재발급 (결함 #1 집중)
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-REFRESH-001 | PASS | body/JSON 200 |
| TC-REFRESH-001b | PASS | **쿠키+application/x-www-form-urlencoded 빈body 200 (FE 실경로, 직전 500)** |
| TC-REFRESH-001c | PASS | 쿠키+CT미지정 200 |
| TC-REFRESH-002 | PASS | 무효 토큰 401 |
| TC-REFRESH-002b | PASS | 쿠키없음+form 빈body 401(500 아님) |
| TC-REFRESH-003 | PASS | 재발급 후 구 accessToken 401(jti회전), 신 accessToken 200 |

### D. 로그아웃
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-LOGOUT-001 | PASS | 200 |
| TC-LOGOUT-002 | PASS | 무효화 RT 재발급 401 |
| TC-LOGOUT-003 | PASS | 로그아웃 후 accessToken /me 401 |
| TC-LOGOUT-004 | PASS | 미인증 로그아웃 401 |

### E. 내 정보 / 비밀번호
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ME-001 | PASS | 200 |
| TC-ME-002 | PASS | 401 |
| TC-PW-001 | PASS | 변경 200, 신 비번 200 / 구 비번 401 |
| TC-PW-002 | PASS | 401 PASSWORD_MISMATCH |
| TC-PW-003 | PASS | 400 PASSWORD_POLICY_VIOLATION |
| TC-PW-004 | PASS | app_user.password_hash BCrypt 13건 / 비-BCrypt 0건 |

### F. 계정 관리
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-USER-001 | PASS | 목록 200 |
| TC-USER-002 | **FAIL** | 계정 생성(유효 요청) **500 INTERNAL_ERROR** — 회귀(아래 분석) |
| TC-USER-003 | PASS | 이메일 중복 409(중복검사에서 조기 반환되어 생성경로 미진입) |
| TC-USER-004 | PASS | 검증 400(바인딩 단계) |
| TC-USER-005 | PASS | 상세 200 / 미존재 404 |
| TC-USER-006 | PASS | 이름 수정 200(ASCII·한글) |
| TC-USER-007 | PASS | 상태 INACTIVE 200 후 로그인 403 |

### G. 역할 관리
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-ROLE-001 | PASS | 역할 목록 200(roleCode 포함) |
| TC-ROLE-002 | PASS | 역할 부여 200(복수 역할) |
| TC-ROLE-003 | PASS | 존재하지 않는 역할 400 ROLE_NOT_FOUND |
| TC-ROLE-004 | PASS | 역할 회수 200 |
| TC-ROLE-005 | PASS | 역할 생성 201 / 중복 409 |

### H. RBAC
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-RBAC-001 | PASS | END_USER admin API 403 |
| TC-RBAC-002 | PASS | 미인증 401 |
| TC-RBAC-003 | PASS | 위조 토큰 401 |

### I. 감사 로깅 (결함 #2 집중)
| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-AUDIT-001 | PASS | 목록 200 |
| TC-AUDIT-002 | PASS | LOGIN103/LOGOUT4/REFRESH16/USER_CHANGE24/ROLE_CHANGE11 모두 기록 |
| TC-AUDIT-003 | **PASS** | **FAILURE 13건 기록·조회됨(직전 0건에서 해결)** |

### J. FE E2E (playwright, 매 항목 새 context, Chrome)
| TC ID | 결과 | 실제 | 증적 |
|-------|------|------|------|
| TC-E2E-001 | PASS | 로그인→/admin/users | shots/e2e-001-login-home.png |
| TC-E2E-002 | PASS | 실패 통일 문구·/login 유지 | shots/e2e-002-login-fail.png |
| TC-E2E-003 | PASS | 미인증 보호라우트→/login | shots/e2e-003-guard.png |
| TC-E2E-004 | **FAIL** | 계정 생성 저장 시 500으로 /admin/users/new 잔류(목록 미반영) — 회귀 | shots/e2e-004-created.png |
| TC-E2E-005 | PASS | 기존 계정 상세 역할부여·비활성화 반영(재수행 확인) | shots/e2e-005-detail.png |
| TC-E2E-006 | PASS | 역할 관리 목록 13행 | shots/e2e-006-roles.png |
| TC-E2E-007 | PASS | 감사 로그 목록 | shots/e2e-007-audit.png |
| TC-E2E-008 | **PASS** | **accessToken 오염 후 401→쿠키 refresh 1회 재발급→세션 유지(직전 500 FAIL에서 해결)** | shots/e2e-008-refresh.png |
| TC-E2E-009 | PASS | 로그아웃(확인 다이얼로그)→/login | shots/e2e-009-logout.png |

## 실패 항목 분석

### 회귀 (BLOCKER) — TC-USER-002 / TC-E2E-004 : 계정 생성(POST /admin/users) 500
- **증상**: 유효한 요청(이메일·이름·roleIds·정책충족 initialPassword)으로 계정 생성 시 **500 INTERNAL_ERROR**. 중복(409)·검증(400)은 생성 이전 단계라 정상.
- **범위 격리**: 다른 감사기록 mutation은 전부 정상 — 계정 수정(200)·상태변경(200)·역할 부여/회수(200)·역할 생성(201). **오직 신규 계정 생성만 500.**
- **회귀 판정**: 직전 사이클(수정 전 빌드)에서는 계정 생성 201 정상 → 이번 수정 빌드에서 깨짐. 계정생성 경로에 대한 유일한 변경점은 **AuditLogService.record()의 `REQUIRES_NEW` 전환**(결함 #2 수정). 성공-생성 경로는 "신규 app_user INSERT(IDENTITY) + user_role INSERT → record() REQUIRES_NEW(감사 독립 커밋)" 순으로, 이 조합에서만 예외 발생 추정.
- **단위테스트 사각지대**: UserAdminServiceTest가 auditLogService를 mock하여 REQUIRES_NEW 실제 트랜잭션을 타지 않음 → 45단위테스트는 통과하지만 런타임 회귀를 검출 못함.
- **요청**: dev-backend가 서버 콘솔의 `GlobalExceptionHandler.log.error("Unexpected error", ...)` 스택트레이스로 정확한 예외 확인 필요. (REQUIRES_NEW 감사 트랜잭션과 미커밋 신규 엔티티/영속성 컨텍스트 상호작용 의심.) 감사기록을 별도 트랜잭션에서 안전하게 커밋하도록 수정 후 계정 생성 경로 통합 검증 권장.
- **영향 요구사항**: REQ-AUTH-001 / FEAT-AUTH-001 / API-AUTH-007(계정 생성), SCR-ADMIN-002.

## 비고
- 직전 결함 2건은 이번 빌드에서 해결 확인.
- 테스트 진행 시 동일 admin 계정 재로그인은 access_token_jti 회전으로 이전 토큰을 무효화하므로(설계대로), admin은 1회 로그인 세션을 유지하여 수행.
</content>
