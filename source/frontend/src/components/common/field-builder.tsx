import { Plus, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { EmptyState } from "@/components/common/empty-state";
import { cn } from "@/lib/utils";
import {
  hasOptions,
  type FormFieldSchema,
  type FormFieldType,
} from "@/components/common/form-schema";

/**
 * 양식 필드 빌더 — SCR-SRM-007 반복 입력.
 * 동적 필드(라벨·유형·필수[+옵션])를 정의해 FormFieldSchema[] 를 생성한다.
 * 저장·검증은 기능 레이어(FE)가 담당한다.
 */
const FIELD_TYPE_LABELS: Record<FormFieldType, string> = {
  text: "한 줄 텍스트",
  number: "숫자",
  select: "단일 선택",
  date: "날짜",
  file: "첨부파일",
};

export interface FieldBuilderProps {
  value: FormFieldSchema[];
  onChange: (fields: FormFieldSchema[]) => void;
  className?: string;
}

function newKey() {
  return `field_${Math.random().toString(36).slice(2, 8)}`;
}

export function FieldBuilder({ value, onChange, className }: FieldBuilderProps) {
  const update = (index: number, patch: Partial<FormFieldSchema>) => {
    onChange(value.map((f, i) => (i === index ? { ...f, ...patch } : f)));
  };

  const add = () => {
    onChange([...value, { key: newKey(), label: "", type: "text", required: false }]);
  };

  const remove = (index: number) => {
    onChange(value.filter((_, i) => i !== index));
  };

  const setOptions = (index: number, raw: string) => {
    const options = raw
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);
    update(index, { options });
  };

  return (
    <div className={cn("flex flex-col gap-3", className)}>
      {value.length === 0 ? (
        <EmptyState
          title="정의된 필드가 없습니다"
          description="아래 버튼으로 양식 필드를 추가하세요."
        />
      ) : (
        value.map((field, index) => (
          <div
            key={field.key}
            className="flex flex-col gap-3 rounded-lg border border-border bg-card p-3"
          >
            <div className="grid gap-3 sm:grid-cols-[1fr_10rem_auto]">
              <div className="flex flex-col gap-1.5">
                <Label htmlFor={`${field.key}-label`}>라벨</Label>
                <Input
                  id={`${field.key}-label`}
                  value={field.label}
                  placeholder="필드 라벨"
                  onChange={(e) => update(index, { label: e.target.value })}
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>유형</Label>
                <Select
                  value={field.type}
                  onValueChange={(v) =>
                    update(index, {
                      type: v as FormFieldType,
                      options: hasOptions(v as FormFieldType) ? (field.options ?? []) : undefined,
                    })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {(Object.keys(FIELD_TYPE_LABELS) as FormFieldType[]).map((t) => (
                      <SelectItem key={t} value={t}>
                        {FIELD_TYPE_LABELS[t]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-end justify-end pb-1">
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  aria-label="필드 삭제"
                  onClick={() => remove(index)}
                >
                  <Trash2 className="text-destructive" />
                </Button>
              </div>
            </div>

            {hasOptions(field.type) ? (
              <div className="flex flex-col gap-1.5">
                <Label htmlFor={`${field.key}-opts`}>옵션 (쉼표로 구분)</Label>
                <Input
                  id={`${field.key}-opts`}
                  placeholder="예: 낮음, 보통, 높음"
                  value={(field.options ?? []).join(", ")}
                  onChange={(e) => setOptions(index, e.target.value)}
                />
              </div>
            ) : null}

            <div className="flex items-center gap-2">
              <Checkbox
                id={`${field.key}-req`}
                checked={!!field.required}
                onCheckedChange={(c) => update(index, { required: c === true })}
              />
              <Label htmlFor={`${field.key}-req`} className="font-normal">
                필수 입력
              </Label>
            </div>
          </div>
        ))
      )}

      <Button type="button" variant="outline" onClick={add} className="self-start">
        <Plus /> 필드 추가
      </Button>
    </div>
  );
}
