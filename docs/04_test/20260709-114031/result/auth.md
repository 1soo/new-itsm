# 통합 테스트 결과(재테스트 2차) — auth (20260709-114031)

> 대상: auth 도메인 회귀 수정본 재테스트 · BE PID 18496(재기동)
> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(docker itsm-postgres)
> 초기 계정: admin@itsm.local / Admin@1234 (SYSTEM_ADMIN)

## 요약

- 총 47건 · 성공 47 · 실패 0  ✅ **전 항목 통과**
- 계정 생성 500 회귀 **해결**(TC-USER-002 201, TC-E2E-004 PASS).
- 직전 결함 2건(#1 refresh 500, #2 FAILURE 감사로그) **회귀 없이 유지**.
- REQUIRES_NEW→감사 propagation 분리 후 감사경로(logout/refresh/계정·역할 변경) 전부 정상 + 감사 기록 확인.

## 수정 확인

- **회귀 근본원인(dev-backend)**: REQUIRES_NEW 감사 insert가 별도 트랜잭션에서 미커밋 신규 app_user를 actor_id FK로 참조 → FK 위반(audit_log_actor_id_fkey)으로 계정 생성 500.
- **수정**: 감사 propagation 분리 — `record()`(호출자 트랜잭션 합류, 원자적)로 성공/변경 감사(create/update/status/role/logout/refresh) 기록, `recordSeparately()`(REQUIRES_NEW)는 로그인 실패(FAILURE) 감사에만 사용.
- **검증 반영**: Testcontainers 실 DDL(FK 포함) 통합테스트 `AuthAdminIntegrationTest` 추가(단위 mock 사각지대 제거).

## 상세

### A. 빌드
| TC ID | 결과 | 비고 |
|-------|------|------|
| TC-BUILD-001 | PASS | `gradlew test` BUILD SUCCESSFUL. 7클래스 47테스트 0 failures(신규 AuthAdminIntegrationTest 2건 포함 — 실 DB FK 통합 검증) |
| TC-BUILD-002 | PASS | FE 변경 없음, 빌드 성공 유효(직전 dist 산출) |

### B. 로그인
| TC | 결과 | | TC | 결과 |
|----|------|-|----|------|
| TC-LOGIN-001 200 | PASS | | TC-LOGIN-002 401 | PASS |
| TC-LOGIN-003 401 | PASS | | TC-LOGIN-004 400 | PASS |
| TC-LOGIN-005 비활성 403 | PASS | | | |

### C. 재발급
| TC | 결과 | 실제 |
|----|------|------|
| TC-REFRESH-001 JSON 200 | PASS | |
| TC-REFRESH-001b form-urlencoded+cookie 200 | PASS | FE 실경로(#1 회귀 없음) |
| TC-REFRESH-002 무효 401 | PASS | |
| TC-REFRESH-003 jti회전 | PASS | 격리검증: 구 AT 401 / 신 AT 200 |

### D. 로그아웃
TC-LOGOUT-001 200 · TC-LOGOUT-002 revoked-RT 401 · TC-LOGOUT-003 AT-after-logout 401 · TC-LOGOUT-004 no-auth 401 — 전부 PASS

### E. 내 정보 / 비밀번호
TC-ME-001 200 · TC-ME-002 401 · TC-PW-001 변경 200(신 200/구 401) · TC-PW-002 401 · TC-PW-003 400 · TC-PW-004 BCrypt만(비-BCrypt 0) — 전부 PASS

### F. 계정 관리 (회귀 focus)
| TC | 결과 | 실제 |
|----|------|------|
| TC-USER-001 목록 200 | PASS | |
| TC-USER-002 생성 201 | **PASS** | **회귀 해소**, 응답에 비밀번호/해시 미포함, USER_CHANGE 감사 기록 확인 |
| TC-USER-003 중복 409 | PASS | |
| TC-USER-004 검증 400 | PASS | |
| TC-USER-005 상세 200/404 | PASS | |
| TC-USER-006 수정 200 | PASS | 한글 반영 |
| TC-USER-007 상태 INACTIVE→로그인 403 | PASS | |

### G. 역할 관리 (audit 경로 회귀 focus)
TC-ROLE-001 목록 200 · TC-ROLE-002 부여 200 · TC-ROLE-003 없는역할 400 · TC-ROLE-004 회수 200 · TC-ROLE-005 생성 201/중복 409 — 전부 PASS (모두 감사 ROLE_CHANGE 기록·정상)

### H. RBAC
TC-RBAC-001 END_USER→admin 403 · TC-RBAC-002 미인증 401 · TC-RBAC-003 위조 401 — 전부 PASS

### I. 감사 로깅
| TC | 결과 | 실제 |
|----|------|------|
| TC-AUDIT-001 목록 200 | PASS | |
| TC-AUDIT-002 이벤트 기록 | PASS | LOGIN131/LOGOUT7/REFRESH21/USER_CHANGE33/ROLE_CHANGE16, 계정생성 USER_CHANGE(SUCCESS) 기록 확인 |
| TC-AUDIT-003 FAILURE 보존 | PASS | FAILURE 20건 유지(#2 회귀 없음) |

### J. FE E2E (playwright, 매 항목 새 context, Chrome) — 9/9 PASS
| TC | 결과 | 증적 |
|----|------|------|
| TC-E2E-001 로그인→관리자홈 | PASS | shots/e2e-001-login-home.png |
| TC-E2E-002 실패 통일문구 | PASS | shots/e2e-002-login-fail.png |
| TC-E2E-003 인증가드 | PASS | shots/e2e-003-guard.png |
| TC-E2E-004 계정 생성 | **PASS** | shots/e2e-004-created.png (회귀 해소) |
| TC-E2E-005 상세 역할부여·비활성화 | PASS | shots/e2e-005-detail.png |
| TC-E2E-006 역할 관리 | PASS | shots/e2e-006-roles.png |
| TC-E2E-007 감사 로그 | PASS | shots/e2e-007-audit.png |
| TC-E2E-008 401→refresh 1회 재시도 | PASS | shots/e2e-008-refresh.png (#1 회귀 없음) |
| TC-E2E-009 로그아웃 | PASS | shots/e2e-009-logout.png |

## 결론
- auth 도메인 통합 테스트 **전 항목 통과(47/47)**. 이전 결함 2건 + 회귀 1건 모두 해소·확인. 실패/미결 항목 없음.
</content>
