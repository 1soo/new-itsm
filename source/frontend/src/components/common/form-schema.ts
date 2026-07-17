import type { TFunction } from "i18next";

/**
 * 동적 폼 스키마 계약 — form.io Form JSON 타입 별칭(2026-07-17 유지보수 요청, form.io 전환).
 * DynamicFormBuilder가 편집(SCR-SRM-007), DynamicFormRenderer가 렌더(SCR-SRM-002)를 담당한다.
 *
 * 기존 FormFieldSchema/FormFieldType/validateForm/hasOptions는 SRM+ESM 화면이 모두
 * 새 컴포넌트로 전환 완료할 때까지(field-builder.tsx/dynamic-form.tsx가 삭제될 때까지)
 * 하위 호환을 위해 남겨둔다 — 전환 완료 후 이 파일 하단 "레거시" 구획째 제거한다.
 */
export type FormIoDisplay = "form" | "pdf" | "wizard";

export interface FormIoSchema {
  display?: FormIoDisplay;
  components: unknown[];
}

export type FormIoSubmissionData = Record<string, unknown>;

/**
 * FormBuilder/Form의 options.builder 팔레트 구성(common.md 8.2절).
 * Basic+Advanced+Layout만 노출하고 Data/Premium/Resource는 완전히 숨긴다.
 *
 * form.io는 컴포넌트별로 고정된 기본 그룹(`builderInfo.group`)을 갖지만, 팔레트에
 * 실제로 표시되는 그룹은 각 그룹의 `components` 맵에 그 키가 `true`(또는 커스텀
 * 디스크립터)로 선언돼 있는지로 결정된다 — `advanced.components.file = true`처럼
 * 컴포넌트 고유 그룹(`file`은 원래 'premium')과 다른 그룹에 선언해도 팔레트에는
 * 선언한 그룹(advanced) 아래 노출된다(WebformBuilder 내부 로직 확인, 2026-07-17
 * TC-SRM-002 결함 수정 — 이전엔 `file`을 별도 `premium` 그룹에 남겨둬 8.2절과
 * 다르게 Premium 탭 자체가 노출되고 File이 그 아래 표시됐다). 그룹당 노출하려는
 * 항목만 `true`, **같은 그룹의 나머지 기본 항목은 전부 명시적으로 `false`** 로
 * 감춰야 한다(생략하면 기본값 그대로 노출됨).
 */
export const FORM_BUILDER_OPTIONS = {
  /**
   * form.io는 폼에 submit 액션의 button 컴포넌트가 하나도 없으면 편집 시작 시 기본 "Submit"
   * 버튼을 스키마에 자동 추가한다(WebformBuilder 내부 동작). `button`을 팔레트에서 숨겨(위
   * Basic 그룹) 관리자가 직접 추가할 수 없게 했더라도 이 자동 추가는 막지 못해, 저장된
   * 스키마에 의도치 않은 "Submit" 버튼이 포함되고 DynamicFormRenderer의 신규 하단
   * 제출/취소 푸터와 중복 노출되는 결함이 있었다(TC-SRM-105, 2026-07-17). 자동 추가 자체를
   * 끈다.
   */
  noAddSubmitButton: true,
  noDefaultSubmitButton: true,
  builder: {
    basic: {
      title: "Basic",
      weight: 0,
      components: {
        textfield: true,
        textarea: true,
        number: true,
        checkbox: true,
        selectboxes: true,
        select: true,
        radio: true,
        password: false,
        button: false,
      },
    },
    advanced: {
      title: "Advanced",
      weight: 10,
      components: {
        email: true,
        phoneNumber: true,
        datetime: true,
        file: true,
        url: false,
        tags: false,
        address: false,
        day: false,
        time: false,
        currency: false,
        survey: false,
        signature: false,
      },
    },
    layout: {
      title: "Layout",
      weight: 20,
      components: {
        columns: true,
        panel: true,
        tabs: true,
        fieldset: true,
        htmlelement: false,
        content: false,
        table: false,
        well: false,
      },
    },
    data: false,
    premium: false,
    resource: false,
  },
} as const;

/* ----------------------------------------------------------------------
 * 레거시(field-builder.tsx/dynamic-form.tsx 전용) — 삭제 예정
 * -------------------------------------------------------------------- */
export type FormFieldType = "text" | "textarea" | "select" | "number" | "date" | "file";

export interface FormFieldSchema {
  key: string;
  label: string;
  type: FormFieldType;
  required?: boolean;
  options?: string[];
}

export type FormValues = Record<string, unknown>;
export type FormErrors = Record<string, string>;

export function hasOptions(type: FormFieldType): boolean {
  return type === "select";
}

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
