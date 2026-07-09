import { type FormEvent, useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  DistributionChart,
  type DistributionDatum,
  KpiCard,
  toast,
} from "@/components/common";
import { incidentApi } from "@/features/incident/api";
import type { IncidentMetrics } from "@/features/incident/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 인시던트 지표(SCR-INC-005) — 기간별 건수·평균 MTTR + 심각도 분포 차트.
 */
export function IncidentMetricsPage() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string }>({ from: "", to: "" });
  const [metrics, setMetrics] = useState<IncidentMetrics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    incidentApi
      .metrics({ from: applied.from || undefined, to: applied.to || undefined })
      .then((m) => active && setMetrics(m))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [applied]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setApplied({ from, to });
  };

  const dist: DistributionDatum[] = metrics
    ? [
        { label: "SEV1", value: metrics.severityDistribution?.SEV1 ?? 0, tone: "danger" },
        { label: "SEV2", value: metrics.severityDistribution?.SEV2 ?? 0, tone: "warning" },
        { label: "SEV3", value: metrics.severityDistribution?.SEV3 ?? 0, tone: "info" },
      ]
    : [];

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">인시던트 지표</h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
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

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <KpiCard label="인시던트 건수" value={loading ? "-" : (metrics?.count ?? 0)} unit="건" />
        <KpiCard
          label="평균 MTTR"
          value={loading ? "-" : Math.round(metrics?.avgMttrMinutes ?? 0)}
          unit="분"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">심각도 분포</CardTitle>
        </CardHeader>
        <CardContent>
          <DistributionChart data={dist} ariaLabel="심각도별 인시던트 분포" />
        </CardContent>
      </Card>
    </div>
  );
}
