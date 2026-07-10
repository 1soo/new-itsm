import { type ReactNode, Fragment } from "react";

import { cn } from "@/lib/utils";

/**
 * 사이드바 내비게이션 — common.md SCR-COM-003 (프레젠테이션 전용).
 * RBAC 필터링·라우팅은 FE가 담당한다: 권한 없는 항목은 items에서 제외하여 전달한다.
 * 활성 강조는 item.active로 제어한다.
 */
export interface NavItem {
  key: string;
  label: string;
  icon?: ReactNode;
  active?: boolean;
  onSelect: () => void;
}

export interface NavGroup {
  key: string;
  /** 그룹 라벨(없으면 구분선만) */
  label?: string;
  items: NavItem[];
}

export interface SidebarProps {
  groups: NavGroup[];
  collapsed?: boolean;
  className?: string;
}

export function Sidebar({ groups, collapsed = false, className }: SidebarProps) {
  return (
    <aside
      className={cn(
        "flex h-full shrink-0 flex-col overflow-y-auto bg-sidebar text-sidebar-foreground transition-[width] duration-200 ease-[var(--motion-sidebar-easing)]",
        collapsed ? "w-16" : "w-60",
        className,
      )}
      aria-label="주요 내비게이션"
    >
      <nav className="flex flex-col gap-1 p-2">
        {groups.map((group, gi) => (
          <Fragment key={group.key}>
            {gi > 0 && (
              <div className="my-2 border-t border-sidebar-border" role="separator" />
            )}
            {group.label && !collapsed ? (
              <p className="px-3 py-1 text-xs font-semibold uppercase tracking-wide text-sidebar-foreground/60">
                {group.label}
              </p>
            ) : null}
            {group.items.map((item) => (
              <button
                key={item.key}
                type="button"
                onClick={item.onSelect}
                aria-current={item.active ? "page" : undefined}
                title={collapsed ? item.label : undefined}
                className={cn(
                  "flex items-center gap-3 overflow-hidden rounded-md px-3 py-2 text-sm font-medium outline-none transition-colors",
                  "hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 focus-visible:ring-ring",
                  item.active
                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                    : "text-sidebar-foreground/90",
                )}
              >
                {item.icon ? (
                  <span className="flex size-5 shrink-0 items-center justify-center [&_svg]:size-5">
                    {item.icon}
                  </span>
                ) : null}
                {/* 라벨은 별도 width 전환 없이 opacity만 애니메이션한다. 사이드바
                    컨테이너의 폭 전환(승인된 예외)에 flex-1 min-w-0으로 자연히
                    종속되어 축소되며, 아이콘은 shrink-0 고정폭이라 collapsed 시
                    자연히 중앙 정렬된다. */}
                <span
                  className={cn(
                    "min-w-0 flex-1 truncate transition-opacity",
                    collapsed ? "opacity-0" : "opacity-100",
                  )}
                >
                  {item.label}
                </span>
              </button>
            ))}
          </Fragment>
        ))}
      </nav>
    </aside>
  );
}
