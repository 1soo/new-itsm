# docs/06_maintenance — 유지보수 이력 인덱스

개발 완료 후 발생한 추가 요구사항(유지보수) 이력을 도메인별로 기록한다.

## 구조

- `{yyyyMMdd-HHmmss}/{domain}/report.md`: 각 유지보수 건의 상세 이력 (요구사항 · 해결 방법 · 변경 파일 · 테스트 결과)

## 인덱스

`maintainer` 에이전트는 이력 생성 때마다 아래 표에 한 줄씩 추가한다.
신규 요청 분석 시 **이 표만 먼저 확인**해 유사 이력 여부를 판단하고, 관련 있어 보이는 항목의 `report.md`만 선택 열람한다. (전수 조사 금지 — 토큰 최소화)

| 일시 | 도메인 | 요약 | 경로 |
|------|--------|------|------|
| 20260711-125730 | auth | 역할-메뉴 동적 매핑(screen/screen_role 확장, 메뉴 관리 화면 신규) | docs/06_maintenance/20260711-125730/auth/report.md |
| 20260711-125730 | common | 헤더 알림 확인처리(모두 지우기/개별 X, notification_dismissal 신규) | docs/06_maintenance/20260711-125730/common/report.md |
| 20260711-161421 | common | 헤더 알림 5초 polling 전환(merge 정책·8건 cap 폐지·백그라운드 정지) | docs/06_maintenance/20260711-161421/common/report.md |
| 20260712-014911 | common | 공용 승인 엔진 신규 구축(도메인/요청유형/역할 기반 커스텀 다단계 승인, 6개 하드코딩 승인 로직 대체) | docs/06_maintenance/20260712-014911/common/report.md |
| 20260712-014911 | auth | 승인 프로세스 관리 화면(SCR-ADMIN-007/008) 및 CRUD API(API-AUTH-023~029) 신규 | docs/06_maintenance/20260712-014911/auth/report.md |
| 20260712-014911 | srm | 서비스요청 approverRole 하드코딩 승인을 공용 승인 엔진으로 이관 | docs/06_maintenance/20260712-014911/srm/report.md |
| 20260712-014911 | change | CAB 승인을 공용 승인 엔진으로 이관 | docs/06_maintenance/20260712-014911/change/report.md |
| 20260712-014911 | knowledge | 게시 승인(게이트키퍼)을 공용 승인 엔진으로 이관(승인/반려 시 자동 게시·DRAFT 전환) | docs/06_maintenance/20260712-014911/knowledge/report.md |
| 20260712-014911 | incident | IN_PROGRESS→RESOLVED 전이 및 해결 처리(resolve) 신규 승인 게이트 추가(우회 결함 수정) | docs/06_maintenance/20260712-014911/incident/report.md |
| 20260712-014911 | problem | WORKAROUND→RESOLVED_CLOSED 전이 신규 승인 게이트 추가(강제종료 제외) | docs/06_maintenance/20260712-014911/problem/report.md |
| 20260712-014911 | asset | 생애주기 전이·자산 폐기(RETIREMENT) 신규 승인 게이트 추가 | docs/06_maintenance/20260712-014911/asset/report.md |
| 20260712-014911 | vulnerability | PRIORITIZATION→REMEDIATION 전이 신규 승인 게이트 추가 | docs/06_maintenance/20260712-014911/vulnerability/report.md |
| 20260712-014911 | compliance | 시정조치 상태전이 신규 승인 게이트 추가(TicketType.CORRECTIVE_ACTION으로 항목별 독립 승인) | docs/06_maintenance/20260712-014911/compliance/report.md |
| 20260712-014911 | esm | 부서요청 상태전이 신규 승인 게이트 추가(HR 케이스·체크리스트 제외) | docs/06_maintenance/20260712-014911/esm/report.md |
| 20260712-111803 | auth | Access Token sessionStorage→Client Memory 전환 및 XSRF 더블서밋 쿠키 기반 CSRF 방지 | docs/06_maintenance/20260712-111803/auth/report.md |
| 20260712-180448 | common | i18n(react-i18next 계열) 도입 및 헤더 지구본 아이콘/언어 선택 팝업 신규(SCR-COM-015), toast·ConfirmDialog SweetAlert2 전환(Modal 제외), 가이드(SCR-COM-012) 영어 본문 신규 | docs/06_maintenance/20260712-180448/common/report.md |
| 20260712-180448 | auth | 로그인/프로필/비밀번호변경 및 관리자 8개 화면 다국어(한/영) 텍스트 전환 | docs/06_maintenance/20260712-180448/auth/report.md |
| 20260712-180448 | service-request | SRM 전 화면 다국어 전환, 공용 field-builder/rating/form-schema 미번역 결함 수정 | docs/06_maintenance/20260712-180448/service-request/report.md |
| 20260712-180448 | incident | INC 전 화면 다국어 전환, formatMinutes 미산정/분 단위 번역 키 통일 | docs/06_maintenance/20260712-180448/incident/report.md |
| 20260712-180448 | problem | PRB 전 화면 다국어 전환, origin 미지정 시 원시 번역 키 노출 회귀 수정 | docs/06_maintenance/20260712-180448/problem/report.md |
| 20260712-180448 | change | CHG 전 화면 다국어 전환, 연계 항목 링크 유형(linkTargetLabel) 원시값 노출 수정 | docs/06_maintenance/20260712-180448/change/report.md |
| 20260712-180448 | knowledge | KM 전 화면 다국어 전환 | docs/06_maintenance/20260712-180448/knowledge/report.md |
| 20260712-180448 | asset | ITAM 전 화면 다국어 전환, 티켓/CI관계/생애주기 라벨 원시값 노출 3건 수정 | docs/06_maintenance/20260712-180448/asset/report.md |
| 20260712-180448 | esm | ESM 11개 화면 다국어 전환, 체크리스트 유형 라벨 공용 헬퍼화 | docs/06_maintenance/20260712-180448/esm/report.md |
| 20260712-180448 | vulnerability | VULN 전 화면 다국어 전환 | docs/06_maintenance/20260712-180448/vulnerability/report.md |
| 20260712-180448 | compliance | COMP 전 화면 다국어 전환, 감사 로그 이벤트 코드(auditEventTypeLabel) 원시값 노출 수정 | docs/06_maintenance/20260712-180448/compliance/report.md |
| 20260712-180448 | infra-monitoring | IOM 전 화면 다국어 전환 | docs/06_maintenance/20260712-180448/infra-monitoring/report.md |
| 20260714-102553 | common | 사이드바 폭/폰트 축소(190px/48px, 12px/10px), DataTable 컬럼폭 고정 인프라(colgroup+table-fixed) 도입 및 17개 목록화면 적용, 1920x1080 기준 PAGE_SIZE 재산정(10/20→13/14/16) | docs/06_maintenance/20260714-102553/common/report.md |
| 20260714-151157 | runtime-upgrade | Java 25 + Spring Boot 4.1.0 + PostgreSQL 18 런타임 업그레이드(gradle 9.6.1, springdoc/jjwt/testcontainers 호환버전, Jackson3/RoleHierarchy 등 Framework7 대응) | docs/06_maintenance/20260714-151157/runtime-upgrade/report.md |
| 20260715-031710 | auth | 감사 로그 actor 오기록 수정(관리자→대상자 오기록), 승인 프로세스 범위 우선순위 3축(도메인/요청유형/역할) 독립 재설계·삭제 기능 추가·상세화면 요청유형 select 미렌더링 결함 수정 | docs/06_maintenance/20260715-031710/auth/report.md |
| 20260715-031710 | common | 공용 승인 엔진 도메인 미지정(전체 도메인) 규칙 지원(approval_process.domain nullable화, 매칭·우선순위 로직 확장) | docs/06_maintenance/20260715-031710/common/report.md |
| 20260715-031710 | srm | 서비스 요청 상세 승인 패널 "승인 절차 없음" 오해 문구를 게이트 평가 전/후 상태에 따라 분기 표시하도록 개선 | docs/06_maintenance/20260715-031710/srm/report.md |
| 20260715-061016 | service-request | 카탈로그 요청유형별 담당자(역할기반 배정 후보 필터)·담당 큐 Select 전환 신규, 요청 큐 배정버튼 노출조건(본인배정/ROUTED이후 숨김)·라우팅 버튼 담당자 미배정 시 비활성화(BE 409) 추가 | docs/06_maintenance/20260715-061016/service-request/report.md |
| 20260715-061016 | auth | 공용 역할 목록 조회 API(API-AUTH-030) 신규(카탈로그 담당자 역할 선택 등 타 도메인 재사용) | docs/06_maintenance/20260715-061016/auth/report.md |
| 20260715-061016 | common | 승인 프로세스 승인자(StepRole) 역할 기반 동적 상세조회 권한(`ApprovalGateService.canApproverView`) 신설, SRM/CHANGE 기존 정적 APPROVER 전체조회 폐지·대체, 승인 프로세스 있는 8개 도메인 전체 적용(INCIDENT 상세조회 역할체크 부재 결함 동시 정리, ESM 댓글권한 범위초과 결함 수정) | docs/06_maintenance/20260715-061016/common/report.md |
| 20260716-125723 | srm | 상태전이 버튼 문구 동작동사형 전환(transitionLabel), 타임라인 code→name+actor 표시, 서비스 카탈로그 카테고리 자유텍스트→고정목록 CRUD 신규(SCR-SRM-009), 양식 필드 여러 줄 텍스트(textarea) 추가 | docs/06_maintenance/20260716-125723/srm/report.md |
| 20260716-125723 | incident | 상태전이 버튼 문구 동작동사형 전환, 타임라인 code→name+actor 표시 | docs/06_maintenance/20260716-125723/incident/report.md |
| 20260716-125723 | problem | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/problem/report.md |
| 20260716-125723 | change | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/change/report.md |
| 20260716-125723 | vulnerability | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/vulnerability/report.md |
| 20260716-125723 | asset | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/asset/report.md |
| 20260716-125723 | esm | 상태전이 버튼 문구 동작동사형 전환, 타임라인 code→name+actor 표시(EsmRequest), 양식 필드 여러 줄 텍스트(공용 반영) | docs/06_maintenance/20260716-125723/esm/report.md |
| 20260716-125723 | common | 승인 대기함(SCR-COM-014) 티켓 상세보기 버튼 추가, 양식 필드 유형 textarea 추가(공용 컴포넌트), 타임라인 code→name/actor 공용 유틸(TimelineMessages·AppUserRepository.resolveDisplayName) 신설 | docs/06_maintenance/20260716-125723/common/report.md |
| 20260718-070801 | srm | 서비스 카탈로그 동적 양식을 form.io 스타일 드래그앤드롭 폼 빌더로 전면 전환(EAV→JSONB form_schema/form_values, FormSubmissionValidator 신규, 한글 key 자동생성 제약 known limitation 문서화) | docs/06_maintenance/20260718-070801/srm/report.md |
| 20260718-114544 | srm | 요청 큐(Queue 엔티티·테이블) 완전 폐지, 요청 분류/필터링을 카탈로그 카테고리 실시간 조인으로 일원화(category-counts API 신규, categoryId 파싱 400 회귀 수정) | docs/06_maintenance/20260718-114544/srm/report.md |
