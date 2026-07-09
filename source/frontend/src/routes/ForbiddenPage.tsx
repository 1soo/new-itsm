import { useNavigate } from "react-router-dom";

import { ForbiddenView } from "@/components/common";

/** 403 접근 거부(SCR-COM-006) — "이전으로" 동작 주입. */
export function ForbiddenPage() {
  const navigate = useNavigate();
  return <ForbiddenView onBack={() => navigate(-1)} />;
}
