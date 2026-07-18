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
export { DynamicFormBuilder, type DynamicFormBuilderProps } from "./dynamic-form-builder";
export { DynamicFormRenderer, type DynamicFormRendererProps } from "./dynamic-form-renderer";
export {
  GRID_COLUMNS,
  GRID_PALETTE_TYPES,
  GRID_PREVIEW_SCALE,
  GRID_ROW_HEIGHT_PX,
  EMPTY_GRID_SCHEMA,
  hasGridOptions,
  hasOptionsLayoutUi,
  hasPlaceholderUi,
  gridMaxHeight,
  type GridAlign,
  type GridComponentType,
  type GridOptionsDirection,
  type GridOptionsGap,
  type GridPosition,
  type GridSize,
  type GridComponentInput,
  type GridComponentValidation,
  type GridInputComponentType,
  type GridInputComponent,
  type GridLabelComponent,
  type GridGuideTextComponent,
  type GridGuideFileComponent,
  type GridComponent,
  type GridFormSchema,
  type GridFormValues,
} from "./form-schema";
/** ESM은 레거시 EAV 그대로 사용(screen/service-request.md 5절, SRM 전용 그리드 전환 대상 아님) */
export { DynamicForm } from "./dynamic-form";
export { FieldBuilder } from "./field-builder";
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
