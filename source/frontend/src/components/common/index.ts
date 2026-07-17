/** 공통 컴포넌트 배럴 — dev-frontend가 기능 화면 조립 시 사용 */
export { StatusBadge, type StatusTone } from "./status-badge";
export { PriorityBadge, type Priority } from "./priority-badge";
export { toast } from "./toast";
export { ConfirmDialog } from "./confirm-dialog";
export { Modal } from "./modal";
export { UserGuideOverview, UserGuideDomainSection, UserGuideRoleSection } from "./user-guide-content";
export { MultiSelect, type MultiSelectOption } from "./multi-select";
export { Pagination } from "./pagination";
export { DataTable, type Column } from "./data-table";
export { EmptyState } from "./empty-state";
export { ForbiddenView } from "./forbidden-view";
export { NotFoundView } from "./not-found-view";
export { TicketListLayout } from "./ticket-list-layout";
export { TicketDetailLayout } from "./ticket-detail-layout";
export { Timeline, type TimelineItem } from "./timeline";
export { KpiCard } from "./kpi-card";
export { TrendChart, type TrendPoint } from "./trend-chart";
export { DistributionChart, type DistributionDatum } from "./distribution-chart";
export { Rating } from "./rating";
export { DynamicFormBuilder } from "./dynamic-form-builder";
export { DynamicFormRenderer } from "./dynamic-form-renderer";
export {
  FORM_BUILDER_OPTIONS,
  type FormIoDisplay,
  type FormIoSchema,
  type FormIoSubmissionData,
} from "./form-schema";
/** @deprecated field-builder.tsx/dynamic-form.tsx 전환 완료 후 제거 예정(SRM+ESM 화면 전환 완료 시) */
export { DynamicForm } from "./dynamic-form";
/** @deprecated field-builder.tsx/dynamic-form.tsx 전환 완료 후 제거 예정(SRM+ESM 화면 전환 완료 시) */
export { FieldBuilder } from "./field-builder";
/** @deprecated form-schema.ts 레거시 구획, 전환 완료 후 제거 예정 */
export {
  validateForm,
  hasOptions,
  type FormFieldType,
  type FormFieldSchema,
  type FormValues,
  type FormErrors,
} from "./form-schema";
export {
  type ApprovalMatchType,
  type ApprovalStepDecisionMode,
  type ApprovalStepStatus,
  type ApprovalRoleDecisionValue,
  type ApprovalStepRole,
  type ApprovalStep,
} from "./approval-schema";
export { ApprovalProcessFlow, type ApprovalRoleOption, type ApprovalStepBoxValue } from "./approval-process-flow";
export { ApprovalStepProgress } from "./approval-step-progress";
export { ApprovalPanel } from "./approval-panel";
