# docs/06_maintenance — 유지보수 이력 인덱스

개발 완료 후 발생한 추가 요구사항(유지보수) 이력을 도메인별로 기록한다.

## 구조

- `{yyyyMMdd-HHmmss}/{domain}/history.md`: 각 유지보수 건의 상세 이력 (요구사항 · 해결 방법 · 변경 파일 · 테스트 결과)

## 인덱스

`maintainer` 에이전트는 이력 생성 때마다 아래 표에 한 줄씩 추가한다.
신규 요청 분석 시 **이 표만 먼저 확인**해 유사 이력 여부를 판단하고, 관련 있어 보이는 항목의 `history.md`만 선택 열람한다. (전수 조사 금지 — 토큰 최소화)

| 일시 | 도메인 | 요약 | 경로 |
|------|--------|------|------|
| 20260711-125730 | auth | 역할-메뉴 동적 매핑(screen/screen_role 확장, 메뉴 관리 화면 신규) | docs/06_maintenance/20260711-125730/auth/history.md |
| 20260711-125730 | common | 헤더 알림 확인처리(모두 지우기/개별 X, notification_dismissal 신규) | docs/06_maintenance/20260711-125730/common/history.md |
| 20260711-161421 | common | 헤더 알림 5초 polling 전환(merge 정책·8건 cap 폐지·백그라운드 정지) | docs/06_maintenance/20260711-161421/common/history.md |
| 20260712-014911 | common | 공용 승인 엔진 신규 구축(도메인/요청유형/역할 기반 커스텀 다단계 승인, 6개 하드코딩 승인 로직 대체) | docs/06_maintenance/20260712-014911/common/history.md |
| 20260712-014911 | auth | 승인 프로세스 관리 화면(SCR-ADMIN-007/008) 및 CRUD API(API-AUTH-023~029) 신규 | docs/06_maintenance/20260712-014911/auth/history.md |
| 20260712-014911 | srm | 서비스요청 approverRole 하드코딩 승인을 공용 승인 엔진으로 이관 | docs/06_maintenance/20260712-014911/srm/history.md |
| 20260712-014911 | change | CAB 승인을 공용 승인 엔진으로 이관 | docs/06_maintenance/20260712-014911/change/history.md |
| 20260712-014911 | knowledge | 게시 승인(게이트키퍼)을 공용 승인 엔진으로 이관(승인/반려 시 자동 게시·DRAFT 전환) | docs/06_maintenance/20260712-014911/knowledge/history.md |
| 20260712-014911 | incident | IN_PROGRESS→RESOLVED 전이 및 해결 처리(resolve) 신규 승인 게이트 추가(우회 결함 수정) | docs/06_maintenance/20260712-014911/incident/history.md |
| 20260712-014911 | problem | WORKAROUND→RESOLVED_CLOSED 전이 신규 승인 게이트 추가(강제종료 제외) | docs/06_maintenance/20260712-014911/problem/history.md |
| 20260712-014911 | asset | 생애주기 전이·자산 폐기(RETIREMENT) 신규 승인 게이트 추가 | docs/06_maintenance/20260712-014911/asset/history.md |
| 20260712-014911 | vulnerability | PRIORITIZATION→REMEDIATION 전이 신규 승인 게이트 추가 | docs/06_maintenance/20260712-014911/vulnerability/history.md |
| 20260712-014911 | compliance | 시정조치 상태전이 신규 승인 게이트 추가(TicketType.CORRECTIVE_ACTION으로 항목별 독립 승인) | docs/06_maintenance/20260712-014911/compliance/history.md |
| 20260712-014911 | esm | 부서요청 상태전이 신규 승인 게이트 추가(HR 케이스·체크리스트 제외) | docs/06_maintenance/20260712-014911/esm/history.md |
| 20260712-111803 | auth | Access Token sessionStorage→Client Memory 전환 및 XSRF 더블서밋 쿠키 기반 CSRF 방지 | docs/06_maintenance/20260712-111803/auth/history.md |
| 20260712-180448 | common | i18n(react-i18next 계열) 도입 및 헤더 지구본 아이콘/언어 선택 팝업 신규(SCR-COM-015), toast·ConfirmDialog SweetAlert2 전환(Modal 제외), 가이드(SCR-COM-012) 영어 본문 신규 | docs/06_maintenance/20260712-180448/common/history.md |
| 20260712-180448 | auth | 로그인/프로필/비밀번호변경 및 관리자 8개 화면 다국어(한/영) 텍스트 전환 | docs/06_maintenance/20260712-180448/auth/history.md |
| 20260712-180448 | service-request | SRM 전 화면 다국어 전환, 공용 field-builder/rating/form-schema 미번역 결함 수정 | docs/06_maintenance/20260712-180448/service-request/history.md |
| 20260712-180448 | incident | INC 전 화면 다국어 전환, formatMinutes 미산정/분 단위 번역 키 통일 | docs/06_maintenance/20260712-180448/incident/history.md |
| 20260712-180448 | problem | PRB 전 화면 다국어 전환, origin 미지정 시 원시 번역 키 노출 회귀 수정 | docs/06_maintenance/20260712-180448/problem/history.md |
| 20260712-180448 | change | CHG 전 화면 다국어 전환, 연계 항목 링크 유형(linkTargetLabel) 원시값 노출 수정 | docs/06_maintenance/20260712-180448/change/history.md |
| 20260712-180448 | knowledge | KM 전 화면 다국어 전환 | docs/06_maintenance/20260712-180448/knowledge/history.md |
| 20260712-180448 | asset | ITAM 전 화면 다국어 전환, 티켓/CI관계/생애주기 라벨 원시값 노출 3건 수정 | docs/06_maintenance/20260712-180448/asset/history.md |
| 20260712-180448 | esm | ESM 11개 화면 다국어 전환, 체크리스트 유형 라벨 공용 헬퍼화 | docs/06_maintenance/20260712-180448/esm/history.md |
| 20260712-180448 | vulnerability | VULN 전 화면 다국어 전환 | docs/06_maintenance/20260712-180448/vulnerability/history.md |
| 20260712-180448 | compliance | COMP 전 화면 다국어 전환, 감사 로그 이벤트 코드(auditEventTypeLabel) 원시값 노출 수정 | docs/06_maintenance/20260712-180448/compliance/history.md |
| 20260712-180448 | infra-monitoring | IOM 전 화면 다국어 전환 | docs/06_maintenance/20260712-180448/infra-monitoring/history.md |
| 20260714-102553 | common | 사이드바 폭/폰트 축소(190px/48px, 12px/10px), DataTable 컬럼폭 고정 인프라(colgroup+table-fixed) 도입 및 17개 목록화면 적용, 1920x1080 기준 PAGE_SIZE 재산정(10/20→13/14/16) | docs/06_maintenance/20260714-102553/common/history.md |
| 20260714-151157 | runtime-upgrade | Java 25 + Spring Boot 4.1.0 + PostgreSQL 18 런타임 업그레이드(gradle 9.6.1, springdoc/jjwt/testcontainers 호환버전, Jackson3/RoleHierarchy 등 Framework7 대응) | docs/06_maintenance/20260714-151157/runtime-upgrade/history.md |
| 20260715-031710 | auth | 감사 로그 actor 오기록 수정(관리자→대상자 오기록), 승인 프로세스 범위 우선순위 3축(도메인/요청유형/역할) 독립 재설계·삭제 기능 추가·상세화면 요청유형 select 미렌더링 결함 수정 | docs/06_maintenance/20260715-031710/auth/history.md |
| 20260715-031710 | common | 공용 승인 엔진 도메인 미지정(전체 도메인) 규칙 지원(approval_process.domain nullable화, 매칭·우선순위 로직 확장) | docs/06_maintenance/20260715-031710/common/history.md |
| 20260715-031710 | srm | 서비스 요청 상세 승인 패널 "승인 절차 없음" 오해 문구를 게이트 평가 전/후 상태에 따라 분기 표시하도록 개선 | docs/06_maintenance/20260715-031710/srm/history.md |
| 20260715-061016 | service-request | 카탈로그 요청유형별 담당자(역할기반 배정 후보 필터)·담당 큐 Select 전환 신규, 요청 큐 배정버튼 노출조건(본인배정/ROUTED이후 숨김)·라우팅 버튼 담당자 미배정 시 비활성화(BE 409) 추가 | docs/06_maintenance/20260715-061016/service-request/history.md |
| 20260715-061016 | auth | 공용 역할 목록 조회 API(API-AUTH-030) 신규(카탈로그 담당자 역할 선택 등 타 도메인 재사용) | docs/06_maintenance/20260715-061016/auth/history.md |
| 20260715-061016 | common | 승인 프로세스 승인자(StepRole) 역할 기반 동적 상세조회 권한(`ApprovalGateService.canApproverView`) 신설, SRM/CHANGE 기존 정적 APPROVER 전체조회 폐지·대체, 승인 프로세스 있는 8개 도메인 전체 적용(INCIDENT 상세조회 역할체크 부재 결함 동시 정리, ESM 댓글권한 범위초과 결함 수정) | docs/06_maintenance/20260715-061016/common/history.md |
| 20260716-125723 | srm | 상태전이 버튼 문구 동작동사형 전환(transitionLabel), 타임라인 code→name+actor 표시, 서비스 카탈로그 카테고리 자유텍스트→고정목록 CRUD 신규(SCR-SRM-009), 양식 필드 여러 줄 텍스트(textarea) 추가 | docs/06_maintenance/20260716-125723/srm/history.md |
| 20260716-125723 | incident | 상태전이 버튼 문구 동작동사형 전환, 타임라인 code→name+actor 표시 | docs/06_maintenance/20260716-125723/incident/history.md |
| 20260716-125723 | problem | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/problem/history.md |
| 20260716-125723 | change | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/change/history.md |
| 20260716-125723 | vulnerability | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/vulnerability/history.md |
| 20260716-125723 | asset | 상태전이 버튼 문구 동작동사형 전환 | docs/06_maintenance/20260716-125723/asset/history.md |
| 20260716-125723 | esm | 상태전이 버튼 문구 동작동사형 전환, 타임라인 code→name+actor 표시(EsmRequest), 양식 필드 여러 줄 텍스트(공용 반영) | docs/06_maintenance/20260716-125723/esm/history.md |
| 20260716-125723 | common | 승인 대기함(SCR-COM-014) 티켓 상세보기 버튼 추가, 양식 필드 유형 textarea 추가(공용 컴포넌트), 타임라인 code→name/actor 공용 유틸(TimelineMessages·AppUserRepository.resolveDisplayName) 신설 | docs/06_maintenance/20260716-125723/common/history.md |
| 20260718-070801 | srm | 서비스 카탈로그 동적 양식을 form.io 스타일 드래그앤드롭 폼 빌더로 전면 전환(EAV→JSONB form_schema/form_values, FormSubmissionValidator 신규, 한글 key 자동생성 제약 known limitation 문서화) | docs/06_maintenance/20260718-070801/srm/history.md |
| 20260718-114544 | srm | 요청 큐(Queue 엔티티·테이블) 완전 폐지, 요청 분류/필터링을 카탈로그 카테고리 실시간 조인으로 일원화(category-counts API 신규, categoryId 파싱 400 회귀 수정) | docs/06_maintenance/20260718-114544/srm/history.md |
| 20260718-120242 | common | 미매핑 경로 404 catch-all 회귀 수정(GlobalExceptionHandler가 NoResourceFoundException까지 500 처리하던 결함, ENDPOINT_NOT_FOUND 404 신규, 전 도메인 영향) | docs/06_maintenance/20260718-120242/common/history.md |
| 20260718-141927 | srm | 서비스 카탈로그 동적 폼 빌더를 form.io(@formio) 완전 제거 후 자체 8×n 그리드 드래그앤드롭 빌더로 전면 재구현(팔레트 7종, Content 설정+정규식/필수 검증, pre-view, 기존 form_schema 리셋, file base64 변환 결함 수정) | docs/06_maintenance/20260718-141927/srm/history.md |
| 20260718-151522 | srm | 그리드 폼 빌더 후속 개선: 유효성 실패 메시지 하단 공통 표시(배열 순서상 첫 위반 1건 순차 진행), date/file 아이콘 전용 표시+선택값 텍스트, label을 입력 컴포넌트에서 완전 분리해 독립 팔레트 컴포넌트로 신규(팔레트 8종) | docs/06_maintenance/20260718-151522/srm/history.md
| 20260718-175042 | srm | 그리드 폼 빌더 세부 개선 2차: placeholder 추가(5종), guide 컴포넌트 신규(안내텍스트+파일 첨부, 팔레트 9종), 1×1 캡션 아이콘 전환+Content 설정 팝업 확대(420px), 팝업 내 개별 실시간 미리보기 신규, date/file "아이콘 전용 표시" 롤백(입력박스+우측 아이콘) | docs/06_maintenance/20260718-175042/srm/history.md |
| 20260718-184021 | srm | 그리드 폼 빌더 세부 개선 3차: Form 설정 팝업 좌 팔레트/중앙 캔버스/우 전체 레이아웃 미리보기 3분할 통합(세팅화면 pre-view 제거, 팝업 확대 w-90vw/max-w-1600px), radio/checkbox 배치방향(row/column)·옵션 여백 3단계 신규(기본 row/1단계), guide 폐기 후 guide-text/guide-file 2종 분리(팔레트 10종) | docs/06_maintenance/20260718-184021/srm/history.md |
| 20260718-192613 | srm | 그리드 폼 빌더 3분할 미리보기 정정: 3분할(팔레트/캔버스/우측 미리보기) 폐기 → 캔버스 카드가 GridComponentBody로 직접 렌더링하는 "캔버스=미리보기 통합" 2분할로 회귀(카드 렌더링은 비활성·드래그만 동작, 타입 아이콘/캡션/점선 placeholder 제거, 설정·삭제 버튼 오버레이화, 1×1 아이콘 전용 표시 로직 자연 폐기) | docs/06_maintenance/20260718-192613/srm/history.md |
| 20260718-214053 | srm | 그리드 폼 빌더 세부 개선 4차: 카탈로그 관리 화면 자체 축소 미리보기 재도입+여백 확대, 팔레트 드래그앤드롭 배치(클릭 병행), 컴포넌트별 높이상한 세분화(text/date/file/select/guide-file 1칸 고정), 정규식 검증 type=text 전용 축소, 정렬 9방향(3×3, verticalAlign 신규) 확장, 그리드 배치형 label 폐기 → "라벨(태그)" 개편(labels 배열+컴포넌트 labelId 참조, 칩 스트립, 2개 이상 참조 시 bounding box 테두리, 기존 label 데이터 리셋) | docs/06_maintenance/20260718-214053/srm/history.md |
| 20260718-222005 | srm | 그리드 폼 빌더 라벨 경계 테두리 보정(5차): 테두리 4px 확장(각 변 2px), 테두리 좌측상단 라벨 텍스트 표시 신규, 참조 1개부터 테두리 적용(직전 4차 "2개 이상" 결정 뒤집음), GridLabel.showBorder 신규(테두리 없음 체크박스, 캔버스 그룹 테두리만 숨김·칩 배지는 유지) | docs/06_maintenance/20260718-222005/srm/history.md |
| 20260718-234334 | srm | 그리드 폼 빌더 개별 미리보기 제거+라벨 표시 보정(6차): Content 설정 팝업 개별 실시간 미리보기 완전 제거, 라벨 텍스트를 legend 스타일(테두리 선 위에 걸침)로 변경, 오버레이 렌더링(참조 1개 이상)과 테두리 스타일(showBorder) 분리해 showBorder=false여도 텍스트 계속 표시하도록 결함 수정, 테두리색 UI를 "테두리 없음" 체크 상태에 따라 조건부 렌더링 | docs/06_maintenance/20260718-234334/srm/history.md |
