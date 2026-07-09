import { useEffect } from "react";
import { Navigate, useLocation } from "react-router-dom";

import { AppLayout } from "@/routes/AppLayout";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { bootstrapAuth } from "@/store/authSlice";

/*
 * 인증 가드 — 보호 라우트 진입 시 세션을 확인한다(SCR-COM-005).
 * 최초 진입 시 /auth/me로 세션을 복구(만료 시 apiClient가 쿠키로 1회 재발급)하고,
 * 미인증이면 로그인 화면으로 이동한다. 인증되면 앱 셸(AppLayout)을 렌더한다.
 */
export function AuthGuard() {
  const dispatch = useAppDispatch();
  const status = useAppSelector((s) => s.auth.status);
  const location = useLocation();

  useEffect(() => {
    if (status === "idle") {
      dispatch(bootstrapAuth());
    }
  }, [status, dispatch]);

  if (status === "idle" || status === "loading") {
    return <FullscreenLoader />;
  }

  if (status === "unauthenticated") {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <AppLayout />;
}
