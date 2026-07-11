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
