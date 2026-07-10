import { useEffect, useLayoutEffect, useRef, useState } from "react";

import { EmptyState } from "@/components/common/empty-state";
import { cn } from "@/lib/utils";

/**
 * 추이 차트 — SCR-SRM-008 기간별 추이. 단일 시계열.
 * 색은 Variation2(info 토큰). SVG 기반(추가 라이브러리 없음), hover 크로스헤어+툴팁.
 * 데이터가 없으면 빈 상태를 표시한다. (dataviz: 단일 계열이라 범례 불필요, 제목이 계열을 명명)
 */
export interface TrendPoint {
  label: string;
  value: number;
}

export interface TrendChartProps {
  data: TrendPoint[];
  height?: number;
  valueFormatter?: (v: number) => string;
  ariaLabel?: string;
  className?: string;
}

const PAD = { top: 16, right: 16, bottom: 28, left: 44 };
const GRID_LINES = 4;

/** 축 상한을 보기 좋은 값으로 올림 */
function niceMax(max: number): number {
  if (max <= 0) return 1;
  const exp = Math.floor(Math.log10(max));
  const base = Math.pow(10, exp);
  const frac = max / base;
  const niceFrac = frac <= 1 ? 1 : frac <= 2 ? 2 : frac <= 5 ? 5 : 10;
  return niceFrac * base;
}

export function TrendChart({
  data,
  height = 220,
  valueFormatter = (v) => String(v),
  ariaLabel,
  className,
}: TrendChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [width, setWidth] = useState(0);
  const [hover, setHover] = useState<number | null>(null);

  useLayoutEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    setWidth(el.clientWidth);
  }, []);

  useEffect(() => {
    const el = containerRef.current;
    if (!el || typeof ResizeObserver === "undefined") return;
    const ro = new ResizeObserver((entries) => {
      setWidth(entries[0].contentRect.width);
    });
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  if (data.length === 0) {
    return (
      <div ref={containerRef} className={cn("w-full", className)} style={{ height }}>
        <EmptyState title="표시할 추이 데이터가 없습니다" className="h-full py-0" />
      </div>
    );
  }

  const innerW = Math.max(0, width - PAD.left - PAD.right);
  const innerH = Math.max(0, height - PAD.top - PAD.bottom);
  const yMax = niceMax(Math.max(...data.map((d) => d.value)));
  const n = data.length;

  const xAt = (i: number) => (n === 1 ? PAD.left + innerW / 2 : PAD.left + (innerW * i) / (n - 1));
  const yAt = (v: number) => PAD.top + innerH * (1 - v / yMax);

  const linePath = data
    .map((d, i) => `${i === 0 ? "M" : "L"}${xAt(i).toFixed(1)},${yAt(d.value).toFixed(1)}`)
    .join(" ");
  const areaPath =
    width > 0
      ? `${linePath} L${xAt(n - 1).toFixed(1)},${(PAD.top + innerH).toFixed(1)} L${xAt(0).toFixed(1)},${(PAD.top + innerH).toFixed(1)} Z`
      : "";

  // x축 라벨 솎아내기(최대 6개 정도)
  const labelEvery = Math.max(1, Math.ceil(n / 6));

  const handleMove = (e: React.PointerEvent<SVGSVGElement>) => {
    if (width === 0) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const i =
      n === 1 ? 0 : Math.round(((x - PAD.left) / innerW) * (n - 1));
    setHover(Math.min(n - 1, Math.max(0, i)));
  };

  const active = hover != null ? data[hover] : null;

  return (
    <div
      ref={containerRef}
      className={cn("relative w-full", className)}
      style={{ height }}
    >
      {width > 0 ? (
        <svg
          width={width}
          height={height}
          role="img"
          aria-label={ariaLabel ?? "기간별 추이 차트"}
          onPointerMove={handleMove}
          onPointerLeave={() => setHover(null)}
        >
          {/* 가로 그리드 + y축 라벨 (recessive) */}
          {Array.from({ length: GRID_LINES + 1 }).map((_, i) => {
            const v = (yMax * (GRID_LINES - i)) / GRID_LINES;
            const y = yAt(v);
            return (
              <g key={i}>
                <line
                  x1={PAD.left}
                  x2={width - PAD.right}
                  y1={y}
                  y2={y}
                  stroke="var(--border)"
                  strokeWidth={1}
                />
                <text
                  x={PAD.left - 8}
                  y={y + 3}
                  textAnchor="end"
                  className="fill-muted-foreground"
                  fontSize={10}
                >
                  {valueFormatter(Math.round(v))}
                </text>
              </g>
            );
          })}

          {/* area + line (Variation2 = info) */}
          <path d={areaPath} fill="var(--info)" fillOpacity={0.12} />
          <path
            d={linePath}
            fill="none"
            stroke="var(--info)"
            strokeWidth={2}
            strokeLinejoin="round"
            strokeLinecap="round"
          />

          {/* x축 라벨 */}
          {data.map((d, i) =>
            i % labelEvery === 0 || i === n - 1 ? (
              <text
                key={i}
                x={xAt(i)}
                y={height - 8}
                textAnchor="middle"
                className="fill-muted-foreground"
                fontSize={10}
              >
                {d.label}
              </text>
            ) : null,
          )}

          {/* hover 크로스헤어 + 마커 */}
          {active && hover != null ? (
            <>
              <line
                x1={xAt(hover)}
                x2={xAt(hover)}
                y1={PAD.top}
                y2={PAD.top + innerH}
                stroke="var(--border)"
                strokeWidth={1}
                strokeDasharray="3 3"
              />
              <circle
                cx={xAt(hover)}
                cy={yAt(active.value)}
                r={4}
                fill="var(--info)"
                stroke="var(--background)"
                strokeWidth={2}
              />
            </>
          ) : null}
        </svg>
      ) : null}

      {/* 툴팁 (HTML 오버레이) */}
      {active && hover != null && width > 0 ? (
        <div
          className="pointer-events-none absolute z-10 -translate-x-1/2 -translate-y-full rounded-lg border border-border bg-popover px-2 py-1 text-xs shadow-overlay"
          style={{ left: xAt(hover), top: yAt(active.value) - 8 }}
        >
          <div className="font-medium text-foreground">{valueFormatter(active.value)}</div>
          <div className="text-muted-foreground">{active.label}</div>
        </div>
      ) : null}
    </div>
  );
}
