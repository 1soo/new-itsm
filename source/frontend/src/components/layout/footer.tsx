import { cn } from "@/lib/utils";

/**
 * 푸터 — common.md SCR-COM-004. 좌측 저작권, 우측 버전. 정적 표시.
 */
export interface FooterProps {
  copyright?: string;
  version?: string;
  className?: string;
}

export function Footer({
  copyright = "© 2026 ITSM Platform",
  version = "v0.1.0",
  className,
}: FooterProps) {
  return (
    <footer
      className={cn(
        "flex h-10 shrink-0 items-center justify-between border-t border-border bg-background px-6 text-xs text-muted-foreground",
        className,
      )}
    >
      <span>{copyright}</span>
      <span>{version}</span>
    </footer>
  );
}
