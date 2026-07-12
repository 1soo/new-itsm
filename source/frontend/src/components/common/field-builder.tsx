import { Plus, Trash2 } from "lucide-react";
import { useTranslation } from "react-i18next";

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
 * 저장·검증은 기능 레이어(FE)가 담당한다. 문구는 `common:fieldBuilder.*` 키(2026-07-12 다국어 지원).
 */
const FIELD_TYPE_LABEL_KEY: Record<FormFieldType, string> = {
  text: "fieldBuilder.fieldType.text",
  number: "fieldBuilder.fieldType.number",
  select: "fieldBuilder.fieldType.select",
  date: "fieldBuilder.fieldType.date",
  file: "fieldBuilder.fieldType.file",
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
  const { t } = useTranslation("common");
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
          title={t("fieldBuilder.emptyTitle")}
          description={t("fieldBuilder.emptyDescription")}
        />
      ) : (
        value.map((field, index) => (
          <div
            key={field.key}
            className="flex flex-col gap-3 rounded-lg border border-border bg-card p-3"
          >
            <div className="grid gap-3 sm:grid-cols-[1fr_10rem_auto]">
              <div className="flex flex-col gap-1.5">
                <Label htmlFor={`${field.key}-label`}>{t("fieldBuilder.labelLabel")}</Label>
                <Input
                  id={`${field.key}-label`}
                  value={field.label}
                  placeholder={t("fieldBuilder.labelPlaceholder")}
                  onChange={(e) => update(index, { label: e.target.value })}
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>{t("fieldBuilder.typeLabel")}</Label>
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
                    {(Object.keys(FIELD_TYPE_LABEL_KEY) as FormFieldType[]).map((fieldType) => (
                      <SelectItem key={fieldType} value={fieldType}>
                        {t(FIELD_TYPE_LABEL_KEY[fieldType])}
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
                  aria-label={t("fieldBuilder.removeFieldAria")}
                  onClick={() => remove(index)}
                >
                  <Trash2 className="text-destructive" />
                </Button>
              </div>
            </div>

            {hasOptions(field.type) ? (
              <div className="flex flex-col gap-1.5">
                <Label htmlFor={`${field.key}-opts`}>{t("fieldBuilder.optionsLabel")}</Label>
                <Input
                  id={`${field.key}-opts`}
                  placeholder={t("fieldBuilder.optionsPlaceholder")}
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
                {t("fieldBuilder.requiredLabel")}
              </Label>
            </div>
          </div>
        ))
      )}

      <Button type="button" variant="outline" onClick={add} className="self-start">
        <Plus /> {t("fieldBuilder.addField")}
      </Button>
    </div>
  );
}
