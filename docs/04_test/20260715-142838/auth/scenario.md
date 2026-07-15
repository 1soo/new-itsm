# 통합 테스트 시나리오 — auth (역할 목록 조회 공개 API, 유지보수 요청 2026-07-15)

## 사전 조건
- 빌드 테스트 통과(TC-SRM-001과 공용)
- 임의 인증 계정 1개(예: po@itsm.local, 비밀번호 `Admin@1234`)

## 시나리오

### TC-AUTH-001 · API-AUTH-030 인증된 임의 역할 200
- 근거: @docs/02_plan/api_spec/auth.md API-AUTH-030
- 절차: po@itsm.local(비관리자, PROCESS_OWNER) 토큰으로 `GET /api/v1/roles` 호출
- 기대 결과: 200, `[{id, roleCode, name}]` 배열 반환. `userCount` 등 관리자 전용 필드 미포함

### TC-AUTH-002 · API-AUTH-030 미인증 401
- 근거: 위와 동일
- 절차: Authorization 헤더 없이 `GET /api/v1/roles` 호출
- 기대 결과: 401

### TC-AUTH-003 · 비관리자 화면(카탈로그 관리) 연동 확인
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007 담당자 역할 select — service-request 결과(TC-SRM-002) 참조
- 기대 결과: SRM 결과에서 이미 검증(중복 수행 없음)
