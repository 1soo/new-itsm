import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Plus, Trash2 } from "lucide-react";

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
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { incidentApi } from "@/features/incident/api";
import type { ActionItem, Postmortem } from "@/features/incident/types";
import { extractErrorMessage, getStatusCode } from "@/lib/apiClient";

/*
 * 포스트모템 편집(SCR-INC-004) — blameless. 요약·타임라인·5 Whys·근본원인(필수)·조치항목.
 * 미작성이면 404 → 빈 폼으로 시작. 근본원인 비어 제출 시 거부(클라이언트 + BE 400).
 */
const EMPTY_ACTION: ActionItem = { description: "", owner: "", dueDate: "", status: "OPEN" };

export function PostmortemPage() {
  const navigate = useNavigate();
  const id = Number(useParams().id);

  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState("");
  const [timeline, setTimeline] = useState("");
  const [fiveWhys, setFiveWhys] = useState<string[]>([""]);
  const [rootCause, setRootCause] = useState("");
  const [actionItems, setActionItems] = useState<ActionItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let active = true;
    incidentApi
      .getPostmortem(id)
      .then((pm) => {
        if (!active) return;
        setSummary(pm.summary ?? "");
        setTimeline(pm.timeline ?? "");
        setFiveWhys(pm.fiveWhys?.length ? pm.fiveWhys : [""]);
        setRootCause(pm.rootCause ?? "");
        setActionItems(pm.actionItems ?? []);
      })
      .catch((err) => {
        // 404 = 미작성이므로 빈 폼 유지, 그 외만 토스트
        if (getStatusCode(err) !== 404) toast.error(extractErrorMessage(err));
      })
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [id]);

  const setWhy = (i: number, v: string) => setFiveWhys((w) => w.map((x, idx) => (idx === i ? v : x)));
  const addWhy = () => setFiveWhys((w) => [...w, ""]);
  const removeWhy = (i: number) => setFiveWhys((w) => (w.length <= 1 ? w : w.filter((_, idx) => idx !== i)));

  const setAction = (i: number, patch: Partial<ActionItem>) =>
    setActionItems((a) => a.map((x, idx) => (idx === i ? { ...x, ...patch } : x)));
  const addAction = () => setActionItems((a) => [...a, { ...EMPTY_ACTION }]);
  const removeAction = (i: number) => setActionItems((a) => a.filter((_, idx) => idx !== i));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!rootCause.trim()) {
      setError("근본 원인은 필수입니다.");
      return;
    }
    const payload: Postmortem = {
      summary: summary.trim() || undefined,
      timeline: timeline.trim() || undefined,
      fiveWhys: fiveWhys.map((w) => w.trim()).filter(Boolean),
      rootCause: rootCause.trim(),
      actionItems: actionItems
        .filter((a) => a.description.trim())
        .map((a) => ({ ...a, dueDate: a.dueDate || undefined })),
    };
    setSaving(true);
    try {
      await incidentApi.savePostmortem(id, payload);
      toast.success("포스트모템이 저장되었습니다");
      navigate(`/incidents/${id}`);
    } catch (err) {
      setError(extractErrorMessage(err, "저장에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <FullscreenLoader />;

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-foreground">포스트모템</h1>
        <Button variant="outline" onClick={() => navigate(`/incidents/${id}`)}>인시던트로</Button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        <Card>
          <CardHeader><CardTitle className="text-base">개요</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="s">요약</Label>
              <Input id="s" value={summary} onChange={(e) => setSummary(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="t">타임라인 요약</Label>
              <Input id="t" value={timeline} onChange={(e) => setTimeline(e.target.value)} />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">5 Whys</CardTitle></CardHeader>
          <CardContent className="space-y-2">
            {fiveWhys.map((w, i) => (
              <div key={i} className="flex items-center gap-2">
                <span className="w-8 shrink-0 text-sm text-muted-foreground">#{i + 1}</span>
                <Input value={w} onChange={(e) => setWhy(i, e.target.value)} placeholder={`왜? ${i + 1}`} />
                <Button type="button" variant="ghost" size="icon" aria-label="삭제" onClick={() => removeWhy(i)}>
                  <Trash2 className="text-destructive" />
                </Button>
              </div>
            ))}
            <Button type="button" variant="outline" size="sm" onClick={addWhy}><Plus /> Why 추가</Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">근본 원인</CardTitle></CardHeader>
          <CardContent className="space-y-1.5">
            <Input value={rootCause} onChange={(e) => setRootCause(e.target.value)} aria-invalid={!!error && !rootCause.trim()} required placeholder="근본 원인(필수)" />
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">조치 항목</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {actionItems.length === 0 ? (
              <p className="text-sm text-muted-foreground">조치 항목이 없습니다.</p>
            ) : (
              actionItems.map((a, i) => (
                <div key={i} className="grid gap-2 rounded-md border border-border p-3 sm:grid-cols-[1fr_8rem_9rem_7rem_auto]">
                  <Input value={a.description} onChange={(e) => setAction(i, { description: e.target.value })} placeholder="조치 내용" />
                  <Input value={a.owner} onChange={(e) => setAction(i, { owner: e.target.value })} placeholder="담당" />
                  <Input type="date" value={a.dueDate ?? ""} onChange={(e) => setAction(i, { dueDate: e.target.value })} />
                  <Select value={a.status} onValueChange={(v) => setAction(i, { status: v as ActionItem["status"] })}>
                    <SelectTrigger><SelectValue /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="OPEN">진행</SelectItem>
                      <SelectItem value="DONE">완료</SelectItem>
                    </SelectContent>
                  </Select>
                  <Button type="button" variant="ghost" size="icon" aria-label="삭제" onClick={() => removeAction(i)}>
                    <Trash2 className="text-destructive" />
                  </Button>
                </div>
              ))
            )}
            <Button type="button" variant="outline" size="sm" onClick={addAction}><Plus /> 조치 항목 추가</Button>
          </CardContent>
        </Card>

        {error ? <p role="alert" className="text-sm text-danger">{error}</p> : null}

        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" onClick={() => navigate(`/incidents/${id}`)}>취소</Button>
          <Button type="submit" loading={saving}>저장</Button>
        </div>
      </form>
    </div>
  );
}
