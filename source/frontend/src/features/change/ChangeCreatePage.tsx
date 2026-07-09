import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

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
import { toast } from "@/components/common";
import { changeApi } from "@/features/change/api";
import { CHANGE_TYPES, RISKS, riskLabel, typeLabel } from "@/features/change/status";
import type { ChangeTemplate, ChangeType, Risk } from "@/features/change/types";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 변경 요청(RFC) 생성(SCR-CHG-002) — 요약(필수)·변경 유형(필수)·위험도·예상 구현·영향 시스템·
 * 롤백 방법·예정 일정. 표준 변경 선택 시 템플릿 목록 조회, 승인 단계 생략 안내. 성공 시 상세 이동.
 */
export function ChangeCreatePage() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState("");
  const [description, setDescription] = useState("");
  const [type, setType] = useState<ChangeType | "">("");
  const [risk, setRisk] = useState<Risk | "">("");
  const [implementationPlan, setImplementationPlan] = useState("");
  const [affectedSystems, setAffectedSystems] = useState("");
  const [rollbackPlan, setRollbackPlan] = useState("");
  const [scheduledAt, setScheduledAt] = useState("");
  const [templates, setTemplates] = useState<ChangeTemplate[]>([]);
  const [templateId, setTemplateId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (type !== "STANDARD") return;
    changeApi.listTemplates().then(setTemplates).catch((err) => toast.error(extractErrorMessage(err)));
  }, [type]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!summary.trim()) {
      setError("요약은 필수입니다.");
      return;
    }
    if (!type) {
      setError("변경 유형은 필수입니다.");
      return;
    }
    setSubmitting(true);
    try {
      const created = await changeApi.create({
        summary: summary.trim(),
        description: description.trim() || undefined,
        type,
        risk: risk || undefined,
        implementationPlan: implementationPlan.trim() || undefined,
        affectedSystems: affectedSystems.trim()
          ? affectedSystems.split(",").map((s) => s.trim()).filter(Boolean)
          : undefined,
        rollbackPlan: rollbackPlan.trim() || undefined,
        scheduledAt: scheduledAt ? new Date(scheduledAt).toISOString() : undefined,
        templateId: type === "STANDARD" && templateId ? Number(templateId) : undefined,
      });
      toast.success(`변경 요청이 등록되었습니다 (${created.ticketKey})`);
      navigate(`/changes/${created.id}`);
    } catch (err) {
      setError(extractErrorMessage(err, "등록에 실패했습니다."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <h1 className="text-xl font-semibold text-foreground">변경 요청(RFC) 생성</h1>
      <Card>
        <CardHeader>
          <CardTitle>새 변경 요청</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="summary">요약</Label>
              <Input id="summary" value={summary} onChange={(e) => setSummary(e.target.value)} aria-invalid={!!error && !summary.trim()} required />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="description">설명</Label>
              <Textarea id="description" value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>변경 유형</Label>
                <Select value={type} onValueChange={(v) => setType(v as ChangeType)}>
                  <SelectTrigger aria-invalid={!!error && !type}><SelectValue placeholder="유형 선택" /></SelectTrigger>
                  <SelectContent>
                    {CHANGE_TYPES.map((t) => (
                      <SelectItem key={t} value={t}>{typeLabel(t)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>위험도</Label>
                <Select value={risk} onValueChange={(v) => setRisk(v as Risk)}>
                  <SelectTrigger><SelectValue placeholder="위험도 선택" /></SelectTrigger>
                  <SelectContent>
                    {RISKS.map((r) => (
                      <SelectItem key={r} value={r}>{riskLabel(r)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {type === "STANDARD" ? (
              <div className="space-y-1.5">
                <Label>표준 변경 템플릿</Label>
                <Select value={templateId} onValueChange={setTemplateId}>
                  <SelectTrigger><SelectValue placeholder="템플릿 선택" /></SelectTrigger>
                  <SelectContent>
                    {templates.map((t) => (
                      <SelectItem key={t.id} value={String(t.id)}>{t.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">표준 변경은 승인 단계가 자동으로 생략(사전승인)됩니다.</p>
              </div>
            ) : null}

            <div className="space-y-1.5">
              <Label htmlFor="plan">예상 구현 계획</Label>
              <Textarea id="plan" value={implementationPlan} onChange={(e) => setImplementationPlan(e.target.value)} rows={2} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="systems">영향 시스템 (쉼표로 구분)</Label>
              <Input id="systems" value={affectedSystems} onChange={(e) => setAffectedSystems(e.target.value)} placeholder="예: 결제시스템, 회원시스템" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rollback">롤백 방법</Label>
              <Textarea id="rollback" value={rollbackPlan} onChange={(e) => setRollbackPlan(e.target.value)} rows={2} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sched">예정 일정</Label>
              <Input id="sched" type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} />
            </div>

            {error ? (
              <p role="alert" className="text-sm text-danger">{error}</p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate("/changes")}>취소</Button>
              <Button type="submit" loading={submitting}>생성</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
