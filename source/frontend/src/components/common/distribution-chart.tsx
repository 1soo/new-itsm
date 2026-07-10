import { useEffect, useLayoutEffect, useRef, useState } from "react";

import { EmptyState } from "@/components/common/empty-state";
import type { StatusTone } from "@/components/common/status-badge";
import { cn } from "@/lib/utils";

/**
 * 분포 차트 — SCR-INC-005 심각도(SEV) 분포. 범주형 세로 막대.
 * 막대 색은 상태 시맨틱 tone(SEV1=danger/SEV2=warning/SEV3=info 등, FE가 지정).
 * 각 막대는 범주 라벨을 가지므로 색상 단독 전달이 아니다(a11y). 빈 데이터 시 빈 상태.
 */
export interface DistributionDatum {
  label: string;
  value: number;
  tone?: StatusTone;
}

export interface DistributionChartProps {
  data: DistributionDatum[];
  height?: number;
  valueFormatter?: (v: number) => string;
  ariaLabel?: string;
  className?: string;
}

const PAD = { top: 20, right: 12, bottom: 28, left: 40 };
const GRID_LINES = 4;

const TONE_VAR: Record<StatusTone, string> = {
  success: "var(--success)",
  warning: "var(--warning)",
  danger: "var(--danger)",
  info: "var(--info)",
  muted: "var(--neutral)",
};

function niceMax(max: number): number {
  if (max <= 0) return 1;
  const exp = Math.floor(Math.log10(max));
  const base = Math.pow(10, exp);
  const frac = max / base;
  const niceFrac = frac <= 1 ? 1 : frac <= 2 ? 2 : frac <= 5 ? 5 : 10;
  return niceFrac * base;
}

export function DistributionChart({
  data,
  height = 220,
  valueFormatter = (v) => String(v),
  ariaLabel,
  className,
}: DistributionChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [width, setWidth] = useState(0);
  const [hover, setHover] = useState<number | null>(null);

  useLayoutEffect(() => {
    if (containerRef.current) setWidth(containerRef.current.clientWidth);
  }, []);

  useEffect(() => {
    const el = containerRef.current;
    if (!el || typeof ResizeObserver === "undefined") return;
    const ro = new ResizeObserver((entries) => setWidth(entries[0].contentRect.width));
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  if (data.length === 0) {
    return (
      <div ref={containerRef} className={cn("w-full", className)} style={{ height }}>
        <EmptyState title="표시할 분포 데이터가 없습니다" className="h-full py-0" />
      </div>
    );
  }

  const innerW = Math.max(0, width - PAD.left - PAD.right);
  const innerH = Math.max(0, height - PAD.top - PAD.bottom);
  const yMax = niceMax(Math.max(...data.map((d) => d.value)));
  const n = data.length;
  const band = n > 0 ? innerW / n : innerW;
  const barW = Math.min(64, band * 0.6);
  const baseline = PAD.top + innerH;

  const xCenter = (i: number) => PAD.left + band * i + band / 2;
  const yAt = (v: number) => PAD.top + innerH * (1 - v / yMax);

  return (
    <div
      ref={containerRef}
      className={cn("relative w-full", className)}
      style={{ height }}
    >
      {width > 0 ? (
        <svg width={width} height={height} role="img" aria-label={ariaLabel ?? "분포 차트"}>
          {/* 가로 그리드 + y축 라벨 */}
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

          {/* 막대 + 값 라벨 + 범주 라벨 */}
          {data.map((d, i) => {
            const x = xCenter(i) - barW / 2;
            const y = yAt(d.value);
            const h = baseline - y;
            const color = TONE_VAR[d.tone ?? "info"];
            const active = hover === i;
            return (
              <g
                key={i}
                onPointerEnter={() => setHover(i)}
                onPointerLeave={() => setHover(null)}
              >
                {/* hover 히트 영역(밴드 전체) */}
                <rect
                  x={PAD.left + band * i}
                  y={PAD.top}
                  width={band}
                  height={innerH}
                  fill="transparent"
                />
                <rect
                  x={x}
                  y={y}
                  width={barW}
                  height={Math.max(0, h)}
                  rx={3}
                  fill={color}
                  fillOpacity={active ? 1 : 0.85}
                />
                <text
                  x={xCenter(i)}
                  y={y - 6}
                  textAnchor="middle"
                  className="fill-foreground"
                  fontSize={11}
                  fontWeight={600}
                >
                  {valueFormatter(d.value)}
                </text>
                <text
                  x={xCenter(i)}
                  y={height - 8}
                  textAnchor="middle"
                  className="fill-muted-foreground"
                  fontSize={10}
                >
                  {d.label}
                </text>
              </g>
            );
          })}
        </svg>
      ) : null}

      {/* 툴팁 */}
      {hover != null && width > 0 ? (
        <div
          className="pointer-events-none absolute z-10 -translate-x-1/2 -translate-y-full rounded-lg border border-border bg-popover px-2 py-1 text-xs shadow-overlay"
          style={{ left: xCenter(hover), top: yAt(data[hover].value) - 8 }}
        >
          <div className="font-medium text-foreground">
            {valueFormatter(data[hover].value)}
          </div>
          <div className="text-muted-foreground">{data[hover].label}</div>
        </div>
      ) : null}
    </div>
  );
}
