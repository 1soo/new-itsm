import { createBrowserRouter } from "react-router-dom";

import { SessionBridge } from "@/routes/SessionBridge";
import { AuthGuard } from "@/routes/AuthGuard";
import { RequireAdmin } from "@/routes/RequireAdmin";
import { DashboardPage } from "@/routes/DashboardPage";
import { ForbiddenPage } from "@/routes/ForbiddenPage";
import { NotFoundPage } from "@/routes/NotFoundPage";
import { LoginPage } from "@/features/auth/LoginPage";
import { ProfilePage } from "@/features/auth/ProfilePage";
import { ChangePasswordPage } from "@/features/auth/ChangePasswordPage";
import { UserListPage } from "@/features/admin/UserListPage";
import { UserCreatePage } from "@/features/admin/UserCreatePage";
import { UserDetailPage } from "@/features/admin/UserDetailPage";
import { RoleManagementPage } from "@/features/admin/RoleManagementPage";
import { AuditLogPage } from "@/features/admin/AuditLogPage";

/*
 * 라우팅 — 화면 ID(SCR-*)와 경로 매핑. screen 테이블 seed 경로와 정합.
 * SessionBridge(세션 만료 연결) → AuthGuard(인증 가드/셸) → RequireAdmin(관리자 RBAC).
 */
export const router = createBrowserRouter([
  {
    element: <SessionBridge />,
    children: [
      // 로그인(SCR-AUTH-001) — 공개 라우트
      { path: "/login", element: <LoginPage /> },

      // 보호 라우트 — 인증 가드 + 앱 셸
      {
        element: <AuthGuard />,
        children: [
          { index: true, path: "/", element: <DashboardPage /> },
          { path: "/profile", element: <ProfilePage /> }, // SCR-AUTH-002
          { path: "/profile/password", element: <ChangePasswordPage /> }, // SCR-AUTH-003
          { path: "/403", element: <ForbiddenPage /> }, // SCR-COM-006

          // 관리자 라우트 — SYSTEM_ADMIN 전용
          {
            element: <RequireAdmin />,
            children: [
              { path: "/admin/users", element: <UserListPage /> }, // SCR-ADMIN-001
              { path: "/admin/users/new", element: <UserCreatePage /> }, // SCR-ADMIN-002
              { path: "/admin/users/:id", element: <UserDetailPage /> }, // SCR-ADMIN-003
              { path: "/admin/roles", element: <RoleManagementPage /> }, // SCR-ADMIN-004
              { path: "/admin/audit-logs", element: <AuditLogPage /> }, // SCR-ADMIN-005
            ],
          },

          // 그 외 미매칭 경로 → 404(SCR-ERR-404)
          { path: "*", element: <NotFoundPage /> },
        ],
      },
    ],
  },
]);
