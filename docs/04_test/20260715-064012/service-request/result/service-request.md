# 통합 테스트 결과 — service-request (20260715-064012)

## 요약
- 총 3건 · 성공 3 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-101 | PASS | `/portal`에 "노트북 신청"(하드웨어)/"비밀번호 초기화"(계정) 2건 정상 노출. "비밀번호 초기화" 선택 후 동적 양식(대상 계정 ID) 렌더링, `admin@itsm.local` 입력 후 제출 → `/service-requests/1`로 정상 이동 | snapshot 확인 |
| TC-SRM-102 | PASS | `/service-requests/queue` 정상 렌더링, 신규 콘솔 에러 없음 | snapshot 확인 |
| TC-SRM-103 | PASS | `/approvals` 정상 렌더링, "현재 대기 중인 승인이 없습니다" 표시(approval_process 미구성 상태이므로 정상), 신규 콘솔 에러 없음 | snapshot 확인 |

## 실패 항목 분석
- 없음 (실패 0건)
