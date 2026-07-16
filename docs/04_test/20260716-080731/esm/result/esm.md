# 통합 테스트 결과 — esm (20260716-080731)

## 요약
- 총 6건 · 성공 6 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ESM-001 | PASS | service-request TC-SRM-001과 동일 빌드 결과 공유(BUILD SUCCESSFUL) | - |
| TC-ESM-002 | PASS | user@itsm.local로 법무 부서 요청(ESM-2026-0004) 제출 후 legal-coord@itsm.local로 처리. IN_PROGRESS "처리 시작", COMPLETED "완료 처리" 버튼 텍스트가 표와 정확히 일치. REJECTED "반려 처리"도 별도 요청(ESM-2026-0005)에서 확인, 표와 일치 | playwright snapshot |
| TC-ESM-003 | PASS | ESM-2026-0005 반려 처리 시 타임라인 메시지 "상태가 반려로 변경되었습니다" — 버튼 라벨("반려 처리")과 다르게 기존 도착 상태명(`requestStatusLabel`) 유지 확인 | playwright snapshot |
| TC-ESM-004 | PASS | ESM-2026-0004 타임라인에 각 상태 변경 항목마다 actor "법무 처리 담당자" 표시, 메시지가 상태 코드가 아닌 한글 라벨("처리중", "완료")로 표시됨 | playwright snapshot |
| TC-ESM-005 | PASS | hr@itsm.local로 HR-1 케이스 접수 후 전체 흐름 수행. DOCUMENTATION "기록 시작", INVESTIGATION "조사 시작", RESOLUTION "해결 처리" — 표와 정확히 일치 | playwright snapshot |
| TC-ESM-006 | PASS | HR-1 상태 이력 4건(접수/기록/조사/해결) 모두 기존과 동일하게 `changedBy` 필드(이메일 `hr@itsm.local`)로 행위 주체자 표시, 신규 `actor` 필드 추가 없음(HrCaseDetailPage.tsx 변경 없음 확인) | playwright snapshot |

## 실패 항목 분석
- 없음
