import { useNavigate } from "react-router-dom";

import { NotFoundView } from "@/components/common";

/** 404 Not Found(SCR-ERR-404) — "홈으로" 동작 주입. */
export function NotFoundPage() {
  const navigate = useNavigate();
  return <NotFoundView onHome={() => navigate("/")} />;
}
