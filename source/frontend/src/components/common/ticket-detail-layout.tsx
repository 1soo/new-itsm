import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

/**
 * 공통 티켓 상세 패턴 셸 — common.md SCR-COM-008.
 * 상단 제목·식별키·배지·액션 / 좌측 본문·코멘트·타임라인(children) / 우측 메타 패널(meta).
 * 허용되지 않은 상태 전이·권한 부족 액션의 노출/비활성은 FE가 actions/meta 주입 시 제어한다.
 */
export interface TicketDetailLayoutProps {
  /** 식별키(예: INC-1024) */
  ticketKey?: string;
  title: string;
  /** 상태·우선순위 배지 등 */
  badges?: ReactNode;
  /** 상태 전이·편집 등 액션 버튼 영역 */
  actions?: ReactNode;
  /** 좌측 본문·코멘트·타임라인 */
  children: ReactNode;
  /** 우측 메타 패널(담당자·SLA·연결 항목·자산/CI) */
  meta?: ReactNode;
  className?: string;
}

export function TicketDetailLayout({
  ticketKey,
  title,
  badges,
  actions,
  children,
  meta,
  className,
}: TicketDetailLayoutProps) {
  return (
    <section className={cn("flex flex-col gap-4", className)}>
      <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border pb-4">
        <div className="space-y-1.5">
          {ticketKey ? (
            <p className="text-xs font-medium text-muted-foreground">{ticketKey}</p>
          ) : null}
          <h1 className="text-heading-large font-bold text-foreground">{title}</h1>
          {badges ? <div className="flex flex-wrap items-center gap-2">{badges}</div> : null}
        </div>
        {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
      </div>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_20rem]">
        <div className="flex min-w-0 flex-col gap-6">{children}</div>
        {meta ? (
          <aside className="flex flex-col gap-4">{meta}</aside>
        ) : null}
      </div>
    </section>
  );
}
