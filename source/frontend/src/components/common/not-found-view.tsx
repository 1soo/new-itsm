import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

/**
 * 404 Not Found — error_404.md SCR-ERR-404 (프레젠테이션 전용).
 * 중앙 정렬. 대형 "404" / 안내 문구 / "홈으로" 버튼(FE가 onHome 주입).
 */
export interface NotFoundViewProps {
  onHome?: () => void;
  className?: string;
}

export function NotFoundView({ onHome, className }: NotFoundViewProps) {
  const { t } = useTranslation("common");
  return (
    <div
      className={cn(
        "flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center",
        className,
      )}
    >
      <p className="text-7xl font-bold text-muted-foreground">404</p>
      <p className="text-sm text-foreground">{t("notFound.description")}</p>
      {onHome ? (
        <Button onClick={onHome}>{t("notFound.homeButton")}</Button>
      ) : null}
    </div>
  );
}
