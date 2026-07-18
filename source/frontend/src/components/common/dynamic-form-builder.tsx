import { useRef, useState, type PointerEvent as ReactPointerEvent } from "react";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import {
  AlignLeft,
  Calendar,
  CheckSquare,
  CircleDot,
  File,
  GripVertical,
  Info,
  List,
  Paperclip,
  Settings2,
  Tag,
  Trash2,
  Type,
  X,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { Modal } from "@/components/common/modal";
import { GridComponentBody, GridLabelOverlays, parseOptions } from "@/components/common/dynamic-form-renderer";
import {
  GRID_COLUMNS,
  GRID_PALETTE_TYPES,
  GRID_ROW_HEIGHT_PX,
  gridMaxHeight,
  hasGridOptions,
  hasOptionsLayoutUi,
  hasPlaceholderUi,
  hasRegexUi,
  type GridAlign,
  type GridAlignVertical,
  type GridComponent,
  type GridComponentType,
  type GridFormSchema,
  type GridInputComponent,
  type GridLabel,
  type GridOptionsDirection,
  type GridOptionsGap,
  type GridPosition,
  type GridSize,
} from "@/components/common/form-schema";

/**
 * 관리자용 그리드 폼 빌더 — SCR-SRM-007 "Form 설정" 팝업(form.io 완전 제거 → 자체 8×n
 * 그리드). 팝업 내부는 **좌 팔레트(9종 — 입력 7종 + 값 입력 없는 정적 컴포넌트
 * `guide-text`/`guide-file`) / 우 8칸 그리드 캔버스** 2분할이다. **캔버스=미리보기 통합**:
 * 각 캔버스 카드가 `GridComponentBody`를 그대로 렌더링해 실제 렌더링 모습을 직접 보여준다
 * (순수 시각적 표현·상호작용 불가). 팔레트 항목은 클릭(첫 빈 칸 자동 배치)과 드래그앤드롭
 * (1칸 스냅 미리보기, 겹치면 차단+경고, 캔버스 밖 드롭 시 취소) 배치를 모두 지원한다
 * (2026-07-18 유지보수 요청 4차, 네이티브 Pointer Events 기반·신규 라이브러리 없음). 캔버스는
 * 스크롤 가능, 배치·리사이즈는 1칸 단위 스냅. 컴포넌트 높이 상한은 유형별로 세분화됐다
 * (`gridMaxHeight`, text/date/file/select/guide-file은 1칸 고정, radio/checkbox/guide-text는
 * 1~2칸, textarea는 무제한). 그리드 직접 배치형 `label` 컴포넌트는 폐기되고, 팔레트 상단
 * "라벨 추가" 버튼으로 만드는 라벨(태그) — 최상위 `labels` 배열, 컴포넌트별 선택적
 * `labelId` 참조 — 로 대체됐다. 참조 컴포넌트가 1개 이상이면 캔버스 위에 경계 테두리
 * 오버레이를 항상 렌더링하며(2026-07-18 유지보수 요청 5차로 "2개 이상"에서 "1개 이상"으로
 * 변경), 테두리 스타일(`border-2`·`borderColor`)만 라벨의 `showBorder !== false`일 때
 * 적용하고 legend 스타일 텍스트(`textColor`, 테두리 선 위에 걸침)는 `showBorder`와 무관하게
 * 항상 표시한다(6차로 렌더링 여부와 테두리 스타일 여부를 분리 — 5차의 "showBorder=false면
 * 텍스트까지 사라지는" 결함 수정). `initialSchema`로 편집 모드 진입, 하단 적용/취소 버튼 —
 * 적용 시 `onApply({components, labels})`로 최신 그리드 스키마를 상위에 전달한다(자동저장
 * 없음, 실제 API 저장은 호출측의 "저장" 버튼). Content 설정 팝업(`ComponentSettingsPopover`)의
 * 개별 실시간 미리보기는 2026-07-18 유지보수 요청 6차로 완전히 제거됐다(캔버스 카드 자체 렌더링과
 * SCR-SRM-007 축소 미리보기(A1)로 충분하다고 판단, 중복 기능 정리) — `GridComponentBody`는
 * 캔버스 카드 렌더링(`BuilderComponentCard`)에서만 재사용한다. Content 설정 팝업은 7차로
 * Radix Popover(트리거 상대 배치) → 공용 `Modal`(Radix Dialog)로 전환돼, 라벨 생성/수정
 * 팝업과 공유하는 `MINI_POPUP_POSITION_CLASS`(`top-[42%]`)로 항상 동일한 화면 고정 좌표(가로
 * 정중앙, 세로 정중앙보다 살짝 위)에 뜬다(트리거 위치 무관). 라벨(태그) 지정 Select도 7차로
 * 팝업 하단→최상단(타입별 설정 항목보다 앞)으로 이동했다. 라벨 경계 오버레이 계산·렌더링은
 * 8차로 `GridLabelOverlays`(dynamic-form-renderer.tsx)로 추출돼 캔버스와 요청 제출 폼이 동일
 * 로직을 공유한다. Content 설정 팝업의 "기본값" 입력 UI는 8차로 실제 입력 타입과 동일화됐다
 * (`DefaultValueEditor` — select/radio는 옵션 중 단일 선택(재클릭 해제 가능), checkbox는 다중
 * 선택, date는 네이티브 date input, file은 UI 자체 없음). 읽기전용·필수 여부 체크박스는 8차로
 * 팝업 본문에서 `Modal`의 `titleExtra`(타이틀 우측)로 이동했다. 내부 UI 텍스트는 8차로
 * `useTranslation(["service-request", "common"])` 기반 i18n 키로 전환됐다(관리자 전용이라
 * 범위 밖이라던 기존 방침 해제).
 */
export interface DynamicFormBuilderProps {
  initialSchema?: GridFormSchema;
  onApply: (schema: GridFormSchema) => void;
  onCancel: () => void;
  className?: string;
}

const CANVAS_BUFFER_ROWS = 3;
const MIN_CANVAS_ROWS = 6;
const OVERLAP_WARNING_MS = 1500;
/** 팔레트 항목 pointer 이동이 이 값(px)을 넘으면 클릭이 아니라 드래그앤드롭 배치로 간주(B1). */
const DRAG_THRESHOLD_PX = 4;
/**
 * 드래그 종료 후 justDraggedRef를 자동 리셋하기까지의 지연(ms). 0(다음 매크로태스크)이어야 한다 —
 * 드래그가 원래 팔레트 버튼 위로 되돌아와 끝난 드문 경우에만 그 gesture 안에서 pointerup 직후
 * "동기적으로" 합성 click이 뒤따라오므로, setTimeout(0)으로도 그 click보다 항상 늦게 실행돼
 * 정상적으로 걸러진다. 반면 값을 300 등으로 늘리면 그 시간 동안 같은 타입의 모든 정상 클릭까지
 * 무차별로 씹히는 회귀가 생긴다(2026-07-18 4차 tester 재현, TC-GF4-B01e). 드래그가 캔버스 등
 * 다른 대상 위에서 끝난 통상적인 경우는 애초에 click 자체가 발생하지 않으므로(mousedown/up 대상
 * 불일치) 이 타임아웃이 유일한 리셋 경로다.
 */
const JUST_DRAGGED_RESET_MS = 0;
const NO_LABEL_VALUE = "__NONE__";
const DEFAULT_LABEL_FORM = { text: "", textColor: "#1d4ed8", borderColor: "#1d4ed8", showBorder: true };
/** Content 설정 팝업·라벨 생성/수정 팝업 공용 고정 위치(2026-07-18 유지보수 요청 7차) — 각 팝업의
 * 트리거(설정 아이콘·라벨 추가 버튼) 위치와 무관하게 항상 화면 뷰포트 기준 동일한 고정 좌표(가로
 * 정중앙은 공용 `DialogContent`의 `left-1/2 -translate-x-1/2` 그대로, 세로만 정중앙(`top-1/2`)
 * 보다 살짝 위로 오버라이드)에 뜬다. */
const MINI_POPUP_POSITION_CLASS = "top-[42%]";

/** i18n 미적용 시 폴백(defaultValue)으로 쓰는 한국어 원본 — 실제 표시 문구는 paletteLabel(t, type). */
const PALETTE_LABELS: Record<GridComponentType, string> = {
  text: "텍스트",
  textarea: "여러 줄 텍스트",
  select: "선택(Select)",
  radio: "라디오",
  checkbox: "체크박스",
  date: "날짜",
  file: "파일",
  "guide-text": "안내 텍스트",
  "guide-file": "가이드 파일",
};

function paletteLabel(t: TFunction, type: GridComponentType): string {
  return t(`dynamicForm.builder.palette.${type}`, { defaultValue: PALETTE_LABELS[type] });
}

const PALETTE_ICONS: Record<GridComponentType, typeof Type> = {
  text: Type,
  textarea: AlignLeft,
  select: List,
  radio: CircleDot,
  checkbox: CheckSquare,
  date: Calendar,
  file: File,
  "guide-text": Info,
  "guide-file": Paperclip,
};

/** i18n 미적용 시 폴백(defaultValue)으로 쓰는 한국어 원본 — 실제 표시 문구는 optionsGapLabel(t, gap). */
const OPTIONS_GAP_LABELS: Record<GridOptionsGap, string> = { 1: "좁게", 2: "보통", 3: "넓게" };

function optionsGapLabel(t: TFunction, gap: GridOptionsGap): string {
  return t(`dynamicForm.builder.optionsGapLabel.${gap}`, { defaultValue: OPTIONS_GAP_LABELS[gap] });
}

function defaultSize(type: GridComponentType): GridSize {
  return type === "textarea" ? { w: 2, h: 2 } : { w: 1, h: 1 };
}

/** 신규 컴포넌트 생성(클릭 배치·드래그앤드롭 배치 공용, B1). */
function buildNewComponent(
  type: GridComponentType,
  key: string,
  position: GridPosition,
  size: GridSize,
): GridComponent {
  if (type === "guide-text") {
    return { key, type, position, size, text: "텍스트", textAlign: "left", textVerticalAlign: "top" };
  }
  if (type === "guide-file") {
    return { key, type, position, size, file: null };
  }
  return {
    key,
    type,
    position,
    size,
    input: { widthPercent: 90, align: "center", verticalAlign: "top", readOnly: false, defaultValue: "" },
    validation: { required: false, regex: "" },
    options: hasGridOptions(type) ? "옵션1,옵션2" : undefined,
    ciLinked: false,
  };
}

function footprint(pos: GridPosition, size: GridSize): string[] {
  const cells: string[] = [];
  for (let c = pos.col; c < pos.col + size.w; c++) {
    for (let r = pos.row; r < pos.row + size.h; r++) {
      cells.push(`${c}:${r}`);
    }
  }
  return cells;
}

function hasOverlap(
  components: GridComponent[],
  excludeKey: string | null,
  pos: GridPosition,
  size: GridSize,
): boolean {
  const cells = new Set(footprint(pos, size));
  for (const comp of components) {
    if (comp.key === excludeKey) continue;
    if (footprint(comp.position, comp.size).some((cell) => cells.has(cell))) return true;
  }
  return false;
}

function findFreePosition(components: GridComponent[], size: GridSize): GridPosition {
  for (let row = 0; row < 500; row++) {
    for (let col = 0; col <= GRID_COLUMNS - size.w; col++) {
      if (!hasOverlap(components, null, { col, row }, size)) {
        return { col, row };
      }
    }
  }
  return { col: 0, row: components.length };
}

function nextKey(components: GridComponent[]): string {
  const existing = new Set(components.map((c) => c.key));
  let n = components.length + 1;
  while (existing.has(`field_${n}`)) n++;
  return `field_${n}`;
}

function nextLabelId(labels: GridLabel[]): string {
  const existing = new Set(labels.map((l) => l.id));
  let n = labels.length + 1;
  while (existing.has(`label_${n}`)) n++;
  return `label_${n}`;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

export function DynamicFormBuilder({ initialSchema, onApply, onCancel, className }: DynamicFormBuilderProps) {
  const { t } = useTranslation(["service-request", "common"]);
  const [components, setComponents] = useState<GridComponent[]>(() => initialSchema?.components ?? []);
  const [labels, setLabels] = useState<GridLabel[]>(() => initialSchema?.labels ?? []);
  const [overlapWarning, setOverlapWarning] = useState(false);
  const [movePreview, setMovePreview] = useState<{ key: string; position: GridPosition } | null>(null);
  const [resizePreview, setResizePreview] = useState<{ key: string; size: GridSize } | null>(null);
  const [dragPreview, setDragPreview] = useState<{
    type: GridComponentType;
    position: GridPosition;
    size: GridSize;
    invalid: boolean;
  } | null>(null);
  const [labelPopupOpen, setLabelPopupOpen] = useState(false);
  const [editingLabelId, setEditingLabelId] = useState<string | null>(null);
  const [labelForm, setLabelForm] = useState(DEFAULT_LABEL_FORM);
  const canvasRef = useRef<HTMLDivElement>(null);
  const warningTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const justDraggedRef = useRef<Partial<Record<GridComponentType, boolean>>>({});
  const justDraggedTimerRef = useRef<Partial<Record<GridComponentType, ReturnType<typeof setTimeout>>>>({});

  const showOverlapWarning = () => {
    setOverlapWarning(true);
    if (warningTimerRef.current) clearTimeout(warningTimerRef.current);
    warningTimerRef.current = setTimeout(() => setOverlapWarning(false), OVERLAP_WARNING_MS);
  };

  const updateComponent = (key: string, patch: Partial<GridComponent>) => {
    setComponents((cs) =>
      cs.map((c) => (c.key === key ? ({ ...c, ...patch } as GridComponent) : c)),
    );
  };

  const removeComponent = (key: string) => {
    setComponents((cs) => cs.filter((c) => c.key !== key));
  };

  const addComponentAt = (type: GridComponentType, position: GridPosition, size: GridSize) => {
    const key = nextKey(components);
    setComponents((cs) => [...cs, buildNewComponent(type, key, position, size)]);
  };

  const handleAddComponent = (type: GridComponentType) => {
    const size = defaultSize(type);
    const position = findFreePosition(components, size);
    addComponentAt(type, position, size);
  };

  const openCreateLabel = () => {
    setEditingLabelId(null);
    setLabelForm(DEFAULT_LABEL_FORM);
    setLabelPopupOpen(true);
  };

  const openEditLabel = (label: GridLabel) => {
    setEditingLabelId(label.id);
    setLabelForm({
      text: label.text,
      textColor: label.textColor,
      borderColor: label.borderColor,
      showBorder: label.showBorder ?? true,
    });
    setLabelPopupOpen(true);
  };

  const saveLabel = () => {
    if (!labelForm.text.trim()) return;
    if (editingLabelId == null) {
      const id = nextLabelId(labels);
      setLabels((ls) => [...ls, { id, ...labelForm, text: labelForm.text.trim() }]);
    } else {
      const id = editingLabelId;
      setLabels((ls) => ls.map((l) => (l.id === id ? { ...l, ...labelForm, text: labelForm.text.trim() } : l)));
    }
    setLabelPopupOpen(false);
  };

  const deleteLabel = (id: string) => {
    setLabels((ls) => ls.filter((l) => l.id !== id));
    setComponents((cs) => cs.map((c) => (c.labelId === id ? { ...c, labelId: null } : c)));
  };

  const cellMetrics = () => {
    const rect = canvasRef.current!.getBoundingClientRect();
    return { cellW: rect.width / GRID_COLUMNS, cellH: GRID_ROW_HEIGHT_PX };
  };

  /** 팔레트 항목 드래그앤드롭 배치(B1) — 기존 클릭 배치는 별도 onClick으로 유지. */
  const startPaletteDrag = (e: ReactPointerEvent<HTMLButtonElement>, type: GridComponentType) => {
    if (e.button !== 0) return;
    const startX = e.clientX;
    const startY = e.clientY;
    const size = defaultSize(type);
    let dragging = false;
    let last: { position: GridPosition; invalid: boolean } | null = null;

    const onMove = (ev: PointerEvent) => {
      if (!dragging && Math.hypot(ev.clientX - startX, ev.clientY - startY) > DRAG_THRESHOLD_PX) {
        dragging = true;
      }
      if (!dragging) return;
      const canvas = canvasRef.current;
      if (!canvas) return;
      const rect = canvas.getBoundingClientRect();
      if (ev.clientX < rect.left || ev.clientX > rect.right || ev.clientY < rect.top || ev.clientY > rect.bottom) {
        last = null;
        setDragPreview(null);
        return;
      }
      const { cellW, cellH } = cellMetrics();
      const position: GridPosition = {
        col: clamp(Math.floor((ev.clientX - rect.left) / cellW), 0, GRID_COLUMNS - size.w),
        row: Math.max(0, Math.floor((ev.clientY - rect.top) / cellH)),
      };
      const invalid = hasOverlap(components, null, position, size);
      last = { position, invalid };
      setDragPreview({ type, position, size, invalid });
    };

    const onUp = () => {
      window.removeEventListener("pointermove", onMove);
      window.removeEventListener("pointerup", onUp);
      setDragPreview(null);
      if (dragging) {
        justDraggedRef.current[type] = true;
        const prevTimer = justDraggedTimerRef.current[type];
        if (prevTimer) clearTimeout(prevTimer);
        justDraggedTimerRef.current[type] = setTimeout(() => {
          justDraggedRef.current[type] = false;
        }, JUST_DRAGGED_RESET_MS);
        if (last) {
          if (last.invalid) showOverlapWarning();
          else addComponentAt(type, last.position, size);
        }
      }
    };
    window.addEventListener("pointermove", onMove);
    window.addEventListener("pointerup", onUp);
  };

  const handlePaletteClick = (type: GridComponentType) => {
    if (justDraggedRef.current[type]) {
      justDraggedRef.current[type] = false;
      return;
    }
    handleAddComponent(type);
  };

  const startMove = (e: ReactPointerEvent<HTMLDivElement>, comp: GridComponent) => {
    if (e.button !== 0) return;
    e.preventDefault();
    const { cellW, cellH } = cellMetrics();
    const startX = e.clientX;
    const startY = e.clientY;
    const startCol = comp.position.col;
    const startRow = comp.position.row;
    let last: GridPosition = { col: startCol, row: startRow };

    const onMove = (ev: PointerEvent) => {
      const dCol = Math.round((ev.clientX - startX) / cellW);
      const dRow = Math.round((ev.clientY - startY) / cellH);
      const candidate: GridPosition = {
        col: clamp(startCol + dCol, 0, GRID_COLUMNS - comp.size.w),
        row: Math.max(0, startRow + dRow),
      };
      if (!hasOverlap(components, comp.key, candidate, comp.size)) {
        last = candidate;
        setMovePreview({ key: comp.key, position: candidate });
      } else {
        showOverlapWarning();
      }
    };
    const onUp = () => {
      window.removeEventListener("pointermove", onMove);
      window.removeEventListener("pointerup", onUp);
      updateComponent(comp.key, { position: last });
      setMovePreview(null);
    };
    window.addEventListener("pointermove", onMove);
    window.addEventListener("pointerup", onUp);
  };

  const startResize = (e: ReactPointerEvent<HTMLDivElement>, comp: GridComponent) => {
    if (e.button !== 0) return;
    e.preventDefault();
    e.stopPropagation();
    const { cellW, cellH } = cellMetrics();
    const startX = e.clientX;
    const startY = e.clientY;
    const startW = comp.size.w;
    const startH = comp.size.h;
    const maxH = gridMaxHeight(comp.type);
    const maxW = Math.min(2, GRID_COLUMNS - comp.position.col);
    let last: GridSize = { w: startW, h: startH };

    const onMove = (ev: PointerEvent) => {
      const dCol = Math.round((ev.clientX - startX) / cellW);
      const dRow = Math.round((ev.clientY - startY) / cellH);
      let candH = Math.max(1, startH + dRow);
      if (Number.isFinite(maxH)) candH = Math.min(candH, maxH);
      const candidate: GridSize = { w: clamp(startW + dCol, 1, maxW), h: candH };
      if (!hasOverlap(components, comp.key, comp.position, candidate)) {
        last = candidate;
        setResizePreview({ key: comp.key, size: candidate });
      } else {
        showOverlapWarning();
      }
    };
    const onUp = () => {
      window.removeEventListener("pointermove", onMove);
      window.removeEventListener("pointerup", onUp);
      updateComponent(comp.key, { size: last });
      setResizePreview(null);
    };
    window.addEventListener("pointermove", onMove);
    window.addEventListener("pointerup", onUp);
  };

  const effectivePosition = (c: GridComponent): GridPosition =>
    movePreview?.key === c.key ? movePreview.position : c.position;
  const effectiveSize = (c: GridComponent): GridSize =>
    resizePreview?.key === c.key ? resizePreview.size : c.size;

  const maxRow = components.reduce((max, c) => {
    const pos = effectivePosition(c);
    const size = effectiveSize(c);
    return Math.max(max, pos.row + size.h);
  }, dragPreview ? dragPreview.position.row + dragPreview.size.h : 0);
  const totalRows = Math.max(MIN_CANVAS_ROWS, maxRow + CANVAS_BUFFER_ROWS);

  return (
    <div className={cn("flex h-full min-h-0 flex-col gap-3", className)}>
      <div className="flex flex-wrap items-center gap-2 border-b border-border pb-3">
        <Button type="button" variant="outline" size="sm" className="gap-1.5" onClick={openCreateLabel}>
          <Tag className="size-3.5" />
          {t("dynamicForm.builder.addLabel", { defaultValue: "라벨 추가" })}
        </Button>
        {labels.map((label) => (
          <span
            key={label.id}
            className="inline-flex items-center gap-1.5 rounded-sm border px-2 py-1 text-xs"
            style={{ color: label.textColor, borderColor: label.borderColor, backgroundColor: `${label.borderColor}1A` }}
          >
            <button type="button" className="max-w-32 truncate" onClick={() => openEditLabel(label)}>
              {label.text}
            </button>
            <button
              type="button"
              aria-label={t("dynamicForm.builder.deleteLabel", { defaultValue: "라벨 삭제" })}
              className="opacity-70 hover:opacity-100"
              onClick={() => deleteLabel(label.id)}
            >
              <X className="size-3" />
            </button>
          </span>
        ))}
      </div>

      <div className="flex flex-1 gap-4 overflow-hidden">
        <div className="flex w-40 shrink-0 flex-col gap-1.5 overflow-y-auto">
          {GRID_PALETTE_TYPES.map((type) => {
            const Icon = PALETTE_ICONS[type];
            return (
              <Button
                key={type}
                type="button"
                variant="outline"
                size="sm"
                className="justify-start gap-2"
                onPointerDown={(e) => startPaletteDrag(e, type)}
                onClick={() => handlePaletteClick(type)}
              >
                <Icon />
                {paletteLabel(t, type)}
              </Button>
            );
          })}
        </div>

        <div className="flex-1 overflow-y-auto rounded-md border border-border bg-muted/20 p-2">
          <div
            ref={canvasRef}
            className="relative grid gap-1"
            style={{
              gridTemplateColumns: `repeat(${GRID_COLUMNS}, minmax(0, 1fr))`,
              gridAutoRows: `${GRID_ROW_HEIGHT_PX}px`,
              minHeight: totalRows * GRID_ROW_HEIGHT_PX,
            }}
          >
            {components.map((comp) => {
              const position = effectivePosition(comp);
              const size = effectiveSize(comp);
              return (
                <BuilderComponentCard
                  key={comp.key}
                  component={comp}
                  position={position}
                  size={size}
                  labels={labels}
                  onMoveStart={(e) => startMove(e, comp)}
                  onResizeStart={(e) => startResize(e, comp)}
                  onDelete={() => removeComponent(comp.key)}
                  onUpdate={(patch) => updateComponent(comp.key, patch)}
                />
              );
            })}

            {dragPreview ? (
              <div
                style={{
                  gridColumn: `${dragPreview.position.col + 1} / span ${dragPreview.size.w}`,
                  gridRow: `${dragPreview.position.row + 1} / span ${dragPreview.size.h}`,
                }}
                className={cn(
                  "pointer-events-none rounded-md border-2 border-dashed",
                  dragPreview.invalid ? "border-destructive bg-destructive/10" : "border-primary bg-primary/10",
                )}
              />
            ) : null}

            <GridLabelOverlays
              components={components}
              labels={labels}
              getPosition={effectivePosition}
              getSize={effectiveSize}
            />
          </div>
        </div>
      </div>

      {overlapWarning ? (
        <p role="alert" className="text-xs text-destructive">
          {t("dynamicForm.builder.overlapWarning", { defaultValue: "이미 배치된 컴포넌트와 겹칩니다" })}
        </p>
      ) : null}

      <div className="flex justify-end gap-2 border-t border-border pt-3">
        <Button type="button" variant="outline" onClick={onCancel}>
          {t("dynamicForm.builder.cancel", { defaultValue: "취소" })}
        </Button>
        <Button type="button" onClick={() => onApply({ components, labels })}>
          {t("dynamicForm.builder.apply", { defaultValue: "적용" })}
        </Button>
      </div>

      <Modal
        open={labelPopupOpen}
        onOpenChange={setLabelPopupOpen}
        title={
          editingLabelId == null
            ? t("dynamicForm.builder.addLabelTitle", { defaultValue: "라벨 추가" })
            : t("dynamicForm.builder.editLabelTitle", { defaultValue: "라벨 수정" })
        }
        className={MINI_POPUP_POSITION_CLASS}
      >
        <div className="space-y-3">
          <div className="space-y-1.5">
            <Label className="text-xs">
              {t("dynamicForm.builder.labelTextField", { defaultValue: "텍스트" })}
            </Label>
            <Input
              className="h-8"
              value={labelForm.text}
              onChange={(e) => setLabelForm((f) => ({ ...f, text: e.target.value }))}
            />
          </div>
          <div className={cn("grid gap-3", labelForm.showBorder ? "grid-cols-2" : "grid-cols-1")}>
            <div className="space-y-1.5">
              <Label className="text-xs">{t("dynamicForm.builder.textColor", { defaultValue: "글자색" })}</Label>
              <input
                type="color"
                className="h-8 w-full cursor-pointer rounded-md border border-input"
                value={labelForm.textColor}
                onChange={(e) => setLabelForm((f) => ({ ...f, textColor: e.target.value }))}
              />
            </div>
            {labelForm.showBorder ? (
              <div className="space-y-1.5">
                <Label className="text-xs">
                  {t("dynamicForm.builder.borderColor", { defaultValue: "테두리색" })}
                </Label>
                <input
                  type="color"
                  className="h-8 w-full cursor-pointer rounded-md border border-input"
                  value={labelForm.borderColor}
                  onChange={(e) => setLabelForm((f) => ({ ...f, borderColor: e.target.value }))}
                />
              </div>
            ) : null}
          </div>
          <label className="flex items-center gap-1.5 text-xs">
            <Checkbox
              checked={!labelForm.showBorder}
              onCheckedChange={(v) => setLabelForm((f) => ({ ...f, showBorder: !v }))}
            />
            {t("dynamicForm.builder.noBorder", { defaultValue: "테두리 없음" })}
          </label>
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="outline" onClick={() => setLabelPopupOpen(false)}>
              {t("dynamicForm.builder.cancel", { defaultValue: "취소" })}
            </Button>
            <Button type="button" onClick={saveLabel}>
              {t("dynamicForm.builder.save", { defaultValue: "저장" })}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

interface BuilderComponentCardProps {
  component: GridComponent;
  position: GridPosition;
  size: GridSize;
  labels: GridLabel[];
  onMoveStart: (e: ReactPointerEvent<HTMLDivElement>) => void;
  onResizeStart: (e: ReactPointerEvent<HTMLDivElement>) => void;
  onDelete: () => void;
  onUpdate: (patch: Partial<GridComponent>) => void;
}

function BuilderComponentCard({
  component,
  position,
  size,
  labels,
  onMoveStart,
  onResizeStart,
  onDelete,
  onUpdate,
}: BuilderComponentCardProps) {
  const { t } = useTranslation(["service-request", "common"]);
  return (
    <div
      style={{
        gridColumn: `${position.col + 1} / span ${size.w}`,
        gridRow: `${position.row + 1} / span ${size.h}`,
      }}
      className="group relative flex cursor-move select-none overflow-hidden rounded-md border border-border bg-card p-1 text-xs"
      onPointerDown={onMoveStart}
    >
      {/* 캔버스=미리보기 통합(5.2절) — 실제 렌더링 모습을 그대로 보여주되 순수 시각적 표현이라
          카드 드래그·오버레이 액션과 충돌하지 않도록 pointer-events-none으로 상호작용을 차단한다. */}
      <div className="pointer-events-none flex-1 overflow-hidden">
        <GridComponentBody component={component} />
      </div>

      <div className="absolute right-1 top-1 flex items-center gap-0.5 rounded-md bg-background/80 opacity-0 backdrop-blur-sm transition-opacity group-hover:opacity-100">
        <ComponentSettingsPopover component={component} labels={labels} onUpdate={onUpdate} />
        <button
          type="button"
          aria-label={t("dynamicForm.builder.deleteField", { defaultValue: "필드 삭제" })}
          className="rounded p-0.5 text-muted-foreground hover:bg-accent hover:text-destructive"
          onPointerDown={(e) => e.stopPropagation()}
          onClick={onDelete}
        >
          <Trash2 className="size-3.5" />
        </button>
      </div>
      <div
        role="presentation"
        aria-hidden="true"
        className="absolute bottom-0 right-0 flex size-4 cursor-nwse-resize items-center justify-center opacity-0 group-hover:opacity-100"
        onPointerDown={onResizeStart}
      >
        <GripVertical className="size-3 rotate-45 text-muted-foreground" />
      </div>
    </div>
  );
}

function ComponentSettingsPopover({
  component,
  labels,
  onUpdate,
}: {
  component: GridComponent;
  labels: GridLabel[];
  onUpdate: (patch: Partial<GridComponent>) => void;
}) {
  const { t } = useTranslation(["service-request", "common"]);
  const [open, setOpen] = useState(false);

  // 읽기전용·필수 여부는 8차로 팝업 본문에서 타이틀 우측(titleExtra)으로 이동(guide-text/guide-file은 대상 없음).
  const titleExtra =
    component.type === "guide-text" || component.type === "guide-file" ? undefined : (
      <div className="flex items-center gap-4">
        <label className="flex items-center gap-1.5 text-xs font-normal">
          <Checkbox
            checked={!!component.input?.readOnly}
            onCheckedChange={(v) => onUpdate({ input: { ...component.input, readOnly: !!v } })}
          />
          {t("dynamicForm.builder.readOnly", { defaultValue: "읽기 전용" })}
        </label>
        <label className="flex items-center gap-1.5 text-xs font-normal">
          <Checkbox
            checked={!!component.validation?.required}
            onCheckedChange={(v) => onUpdate({ validation: { ...component.validation, required: !!v } })}
          />
          {t("dynamicForm.builder.required", { defaultValue: "필수 여부" })}
        </label>
      </div>
    );

  return (
    <>
      <button
        type="button"
        aria-label={t("dynamicForm.builder.settingsTitle", { defaultValue: "컴포넌트 설정" })}
        className="rounded p-0.5 text-muted-foreground hover:bg-accent hover:text-foreground"
        onPointerDown={(e) => e.stopPropagation()}
        onClick={() => setOpen(true)}
      >
        <Settings2 className="size-3.5" />
      </button>
      <Modal
        open={open}
        onOpenChange={setOpen}
        title={t("dynamicForm.builder.settingsTitle", { defaultValue: "컴포넌트 설정" })}
        titleExtra={titleExtra}
        className={cn(MINI_POPUP_POSITION_CLASS, "w-[420px] max-h-[80vh] overflow-y-auto")}
      >
        <div className="space-y-3" onPointerDown={(e) => e.stopPropagation()}>
          <div className="space-y-1.5">
            <Label className="text-xs">
              {t("dynamicForm.builder.labelSelect", { defaultValue: "라벨(태그)" })}
            </Label>
            <Select
              value={component.labelId ?? NO_LABEL_VALUE}
              onValueChange={(v) => onUpdate({ labelId: v === NO_LABEL_VALUE ? null : v })}
            >
              <SelectTrigger className="h-8">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={NO_LABEL_VALUE}>
                  {t("dynamicForm.builder.noneOption", { defaultValue: "없음" })}
                </SelectItem>
                {labels.map((label) => (
                  <SelectItem key={label.id} value={label.id}>
                    {label.text}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {component.type === "guide-text" ? (
            <>
              <div className="space-y-1.5">
                <Label className="text-xs">
                  {t("dynamicForm.builder.guideTextContent", { defaultValue: "안내 텍스트" })}
                </Label>
                <Input
                  className="h-8"
                  value={component.text}
                  onChange={(e) => onUpdate({ text: e.target.value })}
                />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs">{t("dynamicForm.builder.align", { defaultValue: "정렬" })}</Label>
                <AnchorGridToggle
                  align={component.textAlign ?? "left"}
                  verticalAlign={component.textVerticalAlign ?? "top"}
                  onChange={(textAlign, textVerticalAlign) => onUpdate({ textAlign, textVerticalAlign })}
                />
              </div>
            </>
          ) : component.type === "guide-file" ? (
            <div className="space-y-1.5">
              <Label className="text-xs">
                {t("dynamicForm.builder.attachFileOptional", { defaultValue: "첨부 파일(선택)" })}
              </Label>
              <input
                type="file"
                className="block w-full text-xs text-muted-foreground file:mr-2 file:rounded-md file:border file:border-input file:bg-background file:px-2 file:py-1 file:text-xs"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (!file) return;
                  const reader = new FileReader();
                  reader.onload = () =>
                    onUpdate({ file: { name: file.name, dataUrl: reader.result as string } });
                  reader.readAsDataURL(file);
                }}
              />
              {component.file ? (
                <div className="flex items-center justify-between gap-2 text-xs text-muted-foreground">
                  <span className="truncate">{component.file.name}</span>
                  <button
                    type="button"
                    className="shrink-0 text-destructive hover:underline"
                    onClick={() => onUpdate({ file: null })}
                  >
                    {t("dynamicForm.builder.remove", { defaultValue: "제거" })}
                  </button>
                </div>
              ) : null}
            </div>
          ) : (
            <>
              <div className="space-y-1.5">
                <Label className="text-xs">
                  {t("dynamicForm.builder.inputWidth", { defaultValue: "input 폭(%)" })}
                </Label>
                <Input
                  className="h-8"
                  type="number"
                  min={10}
                  max={100}
                  value={component.input?.widthPercent ?? 90}
                  onChange={(e) =>
                    onUpdate({ input: { ...component.input, widthPercent: Number(e.target.value) || 90 } })
                  }
                />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs">
                  {t("dynamicForm.builder.inputAlign", { defaultValue: "input 정렬" })}
                </Label>
                <AnchorGridToggle
                  align={component.input?.align ?? "center"}
                  verticalAlign={component.input?.verticalAlign ?? "top"}
                  onChange={(align, verticalAlign) =>
                    onUpdate({ input: { ...component.input, align, verticalAlign } })
                  }
                />
              </div>

              {hasPlaceholderUi(component.type) ? (
                <div className="space-y-1.5">
                  <Label className="text-xs">
                    {t("dynamicForm.builder.placeholder", { defaultValue: "Placeholder" })}
                  </Label>
                  <Input
                    className="h-8"
                    value={component.input?.placeholder ?? ""}
                    onChange={(e) => onUpdate({ input: { ...component.input, placeholder: e.target.value } })}
                  />
                </div>
              ) : null}

              {component.type !== "file" ? (
                <div className="space-y-1.5">
                  <Label className="text-xs">
                    {t("dynamicForm.builder.defaultValue", { defaultValue: "기본값" })}
                  </Label>
                  <DefaultValueEditor component={component} onUpdate={onUpdate} t={t} />
                </div>
              ) : null}

              {hasRegexUi(component.type) ? (
                <div className="space-y-1.5">
                  <Label className="text-xs">
                    {t("dynamicForm.builder.regex", { defaultValue: "Validation 정규식(선택)" })}
                  </Label>
                  <Input
                    className="h-8"
                    value={component.validation?.regex ?? ""}
                    placeholder={t("dynamicForm.builder.regexPlaceholder", {
                      defaultValue: "예: ^[0-9]{3}-[0-9]{4}$",
                    })}
                    onChange={(e) => onUpdate({ validation: { ...component.validation, regex: e.target.value } })}
                  />
                </div>
              ) : null}

              {hasGridOptions(component.type) ? (
                <div className="space-y-1.5">
                  <Label className="text-xs">
                    {t("dynamicForm.builder.options", { defaultValue: "옵션(콤마로 구분)" })}
                  </Label>
                  <Input
                    className="h-8"
                    value={component.options ?? ""}
                    onChange={(e) => onUpdate({ options: e.target.value })}
                  />
                  <div className="flex items-center gap-3 pt-1">
                    <label className="flex items-center gap-1.5 text-xs text-muted-foreground">
                      <input
                        type="radio"
                        className="accent-primary"
                        name={`ci-linked-${component.key}`}
                        checked={!component.ciLinked}
                        onChange={() => onUpdate({ ciLinked: false })}
                      />
                      {t("dynamicForm.builder.ciNormal", { defaultValue: "일반" })}
                    </label>
                    <label className="flex items-center gap-1.5 text-xs text-muted-foreground">
                      <input
                        type="radio"
                        className="accent-primary"
                        name={`ci-linked-${component.key}`}
                        checked={!!component.ciLinked}
                        onChange={() => onUpdate({ ciLinked: true })}
                      />
                      {t("dynamicForm.builder.ciLinked", { defaultValue: "CI 연계" })}
                    </label>
                  </div>

                  {hasOptionsLayoutUi(component.type) ? (
                    <div className="grid grid-cols-2 gap-2 pt-2">
                      <div className="space-y-1.5">
                        <Label className="text-xs">
                          {t("dynamicForm.builder.optionsDirection", { defaultValue: "옵션 배치 방향" })}
                        </Label>
                        <OptionsDirectionToggle
                          value={component.optionsDirection ?? "row"}
                          onChange={(dir) => onUpdate({ optionsDirection: dir })}
                        />
                      </div>
                      <div className="space-y-1.5">
                        <Label className="text-xs">
                          {t("dynamicForm.builder.optionsGap", { defaultValue: "옵션 간 여백" })}
                        </Label>
                        <OptionsGapToggle
                          value={component.optionsGap ?? 1}
                          onChange={(gap) => onUpdate({ optionsGap: gap })}
                        />
                      </div>
                    </div>
                  ) : null}
                </div>
              ) : null}
            </>
          )}
        </div>
      </Modal>
    </>
  );
}

/**
 * Content 설정 팝업의 "기본값" 입력 UI — 실제 입력 타입과 동일화한다(2026-07-18 유지보수 요청
 * 8차): text/textarea는 Input, select/radio는 옵션 목록 중 단일 선택(재클릭으로 선택 해제
 * 가능), checkbox는 다중 선택 체크박스 그룹, date는 `type="date"` 네이티브 입력, file은 UI
 * 자체를 렌더링하지 않는다(렌더러가 애초에 무시하던 값이라 회귀 없음).
 */
function DefaultValueEditor({
  component,
  onUpdate,
  t,
}: {
  component: GridInputComponent;
  onUpdate: (patch: Partial<GridComponent>) => void;
  t: TFunction;
}) {
  if (component.type === "file") return null;

  if (component.type === "date") {
    return (
      <input
        type="date"
        className="flex h-8 w-full rounded-md border border-input bg-background px-3 text-sm shadow-xs"
        value={typeof component.input?.defaultValue === "string" ? component.input.defaultValue : ""}
        onChange={(e) => onUpdate({ input: { ...component.input, defaultValue: e.target.value } })}
      />
    );
  }

  if (component.type === "select" || component.type === "radio") {
    const options = parseOptions(component.options);
    const current = typeof component.input?.defaultValue === "string" ? component.input.defaultValue : null;
    if (options.length === 0) {
      return (
        <p className="text-xs text-muted-foreground">
          {t("dynamicForm.builder.noOptionsHint", { defaultValue: "옵션을 먼저 입력하세요" })}
        </p>
      );
    }
    return (
      <div className="flex flex-wrap gap-1">
        {options.map((o) => (
          <button
            key={o}
            type="button"
            onClick={() =>
              onUpdate({ input: { ...component.input, defaultValue: current === o ? null : o } })
            }
            className={cn(TOGGLE_PILL_CLASS, current === o ? TOGGLE_PILL_ACTIVE_CLASS : TOGGLE_PILL_INACTIVE_CLASS)}
          >
            {o}
          </button>
        ))}
      </div>
    );
  }

  if (component.type === "checkbox") {
    const options = parseOptions(component.options);
    const current = Array.isArray(component.input?.defaultValue) ? component.input.defaultValue : [];
    if (options.length === 0) {
      return (
        <p className="text-xs text-muted-foreground">
          {t("dynamicForm.builder.noOptionsHint", { defaultValue: "옵션을 먼저 입력하세요" })}
        </p>
      );
    }
    return (
      <div className="flex flex-col gap-1.5">
        {options.map((o) => {
          const checked = current.includes(o);
          return (
            <label key={o} className="flex items-center gap-1.5 text-xs">
              <Checkbox
                checked={checked}
                onCheckedChange={(v) =>
                  onUpdate({
                    input: {
                      ...component.input,
                      defaultValue: v ? [...current, o] : current.filter((x) => x !== o),
                    },
                  })
                }
              />
              {o}
            </label>
          );
        })}
      </div>
    );
  }

  // text/textarea
  return (
    <Input
      className="h-8"
      value={typeof component.input?.defaultValue === "string" ? component.input.defaultValue : ""}
      onChange={(e) => onUpdate({ input: { ...component.input, defaultValue: e.target.value } })}
    />
  );
}

const ANCHOR_H_OPTIONS: GridAlign[] = ["left", "center", "right"];
const ANCHOR_V_OPTIONS: GridAlignVertical[] = ["top", "middle", "bottom"];

/** 정렬 9방향 앵커 선택 위젯(2026-07-18 유지보수 요청 4차, B4) — 기존 가로 3버튼 AlignToggle 대체. */
function AnchorGridToggle({
  align,
  verticalAlign,
  onChange,
}: {
  align: GridAlign;
  verticalAlign: GridAlignVertical;
  onChange: (align: GridAlign, verticalAlign: GridAlignVertical) => void;
}) {
  return (
    <div className="grid w-fit grid-cols-3 gap-1">
      {ANCHOR_V_OPTIONS.flatMap((v) =>
        ANCHOR_H_OPTIONS.map((h) => {
          const active = align === h && verticalAlign === v;
          return (
            <button
              key={`${h}-${v}`}
              type="button"
              aria-label={`${h}-${v}`}
              onClick={() => onChange(h, v)}
              className={cn(
                "flex size-7 items-center justify-center rounded-md border",
                active
                  ? "border-primary bg-primary text-primary-foreground"
                  : "border-input bg-background text-muted-foreground hover:bg-accent",
              )}
            >
              <span className="size-1.5 rounded-full bg-current" aria-hidden="true" />
            </button>
          );
        }),
      )}
    </div>
  );
}

const TOGGLE_PILL_CLASS = "rounded-md border px-2 py-1 text-xs transition-colors";
const TOGGLE_PILL_ACTIVE_CLASS = "border-primary bg-primary text-primary-foreground";
const TOGGLE_PILL_INACTIVE_CLASS = "border-input bg-background text-muted-foreground hover:bg-accent";

function OptionsDirectionToggle({
  value,
  onChange,
}: {
  value: GridOptionsDirection;
  onChange: (direction: GridOptionsDirection) => void;
}) {
  const { t } = useTranslation(["service-request", "common"]);
  const options: { value: GridOptionsDirection; label: string }[] = [
    { value: "row", label: t("dynamicForm.builder.optionsDirectionRow", { defaultValue: "가로" }) },
    { value: "column", label: t("dynamicForm.builder.optionsDirectionColumn", { defaultValue: "세로" }) },
  ];
  return (
    <div className="flex gap-1">
      {options.map(({ value: v, label }) => (
        <button
          key={v}
          type="button"
          onClick={() => onChange(v)}
          className={cn(TOGGLE_PILL_CLASS, value === v ? TOGGLE_PILL_ACTIVE_CLASS : TOGGLE_PILL_INACTIVE_CLASS)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}

function OptionsGapToggle({
  value,
  onChange,
}: {
  value: GridOptionsGap;
  onChange: (gap: GridOptionsGap) => void;
}) {
  const { t } = useTranslation(["service-request", "common"]);
  const options: GridOptionsGap[] = [1, 2, 3];
  return (
    <div className="flex gap-1">
      {options.map((g) => (
        <button
          key={g}
          type="button"
          onClick={() => onChange(g)}
          className={cn(TOGGLE_PILL_CLASS, value === g ? TOGGLE_PILL_ACTIVE_CLASS : TOGGLE_PILL_INACTIVE_CLASS)}
        >
          {optionsGapLabel(t, g)}
        </button>
      ))}
    </div>
  );
}
