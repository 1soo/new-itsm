import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge, toast, TrendChart, type TrendPoint } from "@/components/common";
import { infraApi } from "@/features/infra-monitoring/api";
import { formatDateTime } from "@/features/infra-monitoring/format";
import { METRIC_TYPES, metricTypeLabel, metricTypeUnit, slaMetTone } from "@/features/infra-monitoring/status";
import type { MetricType, UptimeStatus } from "@/features/infra-monitoring/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 지표 대시보드(SCR-IOM-002) — 자산·기간 선택 후 지표 항목 시계열 조회 + SLA(가동률 목표) 대비
 * 실제 가동률 카드. 시계열 차트는 신규 라이브러리 없이 기존 SVG 라인 차트(TrendChart)를 재사용한다.
 */
export function InfraMetricDashboardPage() {
  const { t } = useTranslation("infra-monitoring");
  const [assetId, setAssetId] = useState("");
  const [metricType, setMetricType] = useState<MetricType>("UPTIME");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ assetId: string; metricType: MetricType; from: string; to: string } | null>(null);

  const [points, setPoints] = useState<TrendPoint[]>([]);
  const [uptime, setUptime] = useState<UptimeStatus | null>(null);
  const [loading, setLoading] = useState(false);
  const [targetInput, setTargetInput] = useState("");
  const [savingTarget, setSavingTarget] = useState(false);

  useEffect(() => {
    if (!applied || !applied.assetId) return;
    let active = true;
    setLoading(true);
    const id = Number(applied.assetId);
    Promise.all([
      infraApi.listMetrics({
        assetId: id,
        metricType: applied.metricType,
        from: applied.from ? new Date(`${applied.from}T00:00:00`).toISOString() : undefined,
        to: applied.to ? new Date(`${applied.to}T23:59:59`).toISOString() : undefined,
      }),
      infraApi.getUptimeStatus(id, {
        from: applied.from ? new Date(`${applied.from}T00:00:00`).toISOString() : undefined,
        to: applied.to ? new Date(`${applied.to}T23:59:59`).toISOString() : undefined,
      }),
    ])
      .then(([metrics, status]) => {
        if (!active) return;
        setPoints(metrics.map((m) => ({ label: formatDateTime(m.measuredAt), value: m.value })));
        setUptime(status);
        setTargetInput(status.targetPercentage != null ? String(status.targetPercentage) : "");
      })
      .catch((err) => active && toast.error(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [applied]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    if (!assetId) return;
    setApplied({ assetId, metricType, from, to });
  };

  const handleTargetSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!applied || !targetInput) return;
    setSavingTarget(true);
    try {
      const id = Number(applied.assetId);
      await infraApi.setUptimeTarget(id, { targetPercentage: Number(targetInput) });
      const status = await infraApi.getUptimeStatus(id, {
        from: applied.from ? new Date(`${applied.from}T00:00:00`).toISOString() : undefined,
        to: applied.to ? new Date(`${applied.to}T23:59:59`).toISOString() : undefined,
      });
      setUptime(status);
      toast.success(t("infraMetricDashboard.targetSaveSuccess", { defaultValue: "가동률 목표가 설정되었습니다" }));
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSavingTarget(false);
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">{t("infraMetricDashboard.title", { defaultValue: "지표 대시보드" })}</h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label htmlFor="assetId">{t("infraMetricDashboard.assetIdLabel", { defaultValue: "자산 ID" })}</Label>
          <Input id="assetId" type="number" value={assetId} onChange={(e) => setAssetId(e.target.value)} className="w-32" />
        </div>
        <div className="space-y-1">
          <Label>{t("infraMetricDashboard.metricTypeLabel", { defaultValue: "지표 항목" })}</Label>
          <Select value={metricType} onValueChange={(v) => setMetricType(v as MetricType)}>
            <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
            <SelectContent>
              {METRIC_TYPES.map((ty) => (
                <SelectItem key={ty} value={ty}>{metricTypeLabel(t, ty)}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label htmlFor="from">{t("infraMetricDashboard.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">{t("infraMetricDashboard.filterTo", { defaultValue: "종료일" })}</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <Button type="submit" disabled={!assetId}>{t("infraMetricDashboard.searchButton", { defaultValue: "조회" })}</Button>
      </form>

      {applied ? (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-base">
                {t("infraMetricDashboard.timeSeriesTitle", {
                  metric: metricTypeLabel(t, applied.metricType),
                  defaultValue: `${metricTypeLabel(t, applied.metricType)} 시계열`,
                })}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <TrendChart
                data={points}
                valueFormatter={(v) => `${v}${metricTypeUnit(applied.metricType)}`}
                ariaLabel={t("infraMetricDashboard.timeSeriesAria", {
                  metric: metricTypeLabel(t, applied.metricType),
                  defaultValue: `${metricTypeLabel(t, applied.metricType)} 시계열 차트`,
                })}
              />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">{t("infraMetricDashboard.slaTitle", { defaultValue: "SLA 대비 가동률" })}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">{t("infraMetricDashboard.targetLabel", { defaultValue: "목표" })}</span>
                <span className="text-foreground">
                  {loading ? "-" : uptime?.targetPercentage != null ? `${uptime.targetPercentage}%` : t("infraMetricDashboard.targetNotSet", { defaultValue: "미설정" })}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">{t("infraMetricDashboard.actualLabel", { defaultValue: "실제" })}</span>
                <span className="text-foreground">
                  {loading ? "-" : uptime?.actualPercentage != null ? `${uptime.actualPercentage}%` : "-"}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">{t("infraMetricDashboard.metLabel", { defaultValue: "달성 여부" })}</span>
                <StatusBadge
                  tone={slaMetTone(uptime?.met ?? null)}
                  label={
                    uptime?.targetPercentage == null
                      ? t("infraMetricDashboard.metNotSet", { defaultValue: "목표 미설정" })
                      : uptime.actualPercentage == null
                        ? t("infraMetricDashboard.noActualData", { defaultValue: "가동률 데이터 없음" })
                        : uptime.met
                          ? t("infraMetricDashboard.metAchieved", { defaultValue: "달성" })
                          : t("infraMetricDashboard.metNotAchieved", { defaultValue: "미달성" })
                  }
                />
              </div>
              <form onSubmit={handleTargetSubmit} className="flex items-end gap-2 border-t border-border pt-3">
                <div className="flex-1 space-y-1.5">
                  <Label htmlFor="targetPercentage">{t("infraMetricDashboard.targetInputLabel", { defaultValue: "목표 가동률 설정 (%)" })}</Label>
                  <Input
                    id="targetPercentage"
                    type="number"
                    value={targetInput}
                    onChange={(e) => setTargetInput(e.target.value)}
                  />
                </div>
                <Button type="submit" size="sm" loading={savingTarget} disabled={!targetInput}>
                  {t("infraMetricDashboard.saveButton", { defaultValue: "저장" })}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      ) : null}
    </div>
  );
}
