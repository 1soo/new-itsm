import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import type {
  FormErrors,
  FormFieldSchema,
  FormValues,
} from "@/components/common/form-schema";

/**
 * 스키마 기반 동적 폼 렌더러 — SCR-SRM-002.
 * 제어 컴포넌트: values/onChange로 값 관리, errors로 인라인 오류 표시.
 * 지원 유형은 api_spec formSchema와 동일(text/textarea/select/number/date/file).
 * 필수 검증·제출·API는 기능 레이어(FE)가 담당한다(validateForm 헬퍼 제공).
 */
export interface DynamicFormProps {
  schema: FormFieldSchema[];
  values: FormValues;
  onChange: (values: FormValues) => void;
  errors?: FormErrors;
  disabled?: boolean;
  className?: string;
}

export function DynamicForm({
  schema,
  values,
  onChange,
  errors,
  disabled,
  className,
}: DynamicFormProps) {
  const setValue = (key: string, value: unknown) => {
    onChange({ ...values, [key]: value });
  };

  return (
    <div className={cn("flex flex-col gap-4", className)}>
      {schema.map((field) => {
        const error = errors?.[field.key];
        const fieldId = `df-${field.key}`;

        return (
          <div key={field.key} className="flex flex-col gap-1.5">
            <Label htmlFor={fieldId}>
              {field.label}
              {field.required ? (
                <span className="ml-0.5 text-destructive" aria-hidden="true">
                  *
                </span>
              ) : null}
            </Label>

            {renderControl(field, fieldId, values[field.key], setValue, {
              disabled,
              invalid: !!error,
              describedBy: error ? `${fieldId}-error` : undefined,
            })}

            {error ? (
              <p id={`${fieldId}-error`} className="text-xs text-destructive">
                {error}
              </p>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}

interface ControlMeta {
  disabled?: boolean;
  invalid: boolean;
  describedBy?: string;
}

function renderControl(
  field: FormFieldSchema,
  id: string,
  value: unknown,
  setValue: (key: string, value: unknown) => void,
  meta: ControlMeta,
) {
  const common = {
    id,
    disabled: meta.disabled,
    "aria-invalid": meta.invalid || undefined,
    "aria-describedby": meta.describedBy,
  };

  switch (field.type) {
    case "number":
      return (
        <Input
          {...common}
          type="number"
          value={(value as number | string) ?? ""}
          onChange={(e) =>
            setValue(field.key, e.target.value === "" ? "" : Number(e.target.value))
          }
        />
      );
    case "date":
      return (
        <Input
          {...common}
          type="date"
          value={(value as string) ?? ""}
          onChange={(e) => setValue(field.key, e.target.value)}
        />
      );
    case "file":
      return (
        <Input
          {...common}
          type="file"
          onChange={(e) => setValue(field.key, e.target.files?.[0] ?? null)}
        />
      );
    case "select":
      return (
        <Select
          disabled={meta.disabled}
          value={(value as string) ?? ""}
          onValueChange={(v) => setValue(field.key, v)}
        >
          <SelectTrigger id={id} aria-invalid={meta.invalid || undefined}>
            <SelectValue placeholder="선택" />
          </SelectTrigger>
          <SelectContent>
            {(field.options ?? []).map((o) => (
              <SelectItem key={o} value={o}>
                {o}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      );
    case "textarea":
      return (
        <Textarea
          {...common}
          value={(value as string) ?? ""}
          onChange={(e) => setValue(field.key, e.target.value)}
        />
      );
    case "text":
    default:
      return (
        <Input
          {...common}
          type="text"
          value={(value as string) ?? ""}
          onChange={(e) => setValue(field.key, e.target.value)}
        />
      );
  }
}
