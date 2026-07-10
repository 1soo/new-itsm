import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

/**
 * 공통 티켓 목록/필터 패턴 셸 — common.md SCR-COM-007.
 * 상단 타이틀·액션(신규 생성 등) / 필터바 슬롯 / 목록·페이지네이션(children).
 * "신규 생성" 등 권한 의존 액션의 노출 여부는 FE가 RBAC로 판단하여 actions에 주입한다.
 */
export interface TicketListLayoutProps {
  title: string;
  description?: string;
  /** 우측 상단 액션 영역(예: 신규 생성 버튼). 권한 없으면 FE가 미주입/숨김 */
  actions?: ReactNode;
  /** 필터바 슬롯(상태·우선순위·담당자·기간·키워드) */
  filters?: ReactNode;
  /** 목록 표 + 페이지네이션 */
  children: ReactNode;
  className?: string;
}

export function TicketListLayout({
  title,
  description,
  actions,
  filters,
  children,
  className,
}: TicketListLayoutProps) {
  return (
    <section className={cn("flex flex-col gap-4", className)}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-1">
          <h1 className="text-heading-large font-bold text-foreground">{title}</h1>
          {description ? (
            <p className="text-sm text-muted-foreground">{description}</p>
          ) : null}
        </div>
        {actions ? <div className="flex items-center gap-2">{actions}</div> : null}
      </div>

      {filters ? (
        <div className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
          {filters}
        </div>
      ) : null}

      <div className="flex flex-col gap-4">{children}</div>
    </section>
  );
}
