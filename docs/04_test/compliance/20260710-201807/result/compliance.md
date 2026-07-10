# 통합 테스트 결과 — 컴플라이언스 관리 (Compliance) (20260710-201807)

## 요약
- 총 15건 · 성공 15 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-COMP-000 | PASS | frontend `npm run build`(vite) 성공, backend `./gradlew build`(JUnit 포함) BUILD SUCCESSFUL | |
| TC-COMP-001 | PASS | 이름/근거 미입력 시 클라이언트 검증 메시지("이름과 근거는 필수입니다.") 노출 + API 400(VALIDATION_ERROR) 확인 | |
| TC-COMP-002 | PASS | 등록 성공(COMP-2026-0007 등 식별키 발급) 후 상세(SCR-COMP-003)로 자동 이동, 등록 직후 감사로그에 COMPLIANCE_REQ_CREATE 즉시 반영 | |
| TC-COMP-003 | PASS | 준수상태(전체/준수/미준수)·책임자 지정여부(전체/지정됨/미지정) 필터 및 조합 필터 정상 동작, 미준수(Danger)·책임자 미지정 배지 정상 표시 | |
| TC-COMP-004 | PASS | 존재하지 않는 사용자 ID(999999)로 책임자 지정 시 API 400 + UI 토스트("유효하지 않은 사용자입니다.") | |
| TC-COMP-005 | PASS | 유효한 사용자(id=66)로 책임자 지정 성공, "현재 책임자" 갱신 | |
| TC-COMP-006 | PASS | 시정조치 등록(DETECTED 생성) 직후 요구사항 준수상태 COMPLIANT→NON_COMPLIANT 전환(목록·상세 일관) | |
| TC-COMP-007 | PASS | DETECTED→RESOLVED 직접 전이 시도 API 400(INVALID_STATUS_TRANSITION). UI는 항상 다음 단계 버튼만 노출해 순서 위반 자체가 불가하도록 설계(추가 안전장치) | |
| TC-COMP-008 | PASS | DETECTED→IN_PROGRESS→RESOLVED 순차 전이 성공, 미해결 시정조치 0건 되어 준수상태 NON_COMPLIANT→COMPLIANT 복귀 | |
| TC-COMP-009 | PASS | 상세 "수정" 버튼으로 이름/근거/적용범위 인라인 수정 반영, 재조회 시 유지 | |
| TC-COMP-010 | PASS | 존재하지 않는 변경 ID(999999) 연계 시도 API 400 + UI 토스트("존재하지 않는 변경 요청입니다.") | |
| TC-COMP-011 | PASS | 유효한 변경(CHG-2026-0003) 연계 성공, 요구사항 상세에 연결된 변경 표시. cm@itsm.local로 해당 변경 상세 조회 시 "연계 항목"에 `COMPLIANCE_REQUIREMENT · COMP-2026-0007`로 역노출 확인 | |
| TC-COMP-012 | PASS | 감사 로그에 COMPLIANCE_REQ_CREATE·COMPLIANCE_REQ_UPDATE·COMPLIANCE_ACTION_STATUS_CHANGE(x2) 전부 시간순 기록·조회됨 | |
| TC-COMP-013 | PASS | 정상 조회 시 준수율 86%(6/7)·미해결 시정조치 1건, 요구사항별 상태 표 실제 데이터와 일치. 미래 기간(데이터 없음) 조회 시 KPI 0%/0건으로 정상 표시(400 아님) | 요구사항별 상태 표는 FE 설계상 목록 API(전체) 기반이라 기간 필터 영향을 받지 않음(`features/compliance/CLAUDE.md`에 명시된 의도된 동작, 결함 아님) |
| TC-COMP-014 | PASS | cm@itsm.local(CHANGE_MANAGER) 사이드바에 컴플라이언스 메뉴 미노출, `/compliance/requirements` 직접 접근 시 `/403`로 리다이렉트, API(목록·메트릭) 호출 403(ACCESS_DENIED) | |

## 실패 항목 분석
- 없음.

## 참고(결함 아닌 관찰 사항)
- DB 시드(`21_compliance_seed.sql`)의 `screen.path` 값(`/compliance`, `/compliance/new`, `/compliance/:id`, `/compliance/metrics`)이 실제 FE 라우트(`/compliance/requirements`, `/compliance/requirements/new`, `/compliance/requirements/:id`, `/compliance/metrics`)와 불일치. 다만 RBAC은 프론트 `RequireRoles`(역할 기반) + 백엔드 API 역할 검증으로 이뤄지고 `screen.path` 값을 참조하는 코드는 확인되지 않아 기능상 결함은 아님 — 문서 정합성 차원에서만 참고.
