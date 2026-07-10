# 개발 계획 — COMMON (SCR-COM-002 헤더 알림 팝오버)

## 설계 근거

- `docs/02_plan/screen/common.md` SCR-COM-002(글로벌 헤더) v0.3(2026-07-11) 확정. 근거: `docs/01_analyze/prd/common.md` REQ-COM-001 / `docs/01_analyze/feature/common.md` FEAT-COM-001. (초안과 내용 동일, REQ/FEAT ID 정식 반영됨)
- 신규 백엔드 API 없음. 기존 3개 API 재사용:
  - `srmApi.listApprovals()` (API-SRM-012)
  - `changeApi.listApprovals()` (API-CHG-007)
  - `assetApi.list({ expiringWithinDays, size })` (API-ITAM-001) — 팝오버 항목 확보를 위해 기존 `size=1`(count 전용) 호출을 `size=8`로 변경.

## 스펙

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

## 테스트 관점 (참고, tester 담당)

- 알림 없음(모든 역할 대기 0건) → 벨 뱃지 숨김, 팝오버 열면 빈 상태 문구.
- 승인 대기/자산 만료 혼합 시 순서(서비스요청→변경→자산)·8건 상한·40자 truncate 확인.
- "상세 보기" 클릭 시 개별 상세 경로로 이동 및 팝오버 닫힘.
- 역할별(승인자 아님/자산관리자 아님) 팝오버 항목 노출 제한 확인.
