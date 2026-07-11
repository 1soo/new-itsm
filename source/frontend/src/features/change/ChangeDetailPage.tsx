import { type FormEvent, useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
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
import { ApprovalPanel, StatusBadge, TicketDetailLayout, toast } from "@/components/common";
import type { ApprovalStep } from "@/components/common";
import { FullscreenLoader } from "@/routes/FullscreenLoader";
import { changeApi } from "@/features/change/api";
import {
  fallbackTransitions,
  riskLabel,
  riskTone,
  statusLabel,
  statusTone,
  typeLabel,
  typeTone,
} from "@/features/change/status";
import type { ChangeDetail, LinkTargetType, Outcome } from "@/features/change/types";
import { commonApi } from "@/features/common/api";
import { extractErrorMessage } from "@/lib/apiClient";

/*
 * 변경 상세(SCR-CHG-003) — 6단계 전이(승인 전 구현 전이 UI 차단)·승인경로 배지·승인 이력·
 * 구현 결과 기록·인시던트/문제 연계.
 */
export function ChangeDetailPage() {
  const navigate = useNavigate();
  const id = Number(useParams().id);

  const [detail, setDetail] = useState<ChangeDetail | null>(null);
  const [approvalSteps, setApprovalSteps] = useState<ApprovalStep[]>([]);
  const [approvalCurrentStepNo, setApprovalCurrentStepNo] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);

  const refreshDetail = useCallback(
    (silent: boolean) => {
      if (!silent) setLoading(true);
      return changeApi
        .get(id)
        .then((d) => {
          setDetail(d);
          setNotFound(false);
          if (d.approval.approvalRequestId == null) {
            setApprovalSteps([]);
            setApprovalCurrentStepNo(null);
            return;
          }
          return commonApi.getApproval(d.approval.approvalRequestId).then((a) => {
            setApprovalSteps(a.steps);
            setApprovalCurrentStepNo(a.currentStepNo);
          });
        })
        .catch((err) => {
          if (!silent) {
            toast.error(extractErrorMessage(err));
            setNotFound(true);
          }
        })
        .finally(() => {
          if (!silent) setLoading(false);
        });
    },
    [id],
  );

  const load = useCallback(() => {
    void refreshDetail(false);
  }, [refreshDetail]);

  useEffect(load, [load]);

  const run = async (
    key: string,
    fn: () => Promise<unknown>,
    successMsg?: string,
    reloadOnError = false,
  ) => {
    setBusy(key);
    try {
      await fn();
      if (successMsg) toast.success(successMsg);
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
      // 상태 전이가 게이트(409)로 거부된 경우 BE가 이미 승인 인스턴스를 생성했을 수 있으므로,
      // 전체 로딩 화면 없이 조용히 다시 조회해 승인 패널에 반영한다.
      if (reloadOnError) refreshDetail(true);
    } finally {
      setBusy(null);
    }
  };

  if (loading) return <FullscreenLoader />;
  if (notFound || !detail) {
    return (
      <div className="mx-auto max-w-lg space-y-4 text-center">
        <p className="text-sm text-muted-foreground">변경 요청을 찾을 수 없습니다.</p>
        <Button onClick={() => navigate(-1)}>이전으로</Button>
      </div>
    );
  }

  const transitions = detail.allowedTransitions ?? fallbackTransitions(detail.status);
  const approved = detail.approval.approvalRequestId == null || detail.approval.status === "APPROVED";

  return (
    <TicketDetailLayout
      ticketKey={detail.ticketKey}
      title={detail.summary}
      badges={
        <>
          <StatusBadge tone={typeTone(detail.type)} label={typeLabel(detail.type)} />
          <StatusBadge tone={statusTone(detail.status)} label={statusLabel(detail.status)} />
          {detail.risk ? (
            <StatusBadge tone={riskTone(detail.risk)} label={riskLabel(detail.risk)} />
          ) : (
            <StatusBadge tone="muted" label="위험도 미평가" />
          )}
        </>
      }
      actions={transitions.map((t) => {
        const blocked = t === "IMPLEMENTATION" && !approved;
        return (
          <Button
            key={t}
            loading={busy === `st-${t}`}
            disabled={blocked}
            title={blocked ? "승인 완료 전에는 구현 단계로 전이할 수 없습니다" : undefined}
            onClick={() =>
              run(`st-${t}`, () => changeApi.transition(id, t), `상태가 '${statusLabel(t)}'로 변경되었습니다`, true)
            }
          >
            {statusLabel(t)}
          </Button>
        );
      })}
      meta={
        <>
          <Card>
            <CardHeader><CardTitle className="text-base">구현·롤백 계획</CardTitle></CardHeader>
            <CardContent className="space-y-1.5 text-sm">
              <MetaRow label="구현 계획" value={detail.implementationPlan || "-"} />
              <MetaRow label="롤백 계획" value={detail.rollbackPlan || "-"} />
            </CardContent>
          </Card>

          <ApprovalPanel
            matched={detail.approval.approvalRequestId != null}
            steps={approvalSteps}
            currentStepNo={approvalCurrentStepNo}
            emptyMessage="이 변경에는 승인 절차가 없습니다"
          />

          <Card>
            <CardHeader><CardTitle className="text-base">연계 항목</CardTitle></CardHeader>
            <CardContent className="space-y-1.5 text-sm">
              {detail.links.length === 0 ? (
                <p className="text-muted-foreground">연결 없음</p>
              ) : (
                detail.links.map((l, i) => (
                  <span key={i} className="block text-foreground">{l.type} · {l.targetKey}</span>
                ))
              )}
            </CardContent>
          </Card>
        </>
      }
    >
      <Card>
        <CardHeader><CardTitle className="text-base">설명</CardTitle></CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap text-sm text-foreground">{detail.description || "설명 없음"}</p>
        </CardContent>
      </Card>

      <ResultCard
        detail={detail}
        approved={approved}
        busy={busy === "result"}
        onSubmit={(body) => run("result", () => changeApi.recordResult(id, body), "구현 결과가 기록되었습니다")}
      />

      <LinkCard
        busy={busy === "link"}
        onLink={(targetType, targetId) =>
          run("link", () => changeApi.link(id, { targetType, targetId }), "연계되었습니다")
        }
      />
    </TicketDetailLayout>
  );
}

function MetaRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-2">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right text-foreground">{value}</span>
    </div>
  );
}

function ResultCard({
  detail,
  approved,
  busy,
  onSubmit,
}: {
  detail: ChangeDetail;
  approved: boolean;
  busy: boolean;
  onSubmit: (body: { outcome: Outcome; rolledBack: boolean; note?: string }) => void;
}) {
  const [outcome, setOutcome] = useState<Outcome>(detail.result.outcome ?? "SUCCESS");
  const [rolledBack, setRolledBack] = useState(detail.result.rolledBack);
  const [note, setNote] = useState(detail.result.note ?? "");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSubmit({ outcome, rolledBack, note: note.trim() || undefined });
  };

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">구현 결과 기록</CardTitle></CardHeader>
      <CardContent>
        {!approved ? (
          <p className="mb-3 text-sm text-muted-foreground">승인 완료 전에는 구현 결과를 기록할 수 없습니다.</p>
        ) : null}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>결과</Label>
              <Select value={outcome} onValueChange={(v) => setOutcome(v as Outcome)} disabled={!approved}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="SUCCESS">성공</SelectItem>
                  <SelectItem value="FAILURE">실패</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end gap-2 pb-2">
              <Checkbox id="rb" checked={rolledBack} onCheckedChange={(v) => setRolledBack(!!v)} disabled={!approved} />
              <Label htmlFor="rb">롤백 여부</Label>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="rn">비고</Label>
            <Textarea id="rn" value={note} onChange={(e) => setNote(e.target.value)} rows={2} disabled={!approved} />
          </div>
          <div className="flex justify-end">
            <Button type="submit" loading={busy} disabled={!approved}>결과 저장</Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function LinkCard({
  busy,
  onLink,
}: {
  busy: boolean;
  onLink: (targetType: LinkTargetType, targetId: number) => void;
}) {
  const [targetType, setTargetType] = useState<LinkTargetType>("INCIDENT");
  const [targetId, setTargetId] = useState("");

  return (
    <Card>
      <CardHeader><CardTitle className="text-base">인시던트 / 문제 연계</CardTitle></CardHeader>
      <CardContent>
        <form
          className="flex flex-wrap items-end gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (!targetId) return;
            onLink(targetType, Number(targetId));
            setTargetId("");
          }}
        >
          <div className="space-y-1.5">
            <Label>연계 대상</Label>
            <Select value={targetType} onValueChange={(v) => setTargetType(v as LinkTargetType)}>
              <SelectTrigger className="w-36"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="INCIDENT">인시던트</SelectItem>
                <SelectItem value="PROBLEM">문제</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="tid">대상 ID</Label>
            <Input id="tid" type="number" className="w-40" value={targetId} onChange={(e) => setTargetId(e.target.value)} />
          </div>
          <Button type="submit" loading={busy} disabled={!targetId}>연계</Button>
        </form>
      </CardContent>
    </Card>
  );
}
