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
  | "file"
  | "label"
  | "guide-text"
  | "guide-file";

/** 팔레트 노출 순서(10종, service-request.md 5.3절 — label/guide-text/guide-file은 값 입력 없는 정적 컴포넌트). */
export const GRID_PALETTE_TYPES: readonly GridComponentType[] = [
  "text",
  "textarea",
  "select",
  "radio",
  "checkbox",
  "date",
  "file",
  "label",
  "guide-text",
  "guide-file",
];

/** 축소 비율 — 빌더 팝업 3분할 미리보기(5.6절)가 사용. 기존 CatalogManagePage 외부 pre-view와 동일 비율 재사용. */
export const GRID_PREVIEW_SCALE = 0.45;

/** select/radio/checkbox 등 옵션 목록이 필요한 유형. */
export function hasGridOptions(type: GridComponentType): boolean {
  return type === "select" || type === "radio" || type === "checkbox";
}

/** placeholder 입력 UI가 노출되는 유형(5.4절 — 단일 값 표시 컨트롤을 갖는 유형만, radio/checkbox 제외). */
export function hasPlaceholderUi(type: GridComponentType): boolean {
  return type === "text" || type === "textarea" || type === "select" || type === "date" || type === "file";
}

/** 옵션 배치 방향·여백 설정 UI가 노출되는 유형(5.4절 — radio/checkbox만, select는 배치 개념 없어 제외). */
export function hasOptionsLayoutUi(type: GridComponentType): boolean {
  return type === "radio" || type === "checkbox";
}

/** 유형별 높이(h) 상한. textarea만 제약 없음(5.2절), label/guide-text/guide-file은 다른 비-textarea 타입과 동일하게 2. */
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
  placeholder?: string | null; // 선택, Content 설정 UI는 hasPlaceholderUi(type)인 유형에만 노출
  defaultValue?: string | null;
  readOnly?: boolean;
}

export interface GridComponentValidation {
  required?: boolean;
  regex?: string | null; // 선택 입력, 미지정 시 형식 검증 없음
}

/** 배치 방향(row=가로, column=세로)·여백 3단계(1=좁게~3=넓게). radio/checkbox 전용(5.4절), 기본값은 row/1. */
export type GridOptionsDirection = "row" | "column";
export type GridOptionsGap = 1 | 2 | 3;

/** 7개 입력 컴포넌트 유형(label/labelAlign 속성 없음, guide-text/guide-file도 값 입력 없어 제외 — 5.4절). */
export type GridInputComponentType = Exclude<GridComponentType, "label" | "guide-text" | "guide-file">;

export interface GridInputComponent {
  key: string;
  type: GridInputComponentType;
  position: GridPosition;
  size: GridSize;
  input?: GridComponentInput;
  validation?: GridComponentValidation;
  /** select/radio/checkbox 전용, 콤마(,) 구분 텍스트. */
  options?: string | null;
  /** select/radio/checkbox 옵션 설정 UI의 "CI 연계" 라디오 자리 표시용(실제 동작 없음, 향후 확장). */
  ciLinked?: boolean;
  /** radio/checkbox 전용 옵션 배치 방향(기본 row, hasOptionsLayoutUi(type)인 유형만 편집 UI 노출). */
  optionsDirection?: GridOptionsDirection | null;
  /** radio/checkbox 전용 옵션 간 여백 3단계(기본 1, hasOptionsLayoutUi(type)인 유형만 편집 UI 노출). */
  optionsGap?: GridOptionsGap | null;
}

/** 값 입력이 없는 정적 텍스트 전용 컴포넌트(5.4절). 입력 컴포넌트와 for/aria-label 연결 없음. */
export interface GridLabelComponent {
  key: string;
  type: "label";
  position: GridPosition;
  size: GridSize;
  text: string;
  textAlign?: GridAlign; // 기본값 left
}

/** 값 입력이 없는 정적 안내 텍스트 전용 컴포넌트(guide 2종 분리, 5.4절). label과 구조는 동일하나 "안내문"으로 시각 구분해 렌더링. */
export interface GridGuideTextComponent {
  key: string;
  type: "guide-text";
  position: GridPosition;
  size: GridSize;
  text: string;
  textAlign?: GridAlign; // 기본값 left
}

/** 값 입력이 없는 정적 첨부 가이드 파일 전용 컴포넌트(guide 2종 분리, 5.4절). 첨부 파일은 base64 데이터 URL로 인라인 저장, 제출·검증 대상에서 제외. */
export interface GridGuideFileComponent {
  key: string;
  type: "guide-file";
  position: GridPosition;
  size: GridSize;
  file: { name: string; dataUrl: string } | null;
}

export type GridComponent =
  | GridInputComponent
  | GridLabelComponent
  | GridGuideTextComponent
  | GridGuideFileComponent;

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
