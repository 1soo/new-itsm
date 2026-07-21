# CLAUDE.md

승인 엔진 리포지토리 인터페이스(영속성 기술 비의존, 구현은 infrastructure에 위임).

## 파일
- `ApprovalProcessRepository.java` — 승인 프로세스 정의 저장·조회(게이트 매칭용 도메인별+전체도메인(domain null) 조회, 관리자 목록·우선순위(tier 0/11/14/23/25/37, 2026-07-22 targetState 축 추가로 43/55 신규) 중복 검증 포함, 2026-07-15 3축 재설계)
- `ApprovalProcessRequesterRoleRepository.java` — 규칙의 승인요청자 역할 스코프 저장·조회
- `ApprovalProcessStepRepository.java` — 규칙의 승인자 차수 저장·조회
- `ApprovalProcessStepRoleRepository.java` — 차수별 승인 역할 저장·조회
- `ApprovalRequestRepository.java` — 승인 인스턴스 헤더 저장·조회(티켓별 targetState 무관 최신 인스턴스(재승인요청용)/특정 targetState 최신 인스턴스(게이트 재확인용, 2026-07-22 신규), 대기함 후보 조회, 도메인 목록 API의 pendingApprovalTargetState 배치 조회(2026-07-22 신규, N+1 방지))
- `ApprovalRequestStepRepository.java` — 인스턴스 차수 스냅샷 저장·조회
- `ApprovalRequestStepRoleRepository.java` — 인스턴스 차수별 필요 역할 스냅샷 저장·조회
- `ApprovalDecisionRepository.java` — 역할별 결정 기록 저장·조회(append-only)
