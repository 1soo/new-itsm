# 개발 계획 — COMMON (SCR-COM-002 헤더 알림 팝오버)

> v1(아래 "스펙"~"테스트 관점")은 2026-07-11 최초 개발·통합테스트 PASS·커밋(`e63f923`) 완료분. 이후 사용자 피드백으로 2줄 레이아웃 변경이 추가되어 "v2" 섹션 참고.

## 설계 근거 (v1)

- `docs/02_plan/screen/common.md` SCR-COM-002(글로벌 헤더) v0.3(2026-07-11) 확정. 근거: `docs/01_analyze/prd/common.md` REQ-COM-001 / `docs/01_analyze/feature/common.md` FEAT-COM-001. (초안과 내용 동일, REQ/FEAT ID 정식 반영됨)
- 신규 백엔드 API 없음. 기존 3개 API 재사용:
  - `srmApi.listApprovals()` (API-SRM-012)
  - `changeApi.listApprovals()` (API-CHG-007)
  - `assetApi.list({ expiringWithinDays, size })` (API-ITAM-001) — 팝오버 항목 확보를 위해 기존 `size=1`(count 전용) 호출을 `size=8`로 변경.

## 스펙 (v1, 아래 v2로 대체됨)

1. **노출 개수·정렬**: 서비스요청 승인 대기 → 변경 승인 대기 → 자산 만료 임박 순으로 이어붙여 상위 8건만 노출. 벨 뱃지 카운트는 상한과 무관한 전체 대기 건수 합계(기존 로직 유지).
2. **항목 형식**: 좌측 도메인 Lozenge(Info tone: "서비스요청 승인"/"변경 승인"/"자산 만료") + 내용 1줄(40자 초과 시 말줄임표) + 우측 "상세 보기" 텍스트 버튼.
   - 서비스요청 승인: `{ticketKey} · {requester} 승인 요청`
   - 변경 승인: `{ticketKey} · {type}/{risk} · {requester} 승인 요청`
   - 자산 만료: `{assetKey} · {name} · {expiryDate} 만료 예정`
3. **상세 보기 이동 대상**: 개별 항목 상세(목록 화면 아님) — `/service-requests/{requestId}`, `/changes/{changeId}`, `/assets/{id}`.
4. **빈 상태**: "새로운 알림이 없습니다". 벨 뱃지는 0건이면 숨김(기존 유지).
5. **트리거·닫힘**: 기존 통합검색 미리보기 Popover 패턴 재사용(`Popover`/`PopoverAnchor`/`PopoverContent`, align="end", Overlay elevation, 외부 클릭/Esc로 닫힘). 벨 클릭 시 기존처럼 즉시 target으로 navigate하지 않고 팝오버를 연다("상세 보기" 버튼 또는 라인 클릭이 navigate 담당).
6. **크기**: 폭 320px 고정(검색 미리보기와 달리 트리거 폭에 종속되지 않음). 목록 높이 320px 초과 시 세로 스크롤.

## 담당 범위

### dev-ui — `source/frontend/src/components/layout/header.tsx`

- 통합검색 미리보기(`Popover`/`PopoverAnchor`/`PopoverContent`, L120~159 패턴)와 동일한 구조로 알림 벨에 팝오버 적용.
- 신규 타입 제안(최종 형태는 dev-ui 재량, dev_fe와 직접 협의 가능):
  ```ts
  export interface HeaderNotificationItem {
    key: string;
    domainLabel: string; // "서비스요청 승인" | "변경 승인" | "자산 만료"
    text: string;        // FE가 40자 truncate 처리 완료한 문자열
  }
  ```
- `HeaderProps` 확장: `notifications?: HeaderNotificationItem[]`(undefined=미로딩, []=알림 없음), `onSelectNotification?: (item: HeaderNotificationItem) => void`.
- 기존 `onNotifications` (벨 클릭 시 즉시 navigate)는 팝오버 오픈으로 대체되므로 제거하고, `AppLayout.tsx`의 호출부도 함께 정리 필요 — 제거 시 dev_fe에게 SendMessage로 통보.
- 도메인 배지는 `StatusBadge`(`@/components/common`, tone="info") 재사용(검색 도메인 배지와 동일 컴포넌트).
- 항목 리스트: `text-sm` 본문 + 우측 "상세 보기" 텍스트 버튼(`variant="link"` 또는 `ghost` 소형 버튼), 클릭 시 팝오버 닫고 `onSelectNotification` 호출.
- 빈 상태 문구 "새로운 알림이 없습니다"는 `notifications`가 정의되어 있고 길이 0일 때만 표시.
- `aria-label`은 기존 알림 카운트 패턴 유지.

### dev_fe — `source/frontend/src/routes/AppLayout.tsx`

- 기존 79~121행 `notification` state(`{ count, target }`) 로직을 알림 항목 리스트 조립으로 확장.
- `assetApi.list({ expiringWithinDays: ASSET_EXPIRING_WITHIN_DAYS, size: 1 })` 호출을 `size: 8`로 변경(뱃지 카운트는 `assets.totalElements`로 기존과 동일하게 계산, 리스트는 `assets.content` 등 응답 항목 사용 — 실제 응답 필드명은 `features/asset/api.ts`/`types.ts` 확인).
- 서비스요청/변경 승인 목록에서 각 8건 노출 규칙에 맞게 앞에서부터 이어붙여 상위 8건 slice.
- 각 항목을 `HeaderNotificationItem`으로 매핑(위 텍스트 포맷 규칙 적용, 40자 초과 시 말줄임표 처리 유틸 함수 작성).
- `onSelectNotification`: `navigate(href)` 하며 팝오버는 header 내부에서 자체적으로 닫힘 처리(또는 콜백에서 닫힘 신호 필요 시 dev-ui와 협의).
- 기존 `onNotifications`(즉시 navigate) 콜백 제거.
- 역할 조건(`ROLE_APPROVER`, `ROLE_ASSET_MANAGER`)은 기존 로직 그대로 유지 — 권한 없는 사용자는 해당 소스의 항목이 리스트에 포함되지 않음.

## v3 — 다국어(i18n) 지원 + SweetAlert2 도입 (유지보수 요청, 2026-07-12)

### 설계 근거
- `docs/02_plan/screen/common.md` v0.13. SCR-COM-002(헤더 지구본 아이콘), SCR-COM-009(SweetAlert2), SCR-COM-012(가이드 다국어), SCR-COM-015(언어 선택 신규), 6절 "i18n 아키텍처"(라이브러리·리소스 구조·전환 패턴·도메인별 인벤토리).
- 이번 "common" phase는 i18n 인프라 부트스트랩 + common 네임스페이스 전체(SCR-COM-001~015, SCR-ERR-404) + SweetAlert2 도입을 다룬다. 이후 11개 도메인 phase는 각 도메인 `*Page.tsx`/`status.ts` 텍스트만 번역 키로 치환(레이아웃 변경 없음).
- BE/DB 변경 없음(전 phase 공통).

### 신규 의존성
`i18next`, `react-i18next`, `sweetalert2` — `source/frontend/package.json`. 설치·버전 확정은 dev-ui가 수행(다른 팀원 작업과 충돌 없는 최초 1회 작업이므로 진행 전 dev-lead에게 알리고 진행).

### 파일 충돌 방지를 위한 담당 분리 (이번 common phase 한정)

**locales 리소스 파일(`source/frontend/src/i18n/locales/**`)은 이번 phase 동안 dev-ui가 단독 소유·편집**한다(dev-fe는 이 경로를 직접 편집하지 않는다). dev-fe가 담당 파일(`routes/AppLayout.tsx` 등)에서 필요한 번역 키를 정할 때:
- 6.4절 표에 이미 지정된 키(`common:notification.domainLabel.*`, `common:notification.relativeTime.*`, `common:header.*`)는 그대로 `t()` 호출에 사용하고, 각 호출에 기존 하드코딩 한국어 문자열을 `defaultValue`로 함께 전달한다(`t("notification.domainLabel.assetExpiry", { ns: "common", defaultValue: "자산 만료" })`) — 이렇게 하면 dev-ui가 아직 해당 키를 json에 채우지 않은 시점에도 화면은 기존 한국어 그대로 정상 동작한다.
- 6.4절 표에 없는 신규 문자열(대시보드 환영 문구, 403/404 안내, 검색 결과 화면, 승인 대기함 화면 등)은 dev-fe가 키 이름을 제안(`common:{section}.{itemKey}` 컨벤션)하고 한국어 원문 그대로 `defaultValue`로 넣어 코드에 반영한 뒤, 제안 키·한국어·영어 번역 후보를 SendMessage로 dev-ui에게 전달한다. dev-ui가 최종 키 확정 후 `locales/ko/common.json`·`locales/en/common.json`에 반영한다.
- 이 규칙으로 dev-fe는 locales json을 직접 건드리지 않고도 즉시 개발·검증 가능하다(defaultValue가 폴백 역할).

