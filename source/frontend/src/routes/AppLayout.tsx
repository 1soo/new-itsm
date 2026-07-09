import { useMemo, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  ScrollText,
  ShieldCheck,
  User,
  Users,
} from "lucide-react";

import { AppShell } from "@/components/layout/app-shell";
import type { NavGroup } from "@/components/layout/sidebar";
import { ConfirmDialog } from "@/components/common";
import { isSystemAdmin } from "@/features/auth/roles";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { logout } from "@/store/authSlice";

/*
 * 앱 레이아웃 — dev-ui의 AppShell(프레젠테이션)에 RBAC 메뉴 데이터·헤더 동작을 주입한다.
 * (SCR-COM-001~004) 메뉴는 역할에 따라 필터링하며(권한 없는 항목 제외), 라우팅/인증은 FE가 담당한다.
 * auth 단계 메뉴: 대시보드·내 프로필 + 관리자 그룹(SYSTEM_ADMIN 전용). 타 도메인 메뉴는 이후 단계에서 추가.
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
    path === "/" ? pathname === "/" : pathname.startsWith(path);

  const groups = useMemo<NavGroup[]>(() => {
    const result: NavGroup[] = [
      {
        key: "main",
        items: [
          {
            key: "dashboard",
            label: "대시보드",
            icon: <LayoutDashboard />,
            active: isActive("/"),
            onSelect: () => navigate("/"),
          },
          {
            key: "profile",
            label: "내 프로필",
            icon: <User />,
            active: isActive("/profile"),
            onSelect: () => navigate("/profile"),
          },
        ],
      },
    ];

    if (isSystemAdmin(user?.roles)) {
      result.push({
        key: "admin",
        label: "관리자",
        items: [
          {
            key: "admin-users",
            label: "계정 관리",
            icon: <Users />,
            active: isActive("/admin/users"),
            onSelect: () => navigate("/admin/users"),
          },
          {
            key: "admin-roles",
            label: "역할 관리",
            icon: <ShieldCheck />,
            active: isActive("/admin/roles"),
            onSelect: () => navigate("/admin/roles"),
          },
          {
            key: "admin-audit",
            label: "감사 로그",
            icon: <ScrollText />,
            active: isActive("/admin/audit-logs"),
            onSelect: () => navigate("/admin/audit-logs"),
          },
        ],
      });
    }

    return result;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.roles, pathname, navigate]);

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
