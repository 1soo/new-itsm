import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

/**
 * 타임라인 — common.md SCR-COM-008: 상태 변경·배정·업데이트 이력(프레젠테이션).
 * 데이터 로딩·정렬은 FE가 담당하고, 정렬된 항목을 items로 전달한다.
 */
export interface TimelineItem {
  id: string;
  /** 이벤트 제목(예: "상태 변경: 진행중 → 해결") */
  title: ReactNode;
  /** 부가 설명 */
  description?: ReactNode;
  /** 행위자 */
  actor?: string;
  /** 표시용 시각 문자열 */
  timestamp?: string;
}

export interface TimelineProps {
  items: TimelineItem[];
  className?: string;
}

export function Timeline({ items, className }: TimelineProps) {
  return (
    <ol className={cn("relative flex flex-col gap-4 pl-5", className)}>
      {items.map((item) => (
        <li key={item.id} className="relative">
          <span
            className="absolute -left-[1.375rem] top-1.5 size-2.5 rounded-full bg-info ring-2 ring-background"
            aria-hidden="true"
          />
          <span
            className="absolute -left-[1.075rem] top-4 h-full w-px bg-border last:hidden"
            aria-hidden="true"
          />
          <div className="space-y-0.5">
            <p className="text-sm text-foreground">{item.title}</p>
            {item.description ? (
              <p className="text-sm text-muted-foreground">{item.description}</p>
            ) : null}
            <p className="text-xs text-muted-foreground">
              {[item.actor, item.timestamp].filter(Boolean).join(" · ")}
            </p>
          </div>
        </li>
      ))}
    </ol>
  );
}
