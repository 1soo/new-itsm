import { Check, CircleDashed, X } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { ApprovalStep, ApprovalStepStatus } from "@/components/common/approval-schema";

/**
 * 승인 차수 진행 현황(순수 프레젠테이션) — API-COM-004 응답을 그대로 렌더링한다.
 * common.md SCR-COM-014(승인 대기함 상세)·도메인 상세 화면 공용 패널(`approval-panel.tsx`)이 함께 사용.
 * 승인/반려 액션·반려 사유 입력은 담당하지 않는다(FE가 별도로 조립).
 */
export interface ApprovalStepProgressProps {
  /** 전체 차수(1차부터 순서대로) */
  steps: ApprovalStep[];
  /** 인스턴스의 현재 대기 차수(`currentStepNo`). 지정 시 해당 차수만 Warning 강조 + 역할별 결정 상세 노출 */
  currentStepNo?: number | null;
  /** 압축 표시(도메인 상세 공용 패널용) — 차수별 역할 결정 상세를 생략하고 반려 사유만 노출 */
  compact?: boolean;
  className?: string;
}

const ROLE_DECISION_LABEL = { PENDING: "대기", APPROVE: "승인", REJECT: "반려" } as const;
const ROLE_DECISION_TONE = { PENDING: "muted", APPROVE: "success", REJECT: "danger" } as const;

function stepTone(step: ApprovalStep, currentStepNo?: number | null) {
  if (step.status === "APPROVED") return "success" as const;
  if (step.status === "REJECTED") return "danger" as const;
  if (step.status === "SKIPPED") return "muted" as const;
  return step.stepNo === currentStepNo ? ("warning" as const) : ("muted" as const);
}

const STEP_LABEL: Record<ReturnType<typeof stepTone>, string> = {
  success: "완료",
  danger: "반려",
  warning: "대기중",
  muted: "대기",
};

function StepDot({ status }: { status: ApprovalStepStatus }) {
  const Icon = status === "APPROVED" ? Check : status === "REJECTED" ? X : CircleDashed;
  return (
    <span
      className={cn(
        "flex size-5 shrink-0 items-center justify-center rounded-full",
        status === "APPROVED" && "bg-success text-success-foreground",
        status === "REJECTED" && "bg-danger text-danger-foreground",
        status === "SKIPPED" && "bg-neutral-subtle text-neutral-subtle-foreground",
        status === "PENDING" && "bg-neutral-subtle text-neutral-subtle-foreground",
      )}
      aria-hidden="true"
    >
      <Icon className="size-3" />
    </span>
  );
}

export function ApprovalStepProgress({
  steps,
  currentStepNo,
  compact = false,
  className,
}: ApprovalStepProgressProps) {
  const currentStep = currentStepNo != null ? steps.find((s) => s.stepNo === currentStepNo) : undefined;
  const rejectedStep = steps.find((s) => s.status === "REJECTED");
  const rejectedReason = rejectedStep?.roles.find((r) => r.decision === "REJECT")?.reason;

  return (
    <div className={cn("flex flex-col gap-3", className)}>
      <ol className="flex flex-col gap-2">
        {steps.map((step) => (
          <li key={step.stepNo} className="flex items-center gap-2">
            <StepDot status={step.status} />
            <span className="text-sm text-foreground">{step.stepNo}차</span>
            <Badge variant={stepTone(step, currentStepNo)} className="ml-auto">
              {STEP_LABEL[stepTone(step, currentStepNo)]}
            </Badge>
          </li>
        ))}
      </ol>

      {!compact && currentStep ? (
        <div className="flex flex-col gap-2 rounded-lg border border-border bg-card p-3">
          <p className="text-xs font-medium text-muted-foreground">
            {currentStep.stepNo}차 역할별 결정 현황 (
            {currentStep.decisionMode === "AND" ? "전체 승인 필요" : "역할 중 하나"})
          </p>
          {currentStep.decisionMode === "AND" ? (
            <ul className="flex flex-col gap-1.5">
              {currentStep.roles.map((r) => (
                <li key={r.roleCode} className="flex flex-col gap-0.5 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-foreground">{r.roleName}</span>
                    <Badge variant={ROLE_DECISION_TONE[r.decision]}>{ROLE_DECISION_LABEL[r.decision]}</Badge>
                  </div>
                  {r.decision !== "PENDING" ? (
                    <p className="text-xs text-muted-foreground">
                      {[r.decidedBy, r.reason].filter(Boolean).join(" · ")}
                    </p>
                  ) : null}
                </li>
              ))}
            </ul>
          ) : (
            <OrStepSummary roles={currentStep.roles} />
          )}
        </div>
      ) : null}

      {rejectedStep ? (
        <p className="flex items-start gap-1.5 text-xs text-destructive">
          <X className="size-3.5 shrink-0 translate-y-0.5" aria-hidden="true" />
          <span>
            {rejectedStep.stepNo}차 반려{rejectedReason ? ` 사유: ${rejectedReason}` : ""}
          </span>
        </p>
      ) : null}
    </div>
  );
}

function OrStepSummary({ roles }: { roles: ApprovalStep["roles"] }) {
  const decided = roles.find((r) => r.decision !== "PENDING");
  const decision = decided?.decision ?? "PENDING";
  return (
    <div className="flex flex-col gap-0.5 text-sm">
      <div className="flex items-center justify-between">
        <span className="text-foreground">역할 중 하나</span>
        <Badge variant={ROLE_DECISION_TONE[decision]}>{ROLE_DECISION_LABEL[decision]}</Badge>
      </div>
      {decided ? (
        <p className="text-xs text-muted-foreground">
          {[decided.roleName, decided.decidedBy, decided.reason].filter(Boolean).join(" · ")}
        </p>
      ) : null}
    </div>
  );
}
