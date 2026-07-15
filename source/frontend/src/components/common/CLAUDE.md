# CLAUDE.md

프로젝트 공통 컴포넌트. `ui/`의 프리미티브를 조합해 도메인 무관한 재사용 패턴(상태 표시·목록/상세 셸·차트·폼)을 제공하며, 프레젠테이션만 담당하고 데이터·권한·라우팅은 기능 레이어(FE)가 주입. 화면 설계서(common.md SCR-COM-*)의 공통 UX 구현.

## 파일
- `index.ts` — 공통 컴포넌트 배럴. 기능 화면은 이 모듈에서 import.
- `status-badge.tsx` — 상태 배지. tone(success/warning/danger/info/muted) + 라벨을 FE가 주입. `StatusTone` 타입 제공.
- `priority-badge.tsx` — 우선순위 배지(P1~P4 → Danger~Muted). `Priority` 타입 제공.
- `toast.ts` — 토스트 helper(success/error/info). 외부 API 불변, 내부는 SweetAlert2 toast mixin(우상단, `index.css`의 `itsm-swal-toast*` 클래스로 시맨틱 토큰 스타일링, 2026-07-12 SweetAlert2 도입).
- `confirm-dialog.tsx` — 파괴적/비가역 동작 확인 다이얼로그. 선언형 API(props) 불변, 내부에서 `open` 변화를 감지해 SweetAlert2를 명령형으로 호출하는 래퍼(`index.css`의 `itsm-swal-popup`/`itsm-swal-btn*` 클래스, 2026-07-12 SweetAlert2 도입). 확인 시에만 `onConfirm` 호출, 닫힘은 호출측이 `onOpenChange`로 제어. `confirmLabel`/`cancelLabel` 미지정 시 `common:dialog.confirm`/`common:dialog.cancel`로 폴백.
- `modal.tsx` — 범용 모달(폼/상세 등 비파괴 콘텐츠). 파괴적 확인은 ConfirmDialog 사용. SweetAlert2 도입 대상에서 제외되어 기존 Radix Dialog 그대로 유지.
- `user-guide-content.tsx` — 사용자 가이드 전용 화면(`/guide`, SCR-COM-012)의 섹션 콘텐츠. `UserGuideOverview`(개요 1절, Markdown 순차 렌더링)·`UserGuideDomainSection`(11개 도메인 아코디언)·`UserGuideRoleSection`(16개 역할 아코디언, `myRoles?: string[]`로 "내 역할" 상단 고정+기본 펼침) 3개를 개별 export. 콘텐츠는 `docs/01_analyze/feature/user-guide-content.md`(한국어)·`user-guide-content.en.md`(영어, 2026-07-12 다국어 지원) 원문을 가공 없이 이관한 정적 데이터(`react-markdown`으로 굵게 등 인라인 서식 렌더링), `i18n.language`에 따라 언어별 콘텐츠 세트 선택. 페이지 레이아웃(문서 헤더·TOC)은 담당하지 않음 — FE(`features/guide/GuidePage.tsx`)가 조립.
- `multi-select.tsx` — 다중 선택(Popover + 체크 리스트). 필터바용. `MultiSelectOption` 타입 제공. `placeholder` prop 미지정 시 `common:multiSelect.defaultPlaceholder`로 폴백, 선택 개수·선택 해제 aria-label·빈 목록 문구는 `common:multiSelect.*` 키(2026-07-12 다국어 지원).
- `pagination.tsx` — 페이지네이션(0-based, 최대 5개 번호 창). `nav`/이전·다음 버튼 `aria-label`은 `common:pagination.*` 키(2026-07-12 다국어 지원).
- `data-table.tsx` — 제네릭 데이터 표. 로딩 스켈레톤·빈 상태·행 클릭 지원. `Column<T>` 타입 제공. `Column.width?: number`(px) 지정 시 `<colgroup>`+`table-fixed`로 컬럼 폭 고정(SCR-COM-007, 2026-07-14 유지보수 요청), 미지정 컬럼은 잔여 폭 흡수(auto).
- `empty-state.tsx` — 결과 0건 빈 상태 안내.
- `forbidden-view.tsx` — 403 접근 거부 뷰(프레젠테이션). `onBack` 주입. 문구는 `common:forbidden.*` 키(2026-07-12 다국어 지원).
- `not-found-view.tsx` — 404 Not Found 뷰(프레젠테이션). `onHome` 주입. 문구는 `common:notFound.*` 키(2026-07-12 다국어 지원, "404" 숫자 자체는 번역 대상 아님).
- `ticket-list-layout.tsx` — 공통 티켓 목록/필터 셸(타이틀·액션·필터바·목록).
- `ticket-detail-layout.tsx` — 공통 티켓 상세 셸(제목·배지·액션·본문·우측 메타 패널).
- `timeline.tsx` — 이력 타임라인(프레젠테이션). `TimelineItem` 타입 제공.
- `kpi-card.tsx` — 핵심 지표 카드(라벨·수치·단위).
- `trend-chart.tsx` — 기간별 추이 SVG 라인 차트(hover 툴팁). `TrendPoint` 타입 제공.
- `distribution-chart.tsx` — 범주형 세로 막대 분포 SVG 차트. `DistributionDatum` 타입 제공.
- `rating.tsx` — 별점 위젯(읽기 전용/입력). CSAT용. aria-label은 `common:rating.*` 키(2026-07-12 다국어 지원).
- `dynamic-form.tsx` — 스키마 기반 동적 폼 렌더러(제어 컴포넌트, 인라인 오류).
- `field-builder.tsx` — 동적 폼 필드 정의 빌더(라벨·유형·필수·옵션). 문구는 `common:fieldBuilder.*` 키(2026-07-12 다국어 지원).
- `form-schema.ts` — 동적 폼 스키마 계약(`FormFieldSchema`/`FormValues`/`FormErrors`)과 `validateForm`·`hasOptions` 헬퍼. `validateForm`은 선택 인자 `t`(caller의 `useTranslation` 훅 결과) 전달 시 필수 항목 오류 메시지를 `common:validation.required` 키로 다국어 전환하고, 미전달 시 기존 하드코딩 한국어로 폴백(하위 호환, 2026-07-12 다국어 지원).
- `approval-schema.ts` — 승인 프로세스 공용 타입. API-COM-004(`docs/02_plan/api_spec/common.md`) 응답 구조·필드명(`stepNo`/`decisionMode`/`roles[].{roleCode,decision,decidedBy,reason,decidedAt}`)을 그대로 따른다(`ApprovalStep`/`ApprovalStepRole` 등). `ApprovalMatchType`(AND/OR)은 `approval-process-flow.tsx`와도 공유.
- `approval-process-flow.tsx` — 승인 프로세스 생성/편집 플로우(admin.md SCR-ADMIN-008). 1(승인 요청자)~2(승인자 n차) 단계 카드 스택, 승인자 박스 드래그 재정렬(순서 교체), 역할 선택 슬라이드 패널(`ui/sheet`, 검색+드래그/클릭 추가), AND/OR 체크박스(역할 2개 이상), 승인자 0개 저장 확인 다이얼로그·승인자 박스별 역할 미선택 인라인 오류를 자체 처리. 제어 컴포넌트(`ApprovalStepBoxValue[]` 등 값+onChange). `domain`은 하단 카드 스택 활성화 여부 판정용으로만 받고(도메인/요청유형 선택 UI는 메타데이터 분리 개편으로 FE의 "규칙 정보" 카드가 담당, 유지보수 요청 2026-07-13) `ApprovalRoleOption`/`ApprovalStepBoxValue` 타입 제공. 문구는 `auth:admin.approvalProcessForm.flow.*` 키(2026-07-13 i18n 커버리지 결함 수정). 요청자 박스는 역할 0개(전체 요청자 축 미지정) 저장을 허용한다 — 승인자 박스(steps)만 API-AUTH-027 계약상 roleIds 1개 이상 필수(2026-07-15 우선순위 재설계 결함 수정, 이전에는 요청자에도 승인자와 동일한 1개 이상 검증이 걸려 tier=0/11/23 규칙을 화면에서 생성 못했음).
- `approval-step-progress.tsx` — 승인 차수 진행 현황(순수 프레젠테이션, 액션 없음). API-COM-004 `steps` 그대로 렌더링: 전체 차수 상태(완료 Success/현재 Warning/이후 Muted/반려 Danger) + `currentStepNo` 차수의 역할별 결정 상세(AND 전체 나열/OR "역할 중 하나", decidedBy·reason 포함). `compact` prop으로 상세 생략(반려 사유만 노출) 가능 — `approval-panel.tsx`가 이 모드로 재사용. 승인/반려 액션·사유 입력은 담당하지 않음(SCR-COM-014 화면에서 FE가 별도 조립). 문구는 `common:approval.*` 키(2026-07-12 다국어 지원).
- `approval-panel.tsx` — 도메인 상세 화면(SRM/CHG/INC 등)이 재사용하는 읽기 전용 승인 현황 패널. `ApprovalStepProgress`를 compact 모드로 감싼다. `matched=false`이고 `emptyMessage` 미지정 시 렌더링하지 않음(INC처럼 패널 자체 미노출), 지정 시 안내 문구 표시(SRM처럼 "승인 절차가 없습니다", 이 문구는 FE가 주입해 도메인 자체 i18n 전환 시점에 처리). 패널 타이틀은 `common:approval.panelTitle` 키(2026-07-12 다국어 지원).
