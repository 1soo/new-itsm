import { type FormEvent, useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { type Column, DataTable, KpiCard, StatusBadge, toast } from "@/components/common";
import { complianceApi } from "@/features/compliance/api";
import { complianceStatusLabel, complianceStatusTone } from "@/features/compliance/status";
import type { ComplianceMetrics, RequirementSummary } from "@/features/compliance/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 준수 현황 대시보드(SCR-COMP-004) — 기간 필터 + KPI(준수율·미해결 시정조치 건수) +
 * 요구사항별 상태 표. 표 데이터는 metrics API가 집계값만 제공해 목록 API(전체 조회)로 채운다.
 */
const TABLE_SIZE = 100;

function toStartOfDay(date: string): string {
  return new Date(`${date}T00:00:00`).toISOString();
}
function toEndOfDay(date: string): string {
  return new Date(`${date}T23:59:59`).toISOString();
}

export function ComplianceMetricsPage() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string }>({ from: "", to: "" });
  const [metrics, setMetrics] = useState<ComplianceMetrics | null>(null);
  const [requirements, setRequirements] = useState<RequirementSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    Promise.all([
      complianceApi.metrics({
        from: applied.from ? toStartOfDay(applied.from) : undefined,
        to: applied.to ? toEndOfDay(applied.to) : undefined,
      }),
      complianceApi.list({ page: 0, size: TABLE_SIZE }),
    ])
      .then(([m, r]) => {
        if (!active) return;
        setMetrics(m);
        setRequirements(r.content);
      })
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

  const columns: Column<RequirementSummary>[] = [
    { header: "요구사항", cell: (r) => <span className="line-clamp-1">{r.name}</span> },
    { header: "책임자", cell: (r) => r.owner ?? "미지정" },
    {
      header: "준수 상태",
      cell: (r) => (
        <StatusBadge tone={complianceStatusTone(r.complianceStatus)} label={complianceStatusLabel(r.complianceStatus)} />
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">준수 현황</h1>

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
        <KpiCard
          label="준수율"
          value={loading ? "-" : Math.round((metrics?.complianceRate ?? 0) * 100)}
          unit="%"
        />
        <KpiCard
          label="미해결 시정조치 건수"
          value={loading ? "-" : (metrics?.openCorrectiveActionCount ?? 0)}
          unit="건"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">요구사항별 상태</CardTitle>
        </CardHeader>
        <CardContent>
          <DataTable
            columns={columns}
            data={requirements}
            rowKey={(r) => r.id}
            loading={loading}
            emptyTitle="요구사항이 없습니다"
            emptyDescription="등록된 컴플라이언스 요구사항이 없습니다."
          />
        </CardContent>
      </Card>
    </div>
  );
}
