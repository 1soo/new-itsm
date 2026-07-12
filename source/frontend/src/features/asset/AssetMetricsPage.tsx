import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DistributionChart, type DistributionDatum, KpiCard, toast } from "@/components/common";
import { assetApi } from "@/features/asset/api";
import { typeLabel } from "@/features/asset/status";
import type { AssetMetrics } from "@/features/asset/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 자산 지표 대시보드(SCR-ITAM-005) — 활용률·만료 임박 건수 + 유형별 분포 차트.
 */
export function AssetMetricsPage() {
  const { t } = useTranslation("asset");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string }>({ from: "", to: "" });
  const [metrics, setMetrics] = useState<AssetMetrics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    assetApi
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
        { label: typeLabel(t, "HARDWARE"), value: metrics.typeDistribution?.HARDWARE ?? 0, tone: "info" },
        { label: typeLabel(t, "SOFTWARE"), value: metrics.typeDistribution?.SOFTWARE ?? 0, tone: "muted" },
        { label: typeLabel(t, "CLOUD"), value: metrics.typeDistribution?.CLOUD ?? 0, tone: "info" },
      ]
    : [];

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("assetMetrics.title", { defaultValue: "자산 지표" })}
      </h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label htmlFor="from">{t("assetMetrics.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">{t("assetMetrics.filterTo", { defaultValue: "종료일" })}</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <Button type="submit">{t("assetMetrics.searchButton", { defaultValue: "조회" })}</Button>
      </form>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <KpiCard
          label={t("assetMetrics.utilizationRate", { defaultValue: "활용률" })}
          value={loading ? "-" : Math.round(metrics?.utilizationRate ?? 0)}
          unit="%"
        />
        <KpiCard
          label={t("assetMetrics.expiringCount", { defaultValue: "만료 임박" })}
          value={loading ? "-" : (metrics?.expiringCount ?? 0)}
          unit={t("assetMetrics.countUnit", { defaultValue: "건" })}
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("assetMetrics.typeDistributionTitle", { defaultValue: "유형별 분포" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <DistributionChart data={dist} ariaLabel={t("assetMetrics.typeDistributionAria", { defaultValue: "자산 유형별 분포" })} />
        </CardContent>
      </Card>
    </div>
  );
}
