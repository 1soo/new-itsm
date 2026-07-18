# docs/04_test — 통합 테스트 이력 인덱스

도메인별 통합 테스트 실행 이력을 기록한다.

## 구조

- `{yyyyMMdd-HHmmss}/{domain}/scenario.md`: 실행 시나리오
- `{yyyyMMdd-HHmmss}/{domain}/result/{domain}.md`: 실행 결과 (증적 파일은 result/ 하위에 저장)

## 인덱스

`tester` 에이전트는 결과 작성 후 아래 표에 한 줄씩 추가한다.
이전 실행 이력 확인 시 **이 표만 먼저 확인**해 관련 있어 보이는 항목의
`result/{domain}.md`만 선택 열람한다. (전수 조사 금지 — 토큰 최소화)

| 일시 | 도메인 | 결과 | 요약 | 경로 |
|------|--------|------|------|------|
| 20260709-105458 | auth | partial | 로그인, 토큰 재발급, RBAC 인가, 감사 로그 | `docs/04_test/20260709-105458/auth/result/auth.md` |
| 20260709-112121 | auth | partial | 토큰 재발급 수정확인, 감사 로그 수정확인, 계정 생성 회귀, 트랜잭션 전파 | `docs/04_test/20260709-112121/auth/result/auth.md` |
| 20260709-114031 | auth | pass | 계정 생성 회귀 해소, 감사 로그 트랜잭션 분리, RBAC, 회귀 테스트 | `docs/04_test/20260709-114031/auth/result/auth.md` |
| 20260709-122918 | service-request | partial | 승인 재처리 결함, 상태전이, 카탈로그, CSAT | `docs/04_test/20260709-122918/service-request/result/service-request.md` |
| 20260709-124548 | service-request | pass | 승인 재처리 결함 수정 검증, 상태전이 회귀, 워크플로우 무결성 | `docs/04_test/20260709-124548/service-request/result/service-request.md` |
| 20260709-161520 | incident | pass | 상태전이, 역할배정RBAC, 에스컬레이션, 포스트모템, MTTx지표 | `docs/04_test/20260709-161520/incident/result/incident.md` |
| 20260709-165140 | problem | partial | 상태전이, RCA, 워크어라운드, KEDB, 인시던트 연계 | `docs/04_test/20260709-165140/problem/result/problem.md` |
| 20260709-214912 | problem | pass | 재테스트, 인시던트 연계, 오류코드 정합, 빌드 | `docs/04_test/20260709-214912/problem/result/problem.md` |
| 20260709-230947 | change | partial | 상태전이, 표준변경 사전승인, CAB 승인/반려, 크로스도메인 연계 | `docs/04_test/20260709-230947/change/result/change.md` |
| 20260709-233129 | change | pass | 표준변경 AUTO 승인경로, 승인경로 분류, 회귀테스트 | `docs/04_test/20260709-233129/change/result/change.md` |
| 20260710-055238 | knowledge | pass | 기사작성편집, 검토승인워크플로우, RBAC, KCS티켓연계, 지식지표 | `docs/04_test/20260710-055238/knowledge/result/knowledge.md` |
| 20260710-064414 | asset | partial | 자산 생애주기, 만료 추적, CI/CMDB 관계, 티켓 연계 | `docs/04_test/20260710-064414/asset/result/asset.md` |
| 20260710-071918 | asset | pass | 자산-티켓 양방향 연계, 회귀테스트 | `docs/04_test/20260710-071918/asset/result/asset.md` |
| 20260710-095827 | ui-revamp | pass | 다크모드 토글, Lozenge 배지, 접근성(포커스링·대비), 공통 컴포넌트 API, 도메인 화면 회귀 | `docs/04_test/20260710-095827/ui-revamp/result/ui-revamp.md` |
| 20260710-104411 | knowledge | pass | 목록컬럼줄바꿈, whitespace-nowrap, 검색필터회귀, 페이지네이션 | `docs/04_test/20260710-104411/knowledge/result/knowledge.md` |
| 20260710-131137 | change | pass | 구현결과 기록 폼 가드, 승인 완료 여부, RBAC | `docs/04_test/20260710-131137/change/result/change.md` |
| 20260710-131137 | common | pass | 사이드바, 알림벨, 통합검색 | `docs/04_test/20260710-131137/common/result/common.md` |
| 20260710-173404 | esm | pass | 카탈로그 관리, 온보딩/오프보딩 체크리스트, HR 케이스, 권한 검증, 지표 대시보드 | `docs/04_test/20260710-173404/esm/result/esm.md` |
| 20260710-183750 | vulnerability | pass | 리스크스코어, 상태전이, 담당자배정, 자산연계, 권한제어 | `docs/04_test/20260710-183750/vulnerability/result/vulnerability.md` |
| 20260710-201807 | compliance | pass | 요구사항 등록, 시정조치 상태전이, 책임자 지정, 변경 연계, 감사 로그 | `docs/04_test/20260710-201807/compliance/result/compliance.md` |
| 20260710-234456 | infra-monitoring | pass | 지표 등록, 임계치 알림, 용량 계획, SLA 가동률, 리포팅 집계 | `docs/04_test/20260710-234456/infra-monitoring/result/infra-monitoring.md` |
| 20260710-235959 | infra-monitoring | pass | 용량 활용률 배지, SLA 라벨 구분, 재검증 | `docs/04_test/20260710-235959/infra-monitoring/result/infra-monitoring.md` |
| 20260711-011815 | common | pass | SYSTEM_ADMIN, RBAC, 권한회귀 | `docs/04_test/20260711-011815/common/result/common.md` |
| 20260711-015654 | common | pass | 알림팝오버, 상세보기, 뱃지 | `docs/04_test/20260711-015654/common/result/common.md` |
| 20260711-022915 | common | pass | 알림팝오버, 2줄레이아웃, 상대시간 | `docs/04_test/20260711-022915/common/result/common.md` |
| 20260711-075555 | common | pass | 사용자가이드, 모달, 아코디언 | `docs/04_test/20260711-075555/common/result/common.md` |
| 20260711-083648 | common | pass | 사용자가이드, 전용화면, TOC | `docs/04_test/20260711-083648/common/result/common.md` |
| 20260711-115017 | auth | pass | 메뉴 관리, 역할-메뉴 매핑, 동적 RBAC, 내 메뉴 조회 | `docs/04_test/20260711-115017/auth/result/auth.md` |
| 20260711-120600 | auth | pass | 페이지네이션 무결성, 역할별 사이드바, 회귀 검증, 메뉴 관리 | `docs/04_test/20260711-120600/auth/result/auth.md` |
| 20260711-122421 | common | pass | 알림확인처리, 모두지우기, 개별삭제 | `docs/04_test/20260711-122421/common/result/common.md` |
| 20260711-123218 | common | pass | 알림확인처리, 사용자별격리, 상세보기이동 | `docs/04_test/20260711-123218/common/result/common.md` |
| 20260711-154500 | common | pass | 알림polling, 백그라운드정지, merge정책 | `docs/04_test/20260711-154500/common/result/common.md` |
| 20260711-195405 | approval-engine | pass | SRM 승인 게이트, 관리자 승인 프로세스 CRUD, soft-delete 유니크 제약, 승인 대기함/알림 | `docs/04_test/20260711-195405/approval-engine/result/approval-engine.md` |
| 20260711-204816 | approval-engine | pass | CHANGE 승인 게이트, 전이 버튼 숨김(FE), 승인 대기함 통합 조회 | `docs/04_test/20260711-204816/approval-engine/result/approval-engine.md` |
| 20260711-205100 | approval-engine | pass | CHANGE 승인 게이트, tier 우선순위 매칭, AND 승인 스냅샷 | `docs/04_test/20260711-205100/approval-engine/result/approval-engine.md` |
| 20260711-213921 | approval-engine | pass | KNOWLEDGE 게시 승인 게이트, 게이트키퍼 승인/반려, 자동 상태전환(PUBLISHED/DRAFT) | `docs/04_test/20260711-213921/approval-engine/result/approval-engine.md` |
| 20260711-214200 | approval-engine | pass | KNOWLEDGE 게시 승인 게이트, 승인 이력 보존, 구 메뉴 제거 회귀 | `docs/04_test/20260711-214200/approval-engine/result/approval-engine.md` |
| 20260711-234110 | approval-engine | pass | INCIDENT 승인 게이트, PROBLEM 승인 게이트, resolve() 게이트 우회 발견 | `docs/04_test/20260711-234110/approval-engine/result/approval-engine.md` |
| 20260712-000915 | approval-engine | pass | INCIDENT resolve 게이트 보완, 우회 경로 차단 재검증 | `docs/04_test/20260712-000915/approval-engine/result/approval-engine.md` |
| 20260712-003649 | approval-engine | pass | ASSET 승인 게이트, VULNERABILITY 승인 게이트, lifecycle/retire 엔드포인트 우회 재발방지 | `docs/04_test/20260712-003649/approval-engine/result/approval-engine.md` |
| 20260712-012841 | approval-engine | pass | COMPLIANCE 시정조치 승인, ESM 부서서비스 승인, 항목단위 독립 승인 인스턴스 | `docs/04_test/20260712-012841/approval-engine/result/approval-engine.md` |
| 20260712-021500 | auth | pass | 토큰 저장 방식, CSRF, 더블서밋 쿠키, XSS 방지 | `docs/04_test/20260712-021500/auth/result/auth.md` |
| 20260712-123337 | common | pass | 다국어i18n, SweetAlert2, 언어전환 | `docs/04_test/20260712-123337/common/result/common.md` |
| 20260712-131458 | auth | pass | 다국어(i18n), 영어 전환, RBAC 회귀, 감사 로그 | `docs/04_test/20260712-131458/auth/result/auth.md` |
| 20260712-133620 | service-request | pass | 다국어(i18n) 전환, field-builder 미번역 결함, 카탈로그 관리, 회귀 테스트 | `docs/04_test/20260712-133620/service-request/result/service-request.md` |
| 20260712-141106 | incident | pass | 다국어전환i18n, formatMinutes단위표기, 통합검색상태배지, 날짜포맷유지 | `docs/04_test/20260712-141106/incident/result/incident.md` |
| 20260712-143702 | problem | pass | 다국어, 상태전이, KEDB 검색, 통합검색 | `docs/04_test/20260712-143702/problem/result/problem.md` |
| 20260712-160842 | change | pass | 다국어(i18n) 전환, 연계 항목 라벨, 날짜 포맷 로케일 유지 | `docs/04_test/20260712-160842/change/result/change.md` |
| 20260712-162939 | knowledge | pass | 다국어i18n전환, 검토승인패널, 통합검색배지, 날짜포맷유지 | `docs/04_test/20260712-162939/knowledge/result/knowledge.md` |
| 20260712-164256 | asset | pass | 다국어(i18n), 생애주기 이력 라벨, CI/CMDB 관계 | `docs/04_test/20260712-164256/asset/result/asset.md` |
| 20260712-170529 | esm | pass | 다국어(i18n) 전환, 부서 서비스 포털, HR 케이스, 체크리스트, ESM 지표 | `docs/04_test/20260712-170529/esm/result/esm.md` |
| 20260712-172532 | vulnerability | pass | 다국어i18n, 상태배지, 리스크스코어, 지표대시보드, 로케일포맷 | `docs/04_test/20260712-172532/vulnerability/result/vulnerability.md` |
| 20260712-174109 | compliance | pass | i18n, 다국어 전환, 감사 로그 라벨, 시정조치 승인 게이트 | `docs/04_test/20260712-174109/compliance/result/compliance.md` |
| 20260712-175603 | infra-monitoring | pass | 다국어(i18n) 전환, 지표 등록/대시보드, 임계치 알림, 용량 계획/리포팅, 한국어 재전환 | `docs/04_test/20260712-175603/infra-monitoring/result/infra-monitoring.md` |
| 20260713-164700 | admin-common | pass | 메뉴관리, i18n, 승인프로세스, 권한가드 | `docs/04_test/20260713-164700/admin-common/result/admin-common.md` |
| 20260714-101414 | common | pass | 사이드바축소, 컬럼폭고정, PAGE_SIZE | `docs/04_test/20260714-101414/common/result/common.md` |
| 20260714-150102 | runtime-upgrade | pass | java25, spring-boot4, postgresql18, gradle-upgrade, testcontainers | `docs/04_test/20260714-150102/runtime-upgrade/result/runtime-upgrade.md` |
| 20260714-184455 | runtime-upgrade | pass | testcontainers, postgresql18, docker-compose, swagger-ui, spring-boot4 | `docs/04_test/20260714-184455/runtime-upgrade/result/runtime-upgrade.md` |
| 20260715-064012 | asset | pass | 자산 등록, 회귀테스트 | `docs/04_test/20260715-064012/asset/result/asset.md` |
| 20260715-064012 | auth | pass | 로그인, 토큰 재발급, 감사 로그, 알림 | `docs/04_test/20260715-064012/auth/result/auth.md` |
| 20260715-064012 | change | pass | RFC 생성, 승인 프로세스 미구성 상태 | `docs/04_test/20260715-064012/change/result/change.md` |
| 20260715-064012 | compliance | pass | PG18/Boot4.1 런타임 검증, 준수상태 조회, 책임자 지정 | `docs/04_test/20260715-064012/compliance/result/compliance.md` |
| 20260715-064012 | esm | pass | 부서 서비스 포털, 동적 양식 필드, 카탈로그 | `docs/04_test/20260715-064012/esm/result/esm.md` |
| 20260715-064012 | incident | pass | 런타임업그레이드회귀테스트, 인시던트등록, MTTx미산정 | `docs/04_test/20260715-064012/incident/result/incident.md` |
| 20260715-064012 | infra-monitoring | pass | 지표 조회 폼, 지표 등록, 토큰 재발급 | `docs/04_test/20260715-064012/infra-monitoring/result/infra-monitoring.md` |
| 20260715-064012 | knowledge | pass | 카테고리콤보박스, 기사작성 | `docs/04_test/20260715-064012/knowledge/result/knowledge.md` |
| 20260715-064012 | problem | pass | 우선순위 미리보기, 문제 등록 | `docs/04_test/20260715-064012/problem/result/problem.md` |
| 20260715-064012 | service-request | pass | 서비스 포털 동적 양식, 요청 큐, 승인 대기함 | `docs/04_test/20260715-064012/service-request/result/service-request.md` |
| 20260715-064012 | vulnerability | pass | PostgreSQL18, numeric캐시컬럼, 런타임업그레이드 | `docs/04_test/20260715-064012/vulnerability/result/vulnerability.md` |
| 20260715-112437 | auth | pass | 감사 로그 actor/target, 승인 프로세스, priority tier, 요청자 역할 | `docs/04_test/20260715-112437/auth/result/auth.md` |
| 20260715-112437 | common | pass | 승인프로세스우선순위, tier, 도메인미지정 | `docs/04_test/20260715-112437/common/result/common.md` |
| 20260715-112437 | srm | pass | 승인 패널 문구, 상태 전이 게이트, 서비스 요청 | `docs/04_test/20260715-112437/srm/result/srm.md` |
| 20260715-142838 | auth | pass | 역할 목록 조회, 공개 API, 카탈로그 연동 | `docs/04_test/20260715-142838/auth/result/auth.md` |
| 20260715-142838 | common | pass | 동적상세조회권한, RBAC, 감사로그 | `docs/04_test/20260715-142838/common/result/common.md` |
| 20260715-142838 | service-request | pass | 담당자 후보 배정, 담당 큐 Select, 라우팅 담당자 미배정 가드, assigneeId 결함 수정 | `docs/04_test/20260715-142838/service-request/result/service-request.md` |
| 20260716-080731 | asset | pass | 상태전이 버튼 라벨, 생애주기 이력 | `docs/04_test/20260716-080731/asset/result/asset.md` |
| 20260716-080731 | change | pass | 상태전이 버튼 라벨 동사형 전환, 표준변경 무승인 진행, 타임라인 부재 | `docs/04_test/20260716-080731/change/result/change.md` |
| 20260716-080731 | esm | pass | 상태전이 버튼 라벨, 타임라인 actor 표시, 부서 요청, HR 케이스 | `docs/04_test/20260716-080731/esm/result/esm.md` |
| 20260716-080731 | incident | pass | 상태전이버튼라벨, 타임라인actor표시, 상태라벨 | `docs/04_test/20260716-080731/incident/result/incident.md` |
| 20260716-080731 | problem | pass | 상태전이 버튼 라벨, 동사형 문구, 타임라인 미표시 | `docs/04_test/20260716-080731/problem/result/problem.md` |
| 20260716-080731 | service-request | pass | 상태전이 버튼 라벨 동사형, 타임라인 actor 표시 | `docs/04_test/20260716-080731/service-request/result/service-request.md` |
| 20260716-080731 | vulnerability | pass | 상태전이버튼라벨, 동사형전환, 타임라인 | `docs/04_test/20260716-080731/vulnerability/result/vulnerability.md` |
| 20260716-105846 | service-request | pass | 서비스 카탈로그 카테고리 CRUD, 카테고리 중복/참조무결성 검증 | `docs/04_test/20260716-105846/service-request/result/service-request.md` |
| 20260716-112347 | common | pass | 승인대기함, 상세보기버튼, 토스트조사오류 | `docs/04_test/20260716-112347/common/result/common.md` |
| 20260716-112347 | esm | pass | 동적 양식 필드, textarea 유형, 카탈로그 관리 | `docs/04_test/20260716-112347/esm/result/esm.md` |
| 20260716-112347 | service-request | pass | 양식 필드 textarea 유형, 개행 보존 | `docs/04_test/20260716-112347/service-request/result/service-request.md` |
| 20260717-145302 | srm | partial | form.io 폼 빌더, 동적 폼 렌더러, FormSubmissionValidator pattern 버그(제출 차단) | `docs/04_test/20260717-145302/srm/result/srm.md` |
| 20260717-152015 | srm | partial | pattern 버그 수정확인, 팔레트 구성 수정확인, 제출/취소 버튼 배치(중복 Submit 버튼 결함) | `docs/04_test/20260717-152015/srm/result/srm.md` |
| 20260717-153547 | srm | pass | 중복 Submit 버튼 결함 수정확인, 제출/취소 버튼 배치 | `docs/04_test/20260717-153547/srm/result/srm.md` |
| 20260718-112351 | srm | pass | 요청 큐 폐지, 카테고리 기반 분류 일원화, 실시간 조인(요구사항 충족, /api/v1/queues 500은 도메인 무관 별도 결함) | `docs/04_test/20260718-112351/srm/result/srm.md` |
| 20260718-135109 | srm | pass | form.io 완전 제거, 자체 8×n 그리드 폼 빌더, 팔레트 7종, 겹침 차단, Content 설정, 서버 재검증, ESM 회귀 없음 | `docs/04_test/20260718-135109/srm/result/srm.md` |
| 20260718-145952 | srm | pass | label 컴포넌트, date/file 아이콘화, 순차 단일 오류(배열 순서 기준), 서버 재검증 동일 계약, 기존 SRM 회귀 없음 | `docs/04_test/20260718-145952/srm/result/srm.md` |
| 20260718-172626 | srm | pass | placeholder 7종, guide 컴포넌트(안내+첨부), 1×1 캡션 아이콘화, 개별 실시간 미리보기, date/file 입력박스 롤백 | `docs/04_test/20260718-172626/srm/result/srm.md` |
| 20260718-182813 | srm | pass | Form 설정 팝업 3분할 실시간 미리보기, radio/checkbox 배치방향·여백, guide-text/guide-file 2종 분리, 외부 pre-view 제거 | `docs/04_test/20260718-182813/srm/result/srm.md` |
| 20260718-191941 | srm | pass | 3분할 폐기 → 캔버스=미리보기 통합(2분할), 캔버스 카드 실제 렌더링(상호작용 차단), hover 오버레이 액션, Content 설정 개별 미리보기(상호작용 가능) 구분 | `docs/04_test/20260718-191941/srm/result/srm.md` |
| 20260718-210534 | srm | partial | 축소 미리보기 재도입, DnD 배치(드래그 직후 클릭 씹힘 결함), 높이 상한 세분화, 정규식 text 전용, 라벨 태그 개편(생성/수정/삭제/경계 오버레이), DB 조건부 리셋 | `docs/04_test/20260718-210534/srm/result/srm.md` |
| 20260718-213750 | srm | pass | TC-GF4-B01e 수정확인(JUST_DRAGGED_RESET_MS 300→0), 드래그직후클릭·지연후클릭·드래그취소후클릭 3시나리오 | `docs/04_test/20260718-213750/srm/result/srm.md` |
| 20260718-220645 | srm | pass | 라벨 경계 테두리 보정(1개 이상 기준, 4px 확장, 텍스트 표시, showBorder 체크박스), 즉시 재계산, 캔버스 외부 미노출, 4차 회귀 | `docs/04_test/20260718-220645/srm/result/srm.md` |
| 20260718-233219 | srm | pass | 개별 미리보기 제거, 라벨 legend 스타일, 5차 결함 수정 확인(showBorder=false여도 텍스트 유지), 테두리색 조건부 렌더링+값보존, 이전 차수 회귀 | `docs/04_test/20260718-233219/srm/result/srm.md` |
| 20260719-000230 | srm | pass | 미니팝업 위치 통일(Popover→Modal 전환, top-42%, 트리거 무관 고정좌표), 라벨(태그) Select 최상단 이동, 다른 화면 Modal(top-1/2) 회귀없음, 이전 차수 회귀 | `docs/04_test/20260719-000230/srm/result/srm.md` |
