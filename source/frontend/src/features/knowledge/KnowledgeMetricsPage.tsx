import { type FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KpiCard, toast } from "@/components/common";
import { knowledgeApi } from "@/features/knowledge/api";
import type { KnowledgeMetrics } from "@/features/knowledge/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 지식 지표 대시보드(SCR-KM-005) — 기간별 사용량·무결과 검색·유용성·차단율 + 무결과 키워드 랭킹.
 */
export function KnowledgeMetricsPage() {
  const { t } = useTranslation("knowledge");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [applied, setApplied] = useState<{ from: string; to: string }>({ from: "", to: "" });
  const [metrics, setMetrics] = useState<KnowledgeMetrics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    knowledgeApi
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
        {t("knowledgeMetrics.title", { defaultValue: "지식 지표" })}
      </h1>

      <form onSubmit={handleSearch} className="flex flex-wrap items-end gap-2 rounded-lg border border-border bg-card p-3">
        <div className="space-y-1">
          <Label htmlFor="from">{t("knowledgeMetrics.filterFrom", { defaultValue: "시작일" })}</Label>
          <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="to">{t("knowledgeMetrics.filterTo", { defaultValue: "종료일" })}</Label>
          <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <Button type="submit">{t("knowledgeMetrics.searchButton", { defaultValue: "조회" })}</Button>
      </form>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          label={t("knowledgeMetrics.usageCount", { defaultValue: "사용량" })}
          value={loading ? "-" : (metrics?.usageCount ?? 0)}
          unit={t("knowledgeMetrics.countUnit", { defaultValue: "건" })}
        />
        <KpiCard
          label={t("knowledgeMetrics.noResultSearchCount", { defaultValue: "무결과 검색" })}
          value={loading ? "-" : (metrics?.noResultSearchCount ?? 0)}
          unit={t("knowledgeMetrics.countUnit", { defaultValue: "건" })}
        />
        <KpiCard
          label={t("knowledgeMetrics.helpfulRate", { defaultValue: "유용성" })}
          value={loading ? "-" : Math.round(metrics?.helpfulRate ?? 0)}
          unit="%"
        />
        <KpiCard
          label={t("knowledgeMetrics.deflectionRate", { defaultValue: "티켓 차단율" })}
          value={loading ? "-" : Math.round(metrics?.deflectionRate ?? 0)}
          unit="%"
        />
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base">{t("knowledgeMetrics.noResultKeywordsTitle", { defaultValue: "무결과 검색 키워드" })}</CardTitle></CardHeader>
        <CardContent>
          {!metrics || metrics.topNoResultKeywords.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t("knowledgeMetrics.noData", { defaultValue: "데이터가 없습니다." })}</p>
          ) : (
            <ol className="space-y-1.5 text-sm">
              {metrics.topNoResultKeywords.map((k, i) => (
                <li key={k} className="flex items-center gap-2">
                  <span className="w-6 text-muted-foreground">#{i + 1}</span>
                  <span className="text-foreground">{k}</span>
                </li>
              ))}
            </ol>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
