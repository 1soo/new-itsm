# CLAUDE.md

도메인별 스키마 DDL과 시드 데이터. 컨테이너 최초 기동 시 파일명 알파벳(번호) 순으로 실행된다. 단일 원천은 `docs/02_plan/database/*.md`이며 공통 컬럼 규칙(id, created_by/at, updated_by/at, is_deleted)을 따른다.

## 파일
- `01_schema.sql` — auth 도메인 스키마: `role`, `app_user`, `user_role`, `screen`, `screen_role`(RBAC), `refresh_token`, `audit_log`(뒤 둘은 append-only).
- `02_seed.sql` — auth 시드: 역할 11종, SYSTEM_ADMIN 초기 계정(admin@itsm.local), auth/admin/common/error 화면(SCR-*)과 역할-화면 매핑.
- `03_common_schema.sql` — 티켓 공용 다형 참조 테이블: `approval`(승인), `comment`(코멘트), `timeline_event`(타임라인). ticket_type/ticket_id 다형 참조라 FK 대신 앱에서 검증.
- `04_srm_schema.sql` — service-request 스키마: `queue`, `service_catalog_item`, `catalog_form_field`, `service_request`, `service_request_form_value`(EAV), `csat`(만족도).
- `05_srm_seed.sql` — service-request 시드: 기본 큐 2종, 예시 카탈로그 항목·동적 양식 필드, 화면(SCR-SRM-001~008)과 역할-화면 매핑 증분.
- `06_incident_schema.sql` — incident 스키마 + 공용 `ticket_link`(다형 링크) 도입: `incident`, `incident_responder`, `incident_severity_history`, `postmortem`, `postmortem_five_why`, `postmortem_action_item`.
- `07_incident_seed.sql` — incident 시드: 화면(SCR-INC-001~005)·역할-화면 매핑 증분, 테스트 계정(im@/agent@itsm.local, INCIDENT_MANAGER/SERVICE_DESK_AGENT). 트랜잭션 데이터는 seed 안 함.
- `08_problem_schema.sql` — problem 스키마: `problem`(문제 티켓), `problem_five_why`(RCA), `known_error`(KEDB), `problem_action`(후속 조치).
- `09_problem_seed.sql` — problem 시드: 화면(SCR-PRB-001~004)·역할-화면 매핑 증분, 테스트 계정(pm@itsm.local, PROBLEM_MANAGER). 트랜잭션 데이터는 seed 안 함.
- `10_change_schema.sql` — change 스키마: `change_template`(표준 변경 사전승인 템플릿), `change_request`(RFC, 6단계 상태·승인경로·구현결과), `change_affected_system`(영향 시스템). 승인·연계는 `approval`/`ticket_link` 재사용.
- `11_change_seed.sql` — change 시드: 표준 변경 템플릿 1건, 화면(SCR-CHG-001~006)·역할-화면 매핑 증분(CHANGE_MANAGER/APPROVER, 둘 다 auth 단계 기존 역할 재사용), 테스트 계정(cm@/cab@itsm.local). 트랜잭션 데이터는 seed 안 함.
- `12_knowledge_schema.sql` — knowledge 스키마: `knowledge_category`/`knowledge_label`(분류), `knowledge_article`(기사, DRAFT/IN_REVIEW/PUBLISHED, 유용성·조회 집계), `article_label`(매핑), `knowledge_feedback`(유용성 평가), `knowledge_review`(검토 이력), `search_log`(무결과 검색 로그, append-only). KCS 연계는 `ticket_link` 재사용.
- `13_knowledge_seed.sql` — knowledge 시드: 카테고리 3건(네트워크/계정·권한/결제), 화면(SCR-KM-001~005)·역할-화면 매핑 증분(KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER, 둘 다 auth 단계 기존 역할 재사용), 테스트 계정(kc@/kg@itsm.local). 트랜잭션 데이터는 seed 안 함.
