import { useMemo, useRef, useState, type ChangeEvent } from "react";
import { Calendar, File as FileIcon, Info } from "lucide-react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import {
  GRID_COLUMNS,
  GRID_ROW_HEIGHT_PX,
  hasRegexUi,
  type GridAlign,
  type GridAlignVertical,
  type GridComponent,
  type GridFormSchema,
  type GridFormValues,
  type GridGuideFileComponent,
  type GridGuideTextComponent,
  type GridInputComponent,
  type GridLabel,
  type GridPosition,
  type GridSize,
} from "@/components/common/form-schema";

/**
 * 요청자용 그리드 폼 렌더 컴포넌트 — SCR-SRM-002(2026-07-18 유지보수 요청, form.io 완전 제거
 * → 자체 8×n 그리드). `schema.components`의 position/size를 CSS Grid(8열)로 그대로 배치한다.
 * `type=guide-text`/`type=guide-file`은 값 입력 없는 정적 컴포넌트로 렌더(5.4절), 입력
 * 컴포넌트는 캡션(label)을 갖지 않는다(그리드 직접 배치형 label은 폐기되고 컴포넌트에 부여하는
 * 라벨(태그)로 대체됨 — 5.8절). 라벨(태그) 경계 그룹(테두리+legend 텍스트, `GridLabelOverlays`)은
 * 2026-07-18 유지보수 요청 8차로 캔버스 전용에서 이 렌더러까지 확대돼, 게이팅 없이 요청 제출 폼과
 * A1 축소 미리보기 둘 다에 노출된다(dynamic-form-builder.tsx와 로직·스타일 공유, 로직 중복 금지).
 * 입력 컴포넌트·guide-text는 가로(`align`/`textAlign`)+세로(`verticalAlign`/`textVerticalAlign`,
 * 2026-07-18 유지보수 요청 4차 신규, 기본 top) 9방향 정렬을 지원한다. 유효성 오류는 필드별
 * 인라인이 아니라 제출 클릭 시 `components` 배열 순서상 첫 번째 위반 1건만 폼 하단에 표시한다
 * (5.5절, regex는 `type=text` 전용 — 4차로 축소. 서버 재검증은 common/form/FormSubmissionValidator,
 * api_spec/common.md 0-2절도 동일 계약). 제출/취소 버튼은 하단 우측 정렬 푸터(취소→제출 순).
 * `hideFooter`는 CatalogManagePage의 카탈로그 항목 저장 폼과 무관하며, 빌더 팝업 캔버스=미리보기
 * 통합과 A1 축소 미리보기 재사용 전용. 컴포넌트별 렌더 로직(`GridComponentBody`)은 export해
 * 빌더 팝업의 캔버스 카드·개별 실시간 미리보기(dynamic-form-builder.tsx)가 그대로 재사용한다
 * (로직 중복 구현 금지). date/file/select는 placeholder 미지정 시 하드코딩 기본 문구 없이 완전히
 * 빈 상태로 표시한다(2026-07-18 유지보수 요청 8차). 내부 텍스트는 `useTranslation(["service-request",
 * "common"])` 기반 i18n 키를 사용한다(8차로 "관리자 전용이라 범위 밖" 방침 해제).
 */
export interface DynamicFormRendererProps {
  schema: GridFormSchema;
  submissionData?: GridFormValues;
  onSubmit: (data: GridFormValues) => void;
  onCancel?: () => void;
  submitLabel?: string;
  cancelLabel?: string;
  disabled?: boolean;
  hideFooter?: boolean;
  className?: string;
}

const OPTIONS_GAP_CLASS: Record<1 | 2 | 3, string> = { 1: "gap-1", 2: "gap-2", 3: "gap-3" };
/** radio/checkbox 옵션 묶음 정렬 — row는 주축(justify-content), column은 교차축(align-items)에 반영. */
const OPTIONS_JUSTIFY_CLASS: Record<GridAlign, string> = {
  left: "justify-start",
  center: "justify-center",
  right: "justify-end",
};
const OPTIONS_ITEMS_CLASS: Record<GridAlign, string> = {
  left: "items-start",
  center: "items-center",
  right: "items-end",
};

