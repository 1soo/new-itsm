import { useEffect, useMemo, useRef, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

import { AppShell } from "@/components/layout/app-shell";
import type { HeaderNotificationItem, HeaderSearchResult } from "@/components/layout/header";
import type { NavGroup } from "@/components/layout/sidebar";
import { ConfirmDialog } from "@/components/common";
import { hasAnyRole, ROLE_APPROVER, ROLE_ASSET_MANAGER } from "@/features/auth/roles";
import { srmApi } from "@/features/service-request/api";
import { changeApi } from "@/features/change/api";
import { riskLabel, typeLabel } from "@/features/change/status";
import { assetApi } from "@/features/asset/api";
import { formatDate } from "@/features/asset/format";
import { searchApi } from "@/features/search/api";
import { domainLabel } from "@/features/search/status";
import { navConfig } from "@/routes/navConfig";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { logout } from "@/store/authSlice";

/** 자산 만료 임박 판정 임계값(일). API-ITAM-001 서버 계산 기준(EXPIRING)과 동일하게 맞춘다. */
const ASSET_EXPIRING_WITHIN_DAYS = 30;

/** 알림 팝오버 미리보기 상한(common.md SCR-COM-002 기준 8건). 벨 뱃지 카운트는 이 상한과 무관하게 전체 대기 건수 합계. */
const NOTIFICATION_PREVIEW_SIZE = 8;
/** 알림 항목 본문 표시 최대 길이(초과 시 말줄임표). */
const NOTIFICATION_TEXT_MAX_LENGTH = 40;

function truncateNotificationText(text: string): string {
  return text.length > NOTIFICATION_TEXT_MAX_LENGTH
    ? `${text.slice(0, NOTIFICATION_TEXT_MAX_LENGTH)}…`
    : text;
}

/** 헤더 통합 검색 미리보기 결과 건수(공유 API-SEARCH-001, common.md SCR-COM-002 기준 5~8). */
const SEARCH_PREVIEW_SIZE = 8;
/** 입력 중 미리보기 조회 디바운스(ms). */
const SEARCH_DEBOUNCE_MS = 300;

/*
 * 앱 레이아웃 — dev-ui의 AppShell(프레젠테이션)에 RBAC 필터된 메뉴·헤더 동작을 주입한다.
 * (SCR-COM-001~004) 메뉴 정의는 navConfig(중앙 관리)에서 가져와 역할로 필터링하고,
 * active 계산·navigate 주입·로그아웃 확인은 FE가 담당한다.
 */
export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useAppDispatch();
  const user = useAppSelector((s) => s.auth.user);

  const [logoutOpen, setLogoutOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  const pathname = location.pathname;
  const matchesPath = (path: string) =>
    path === "/" ? pathname === "/" : pathname === path || pathname.startsWith(`${path}/`);

  // 하위 경로가 상위 메뉴 경로의 접두사와도 일치해 중복 active되는 것을 막기 위해,
  // 매칭되는 항목 중 path가 가장 긴(가장 구체적인, 정확 일치 시 자동으로 최장인) 항목만 active로 선정한다.
  const activeKey = useMemo(() => {
    let best: { key: string; path: string } | null = null;
    for (const group of navConfig) {
      for (const item of group.items) {
        if (!matchesPath(item.path)) continue;
        if (!best || item.path.length > best.path.length) {
          best = item;
        }
      }
    }
    return best?.key;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);

  const roles = user?.roles;
  const groups = useMemo<NavGroup[]>(() => {
    return navConfig
      .map((group) => {
        const items = group.items
          .filter((item) => !item.roles || hasAnyRole(roles, item.roles))
          .map((item) => ({
            key: item.key,
            label: item.label,
            icon: item.icon,
            active: item.key === activeKey,
            onSelect: () => navigate(item.path),
          }));
        return { key: group.key, label: group.label, items };
      })
      .filter((group) => group.items.length > 0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roles, activeKey, navigate]);

  // 알림 벨: 역할별 승인 대기(서비스요청·CAB)와 자산 만료 임박 항목을 조합해 팝오버 리스트로 조립한다
  // (신규 API 없이 기존 대기함 API 조합, 서비스요청→변경→자산 순으로 이어붙여 상위 8건만 노출).
  // 뱃지 카운트는 상한과 무관한 전체 대기 건수 합계. 항목 클릭 시 이동할 상세 경로는 key로 조회한다.
  const [notificationCount, setNotificationCount] = useState(0);
  const [notificationItems, setNotificationItems] = useState<HeaderNotificationItem[] | undefined>(
    undefined,
  );
  const notificationHrefByKey = useRef<Map<string, string>>(new Map());

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      let count = 0;
      const items: (HeaderNotificationItem & { href: string })[] = [];

      if (hasAnyRole(roles, [ROLE_APPROVER])) {
        const [srmApprovals, chgApprovals] = await Promise.all([
          srmApi.listApprovals(),
          changeApi.listApprovals(),
        ]);
        count += srmApprovals.length + chgApprovals.length;

        for (const a of srmApprovals) {
          items.push({
            key: `sr-${a.requestId}`,
            domainLabel: "서비스요청 승인",
            text: truncateNotificationText(`${a.ticketKey} · ${a.requester} 승인 요청`),
            href: `/service-requests/${a.requestId}`,
          });
        }
        for (const a of chgApprovals) {
          items.push({
            key: `chg-${a.changeId}`,
            domainLabel: "변경 승인",
            text: truncateNotificationText(
              `${a.ticketKey} · ${typeLabel(a.type)}/${a.risk ? riskLabel(a.risk) : "-"} · ${a.requester} 승인 요청`,
            ),
            href: `/changes/${a.changeId}`,
          });
        }
      }

      if (hasAnyRole(roles, [ROLE_ASSET_MANAGER])) {
        const assets = await assetApi.list({
          expiringWithinDays: ASSET_EXPIRING_WITHIN_DAYS,
          size: NOTIFICATION_PREVIEW_SIZE,
        });
        count += assets.totalElements;

        for (const asset of assets.content) {
          items.push({
            key: `asset-${asset.id}`,
            domainLabel: "자산 만료",
            text: truncateNotificationText(
              `${asset.assetKey} · ${asset.name} · ${formatDate(asset.expiryDate)} 만료 예정`,
            ),
            href: `/assets/${asset.id}`,
          });
        }
      }

      if (!cancelled) {
        const preview = items.slice(0, NOTIFICATION_PREVIEW_SIZE);
        notificationHrefByKey.current = new Map(preview.map((item) => [item.key, item.href]));
        setNotificationCount(count);
        setNotificationItems(preview.map(({ key, domainLabel: label, text }) => ({ key, domainLabel: label, text })));
      }
    };

    load().catch(() => {
      if (!cancelled) {
        notificationHrefByKey.current = new Map();
        setNotificationCount(0);
        setNotificationItems([]);
      }
    });

    return () => {
      cancelled = true;
    };
  }, [roles]);

  const handleSelectNotification = (item: HeaderNotificationItem) => {
    const href = notificationHrefByKey.current.get(item.key);
    if (href) navigate(href);
  };

  // 헤더 통합 검색 미리보기: 입력 중 디바운스 후 API-SEARCH-001을 size=8로 호출해 드롭다운에 표시.
  // 항목 클릭 시 원본 검색 결과의 url로 이동(Header는 도메인 데이터를 몰라도 되도록 key로만 매핑).
  const [searchResults, setSearchResults] = useState<HeaderSearchResult[] | undefined>(undefined);
  const searchUrlByKey = useRef<Map<string, string>>(new Map());
  const searchDebounceRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => {
    return () => clearTimeout(searchDebounceRef.current);
  }, []);

  const handleSearchInputChange = (query: string) => {
    clearTimeout(searchDebounceRef.current);
    const keyword = query.trim();
    if (!keyword) {
      setSearchResults(undefined);
      return;
    }
    searchDebounceRef.current = setTimeout(() => {
      searchApi
        .search({ keyword, size: SEARCH_PREVIEW_SIZE })
        .then((res) => {
          searchUrlByKey.current = new Map(res.content.map((r) => [`${r.domain}-${r.key}`, r.url]));
          setSearchResults(
            res.content.map((r) => ({
              key: `${r.domain}-${r.key}`,
              title: r.title,
              subtitle: `${domainLabel(r.domain)} · ${r.key}`,
            })),
          );
        })
        .catch(() => setSearchResults([]));
    }, SEARCH_DEBOUNCE_MS);
  };

  const handleSelectSearchResult = (result: HeaderSearchResult) => {
    const url = searchUrlByKey.current.get(result.key);
    if (url) navigate(url);
  };

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await dispatch(logout()).unwrap();
    } catch {
      // 서버 오류가 나도 클라이언트 세션은 종료됐으므로 로그인으로 이동.
    } finally {
      setLoggingOut(false);
      setLogoutOpen(false);
      navigate("/login", { replace: true });
    }
  };

  return (
    <>
      <AppShell
        header={{
          title: "ITSM",
          user: user ? { name: user.name, email: user.email } : undefined,
          notificationCount,
          notifications: notificationItems,
          onSelectNotification: handleSelectNotification,
          searchResults,
          onSearch: (query) => {
            if (query.trim()) navigate(`/search?keyword=${encodeURIComponent(query.trim())}`);
          },
          onSearchInputChange: handleSearchInputChange,
          onSelectSearchResult: handleSelectSearchResult,
          onProfile: () => navigate("/profile"),
          onChangePassword: () => navigate("/profile/password"),
          onLogout: () => setLogoutOpen(true),
        }}
        sidebar={{ groups }}
      >
        <Outlet />
      </AppShell>

      <ConfirmDialog
        open={logoutOpen}
        onOpenChange={setLogoutOpen}
        title="로그아웃"
        description="현재 세션에서 로그아웃하시겠습니까?"
        confirmLabel="로그아웃"
        loading={loggingOut}
        onConfirm={handleLogout}
      />
    </>
  );
}
