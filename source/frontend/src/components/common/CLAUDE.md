# CLAUDE.md

프로젝트 공통 컴포넌트. `ui/`의 프리미티브를 조합해 도메인 무관한 재사용 패턴(상태 표시·목록/상세 셸·차트·폼)을 제공하며, 프레젠테이션만 담당하고 데이터·권한·라우팅은 기능 레이어(FE)가 주입한다. 화면 설계서(common.md SCR-COM-*)의 공통 UX를 구현한다.

## 파일
- `index.ts` — 공통 컴포넌트 배럴. 기능 화면은 이 모듈에서 import 한다.
- `status-badge.tsx` — 상태 배지. tone(success/warning/danger/info/muted) + 라벨을 FE가 주입. `StatusTone` 타입 제공.
- `priority-badge.tsx` — 우선순위 배지(P1~P4 → Danger~Muted). `Priority` 타입 제공.
- `toast.ts` — 토스트 helper(success/error/info). 시맨틱 색상 지정. sonner 래핑.
- `confirm-dialog.tsx` — 파괴적/비가역 동작 확인 다이얼로그. 확인 시에만 `onConfirm` 호출.
- `modal.tsx` — 범용 모달(폼/상세 등 비파괴 콘텐츠). 파괴적 확인은 ConfirmDialog 사용.
- `multi-select.tsx` — 다중 선택(Popover + 체크 리스트). 필터바용. `MultiSelectOption` 타입 제공.
- `pagination.tsx` — 페이지네이션(0-based, 최대 5개 번호 창).
- `data-table.tsx` — 제네릭 데이터 표. 로딩 스켈레톤·빈 상태·행 클릭 지원. `Column<T>` 타입 제공.
- `empty-state.tsx` — 결과 0건 빈 상태 안내.
- `forbidden-view.tsx` — 403 접근 거부 뷰(프레젠테이션). `onBack` 주입.
- `not-found-view.tsx` — 404 Not Found 뷰(프레젠테이션). `onHome` 주입.
- `ticket-list-layout.tsx` — 공통 티켓 목록/필터 셸(타이틀·액션·필터바·목록).
- `ticket-detail-layout.tsx` — 공통 티켓 상세 셸(제목·배지·액션·본문·우측 메타 패널).
- `timeline.tsx` — 이력 타임라인(프레젠테이션). `TimelineItem` 타입 제공.
- `kpi-card.tsx` — 핵심 지표 카드(라벨·수치·단위).
- `trend-chart.tsx` — 기간별 추이 SVG 라인 차트(hover 툴팁). `TrendPoint` 타입 제공.
- `distribution-chart.tsx` — 범주형 세로 막대 분포 SVG 차트. `DistributionDatum` 타입 제공.
- `rating.tsx` — 별점 위젯(읽기 전용/입력). CSAT용.
- `dynamic-form.tsx` — 스키마 기반 동적 폼 렌더러(제어 컴포넌트, 인라인 오류).
- `field-builder.tsx` — 동적 폼 필드 정의 빌더(라벨·유형·필수·옵션).
- `form-schema.ts` — 동적 폼 스키마 계약(`FormFieldSchema`/`FormValues`/`FormErrors`)과 `validateForm`·`hasOptions` 헬퍼.
