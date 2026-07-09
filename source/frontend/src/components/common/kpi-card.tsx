import { type ReactNode } from "react";

import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

/**
 * KPI 카드 — SCR-SRM-008 핵심 지표 수치.
 * 라벨과 수치(+선택 단위)를 표시한다. Base 강조(값 상단 액센트 바).
 */
export interface KpiCardProps {
  label: string;
  value: ReactNode;
  unit?: string;
  className?: string;
}

export function KpiCard({ label, value, unit, className }: KpiCardProps) {
  return (
    <Card className={cn("border-t-2 border-t-primary", className)}>
      <CardContent className="space-y-1.5 p-5">
        <p className="truncate text-sm text-muted-foreground">{label}</p>
        <p className="text-2xl font-semibold text-foreground">
          {value}
          {unit ? (
            <span className="ml-1 text-base font-normal text-muted-foreground">
              {unit}
            </span>
          ) : null}
        </p>
      </CardContent>
    </Card>
  );
}