function isEmptyValue(value: unknown): boolean {
  return value == null || value === "" || (Array.isArray(value) && value.length === 0);
}

function justifyFor(align: GridAlign | undefined): string {
  if (align === "left") return "mr-auto";
  if (align === "right") return "ml-auto";
  return "mx-auto";
}

function textAlignFor(align: GridAlign | undefined): string {
  if (align === "center") return "text-center";
  if (align === "right") return "text-right";
  return "text-left";
}

/** 세로 정렬(2026-07-18 유지보수 요청 4차 신규) — flex 컨테이너의 align-items/justify-content 매핑, 기본 top. */
function verticalAlignItemsFor(align: GridAlignVertical | null | undefined): string {
  if (align === "middle") return "items-center";
  if (align === "bottom") return "items-end";
  return "items-start";
}

function verticalAlignJustifyFor(align: GridAlignVertical | undefined): string {
  if (align === "middle") return "justify-center";
  if (align === "bottom") return "justify-end";
  return "justify-start";
}

export function parseOptions(options: string | null | undefined): string[] {
  return (options ?? "")
    .split(",")
    .map((o) => o.trim())
    .filter((o) => o.length > 0);
}

export interface GridLabelOverlaysProps {
  components: GridComponent[];
  labels: GridLabel[];
  /** 이동/리사이즈 중인 임시 위치·크기를 반영하려는 호출측(빌더 캔버스)이 오버라이드. 기본값은
   * 저장된 position/size 그대로(렌더러 기본 동작). */
  getPosition?: (component: GridComponent) => GridPosition;
  getSize?: (component: GridComponent) => GridSize;
}

/**
 * 라벨(태그) 경계 그룹(테두리+legend 텍스트) 오버레이 — 참조 컴포넌트가 1개 이상이면 항상
 * 렌더링하고, 테두리 관련 클래스·`borderColor`만 `label.showBorder !== false`일 때 적용한다
 * (2026-07-18 유지보수 요청 6차 결함 수정, legend 텍스트는 showBorder와 무관하게 항상 표시).
 * 그리드 라인 좌표(`gridColumn`/`gridRow`)를 그대로 쓰므로 8칸 CSS Grid를 쓰는 캔버스
 * (dynamic-form-builder.tsx)와 이 렌더러 양쪽에서 픽셀 환산 없이 공유한다(8차, 로직 중복 금지).
 */
export function GridLabelOverlays({
  components,
  labels,
  getPosition = (c) => c.position,
  getSize = (c) => c.size,
}: GridLabelOverlaysProps) {
  return (
    <>
      {labels.map((label) => {
        const refs = components.filter((c) => c.labelId === label.id);
        if (refs.length < 1) return null;
        const minCol = Math.min(...refs.map((c) => getPosition(c).col));
        const minRow = Math.min(...refs.map((c) => getPosition(c).row));
        const maxCol = Math.max(...refs.map((c) => getPosition(c).col + getSize(c).w));
        const maxRow = Math.max(...refs.map((c) => getPosition(c).row + getSize(c).h));
        const showBorder = label.showBorder !== false;
        return (
          <div
            key={label.id}
            style={{
              gridColumn: `${minCol + 1} / ${maxCol + 1}`,
              gridRow: `${minRow + 1} / ${maxRow + 1}`,
              borderColor: showBorder ? label.borderColor : undefined,
            }}
            className={cn("pointer-events-none relative -m-[2px] rounded-md", showBorder && "border-2")}
          >
            <span
              className="absolute left-2 -top-[7px] truncate rounded-sm bg-background/80 px-1 text-[10px] font-medium"
              style={{ color: label.textColor }}
            >
              {label.text}
            </span>
          </div>
        );
      })}
    </>
  );
}

