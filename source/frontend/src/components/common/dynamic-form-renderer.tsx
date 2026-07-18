import { useMemo, useRef, useState, type ChangeEvent } from "react";
import { Calendar, File as FileIcon } from "lucide-react";

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
  type GridAlign,
  type GridComponent,
  type GridFormSchema,
  type GridFormValues,
  type GridGuideComponent,
  type GridInputComponent,
  type GridLabelComponent,
} from "@/components/common/form-schema";

/**
 * 요청자용 그리드 폼 렌더 컴포넌트 — SCR-SRM-002(2026-07-18 유지보수 요청, form.io 완전 제거
 * → 자체 8×n 그리드). `schema.components`의 position/size를 CSS Grid(8열)로 그대로 배치한다.
 * `type=label`/`type=guide`는 값 입력 없는 정적 컴포넌트로 렌더(5.4절), 입력 컴포넌트는 더 이상
 * 캡션(label)을 갖지 않는다(라벨이 필요하면 관리자가 별도 label 컴포넌트를 인접 셀에 배치, for/
 * aria-label 연결 없음 — 5.4절 확정 사항). 유효성 오류는 필드별 인라인이 아니라 제출 클릭 시
 * `components` 배열 순서상 첫 번째 위반 1건만 폼 하단에 표시한다(5.5절, 서버 재검증은
 * common/form/FormSubmissionValidator, api_spec/common.md 0-2절도 동일 계약). 제출/취소
 * 버튼은 하단 우측 정렬 푸터(취소→제출 순). `hideFooter`는 CatalogManagePage의 pre-view
 * 축소판 전용. 컴포넌트별 렌더 로직(`GridComponentBody`)은 export해 빌더 팝업의 개별 실시간
 * 미리보기(dynamic-form-builder.tsx)가 그대로 재사용한다(로직 중복 구현 금지).
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

const REQUIRED_ERROR_MESSAGE = "필수 항목을 입력하세요.";
const PATTERN_ERROR_MESSAGE = "입력 형식이 올바르지 않습니다.";
const DEFAULT_DATE_PLACEHOLDER = "날짜를 선택하세요";
const DEFAULT_FILE_PLACEHOLDER = "파일을 선택하세요";

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

function parseOptions(options: string | null | undefined): string[] {
  return (options ?? "")
    .split(",")
    .map((o) => o.trim())
    .filter((o) => o.length > 0);
}

export function DynamicFormRenderer({
  schema,
  submissionData,
  onSubmit,
  onCancel,
  submitLabel = "제출",
  cancelLabel = "취소",
  disabled,
  hideFooter,
  className,
}: DynamicFormRendererProps) {
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
      if (comp.type === "label" || comp.type === "guide") continue;
      const value = values[comp.key];
      const empty = isEmptyValue(value);
      if (comp.validation?.required && empty) {
        setFormError(REQUIRED_ERROR_MESSAGE);
        return;
      }
      if (!empty && comp.validation?.regex) {
        try {
          const re = new RegExp(comp.validation.regex);
          if (!re.test(String(value))) {
            setFormError(PATTERN_ERROR_MESSAGE);
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
          const isStatic = comp.type === "label" || comp.type === "guide";
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
              {cancelLabel}
            </Button>
          ) : null}
          <Button type="button" onClick={handleSubmit} disabled={disabled}>
            {submitLabel}
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
  if (component.type === "label") {
    return <StaticLabelBody component={component} />;
  }
  if (component.type === "guide") {
    return <StaticGuideBody component={component} />;
  }

  const readOnly = component.input?.readOnly || disabled;
  const widthPercent = component.input?.widthPercent ?? 90;
  const options = useMemo(() => parseOptions(component.options), [component.options]);
  const fieldId = `gf-${component.key}`;
  const resolvedValue = value ?? component.input?.defaultValue ?? undefined;

  return (
    <div
      style={{ width: `${widthPercent}%` }}
      className={cn("h-full min-h-0 overflow-auto", justifyFor(component.input?.align))}
    >
      {renderControl(component, fieldId, resolvedValue, readOnly, options, onChange ?? (() => {}))}
    </div>
  );
}

function StaticLabelBody({ component }: { component: GridLabelComponent }) {
  return (
    <div
      className={cn(
        "h-full w-full overflow-hidden text-sm text-foreground",
        textAlignFor(component.textAlign),
      )}
    >
      {component.text}
    </div>
  );
}

function StaticGuideBody({ component }: { component: GridGuideComponent }) {
  return (
    <div className="flex h-full w-full flex-col gap-1 overflow-auto text-sm text-foreground">
      <p className="whitespace-pre-wrap break-words">{component.guideText}</p>
      {component.file ? (
        <a
          href={component.file.dataUrl}
          download={component.file.name}
          className="inline-flex w-fit items-center gap-1 truncate text-xs text-primary underline"
        >
          <FileIcon className="size-3.5 shrink-0" />
          <span className="truncate">{component.file.name}</span>
        </a>
      ) : null}
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
            <SelectValue placeholder={placeholder ?? "선택"} />
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
      return (
        <div className="flex flex-col gap-1 overflow-y-auto">
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
      return (
        <div className="flex flex-col gap-1 overflow-y-auto">
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
          {stringValue || placeholder || DEFAULT_DATE_PLACEHOLDER}
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
          {fileName || placeholder || DEFAULT_FILE_PLACEHOLDER}
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
