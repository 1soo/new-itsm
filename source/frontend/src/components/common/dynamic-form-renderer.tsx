import { useMemo, useState } from "react";

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
} from "@/components/common/form-schema";

/**
 * 요청자용 그리드 폼 렌더 컴포넌트 — SCR-SRM-002(2026-07-18 유지보수 요청, form.io 완전 제거
 * → 자체 8×n 그리드). `schema.components`의 position/size를 CSS Grid(8열)로 그대로 배치한다.
 * 클라이언트 검증은 `validation.regex`(정규식)·`validation.required`만 수행(서버 재검증은
 * common/form/FormSubmissionValidator, api_spec/common.md 0-2절). 제출/취소 버튼은 하단
 * 우측 정렬 푸터(취소→제출 순). `hideFooter`는 CatalogManagePage의 pre-view 축소판 전용.
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

function requiredErrorMessage(label: string): string {
  return `${label}은(는) 필수 항목입니다.`;
}

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
  const [errors, setErrors] = useState<Record<string, string>>({});

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
    const nextErrors: Record<string, string> = {};
    for (const comp of schema.components) {
      const value = values[comp.key];
      const empty = isEmptyValue(value);
      if (comp.validation?.required && empty) {
        nextErrors[comp.key] = requiredErrorMessage(comp.label);
        continue;
      }
      if (!empty && comp.validation?.regex) {
        try {
          const re = new RegExp(comp.validation.regex);
          if (!re.test(String(value))) {
            nextErrors[comp.key] = PATTERN_ERROR_MESSAGE;
          }
        } catch {
          /* 잘못된 정규식은 클라이언트 검증에서 무시(서버 재검증에 위임) */
        }
      }
    }
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;
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
        {schema.components.map((comp) => (
          <GridFieldControl
            key={comp.key}
            component={comp}
            value={values[comp.key]}
            error={errors[comp.key]}
            disabled={disabled}
            onChange={(v) => setValue(comp.key, v)}
          />
        ))}
      </div>

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

interface GridFieldControlProps {
  component: GridComponent;
  value: unknown;
  error?: string;
  disabled?: boolean;
  onChange: (value: unknown) => void;
}

function GridFieldControl({ component, value, error, disabled, onChange }: GridFieldControlProps) {
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
      className="flex flex-col gap-1 overflow-hidden p-1"
    >
      <label
        htmlFor={fieldId}
        className={cn("truncate text-xs font-medium text-foreground", textAlignFor(component.labelAlign))}
      >
        {component.label}
        {component.validation?.required ? (
          <span className="ml-0.5 text-destructive" aria-hidden="true">
            *
          </span>
        ) : null}
      </label>

      <div
        style={{ width: `${widthPercent}%` }}
        className={cn("min-h-0 flex-1 overflow-auto", justifyFor(component.input?.align))}
      >
        {renderControl(component, fieldId, resolvedValue, readOnly, options, onChange)}
      </div>

      {error ? (
        <p className="truncate text-[10px] text-destructive" title={error}>
          {error}
        </p>
      ) : null}
    </div>
  );
}

function renderControl(
  component: GridComponent,
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
        <Input
          id={id}
          type="date"
          value={(value as string) ?? ""}
          readOnly={readOnly}
          disabled={readOnly}
          onChange={(e) => onChange(e.target.value)}
        />
      );
    case "file":
      return (
        <Input
          id={id}
          type="file"
          disabled={readOnly}
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (!file) {
              onChange(null);
              return;
            }
            const reader = new FileReader();
            reader.onload = () => onChange(reader.result);
            reader.readAsDataURL(file);
          }}
        />
      );
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
