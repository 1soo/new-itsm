import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KpiCard, toast } from "@/components/common";
import { srmApi } from "@/features/service-request/api";
import type { RequestMetrics } from "@/features/service-request/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 요청 지표 대시보드(SCR-SRM-008) — CSAT·응답/해결시간·SLA 준수율 집계.
 * API-SRM-015는 집계값만 반환(시계열 없음)하므로 KPI 카드로 표시한다. (추이 차트용 시계열은 미제공 — dev-backend 협의)
 */
function roundOr0(n: number | undefined, digits = 0): string {
  if (n == null || Number.isNaN(n)) return "0";
  return n.toFixed(digits);
}

export function MetricsPage() {
  const { t } = useTranslation("service-request");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string }>({ from: "", to: "" });
  const [metrics, setMetrics] = useState<RequestMetrics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    srmApi
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

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("metrics.title", { defaultValue: "요청 지표" })}
      </h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label htmlFor="from">{t("metrics.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">{t("metrics.filterTo", { defaultValue: "종료일" })}</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <Button type="submit">{t("metrics.searchButton", { defaultValue: "조회" })}</Button>
      </form>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          label={t("metrics.csatAvg", { defaultValue: "CSAT 평균" })}
          value={loading ? "-" : roundOr0(metrics?.csatAvg, 1)}
          unit="/ 5"
        />
        <KpiCard
          label={t("metrics.avgResponseTime", { defaultValue: "평균 응답 시간" })}
          value={loading ? "-" : roundOr0(metrics?.avgResponseMinutes)}
          unit={t("metrics.minutesUnit", { defaultValue: "분" })}
        />
        <KpiCard
          label={t("metrics.avgResolveTime", { defaultValue: "평균 해결 시간" })}
          value={loading ? "-" : roundOr0(metrics?.avgResolveMinutes)}
          unit={t("metrics.minutesUnit", { defaultValue: "분" })}
        />
        <KpiCard
          label={t("metrics.slaComplianceRate", { defaultValue: "SLA 준수율" })}
          value={loading ? "-" : roundOr0(metrics?.slaComplianceRate, 1)}
          unit="%"
        />
      </div>
    </div>
  );
}
