import { useTranslation } from "react-i18next";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ApprovalStepProgress } from "@/components/common/approval-step-progress";
import type { ApprovalRequestStatus, ApprovalStep } from "@/components/common/approval-schema";

/**
 * 공용 승인 패널(읽기 전용, 압축 표시) — 도메인 상세 화면(SRM/CHG/INC 등)이 재사용하는 작은 패널.
 * `ApprovalStepProgress`를 compact 모드로 감싸 차수 진행 상태 + 반려 사유만 보여준다.
 * 결정(승인/반려) 액션은 SCR-COM-014(승인 대기함)에서만 수행한다.
 * 매칭되는 승인 프로세스가 없을 때의 표현은 도메인마다 달라(문구·노출 여부) FE가 props로 주입한다.
 * `targetStateLabel`/`status`/`onResubmit`(2026-07-22 유지보수 요청, 확정 방침 4): 최신 인스턴스가
 * REJECTED면 재승인요청 버튼을 노출한다 — 실제 API-COM-006 호출·성공 후 갱신은 호출측(FE)이
 * `onResubmit`으로 주입한다(이 컴포넌트는 프레젠테이션만 담당, 소유권 판정 없이 버튼만 노출).
 */
export interface ApprovalPanelProps {
  /** 이 티켓/전이에 매칭되는 승인 프로세스가 있는지 */
  matched: boolean;
  /** matched=true일 때 표시할 차수 목록(API-COM-004 steps) */
  steps?: ApprovalStep[];
  /** 인스턴스의 현재 대기 차수 */
  currentStepNo?: number | null;
  /** matched=false일 때 안내 문구(도메인마다 문구 상이). 미지정 시 패널을 렌더링하지 않는다. */
  emptyMessage?: string;
  /** 인스턴스의 targetState 표시명 — "어느 상태로 가기 위한 승인인지" 패널 타이틀 옆에 노출. */
  targetStateLabel?: string | null;
  /** 인스턴스 상태(REJECTED일 때만 재승인요청 버튼 노출). */
  status?: ApprovalRequestStatus | null;
  /** 재승인요청 버튼 클릭 핸들러(API-COM-006 호출은 호출측 담당). 미지정 시 버튼 미노출. */
  onResubmit?: () => void;
  /** 재승인요청 진행 중 로딩 표시(호출측 상태 주입). */
  resubmitting?: boolean;
  className?: string;
}

export function ApprovalPanel({
  matched,
  steps = [],
  currentStepNo,
  emptyMessage,
  targetStateLabel,
  status,
  onResubmit,
  resubmitting = false,
  className,
}: ApprovalPanelProps) {
  const { t } = useTranslation("common");
  const showResubmitButton = status === "REJECTED" && !!onResubmit;

  if (!matched) {
    if (!emptyMessage) return null;
    return (
      <Card className={cn(className)}>
        <CardHeader>
          <CardTitle>{t("approval.panelTitle")}</CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <p className="text-sm text-muted-foreground">{emptyMessage}</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={cn(className)}>
      <CardHeader className="flex-row items-center justify-between gap-2 space-y-0">
        <CardTitle>{t("approval.panelTitle")}</CardTitle>
        {targetStateLabel ? (
          <span className="text-xs text-muted-foreground">
            {t("approval.targetStateLabel", { targetState: targetStateLabel, defaultValue: `대상 상태: ${targetStateLabel}` })}
          </span>
        ) : null}
      </CardHeader>
      <CardContent className="flex flex-col gap-3 pt-0">
        <ApprovalStepProgress steps={steps} currentStepNo={currentStepNo} compact />
        {showResubmitButton ? (
          <div className="flex justify-end">
            <Button size="sm" variant="outline" loading={resubmitting} onClick={onResubmit}>
              {t("approval.resubmitButton", { defaultValue: "재승인요청" })}
            </Button>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}
