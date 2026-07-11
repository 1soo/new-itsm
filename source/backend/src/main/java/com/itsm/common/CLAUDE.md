# CLAUDE.md

전 도메인이 공유하는 공통 모듈. 설정·공통 엔티티·예외 처리·보안·티켓 공통 요소.

## 하위 디렉토리
- `config/` — 전역 설정(Security, OpenAPI, JPA Auditing)
- `entity/` — 공통 엔티티 기반 클래스(BaseEntity)
- `exception/` — 공통 예외 처리 모듈(ErrorCode, GlobalExceptionHandler 등)
- `security/` — JWT 인증·인가 컴포넌트
- `ticket/` — 티켓 공통 요소(댓글·타임라인·승인·연계)
- `notification/` — 알림 확인처리(common 도메인 최초 API, auth와 동일한 4계층 구조로 신설)
