import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

/**
 * 상태 배지 — common.md 2.1절 상태 시맨틱 매핑.
 * 도메인별 상태 라벨은 기능 레이어(FE)가 tone과 함께 주입한다.
 *
 * - success : 완료·해결·게시·SLA 준수
 * - warning : 대기·검토·SLA 임박·만료 임박
 * - danger  : 실패·SLA 위반·SEV1·반려
 * - info    : 진행중·정보 안내
 * - muted   : 종료·비활성·초안
 */
export type StatusTone = "success" | "warning" | "danger" | "info" | "muted";

export interface StatusBadgeProps {
  tone: StatusTone;
  label: string;
  className?: string;
  /** Lozenge 강조 단계. 기본 subtle, SEV1 등 강한 강조가 꼭 필요할 때만 bold. */
  emphasis?: "subtle" | "bold";
}

export function StatusBadge({ tone, label, className, emphasis }: StatusBadgeProps) {
  return (
    <Badge variant={tone} emphasis={emphasis} className={cn(className)}>
      {label}
    </Badge>
  );
}
