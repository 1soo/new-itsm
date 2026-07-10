import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/lib/utils";

/**
 * 배지 — common.md 2.2절 Lozenge(4px radius + 테두리).
 * variant는 시맨틱 토큰만 사용한다(색상 하드코딩 금지).
 * emphasis는 subtle(기본)/bold이며, bold는 P1·SEV1 등 강조가 꼭 필요한 예외에만 허용한다.
 */
const badgeVariants = cva(
  "inline-flex items-center gap-1 rounded-sm border px-2 py-0.5 text-xs font-medium transition-colors focus:outline-none",
  {
    variants: {
      variant: {
        default: "border-transparent bg-primary text-primary-foreground",
        secondary: "border-transparent bg-secondary text-secondary-foreground",
        outline: "border-border text-foreground",
        success: "border-success-subtle-foreground/30 bg-success-subtle text-success-subtle-foreground",
        warning: "border-warning-subtle-foreground/30 bg-warning-subtle text-warning-subtle-foreground",
        danger: "border-danger-subtle-foreground/30 bg-danger-subtle text-danger-subtle-foreground",
        info: "border-info-subtle-foreground/30 bg-info-subtle text-info-subtle-foreground",
        muted: "border-neutral-subtle-foreground/30 bg-neutral-subtle text-neutral-subtle-foreground",
      },
      emphasis: {
        subtle: "",
        bold: "",
      },
    },
    compoundVariants: [
      { variant: "success", emphasis: "bold", class: "border-transparent bg-success text-success-foreground" },
      { variant: "warning", emphasis: "bold", class: "border-transparent bg-warning text-warning-foreground" },
      { variant: "danger", emphasis: "bold", class: "border-transparent bg-danger text-danger-foreground" },
      { variant: "info", emphasis: "bold", class: "border-transparent bg-info text-info-foreground" },
      { variant: "muted", emphasis: "bold", class: "border-transparent bg-neutral text-neutral-foreground" },
    ],
    defaultVariants: {
      variant: "default",
      emphasis: "subtle",
    },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, emphasis, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ variant, emphasis }), className)} {...props} />;
}

export { Badge, badgeVariants };
