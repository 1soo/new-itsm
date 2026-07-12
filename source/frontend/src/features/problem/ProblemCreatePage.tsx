import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PriorityBadge, toast } from "@/components/common";
import { problemApi } from "@/features/problem/api";
import { LEVELS, ORIGINS, computePriority, levelLabel, originLabel } from "@/features/problem/status";
import type { Level, Origin } from "@/features/problem/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 문제 등록(SCR-PRB-002) — 요약(필수)·설명·출처·조사사유·영향도·긴급도·구성요소.
 * 영향도/긴급도 입력 시 우선순위 실시간 미리보기(둘 중 하나라도 없으면 미산정). 성공 시 상세 이동.
 */
export function ProblemCreatePage() {
  const { t } = useTranslation("problem");
  const navigate = useNavigate();
  const [summary, setSummary] = useState("");
  const [description, setDescription] = useState("");
  const [origin, setOrigin] = useState<Origin | "">("");
  const [investigationReason, setInvestigationReason] = useState("");
  const [impact, setImpact] = useState<Level | "">("");
  const [urgency, setUrgency] = useState<Level | "">("");
  const [component, setComponent] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const previewPriority = computePriority(impact, urgency);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!summary.trim()) {
      setError(t("problemCreate.summaryRequiredError", { defaultValue: "요약은 필수입니다." }));
      return;
    }
    setSubmitting(true);
    try {
      const created = await problemApi.create({
        summary: summary.trim(),
        description: description.trim() || undefined,
        origin: origin || undefined,
        investigationReason: investigationReason.trim() || undefined,
        impact: impact || undefined,
        urgency: urgency || undefined,
        component: component.trim() || undefined,
      });
      toast.success(
        t("problemCreate.success", {
          ticketKey: created.ticketKey,
          defaultValue: `문제가 등록되었습니다 (${created.ticketKey})`,
        }),
      );
      navigate(`/problems/${created.id}`);
    } catch (err) {
      setError(extractErrorMessage(err, t("problemCreate.failed", { defaultValue: "등록에 실패했습니다." })));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold text-foreground">
        {t("problemCreate.title", { defaultValue: "문제 등록" })}
      </h1>
      <Card>
        <CardHeader>
          <CardTitle>{t("problemCreate.cardTitle", { defaultValue: "새 문제" })}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="summary">{t("problemCreate.summary", { defaultValue: "요약" })}</Label>
              <Input id="summary" value={summary} onChange={(e) => setSummary(e.target.value)} aria-invalid={!!error && !summary.trim()} required />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="description">{t("problemCreate.description", { defaultValue: "설명" })}</Label>
              <Textarea id="description" value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>{t("problemList.columnOrigin", { defaultValue: "출처" })}</Label>
                <Select value={origin} onValueChange={(v) => setOrigin(v as Origin)}>
                  <SelectTrigger><SelectValue placeholder={t("problemCreate.originPlaceholder", { defaultValue: "출처 선택" })} /></SelectTrigger>
                  <SelectContent>
                    {ORIGINS.map((o) => (
                      <SelectItem key={o} value={o}>{originLabel(t, o)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="component">{t("problemCreate.component", { defaultValue: "구성요소" })}</Label>
                <Input id="component" value={component} onChange={(e) => setComponent(e.target.value)} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="reason">{t("problemCreate.investigationReason", { defaultValue: "조사 사유" })}</Label>
              <Input id="reason" value={investigationReason} onChange={(e) => setInvestigationReason(e.target.value)} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>{t("problemCreate.impact", { defaultValue: "영향도" })}</Label>
                <Select value={impact} onValueChange={(v) => setImpact(v as Level)}>
                  <SelectTrigger><SelectValue placeholder={t("problemCreate.impact", { defaultValue: "영향도" })} /></SelectTrigger>
                  <SelectContent>
                    {LEVELS.map((l) => (
                      <SelectItem key={l} value={l}>{levelLabel(t, l)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>{t("problemCreate.urgency", { defaultValue: "긴급도" })}</Label>
                <Select value={urgency} onValueChange={(v) => setUrgency(v as Level)}>
                  <SelectTrigger><SelectValue placeholder={t("problemCreate.urgency", { defaultValue: "긴급도" })} /></SelectTrigger>
                  <SelectContent>
                    {LEVELS.map((l) => (
                      <SelectItem key={l} value={l}>{levelLabel(t, l)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="flex items-center gap-2 rounded-md border border-border bg-muted/40 px-3 py-2 text-sm">
              <span className="text-muted-foreground">
                {t("problemCreate.priorityPreview", { defaultValue: "우선순위 미리보기" })}
              </span>
              {previewPriority ? (
                <PriorityBadge priority={previewPriority} />
              ) : (
                <span className="text-muted-foreground">
                  {t("problemList.priorityNotCalculated", { defaultValue: "미산정" })}
                </span>
              )}
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">{error}</p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/problems")}>
                {t("problemCreate.cancel", { defaultValue: "취소" })}
              </Button>
              <Button type="submit" loading={submitting}>
                {t("problemCreate.submit", { defaultValue: "등록" })}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
