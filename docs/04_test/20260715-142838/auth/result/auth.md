# 통합 테스트 결과 — auth (역할 목록 조회 공개 API) (20260715-142838)

## 요약
- 총 3건 · 성공 3 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-AUTH-001 | PASS | po@itsm.local(PROCESS_OWNER, 비관리자) 토큰으로 `GET /api/v1/roles` → 200, 16개 역할 배열(`id`/`roleCode`/`name`만, `userCount` 등 관리자 전용 필드 없음) | curl |
| TC-AUTH-002 | PASS | Authorization 헤더 없이 `GET /api/v1/roles` → 401 | curl |
| TC-AUTH-003 | PASS | service-request 결과(TC-SRM-002/003) 참조 — 카탈로그 관리 화면 담당자 역할 select가 이 API로 정상 연동됨 확인 | service-request/result/service-request.md |
