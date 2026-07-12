import { ChevronLeft, ChevronRight } from "lucide-react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

/**
 * 페이지네이션 — common.md SCR-COM-007.
 * 0-based page 인덱스. 총 페이지 수 기준 이전/다음·번호 이동.
 */
export interface PaginationProps {
  page: number; // 0-based 현재 페이지
  totalPages: number;
  onPageChange: (page: number) => void;
  className?: string;
}

/** 현재 페이지 주변 최대 5개 번호 창을 계산한다. */
function pageWindow(page: number, totalPages: number): number[] {
  const windowSize = 5;
  let start = Math.max(0, page - Math.floor(windowSize / 2));
  const end = Math.min(totalPages, start + windowSize);
  start = Math.max(0, end - windowSize);
  return Array.from({ length: end - start }, (_, i) => start + i);
}

export function Pagination({
  page,
  totalPages,
  onPageChange,
  className,
}: PaginationProps) {
  const { t } = useTranslation("common");
  if (totalPages <= 1) return null;

  const pages = pageWindow(page, totalPages);

  return (
    <nav
      className={cn("flex items-center justify-center gap-1", className)}
      aria-label={t("pagination.nav")}
    >
      <Button
        variant="outline"
        size="icon"
        onClick={() => onPageChange(page - 1)}
        disabled={page <= 0}
        aria-label={t("pagination.previous")}
      >
        <ChevronLeft />
      </Button>
      {pages.map((p) => (
        <Button
          key={p}
          variant={p === page ? "default" : "outline"}
          size="icon"
          onClick={() => onPageChange(p)}
          aria-current={p === page ? "page" : undefined}
        >
          {p + 1}
        </Button>
      ))}
      <Button
        variant="outline"
        size="icon"
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        aria-label={t("pagination.next")}
      >
        <ChevronRight />
      </Button>
    </nav>
  );
}
