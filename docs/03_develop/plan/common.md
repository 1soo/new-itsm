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
