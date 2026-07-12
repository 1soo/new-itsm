import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
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
import { incidentApi } from "@/features/incident/api";
import { SEVERITIES } from "@/features/incident/status";
import type { Severity } from "@/features/incident/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 인시던트 등록(SCR-INC-002) — 요약(필수)·심각도(필수)·설명·영향 서비스/제품.
 * 요약·심각도 누락 시 인라인 오류. 성공 시 상세 이동.
 */
export function IncidentCreatePage() {
  const { t } = useTranslation("incident");
  const navigate = useNavigate();
  const [summary, setSummary] = useState("");
  const [description, setDescription] = useState("");
  const [severity, setSeverity] = useState<Severity | "">("");
  const [affectedService, setAffectedService] = useState("");
  const [affectedProduct, setAffectedProduct] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!summary.trim()) {
      setError(t("incidentCreate.summaryRequiredError", { defaultValue: "요약은 필수입니다." }));
      return;
    }
    if (!severity) {
      setError(t("incidentCreate.severityRequiredError", { defaultValue: "심각도는 필수입니다." }));
      return;
    }
    setSubmitting(true);
    try {
      const created = await incidentApi.create({
        summary: summary.trim(),
        description: description.trim() || undefined,
        severity,
        affectedService: affectedService.trim() || undefined,
        affectedProduct: affectedProduct.trim() || undefined,
      });
      toast.success(
        t("incidentCreate.success", {
          ticketKey: created.ticketKey,
          defaultValue: `인시던트가 등록되었습니다 (${created.ticketKey})`,
        }),
      );
      navigate(`/incidents/${created.id}`);
    } catch (err) {
      setError(extractErrorMessage(err, t("incidentCreate.failed", { defaultValue: "등록에 실패했습니다." })));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("incidentCreate.title", { defaultValue: "인시던트 등록" })}
      </h1>
      <Card>
        <CardHeader>
          <CardTitle>{t("incidentCreate.cardTitle", { defaultValue: "새 인시던트" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="summary">{t("incidentCreate.summary", { defaultValue: "요약" })}</Label>
              <Input id="summary" value={summary} onChange={(e) => setSummary(e.target.value)} required />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="description">{t("incidentCreate.description", { defaultValue: "설명" })}</Label>
              <Input id="description" value={description} onChange={(e) => setDescription(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>{t("incidentList.columnSeverity", { defaultValue: "심각도" })}</Label>
              <Select value={severity} onValueChange={(v) => setSeverity(v as Severity)}>
                <SelectTrigger aria-invalid={!!error && !severity}>
                  <SelectValue placeholder={t("incidentCreate.severityPlaceholder", { defaultValue: "심각도 선택" })} />
                </SelectTrigger>
                <SelectContent>
                  {SEVERITIES.map((s) => (
                    <SelectItem key={s} value={s}>{s}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="svc">{t("incidentCreate.affectedService", { defaultValue: "영향 서비스" })}</Label>
                <Input id="svc" value={affectedService} onChange={(e) => setAffectedService(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="prd">{t("incidentCreate.affectedProduct", { defaultValue: "영향 제품" })}</Label>
                <Input id="prd" value={affectedProduct} onChange={(e) => setAffectedProduct(e.target.value)} />
              </div>
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">{error}</p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/incidents")}>
                {t("incidentCreate.cancel", { defaultValue: "취소" })}
              </Button>
              <Button type="submit" loading={submitting}>
                {t("incidentCreate.submit", { defaultValue: "등록" })}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
