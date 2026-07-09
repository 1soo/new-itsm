import { type FormEvent, useState } from "react";
import { Bell, Menu, Search } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

/**
 * 글로벌 헤더 — common.md SCR-COM-002 (프레젠테이션 전용).
 * 좌: 로고·현재 도메인 타이틀 / 중앙: 통합 검색 / 우: 알림 벨·사용자 메뉴.
 * 검색 실행·로그아웃·프로필 이동 등 동작은 FE가 콜백으로 주입한다.
 */
export interface HeaderUser {
  name: string;
  email: string;
}

export interface HeaderProps {
  title?: string;
  user?: HeaderUser;
  notificationCount?: number;
  onToggleSidebar?: () => void;
  onSearch?: (query: string) => void;
  onNotifications?: () => void;
  onProfile?: () => void;
  onChangePassword?: () => void;
  onLogout?: () => void;
  className?: string;
}

function initials(name: string) {
  return name.trim().slice(0, 2).toUpperCase();
}

export function Header({
  title = "ITSM",
  user,
  notificationCount = 0,
  onToggleSidebar,
  onSearch,
  onNotifications,
  onProfile,
  onChangePassword,
  onLogout,
  className,
}: HeaderProps) {
  const [query, setQuery] = useState("");

  const submitSearch = (e: FormEvent) => {
    e.preventDefault();
    onSearch?.(query.trim());
  };

  return (
    <header
      className={cn(
        "flex h-14 shrink-0 items-center gap-4 border-b border-border bg-background px-4",
        className,
      )}
    >
      {onToggleSidebar ? (
        <Button
          variant="ghost"
          size="icon"
          onClick={onToggleSidebar}
          aria-label="사이드바 토글"
        >
          <Menu />
        </Button>
      ) : null}

      <div className="flex items-center gap-2">
        <span className="flex size-7 items-center justify-center rounded-md bg-primary text-sm font-bold text-primary-foreground">
          IT
        </span>
        <span className="text-base font-semibold text-foreground">{title}</span>
      </div>

      <form onSubmit={submitSearch} className="relative mx-auto hidden w-full max-w-md md:block">
        <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="지식·티켓 검색"
          className="pl-9"
          aria-label="통합 검색"
        />
      </form>

      <div className="ml-auto flex items-center gap-1">
        <Button
          variant="ghost"
          size="icon"
          onClick={onNotifications}
          aria-label={`알림${notificationCount > 0 ? ` ${notificationCount}건` : ""}`}
          className="relative"
        >
          <Bell />
          {notificationCount > 0 ? (
            <span className="absolute right-1 top-1 flex min-w-4 items-center justify-center rounded-full bg-warning px-1 text-[10px] font-bold leading-4 text-warning-foreground">
              {notificationCount > 99 ? "99+" : notificationCount}
            </span>
          ) : null}
        </Button>

        {user ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="rounded-full"
                aria-label="사용자 메뉴"
              >
                <Avatar className="size-8">
                  <AvatarFallback>{initials(user.name)}</AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <div className="flex flex-col">
                  <span className="truncate text-sm font-medium">{user.name}</span>
                  <span className="truncate text-xs font-normal text-muted-foreground">
                    {user.email}
                  </span>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onSelect={onProfile}>내 프로필</DropdownMenuItem>
              <DropdownMenuItem onSelect={onChangePassword}>
                비밀번호 변경
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onSelect={onLogout} className="text-destructive">
                로그아웃
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null}
      </div>
    </header>
  );
}
