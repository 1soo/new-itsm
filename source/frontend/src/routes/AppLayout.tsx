import { useEffect, useMemo, useRef, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

import { AppShell } from "@/components/layout/app-shell";
import type { HeaderNotificationItem, HeaderSearchResult } from "@/components/layout/header";
import type { NavGroup } from "@/components/layout/sidebar";
import { ConfirmDialog, toast } from "@/components/common";
import { hasAnyRole, ROLE_APPROVER, ROLE_ASSET_MANAGER } from "@/features/auth/roles";
import { authApi } from "@/features/auth/api";
import type { MenuGroup as MyMenuGroup } from "@/features/auth/types";
import { srmApi } from "@/features/service-request/api";
import { changeApi } from "@/features/change/api";
import { assetApi } from "@/features/asset/api";
import { formatDate } from "@/features/asset/format";
import { searchApi } from "@/features/search/api";
import { domainLabel } from "@/features/search/status";
import { commonApi } from "@/features/common/api";
import type { DismissalItem, NotificationType } from "@/features/common/types";
import { resolveIcon } from "@/lib/icon";
import { extractErrorMessage } from "@/lib/apiClient";
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

const RELATIVE_TIME_MINUTE_MS = 60_000;
const RELATIVE_TIME_HOUR_MS = 60 * RELATIVE_TIME_MINUTE_MS;
const RELATIVE_TIME_DAY_MS = 24 * RELATIVE_TIME_HOUR_MS;
const RELATIVE_TIME_WEEK_MS = 7 * RELATIVE_TIME_DAY_MS;

/** 승인 대기 항목의 상대 시간 표시(common.md v2). 7일 이상은 절대 날짜로 대체. */
function formatRelativeTime(iso: string, now: number): string {
  const diff = now - new Date(iso).getTime();
  if (diff < RELATIVE_TIME_MINUTE_MS) return "방금 전";
  if (diff < RELATIVE_TIME_HOUR_MS) return `${Math.floor(diff / RELATIVE_TIME_MINUTE_MS)}분 전`;
  if (diff < RELATIVE_TIME_DAY_MS) return `${Math.floor(diff / RELATIVE_TIME_HOUR_MS)}시간 전`;
  if (diff < RELATIVE_TIME_WEEK_MS) return `${Math.floor(diff / RELATIVE_TIME_DAY_MS)}일 전`;
  return formatDate(iso);
}

/** 알림 항목의 원본 데이터 — timeLabel을 팝오버 오픈 시점 기준으로 재계산하기 위해 raw 시각을 보관한다. */
interface NotificationSource {
  key: string;
  domainLabel: string;
  title: string;
  /** 상대 시간 계산 기준 ISO 시각(자산 만료처럼 상대 시간이 아니면 null). */
  atIso: string | null;
  /** atIso가 null일 때 사용할 고정 시간/만료 텍스트. */
  fixedTimeLabel?: string;
  href: string;
  /** 확인처리(API-COM-001) 대상 식별. */
  notificationType: NotificationType;
  sourceId: number;
}

/** 확인처리 이력 매칭 키(notificationType+sourceId 조합). */
function dismissalKey(notificationType: NotificationType, sourceId: number): string {
  return `${notificationType}:${sourceId}`;
}

/** 헤더 통합 검색 미리보기 결과 건수(공유 API-SEARCH-001, common.md SCR-COM-002 기준 5~8). */
const SEARCH_PREVIEW_SIZE = 8;
/** 입력 중 미리보기 조회 디바운스(ms). */
const SEARCH_DEBOUNCE_MS = 300;

/*
 * 앱 레이아웃 — dev-ui의 AppShell(프레젠테이션)에 RBAC 메뉴·헤더 동작을 주입한다.
 * (SCR-COM-001~004) 사이드바 메뉴는 마운트 시 GET /api/v1/menus/mine(API-AUTH-022) 1회
 * 호출로 구성한다(Role-Menu 동적 매핑 — 역할 필터링은 서버가 수행). active 계산·navigate
 * 주입·로그아웃 확인은 FE가 담당한다.
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

  // 메뉴 목록은 로그인 세션 동안 고정이므로 마운트 시 1회만 조회한다.
  // 실패해도 치명적이지 않으므로 토스트 없이 조용히 빈 사이드바로 둔다.
  const [menuGroups, setMenuGroups] = useState<MyMenuGroup[]>([]);
  useEffect(() => {
    let cancelled = false;
    authApi
      .getMyMenu()
      .then((res) => {
        if (!cancelled) setMenuGroups(res.groups);
      })
      .catch(() => {
        if (!cancelled) setMenuGroups([]);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  // 하위 경로가 상위 메뉴 경로의 접두사와도 일치해 중복 active되는 것을 막기 위해,
  // 매칭되는 항목 중 path가 가장 긴(가장 구체적인, 정확 일치 시 자동으로 최장인) 항목만 active로 선정한다.
  const activeKey = useMemo(() => {
    let best: { key: string; path: string } | null = null;
    for (const group of menuGroups) {
      for (const item of group.items) {
        if (!matchesPath(item.path)) continue;
        if (!best || item.path.length > best.path.length) {
          best = { key: item.screenCode, path: item.path };
        }
      }
    }
    return best?.key;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname, menuGroups]);

  const roles = user?.roles;
  const groups = useMemo<NavGroup[]>(() => {
    return menuGroups.map((group) => ({
      key: group.groupCode ?? "main",
      label: group.groupLabel ?? undefined,
      items: group.items.map((item) => {
        const Icon = resolveIcon(item.iconName);
        return {
          key: item.screenCode,
          label: item.screenName,
          icon: <Icon />,
          active: item.screenCode === activeKey,
          onSelect: () => navigate(item.path),
        };
      }),
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [menuGroups, activeKey, navigate]);

  // 알림 벨: 역할별 승인 대기(서비스요청·CAB)와 자산 만료 임박 항목을 조합해 팝오버 리스트로 조립한다
  // (신규 API 없이 기존 대기함 API 조합, 서비스요청→변경→자산 순으로 이어붙여 상위 8건만 노출).
  // 뱃지 카운트는 상한과 무관한 전체 대기 건수 합계. 항목 클릭 시 이동할 상세 경로는 key로 조회한다.
  // 원본 시각(atIso)을 보관해두고, 팝오버가 열릴 때마다 그 시점 기준으로 timeLabel을 재계산한다
  // (자산 만료는 상대 시간이 아니라 고정 문자열이라 재계산 대상이 아님).
  const [notificationCount, setNotificationCount] = useState(0);
  const [notificationItems, setNotificationItems] = useState<HeaderNotificationItem[] | undefined>(
    undefined,
  );
  const notificationSources = useRef<NotificationSource[]>([]);
  const notificationHrefByKey = useRef<Map<string, string>>(new Map());
  const notificationDismissTargetByKey = useRef<Map<string, DismissalItem>>(new Map());

  const buildNotificationItems = (now: number): HeaderNotificationItem[] =>
    notificationSources.current.map((s) => ({
      key: s.key,
      domainLabel: s.domainLabel,
      text: truncateNotificationText(s.title),
      timeLabel: s.atIso ? formatRelativeTime(s.atIso, now) : (s.fixedTimeLabel ?? ""),
    }));

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      // 확인처리된 알림은 후보에서 제외한다(API-COM-002). 조회 실패는 치명적이지 않으므로
      // 필터링 없이(빈 이력으로 간주) 진행한다.
      const dismissedKeys = await commonApi
        .listDismissals()
        .then((res) => new Set(res.items.map((d) => dismissalKey(d.notificationType, d.sourceId))))
        .catch(() => new Set<string>());

      let count = 0;
      const sources: NotificationSource[] = [];

      if (hasAnyRole(roles, [ROLE_APPROVER])) {
        const [srmApprovals, chgApprovals] = await Promise.all([
          srmApi.listApprovals(),
          changeApi.listApprovals(),
        ]);
        const srmVisible = srmApprovals.filter(
          (a) => !dismissedKeys.has(dismissalKey("SERVICE_REQUEST_APPROVAL", a.requestId)),
        );
        const chgVisible = chgApprovals.filter(
          (a) => !dismissedKeys.has(dismissalKey("CHANGE_APPROVAL", a.changeId)),
        );
        count += srmVisible.length + chgVisible.length;

        for (const a of srmVisible) {
          sources.push({
            key: `sr-${a.requestId}`,
            domainLabel: "서비스요청 승인",
            title: a.catalogItemName,
            atIso: a.requestedAt,
            href: `/service-requests/${a.requestId}`,
            notificationType: "SERVICE_REQUEST_APPROVAL",
            sourceId: a.requestId,
          });
        }
        for (const a of chgVisible) {
          sources.push({
            key: `chg-${a.changeId}`,
            domainLabel: "변경 승인",
            title: a.summary,
            atIso: a.createdAt,
            href: `/changes/${a.changeId}`,
            notificationType: "CHANGE_APPROVAL",
            sourceId: a.changeId,
          });
        }
      }

      if (hasAnyRole(roles, [ROLE_ASSET_MANAGER])) {
        const assets = await assetApi.list({
          expiringWithinDays: ASSET_EXPIRING_WITHIN_DAYS,
          size: NOTIFICATION_PREVIEW_SIZE,
        });
        const visibleAssets = assets.content.filter(
          (asset) => !dismissedKeys.has(dismissalKey("ASSET_EXPIRY", asset.id)),
        );
        // totalElements는 전체 만료 임박 건수 기준이라, 현재 배치(상위 size건)에서 확인처리된
        // 건수만큼만 근사로 차감한다(화면 밖의 확인처리 이력까지는 정확히 반영하지 못함).
        const dismissedInBatch = assets.content.length - visibleAssets.length;
        count += Math.max(0, assets.totalElements - dismissedInBatch);

        for (const asset of visibleAssets) {
          sources.push({
            key: `asset-${asset.id}`,
            domainLabel: "자산 만료",
            title: asset.name,
            atIso: null,
            fixedTimeLabel: `${formatDate(asset.expiryDate)} 만료`,
            href: `/assets/${asset.id}`,
            notificationType: "ASSET_EXPIRY",
            sourceId: asset.id,
          });
        }
      }

      if (!cancelled) {
        notificationSources.current = sources.slice(0, NOTIFICATION_PREVIEW_SIZE);
        notificationHrefByKey.current = new Map(
          notificationSources.current.map((s) => [s.key, s.href]),
        );
        notificationDismissTargetByKey.current = new Map(
          notificationSources.current.map((s) => [
            s.key,
            { notificationType: s.notificationType, sourceId: s.sourceId },
          ]),
        );
        setNotificationCount(count);
        setNotificationItems(buildNotificationItems(Date.now()));
      }
    };

    load().catch(() => {
      if (!cancelled) {
        notificationSources.current = [];
        notificationHrefByKey.current = new Map();
        notificationDismissTargetByKey.current = new Map();
        setNotificationCount(0);
        setNotificationItems([]);
      }
    });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roles]);

  const handleSelectNotification = (item: HeaderNotificationItem) => {
    const href = notificationHrefByKey.current.get(item.key);
    if (href) navigate(href);
  };

  /** 팝오버가 열릴 때마다 그 시점 기준으로 timeLabel을 다시 계산(common.md v2 — 데이터 로딩 시점 고정 방지). */
  const handleNotificationsOpenChange = (open: boolean) => {
    if (!open) return;
    setNotificationItems(buildNotificationItems(Date.now()));
  };

  // 알림 확인처리(API-COM-001) — 원본 승인 대기·자산 만료 데이터는 변경하지 않고 표시 여부만 낙관적으로 갱신한다.
  const handleDismissAllNotifications = async () => {
    const targets = notificationSources.current.map((s) => ({
      notificationType: s.notificationType,
      sourceId: s.sourceId,
    }));
    if (targets.length === 0) return;
    try {
      await commonApi.dismissNotifications(targets);
      notificationSources.current = [];
      notificationHrefByKey.current = new Map();
      notificationDismissTargetByKey.current = new Map();
      setNotificationCount((c) => Math.max(0, c - targets.length));
      setNotificationItems([]);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const handleDismissNotification = async (item: HeaderNotificationItem) => {
    const target = notificationDismissTargetByKey.current.get(item.key);
    if (!target) return;
    try {
      await commonApi.dismissNotifications([target]);
      notificationSources.current = notificationSources.current.filter((s) => s.key !== item.key);
      notificationHrefByKey.current.delete(item.key);
      notificationDismissTargetByKey.current.delete(item.key);
      setNotificationCount((c) => Math.max(0, c - 1));
      setNotificationItems(buildNotificationItems(Date.now()));
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
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
          onNotificationsOpenChange: handleNotificationsOpenChange,
          onDismissAllNotifications: handleDismissAllNotifications,
          onDismissNotification: handleDismissNotification,
          searchResults,
          onSearch: (query) => {
            if (query.trim()) navigate(`/search?keyword=${encodeURIComponent(query.trim())}`);
          },
          onSearchInputChange: handleSearchInputChange,
          onSelectSearchResult: handleSelectSearchResult,
          onOpenGuide: () => navigate("/guide"),
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
