import { createBrowserRouter } from "react-router-dom";

import { SessionBridge } from "@/routes/SessionBridge";
import { AuthGuard } from "@/routes/AuthGuard";
import { RequireAdmin } from "@/routes/RequireAdmin";
import { RequireRoles } from "@/routes/RequireRoles";
import { DashboardPage } from "@/routes/DashboardPage";
import { ForbiddenPage } from "@/routes/ForbiddenPage";
import { NotFoundPage } from "@/routes/NotFoundPage";
import {
  ROLE_APPROVER,
  ROLE_END_USER,
  ROLE_INCIDENT_MANAGER,
  ROLE_PROCESS_OWNER,
  ROLE_SERVICE_DESK_AGENT,
} from "@/features/auth/roles";
import { LoginPage } from "@/features/auth/LoginPage";
import { ProfilePage } from "@/features/auth/ProfilePage";
import { ChangePasswordPage } from "@/features/auth/ChangePasswordPage";
import { UserListPage } from "@/features/admin/UserListPage";
import { UserCreatePage } from "@/features/admin/UserCreatePage";
import { UserDetailPage } from "@/features/admin/UserDetailPage";
import { RoleManagementPage } from "@/features/admin/RoleManagementPage";
import { AuditLogPage } from "@/features/admin/AuditLogPage";
import { PortalPage } from "@/features/service-request/PortalPage";
import { RequestSubmitPage } from "@/features/service-request/RequestSubmitPage";
import { RequestListPage } from "@/features/service-request/RequestListPage";
import { RequestQueuePage } from "@/features/service-request/RequestQueuePage";
import { RequestDetailPage } from "@/features/service-request/RequestDetailPage";
import { ApprovalInboxPage } from "@/features/service-request/ApprovalInboxPage";
import { CatalogManagePage } from "@/features/service-request/CatalogManagePage";
import { MetricsPage } from "@/features/service-request/MetricsPage";
import { IncidentListPage } from "@/features/incident/IncidentListPage";
import { IncidentCreatePage } from "@/features/incident/IncidentCreatePage";
import { IncidentDetailPage } from "@/features/incident/IncidentDetailPage";
import { PostmortemPage } from "@/features/incident/PostmortemPage";
import { IncidentMetricsPage } from "@/features/incident/IncidentMetricsPage";

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

          // 서비스 요청(SRM) — 역할별 RBAC
          {
            element: <RequireRoles roles={[ROLE_END_USER]} />,
            children: [
              { path: "/portal", element: <PortalPage /> }, // SCR-SRM-001
              { path: "/portal/requests/new", element: <RequestSubmitPage /> }, // SCR-SRM-002
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_END_USER, ROLE_SERVICE_DESK_AGENT]} />,
            children: [
              { path: "/service-requests", element: <RequestListPage /> }, // SCR-SRM-003
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_SERVICE_DESK_AGENT]} />,
            children: [
              { path: "/service-requests/queue", element: <RequestQueuePage /> }, // SCR-SRM-004
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_PROCESS_OWNER]} />,
            children: [
              { path: "/admin/service-catalog", element: <CatalogManagePage /> }, // SCR-SRM-007
              { path: "/service-requests/metrics", element: <MetricsPage /> }, // SCR-SRM-008
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_APPROVER]} />,
            children: [
              { path: "/approvals/service-requests", element: <ApprovalInboxPage /> }, // SCR-SRM-006
            ],
          },
          {
            element: (
              <RequireRoles roles={[ROLE_END_USER, ROLE_SERVICE_DESK_AGENT, ROLE_APPROVER]} />
            ),
            children: [
              { path: "/service-requests/:id", element: <RequestDetailPage /> }, // SCR-SRM-005
            ],
          },

          // 인시던트(INC) — SERVICE_DESK_AGENT/INCIDENT_MANAGER
          {
            element: <RequireRoles roles={[ROLE_SERVICE_DESK_AGENT, ROLE_INCIDENT_MANAGER]} />,
            children: [
              { path: "/incidents", element: <IncidentListPage /> }, // SCR-INC-001
              { path: "/incidents/new", element: <IncidentCreatePage /> }, // SCR-INC-002
              { path: "/incidents/:id", element: <IncidentDetailPage /> }, // SCR-INC-003
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_INCIDENT_MANAGER]} />,
            children: [
              { path: "/incidents/:id/postmortem", element: <PostmortemPage /> }, // SCR-INC-004
              { path: "/incidents/metrics", element: <IncidentMetricsPage /> }, // SCR-INC-005
            ],
          },

          // 그 외 미매칭 경로 → 404(SCR-ERR-404)
          { path: "*", element: <NotFoundPage /> },
        ],
      },
    ],
  },
]);
