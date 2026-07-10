import { type FormEvent, useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { KpiCard, toast } from "@/components/common";
import { esmApi } from "@/features/esm/api";
import { DEPARTMENTS, departmentLabel } from "@/features/esm/status";
import type { Department, EsmMetrics } from "@/features/esm/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * ESM 지표 대시보드(SCR-ESM-011) — 부서별 처리량·평균 처리시간·온보딩/오프보딩 완료율.
 * API-ESM-017은 집계값만 반환(시계열 없음)하므로 KPI 카드로 표시한다.
 */
const ALL = "ALL";

function roundOr0(n: number | undefined, digits = 0): string {
  if (n == null || Number.isNaN(n)) return "0";
  return n.toFixed(digits);
}

/** BE가 OffsetDateTime(ISO.DATE_TIME)으로 바인딩하므로 date input 값을 로컬 자정/자정 직전 시각의 UTC ISO 문자열로 변환한다(다른 도메인의 toISOString() 컨벤션과 동일). */
function toStartOfDay(date: string): string {
  return new Date(`${date}T00:00:00`).toISOString();
}
function toEndOfDay(date: string): string {
  return new Date(`${date}T23:59:59`).toISOString();
}

export function EsmMetricsPage() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [departmentInput, setDepartmentInput] = useState(ALL);
  const [applied, setApplied] = useState<{ from: string; to: string; department: string }>({
    from: "",
    to: "",
    department: ALL,
  });
  const [metrics, setMetrics] = useState<EsmMetrics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    esmApi
      .metrics({
        from: applied.from ? toStartOfDay(applied.from) : undefined,
        to: applied.to ? toEndOfDay(applied.to) : undefined,
        department: applied.department === ALL ? "" : (applied.department as Department),
      })
      .then((m) => active && setMetrics(m))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [applied]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setApplied({ from, to, department: departmentInput });
  };

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">ESM 지표</h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label>부서</Label>
          <Select value={departmentInput} onValueChange={setDepartmentInput}>
            <SelectTrigger className="w-36">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>전체</SelectItem>
              {DEPARTMENTS.map((d) => (
                <SelectItem key={d} value={d}>
                  {departmentLabel(d)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label htmlFor="from">시작일</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">종료일</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <Button type="submit">조회</Button>
      </form>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard label="처리 건수" value={loading ? "-" : String(metrics?.requestCount ?? 0)} unit="건" />
        <KpiCard label="평균 처리 시간" value={loading ? "-" : roundOr0(metrics?.avgProcessingMinutes)} unit="분" />
        <KpiCard label="온보딩 완료율" value={loading ? "-" : roundOr0(metrics?.onboardingCompletionRate, 1)} unit="%" />
        <KpiCard label="오프보딩 완료율" value={loading ? "-" : roundOr0(metrics?.offboardingCompletionRate, 1)} unit="%" />
      </div>
    </div>
  );
}
