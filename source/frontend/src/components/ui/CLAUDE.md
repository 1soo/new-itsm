# CLAUDE.md

shadcn/ui(new-york 스타일) 기반 공통 UI 프리미티브. Radix 위에 디자인 토큰(시맨틱 색상)을 입힌 저수준 컴포넌트로, 색상을 하드코딩하지 않고 `@/lib/utils`의 `cn`으로 클래스를 병합한다.

## 파일
- `accordion.tsx` — 아코디언(Radix Accordion). Item/Trigger(우측 Chevron)/Content, 접기/펼치기 애니메이션.
- `alert-dialog.tsx` — 파괴적 동작 확인용 모달(Radix AlertDialog). Overlay/Content/Header/Footer/Title/Action/Cancel 조합.
- `avatar.tsx` — 아바타(Radix Avatar). 이미지 + 이니셜 Fallback.
- `badge.tsx` — 배지. cva 변형(default/secondary/outline/success/warning/danger/info/muted) 시맨틱 토큰 사용.
- `button.tsx` — 버튼. cva 변형(variant/size), `asChild`(Slot)·`loading`(스피너) 지원.
- `card.tsx` — 카드 컨테이너. Card/Header/Title/Description/Content/Footer.
- `checkbox.tsx` — 체크박스(Radix Checkbox) + Check 인디케이터.
- `dialog.tsx` — 범용 모달(Radix Dialog). Overlay/Content(닫기 버튼 내장)/Header/Footer/Title/Description.
- `dropdown-menu.tsx` — 드롭다운 메뉴(Radix DropdownMenu). Item/CheckboxItem/Label/Separator 등.
- `input.tsx` — 텍스트 입력. `aria-invalid` 시 오류 스타일.
- `label.tsx` — 폼 라벨(Radix Label).
- `popover.tsx` — 팝오버(Radix Popover). Trigger/Content/Anchor.
- `select.tsx` — 셀렉트(Radix Select). Trigger/Content/Item/Label/Separator, 스크롤 버튼 포함.
- `skeleton.tsx` — 로딩 스켈레톤(pulse 애니메이션).
- `sonner.tsx` — 토스트 컨테이너(sonner Toaster). 우상단 배치, `main.tsx`에서 마운트.
- `table.tsx` — 표 프리미티브. Table/Header/Body/Row/Head/Cell/Caption.
- `textarea.tsx` — 여러 줄 입력. `aria-invalid` 시 오류 스타일.
