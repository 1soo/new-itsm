import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KpiCard, toast } from "@/components/common";
import { changeApi } from "@/features/change/api";
import type { ChangeMetrics } from "@/features/change/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 변경 지표 대시보드(SCR-CHG-006) — 기간별 성공률·실패율·긴급 변경 비율.
 * REQ-CHG-010은 집계·조회만 요구하므로 시계열 추이 차트는 범위 제외.
 */
export function ChangeMetricsPage() {
  const { t } = useTranslation("change");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string }>({ from: "", to: "" });
  const [metrics, setMetrics] = useState<ChangeMetrics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    changeApi
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

  const pct = (v: number | undefined) => (loading ? "-" : Math.round(v ?? 0));

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("changeMetrics.title", { defaultValue: "변경 지표" })}
      </h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label htmlFor="from">{t("changeMetrics.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">{t("changeMetrics.filterTo", { defaultValue: "종료일" })}</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <Button type="submit">{t("changeMetrics.searchButton", { defaultValue: "조회" })}</Button>
      </form>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <KpiCard label={t("changeMetrics.successRate", { defaultValue: "성공률" })} value={pct(metrics?.successRate)} unit="%" />
        <KpiCard label={t("changeMetrics.failureRate", { defaultValue: "실패율" })} value={pct(metrics?.failureRate)} unit="%" />
        <KpiCard label={t("changeMetrics.emergencyRate", { defaultValue: "긴급 변경 비율" })} value={pct(metrics?.emergencyRate)} unit="%" />
      </div>
    </div>
  );
}
