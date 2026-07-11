# CLAUDE.md

전 도메인이 공유하는 공통 모듈. 설정·공통 엔티티·예외 처리·보안·티켓 공통 요소.

## 하위 디렉토리
- `config/` — 전역 설정(Security, OpenAPI, JPA Auditing)
- `entity/` — 공통 엔티티 기반 클래스(BaseEntity)
- `exception/` — 공통 예외 처리 모듈(ErrorCode, GlobalExceptionHandler 등)
- `security/` — JWT 인증·인가 컴포넌트
- `ticket/` — 티켓 공통 요소(댓글·타임라인·연계). 승인은 `approval/`로 완전 이전됨(2026-07-11)
- `notification/` — 알림 확인처리(common 도메인 최초 API, auth와 동일한 4계층 구조로 신설)
- `approval/` — 승인 프로세스 커스텀 기능(전 도메인 공용 승인 엔진). 게이트 체크·대기함·상세·결정(API-COM-003~005)
