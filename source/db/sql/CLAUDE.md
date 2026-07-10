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
- `14_asset_schema.sql` — asset 스키마(7/7 마지막 도메인): `asset`(asset_key AST-####, 생애주기 5단계, 만료일 3종), `asset_attribute`(EAV), `asset_lifecycle_history`(append-only), `expiry_alert`, `configuration_item`(CI), `ci_relation`(CI 자기참조 의존관계, 자기참조 금지 CHECK). 티켓 연계는 `ticket_link`(source_type='ASSET'/'CI') 재사용.
- `15_asset_seed.sql` — asset 시드: 화면(SCR-ITAM-001~005)·역할-화면 매핑 증분(ASSET_MANAGER, auth 단계 기존 역할 재사용), 테스트 계정(am@itsm.local). 트랜잭션 데이터(asset 등)는 seed 안 함.
- `16_esm_schema.sql` — esm(엔터프라이즈 서비스 관리, 확장 도메인 1/4) 스키마: auth 증분 `app_user.department` 컬럼 추가(NULL 허용, HR/LEGAL/FACILITIES/FINANCE/IT) + 신규 테이블 8개(`esm_catalog_item`, `esm_catalog_form_field`(EAV), `esm_checklist_template_task`, `esm_checklist`, `esm_checklist_task`(자산 회수는 `asset.id` FK, nullable), `esm_request`, `esm_request_form_value`(EAV), `esm_hr_case`(민감정보)). 코멘트/타임라인은 `common`(03) 재사용.
- `17_esm_seed.sql` — esm 시드: 신규 역할 2종(`HR_CASE_MANAGER`, `DEPT_COORDINATOR`) + 해당 역할의 공통 기본 접근(02_seed CROSS JOIN 시점에 없던 역할이라 별도 증분), 화면(SCR-ESM-001~011)·역할-화면 매핑, 테스트 계정(hr@/legal-coord@/facilities-coord@/it-coord@itsm.local — 서로 다른 부서로 department 접근 제어 테스트용, it-coord는 오프보딩 자산회수 하위작업 완료·체크리스트 전체 자동완료 시나리오 검증용), 부서별(HR/LEGAL/FACILITIES/FINANCE) 카탈로그 항목 5건(온보딩·오프보딩 각 1건 + 체크리스트 템플릿 하위 작업 각 2건, 서로 다른 부서 배정).
- `18_vulnerability_schema.sql` — vulnerability(취약점 관리, 확장 도메인 2/4) 스키마: common 증분 `ticket_link` CHECK(source_type/target_type)에 `VULNERABILITY` 추가 + 신규 테이블 3개(`vulnerability`(ticket_key VULN-YYYY-####, severity×exploitability→risk_score 캐시(1~12, BE 산식 가중치 4/3/2/1 곱의 실제 상한에 맞춤), 6단계 상태), `vulnerability_remediation`(개선 조치 이력), `vulnerability_verification`(검증 결과 이력, 재검증 반복)). 코멘트/타임라인은 `common`(03) 재사용(ticket_type에 CHECK 없어 증분 불필요).
- `19_vulnerability_seed.sql` — vulnerability 시드: 신규 역할(`VULNERABILITY_MANAGER`, 단일) + 공통 기본 접근 증분(02_seed CROSS JOIN 시점에 없던 역할), 화면(SCR-VULN-001~004)·역할-화면 매핑, 테스트 계정(vm@itsm.local), 심각도별(CRITICAL/HIGH/MEDIUM/LOW) 예시 취약점 4건(그중 CRITICAL 1건은 자산이 존재하면 `ticket_link`로 연계, 자산이 없으면 스킵).
