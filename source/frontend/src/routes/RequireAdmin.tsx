import { Navigate, Outlet } from "react-router-dom";

import { isSystemAdmin } from "@/features/auth/roles";
import { useAppSelector } from "@/store/hooks";

/*
 * 관리자 라우트 가드 — 관리자(/admin/*) 라우트는 SYSTEM_ADMIN 전용.
 * 권한 미달 시 403(SCR-COM-006)으로 이동한다. (인가 설계 준수)
 */
export function RequireAdmin() {
  const user = useAppSelector((s) => s.auth.user);

  if (!isSystemAdmin(user?.roles)) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}
