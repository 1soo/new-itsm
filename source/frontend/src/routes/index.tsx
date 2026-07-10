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
  ROLE_ASSET_MANAGER,
  ROLE_CHANGE_MANAGER,
  ROLE_COMPLIANCE_OFFICER,
  ROLE_DEPT_COORDINATOR,
  ROLE_END_USER,
  ROLE_HR_CASE_MANAGER,
  ROLE_INCIDENT_MANAGER,
  ROLE_KNOWLEDGE_CONTRIBUTOR,
  ROLE_KNOWLEDGE_GATEKEEPER,
  ROLE_PROBLEM_MANAGER,
  ROLE_PROCESS_OWNER,
  ROLE_SERVICE_DESK_AGENT,
  ROLE_VULNERABILITY_MANAGER,
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
import { ProblemListPage } from "@/features/problem/ProblemListPage";
import { ProblemCreatePage } from "@/features/problem/ProblemCreatePage";
import { ProblemDetailPage } from "@/features/problem/ProblemDetailPage";
import { KnownErrorSearchPage } from "@/features/problem/KnownErrorSearchPage";
import { ChangeListPage } from "@/features/change/ChangeListPage";
import { ChangeCreatePage } from "@/features/change/ChangeCreatePage";
import { ChangeDetailPage } from "@/features/change/ChangeDetailPage";
import { ChangeApprovalInboxPage } from "@/features/change/ChangeApprovalInboxPage";
import { ChangeSchedulePage } from "@/features/change/ChangeSchedulePage";
import { ChangeMetricsPage } from "@/features/change/ChangeMetricsPage";
import { KnowledgeListPage } from "@/features/knowledge/KnowledgeListPage";
import { ArticleViewPage } from "@/features/knowledge/ArticleViewPage";
import { ArticleEditPage } from "@/features/knowledge/ArticleEditPage";
import { ReviewInboxPage } from "@/features/knowledge/ReviewInboxPage";
import { KnowledgeMetricsPage } from "@/features/knowledge/KnowledgeMetricsPage";
import { AssetListPage } from "@/features/asset/AssetListPage";
import { AssetFormPage } from "@/features/asset/AssetFormPage";
import { AssetDetailPage } from "@/features/asset/AssetDetailPage";
import { CiRelationPage } from "@/features/asset/CiRelationPage";
import { AssetMetricsPage } from "@/features/asset/AssetMetricsPage";
import { SearchResultsPage } from "@/features/search/SearchResultsPage";
import { DeptPortalPage } from "@/features/esm/DeptPortalPage";
import { DeptRequestSubmitPage } from "@/features/esm/DeptRequestSubmitPage";
import { MyEsmRequestsPage } from "@/features/esm/MyEsmRequestsPage";
import { EsmRequestQueuePage } from "@/features/esm/EsmRequestQueuePage";
import { EsmRequestDetailPage } from "@/features/esm/EsmRequestDetailPage";
import { EsmCatalogManagePage } from "@/features/esm/EsmCatalogManagePage";
import { HrCaseListPage } from "@/features/esm/HrCaseListPage";
import { HrCaseDetailPage } from "@/features/esm/HrCaseDetailPage";
import { ChecklistDetailPage } from "@/features/esm/ChecklistDetailPage";
import { MyChecklistTasksPage } from "@/features/esm/MyChecklistTasksPage";
import { EsmMetricsPage } from "@/features/esm/EsmMetricsPage";
import { VulnerabilityListPage } from "@/features/vulnerability/VulnerabilityListPage";
import { VulnerabilityCreatePage } from "@/features/vulnerability/VulnerabilityCreatePage";
import { VulnerabilityDetailPage } from "@/features/vulnerability/VulnerabilityDetailPage";
import { VulnerabilityMetricsPage } from "@/features/vulnerability/VulnerabilityMetricsPage";
import { ComplianceListPage } from "@/features/compliance/ComplianceListPage";
import { ComplianceCreatePage } from "@/features/compliance/ComplianceCreatePage";
import { ComplianceDetailPage } from "@/features/compliance/ComplianceDetailPage";
import { ComplianceMetricsPage } from "@/features/compliance/ComplianceMetricsPage";

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
          { path: "/search", element: <SearchResultsPage /> }, // SCR-COM-011

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

          // 문제(PRB) — PROBLEM_MANAGER
          {
            element: <RequireRoles roles={[ROLE_PROBLEM_MANAGER]} />,
            children: [
              { path: "/problems", element: <ProblemListPage /> }, // SCR-PRB-001
              { path: "/problems/new", element: <ProblemCreatePage /> }, // SCR-PRB-002
              { path: "/problems/:id", element: <ProblemDetailPage /> }, // SCR-PRB-003
              { path: "/known-errors", element: <KnownErrorSearchPage /> }, // SCR-PRB-004
            ],
          },

          // 변경(CHG) — CHANGE_MANAGER/APPROVER
          {
            element: <RequireRoles roles={[ROLE_CHANGE_MANAGER]} />,
            children: [
              { path: "/changes", element: <ChangeListPage /> }, // SCR-CHG-001
              { path: "/changes/new", element: <ChangeCreatePage /> }, // SCR-CHG-002
              { path: "/changes/schedule", element: <ChangeSchedulePage /> }, // SCR-CHG-005
              { path: "/changes/metrics", element: <ChangeMetricsPage /> }, // SCR-CHG-006
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_APPROVER]} />,
            children: [
              { path: "/approvals/changes", element: <ChangeApprovalInboxPage /> }, // SCR-CHG-004
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_CHANGE_MANAGER, ROLE_APPROVER]} />,
            children: [
              { path: "/changes/:id", element: <ChangeDetailPage /> }, // SCR-CHG-003
            ],
          },

          // 지식 관리(KM) — KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER(검색·열람은 END_USER/SERVICE_DESK_AGENT도 포함)
          {
            element: (
              <RequireRoles
                roles={[
                  ROLE_END_USER,
                  ROLE_SERVICE_DESK_AGENT,
                  ROLE_KNOWLEDGE_CONTRIBUTOR,
                  ROLE_KNOWLEDGE_GATEKEEPER,
                ]}
              />
            ),
            children: [
              { path: "/knowledge", element: <KnowledgeListPage /> }, // SCR-KM-001
              { path: "/knowledge/:id", element: <ArticleViewPage /> }, // SCR-KM-002
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_KNOWLEDGE_CONTRIBUTOR]} />,
            children: [
              { path: "/knowledge/new", element: <ArticleEditPage /> }, // SCR-KM-003
              { path: "/knowledge/:id/edit", element: <ArticleEditPage /> }, // SCR-KM-003
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_KNOWLEDGE_GATEKEEPER]} />,
            children: [
              { path: "/knowledge/reviews", element: <ReviewInboxPage /> }, // SCR-KM-004
              { path: "/knowledge/metrics", element: <KnowledgeMetricsPage /> }, // SCR-KM-005
            ],
          },

          // 자산(ITAM) — ASSET_MANAGER
          {
            element: <RequireRoles roles={[ROLE_ASSET_MANAGER]} />,
            children: [
              { path: "/assets", element: <AssetListPage /> }, // SCR-ITAM-001
              { path: "/assets/new", element: <AssetFormPage /> }, // SCR-ITAM-002
              { path: "/assets/:id/edit", element: <AssetFormPage /> }, // SCR-ITAM-002
              { path: "/assets/:id", element: <AssetDetailPage /> }, // SCR-ITAM-003
              { path: "/assets/cis", element: <CiRelationPage /> }, // SCR-ITAM-004
              { path: "/assets/metrics", element: <AssetMetricsPage /> }, // SCR-ITAM-005
            ],
          },

          // 엔터프라이즈 서비스 관리(ESM) — END_USER(포털·제출·내 요청)/DEPT_COORDINATOR(처리 큐·내 하위 작업)/PROCESS_OWNER(카탈로그·지표)
          {
            element: <RequireRoles roles={[ROLE_END_USER]} />,
            children: [
              { path: "/esm/portal", element: <DeptPortalPage /> }, // SCR-ESM-001
              { path: "/esm/portal/requests/new", element: <DeptRequestSubmitPage /> }, // SCR-ESM-002
              { path: "/esm/requests", element: <MyEsmRequestsPage /> }, // SCR-ESM-003
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_DEPT_COORDINATOR]} />,
            children: [
              { path: "/esm/requests/queue", element: <EsmRequestQueuePage /> }, // SCR-ESM-004
              { path: "/esm/checklist-tasks", element: <MyChecklistTasksPage /> }, // SCR-ESM-010
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_END_USER, ROLE_DEPT_COORDINATOR]} />,
            children: [
              { path: "/esm/requests/:id", element: <EsmRequestDetailPage /> }, // SCR-ESM-005
              { path: "/esm/checklists/:id", element: <ChecklistDetailPage /> }, // SCR-ESM-009
            ],
          },
          {
            element: <RequireRoles roles={[ROLE_PROCESS_OWNER]} />,
            children: [
              { path: "/admin/esm-catalog", element: <EsmCatalogManagePage /> }, // SCR-ESM-006
              { path: "/esm/metrics", element: <EsmMetricsPage /> }, // SCR-ESM-011
            ],
          },

          // HR 케이스 — HR_CASE_MANAGER 전용(민감정보, 타 역할 403)
          {
            element: <RequireRoles roles={[ROLE_HR_CASE_MANAGER]} />,
            children: [
              { path: "/esm/hr-cases", element: <HrCaseListPage /> }, // SCR-ESM-007
              { path: "/esm/hr-cases/:id", element: <HrCaseDetailPage /> }, // SCR-ESM-008
            ],
          },

          // 취약점(VULN) — VULNERABILITY_MANAGER
          {
            element: <RequireRoles roles={[ROLE_VULNERABILITY_MANAGER]} />,
            children: [
              { path: "/vulnerabilities", element: <VulnerabilityListPage /> }, // SCR-VULN-001
              { path: "/vulnerabilities/new", element: <VulnerabilityCreatePage /> }, // SCR-VULN-002
              { path: "/vulnerabilities/:id", element: <VulnerabilityDetailPage /> }, // SCR-VULN-003
              { path: "/vulnerabilities/metrics", element: <VulnerabilityMetricsPage /> }, // SCR-VULN-004
            ],
          },

          // 컴플라이언스(COMP) — COMPLIANCE_OFFICER
          {
            element: <RequireRoles roles={[ROLE_COMPLIANCE_OFFICER]} />,
            children: [
              { path: "/compliance/requirements", element: <ComplianceListPage /> }, // SCR-COMP-001
              { path: "/compliance/requirements/new", element: <ComplianceCreatePage /> }, // SCR-COMP-002
              { path: "/compliance/requirements/:id", element: <ComplianceDetailPage /> }, // SCR-COMP-003
              { path: "/compliance/metrics", element: <ComplianceMetricsPage /> }, // SCR-COMP-004
            ],
          },

          // 그 외 미매칭 경로 → 404(SCR-ERR-404)
          { path: "*", element: <NotFoundPage /> },
        ],
      },
    ],
  },
]);
