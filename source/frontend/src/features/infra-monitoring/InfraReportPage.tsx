import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KpiCard, toast } from "@/components/common";
import { infraApi } from "@/features/infra-monitoring/api";
import type { InfraReport } from "@/features/infra-monitoring/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 인프라 지표 리포팅(SCR-IOM-005) — 기간·자산 필터 + KPI(평균 가동률·CPU·메모리·응답시간·
 * 용량 활용률) 집계 조회. 데이터 없으면 0으로 표시(다른 도메인 리포팅 컨벤션과 동일).
 */
export function InfraReportPage() {
  const { t } = useTranslation("infra-monitoring");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [assetId, setAssetId] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string; assetId: string }>({
    from: "",
    to: "",
    assetId: "",
  });
  const [report, setReport] = useState<InfraReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    infraApi
      .getReport({
        from: applied.from || undefined,
        to: applied.to || undefined,
        assetId: applied.assetId ? Number(applied.assetId) : undefined,
      })
      .then((r) => active && setReport(r))
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [applied]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    setApplied({ from, to, assetId });
  };

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">{t("infraReport.title", { defaultValue: "인프라 지표 리포팅" })}</h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label htmlFor="from">{t("infraReport.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">{t("infraReport.filterTo", { defaultValue: "종료일" })}</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="assetId">{t("infraReport.assetIdLabel", { defaultValue: "자산 ID (선택)" })}</Label>
          <Input id="assetId" type="number" value={assetId} onChange={(e) => setAssetId(e.target.value)} className="w-32" />
        </div>
        <Button type="submit">{t("infraReport.searchButton", { defaultValue: "조회" })}</Button>
      </form>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <KpiCard label={t("infraReport.avgUptime", { defaultValue: "평균 가동률" })} value={loading ? "-" : Math.round(report?.avgUptime ?? 0)} unit="%" />
        <KpiCard label={t("infraReport.avgCpu", { defaultValue: "평균 CPU" })} value={loading ? "-" : Math.round(report?.avgCpu ?? 0)} unit="%" />
        <KpiCard label={t("infraReport.avgMemory", { defaultValue: "평균 메모리" })} value={loading ? "-" : Math.round(report?.avgMemory ?? 0)} unit="%" />
        <KpiCard label={t("infraReport.avgResponseTime", { defaultValue: "평균 응답시간" })} value={loading ? "-" : Math.round(report?.avgResponseTime ?? 0)} unit="ms" />
        <KpiCard label={t("infraReport.avgCapacityUtilization", { defaultValue: "평균 용량 활용률" })} value={loading ? "-" : Math.round((report?.avgCapacityUtilization ?? 0) * 100)} unit="%" />
      </div>
    </div>
  );
}
