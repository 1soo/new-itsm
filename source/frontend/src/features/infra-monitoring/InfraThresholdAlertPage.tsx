import { type FormEvent, type ReactNode, useEffect, useState } from "react";
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
import { type Column, DataTable, StatusBadge, toast } from "@/components/common";
import { infraApi } from "@/features/infra-monitoring/api";
import { formatDateTime } from "@/features/infra-monitoring/format";
import { METRIC_TYPES, metricTypeLabel, thresholdTypeLabel } from "@/features/infra-monitoring/status";
import type { MetricAlert, MetricType } from "@/features/infra-monitoring/types";
import { extractErrorMessage } from "@/lib/apiClient";

const ALL = "ALL";

/*
 * 임계치 설정·알림 목록(SCR-IOM-003) — 지표 항목별(전역) 상한/하한 설정 폼(항목 선택 시
 * 현재 설정값 자동 반영) + 초과 알림 목록(자산·지표·초과값·발생시각, 확인 처리).
 * 임계치가 설정되지 않은 항목은 알림이 생성되지 않음을 폼에 안내한다.
 */
export function InfraThresholdAlertPage() {
  const { t } = useTranslation("infra-monitoring");
  const [metricType, setMetricType] = useState<MetricType>("UPTIME");
  const [upperLimit, setUpperLimit] = useState("");
  const [lowerLimit, setLowerLimit] = useState("");
  const [savingThreshold, setSavingThreshold] = useState(false);

  const [assetIdFilter, setAssetIdFilter] = useState("");
  const [acknowledgedFilter, setAcknowledgedFilter] = useState(ALL);
  const [applied, setApplied] = useState<{ assetId: string; acknowledged: string }>({
    assetId: "",
    acknowledged: ALL,
  });
  const [alerts, setAlerts] = useState<MetricAlert[]>([]);
  const [loadingAlerts, setLoadingAlerts] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);

  useEffect(() => {
    let active = true;
    infraApi
      .listThresholds()
      .then((list) => {
        if (!active) return;
        const found = list.find((t) => t.metricType === metricType);
        setUpperLimit(found?.upperLimit != null ? String(found.upperLimit) : "");
        setLowerLimit(found?.lowerLimit != null ? String(found.lowerLimit) : "");
      })
      .catch((err) => active && toast.error(extractErrorMessage(err)));
    return () => {
      active = false;
    };
  }, [metricType]);

  const loadAlerts = () => {
    setLoadingAlerts(true);
    infraApi
      .listAlerts({
        assetId: applied.assetId ? Number(applied.assetId) : undefined,
        acknowledged: applied.acknowledged === ALL ? undefined : applied.acknowledged === "true",
      })
      .then(setAlerts)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoadingAlerts(false));
  };

  useEffect(loadAlerts, [applied]);

  const handleThresholdSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSavingThreshold(true);
    try {
      await infraApi.setThreshold(metricType, {
        upperLimit: upperLimit ? Number(upperLimit) : null,
        lowerLimit: lowerLimit ? Number(lowerLimit) : null,
      });
      toast.success(t("infraThresholdAlert.thresholdSaveSuccess", { defaultValue: "임계치가 설정되었습니다" }));
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSavingThreshold(false);
    }
  };

  const handleAlertSearch = (e: FormEvent) => {
    e.preventDefault();
    setApplied({ assetId: assetIdFilter, acknowledged: acknowledgedFilter });
  };

  const handleAcknowledge = async (id: number) => {
    setBusyId(id);
    try {
      await infraApi.acknowledgeAlert(id);
      toast.success(t("infraThresholdAlert.acknowledgeSuccess", { defaultValue: "알림을 확인 처리했습니다" }));
      loadAlerts();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  };

  const dim = (a: MetricAlert, node: ReactNode) => (
    <span className={a.acknowledged ? "opacity-50" : undefined}>{node}</span>
  );

  const columns: Column<MetricAlert>[] = [
    { header: t("infraThresholdAlert.columnAsset", { defaultValue: "자산" }), cell: (a) => dim(a, a.assetKey) },
    { header: t("infraThresholdAlert.columnMetric", { defaultValue: "지표" }), cell: (a) => dim(a, metricTypeLabel(t, a.metricType)) },
    { header: t("infraThresholdAlert.columnExceedValue", { defaultValue: "초과값" }), cell: (a) => dim(a, a.value) },
    { header: t("infraThresholdAlert.columnExceedType", { defaultValue: "초과 유형" }), cell: (a) => dim(a, thresholdTypeLabel(t, a.thresholdType)) },
    { header: t("infraThresholdAlert.columnOccurredAt", { defaultValue: "발생시각" }), cell: (a) => dim(a, formatDateTime(a.occurredAt)) },
    {
      header: t("infraThresholdAlert.columnAcknowledged", { defaultValue: "확인 여부" }),
      cell: (a) =>
        a.acknowledged ? (
          <StatusBadge tone="muted" label={t("infraThresholdAlert.acknowledgedLabel", { defaultValue: "확인됨" })} />
        ) : (
          <StatusBadge tone="danger" label={t("infraThresholdAlert.unacknowledgedLabel", { defaultValue: "미확인" })} />
        ),
    },
    {
      header: "",
      cell: (a) =>
        a.acknowledged ? null : (
          <Button size="sm" loading={busyId === a.id} onClick={() => handleAcknowledge(a.id)}>
            {t("infraThresholdAlert.acknowledgeButton", { defaultValue: "확인 처리" })}
          </Button>
        ),
    },
  ];

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-foreground">{t("infraThresholdAlert.title", { defaultValue: "임계치 설정·알림 목록" })}</h1>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("infraThresholdAlert.thresholdCardTitle", { defaultValue: "지표 항목별 임계치 설정" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleThresholdSubmit} className="flex flex-wrap items-end gap-3">
            <div className="space-y-1.5">
              <Label>{t("infraThresholdAlert.metricTypeLabel", { defaultValue: "지표 항목" })}</Label>
              <Select value={metricType} onValueChange={(v) => setMetricType(v as MetricType)}>
                <SelectTrigger className="w-40"><SelectValue /></SelectTrigger>
                <SelectContent>
                  {METRIC_TYPES.map((ty) => (
                    <SelectItem key={ty} value={ty}>{metricTypeLabel(t, ty)}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="upper">{t("infraThresholdAlert.upperLabel", { defaultValue: "상한" })}</Label>
              <Input id="upper" type="number" value={upperLimit} onChange={(e) => setUpperLimit(e.target.value)} className="w-32" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="lower">{t("infraThresholdAlert.lowerLabel", { defaultValue: "하한" })}</Label>
              <Input id="lower" type="number" value={lowerLimit} onChange={(e) => setLowerLimit(e.target.value)} className="w-32" />
            </div>
            <Button type="submit" loading={savingThreshold}>{t("infraThresholdAlert.saveButton", { defaultValue: "저장" })}</Button>
          </form>
          <p className="mt-2 text-xs text-muted-foreground">
            {t("infraThresholdAlert.thresholdHint", {
              defaultValue: "상한·하한을 비워두면 해당 조건은 임계치 비교에서 제외됩니다. 항목 전체가 미설정이면 알림이 생성되지 않습니다.",
            })}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("infraThresholdAlert.alertsCardTitle", { defaultValue: "임계치 초과 알림" })}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <form onSubmit={handleAlertSearch} className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="assetIdFilter">{t("infraThresholdAlert.assetIdFilterLabel", { defaultValue: "자산 ID" })}</Label>
              <Input id="assetIdFilter" type="number" value={assetIdFilter} onChange={(e) => setAssetIdFilter(e.target.value)} className="w-32" />
            </div>
            <div className="space-y-1">
              <Label>{t("infraThresholdAlert.acknowledgedFilterLabel", { defaultValue: "확인 여부" })}</Label>
              <Select value={acknowledgedFilter} onValueChange={setAcknowledgedFilter}>
                <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL}>{t("infraThresholdAlert.filterAll", { defaultValue: "전체" })}</SelectItem>
                  <SelectItem value="false">{t("infraThresholdAlert.filterUnacknowledged", { defaultValue: "미확인" })}</SelectItem>
                  <SelectItem value="true">{t("infraThresholdAlert.filterAcknowledged", { defaultValue: "확인됨" })}</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button type="submit">{t("infraThresholdAlert.searchButton", { defaultValue: "검색" })}</Button>
          </form>
          <DataTable
            columns={columns}
            data={alerts}
            rowKey={(a) => a.id}
            loading={loadingAlerts}
            emptyTitle={t("infraThresholdAlert.emptyTitle", { defaultValue: "알림이 없습니다" })}
            emptyDescription={t("infraThresholdAlert.emptyDescription", { defaultValue: "조건에 맞는 임계치 초과 알림이 없습니다." })}
          />
        </CardContent>
      </Card>
    </div>
  );
}