### dev-ui 담당 범위

1. **i18n 코어 인프라** (`source/frontend/src/i18n/` 신규):
   - `index.ts` — `i18next.use(initReactI18next).init({ resources, lng: 저장된 언어 또는 "ko", fallbackLng: "ko", defaultNS: "common", ns: [12개 네임스페이스] })`. Provider 불필요(default 인스턴스 사용, `main.tsx`에서 `import "@/i18n"`만 추가 — 이 한 줄은 dev-ui가 직접 추가하고 dev-fe에게 통보).
   - `language.ts` — `itsm-language` 키로 `localStorage` 읽기/쓰기, 기본값 "ko"(`theme-toggle.tsx`와 동일 패턴).
   - `locales/ko/*.json`, `locales/en/*.json` — 12개 네임스페이스(common + 11개 도메인) 파일 생성. 이번 phase는 `common.json` 양쪽 언어를 채우고, 나머지 11개 도메인 파일은 빈 객체(`{}`)로 스캐폴딩만 해둔다(각 도메인 phase 담당 개발자가 채움).
2. **`components/layout/language-toggle.tsx` 신규**: `theme-toggle.tsx`와 동일한 자체 상태 관리 패턴(자체 컴포넌트, Popover 사용)으로 SCR-COM-015 구현 — Globe 아이콘 버튼, 클릭 시 팝업(한국어/English, 현재 선택 체크 아이콘), `i18next.changeLanguage()` + `language.ts` 저장.
3. **`components/layout/header.tsx`**: `HelpCircle`(가이드)와 `ThemeToggle` 사이에 `LanguageToggle` 삽입(SCR-COM-002 순서). 헤더 자체의 하드코딩 문자열(사용자 가이드/사이드바 토글/통합 검색 placeholder·aria-label/검색 결과 없음/알림 aria-label/모두 지우기/새로운 알림이 없습니다/상세 보기/사용자 메뉴/내 프로필/비밀번호 변경/로그아웃 aria-label 등)을 `useTranslation("common")`의 `t()`로 전환, `common.json`에 `header.*` 키로 채움.
4. **SweetAlert2 도입**: `components/common/toast.ts`·`components/common/confirm-dialog.tsx` 내부 구현 교체(외부 API 불변, SCR-COM-009 구현 참고 그대로). `ConfirmDialog`의 기본 `confirmLabel="확인"`/`cancelLabel="취소"`도 `common:dialog.confirm`/`common:dialog.cancel` 키로 전환(호출부가 명시적으로 넘기지 않는 경우의 폴백). 커스텀 CSS 클래스는 기존 시맨틱 토큰만 참조(`index.css` 또는 신규 CSS 파일에 추가, 형식은 dev-ui 재량).
5. **`components/common/user-guide-content.tsx` + 영어 콘텐츠**: `docs/01_analyze/feature/user-guide-content.en.md` 신규 작성(원문 `user-guide-content.md`, 258줄과 절·아코디언 구조 1:1 동일하게 번역, 가공 없이 그대로). 이후 `user-guide-content.tsx`에 영어 콘텐츠 세트를 추가하고 `i18n.language`(또는 `useTranslation`의 `i18n`)에 따라 `UserGuideOverview`/`UserGuideDomainSection`/`UserGuideRoleSection`가 언어별 세트를 선택하도록 전환.
6. 위 자신의 컴포넌트(`header.tsx`/`language-toggle.tsx`/`toast.ts`/`confirm-dialog.tsx`/`user-guide-content.tsx`) 안에서 사용하는 모든 `common.json` 키는 dev-ui가 직접 채운다.

### dev-fe 담당 범위

