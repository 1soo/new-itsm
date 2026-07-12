import { useTranslation } from "react-i18next";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { ApprovalStepProgress } from "@/components/common/approval-step-progress";
import type { ApprovalStep } from "@/components/common/approval-schema";

/**
 * 공용 승인 패널(읽기 전용, 압축 표시) — 도메인 상세 화면(SRM/CHG/INC 등)이 재사용하는 작은 패널.
 * `ApprovalStepProgress`를 compact 모드로 감싸 차수 진행 상태 + 반려 사유만 보여준다.
 * 결정(승인/반려) 액션은 SCR-COM-014(승인 대기함)에서만 수행한다.
 * 매칭되는 승인 프로세스가 없을 때의 표현은 도메인마다 달라(문구·노출 여부) FE가 props로 주입한다.
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
  className?: string;
}

export function ApprovalPanel({
  matched,
  steps = [],
  currentStepNo,
  emptyMessage,
  className,
}: ApprovalPanelProps) {
  const { t } = useTranslation("common");
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
      <CardHeader>
        <CardTitle>{t("approval.panelTitle")}</CardTitle>
      </CardHeader>
      <CardContent className="pt-0">
        <ApprovalStepProgress steps={steps} currentStepNo={currentStepNo} compact />
      </CardContent>
    </Card>
  );
}
