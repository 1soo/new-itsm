import type { TFunction } from "i18next";

/**
 * 동적 폼 그리드 스키마 계약 — SRM 전용 자체 8×n 그리드 빌더(2026-07-18 유지보수 요청,
 * form.io 완전 제거 → 자체 구현). DynamicFormBuilder가 편집(SCR-SRM-007 "Form 설정" 팝업),
 * DynamicFormRenderer가 렌더(SCR-SRM-002)를 담당한다. screen/service-request.md 5절 계약.
 */
export const GRID_COLUMNS = 8;

/** 그리드 행 높이(px, 고정값 — 5.2절 "세로 행 높이는 고정값을 사용"). 빌더 캔버스·렌더러·pre-view가 공유. */
export const GRID_ROW_HEIGHT_PX = 88;

export type GridComponentType =
  | "text"
  | "textarea"
  | "select"
  | "radio"
  | "checkbox"
  | "date"
  | "file";

/** 팔레트 노출 순서(7종, service-request.md 5.3절). */
export const GRID_PALETTE_TYPES: readonly GridComponentType[] = [
  "text",
  "textarea",
  "select",
  "radio",
  "checkbox",
  "date",
  "file",
];

/** select/radio/checkbox 등 옵션 목록이 필요한 유형. */
export function hasGridOptions(type: GridComponentType): boolean {
  return type === "select" || type === "radio" || type === "checkbox";
}

/** 유형별 높이(h) 상한. textarea만 제약 없음(5.2절). */
export function gridMaxHeight(type: GridComponentType): number {
  return type === "textarea" ? Infinity : 2;
}

export type GridAlign = "left" | "center" | "right";

export interface GridPosition {
  col: number; // 0~7
  row: number; // 0 이상
}

export interface GridSize {
  w: number; // 1~2
  h: number; // 1~2(textarea는 높이 제약 없음)
}

export interface GridComponentInput {
  widthPercent?: number; // 기본값 90
  align?: GridAlign; // 기본값 center
  defaultValue?: string | null;
  readOnly?: boolean;
}

export interface GridComponentValidation {
  required?: boolean;
  regex?: string | null; // 선택 입력, 미지정 시 형식 검증 없음
}

export interface GridComponent {
  key: string;
  type: GridComponentType;
  label: string;
  position: GridPosition;
  size: GridSize;
  labelAlign?: GridAlign; // 기본값 left
  input?: GridComponentInput;
  validation?: GridComponentValidation;
  /** select/radio/checkbox 전용, 콤마(,) 구분 텍스트. */
  options?: string | null;
  /** select/radio/checkbox 옵션 설정 UI의 "CI 연계" 라디오 자리 표시용(실제 동작 없음, 향후 확장). */
  ciLinked?: boolean;
}

export interface GridFormSchema {
  components: GridComponent[];
}

export type GridFormValues = Record<string, unknown>;

export const EMPTY_GRID_SCHEMA: GridFormSchema = { components: [] };

/* ----------------------------------------------------------------------
 * 레거시(ESM field-builder.tsx/dynamic-form.tsx 전용) — ESM은 레거시 EAV 그대로
 * 사용하므로(screen/service-request.md 5절 참고) SRM의 form.io 제거와 무관하게 유지한다.
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
