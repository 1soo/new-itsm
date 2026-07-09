import { ShieldAlert } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

/**
 * 403 접근 거부 — common.md SCR-COM-006 (프레젠테이션 전용).
 * 중앙 정렬 안내 카드. "이전으로" 동작은 FE가 onBack으로 주입한다.
 */
export interface ForbiddenViewProps {
  onBack?: () => void;
  className?: string;
}

export function ForbiddenView({ onBack, className }: ForbiddenViewProps) {
  return (
    <div
      className={cn(
        "flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center",
        className,
      )}
    >
      <ShieldAlert className="size-14 text-danger" aria-hidden="true" />
      <div className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">접근 권한이 없습니다</h1>
        <p className="text-sm text-muted-foreground">
          이 페이지에 접근할 권한이 없습니다.
        </p>
      </div>
      {onBack ? (
        <Button onClick={onBack}>이전으로</Button>
      ) : null}
    </div>
  );
}
