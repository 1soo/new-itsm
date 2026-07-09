import { useEffect, useRef } from "react";
import { Outlet, useNavigate } from "react-router-dom";

import { toast } from "@/components/common";
import { setSessionExpiredHandler } from "@/lib/apiClient";
import { useAppDispatch, useAppSelector } from "@/store/hooks";
import { sessionExpired } from "@/store/authSlice";

/*
 * 세션 브리지 — apiClient의 세션 만료(401 재발급 실패) 이벤트를 라우터/스토어에 연결한다.
 * (SCR-COM-005) 재발급 실패 시 세션 종료 후 로그인 화면으로 이동하고,
 * 이전에 인증된 상태였을 때만 만료 토스트를 표시한다(초기 부트스트랩 시 오탐 방지).
 */
export function SessionBridge() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const status = useAppSelector((s) => s.auth.status);

  const statusRef = useRef(status);
  statusRef.current = status;

  useEffect(() => {
    setSessionExpiredHandler(() => {
      const wasAuthenticated = statusRef.current === "authenticated";
      dispatch(sessionExpired());
      if (wasAuthenticated) {
        toast.error("세션이 만료되어 다시 로그인해주세요");
      }
      navigate("/login", { replace: true });
    });
    return () => setSessionExpiredHandler(null);
  }, [dispatch, navigate]);

  return <Outlet />;
}
