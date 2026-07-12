import type { TFunction } from "i18next";

/**
 * 동적 폼 스키마 계약 — service-request api_spec의 formSchema와 동일 형태.
 * FieldBuilder가 스키마를 생성하고(SCR-SRM-007), DynamicForm이 소비해 렌더한다(SCR-SRM-002).
 */
export type FormFieldType = "text" | "select" | "number" | "date" | "file";

export interface FormFieldSchema {
  /** 값 저장 키(고유) */
  key: string;
  label: string;
  type: FormFieldType;
  required?: boolean;
  /** select 전용 옵션 목록 */
  options?: string[];
}

export type FormValues = Record<string, unknown>;
export type FormErrors = Record<string, string>;

/** select 유형만 옵션을 가진다 */
export function hasOptions(type: FormFieldType): boolean {
  return type === "select";
}

/**
 * 필수 필드 검증 — 제출 전 호출. 빈 값(빈 문자열/빈 배열/undefined)이면 오류.
 * 반환된 FormErrors를 DynamicForm errors prop에 전달하면 인라인 표시된다.
 * `t`(caller의 `useTranslation` 훅 결과) 전달 시 `common:validation.required` 키로 다국어 전환,
 * 미전달 시 기존 하드코딩 한국어 문구로 폴백(도메인 i18n 전환 전 호출부와 하위 호환).
 */
export function validateForm(
  schema: FormFieldSchema[],
  values: FormValues,
  t?: TFunction,
): FormErrors {
  const errors: FormErrors = {};
  for (const field of schema) {
    if (!field.required) continue;
    const v = values[field.key];
    const empty = v == null || v === "" || (Array.isArray(v) && v.length === 0);
    if (empty) {
      errors[field.key] = t
        ? t("validation.required", {
            ns: "common",
            label: field.label,
            defaultValue: `${field.label}은(는) 필수 항목입니다.`,
          })
        : `${field.label}은(는) 필수 항목입니다.`;
    }
  }
  return errors;
}
