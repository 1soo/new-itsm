import { type FormEvent, useState } from "react";
import { Bell, HelpCircle, Menu, Search, X } from "lucide-react";

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
import { Popover, PopoverAnchor, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { StatusBadge } from "@/components/common";
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

/** 통합 검색 미리보기 드롭다운 항목 — 도메인 무관 프레젠테이션 형태(FE가 도메인 데이터를 매핑해 전달). */
export interface HeaderSearchResult {
  key: string;
  title: string;
  subtitle?: string;
}

/** 알림 팝오버 항목 — 도메인 무관 프레젠테이션 형태(FE가 40자 truncate·시간 표시 계산 완료한 텍스트를 전달). */
export interface HeaderNotificationItem {
  key: string;
  /** "서비스요청 승인" | "변경 승인" | "자산 만료" */
  domainLabel: string;
  /** 1행 우측 시간/만료 표시(예: "N분 전", "2026-08-10", "2026-08-10 만료"). */
  timeLabel: string;
  /** 2행 제목(40자 초과 시 말줄임표). */
  text: string;
}

export interface HeaderProps {
  title?: string;
  user?: HeaderUser;
  notificationCount?: number;
  /** 입력 중 미리보기 대상 결과(undefined면 드롭다운 미표시, 빈 배열이면 "결과 없음" 표시). FE가 디바운스 검색 결과를 주입. */
  searchResults?: HeaderSearchResult[];
  onToggleSidebar?: () => void;
  /** Enter/검색 버튼 클릭 시 전체 검색 실행. */
  onSearch?: (query: string) => void;
  /** 입력 중(디바운스는 FE 담당) 호출되어 미리보기 결과 조회를 트리거. */
  onSearchInputChange?: (query: string) => void;
  /** 미리보기 결과 항목 클릭 시 해당 상세로 이동. */
  onSelectSearchResult?: (result: HeaderSearchResult) => void;
  /** 알림 팝오버 항목(undefined면 로딩 전 상태로 팝오버 미표시, 빈 배열이면 "새로운 알림이 없습니다" 표시). FE가 상위 8건을 조립해 주입. */
  notifications?: HeaderNotificationItem[];
  /** 알림 항목의 "상세 보기" 클릭 시 개별 상세로 이동. */
  onSelectNotification?: (item: HeaderNotificationItem) => void;
  /** 알림 팝오버 열림/닫힘 상태 변경 시 호출(열릴 때 FE가 timeLabel을 현재 시각 기준으로 재계산). */
  onNotificationsOpenChange?: (open: boolean) => void;
  /** "모두 지우기" 클릭 시 호출(표시 중 알림 전체 확인처리). */
  onDismissAllNotifications?: () => void;
  /** 알림 항목의 개별 X 클릭 시 호출(해당 1건만 확인처리). 라인 클릭(상세 이동)과는 분리된 동작. */
  onDismissNotification?: (item: HeaderNotificationItem) => void;
  onProfile?: () => void;
  onChangePassword?: () => void;
  onLogout?: () => void;
  /** "?" 사용자 가이드 아이콘 클릭 시 호출(FE가 `/guide`로 이동). */
  onOpenGuide?: () => void;
  className?: string;
}

function initials(name: string) {
  return name.trim().slice(0, 2).toUpperCase();
}

export function Header({
  title = "ITSM",
  user,
  notificationCount = 0,
  searchResults,
  onToggleSidebar,
  onSearch,
  onSearchInputChange,
  onSelectSearchResult,
  notifications,
  onSelectNotification,
  onNotificationsOpenChange,
  onDismissAllNotifications,
  onDismissNotification,
  onProfile,
  onChangePassword,
  onLogout,
  onOpenGuide,
  className,
}: HeaderProps) {
  const [query, setQuery] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [notificationOpen, setNotificationOpen] = useState(false);

  const submitSearch = (e: FormEvent) => {
    e.preventDefault();
    setSearchOpen(false);
    onSearch?.(query.trim());
  };

  const handleQueryChange = (value: string) => {
    setQuery(value);
    onSearchInputChange?.(value);
    setSearchOpen(value.trim().length > 0);
  };

  const handleSelectResult = (result: HeaderSearchResult) => {
    setSearchOpen(false);
    onSelectSearchResult?.(result);
  };

  const handleSelectNotification = (item: HeaderNotificationItem) => {
    setNotificationOpen(false);
    onSelectNotification?.(item);
  };

  const handleNotificationOpenChange = (open: boolean) => {
    setNotificationOpen(open);
    onNotificationsOpenChange?.(open);
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

      <Popover open={searchOpen && searchResults !== undefined} onOpenChange={setSearchOpen}>
        <PopoverAnchor asChild>
          <form onSubmit={submitSearch} className="relative mx-auto hidden w-full max-w-md md:block">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              value={query}
              onChange={(e) => handleQueryChange(e.target.value)}
              onFocus={() => setSearchOpen(query.trim().length > 0)}
              placeholder="지식·티켓 검색"
              className="pl-9"
              aria-label="통합 검색"
            />
          </form>
        </PopoverAnchor>
        <PopoverContent
          className="w-[var(--radix-popover-trigger-width)] p-1"
          align="start"
          onOpenAutoFocus={(e) => e.preventDefault()}
        >
          <ul className="max-h-72 overflow-auto">
            {searchResults && searchResults.length === 0 ? (
              <li className="px-2 py-1.5 text-sm text-muted-foreground">검색 결과가 없습니다</li>
            ) : (
              searchResults?.map((r) => (
                <li key={r.key}>
                  <button
                    type="button"
                    onClick={() => handleSelectResult(r)}
                    className="flex w-full flex-col items-start gap-0.5 rounded-sm px-2 py-1.5 text-left text-sm outline-none hover:bg-accent focus-visible:bg-accent"
                  >
                    <span className="text-foreground">{r.title}</span>
                    {r.subtitle ? <span className="text-xs text-muted-foreground">{r.subtitle}</span> : null}
                  </button>
                </li>
              ))
            )}
          </ul>
        </PopoverContent>
      </Popover>

      <div className="ml-auto flex items-center gap-1">
        <Button
          variant="ghost"
          size="icon"
          onClick={onOpenGuide}
          aria-label="사용자 가이드"
        >
          <HelpCircle />
        </Button>
        <ThemeToggle />
        <Popover open={notificationOpen} onOpenChange={handleNotificationOpenChange}>
          <PopoverTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
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
          </PopoverTrigger>
          <PopoverContent className="w-80 p-1" align="end">
            {notifications && notifications.length > 0 && onDismissAllNotifications ? (
              <div className="flex items-center justify-end border-b border-border px-2 py-1">
                <Button
                  type="button"
                  variant="link"
                  size="sm"
                  className="h-auto p-0 text-xs"
                  aria-label="모든 알림 확인처리"
                  onClick={onDismissAllNotifications}
                >
                  모두 지우기
                </Button>
              </div>
            ) : null}
            <ul className="max-h-80 overflow-auto">
              {notifications && notifications.length === 0 ? (
                <li className="px-2 py-1.5 text-sm text-muted-foreground">
                  새로운 알림이 없습니다
                </li>
              ) : (
                notifications?.map((n) => (
                  <li
                    key={n.key}
                    onClick={() => handleSelectNotification(n)}
                    className="flex cursor-pointer flex-col gap-1 rounded-sm px-2 py-1.5 hover:bg-accent"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <StatusBadge tone="info" label={n.domainLabel} className="shrink-0" />
                      <div className="flex shrink-0 items-center gap-1">
                        <span className="text-xs text-muted-foreground">{n.timeLabel}</span>
                        {onDismissNotification ? (
                          <button
                            type="button"
                            aria-label="알림 확인처리"
                            onClick={(e) => {
                              e.stopPropagation();
                              onDismissNotification(n);
                            }}
                            className="rounded-sm p-0.5 text-muted-foreground outline-none hover:bg-accent hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring"
                          >
                            <X className="size-3.5" />
                          </button>
                        ) : null}
                      </div>
                    </div>
                    <div className="flex items-center justify-between gap-2">
                      <span className="flex-1 truncate text-sm text-foreground">{n.text}</span>
                      <Button
                        type="button"
                        variant="link"
                        size="sm"
                        className="h-auto shrink-0 p-0 text-xs"
                        onClick={() => handleSelectNotification(n)}
                      >
                        상세 보기
                      </Button>
                    </div>
                  </li>
                ))
              )}
            </ul>
          </PopoverContent>
        </Popover>

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
