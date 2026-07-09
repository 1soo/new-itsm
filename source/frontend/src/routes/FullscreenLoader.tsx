import { Loader2 } from "lucide-react";

/** 전체 화면 로딩 표시 — 세션 부트스트랩 등 초기 로딩 시 사용. */
export function FullscreenLoader() {
  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Loader2 className="size-8 animate-spin text-primary" aria-label="로딩 중" />
    </div>
  );
}
