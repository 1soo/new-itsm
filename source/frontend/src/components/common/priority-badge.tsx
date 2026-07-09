import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

/**
 * 우선순위 배지 — common.md SCR-COM-007: "P1~P4 등급 · Danger~Muted".
 * P1(최상위 긴급) → Danger, P2 → Warning, P3 → Info, P4 → Muted.
 */
export type Priority = "P1" | "P2" | "P3" | "P4";

const PRIORITY_TONE = {
  P1: "danger",
  P2: "warning",
  P3: "info",
  P4: "muted",
} as const;

export interface PriorityBadgeProps {
  priority: Priority;
  /** 표시 라벨(미지정 시 등급 코드 표시) */
  label?: string;
  className?: string;
}

export function PriorityBadge({ priority, label, className }: PriorityBadgeProps) {
  return (
    <Badge variant={PRIORITY_TONE[priority]} className={cn(className)}>
      {label ?? priority}
    </Badge>
  );
}
