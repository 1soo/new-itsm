import { type FormEvent, useState } from "react";
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
import { toast } from "@/components/common";
import { infraApi } from "@/features/infra-monitoring/api";
import { METRIC_TYPES, metricTypeLabel, metricTypeUnit } from "@/features/infra-monitoring/status";
import type { MetricType } from "@/features/infra-monitoring/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 인프라 지표 등록(SCR-IOM-001) — 자산(숫자 ID)·지표 항목·값·측정 시각(선택, 미입력 시 BE 기본값)
 * 입력 후 등록. 임계치 초과로 알림이 생성되면 토스트로 안내한다.
 */
export function InfraMetricRegisterPage() {
  const { t } = useTranslation("infra-monitoring");
  const [assetId, setAssetId] = useState("");
  const [metricType, setMetricType] = useState<MetricType | "">("");
  const [value, setValue] = useState("");
  const [measuredAt, setMeasuredAt] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!assetId || !metricType || !value) {
      setError(t("infraMetricRegister.requiredError", { defaultValue: "자산·지표 항목·값은 필수입니다." }));
      return;
    }
    setSubmitting(true);
    try {
      const created = await infraApi.registerMetric({
        assetId: Number(assetId),
        metricType,
        value: Number(value),
        measuredAt: measuredAt ? new Date(measuredAt).toISOString() : undefined,
      });
      toast.success(t("infraMetricRegister.success", { defaultValue: "지표가 등록되었습니다" }));
      if (created.alertGenerated) {
        toast.error(t("infraMetricRegister.alertGenerated", { defaultValue: "임계치 초과 알림이 생성되었습니다" }));
      }
      setValue("");
      setMeasuredAt("");
    } catch (err) {
      setError(extractErrorMessage(err, t("infraMetricRegister.failed", { defaultValue: "등록에 실패했습니다." })));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold text-foreground">{t("infraMetricRegister.title", { defaultValue: "인프라 지표 등록" })}</h1>
      <Card>
        <CardHeader>
          <CardTitle>{t("infraMetricRegister.cardTitle", { defaultValue: "지표 입력" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="assetId">{t("infraMetricRegister.assetIdLabel", { defaultValue: "자산 ID" })}</Label>
              <Input
                id="assetId"
                type="number"
                value={assetId}
                onChange={(e) => setAssetId(e.target.value)}
                aria-invalid={!!error && !assetId}
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>{t("infraMetricRegister.metricTypeLabel", { defaultValue: "지표 항목" })}</Label>
                <Select value={metricType} onValueChange={(v) => setMetricType(v as MetricType)}>
                  <SelectTrigger aria-invalid={!!error && !metricType}>
                    <SelectValue placeholder={t("infraMetricRegister.metricTypePlaceholder", { defaultValue: "지표 항목 선택" })} />
                  </SelectTrigger>
                  <SelectContent>
                    {METRIC_TYPES.map((ty) => (
                      <SelectItem key={ty} value={ty}>{metricTypeLabel(t, ty)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="value">
                  {t("infraMetricRegister.valueLabel", { defaultValue: "값" })}
                  {metricType ? ` (${metricTypeUnit(metricType)})` : ""}
                </Label>
                <Input
                  id="value"
                  type="number"
                  value={value}
                  onChange={(e) => setValue(e.target.value)}
                  aria-invalid={!!error && !value}
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="measuredAt">{t("infraMetricRegister.measuredAtLabel", { defaultValue: "측정 시각 (선택, 미입력 시 현재 시각)" })}</Label>
              <Input
                id="measuredAt"
                type="datetime-local"
                value={measuredAt}
                onChange={(e) => setMeasuredAt(e.target.value)}
              />
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">{error}</p>
            ) : null}

            <div className="flex justify-end">
              <Button type="submit" loading={submitting}>{t("infraMetricRegister.submitButton", { defaultValue: "등록" })}</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
