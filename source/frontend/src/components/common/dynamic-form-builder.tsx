import { useRef, useState, type PointerEvent as ReactPointerEvent } from "react";
import {
  AlignCenter,
  AlignLeft,
  AlignRight,
  Calendar,
  CheckSquare,
  CircleDot,
  File,
  GripVertical,
  Info,
  List,
  Paperclip,
  Settings2,
  Text as LabelIcon,
  Trash2,
  Type,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { GridComponentBody } from "@/components/common/dynamic-form-renderer";
import {
  GRID_COLUMNS,
  GRID_PALETTE_TYPES,
  GRID_ROW_HEIGHT_PX,
  gridMaxHeight,
  hasGridOptions,
  hasOptionsLayoutUi,
  hasPlaceholderUi,
  type GridAlign,
  type GridComponent,
  type GridComponentType,
  type GridFormSchema,
  type GridInputComponent,
  type GridOptionsDirection,
  type GridOptionsGap,
  type GridPosition,
  type GridSize,
} from "@/components/common/form-schema";

/**
 * 관리자용 그리드 폼 빌더 — SCR-SRM-007 "Form 설정" 팝업(form.io 완전 제거 → 자체 8×n
 * 그리드). 팝업 내부는 **좌 팔레트(10종 — 입력 7종 + 값 입력 없는 정적 컴포넌트
 * `label`/`guide-text`/`guide-file`) / 우 8칸 그리드 캔버스** 2분할이다(2026-07-18 후속
 * 유지보수 정정 — 별도 미리보기 패널은 사용자 피드백으로 폐기, **캔버스=미리보기 통합**: 각
 * 캔버스 카드가 `GridComponentBody`를 그대로 렌더링해 실제 렌더링 모습을 직접 보여준다,
 * 순수 시각적 표현·상호작용 불가). 캔버스는 스크롤 가능, 배치·리사이즈는 1칸 단위 스냅,
 * 겹침 배치는 차단+인라인 안내. 입력 7종은 `label`/`labelAlign` 속성을 갖지 않는다(라벨이
 * 필요하면 `label` 컴포넌트를 별도 셀에 배치, 접근성 연결 없음). `initialSchema`로 편집
 * 모드 진입, 하단 적용/취소 버튼 — 적용 시 `onApply`로 최신 그리드 스키마를 상위에 전달한다
 * (자동저장 없음, 실제 API 저장은 호출측의 "저장" 버튼). Content 설정 팝업 내 개별 실시간
 * 미리보기(상호작용 가능)는 렌더러(`GridComponentBody`, dynamic-form-renderer.tsx)를 그대로
 * 재사용한다(로직 중복 구현 금지, 캔버스 카드 렌더링과 별개 기능).
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

const PALETTE_LABELS: Record<GridComponentType, string> = {
  text: "텍스트",
  textarea: "여러 줄 텍스트",
  select: "선택(Select)",
  radio: "라디오",
  checkbox: "체크박스",
  date: "날짜",
  file: "파일",
  label: "라벨",
  "guide-text": "안내 텍스트",
  "guide-file": "가이드 파일",
};

const PALETTE_ICONS: Record<GridComponentType, typeof Type> = {
  text: Type,
  textarea: AlignLeft,
  select: List,
  radio: CircleDot,
  checkbox: CheckSquare,
  date: Calendar,
  file: File,
  label: LabelIcon,
  "guide-text": Info,
  "guide-file": Paperclip,
};

const OPTIONS_GAP_LABELS: Record<GridOptionsGap, string> = { 1: "좁게", 2: "보통", 3: "넓게" };

function defaultSize(type: GridComponentType): GridSize {
  return type === "textarea" ? { w: 2, h: 2 } : { w: 1, h: 1 };
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

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

export function DynamicFormBuilder({ initialSchema, onApply, onCancel, className }: DynamicFormBuilderProps) {
  const [components, setComponents] = useState<GridComponent[]>(() => initialSchema?.components ?? []);
  const [overlapWarning, setOverlapWarning] = useState(false);
  const [movePreview, setMovePreview] = useState<{ key: string; position: GridPosition } | null>(null);
  const [resizePreview, setResizePreview] = useState<{ key: string; size: GridSize } | null>(null);
  const canvasRef = useRef<HTMLDivElement>(null);
  const warningTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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

  const handleAddComponent = (type: GridComponentType) => {
    const size = defaultSize(type);
    const position = findFreePosition(components, size);
    const key = nextKey(components);
    if (type === "label") {
      setComponents((cs) => [
        ...cs,
        { key, type: "label", position, size, text: "텍스트", textAlign: "left" },
      ]);
      return;
    }
    if (type === "guide-text") {
      setComponents((cs) => [
        ...cs,
        { key, type: "guide-text", position, size, text: "텍스트", textAlign: "left" },
      ]);
      return;
    }
    if (type === "guide-file") {
      setComponents((cs) => [...cs, { key, type: "guide-file", position, size, file: null }]);
      return;
    }
    const newComponent: GridInputComponent = {
      key,
      type,
      position,
      size,
      input: { widthPercent: 90, align: "center", readOnly: false, defaultValue: "" },
      validation: { required: false, regex: "" },
      options: hasGridOptions(type) ? "옵션1,옵션2" : undefined,
      ciLinked: false,
    };
    setComponents((cs) => [...cs, newComponent]);
  };

  const cellMetrics = () => {
    const rect = canvasRef.current!.getBoundingClientRect();
    return { cellW: rect.width / GRID_COLUMNS, cellH: GRID_ROW_HEIGHT_PX };
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

  const maxRow = components.reduce((max, c) => {
    const pos = movePreview?.key === c.key ? movePreview.position : c.position;
    const size = resizePreview?.key === c.key ? resizePreview.size : c.size;
    return Math.max(max, pos.row + size.h);
  }, 0);
  const totalRows = Math.max(MIN_CANVAS_ROWS, maxRow + CANVAS_BUFFER_ROWS);

  return (
    <div className={cn("flex h-full min-h-0 flex-col gap-3", className)}>
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
                onClick={() => handleAddComponent(type)}
              >
                <Icon />
                {PALETTE_LABELS[type]}
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
              const position = movePreview?.key === comp.key ? movePreview.position : comp.position;
              const size = resizePreview?.key === comp.key ? resizePreview.size : comp.size;
              return (
                <BuilderComponentCard
                  key={comp.key}
                  component={comp}
                  position={position}
                  size={size}
                  onMoveStart={(e) => startMove(e, comp)}
                  onResizeStart={(e) => startResize(e, comp)}
                  onDelete={() => removeComponent(comp.key)}
                  onUpdate={(patch) => updateComponent(comp.key, patch)}
                />
              );
            })}
          </div>
        </div>
      </div>

      {overlapWarning ? (
        <p role="alert" className="text-xs text-destructive">
          이미 배치된 컴포넌트와 겹칩니다
        </p>
      ) : null}

      <div className="flex justify-end gap-2 border-t border-border pt-3">
        <Button type="button" variant="outline" onClick={onCancel}>
          취소
        </Button>
        <Button type="button" onClick={() => onApply({ components })}>
          적용
        </Button>
      </div>
    </div>
  );
}

interface BuilderComponentCardProps {
  component: GridComponent;
  position: GridPosition;
  size: GridSize;
  onMoveStart: (e: ReactPointerEvent<HTMLDivElement>) => void;
  onResizeStart: (e: ReactPointerEvent<HTMLDivElement>) => void;
  onDelete: () => void;
  onUpdate: (patch: Partial<GridComponent>) => void;
}

function BuilderComponentCard({
  component,
  position,
  size,
  onMoveStart,
  onResizeStart,
  onDelete,
  onUpdate,
}: BuilderComponentCardProps) {
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
        <ComponentSettingsPopover component={component} onUpdate={onUpdate} />
        <button
          type="button"
          aria-label="필드 삭제"
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
  onUpdate,
}: {
  component: GridComponent;
  onUpdate: (patch: Partial<GridComponent>) => void;
}) {
  const [open, setOpen] = useState(false);
  const [previewValue, setPreviewValue] = useState<unknown>(undefined);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          type="button"
          aria-label="컴포넌트 설정"
          className="rounded p-0.5 text-muted-foreground hover:bg-accent hover:text-foreground"
          onPointerDown={(e) => e.stopPropagation()}
        >
          <Settings2 className="size-3.5" />
        </button>
      </PopoverTrigger>
      <PopoverContent
        className="w-[420px] space-y-3 overflow-y-auto"
        style={{ maxHeight: "var(--radix-popover-content-available-height)" }}
        align="start"
        onPointerDown={(e) => e.stopPropagation()}
      >
        {component.type === "label" || component.type === "guide-text" ? (
          <>
            <div className="space-y-1.5">
              <Label className="text-xs">표시 텍스트</Label>
              <Input
                className="h-8"
                value={component.text}
                onChange={(e) => onUpdate({ text: e.target.value })}
              />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">정렬</Label>
              <AlignToggle
                value={component.textAlign ?? "left"}
                onChange={(align) => onUpdate({ textAlign: align })}
              />
            </div>
          </>
        ) : component.type === "guide-file" ? (
          <div className="space-y-1.5">
            <Label className="text-xs">첨부 파일(선택)</Label>
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
                  제거
                </button>
              </div>
            ) : null}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 gap-2">
              <div className="space-y-1.5">
                <Label className="text-xs">input 폭(%)</Label>
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
                <Label className="text-xs">input 정렬</Label>
                <AlignToggle
                  value={component.input?.align ?? "center"}
                  onChange={(align) => onUpdate({ input: { ...component.input, align } })}
                />
              </div>
            </div>

            {hasPlaceholderUi(component.type) ? (
              <div className="space-y-1.5">
                <Label className="text-xs">Placeholder</Label>
                <Input
                  className="h-8"
                  value={component.input?.placeholder ?? ""}
                  onChange={(e) => onUpdate({ input: { ...component.input, placeholder: e.target.value } })}
                />
              </div>
            ) : null}

            <div className="space-y-1.5">
              <Label className="text-xs">기본값</Label>
              <Input
                className="h-8"
                value={component.input?.defaultValue ?? ""}
                onChange={(e) => onUpdate({ input: { ...component.input, defaultValue: e.target.value } })}
              />
            </div>

            <div className="flex items-center gap-4">
              <label className="flex items-center gap-1.5 text-xs">
                <Checkbox
                  checked={!!component.input?.readOnly}
                  onCheckedChange={(v) => onUpdate({ input: { ...component.input, readOnly: !!v } })}
                />
                읽기 전용
              </label>
              <label className="flex items-center gap-1.5 text-xs">
                <Checkbox
                  checked={!!component.validation?.required}
                  onCheckedChange={(v) => onUpdate({ validation: { ...component.validation, required: !!v } })}
                />
                필수 여부
              </label>
            </div>

            <div className="space-y-1.5">
              <Label className="text-xs">Validation 정규식(선택)</Label>
              <Input
                className="h-8"
                value={component.validation?.regex ?? ""}
                placeholder="예: ^[0-9]{3}-[0-9]{4}$"
                onChange={(e) => onUpdate({ validation: { ...component.validation, regex: e.target.value } })}
              />
            </div>

            {hasGridOptions(component.type) ? (
              <div className="space-y-1.5">
                <Label className="text-xs">옵션(콤마로 구분)</Label>
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
                    일반
                  </label>
                  <label className="flex items-center gap-1.5 text-xs text-muted-foreground">
                    <input
                      type="radio"
                      className="accent-primary"
                      name={`ci-linked-${component.key}`}
                      checked={!!component.ciLinked}
                      onChange={() => onUpdate({ ciLinked: true })}
                    />
                    CI 연계
                  </label>
                </div>

                {hasOptionsLayoutUi(component.type) ? (
                  <div className="grid grid-cols-2 gap-2 pt-2">
                    <div className="space-y-1.5">
                      <Label className="text-xs">옵션 배치 방향</Label>
                      <OptionsDirectionToggle
                        value={component.optionsDirection ?? "row"}
                        onChange={(dir) => onUpdate({ optionsDirection: dir })}
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label className="text-xs">옵션 간 여백</Label>
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

        <div className="space-y-1.5 border-t border-border pt-3">
          <Label className="text-xs">미리보기</Label>
          <div
            className="flex overflow-hidden rounded-md border border-dashed border-border bg-muted/20 p-1"
            style={{ height: GRID_ROW_HEIGHT_PX * component.size.h }}
          >
            <GridComponentBody component={component} value={previewValue} onChange={setPreviewValue} />
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}

function AlignToggle({ value, onChange }: { value: GridAlign; onChange: (align: GridAlign) => void }) {
  const options: { value: GridAlign; Icon: typeof AlignLeft }[] = [
    { value: "left", Icon: AlignLeft },
    { value: "center", Icon: AlignCenter },
    { value: "right", Icon: AlignRight },
  ];
  return (
    <div className="flex gap-1">
      {options.map(({ value: v, Icon }) => (
        <button
          key={v}
          type="button"
          aria-label={v}
          onClick={() => onChange(v)}
          className={cn(
            "flex size-8 items-center justify-center rounded-md border",
            value === v
              ? "border-primary bg-primary text-primary-foreground"
              : "border-input bg-background text-muted-foreground hover:bg-accent",
          )}
        >
          <Icon className="size-4" />
        </button>
      ))}
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
  const options: { value: GridOptionsDirection; label: string }[] = [
    { value: "row", label: "가로" },
    { value: "column", label: "세로" },
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
          {OPTIONS_GAP_LABELS[g]}
        </button>
      ))}
    </div>
  );
}