export function DynamicFormRenderer({
  schema,
  submissionData,
  onSubmit,
  onCancel,
  submitLabel,
  cancelLabel,
  disabled,
  hideFooter,
  className,
}: DynamicFormRendererProps) {
  const { t } = useTranslation(["service-request", "common"]);
  const [values, setValues] = useState<GridFormValues>(() => submissionData ?? {});
  const [formError, setFormError] = useState<string | null>(null);

  const rowCount = useMemo(() => {
    return schema.components.reduce(
      (max, c) => Math.max(max, c.position.row + c.size.h),
      1,
    );
  }, [schema.components]);

  const setValue = (key: string, value: unknown) => {
    setValues((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
    for (const comp of schema.components) {
      if (comp.type === "guide-text" || comp.type === "guide-file") continue;
      const value = values[comp.key];
      const empty = isEmptyValue(value);
      if (comp.validation?.required && empty) {
        setFormError(t("dynamicForm.renderer.requiredError", { defaultValue: "필수 항목을 입력하세요." }));
        return;
      }
      if (!empty && hasRegexUi(comp.type) && comp.validation?.regex) {
        try {
          const re = new RegExp(comp.validation.regex);
          if (!re.test(String(value))) {
            setFormError(t("dynamicForm.renderer.patternError", { defaultValue: "입력 형식이 올바르지 않습니다." }));
            return;
          }
        } catch {
          /* 잘못된 정규식은 클라이언트 검증에서 무시(서버 재검증에 위임) */
        }
      }
    }
    setFormError(null);
    onSubmit(values);
  };

  return (
    <div className={cn("flex flex-col gap-4", className)}>
      <div
        className="grid gap-2"
        style={{
          gridTemplateColumns: `repeat(${GRID_COLUMNS}, minmax(0, 1fr))`,
          gridAutoRows: `${GRID_ROW_HEIGHT_PX}px`,
          minHeight: rowCount * GRID_ROW_HEIGHT_PX,
        }}
      >
        {schema.components.map((comp) => {
          const isStatic = comp.type === "guide-text" || comp.type === "guide-file";
          return (
            <div
              key={comp.key}
              style={{
                gridColumn: `${comp.position.col + 1} / span ${comp.size.w}`,
                gridRow: `${comp.position.row + 1} / span ${comp.size.h}`,
              }}
              className="flex h-full overflow-hidden p-1"
            >
              <GridComponentBody
                component={comp}
                value={isStatic ? undefined : values[comp.key]}
                disabled={disabled}
                onChange={isStatic ? undefined : (v) => setValue(comp.key, v)}
              />
            </div>
          );
        })}

        <GridLabelOverlays components={schema.components} labels={schema.labels ?? []} />
      </div>

      {formError ? (
        <p role="alert" className="text-sm text-destructive">
          {formError}
        </p>
      ) : null}

      {hideFooter ? null : (
        <div className="flex justify-end gap-2">
          {onCancel ? (
            <Button type="button" variant="outline" onClick={onCancel} disabled={disabled}>
              {cancelLabel ?? t("dynamicForm.renderer.cancel", { defaultValue: "취소" })}
            </Button>
          ) : null}
          <Button type="button" onClick={handleSubmit} disabled={disabled}>
            {submitLabel ?? t("dynamicForm.renderer.submit", { defaultValue: "제출" })}
          </Button>
        </div>
      )}
    </div>
  );
}

export interface GridComponentBodyProps {
  component: GridComponent;
  value?: unknown;
  disabled?: boolean;
  onChange?: (value: unknown) => void;
}

/**
 * 컴포넌트 단독 렌더링(그리드 포지셔닝 없이 본문만) — 메인 그리드와 빌더 팝업의 개별 실시간
 * 미리보기(dynamic-form-builder.tsx)가 동일 로직을 공유한다.
 */
export function GridComponentBody({ component, value, disabled, onChange }: GridComponentBodyProps) {
  if (component.type === "guide-text") {
    return <StaticGuideTextBody component={component} />;
  }
  if (component.type === "guide-file") {
    return <StaticGuideFileBody component={component} />;
  }

  const readOnly = component.input?.readOnly || disabled;
  const widthPercent = component.input?.widthPercent ?? 90;
  const options = useMemo(() => parseOptions(component.options), [component.options]);
  const fieldId = `gf-${component.key}`;
  const defaultValue = component.input?.defaultValue;
  // checkbox만 배열 기본값, 나머지 유형은 단일 문자열(2026-07-18 유지보수 요청 8차).
  const fallbackValue =
    component.type === "checkbox"
      ? Array.isArray(defaultValue)
        ? defaultValue
        : undefined
      : typeof defaultValue === "string"
        ? defaultValue
        : undefined;
  const resolvedValue = value ?? fallbackValue;
  const isTextarea = component.type === "textarea";

  return (
    <div
      className={cn(
        "flex h-full w-full min-h-0",
        isTextarea ? "items-stretch" : verticalAlignItemsFor(component.input?.verticalAlign),
      )}
    >
      <div
        style={{ width: `${widthPercent}%` }}
        className={cn(isTextarea ? "h-full" : "min-h-0", "overflow-auto", justifyFor(component.input?.align))}
      >
        {renderControl(component, fieldId, resolvedValue, readOnly, options, onChange ?? (() => {}))}
      </div>
    </div>
  );
}

/** 안내 텍스트 전용(정적, 안내 아이콘으로 시각 구분 — 5.4절). textVerticalAlign(4차 신규)은 flex-col justify로 반영. */
function StaticGuideTextBody({ component }: { component: GridGuideTextComponent }) {
  return (
    <div
      className={cn(
        "flex h-full w-full flex-col gap-1 overflow-hidden text-sm text-foreground",
        verticalAlignJustifyFor(component.textVerticalAlign),
      )}
    >
      <Info className="size-3.5 shrink-0 text-muted-foreground" />
      <p className={cn("whitespace-pre-wrap break-words", textAlignFor(component.textAlign))}>
        {component.text}
      </p>
    </div>
  );
}

/** 첨부 가이드 파일 전용(텍스트 없음, 다운로드 링크만 — 5.4절). */
function StaticGuideFileBody({ component }: { component: GridGuideFileComponent }) {
  const { t } = useTranslation(["service-request", "common"]);
  return (
    <div className="flex h-full w-full items-center overflow-hidden text-sm text-foreground">
      {component.file ? (
        <a
          href={component.file.dataUrl}
          download={component.file.name}
          className="inline-flex w-fit items-center gap-1 truncate text-xs text-primary underline"
        >
          <FileIcon className="size-3.5 shrink-0" />
          <span className="truncate">{component.file.name}</span>
        </a>
      ) : (
        <span className="text-xs text-muted-foreground">
          {t("dynamicForm.renderer.guideFileEmpty", { defaultValue: "첨부된 파일이 없습니다." })}
        </span>
      )}
    </div>
  );
}

function renderControl(
  component: GridInputComponent,
  id: string,
  value: unknown,
  readOnly: boolean | undefined,
  options: string[],
  onChange: (value: unknown) => void,
) {
  const placeholder = component.input?.placeholder ?? undefined;

  switch (component.type) {
    case "textarea":
      return (
        <Textarea
          id={id}
          className="h-full resize-none"
          value={(value as string) ?? ""}
          placeholder={placeholder}
          readOnly={readOnly}
          disabled={readOnly}
          onChange={(e) => onChange(e.target.value)}
        />
      );
    case "date":
      return (
        <DateFieldControl
          id={id}
          value={value}
          placeholder={placeholder}
          disabled={readOnly}
          onChange={onChange}
        />
      );
    case "file":
      return (
        <FileFieldControl id={id} placeholder={placeholder} disabled={readOnly} onChange={onChange} />
      );
    case "select":
      return (
        <Select
          value={(value as string) ?? ""}
          onValueChange={(v) => onChange(v)}
          disabled={readOnly}
        >
          <SelectTrigger id={id}>
            <SelectValue placeholder={placeholder} />
          </SelectTrigger>
          <SelectContent>
            {options.map((o) => (
              <SelectItem key={o} value={o}>
                {o}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      );
    case "radio": {
      const selected = (value as string) ?? "";
      const direction = component.optionsDirection ?? "row";
      const gapClass = OPTIONS_GAP_CLASS[component.optionsGap ?? 1];
      const align = component.input?.align ?? "center";
      return (
        <div
          className={cn(
            "overflow-y-auto",
            direction === "row"
              ? cn("flex flex-row flex-wrap", OPTIONS_JUSTIFY_CLASS[align])
              : cn("flex flex-col", OPTIONS_ITEMS_CLASS[align]),
            gapClass,
          )}
        >
          {options.map((o) => (
            <label key={o} className="flex items-center gap-1.5 break-words text-xs">
              <input
                type="radio"
                className="accent-primary size-3.5 shrink-0"
                name={id}
                checked={selected === o}
                disabled={readOnly}
                onChange={() => onChange(o)}
              />
              {o}
            </label>
          ))}
        </div>
      );
    }
    case "checkbox": {
      const selected = Array.isArray(value) ? (value as string[]) : [];
      const direction = component.optionsDirection ?? "row";
      const gapClass = OPTIONS_GAP_CLASS[component.optionsGap ?? 1];
      const align = component.input?.align ?? "center";
      return (
        <div
          className={cn(
            "overflow-y-auto",
            direction === "row"
              ? cn("flex flex-row flex-wrap", OPTIONS_JUSTIFY_CLASS[align])
              : cn("flex flex-col", OPTIONS_ITEMS_CLASS[align]),
            gapClass,
          )}
        >
          {options.map((o) => {
            const checked = selected.includes(o);
            return (
              <label key={o} className="flex items-center gap-1.5 break-words text-xs">
                <Checkbox
                  checked={checked}
                  disabled={readOnly}
                  onCheckedChange={(next) =>
                    onChange(next ? [...selected, o] : selected.filter((s) => s !== o))
                  }
                />
                {o}
              </label>
            );
          })}
        </div>
      );
    }
    case "text":
    default:
      return (
        <Input
          id={id}
          type="text"
          value={(value as string) ?? ""}
          placeholder={placeholder}
          readOnly={readOnly}
          disabled={readOnly}
          onChange={(e) => onChange(e.target.value)}
        />
      );
  }
}

/** date 필드 — 입력 박스(폭%/정렬은 상위 wrapper 적용)+박스 우측 아이콘, 박스 전체가 클릭 영역(5.4절 롤백). */
function DateFieldControl({
  id,
  value,
  placeholder,
  disabled,
  onChange,
}: {
  id: string;
  value: unknown;
  placeholder?: string;
  disabled?: boolean;
  onChange: (value: unknown) => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const stringValue = (value as string) ?? "";

  const openPicker = () => {
    const el = inputRef.current;
    if (!el) return;
    if (typeof el.showPicker === "function") {
      el.showPicker();
    } else {
      el.click();
    }
  };

  return (
    <div className="relative w-full">
      <button
        type="button"
        onClick={openPicker}
        disabled={disabled}
        className="flex w-full items-center justify-between gap-1.5 rounded-md border border-input bg-background px-3 py-2 text-left text-sm shadow-xs disabled:cursor-not-allowed disabled:opacity-50"
      >
        <span className={cn("truncate", !stringValue && "text-muted-foreground")}>
          {stringValue || placeholder}
        </span>
        <Calendar className="size-4 shrink-0 text-muted-foreground" />
      </button>
      <input
        ref={inputRef}
        id={id}
        type="date"
        className="sr-only"
        value={stringValue}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}

/** file 필드 — 입력 박스+박스 우측 아이콘, 박스 전체가 클릭 영역(5.4절 롤백). 제출 값 자체는 base64 데이터 URL 문자열(api_spec/common.md 0-2절). */
function FileFieldControl({
  id,
  placeholder,
  disabled,
  onChange,
}: {
  id: string;
  placeholder?: string;
  disabled?: boolean;
  onChange: (value: unknown) => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [fileName, setFileName] = useState<string | null>(null);

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) {
      setFileName(null);
      onChange(null);
      return;
    }
    setFileName(file.name);
    const reader = new FileReader();
    reader.onload = () => onChange(reader.result);
    reader.readAsDataURL(file);
  };

  return (
    <div className="relative w-full">
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={disabled}
        className="flex w-full items-center justify-between gap-1.5 rounded-md border border-input bg-background px-3 py-2 text-left text-sm shadow-xs disabled:cursor-not-allowed disabled:opacity-50"
      >
        <span className={cn("truncate", !fileName && "text-muted-foreground")}>
          {fileName || placeholder}
        </span>
        <FileIcon className="size-4 shrink-0 text-muted-foreground" />
      </button>
      <input
        ref={inputRef}
        id={id}
        type="file"
        className="sr-only"
        disabled={disabled}
        onChange={handleFileChange}
      />
    </div>
  );
}
