# CLAUDE.md

엔터프라이즈 서비스 관리(esm, Enterprise Service Management) 도메인. 부서별(HR/법무/시설/재무) 서비스 카탈로그·동적 양식·부서 요청 처리, HR 케이스 관리(민감정보, HR_CASE_MANAGER 전용), 온보딩/오프보딩 체크리스트(부서 간 하위 작업 오케스트레이션·자산 회수 자동연계)·ESM 지표 담당. IT 부서 요청은 기존 SRM 사용, ESM은 다루지 않음. srm 패키지 컨벤션(카탈로그+동적양식+EAV) 재사용. 코멘트/타임라인은 `common.ticket`(ticket_type=ESM_REQUEST/HR_CASE) 재사용. DDD 4계층.

## 하위 디렉토리
- `application/` — 유스케이스 서비스와 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
