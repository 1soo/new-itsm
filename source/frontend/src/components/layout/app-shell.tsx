import { type ReactNode, useState } from "react";

import { Header, type HeaderProps } from "@/components/layout/header";
import { Sidebar, type SidebarProps } from "@/components/layout/sidebar";
import { Footer, type FooterProps } from "@/components/layout/footer";
import { cn } from "@/lib/utils";

/**
 * 앱 셸 — common.md SCR-COM-001.
 * 상단 고정 헤더 / 좌측 사이드바(접기 가능) / 우측 메인 콘텐츠 / 하단 푸터.
 * 인증 가드·RBAC 메뉴 필터·라우팅은 FE가 담당하며, 셸은 프레젠테이션만 제공한다.
 */
export interface AppShellProps {
  header: Omit<HeaderProps, "onToggleSidebar">;
  sidebar: Omit<SidebarProps, "collapsed">;
  footer?: FooterProps;
  children: ReactNode;
  className?: string;
}

export function AppShell({
  header,
  sidebar,
  footer,
  children,
  className,
}: AppShellProps) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className={cn("flex h-screen flex-col overflow-hidden", className)}>
      <Header {...header} onToggleSidebar={() => setCollapsed((c) => !c)} />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar {...sidebar} collapsed={collapsed} />
        <div className="flex flex-1 flex-col overflow-hidden">
          <main className="flex-1 overflow-y-auto bg-background p-6">{children}</main>
          <Footer {...footer} />
        </div>
      </div>
    </div>
  );
}