1. **`routes/AppLayout.tsx`**: 6.4절 표 그대로 전환 — `ticketTypeApprovalLabel()` 결과·`"자산 만료"`·`formatRelativeTime`의 4개 상대시간 문자열을 `common:notification.*` 키(`t()` + `defaultValue`)로 치환. `useTranslation("common")` 훅 사용.
2. **`features/common/status.ts`**: `ticketTypeApprovalLabel(t: TFunction, ...)` 형태로 6.3절 전환 패턴 적용(`t` 인자를 받아 `t(\`notification.domainLabel.${ticketType}\`, { ns: "common", defaultValue: 기존값 })`). 호출부(`AppLayout.tsx`, `ApprovalInboxPage.tsx` 등) 모두 `t` 전달하도록 수정.
3. **`features/common/ApprovalInboxPage.tsx`**(SCR-COM-014), **`features/search/SearchResultsPage.tsx`/`features/search/status.ts`**(SCR-COM-011), **`routes/DashboardPage.tsx`**(SCR-COM-013), **`routes/ForbiddenPage.tsx`**(SCR-COM-006)/**`routes/NotFoundPage.tsx`**(SCR-ERR-404), 세션 만료 토스트 문구("세션이 만료되어 다시 로그인해주세요", `AuthGuard.tsx`/`SessionBridge.tsx`/apiClient 인터셉터 위치 확인 후 처리) — 하드코딩 한국어 문자열을 `useTranslation("common")`+`t()`(6.4절에 없는 키는 위 "파일 충돌 방지" 규칙대로 제안)로 전환.
4. **`features/guide/GuidePage.tsx`**: 페이지 chrome(문서 헤더 타이틀 "사용자 가이드", TOC 3개 링크 라벨, "내 역할" 배지 라벨 등, 본문 콘텐츠 자체는 dev-ui 소관)을 번역 키로 전환.
5. 위 항목에서 필요한 신규 키(6.4절 미포함분)는 한국어 원문+영어 번역 후보와 함께 SendMessage로 dev-ui에게 전달, dev-ui가 `common.json` 반영 후 회신하면 코드의 `defaultValue`는 그대로 두어도 무방(실제 값은 json이 우선).

### 완료 기준(공통 phase)
- 헤더에 지구본 아이콘이 "?"와 테마 토글 사이에 노출되고, 클릭 시 한국어/English 팝업에서 전환 시 새로고침 없이 화면 텍스트가 즉시 바뀐다.
- 새로고침/재방문 시 `itsm-language` 저장값이 유지된다(기본값 한국어).

## 상태 전이 버튼 라벨·타임라인 actor 공통 아키텍처 + 승인 대기함 상세보기 버튼 (유지보수 요청, 2026-07-16)

### 설계 근거
- `docs/02_plan/screen/common.md` v0.16 SCR-COM-008(상태 전이 버튼 라벨 아키텍처·타임라인 actor 아키텍처), SCR-COM-014(상세보기 버튼).
- `docs/00_context/glossary.md` "전이 버튼 라벨(Transition Button Label)".

### 공통 아키텍처(참고용 — 실제 구현은 각 도메인 phase에서 수행)
1. **전이 버튼 라벨(동작 동사형)**: SRM/INCIDENT/PROBLEM/CHANGE/VULNERABILITY/ASSET/ESM 7개 도메인의 `features/{domain}/status.ts`에 `transitionLabel(t, target)` 신규 함수 추가(기존 `statusLabel`은 배지·전이 완료 토스트용으로 시그니처·용도 변경 없이 그대로 유지 — 별도 함수로 분리). i18n 키 `{ns}:transition.{target}`. 각 도메인 상세 화면의 전이 버튼 텍스트만 `statusLabel`→`transitionLabel`로 교체. 구체 라벨 값은 각 도메인 `docs/02_plan/screen/{domain}.md`의 매핑표 참고. 구현 상세는 각 도메인 `docs/03_develop/plan/{domain}.md` 참고.
2. **타임라인 actor + 코드→라벨**: SRM/ESM(부서요청만)/INCIDENT 3개 도메인만 대상(이 3개 도메인만 공통 `Timeline` 컴포넌트 사용). BE는 각 도메인 상태 enum에 `label()` 메서드 추가하고 `TimelineEntry` DTO에 `actor` 필드 추가(`appUserRepository.findByEmail(event.getCreatedBy())`로 이름 resolve, 실패 시 email 폴백), `STATUS_*` 타임라인 메시지의 `target.name()`을 `target.label()`로 교체. FE는 각 도메인 상세 페이지의 `timelineItems` 매핑에 `actor: entry.actor` 추가(공통 `Timeline`/`TimelineItem`은 이미 `actor` prop 지원 — `components/common/timeline.tsx` 변경 없음). 구현 상세는 `docs/03_develop/plan/service-request.md`/`esm.md`/`incident.md` 참고.

### 담당 범위(이 phase, common 소유 파일)

#### FE (dev-fe) — `source/frontend/src/features/common/ApprovalInboxPage.tsx`
- 목록 표 각 행에 "상세보기" 버튼 추가(기존 승인/반려 처리용 "상세" 버튼과 별개 위치·동작). 클릭 시 `ticketDetailPath(item.ticketType, item.ticketId)`(같은 디렉토리 `status.ts`의 기존 헬퍼, 헤더 알림 드롭다운과 동일 재사용)로 `navigate`. 목록 행 데이터(API-COM-003 응답)에 `ticketType`/`ticketId`가 이미 있으므로 신규 API·타입 변경 없음.

### 완료 기준
- 승인 대기함 목록의 각 행에서 "상세보기" 클릭 시 승인 처리 모달이 아니라 해당 티켓의 실제 상세 화면으로 이동한다.
- 토스트·확인 다이얼로그가 SweetAlert2로 렌더링되며 기존 호출부(83개+) 수정 없이 동작, 라이트/다크 테마에 맞춰 스타일이 반영된다.
- `Modal`(`components/common/modal.tsx`)은 변경되지 않고 기존 Radix Dialog 그대로 유지된다.
- `/guide` 진입 시 언어 전환에 따라 본문(11개 도메인+16개 역할 포함)이 영어로도 정상 렌더링된다.
- 날짜/숫자 포맷은 언어 전환과 무관하게 기존 `ko-KR` 그대로 유지된다(회귀 없음).

## 테스트 관점 (v1, 참고)

- 알림 없음(모든 역할 대기 0건) → 벨 뱃지 숨김, 팝오버 열면 빈 상태 문구.
- 승인 대기/자산 만료 혼합 시 순서(서비스요청→변경→자산)·8건 상한·40자 truncate 확인.
- "상세 보기" 클릭 시 개별 상세 경로로 이동 및 팝오버 닫힘.
- 역할별(승인자 아님/자산관리자 아님) 팝오버 항목 노출 제한 확인.

---

## v2 — 알림 라인 2줄 레이아웃(제목·상대 시간)

### 설계 근거

- `docs/02_plan/screen/common.md` SCR-COM-002 v0.4(REQ-COM-001/FEAT-COM-001) — 각 알림 라인을 2줄로 변경: 1행(도메인 라벨 + 우측 시간/만료 표시), 2행(제목, 40자 초과 시 truncate).
- API 응답 필드 추가(신규 API 아님):
  - `docs/02_plan/api_spec/service-request.md` API-SRM-012: `catalogItemName` 추가.
  - `docs/02_plan/api_spec/change.md` API-CHG-007: `summary`, `createdAt` 추가.
  - API-ITAM-001은 기존 필드(`name`, `expiryDate`)로 충분, 변경 없음.

### 스펙

1. **1행**: 좌측 도메인 라벨(Lozenge, 기존과 동일: "서비스요청 승인"/"변경 승인"/"자산 만료") + 우측 시간 표시.
   - 서비스요청·변경: `requestedAt`/`createdAt` 기준 상대 시간 — 60초 미만 "방금 전", 60분 미만 "N분 전", 24시간 미만 "N시간 전", 7일 미만 "N일 전", 7일 이상은 절대 날짜(목록 화면과 동일 포맷, 예: `2026-08-10`). 계산 기준 시각은 팝오버를 연 시점(1회 계산, 실시간 갱신 불필요).
   - 자산 만료: `{expiryDate} 만료`(상대 시간 아님, 목록 화면과 동일 날짜 포맷 그대로 표시).
2. **2행**: 제목만, 40자 초과 시 말줄임표.
   - 서비스요청: `catalogItemName`
   - 변경: `summary`
   - 자산: `name`
3. 그 외(정렬·8건 상한·뱃지 카운트·상세 보기 이동 경로·빈 상태·트리거/닫힘·크기 320px 등)는 v1과 동일하게 유지.

### 담당 범위

#### dev_be — `source/backend/src/main/java/com/itsm/srm/application/ServiceRequestService.java`, `source/backend/src/main/java/com/itsm/change/application/ChangeService.java`

- `ServiceRequestService.pendingApprovals()`의 `PendingApprovalResponse`에 `catalogItemName` 필드 추가 — 이미 존재하는 `catalogName(Long)` 헬퍼(L437 부근, `catalogItemRepository.findById(id).map(ServiceCatalogItem::getName)`)를 `sr.getCatalogItemId()`에 적용해 채움.
- `ChangeService.pendingApprovals()`의 `PendingChangeApprovalResponse`에 `summary`(`c.getSummary()`), `createdAt`(`a.getCreatedAt()`, 이미 스트림 내 `a` 변수로 접근 가능) 필드 추가.
- 두 DTO(레코드로 추정) 필드 추가에 따른 기존 단위 테스트(`ServiceRequestServiceTest` 등) 갱신.
- API 응답 계약 변경이므로 `docs/02_plan/api_spec/service-request.md`(API-SRM-012)·`docs/02_plan/api_spec/change.md`(API-CHG-007)는 designer가 이미 갱신 완료 — 응답 필드명 그대로 구현하면 됨.

#### dev-ui — `source/frontend/src/components/layout/header.tsx`

- 알림 항목을 2줄 레이아웃으로 변경: 1행(`StatusBadge` 도메인 라벨 + 우측 시간/만료 텍스트, 우측 정렬), 2행(제목, truncate) + 우측 또는 하단 "상세 보기" 버튼(기존 배치 유지해도 무방, 2줄 구조에 맞게 재배치는 dev-ui 재량).
- `HeaderNotificationItem` 타입에 시간/만료 표시 문자열 필드 추가 필요(예: `timeLabel: string`) — FE가 상대 시간/절대 날짜 계산까지 마쳐서 문자열로 전달하는 방식 유지(v1과 동일하게 UI는 계산 로직 없이 표시만 담당).
- 제목(`text`)은 v1처럼 이미 조합된 문구가 아니라 순수 제목 문자열이 전달됨 — truncate 처리(40자)는 v1과 동일하게 FE에서 처리하거나 UI CSS truncate로 안전망 적용.

#### dev_fe — `source/frontend/src/routes/AppLayout.tsx`

- 알림 항목 조립 시 제목 필드를 `catalogItemName`/`summary`/`name`으로 교체(기존 "티켓키 · 요청자 승인 요청" 조합 문구 제거).
- 상대 시간/절대 날짜 계산 유틸 작성(팝오버 오픈 시점 `now` 1회 캡처 — 벨 클릭 핸들러 또는 알림 목록 계산 시점에 `Date.now()` 캡처해 재사용, 목록 로딩 시점과 팝오버 오픈 시점이 분리되어 있다면 팝오버 오픈 시점 기준으로 재계산):
  - 60초 미만 "방금 전" / 60분 미만 "N분 전" / 24시간 미만 "N시간 전" / 7일 미만 "N일 전" / 7일 이상 절대 날짜(기존 날짜 포맷 유틸 재사용, 예: `features/search/format.ts` 또는 자산 목록 화면에서 쓰는 포맷터 확인).
  - 자산 만료 항목은 상대 시간 계산 없이 `{expiryDate} 만료` 문자열 그대로 구성(자산 목록 화면의 날짜 표시 포맷과 동일하게).
- `srmApi`/`changeApi` 응답 타입에 `catalogItemName`/`summary`/`createdAt` 필드 반영(BE 배포 후 타입 갱신 필요 — dev_be 작업 완료 확인 후 진행).

### 테스트 관점 (v2, 참고)

- 1행: 도메인 라벨 + 시간 표시 정확성(방금 전/N분 전/N시간 전/N일 전/절대 날짜 경계값 확인은 코드 리뷰 위주로, seed 데이터로 전 구간 실증은 제한적일 수 있음).
- 2행: 제목이 카탈로그 항목명/summary/자산명으로 정확히 표시되는지, 40자 초과 시 truncate.
- 자산 항목 우측 표시가 "{expiryDate} 만료" 형식인지(상대 시간 아님).
- 기존 v1 통과 항목(정렬·8건 상한·뱃지·이동·빈 상태·크기·라인 클릭) 회귀 확인.

---

## 사용자 가이드 모달 (SCR-COM-012, 신규 화면)

### 설계 근거

- `docs/02_plan/screen/common.md` SCR-COM-012(2026-07-11, v0.5) — REQ-COM-002/FEAT-COM-002.
- 콘텐츠 원문: `docs/01_analyze/feature/user-guide-content.md`(개요 1문단, 11개 업무 도메인 목적/원칙 표, 16개 역할 페르소나/수행 내용 표) — 그대로 옮겨 쓰면 됨.
- 신규 API 없음. 전부 정적 콘텐츠(FE/UI 정적 데이터). 역할 판별만 로그인 사용자의 기존 역할 정보(`auth` 스토어의 `user.roles`, 이미 로드되어 있음) 사용.

### 스펙

1. **트리거**: 헤더 "?" 아이콘 버튼. 배치 순서: 통합검색 - "?" - 테마 토글 - 알림 벨 - 사용자 메뉴(테마 토글 왼쪽에 삽입).
2. **구조**: 대형 모달(`components/common/modal.tsx` 재사용, Overlay elevation). 상단 타이틀 "사용자 가이드"+닫기(X) / 탭 3개("개요"/"도메인 및 원칙"/"역할별 수행 내용과 방법") / 탭별 콘텐츠(세로 스크롤).
3. **개요 탭**: 콘텐츠 문서 1절 문단 그대로 표시.
4. **도메인 및 원칙 탭**: 11개 도메인 아코디언(도메인명+목적+핵심 원칙), 역할 무관 항상 전체 노출, 기본 전부 접힘, 클릭 시 개별 펼침.
5. **역할별 수행 내용과 방법 탭**: 16개 역할 아코디언(역할명+페르소나+수행 내용). 로그인 사용자가 보유한 역할은 최상단에 "내 역할"(Info tone) 배지와 함께 기본 펼쳐진 상태로 고정, 나머지는 그 아래 접힌 상태로 나열(클릭 시 펼침). 역할 미보유(이론상 없음)여도 16개 전체 접힌 상태로 노출.
6. **동작**: "?" 클릭 시 모달 오픈(기본 탭="개요"). 배경 클릭/Esc/닫기 버튼으로 닫힘. 탭 전환 시 서버 재조회 없음(정적).

### 담당 범위

#### dev-ui — 신규 컴포넌트 `source/frontend/src/components/common/user-guide-modal.tsx` + `source/frontend/src/components/layout/header.tsx`

- Tabs/Accordion용 Radix 프리미티브가 아직 없음(`@radix-ui/react-tabs`, `@radix-ui/react-accordion` 미설치, `components/ui/`에 `tabs.tsx`/`accordion.tsx` 없음) — 신규 설치 및 `components/ui/tabs.tsx`, `components/ui/accordion.tsx` 프리미티브 추가 필요(다른 shadcn류 프리미티브와 동일한 패턴).
- `user-guide-modal.tsx`: `docs/01_analyze/feature/user-guide-content.md`의 개요 문단·11개 도메인 표·16개 역할 표를 정적 데이터(상수)로 옮겨 하드코딩. `myRoles?: string[]` prop을 받아 "역할별 수행 내용과 방법" 탭에서 보유 역할을 상단 고정 노출("내 역할" `StatusBadge` tone="info")·기본 펼침 처리.
- `header.tsx`: 테마 토글 왼쪽에 "?" 아이콘 버튼(`aria-label`="사용자 가이드") 추가, 클릭 시 `user-guide-modal.tsx` 오픈. 모달 오픈 상태는 header 내부에서 자체 관리(알림 팝오버처럼 FE 콜백 불필요 — 정적 콘텐츠라 FE 개입 필요 없음).
- `HeaderProps`에 `myRoles?: string[]` 추가(FE가 로그인 사용자 역할 배열을 그대로 전달).

#### dev_fe — `source/frontend/src/routes/AppLayout.tsx`

- `AppShell`의 `header` prop에 `myRoles: user?.roles` 한 줄 추가(이미 `useAppSelector((s) => s.auth.user)`로 로드된 값 재사용, 신규 조회 없음).

### 테스트 관점 (참고, tester 담당)

- "?" 클릭 시 모달 오픈, 기본 탭="개요".
- "도메인 및 원칙" 탭: 11개 도메인 아코디언 전체 노출(역할 무관), 개별 펼침/접힘 동작.
- "역할별 수행 내용과 방법" 탭: 로그인 사용자 보유 역할이 상단에 "내 역할" 배지+기본 펼침으로 고정 노출, 나머지 역할은 접힌 상태로 하단 나열 및 개별 펼침 가능(역할이 여러 개인 계정, 1개인 계정 각각 확인).
- 배경 클릭/Esc/닫기 버튼으로 모달 닫힘.
- 탭 전환 시 네트워크 요청 없음(정적 콘텐츠).

---

## 사용자 가이드: 모달 → 전용 화면 전환 (SCR-COM-012 v0.8)

> 위 "사용자 가이드 모달" 섹션(모달 방식)은 폐기되고 전용 화면(`/guide`)으로 대체된다. 기존 `UserGuideModal`/헤더 `myRoles` 연동을 아래 내용으로 교체한다.

### 설계 근거

- `docs/02_plan/screen/common.md` SCR-COM-012 v0.8(REQ-COM-002/FEAT-COM-002). designer 확정 사항:
  1. "개요"(1절)만 아코디언 없이 원문 그대로 순차 Markdown 렌더링. "도메인 및 원칙"(2절)·"역할별 수행 내용과 방법"(3절)은 기존 모달과 동일하게 구조화된 아코디언 리스트(항목 내부 텍스트만 Markdown 인라인 서식).
  2. `docs/01_analyze/feature/user-guide-content.md`(v0.2, 서술형) 내용을 가공 없이 그대로 frontend 소스로 이관(빌드 스크립트로 docs 복사하는 방식 아님).
  3. 좌측 TOC는 3개 링크(개요/도메인 및 원칙/역할별 수행 내용과 방법)만 존재, 도메인/역할 개별 하위 링크 없음.
- Confluence 문서 페이지 스타일: 상단 문서 헤더(제목, `heading.large`) + 좌측 sticky TOC + 우측 본문(최대 폭 제한, `heading.medium`/`heading.small` 계층). 본문은 카드가 아닌 페이지 배경 위 텍스트(`--card` 미사용, `--foreground`만).
- 신규 API 없음.

### 담당 범위

#### dev-ui — `source/frontend/src/components/common/`(신규/리팩터), `source/frontend/src/components/layout/header.tsx`

1. 기존 `user-guide-modal.tsx`(`UserGuideModal`)를 모달이 아닌 페이지 임베드용 프레젠테이션 컴포넌트로 리팩터(파일명 변경 가능, 예: `user-guide-content.tsx`). 다음 3개를 개별 export하는 구조를 제안(재량껏 조정 가능):
   - `UserGuideOverview` — `user-guide-content.md` 1절(개요) 원문을 Markdown 렌더링(H2/문단/굵게). **2·3절 텍스트는 포함하지 않음.**
   - `UserGuideDomainSection` — 11개 도메인 아코디언(기존 로직 재사용, 콘텐츠는 v0.2 서술형 문구로 갱신 — 표 형식이 아니라 문단+**핵심 원칙** 굵게 포함).
   - `UserGuideRoleSection` — 16개 역할 아코디언(`myRoles?: string[]` prop, "내 역할" 상단 고정+기본 펼침 로직은 기존과 동일 유지). 콘텐츠는 v0.2의 페르소나+구체적 메뉴/버튼 서술형으로 전면 갱신(`docs/01_analyze/feature/user-guide-content.md` 3절 그대로 옮김, 임의 축약 금지).
   - 항목 내부 텍스트에 포함된 `**굵게**` 등 인라인 서식은 Markdown 렌더링 필요(개요와 동일한 렌더러 재사용 권장).
2. Markdown 렌더링 라이브러리 신규 도입 필요(예: `react-markdown`, 라이브러리 선택은 재량).
3. `components/ui/tabs.tsx`(Radix 신규 설치분)는 이전 모달의 탭 UI 전용이었고 신규 화면(TOC+아코디언)에서는 탭이 없어 더 이상 필요 없을 가능성이 높음 — 다른 곳에서 쓰이지 않는다면(grep으로 확인) 제거해 오브젼(orphan) 코드로 남기지 않는다. `accordion.tsx`는 계속 재사용.
4. `header.tsx`: "?" 버튼의 `onClick={() => setGuideOpen(true)}` 및 `UserGuideModal` 렌더링(L295), `guideOpen` state, `myRoles` prop을 전부 제거. 대신 `onOpenGuide?: () => void` 콜백 prop을 추가해 "?" 클릭 시 호출(다른 헤더 아이콘의 navigate 콜백 패턴과 동일, 예: `onProfile`).

#### dev_fe — 신규 `source/frontend/src/features/guide/`, `source/frontend/src/routes/index.tsx`, `source/frontend/src/routes/AppLayout.tsx`

1. 신규 라우트 `/guide` 등록(`routes/index.tsx`, 인증 필요 영역 — 다른 보호 라우트와 동일하게 `AppLayout` 하위 children에 추가) 및 신규 페이지 컴포넌트 `features/guide/GuidePage.tsx`(신규 디렉토리라 `CLAUDE.md` 함께 생성).
2. `GuidePage.tsx` 구성: 상단 문서 헤더("사용자 가이드" 타이틀) / 좌측 sticky TOC(3개 링크, `IntersectionObserver` 등으로 스크롤 위치에 따라 활성 링크 강조, 클릭 시 해당 섹션으로 스크롤) / 우측 본문(`UserGuideOverview` → `UserGuideDomainSection` → `UserGuideRoleSection myRoles={user?.roles}` 순서로 배치, 최대 폭 제한 컨테이너).
3. `AppLayout.tsx`: `header` prop에서 `myRoles` 제거, `onOpenGuide: () => navigate("/guide")` 추가(다른 `onProfile` 등과 동일 패턴).
4. `user.roles`는 `GuidePage.tsx`에서 직접 `useAppSelector`로 조회해 `UserGuideRoleSection`에 전달(더 이상 header를 경유하지 않음).

### 테스트 관점 (참고, tester 담당)

- 헤더 "?" 클릭 시 `/guide`로 라우팅 이동(모달 아님), 뒤로가기로 이전 화면 복귀.
- 상단 문서 헤더("사용자 가이드") 노출, 진입 시 최상단(개요)부터 표시.
- 좌측 TOC 3개 링크 클릭 시 해당 섹션 스크롤 이동, 스크롤에 따라 활성 링크 강조.
- "개요" 섹션: 원문(1절) 그대로 Markdown 렌더링(굵게 등 서식 적용) 확인.
- "도메인 및 원칙" 섹션: 11개 아코디언 전체 노출(역할 무관), 기본 전부 접힘, 개별 펼침, v0.2 서술형 문구로 갱신 확인.
- "역할별 수행 내용과 방법" 섹션: 로그인 역할이 상단 "내 역할" 배지+기본 펼침 고정, 나머지 접힘, v0.2 페르소나/구체 메뉴·버튼 서술형 문구 확인.
- 사용자 가이드 진입 시 네트워크 요청 없음(정적 콘텐츠) 확인.

---

## 알림 확인처리 (모두 지우기·개별 X, 유지보수 요청, 2026-07-11)

> Main 요청(유지보수). common 도메인 첫 번째 백엔드 API(`notification_dismissal` 테이블·API-COM-001/002 신규). UI 신규 소집 없이 FE가 `header.tsx` 변경까지 직접 담당(Main 지시, 기존 알림 팝오버 마크업 재사용 범위 내).

### 설계 근거

- DB: `docs/02_plan/database/common.md` 4절 `notification_dismissal`(append-only, UNIQUE(user_id, notification_type, source_id))
- API: `docs/02_plan/api_spec/common.md` API-COM-001(확인처리, 개별/일괄 공용)·API-COM-002(확인처리 이력 조회) — common 도메인 최초 API 명세서
- 화면: `docs/02_plan/screen/common.md` SCR-COM-002 "모두 지우기"·개별 X 버튼(상태·인터랙션 절 "알림 확인처리(신규...)" 문단)
- 참고 기존 코드: `source/frontend/src/routes/AppLayout.tsx`(알림 조립 로직, 위 v1/v2 섹션 참고), `source/frontend/src/components/layout/header.tsx`(알림 팝오버 마크업), `source/backend/src/main/java/com/itsm/common/`(공통 백엔드 모듈 — 이번이 common 도메인 백엔드 최초 구현이므로 새 하위 패키지 구성 필요, `auth` 도메인 4계층 구조를 그대로 따른다)

### 담당 범위

#### DB (dev-database) — `source/db/sql/`

- 신규 파일 `25_common_notification_dismissal.sql`(auth 쪽 `24_auth_menu_columns.sql` 다음 순번). `CREATE TABLE notification_dismissal`(`database/common.md` 4절 컬럼 그대로: id/user_id/notification_type/source_id/dismissed_at/created_by/created_at, `updated_*`/`is_deleted` 없음) + `UNIQUE(user_id, notification_type, source_id)` + FK(`user_id` → `app_user.id`) + 조회 인덱스(`user_id, notification_type, source_id`).
- 시드 데이터 없음(사용자 조작으로만 채워지는 테이블).
- 완료 후 `source/db/sql/CLAUDE.md`에 파일 추가 사실 반영.

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/common/`

- common 도메인 백엔드 최초 구현이므로 `auth` 패키지의 4계층 구조(application/domain/infrastructure/presentation)를 그대로 따라 `common` 패키지 하위에 신설(기존 `common/ticket/` 등과는 별개로 `common/notification/` 하위 패키지 신설 권장 — 재량).
- 엔티티: `NotificationDismissal`(append-only, `BaseEntity` 미상속 — `RefreshToken`/`AuditLog` 패턴과 동일하게 id/userId/notificationType/sourceId/dismissedAt/createdBy/createdAt만 직접 필드로 선언).
- 리포지토리: `NotificationDismissalRepository`(도메인 인터페이스) + JPA 구현체. 메서드: `existsByUserIdAndNotificationTypeAndSourceId`, `findByUserId`(정렬은 `dismissedAt` 무관 — API-COM-002는 정렬 규정 없음, 등록 편의상 `id` 오름차순 정도면 충분).
- DTO: `DismissNotificationsRequest`(items: List\<Item\>, Item{notificationType, sourceId}), `DismissResultResponse`(dismissedCount), `NotificationDismissalResponse`(notificationType, sourceId, dismissedAt), `NotificationDismissalListResponse`(items).
- 서비스 `NotificationDismissalService`: `dismiss(userId, items)` — 각 item에 대해 `existsBy...`로 이미 처리된 항목은 건너뛰고(멱등), 신규만 저장, 저장 건수를 `dismissedCount`로 반환. `principal.userId()`는 `SecurityUtils.currentPrincipal()`에서 조회(Request Body에 userId 없음 — API 명세 그대로). `list(userId)` — 로그인 사용자 전체 이력 조회.
- 컨트롤러 `presentation/NotificationDismissalController.java`: `POST /api/v1/notifications/dismissals`(items 1개 이상 검증, 0개면 400 `VALIDATION_ERROR`), `GET /api/v1/notifications/dismissals`. `/admin/**` 매처 밖이라 인증만 요구(`SecurityConfig` 추가 변경 불필요 — 이미 `anyRequest().authenticated()`로 커버).
- 신규 `ErrorCode` 불필요(기존 `VALIDATION_ERROR` 재사용). 감사 로그 기록 불필요(확인처리는 감사 대상 이벤트가 아님 — `audit_log`의 `EventType`에도 해당 유형 없음).

#### FE (dev-frontend) — `source/frontend/src/`

- 신규 `features/common/`(디렉토리 최초 생성이므로 `CLAUDE.md` 함께 작성) — `api.ts`(`commonApi.dismissNotifications(items)`/`commonApi.listDismissals()`), `types.ts`(`NotificationType`, `DismissalItem` 등). `docs/02_plan/api_spec/common.md`가 계약 기준.
- `routes/AppLayout.tsx`: 알림 조립 로직(기존 `load()` 함수, 서비스요청/변경 승인·자산 만료 소스 수집부) 앞단에 `commonApi.listDismissals()` 결과를 조회해 `(notificationType, sourceId)` 매칭 항목을 후보에서 제외한 뒤 상위 8건을 구성하도록 수정. 뱃지 카운트(`count`)도 확인처리된 항목만큼 제외한 값으로 계산(각 소스 전체 건수에서 확인처리 이력과 매칭되는 건수를 뺀 값 — 서비스요청/변경은 개별 매칭, 자산은 `expiringWithinDays` 전체 건수 기준이라 근사 계산 필요, 화면 설계서 문구("확인처리된 알림은 제외한 전체 건수 합계") 그대로 구현).
- "모두 지우기"/개별 X 클릭 핸들러 신규 작성: 클릭 시 `commonApi.dismissNotifications(items)` 호출(모두 지우기=현재 표시 중 상위 8건 전체, 개별 X=해당 1건) → 성공 시 로컬 state에서 즉시 제거 + 카운트 차감(서버 재조회 없이 낙관적 업데이트, 화면 설계서 "성공 시 목록을 즉시 비워" 문구에 맞춤). 실패 시 토스트 오류.
- `components/layout/header.tsx` 직접 수정(이번 유지보수는 UI 미소집, Main 지시): 팝오버 헤더 우측 상단에 "모두 지우기" 텍스트 버튼 추가(`aria-label`="모든 알림 확인처리"), 각 알림 라인 1행 우측(시간 표시 옆)에 개별 X 아이콘 버튼 추가(`aria-label`="알림 확인처리", 클릭 시 `e.stopPropagation()`으로 라인 클릭·상세보기 이동과 분리). `HeaderNotificationItem`에 `sourceId`/`notificationType`(또는 기존 `key` 재사용) 식별자 필드 추가해 개별 확인처리 대상 특정. `HeaderProps`에 `onDismissAllNotifications?: () => void`, `onDismissNotification?: (item: HeaderNotificationItem) => void` 콜백 추가. 확인 다이얼로그 불필요(파괴적 동작 아님, 화면 설계서 명시).
- 라우팅/신규 화면 없음(기존 헤더 팝오버 확장).

### 진행 순서

1. DB: `notification_dismissal` 테이블 생성 → 먼저 완료.
2. BE: 테이블 확정 후 엔티티/서비스/컨트롤러 구현.
3. FE: API 계약 확정 후 `features/common/` 신설 + `AppLayout.tsx`/`header.tsx` 수정.

### 완료(테스트 통과) 기준

- BE: API-COM-001(개별 1건/일괄 8건, 중복 포함 시 멱등 처리, items 누락 400)·API-COM-002(이력 없음 시 빈 배열) 정상 동작.
- FE: "모두 지우기" 클릭 시 표시 중 알림 전체 확인처리 후 빈 상태 전환·뱃지 차감, 개별 X 클릭 시 해당 1건만 제거(라인 클릭·상세보기와 이벤트 분리 확인), 확인처리된 알림은 재로그인/재조회 후에도 다시 나타나지 않음(영구 저장 확인), 원본 승인 대기·자산 만료 데이터는 변경되지 않음(확인처리가 표시 여부에만 영향).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.

---

## 헤더 알림 5초 polling 전환 (유지보수 요청, 2026-07-11)

> BE/DB 변경 없음(기존 3개 도메인 API + API-COM-002 재사용). FE 단독 작업. UI(`header.tsx`)는 변경 불필요 — 이미 `max-h-80 overflow-auto` 스크롤과 "모두 지우기"/개별 X를 지원하므로 표시 상한 폐지에 마크업 변경이 필요 없다(designer 확인).

### 설계 근거

- `docs/02_plan/screen/common.md` SCR-COM-002 v0.11(2026-07-11) "알림 5초 polling(신규...)" 문단, `docs/02_plan/api_spec/common.md` v0.2(엔드포인트/스키마 변경 없음).
- 참고 기존 코드: `source/frontend/src/routes/AppLayout.tsx`(L150~324 알림 조립 로직 — 위 "알림 확인처리" 섹션에서 구현된 부분).

### 스펙

1. 조회 주기: 세션 인증 유지 중 5초 간격 polling(신규 API 없음, 기존 3개 도메인 API + `commonApi.listDismissals()`를 매 주기 재조회).
2. Page Visibility API로 탭 비활성(백그라운드) 시 polling 정지, 포그라운드 복귀 시 재개.
3. 팝오버가 열려 있어도 polling은 계속 진행.
4. **merge 규칙**: 매 poll마다 새로 계산한 후보 목록(SR→CHG→Asset 순, 확인처리 이력 제외) 중 현재 표시 목록에 없는 항목(key=notificationType+sourceId 기준)만 판별해 **기존 목록 뒤에 추가**한다. 기존에 표시된 항목은 서버 응답에서 더 이상 나오지 않아도 절대 제거하지 않는다 — 제거는 오직 사용자의 개별 X/모두 지우기(기존 구현된 `handleDismissNotification`/`handleDismissAllNotifications`)에서만 발생.
5. 팝오버 "상위 8건" 표시 상한 폐지 — `NOTIFICATION_PREVIEW_SIZE`를 이용한 `slice(0, ...)` 제거. 단, `assetApi.list({ size: 8 })`의 `size=8`은 API-ITAM-001 페이지 크기 파라미터(API 계약, `docs/02_plan/api_spec/asset.md` 무변경)이므로 그대로 유지 — display cap과 API page size를 별도 상수로 분리한다.
6. 벨 뱃지 카운트는 표시 목록 merge와 무관하게 매 poll마다 서버 기준(확인처리 이력 제외 전체 대기 합계)으로 재계산 — 기존 계산 로직(자산은 `totalElements - dismissedInBatch` 근사) 그대로 유지, 재계산 시점만 5초마다로 확장.
7. 연속 조회 실패 시 backoff/중단 없이 5초 고정 간격 재시도 유지 — **최초 로드 실패**는 기존처럼 빈 상태로 초기화하되, **polling 도중 실패**는 현재 표시 상태를 그대로 두고 다음 주기에 재시도(목록을 비우거나 롤백하지 않음).

### 담당 범위

#### dev-frontend — `source/frontend/src/routes/AppLayout.tsx`

- L27-28 `NOTIFICATION_PREVIEW_SIZE` 상수 제거(또는 자산 API 페이지 크기 전용 상수로 이름 변경, 예: `ASSET_EXPIRY_QUERY_SIZE = 8`) — `assetApi.list({ size: ... })` 호출(L223~226)에는 이 값을 그대로 사용하고, L250 `sources.slice(0, NOTIFICATION_PREVIEW_SIZE)` cap은 제거(후보 전체를 그대로 사용).
- L171-279 기존 `useEffect(() => { const load = async () => {...}; load()... }, [roles])`를 polling 구조로 확장:
  - 후보 목록 계산 로직(다움처리 이력 조회 → 역할별 SR/CHG/Asset 소스 조립, L177~247)은 재사용하되 **최초 로드**와 **polling 재조회**를 구분하는 파라미터를 두어, 최초 로드는 결과로 전체 교체, polling 재조회는 위 4번 merge 규칙(key 기준 신규 항목만 append) 적용.
  - `notificationHrefByKey`/`notificationDismissTargetByKey` 맵은 매번 `notificationSources.current`(merge 후 전체) 기준으로 재구성.
  - `setInterval(5000)`으로 polling 시작, `document.visibilityState`가 `"hidden"`이면 `clearInterval`, `visibilitychange` 이벤트로 `"visible"` 복귀 시 즉시 1회 재조회 후 interval 재개. 언마운트 시 interval·리스너 정리.
  - polling 재조회 실패 시(catch) 상태를 초기화하지 않고 조용히 skip(다음 5초 주기에 자동 재시도) — 최초 로드 실패 시의 기존 reset-to-empty 로직(L265~273)은 그대로 유지.
  - `handleDismissAllNotifications`/`handleDismissNotification`(L293~324)은 변경 불필요(이미 로컬 state에서 즉시 제거하는 낙관적 업데이트 — polling과 충돌하지 않음, 다음 poll의 후보 목록도 `listDismissals()`로 이미 걸러짐).
- `header.tsx`는 변경하지 않는다(설계 확인 — 이미 스크롤·무제한 리스트 렌더 지원).

### 완료(테스트 통과) 기준

- 5초 간격으로 승인 대기/자산 만료 알림이 자동 갱신(신규 승인 대기 생성 후 5초~10초 내 팝오버에 미조작으로 반영)되는지 확인.
- 탭을 백그라운드로 전환하면 polling이 멈추고(네트워크 탭 등에서 요청 정지 확인), 포그라운드 복귀 시 재개되는지 확인.
- 팝오버가 열려 있는 상태에서도 새 알림이 추가되는지(merge) 확인.
- 이미 표시된 알림이 서버에서 사라져도(예: 다른 경로로 처리) 화면에서 사라지지 않고, 개별 X/모두 지우기로만 제거되는지 확인.
- 팝오버에 9건 이상 누적 시 상한 없이 전체가 스크롤로 노출되는지 확인(8건 cap 폐지).
- 뱃지 카운트가 5초마다 서버 기준으로 갱신되는지, merge된 표시 목록 건수와 뱃지 값이 달라질 수 있는지(정상 동작) 확인.
- API 연속 실패 시 화면이 비워지지 않고 유지되며, 복구 후 정상 반영되는지 확인(가능한 범위 내 재현).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.

## 승인 대상자 역할 기반 동적 상세조회 권한 (유지보수 요청, 2026-07-15)

> Main 요청(유지보수). SRM/CHANGE의 정적 "APPROVER=도메인 전체조회" 권한을 폐지하고, 8개 도메인(SRM/CHANGE/INCIDENT/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM) 상세조회에 공용 동적 판정을 적용한다. UI 신규 소집 없음(FE는 라우트 가드 1줄 추가만).

### 설계 근거

- API: `docs/02_plan/api_spec/common.md` 0-1절(판정 절차)
- 권한: `docs/02_plan/security/authorization/approver.md` v0.3
- 참고 기존 코드: `source/backend/src/main/java/com/itsm/common/approval/application/ApprovalGateService.java`(private `matchProcess` — 도메인/요청유형/요청자역할 3축 매칭 로직 재사용), `source/frontend/src/routes/index.tsx`(`RequireRoles` 가드)
- 8개 도메인 모두 이미 `ApprovalGateService.checkGate(DOMAIN, requestSubtypeKey, requesterIdOf(entity), TT, id)` 패턴으로 게이트를 호출 중이므로(각 도메인 서비스에 `DOMAIN` 상수·`requesterIdOf` 헬퍼 기존 존재), 이번 작업은 그 값들을 그대로 재사용해 조회 가드에 OR 조건만 추가하면 된다.

### 담당 범위

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/common/approval/application/ApprovalGateService.java`

- 신규 public 메서드 `canApproverView(String domain, String requestSubtypeKey, Long requesterId)`: 기존 private `matchProcess(domain, requestSubtypeKey, requesterId)`로 매칭 규칙 1개 조회 → 없으면 false. 매칭되면 그 규칙의 **전체 차수**(`processStepRepository.findByApprovalProcessIdOrderByStepNoAsc`)를 순회하며 각 차수의 `processStepRoleRepository.findByStepId`로 역할 id를 전부 모은다(현재 차수만이 아니라 전체 차수 — 인스턴스 생성 전에도 조회 가능해야 하므로, common.md 0-1절 2번). 로그인 사용자(`SecurityUtils.currentPrincipal().roles()`, role code 집합)를 `roleResolver.roleIdsOf(...)`로 role_id 집합 변환 후 교집합 있으면 true, 없거나 승인자 역할 자체가 0개면 false.
- 8개 도메인 상세조회 서비스에 이 메서드를 **기존 조건과 OR**로 추가한다(각 담당 개발이 실제 코드를 열어 최소 변경으로 적용, 현재 가드 위치):
  | 도메인 | 파일 · 현재 가드 | 적용 방식 |
  |---|---|---|
  | SRM | `srm/application/ServiceRequestService.java` `assertCanView` (`hasAnyRole(AGENT, PROCESS_OWNER, APPROVER)`) | **APPROVER 조건 제거** 후 `\|\| approvalGateService.canApproverView("SERVICE_REQUEST", String.valueOf(sr.getCatalogItemId()), sr.getRequesterId())`로 대체(상세 계획 `docs/03_develop/plan/service-request.md` 참조) |
  | CHANGE | `change/application/ChangeService.java` 상세조회 가드(정적 APPROVER 전체조회 조건) | 그 조건 제거 후 canApproverView(domain="CHANGE", requestSubtypeKey=변경유형 코드, requesterIdOf(change)) OR로 대체 |
  | INCIDENT | `incident/application/IncidentService.java` `detail()`(현재 역할 체크 자체 없음 — 결함) | **신규로** `SecurityUtils.hasAnyRole("SERVICE_DESK_AGENT", "INCIDENT_MANAGER") \|\| approvalGateService.canApproverView("INCIDENT", null, requesterIdOf(inc))` 가드 추가, 불만족 시 403(둘 다 없으면 접근 불가) |
  | PROBLEM | `problem/application/ProblemService.java` 상세조회 가드(PROBLEM_MANAGER 전용) | canApproverView(domain="PROBLEM", requestSubtypeKey=null, requesterIdOf(problem)) OR 추가 |
  | ASSET | `asset/application/AssetService.java` `detail()` | **스킵(코드 변경 없음, designer 확인 완료)** — ASSET 상세조회의 의도된 RBAC는 코드 그대로 "인증된 사용자 전반 허용"이 맞다(`AssetService` 클래스 주석 근거). `approver.md` v0.3이 ASSET을 PROBLEM/VULNERABILITY/COMPLIANCE와 같은 "매니저 전용" 묶음으로 잘못 분류했던 설계 문서 오류였고, designer가 `common.md`(api_spec) 0-1절·`approver.md` 2/3/4절을 정정 완료했다. INCIDENT(근거 없는 결함)와 달리 ASSET은 코드 주석에 의도가 명확해 이번 유지보수에서 신규 제한을 추가하지 않는다(2026-07-15 designer 확인) |
  | VULNERABILITY | `vulnerability/application/VulnerabilityService.java` 상세조회 가드(VULNERABILITY_MANAGER 전용) | canApproverView(domain="VULNERABILITY", requestSubtypeKey=null, requesterIdOf(vulnerability)) OR 추가 |
  | COMPLIANCE | `compliance/application/ComplianceService.java` 상세조회 가드(COMPLIANCE_OFFICER 전용, requirement 단위) | requirement은 자체 requester 개념이 없고 0~n개 CorrectiveAction이 딸려있다(각기 다른 등록자 가능) — 해당 requirement의 action들을 순회하며 각각 `canApproverView("COMPLIANCE", null, requesterIdOf(action))` 호출해 **하나라도 true면 허용**, action이 0건이면 매칭 문맥이 없으므로 false(2026-07-15 확인, dev-lead) |
  | ESM | `esm/application/EsmRequestService.java` `assertCanView`(요청자 본인+DEPT_COORDINATOR) | canApproverView(domain="ESM", requestSubtypeKey=null, esmRequest.getRequesterId()) OR 추가 |
- `requestSubtypeKey`는 각 도메인이 기존 `checkGate` 호출부에 넘기는 값과 동일하게 맞춘다(하위유형 없는 도메인은 null, CHANGE는 변경유형 코드, SRM은 `catalogItemId` 문자열화). **상세조회(단건) API에만 적용**하며 목록 API에는 적용하지 않는다.

#### FE (dev-frontend) — `source/frontend/src/routes/index.tsx`

- SRM/CHANGE/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM **7개 도메인**(INCIDENT 제외 — 백엔드 신규 역할체크로 커버)의 상세 라우트 `RequireRoles` 역할 목록에 `ROLE_APPROVER`를 추가한다(기존 목록에 추가만, 다른 로직 변경 없음). 실제 조회 가능 여부는 백엔드 403으로 최종 판정(매칭 안 되면 화면 진입 후 403 처리, 기존 패턴과 동일).

### 완료(테스트 통과) 기준

- BE: SRM/CHANGE는 매칭 안 되는 APPROVER 403(기존 전체조회 회귀 없음), 매칭되면 200. INCIDENT는 SERVICE_DESK_AGENT/INCIDENT_MANAGER 미보유 + 매칭 안 되는 APPROVER 403(결함 정리 확인), 매칭되면 200. PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM은 매칭되는 APPROVER 200, 매칭 안 되면 403(기존 매니저 전용 조건은 그대로 유지).
- FE: 7개 도메인 상세 라우트에 APPROVER 역할 계정으로 내비게이션 가능.
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.

## 개발 계획 — 2026-07-17 유지보수: 동적 폼 빌더·렌더러 공용 아키텍처(form.io)

> SRM 도메인 개발 phase에서 함께 만들어지는 SRM/ESM 공용 산출물이다(SRM `docs/03_develop/plan/service-request.md`가 이 절을 실행 지시로 참조). ESM 도메인 phase에서는 여기서 만든 컴포넌트를 그대로 재사용하며 신규 작업 없음.

- 설계 근거: `docs/02_plan/screen/common.md` 8절(8.1~8.4), `docs/02_plan/api_spec/common.md` 0-2절, `docs/02_plan/database/service-request.md` 1절.
- 참고: `docs/source/form_io/overview.md`·`form-builder.md`·`integration-guide-for-itsm.md`·`component-schema-and-validation.md`.

### 담당 범위

#### UI (dev-ui) — `source/frontend/src/components/common/`

- `package.json`에 `@formio/js`, `@formio/react` 의존성 추가 후 설치(신규 설치임을 dev-lead에 알리고 진행).
- `dynamic-form-builder.tsx`(신규, `field-builder.tsx` 대체): `@formio/react` `FormBuilder` 래핑. `initialForm`(기존 저장된 Form.io Form JSON)으로 편집 모드 진입, `onChange(form)`으로 최신 Form JSON을 상위 상태에 축적만 하고 자동저장하지 않음. 팔레트는 8.2절대로 `options.builder`에서 Basic(textfield/textarea/number/checkbox/selectboxes/select/radio)+Advanced(email/phoneNumber/datetime `enableTime:false`/file `storage:'base64'` 기본 강제)+Layout(columns/panel/tabs/fieldset)만 노출, Data/Premium/Resource는 `false`로 숨김.
- `dynamic-form-renderer.tsx`(신규, `dynamic-form.tsx` 대체): `@formio/react` `Form` 래핑. `src`에 `formSchema` 그대로 주입, `onSubmit(submission)`으로 `submission.data`를 상위 콜백에 전달(클라이언트 검증은 Form.io 내장 사용, 서버 재검증은 BE `FormSubmissionValidator`가 담당하므로 여기선 추가 검증 로직 불필요).
- `form-schema.ts`: 기존 `FormFieldSchema`/`FormFieldType`/`validateForm`/`hasOptions`는 폐기. Form.io Form JSON 타입 별칭(예: `FormIoSchema { display: string; components: unknown[] }`)과 8.2절 팔레트 옵션 상수만 남긴다.
- CSS 스코핑(8.3절): `@formio/js`의 폼 전용 CSS(`formio.form.min.css` 계열, Bootstrap 전체 리셋 아님)만 임포트. 두 컴포넌트의 렌더 최상위를 `.formio-scope` 래퍼로 감싸고, `index.css`에 그 하위 스코프로 버튼 강조색·입력 테두리/라운드·폰트를 ADS 토큰으로 오버라이드하는 규칙을 추가(컴포넌트 구조 자체는 건드리지 않음).
  - **최종 결정(dev-lead, 2026-07-17 확정 — 이후 절대 변경 없음)**: formio 폼 전용 CSS만으로는 `.btn`/`.form-control`/`.card`/`.row`/`.col-*`/tabs(`.nav-tabs`) 등 Bootstrap 클래스 자체가 정의되지 않아 팔레트·Layout 컴포넌트(columns/panel/tabs)가 스타일 없이 깨짐(dev-ui 확인). postcss 기반 전체 스코핑안도 구현·검증했으나, 신규 빌드 의존성·생성 스크립트 없이 8.3절 문자 그대로("전체 Bootstrap CSS 미도입")를 지킬 수 있는 **부트스트랩 핵심 클래스(grid/btn/form-control/card/nav-tabs 등)만 `.formio-scope`/`.formio-dialog`(컴포넌트 편집 모달, body 포털) 스코프 하위에 ADS 토큰으로 최소 재구현**하는 방식(devDependency 추가 없음, 빌드 CSS 108.10KB)을 **최종 확정**한다. 8.2절 팔레트가 Basic+Advanced+Layout으로 이미 범위 고정(Data/Premium/Resource 숨김)이라 실제 렌더링될 Bootstrap 클래스 집합도 유한해 수동 재구현의 누락 위험이 낮다고 판단했다. 전역 CSS에는 영향 없음. playwright로 팔레트 구성(Basic 7종/Advanced 3종/Layout 4종/Premium 1종=file, Data 전체 숨김)·columns 레이아웃·편집 모달 정상 렌더링 재검증 완료.
  - 구현 중 추가로 발견·수정된 결함: (1) `FORM_BUILDER_OPTIONS`에서 노출하려는 컴포넌트만 `true`로 지정하는 것으로는 부족 — Form.io가 그룹별 기본 포함 컴포넌트를 내부 고정값으로 갖고 있어(예: `file`은 Advanced가 아니라 Premium 그룹 소속) 각 그룹의 나머지 기본 항목도 전부 명시적으로 `false` 처리해야 8.2절 팔레트가 정확히 지켜짐. (2) Basic 팔레트에서 `button`을 뺀 설계(8.2절)로 인해 `Form`(Renderer)이 제출 버튼 없는 폼을 렌더링할 수 있는 사각지대 발견 — `dynamic-form-renderer.tsx`에 제출 버튼이 없으면 자동 보강하는 `ensureSubmitButton` 로직 추가(Builder는 Form.io가 내부적으로 자동 추가하므로 대상 아님).
- 기존 `field-builder.tsx`/`dynamic-form.tsx`는 SRM+ESM 화면이 모두 새 컴포넌트로 전환 완료할 때까지 삭제하지 않는다(ESM 도메인 phase 완료 후 정리 — dev-fe에게 전환 완료 시점 확인 후 삭제).
- 신규 파일 추가에 맞춰 `source/frontend/src/components/common/CLAUDE.md` 갱신.

#### BE (dev-be) — `source/backend/src/main/java/com/itsm/common/form/`(신규 패키지)

- `FormSubmissionValidator.java`: 입력은 카탈로그 항목의 `form_schema`(Form.io Form JSON)와 제출된 `formValues` 맵. `components`를 재귀 순회해 `input:true`인 리프만 검증 대상으로 수집(컬럼/패널/탭 등 `input:false` 레이아웃은 하위 `components`만 펼치고 자체는 제외)하고, 각 리프의 `validate` 규칙(`required`/`minLength`/`maxLength`/`min`/`max`/`pattern`)을 적용한다(`docs/source/form_io/component-schema-and-validation.md` 3절 그대로). 위반 시 400(도메인별 API 문서 참조). `conditional`/`calculateValue`/`custom`은 해석하지 않는다(범위 밖).
- SRM `ServiceRequestService.validateRequiredFields()`를 이 클래스 호출로 대체하는 작업은 아래 SRM 절에서 수행(이 패키지 자체는 SRM/ESM 어느 도메인 서비스에도 아직 의존성이 없는 순수 유틸리티로 구현).
- `source/backend/src/main/java/com/itsm/common/form/CLAUDE.md` 신규 생성.

### 완료 기준
- 두 신규 공용 컴포넌트·`FormSubmissionValidator`가 SRM 화면·API(아래 SRM 절)에 통합되어 정상 동작하는 것으로 검증한다(별도 단독 테스트 없음, SRM 도메인 통합 테스트에 포함).

## 추가 요구사항(2026-07-17, SRM 통합테스트 진행 중 접수) — 폼 렌더러 제출/취소 버튼 우측 하단 배치

> 사용자 요청: "제출과 취소 버튼은 양식 우측 하단에 고정해." Main 확인 결과(2026-07-17): 버튼 순서는 "취소 → 제출"(제출이 가장 오른쪽), "고정"은 **sticky 아님** — 폼 맨 아래 우측 정렬만 하면 되고 스크롤 시 폼 내용과 함께 자연스럽게 흘러가면 된다.

- SRM은 이미 개발 완료된 상태라 **레트로핏**(아래 `docs/03_develop/plan/service-request.md` 절 참조), ESM은 아직 미착수라 처음부터 반영(`docs/03_develop/plan/esm.md` FE절에 반영됨).

#### UI (dev-ui) — `source/frontend/src/components/common/dynamic-form-renderer.tsx`

- 현재 구조(Form.io 내장 제출 버튼(폼 내부, `ensureSubmitButton`으로 스키마에 보강) + 화면단이 별도로 렌더링하는 커스텀 "취소" 버튼)를 다음으로 교체한다.
- `DynamicFormRenderer`가 **자체 하단 푸터**(취소 버튼 + 제출 버튼, `flex justify-end gap-2` 우측 정렬, 취소가 왼쪽·제출이 가장 오른쪽)를 렌더링하도록 변경. 제출 버튼은 컴포넌트 내부 Form.io 인스턴스(ref)의 제출을 트리거(schema 자체에 보이는 submit 컴포넌트는 렌더링하지 않도록 숨김 처리 — 화면에 중복 버튼 노출 방지).
- 신규 prop: `onCancel?: () => void`(미지정 시 취소 버튼 숨김), `submitLabel?: string`/`cancelLabel?: string`(미지정 시 기본값 "제출"/"취소" — 도메인별 i18n 텍스트는 호출 측(FE)이 전달).
- sticky 처리 없음 — 폼 콘텐츠 흐름에 자연스럽게 포함되는 일반 레이아웃.
- 완료되면 SRM(dev-fe)·ESM(dev-fe) 양쪽에 새 API(`onCancel` 등) 알려준다.

#### FE — 소비 화면 반영은 아래 SRM/ESM 각 도메인 절 참조.
