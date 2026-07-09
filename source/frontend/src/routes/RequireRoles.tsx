import { Navigate, Outlet } from "react-router-dom";

import { hasAnyRole } from "@/features/auth/roles";
import { useAppSelector } from "@/store/hooks";

/*
 * 역할 기반 라우트 가드 — allowed 역할 중 하나라도 보유해야 접근 허용.
 * 미달 시 403(SCR-COM-006)으로 이동한다. (인가 설계 준수, 레코드 단위 권한은 BE가 최종 검증)
 */
export function RequireRoles({ roles }: { roles: string[] }) {
  const user = useAppSelector((s) => s.auth.user);

  if (!hasAnyRole(user?.roles, roles)) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}
