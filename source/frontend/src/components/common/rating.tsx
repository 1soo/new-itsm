import { useState } from "react";
import { Star } from "lucide-react";

import { cn } from "@/lib/utils";

/**
 * 별점 — SCR-SRM-005 CSAT 위젯. 종료 시 요청자에게 노출.
 * 읽기 전용(readOnly) 또는 입력용(onChange). 채워진 별은 Success 톤.
 */
export interface RatingProps {
  value: number;
  max?: number;
  onChange?: (value: number) => void;
  readOnly?: boolean;
  size?: "sm" | "md" | "lg";
  className?: string;
}

const SIZE_CLASS = {
  sm: "size-4",
  md: "size-5",
  lg: "size-7",
} as const;

export function Rating({
  value,
  max = 5,
  onChange,
  readOnly = false,
  size = "md",
  className,
}: RatingProps) {
  const [hover, setHover] = useState<number | null>(null);
  const interactive = !readOnly && !!onChange;
  const shown = hover ?? value;

  return (
    <div
      className={cn("inline-flex items-center gap-1", className)}
      role={interactive ? "radiogroup" : "img"}
      aria-label={`별점 ${value} / ${max}`}
      onMouseLeave={() => setHover(null)}
    >
      {Array.from({ length: max }).map((_, i) => {
        const starValue = i + 1;
        const filled = starValue <= shown;
        const StarIcon = (
          <Star
            className={cn(
              SIZE_CLASS[size],
              filled ? "fill-success text-success" : "fill-transparent text-muted-foreground",
            )}
            aria-hidden="true"
          />
        );

        if (!interactive) {
          return <span key={i}>{StarIcon}</span>;
        }

        return (
          <button
            key={i}
            type="button"
            role="radio"
            aria-checked={starValue === value}
            aria-label={`${starValue}점`}
            className="rounded outline-none transition-transform hover:scale-110 focus-visible:ring-2 focus-visible:ring-ring"
            onClick={() => onChange?.(starValue)}
            onMouseEnter={() => setHover(starValue)}
          >
            {StarIcon}
          </button>
        );
      })}
    </div>
  );
}
