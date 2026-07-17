import { useRef } from "react";
import { FormBuilder } from "@formio/react";
import type { FormType } from "@formio/react";

import "@formio/js/dist/formio.builder.min.css";

import { FORM_BUILDER_OPTIONS, type FormIoSchema } from "@/components/common/form-schema";

/**
 * 관리자용 폼 설계 컴포넌트 — SCR-SRM-007/SCR-ESM-006(← field-builder.tsx 대체).
 * `@formio/react` FormBuilder 래핑. `initialForm`으로 편집 모드 진입, `onChange`로
 * 최신 Form JSON을 상위 상태에 축적만 하고(자동저장 없음) 저장은 호출측의 명시적
 * "저장" 버튼이 담당한다(common.md 8.1/8.4절).
 */
export interface DynamicFormBuilderProps {
  initialForm?: FormIoSchema;
  onChange: (form: FormIoSchema) => void;
  className?: string;
}

export function DynamicFormBuilder({ initialForm, onChange, className }: DynamicFormBuilderProps) {
  const initialFormRef = useRef(initialForm ?? { display: "form", components: [] });

  return (
    <div className={`formio-scope ${className ?? ""}`}>
      <FormBuilder
        initialForm={initialFormRef.current as unknown as FormType}
        options={FORM_BUILDER_OPTIONS}
        onChange={(form: FormType) => onChange(forceFileBase64Storage(form as unknown as FormIoSchema))}
      />
    </div>
  );
}

/** file 컴포넌트 storage 기본값을 'base64'로 강제(common.md 8.2절, 별도 스토리지 프로바이더 미사용) */
function forceFileBase64Storage(form: FormIoSchema): FormIoSchema {
  const patch = (components: unknown[]): unknown[] =>
    components.map((c) => {
      if (typeof c !== "object" || c === null) return c;
      const component = c as Record<string, unknown>;
      const next: Record<string, unknown> = { ...component };
      if (next.type === "file" && !next.storage) {
        next.storage = "base64";
      }
      if (Array.isArray(next.components)) {
        next.components = patch(next.components);
      }
      if (Array.isArray(next.columns)) {
        next.columns = (next.columns as Record<string, unknown>[]).map((col) => ({
          ...col,
          components: Array.isArray(col.components) ? patch(col.components as unknown[]) : col.components,
        }));
      }
      return next;
    });

  return { ...form, components: patch(form.components) };
}
