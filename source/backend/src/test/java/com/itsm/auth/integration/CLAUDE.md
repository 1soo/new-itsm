# CLAUDE.md

auth 도메인 통합 테스트(Testcontainers PostgreSQL).

## 파일
- `AuthAdminIntegrationTest.java` — 인증·계정·역할·감사 로그 API end-to-end 통합 테스트
- `ApprovalProcessAdminIntegrationTest.java` — 승인 프로세스 관리자 CRUD(API-AUTH-027/028) 통합 테스트. steps·requesterRoleIds 교체(delete 후 재삽입) 시 Hibernate flush 순서로 인한 UNIQUE 제약 위반 회귀 방지(flush() 강제 호출 검증)
