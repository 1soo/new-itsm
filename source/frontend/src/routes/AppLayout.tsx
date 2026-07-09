import { useMemo, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

import { AppShell } from "@/components/layout/app-shell";
import type { NavGroup } from "@/components/layout/sidebar";
import { ConfirmDialog } from "@/components/common";
import { hasAnyRole } from "@/features/auth/roles";
import { navConfig } from "@/routes/navConfig";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { logout } from "@/store/authSlice";

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
  const isActive = (path: string) =>
    path === "/" ? pathname === "/" : pathname === path || pathname.startsWith(`${path}/`);

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
            active: isActive(item.path),
            onSelect: () => navigate(item.path),
          }));
        return { key: group.key, label: group.label, items };
      })
      .filter((group) => group.items.length > 0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roles, pathname, navigate]);

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
