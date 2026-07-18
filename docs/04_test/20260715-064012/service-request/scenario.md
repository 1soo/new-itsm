# 통합 테스트 시나리오 — service-request (런타임 업그레이드 후 기능 회귀)

## 사전 조건
- auth 도메인 로그인 성공(TC-AUTH-101) 전제.
- 신규 화면/API 변경 없음, 기존처럼 동작하는지 확인 목적.

## 시나리오

### TC-SRM-101 · 서비스 포털 카탈로그 조회 및 요청 제출(승인 불요 유형)
- 근거: @docs/01_analyze/feature/service-request.md (SCR-SRM-001/002)
- 전제: 로그인 상태, 카탈로그 항목(노트북 신청/비밀번호 초기화) 시드됨
- 절차:
  1. `/portal` 접근, 카탈로그 2건 노출 확인
  2. "비밀번호 초기화"(승인 불요) 선택 → 동적 양식(대상 계정 ID) 입력 → 제출
- 기대 결과: 요청이 생성되어 상세 화면(`/service-requests/{id}`)으로 이동, 콘솔 에러 없음

### TC-SRM-102 · 요청 큐(상담원) 조회
- 근거: @docs/01_analyze/feature/service-request.md (SCR-SRM-004)
- 전제: TC-SRM-101 완료
- 절차:
  1. `/service-requests/queue` 접근
- 기대 결과: 큐 화면 정상 렌더링, 콘솔 에러 없음

### TC-SRM-103 · 승인 대기함(공용 승인 엔진) 조회
- 근거: @docs/06_maintenance/20260712-014911/srm/history.md (공용 승인 엔진 이관)
- 전제: 로그인 상태
- 절차:
  1. `/approvals` 접근
- 기대 결과: 승인 대기함 화면 정상 렌더링("현재 대기 중인 승인이 없습니다" — approval_process 미구성 상태이므로 정상), 콘솔 에러 없음
