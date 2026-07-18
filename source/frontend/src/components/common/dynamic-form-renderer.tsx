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
  type GridFormSchema,
  type GridFormValues,
  type GridInputComponent,
  type GridLabelComponent,
} from "@/components/common/form-schema";

/**
 * 요청자용 그리드 폼 렌더 컴포넌트 — SCR-SRM-002(2026-07-18 유지보수 요청, form.io 완전 제거
 * → 자체 8×n 그리드). `schema.components`의 position/size를 CSS Grid(8열)로 그대로 배치한다.
 * `type=label`은 값 입력 없는 정적 텍스트로 렌더(5.4절), 입력 컴포넌트는 더 이상 캡션(label)을
 * 갖지 않는다(라벨이 필요하면 관리자가 별도 label 컴포넌트를 인접 셀에 배치, for/aria-label
 * 연결 없음 — 5.4절 확정 사항). 유효성 오류는 필드별 인라인이 아니라 제출 클릭 시
 * `components` 배열 순서상 첫 번째 위반 1건만 폼 하단에 표시한다(5.5절, 서버 재검증은
 * common/form/FormSubmissionValidator, api_spec/common.md 0-2절도 동일 계약). 제출/취소
 * 버튼은 하단 우측 정렬 푸터(취소→제출 순). `hideFooter`는 CatalogManagePage의 pre-view
 * 축소판 전용.
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
      if (comp.type === "label") continue;
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
        {schema.components.map((comp) =>
          comp.type === "label" ? (
            <GridLabelCell key={comp.key} component={comp} />
          ) : (
            <GridFieldControl
              key={comp.key}
              component={comp}
              value={values[comp.key]}
              disabled={disabled}
              onChange={(v) => setValue(comp.key, v)}
            />
          ),
        )}
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

function GridLabelCell({ component }: { component: GridLabelComponent }) {
  return (
    <div
      style={{
        gridColumn: `${component.position.col + 1} / span ${component.size.w}`,
        gridRow: `${component.position.row + 1} / span ${component.size.h}`,
      }}
      className={cn(
        "overflow-hidden p-1 text-sm text-foreground",
        textAlignFor(component.textAlign),
      )}
    >
      {component.text}
    </div>
  );
}

interface GridFieldControlProps {
  component: GridInputComponent;
  value: unknown;
  disabled?: boolean;
  onChange: (value: unknown) => void;
}

function GridFieldControl({ component, value, disabled, onChange }: GridFieldControlProps) {
  const readOnly = component.input?.readOnly || disabled;
  const widthPercent = component.input?.widthPercent ?? 90;
  const options = useMemo(() => parseOptions(component.options), [component.options]);
  const fieldId = `gf-${component.key}`;
  const resolvedValue = value ?? component.input?.defaultValue ?? undefined;

  return (
    <div
      style={{
        gridColumn: `${component.position.col + 1} / span ${component.size.w}`,
        gridRow: `${component.position.row + 1} / span ${component.size.h}`,
      }}
      className="flex h-full overflow-hidden p-1"
    >
      <div
        style={{ width: `${widthPercent}%` }}
        className={cn("h-full min-h-0 overflow-auto", justifyFor(component.input?.align))}
      >
        {renderControl(component, fieldId, resolvedValue, readOnly, options, onChange)}
      </div>
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
  switch (component.type) {
    case "textarea":
      return (
        <Textarea
          id={id}
          className="h-full resize-none"
          value={(value as string) ?? ""}
          readOnly={readOnly}
          disabled={readOnly}
          onChange={(e) => onChange(e.target.value)}
        />
      );
    case "date":
      return (
        <DateFieldControl id={id} value={value} disabled={readOnly} onChange={onChange} />
      );
    case "file":
      return <FileFieldControl id={id} disabled={readOnly} onChange={onChange} />;
    case "select":
      return (
        <Select
          value={(value as string) ?? ""}
          onValueChange={(v) => onChange(v)}
          disabled={readOnly}
        >
          <SelectTrigger id={id}>
            <SelectValue placeholder="선택" />
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
          readOnly={readOnly}
          disabled={readOnly}
          onChange={(e) => onChange(e.target.value)}
        />
      );
  }
}

/** date 필드 — 아이콘 클릭 시 숨겨진 네이티브 input의 피커를 열고, 선택값은 아이콘 옆 텍스트로 표시(5.4절). */
function DateFieldControl({
  id,
  value,
  disabled,
  onChange,
}: {
  id: string;
  value: unknown;
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
    <div className="flex items-center gap-1.5 overflow-hidden">
      <button
        type="button"
        onClick={openPicker}
        disabled={disabled}
        aria-label="날짜 선택"
        className="flex shrink-0 items-center justify-center rounded-md border border-input bg-background p-1.5 text-muted-foreground hover:bg-accent disabled:cursor-not-allowed disabled:opacity-50"
      >
        <Calendar className="size-4" />
      </button>
      <span className="truncate text-xs text-muted-foreground">{stringValue}</span>
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

/** file 필드 — 아이콘 클릭 시 숨겨진 네이티브 input을 트리거, 선택 파일명은 아이콘 옆 텍스트로 표시(5.4절). 제출 값 자체는 base64 데이터 URL 문자열(api_spec/common.md 0-2절). */
function FileFieldControl({
  id,
  disabled,
  onChange,
}: {
  id: string;
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
    <div className="flex items-center gap-1.5 overflow-hidden">
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={disabled}
        aria-label="파일 선택"
        className="flex shrink-0 items-center justify-center rounded-md border border-input bg-background p-1.5 text-muted-foreground hover:bg-accent disabled:cursor-not-allowed disabled:opacity-50"
      >
        <FileIcon className="size-4" />
      </button>
      <span className="truncate text-xs text-muted-foreground">{fileName ?? ""}</span>
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
