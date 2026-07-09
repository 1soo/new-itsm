import type { ReactNode } from "react";
import {
  Activity,
  AlertTriangle,
  BookOpen,
  CalendarDays,
  ClipboardList,
  GitPullRequest,
  Inbox,
  LayoutDashboard,
  LayoutGrid,
  ListChecks,
  ScrollText,
  SearchCode,
  Settings2,
  ShieldCheck,
  Stamp,
  User,
  Users,
} from "lucide-react";

import {
  ROLE_APPROVER,
  ROLE_CHANGE_MANAGER,
  ROLE_END_USER,
  ROLE_INCIDENT_MANAGER,
  ROLE_PROBLEM_MANAGER,
  ROLE_PROCESS_OWNER,
  ROLE_SERVICE_DESK_AGENT,
  ROLE_SYSTEM_ADMIN,
} from "@/features/auth/roles";

/*
 * 사이드바 내비게이션 설정(중앙 관리) — 도메인 확장 시 여기에만 추가한다.
 * roles가 없으면 인증된 모든 사용자에게 노출, 있으면 해당 역할 보유자에게만 노출(RBAC).
 * 실제 필터링·active 계산·navigate 주입은 AppLayout이 수행한다(dev-ui 셸은 프레젠테이션 전용).
 */
export interface NavItemDef {
  key: string;
  label: string;
  path: string;
  icon: ReactNode;
  /** 노출 허용 역할(미지정 시 전체 인증 사용자). */
  roles?: string[];
}

export interface NavGroupDef {
  key: string;
  label?: string;
  items: NavItemDef[];
}

export const navConfig: NavGroupDef[] = [
  {
    key: "main",
    items: [
      { key: "dashboard", label: "대시보드", path: "/", icon: <LayoutDashboard /> },
      { key: "profile", label: "내 프로필", path: "/profile", icon: <User /> },
    ],
  },
  {
    key: "srm",
    label: "서비스 요청",
    items: [
      { key: "srm-portal", label: "서비스 포털", path: "/portal", icon: <LayoutGrid />, roles: [ROLE_END_USER] },
      {
        key: "srm-requests",
        label: "요청 목록",
        path: "/service-requests",
        icon: <ClipboardList />,
        roles: [ROLE_END_USER, ROLE_SERVICE_DESK_AGENT],
      },
      { key: "srm-queue", label: "요청 큐", path: "/service-requests/queue", icon: <Inbox />, roles: [ROLE_SERVICE_DESK_AGENT] },
      {
        key: "srm-approvals",
        label: "승인 대기함",
        path: "/approvals/service-requests",
        icon: <Stamp />,
        roles: [ROLE_APPROVER],
      },
      {
        key: "srm-catalog",
        label: "서비스 카탈로그",
        path: "/admin/service-catalog",
        icon: <Settings2 />,
        roles: [ROLE_PROCESS_OWNER],
      },
      {
        key: "srm-metrics",
        label: "요청 지표",
        path: "/service-requests/metrics",
        icon: <ListChecks />,
        roles: [ROLE_PROCESS_OWNER],
      },
    ],
  },
  {
    key: "inc",
    label: "인시던트",
    items: [
      {
        key: "inc-list",
        label: "인시던트",
        path: "/incidents",
        icon: <AlertTriangle />,
        roles: [ROLE_SERVICE_DESK_AGENT, ROLE_INCIDENT_MANAGER],
      },
      {
        key: "inc-metrics",
        label: "인시던트 지표",
        path: "/incidents/metrics",
        icon: <Activity />,
        roles: [ROLE_INCIDENT_MANAGER],
      },
    ],
  },
  {
    key: "prb",
    label: "문제",
    items: [
      {
        key: "prb-list",
        label: "문제",
        path: "/problems",
        icon: <BookOpen />,
        roles: [ROLE_PROBLEM_MANAGER],
      },
      {
        key: "prb-kedb",
        label: "KEDB 검색",
        path: "/known-errors",
        icon: <SearchCode />,
        roles: [ROLE_PROBLEM_MANAGER],
      },
    ],
  },
  {
    key: "chg",
    label: "변경",
    items: [
      {
        key: "chg-list",
        label: "변경",
        path: "/changes",
        icon: <GitPullRequest />,
        roles: [ROLE_CHANGE_MANAGER],
      },
      {
        key: "chg-approvals",
        label: "CAB 승인 대기함",
        path: "/approvals/changes",
        icon: <Stamp />,
        roles: [ROLE_APPROVER],
      },
      {
        key: "chg-schedule",
        label: "변경 일정",
        path: "/changes/schedule",
        icon: <CalendarDays />,
        roles: [ROLE_CHANGE_MANAGER],
      },
      {
        key: "chg-metrics",
        label: "변경 지표",
        path: "/changes/metrics",
        icon: <Activity />,
        roles: [ROLE_CHANGE_MANAGER],
      },
    ],
  },
  {
    key: "admin",
    label: "관리자",
    items: [
      { key: "admin-users", label: "계정 관리", path: "/admin/users", icon: <Users />, roles: [ROLE_SYSTEM_ADMIN] },
      { key: "admin-roles", label: "역할 관리", path: "/admin/roles", icon: <ShieldCheck />, roles: [ROLE_SYSTEM_ADMIN] },
      {
        key: "admin-audit",
        label: "감사 로그",
        path: "/admin/audit-logs",
        icon: <ScrollText />,
        roles: [ROLE_SYSTEM_ADMIN],
      },
    ],
  },
];
